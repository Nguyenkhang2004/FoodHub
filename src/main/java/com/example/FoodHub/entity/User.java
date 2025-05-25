package com.example.FoodHub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Size(max = 255)
    @Column(name = "address")
    private String address;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "registration_date", nullable = false)
    private Instant registrationDate = Instant.now();

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_auth_user", nullable = false)
    private Boolean isAuthUser = false;

    @Size(max = 50)
    @Column(name = "oauth_provider", length = 50)
    private String oauthProvider;

    @Column(name = "last_login")
    private Instant lastLogin;

    @OneToMany(mappedBy = "user")
    private Set<Booking> bookings = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<RestaurantOrder> restaurantOrders = new LinkedHashSet<>();

    @Size(max = 255)
    @NotNull
    @Column(name = "username", nullable = false)
    private String username;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_name")
    private Role roleName;

}