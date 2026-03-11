package com.milk_ecommerce_backend.service;

import com.milk_ecommerce_backend.exception.NotFoundException;
import com.milk_ecommerce_backend.model.Cart;
import com.milk_ecommerce_backend.model.Product;
import com.milk_ecommerce_backend.model.User;
import com.milk_ecommerce_backend.repository.CartRepository;
import com.milk_ecommerce_backend.repository.ProductRepository;
import com.milk_ecommerce_backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // ADD TO CART (JWT user email)
    public Cart addByEmail(String email, Long productId, Integer quantity) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        int qty = (quantity == null || quantity < 1) ? 1 : quantity;

        Cart cart = cartRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElse(null);

        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(qty);
        } else {
            int oldQty = (cart.getQuantity() == null) ? 0 : cart.getQuantity();
            cart.setQuantity(oldQty + qty);
        }

        BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(cart.getQuantity()));
        cart.setTotalPrice(totalPrice);

        return cartRepository.save(cart);
    }

    // GET MY CART
    public List<Cart> myCartByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return cartRepository.findByUserId(user.getId());
    }

    // REMOVE FROM CART
    public void removeByEmail(String email, Long cartId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (cart.getUser() == null ||
            cart.getUser().getId() == null ||
            !cart.getUser().getId().equals(user.getId())) {

            throw new RuntimeException("Not allowed");
        }

        cartRepository.delete(cart);
    }
 // ✅ UPDATE QTY (JWT email safety)
    public Cart updateQtyByEmail(String email, Long cartId, Integer quantity) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (cart.getUser() == null || cart.getUser().getId() == null ||
                !cart.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not allowed");
        }

        int qty = (quantity == null || quantity < 1) ? 1 : quantity;

        // optional: stock check (recommended)
        Product product = cart.getProduct();
        if (product == null || product.getId() == null) {
            throw new RuntimeException("Invalid cart item: product missing");
        }

        Product fresh = productRepository.findById(product.getId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Integer stock = fresh.getStock();
        if (stock != null && stock > 0 && qty > stock) {
            throw new RuntimeException("Insufficient stock for product: " + fresh.getName());
        }

        cart.setQuantity(qty);

        BigDecimal price = (fresh.getPrice() != null) ? fresh.getPrice() : BigDecimal.ZERO;
        cart.setTotalPrice(price.multiply(BigDecimal.valueOf(qty)));

        return cartRepository.save(cart);
    }
}