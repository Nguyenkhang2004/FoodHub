package com.example.FoodHub.controller;

import com.example.FoodHub.entity.Cart;
import com.example.FoodHub.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public List<Cart> getCart(Authentication authentication) {
        return cartService.getCartByUser(authentication.getName());
    }

    @PostMapping
    public Cart addItemToCart(
            Authentication authentication,
            @RequestParam Integer menuItemId,
            @RequestParam Integer quantity) {
        return cartService.addItemToCart(authentication.getName(), menuItemId, quantity);
    }

    @PutMapping("/{cartId}")
    public Cart updateCartItem(
            Authentication authentication,
            @PathVariable Integer cartId,
            @RequestParam Integer quantity) {
        return cartService.updateCartItem(authentication.getName(), cartId, quantity);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> removeCartItem(
            Authentication authentication,
            @PathVariable Integer cartId) {
        cartService.removeCartItem(authentication.getName(), cartId);
        return ResponseEntity.noContent().build();
    }
}