package com.example.FoodHub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "branch")
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "address", nullable = false)
    private String address;

    @OneToMany(mappedBy = "branch")
    private Set<Booking> bookings = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch")
    private Set<ChatMessage> chatMessages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch")
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch")
    private Set<RestaurantOrder> restaurantOrders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch")
    private Set<RestaurantTable> restaurantTables = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch")
    private Set<User> users = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch")
    private Set<MenuItemBranch> menuItemBranches = new LinkedHashSet<>();

}