package com.example.financeapp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity quản lý ngân sách chi tiêu
 * Cho phép user thiết lập giới hạn chi tiêu theo category và thời gian
 */
@Entity
@Table(
    name = "budgets",
    indexes = {
        @Index(name = "idx_user_wallet", columnList = "user_id,wallet_id"),
        @Index(name = "idx_period", columnList = "start_date,end_date"),
        @Index(name = "idx_category", columnList = "category_id"),
        @Index(name = "idx_active", columnList = "is_active")
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uk_budget_period_category",
        columnNames = {"user_id", "wallet_id", "category_id", "start_date", "end_date"}
    )
)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long budgetId;

    // ===== USER & WALLET =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    // ===== CATEGORY =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // ===== BUDGET INFO =====
    @Column(name = "budget_name", nullable = false, length = 100)
    private String budgetName;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // Số tiền ngân sách

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    // ===== PERIOD =====
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // ===== TRACKING =====
    @Column(name = "spent_amount", precision = 15, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO; // Số tiền đã chi

    @Column(name = "remaining_amount", precision = 15, scale = 2)
    private BigDecimal remainingAmount; // Số tiền còn lại

    @Column(name = "percentage_used", precision = 5, scale = 2)
    private BigDecimal percentageUsed = BigDecimal.ZERO; // % đã dùng

    // ===== ALERT SETTINGS =====
    @Column(name = "alert_threshold", precision = 5, scale = 2)
    private BigDecimal alertThreshold = new BigDecimal("80.00"); // Cảnh báo khi đạt 80%

    @Column(name = "alert_sent")
    private Boolean alertSent = false; // Đã gửi cảnh báo chưa

    // ===== RECURRING =====
    @Column(name = "is_recurring")
    private Boolean isRecurring = false; // Ngân sách lặp lại hàng tháng

    @Column(name = "recurrence_type", length = 20)
    private String recurrenceType; // MONTHLY, QUARTERLY, YEARLY

    // ===== STATUS =====
    @Column(name = "is_active")
    private Boolean isActive = true; // Ngân sách còn hiệu lực không

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ===== TIMESTAMPS =====
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ===== LIFECYCLE HOOKS =====
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        updateRemainingAmount();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateRemainingAmount();
    }

    // ===== BUSINESS METHODS =====

    /**
     * Cập nhật số tiền còn lại và phần trăm đã dùng
     */
    public void updateRemainingAmount() {
        if (this.spentAmount == null) {
            this.spentAmount = BigDecimal.ZERO;
        }
        
        this.remainingAmount = this.amount.subtract(this.spentAmount);
        
        // Tính phần trăm
        if (this.amount.compareTo(BigDecimal.ZERO) > 0) {
            this.percentageUsed = this.spentAmount
                .multiply(new BigDecimal("100"))
                .divide(this.amount, 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Kiểm tra ngân sách có đang active không (trong khoảng thời gian)
     */
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return this.isActive && 
               !today.isBefore(this.startDate) && 
               !today.isAfter(this.endDate);
    }

    /**
     * Kiểm tra có vượt ngân sách không
     */
    public boolean isOverBudget() {
        return this.spentAmount.compareTo(this.amount) > 0;
    }

    /**
     * Kiểm tra có nên gửi cảnh báo không
     */
    public boolean shouldSendAlert() {
        return !this.alertSent && 
               this.percentageUsed.compareTo(this.alertThreshold) >= 0;
    }

    /**
     * Kiểm tra ngân sách đã hết hạn chưa
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(this.endDate);
    }

    // ===== GETTERS & SETTERS =====

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
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

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
        updateRemainingAmount();
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public BigDecimal getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(BigDecimal percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public BigDecimal getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(BigDecimal alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public Boolean getAlertSent() {
        return alertSent;
    }

    public void setAlertSent(Boolean alertSent) {
        this.alertSent = alertSent;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

