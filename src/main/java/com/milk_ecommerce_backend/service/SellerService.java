package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.dto.SellerApplyRequest;
import com.milk_ecommerce_backend.dto.seller.LowStockItemDto;
import com.milk_ecommerce_backend.dto.seller.RecentSellerOrderDto;
import com.milk_ecommerce_backend.dto.seller.SellerDashboardResponse;
import com.milk_ecommerce_backend.exception.NotFoundException;
import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.model.OrderItem;
import com.milk_ecommerce_backend.model.Product;
import com.milk_ecommerce_backend.model.Role;
import com.milk_ecommerce_backend.model.Seller;
import com.milk_ecommerce_backend.model.User;
import com.milk_ecommerce_backend.repository.OrderItemRepository;
import com.milk_ecommerce_backend.repository.ProductRepository;
import com.milk_ecommerce_backend.repository.RoleRepository;
import com.milk_ecommerce_backend.repository.SellerRepository;
import com.milk_ecommerce_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public SellerService(
            SellerRepository sellerRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmailService emailService,
            ProductRepository productRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Seller apply(String email, SellerApplyRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (sellerRepository.existsByUser_Id(user.getId())) {
            throw new RuntimeException("Seller already applied");
        }

        Seller s = new Seller();
        s.setUser(user);
        s.setShopName(req.getShopName().trim());
        s.setGstNumber(req.getGstNumber() != null ? req.getGstNumber().trim() : null);
        s.setApprovalStatus("PENDING");
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());

        return sellerRepository.save(s);
    }

    @Transactional(readOnly = true)
    public List<Seller> pending() {
        return sellerRepository.findByApprovalStatusWithUser("PENDING");
    }

    @Transactional(readOnly = true)
    public List<Seller> all() {
        return sellerRepository.findAllWithUser();
    }

    @Transactional
    public Seller approve(Long sellerId) {
        Seller s = sellerRepository.findByIdWithUser(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        s.setApprovalStatus("APPROVED");
        s.setApprovedDate(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());

        User user = s.getUser();

        Role sellerRole = roleRepository.findByRoleName("SELLER")
                .orElseThrow(() -> new RuntimeException("SELLER role not found"));

        user.setRole(sellerRole);
        userRepository.save(user);

        Seller savedSeller = sellerRepository.save(s);

        try {
            String displayName = user.getName() != null && !user.getName().isBlank()
                    ? user.getName().trim()
                    : "User";

            emailService.sendSellerApprovedEmail(
                    user.getEmail(),
                    displayName,
                    savedSeller.getShopName()
            );
        } catch (Exception ex) {
            System.err.println("Seller approved email failed: " + ex.getMessage());
        }

        return savedSeller;
    }

    @Transactional
    public Seller reject(Long sellerId) {
        Seller s = sellerRepository.findByIdWithUser(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        s.setApprovalStatus("REJECTED");
        s.setUpdatedAt(LocalDateTime.now());

        Seller savedSeller = sellerRepository.save(s);

        try {
            User user = savedSeller.getUser();
            String displayName = user.getName() != null && !user.getName().isBlank()
                    ? user.getName().trim()
                    : "User";

            emailService.sendSellerRejectedEmail(
                    user.getEmail(),
                    displayName,
                    savedSeller.getShopName()
            );
        } catch (Exception ex) {
            System.err.println("Seller rejected email failed: " + ex.getMessage());
        }

        return savedSeller;
    }

    @Transactional(readOnly = true)
    public SellerDashboardResponse getDashboard(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("User email missing");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = "";
        if (user.getRole() != null && user.getRole().getRoleName() != null) {
            role = user.getRole().getRoleName().trim().toUpperCase();
        }
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        if (!"SELLER".equals(role)) {
            throw new RuntimeException("Only seller can access dashboard");
        }

        Seller seller = sellerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Seller record not found"));

        SellerDashboardResponse res = new SellerDashboardResponse();
        res.setSellerId(seller.getId());
        res.setShopName(seller.getShopName());
        res.setApprovalStatus(seller.getApprovalStatus());
        res.setRevenue(BigDecimal.ZERO);

        List<Product> myProducts = productRepository.findAll().stream()
                .filter(p -> p.getSeller() != null && p.getSeller().getId() != null)
                .filter(p -> seller.getId().equals(p.getSeller().getId()))
                .collect(Collectors.toList());

        res.setProducts(myProducts.size());

        List<LowStockItemDto> lowStock = myProducts.stream()
                .filter(p -> {
                    Integer stock = p.getStock();
                    return stock != null && stock <= 5;
                })
                .sorted(Comparator.comparingInt(p -> p.getStock() == null ? 0 : p.getStock()))
                .map(p -> new LowStockItemDto(
                        p.getId(),
                        p.getName(),
                        p.getStock()
                ))
                .collect(Collectors.toList());

        res.setLowStock(lowStock);

        List<OrderItem> sellerItems = new ArrayList<>();
        for (Product p : myProducts) {
            if (p.getId() == null) {
                continue;
            }
            sellerItems.addAll(orderItemRepository.findByProduct_Id(p.getId()));
        }

        List<Long> uniqueOrderIds = sellerItems.stream()
                .filter(i -> i.getOrder() != null && i.getOrder().getId() != null)
                .map(i -> i.getOrder().getId())
                .distinct()
                .collect(Collectors.toList());

        res.setOrders(uniqueOrderIds.size());

        BigDecimal totalRevenue = sellerItems.stream()
                .map(i -> {
                    BigDecimal price = i.getPrice() == null ? BigDecimal.ZERO : i.getPrice();
                    int qty = i.getQuantity() == null ? 0 : i.getQuantity();
                    return price.multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        res.setRevenue(totalRevenue);

        List<RecentSellerOrderDto> recentOrders = sellerItems.stream()
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getCreatedAt();
                    LocalDateTime tb = b.getCreatedAt();

                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;
                    if (tb == null) return -1;

                    return tb.compareTo(ta);
                })
                .limit(7)
                .map(this::toRecentOrderDto)
                .collect(Collectors.toList());

        res.setRecentOrders(recentOrders);

        return res;
    }

    private RecentSellerOrderDto toRecentOrderDto(OrderItem item) {
        RecentSellerOrderDto dto = new RecentSellerOrderDto();

        Order order = item.getOrder();

        if (order != null) {
            dto.setOrderId(order.getId());
            dto.setOrderStatus(order.getOrderStatus());
            dto.setOrderDate(order.getOrderDate() != null ? order.getOrderDate().toString() : null);
        }

        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());

        if (item.getProduct() != null) {
            dto.setProduct(new RecentSellerOrderDto.ProductMiniDto(
                    item.getProduct().getId(),
                    item.getProduct().getName()
            ));
        }

        return dto;
    }
}