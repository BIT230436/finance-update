package com.example.financeapp.dto;

import java.math.BigDecimal;

/**
 * DTO cho cảnh báo ngân sách
 */
public class BudgetAlertDTO {

    private Long budgetId;
    private String budgetName;
    private String categoryName;
    private BigDecimal amount;
    private BigDecimal spentAmount;
    private BigDecimal percentageUsed;
    private String alertType; // "WARNING", "DANGER", "EXCEEDED"
    private String message;

    // ===== CONSTRUCTORS =====
    public BudgetAlertDTO() {
    }

    public BudgetAlertDTO(Long budgetId, String budgetName, String categoryName, 
                          BigDecimal amount, BigDecimal spentAmount, BigDecimal percentageUsed,
                          String alertType, String message) {
        this.budgetId = budgetId;
        this.budgetName = budgetName;
        this.categoryName = categoryName;
        this.amount = amount;
        this.spentAmount = spentAmount;
        this.percentageUsed = percentageUsed;
        this.alertType = alertType;
        this.message = message;
    }

    // ===== GETTERS & SETTERS =====

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public String getBudgetName() {
        return budgetName;
    }

    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(BigDecimal percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

