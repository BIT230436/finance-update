package com.example.financeapp.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho request tạo ngân sách mới
 */
public class CreateBudgetRequest {

    @NotNull(message = "Wallet ID không được để trống")
    private Long walletId;

    @NotNull(message = "Category ID không được để trống")
    private Long categoryId;

    @NotBlank(message = "Tên ngân sách không được để trống")
    @Size(max = 100, message = "Tên ngân sách không được vượt quá 100 ký tự")
    private String budgetName;

    @NotNull(message = "Số tiền ngân sách không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền ngân sách phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @DecimalMin(value = "0", message = "Ngưỡng cảnh báo phải từ 0 trở lên")
    @DecimalMax(value = "100", message = "Ngưỡng cảnh báo không được vượt quá 100")
    private BigDecimal alertThreshold = new BigDecimal("80.00"); // Default 80%

    private Boolean isRecurring = false;

    @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY|", message = "Loại lặp lại phải là MONTHLY, QUARTERLY hoặc YEARLY")
    private String recurrenceType;

    private String notes;

    // ===== CONSTRUCTORS =====
    public CreateBudgetRequest() {
    }

    // ===== GETTERS & SETTERS =====

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

