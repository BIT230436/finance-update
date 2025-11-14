package com.example.financeapp.service;

import com.example.financeapp.dto.*;
import com.example.financeapp.entity.Budget;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface cho quản lý ngân sách
 */
public interface BudgetService {

    /**
     * Tạo ngân sách mới
     */
    Budget createBudget(Long userId, CreateBudgetRequest request);

    /**
     * Cập nhật ngân sách
     */
    Budget updateBudget(Long userId, Long budgetId, UpdateBudgetRequest request);

    /**
     * Xóa ngân sách
     */
    void deleteBudget(Long userId, Long budgetId);

    /**
     * Lấy chi tiết ngân sách
     */
    BudgetDTO getBudgetDetails(Long userId, Long budgetId);

    /**
     * Lấy tất cả ngân sách của user
     */
    List<BudgetDTO> getAllBudgets(Long userId);

    /**
     * Lấy ngân sách đang active
     */
    List<BudgetDTO> getActiveBudgets(Long userId);

    /**
     * Lấy ngân sách theo wallet
     */
    List<BudgetDTO> getBudgetsByWallet(Long userId, Long walletId);

    /**
     * Lấy ngân sách theo category
     */
    List<BudgetDTO> getBudgetsByCategory(Long userId, Long categoryId);

    /**
     * Lấy tổng quan ngân sách
     */
    BudgetSummaryDTO getBudgetSummary(Long userId);

    /**
     * Lấy tổng quan ngân sách theo wallet
     */
    BudgetSummaryDTO getBudgetSummaryByWallet(Long userId, Long walletId);

    /**
     * Cập nhật số tiền đã chi (gọi từ TransactionService)
     */
    void updateSpentAmount(Long budgetId);

    /**
     * Cập nhật spent amount cho tất cả budgets liên quan đến transaction
     */
    void updateSpentAmountsForTransaction(Long walletId, Long categoryId, LocalDate transactionDate);

    /**
     * Tính toán lại spent amount cho budget
     */
    void recalculateSpentAmount(Long budgetId);

    /**
     * Kiểm tra và gửi cảnh báo nếu cần
     */
    List<BudgetAlertDTO> checkAndSendAlerts(Long userId);

    /**
     * Tự động tạo budget mới cho các recurring budgets
     */
    void renewRecurringBudgets();

    /**
     * Vô hiệu hóa các budget đã hết hạn
     */
    void deactivateExpiredBudgets();
}

