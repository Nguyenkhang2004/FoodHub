package com.example.FoodHub.repository;

import com.example.FoodHub.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByStatus(String status);
}