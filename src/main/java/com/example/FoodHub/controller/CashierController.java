package com.example.FoodHub.controller;

import com.example.FoodHub.entity.Payment;
import com.example.FoodHub.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cashier")
@PreAuthorize("hasRole('CASHIER')")
@RequiredArgsConstructor
public class CashierController {

    private final PaymentService paymentService;


    @GetMapping("/payments")
    public String showPayments(Model model) {
        model.addAttribute("payments", paymentService.getPendingPayments());
        model.addAttribute("newPayment", new Payment());
        return "cashier/payments";
    }

    @PostMapping("/payments/process")
    public String processPayment(@ModelAttribute Payment payment, Model model) {
        paymentService.processPayment(payment);
        model.addAttribute("payments", paymentService.getPendingPayments());
        return "redirect:/cashier/payments";
    }

    @GetMapping("/transactions")
    public String showTransactions(Model model) {
        model.addAttribute("transactions", paymentService.getAllTransactions());
        return "cashier/transactions";
    }

    @PostMapping("/transactions/{paymentId}/refund")
    public String refundTransaction(@PathVariable Integer paymentId, Model model) {
        paymentService.refundPayment(paymentId);
        model.addAttribute("transactions", paymentService.getAllTransactions());
        return "redirect:/cashier/transactions";
    }
}