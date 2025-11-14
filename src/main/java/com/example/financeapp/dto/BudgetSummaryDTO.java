package com.example.financeapp.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tổng hợp thông tin ngân sách
 */
public class BudgetSummaryDTO {

    private Integer totalBudgets;
    private Integer activeBudgets;
    private Integer expiredBudgets;
    private Integer overBudgets;
    private BigDecimal totalBudgetAmount;
    private BigDecimal totalSpentAmount;
    private BigDecimal totalRemainingAmount;
    private BigDecimal overallPercentageUsed;
    private String currencyCode;
    private List<BudgetDTO> budgets;
    private List<BudgetAlertDTO> alerts;

    // ===== CONSTRUCTORS =====
    public BudgetSummaryDTO() {
    }

    // ===== GETTERS & SETTERS =====

    public Integer getTotalBudgets() {
        return totalBudgets;
    }

    public void setTotalBudgets(Integer totalBudgets) {
        this.totalBudgets = totalBudgets;
    }

    public Integer getActiveBudgets() {
        return activeBudgets;
    }

    public void setActiveBudgets(Integer activeBudgets) {
        this.activeBudgets = activeBudgets;
    }

    public Integer getExpiredBudgets() {
        return expiredBudgets;
    }

    public void setExpiredBudgets(Integer expiredBudgets) {
        this.expiredBudgets = expiredBudgets;
    }

    public Integer getOverBudgets() {
        return overBudgets;
    }

    public void setOverBudgets(Integer overBudgets) {
        this.overBudgets = overBudgets;
    }

    public BigDecimal getTotalBudgetAmount() {
        return totalBudgetAmount;
    }

    public void setTotalBudgetAmount(BigDecimal totalBudgetAmount) {
        this.totalBudgetAmount = totalBudgetAmount;
    }

    public BigDecimal getTotalSpentAmount() {
        return totalSpentAmount;
    }

    public void setTotalSpentAmount(BigDecimal totalSpentAmount) {
        this.totalSpentAmount = totalSpentAmount;
    }

    public BigDecimal getTotalRemainingAmount() {
        return totalRemainingAmount;
    }

    public void setTotalRemainingAmount(BigDecimal totalRemainingAmount) {
        this.totalRemainingAmount = totalRemainingAmount;
    }

    public BigDecimal getOverallPercentageUsed() {
        return overallPercentageUsed;
    }

    public void setOverallPercentageUsed(BigDecimal overallPercentageUsed) {
        this.overallPercentageUsed = overallPercentageUsed;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public List<BudgetDTO> getBudgets() {
        return budgets;
    }

    public void setBudgets(List<BudgetDTO> budgets) {
        this.budgets = budgets;
    }

    public List<BudgetAlertDTO> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<BudgetAlertDTO> alerts) {
        this.alerts = alerts;
    }
}

