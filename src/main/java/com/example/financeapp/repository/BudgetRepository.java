package com.example.financeapp.repository;

import com.example.financeapp.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Lấy tất cả ngân sách của user
     */
    List<Budget> findByUser_UserId(Long userId);

    /**
     * Lấy ngân sách active của user
     */
    List<Budget> findByUser_UserIdAndIsActiveTrue(Long userId);

    /**
     * Lấy ngân sách của wallet
     */
    List<Budget> findByWallet_WalletId(Long walletId);

    /**
     * Lấy ngân sách của user và wallet
     */
    List<Budget> findByUser_UserIdAndWallet_WalletId(Long userId, Long walletId);

    /**
     * Lấy ngân sách theo category
     */
    List<Budget> findByUser_UserIdAndCategory_CategoryId(Long userId, Long categoryId);

    /**
     * Tìm ngân sách trong khoảng thời gian
     */
    @Query("SELECT b FROM Budget b WHERE b.user.userId = :userId " +
           "AND b.startDate <= :date AND b.endDate >= :date")
    List<Budget> findActiveBudgetsOnDate(
        @Param("userId") Long userId, 
        @Param("date") LocalDate date
    );

    /**
     * Tìm ngân sách đang active (trong period hiện tại)
     */
    @Query("SELECT b FROM Budget b WHERE b.user.userId = :userId " +
           "AND b.isActive = true " +
           "AND b.startDate <= CURRENT_DATE AND b.endDate >= CURRENT_DATE")
    List<Budget> findCurrentActiveBudgets(@Param("userId") Long userId);

    /**
     * Tìm ngân sách theo category trong kỳ cụ thể
     */
    @Query("SELECT b FROM Budget b WHERE b.user.userId = :userId " +
           "AND b.wallet.walletId = :walletId " +
           "AND b.category.categoryId = :categoryId " +
           "AND ((b.startDate <= :endDate AND b.endDate >= :startDate))")
    List<Budget> findOverlappingBudgets(
        @Param("userId") Long userId,
        @Param("walletId") Long walletId,
        @Param("categoryId") Long categoryId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Tìm ngân sách cần gửi cảnh báo
     */
    @Query("SELECT b FROM Budget b WHERE b.isActive = true " +
           "AND b.alertSent = false " +
           "AND b.percentageUsed >= b.alertThreshold " +
           "AND b.startDate <= CURRENT_DATE AND b.endDate >= CURRENT_DATE")
    List<Budget> findBudgetsNeedingAlert();

    /**
     * Tìm ngân sách đã vượt
     */
    @Query("SELECT b FROM Budget b WHERE b.user.userId = :userId " +
           "AND b.spentAmount > b.amount")
    List<Budget> findOverBudgets(@Param("userId") Long userId);

    /**
     * Tìm ngân sách sắp hết hạn (trong X ngày tới)
     */
    @Query("SELECT b FROM Budget b WHERE b.user.userId = :userId " +
           "AND b.isActive = true " +
           "AND b.endDate BETWEEN CURRENT_DATE AND :expiryDate")
    List<Budget> findExpiringBudgets(
        @Param("userId") Long userId,
        @Param("expiryDate") LocalDate expiryDate
    );

    /**
     * Tìm ngân sách recurring cần tạo mới cho kỳ tiếp theo
     */
    @Query("SELECT b FROM Budget b WHERE b.isRecurring = true " +
           "AND b.endDate < CURRENT_DATE " +
           "AND b.isActive = true")
    List<Budget> findRecurringBudgetsToRenew();

    /**
     * Kiểm tra budget có tồn tại không (để tránh duplicate)
     */
    boolean existsByUser_UserIdAndWallet_WalletIdAndCategory_CategoryIdAndStartDateAndEndDate(
        Long userId, Long walletId, Long categoryId, LocalDate startDate, LocalDate endDate
    );

    /**
     * Đếm số ngân sách active của user
     */
    long countByUser_UserIdAndIsActiveTrue(Long userId);

    /**
     * Tìm budget và kiểm tra quyền truy cập
     */
    Optional<Budget> findByBudgetIdAndUser_UserId(Long budgetId, Long userId);
}

