package com.example.FoodHub.service;

import com.example.FoodHub.entity.Cart;
import com.example.FoodHub.entity.MenuItem;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.repository.CartRepository;
import com.example.FoodHub.repository.MenuItemRepository;
import com.example.FoodHub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<Cart> getCartByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cartRepository.findByUser(user);
    }

    @Transactional
    public Cart addItemToCart(String email, Integer menuItemId, Integer quantity) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        Cart existingCart = cartRepository.findByUserAndMenuItemId(user, menuItemId)
                .orElse(null);
        if (existingCart != null) {
            existingCart.setQuantity(existingCart.getQuantity() + quantity);
            return cartRepository.save(existingCart);
        }

        Cart cart = Cart.builder()
                .user(user)
                .menuItem(menuItem)
                .quantity(quantity)
                .addedAt(Instant.now())
                .build();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateCartItem(String email, Integer cartId, Integer quantity) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cart.getUser().equals(user)) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        if (quantity <= 0) {
            cartRepository.delete(cart);
            return null;
        }

        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public void removeCartItem(String email, Integer cartId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cart.getUser().equals(user)) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        cartRepository.delete(cart);
    }
}