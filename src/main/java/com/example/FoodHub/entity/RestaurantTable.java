package com.example.FoodHub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "restaurant_table")
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "table_number", nullable = false, length = 50)
    private String tableNumber;

    @Size(max = 255)
    @NotNull
    @Column(name = "qr_code", nullable = false)
    private String qrCode;

    @Size(max = 10)
    @NotNull
    @Column(name = "area", nullable = false, length = 10)
    private String area;

    @NotNull
    @ColumnDefault("'AVAILABLE'")
    @Column(name = "status", nullable = false)
    private String status;

    @Size(max = 512)
    @Column(name = "current_token")
    private String currentToken;

    @Column(name = "token_expiry")
    private Instant tokenExpiry;

}