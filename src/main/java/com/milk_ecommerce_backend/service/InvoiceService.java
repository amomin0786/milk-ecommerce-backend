package com.milk_ecommerce_backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.milk_ecommerce_backend.model.Order;
import com.milk_ecommerce_backend.model.OrderItem;
import com.milk_ecommerce_backend.repository.OrderItemRepository;
import com.milk_ecommerce_backend.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    // ✅ keep session open for lazy properties (extra safe)
    @Transactional(readOnly = true)
    public byte[] generateInvoice(Long orderId) {

        // ✅ IMPORTANT: fetch user with order
        Order order = orderRepository.findByIdWithUser(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Milk Marketplace Invoice");
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            String email = (order.getUser() != null && order.getUser().getEmail() != null)
                    ? order.getUser().getEmail()
                    : "Customer";

            document.add(new Paragraph("Order ID: " + order.getId()));
            document.add(new Paragraph("Customer: " + email));
            document.add(new Paragraph("Payment Method: " + (order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD")));
            document.add(new Paragraph("Status: " + (order.getOrderStatus() != null ? order.getOrderStatus() : "PENDING")));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            table.addCell("Product");
            table.addCell("Price");
            table.addCell("Qty");
            table.addCell("Subtotal");

            if (items != null && !items.isEmpty()) {
                for (OrderItem item : items) {

                    String name = (item.getProduct() != null && item.getProduct().getName() != null)
                            ? item.getProduct().getName()
                            : "Product";

                    BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    int qty = item.getQuantity() != null ? item.getQuantity() : 0;

                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));

                    table.addCell(name);
                    table.addCell("₹ " + price);
                    table.addCell(String.valueOf(qty));
                    table.addCell("₹ " + subtotal);
                }
            } else {
                table.addCell("No items");
                table.addCell("-");
                table.addCell("-");
                table.addCell("-");
            }

            document.add(table);

            BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total Amount: ₹ " + total));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Invoice generation failed: " + e.getMessage(), e);
        }
    }
}