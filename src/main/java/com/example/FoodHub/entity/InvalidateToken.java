package com.example.FoodHub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "invalidate_token")
public class InvalidateToken {
    @Id
    @Size(max = 512)
    @Column(name = "token", nullable = false, length = 512)
    private String token;

    @NotNull
    @Column(name = "expiry_time", nullable = false)
    private Instant expiryTime;

}