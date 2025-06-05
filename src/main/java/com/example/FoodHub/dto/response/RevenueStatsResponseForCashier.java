package com.example.FoodHub.dto.response;

import java.math.BigDecimal;

public class RevenueStatsResponseForCashier {
    private BigDecimal totalRevenue;
    private BigDecimal cashRevenue;
    private BigDecimal vnpayRevenue;
    private BigDecimal pendingRevenue;
    private BigDecimal paidRevenue;
    private BigDecimal cancelledRevenue;

    public RevenueStatsResponseForCashier(BigDecimal totalRevenue, BigDecimal cashRevenue, BigDecimal vnpayRevenue,
                                          BigDecimal pendingRevenue, BigDecimal paidRevenue, BigDecimal cancelledRevenue) {
        this.totalRevenue = totalRevenue;
        this.cashRevenue = cashRevenue;
        this.vnpayRevenue = vnpayRevenue;
        this.pendingRevenue = pendingRevenue;
        this.paidRevenue = paidRevenue;
        this.cancelledRevenue = cancelledRevenue;
    }

    // Getters
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getCashRevenue() {
        return cashRevenue;
    }

    public BigDecimal getVnpayRevenue() {
        return vnpayRevenue;
    }

    public BigDecimal getPendingRevenue() {
        return pendingRevenue;
    }

    public BigDecimal getPaidRevenue() {
        return paidRevenue;
    }

    public BigDecimal getCancelledRevenue() {
        return cancelledRevenue;
    }

    // Setters
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public void setCashRevenue(BigDecimal cashRevenue) {
        this.cashRevenue = cashRevenue;
    }

    public void setVnpayRevenue(BigDecimal vnpayRevenue) {
        this.vnpayRevenue = vnpayRevenue;
    }

    public void setPendingRevenue(BigDecimal pendingRevenue) {
        this.pendingRevenue = pendingRevenue;
    }

    public void setPaidRevenue(BigDecimal paidRevenue) {
        this.paidRevenue = paidRevenue;
    }

    public void setCancelledRevenue(BigDecimal cancelledRevenue) {
        this.cancelledRevenue = cancelledRevenue;
    }
}