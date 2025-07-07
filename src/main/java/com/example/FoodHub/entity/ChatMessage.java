package com.example.FoodHub.entity;

import com.example.FoodHub.utils.TimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @Size(max = 255)
    @NotNull
    @Column(name = "sender", nullable = false)
    private String sender;

    @NotNull
    @Column(name = "message", nullable = false)
    private String message;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp = TimeUtils.getNowInVietNam();

}