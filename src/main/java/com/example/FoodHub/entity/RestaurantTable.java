package com.example.FoodHub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "restaurant_table")
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Size(max = 50)
    @NotNull
    @Column(name = "table_number", nullable = false, length = 50)
    private String tableNumber;

    @Size(max = 255)
    @NotNull
    @Column(name = "qr_code", nullable = false)
    private String qrCode;

    @OneToMany(mappedBy = "table")
    private Set<ChatMessage> chatMessages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "table")
    private Set<RestaurantOrder> restaurantOrders = new LinkedHashSet<>();

    @NotNull
    @ColumnDefault("'AVAILABLE'")
    @Column(name = "status", nullable = false)
    private String status;

}