package com.example.financeapp.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho request cập nhật ngân sách
 */
public class UpdateBudgetRequest {

    @Size(max = 100, message = "Tên ngân sách không được vượt quá 100 ký tự")
    private String budgetName;

    @DecimalMin(value = "0.01", message = "Số tiền ngân sách phải lớn hơn 0")
    private BigDecimal amount;

    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin(value = "0", message = "Ngưỡng cảnh báo phải từ 0 trở lên")
    @DecimalMax(value = "100", message = "Ngưỡng cảnh báo không được vượt quá 100")
    private BigDecimal alertThreshold;

    private Boolean isRecurring;

    @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY|", message = "Loại lặp lại phải là MONTHLY, QUARTERLY hoặc YEARLY")
    private String recurrenceType;

    private Boolean isActive;

    private String notes;

    // ===== CONSTRUCTORS =====
    public UpdateBudgetRequest() {
    }

    // ===== GETTERS & SETTERS =====

    public String getBudgetName() {
        return budgetName;
    }

    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(BigDecimal alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean recurring) {
        isRecurring = recurring;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

