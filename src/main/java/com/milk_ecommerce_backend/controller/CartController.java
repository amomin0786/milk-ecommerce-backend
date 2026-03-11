package com.milk_ecommerce_backend.controller;

import com.milk_ecommerce_backend.model.Cart;
import com.milk_ecommerce_backend.model.CartItemResponse;
import com.milk_ecommerce_backend.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:4200")
public class CartController {

    @Autowired
    private CartService cartService;

    // ✅ ADD: /api/cart/add/{productId}?qty=1
    @PostMapping("/add/{productId}")
    public CartItemResponse add(Authentication auth,
                               @PathVariable Long productId,
                               @RequestParam(defaultValue = "1") Integer qty) {

        String email = auth.getName();
        Cart cart = cartService.addByEmail(email, productId, qty);
        return new CartItemResponse(cart);
    }

    // ✅ MY CART: /api/cart/my
    @GetMapping("/my")
    public List<CartItemResponse> my(Authentication auth) {

        String email = auth.getName();

        return cartService.myCartByEmail(email)
                .stream()
                .map(CartItemResponse::new)
                .toList();
    }

    // ✅ REMOVE: /api/cart/remove/{cartId}
    @DeleteMapping("/remove/{cartId}")
    public String remove(Authentication auth, @PathVariable Long cartId) {
        String email = auth.getName();
        cartService.removeByEmail(email, cartId);
        return "Removed";
    }
 // ✅ UPDATE: /api/cart/update/{cartId}?qty=2
    @PutMapping("/update/{cartId}")
    public CartItemResponse update(Authentication auth,
                                   @PathVariable Long cartId,
                                   @RequestParam(defaultValue = "1") Integer qty) {

        String email = auth.getName();
        Cart cart = cartService.updateQtyByEmail(email, cartId, qty);
        return new CartItemResponse(cart);
    }
}