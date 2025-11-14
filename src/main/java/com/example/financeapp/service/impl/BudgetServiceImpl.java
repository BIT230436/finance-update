package com.example.financeapp.service.impl;

import com.example.financeapp.dto.*;
import com.example.financeapp.entity.*;
import com.example.financeapp.repository.*;
import com.example.financeapp.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletMemberRepository walletMemberRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Override
    @Transactional
    public Budget createBudget(Long userId, CreateBudgetRequest request) {
        // 1. Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // 2. Validate wallet và quyền truy cập
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        if (!walletMemberRepository.existsByWallet_WalletIdAndUser_UserId(request.getWalletId(), userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập ví này");
        }

        // 3. Validate category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        // 4. Kiểm tra category phải là "Chi tiêu"
        if (!"Chi tiêu".equals(category.getTransactionType().getTypeName())) {
            throw new RuntimeException("Chỉ có thể tạo ngân sách cho danh mục Chi tiêu");
        }

        // 5. Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        // 6. Kiểm tra trùng lặp (cùng wallet, category, và period)
        List<Budget> overlapping = budgetRepository.findOverlappingBudgets(
                userId,
                request.getWalletId(),
                request.getCategoryId(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            throw new RuntimeException(
                "Đã tồn tại ngân sách cho danh mục \"" + category.getCategoryName() + 
                "\" trong khoảng thời gian này"
            );
        }

        // 7. Validate recurring settings
        if (Boolean.TRUE.equals(request.getIsRecurring())) {
            if (request.getRecurrenceType() == null || request.getRecurrenceType().isEmpty()) {
                throw new RuntimeException("Vui lòng chọn loại lặp lại cho ngân sách");
            }
        }

        // 8. Tạo budget mới
        Budget budget = new Budget();
        budget.setUser(user);
        budget.setWallet(wallet);
        budget.setCategory(category);
        budget.setBudgetName(request.getBudgetName().trim());
        budget.setAmount(request.getAmount());
        budget.setCurrencyCode(wallet.getCurrencyCode());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setAlertThreshold(request.getAlertThreshold() != null ? 
            request.getAlertThreshold() : new BigDecimal("80.00"));
        budget.setIsRecurring(request.getIsRecurring());
        budget.setRecurrenceType(request.getRecurrenceType());
        budget.setNotes(request.getNotes());
        budget.setIsActive(true);
        budget.setSpentAmount(BigDecimal.ZERO);

        Budget savedBudget = budgetRepository.save(budget);

        // 9. Tính toán spent amount cho khoảng thời gian này
        recalculateSpentAmount(savedBudget.getBudgetId());

        return savedBudget;
    }

    @Override
    @Transactional
    public Budget updateBudget(Long userId, Long budgetId, UpdateBudgetRequest request) {
        // 1. Tìm budget và kiểm tra quyền
        Budget budget = budgetRepository.findByBudgetIdAndUser_UserId(budgetId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngân sách hoặc bạn không có quyền chỉnh sửa"));

        // 2. Cập nhật các trường nếu có
        if (request.getBudgetName() != null && !request.getBudgetName().trim().isEmpty()) {
            budget.setBudgetName(request.getBudgetName().trim());
        }

        if (request.getAmount() != null) {
            budget.setAmount(request.getAmount());
        }

        if (request.getStartDate() != null) {
            budget.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            budget.setEndDate(request.getEndDate());
        }

        // Validate dates nếu có thay đổi
        if (budget.getEndDate().isBefore(budget.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        if (request.getAlertThreshold() != null) {
            budget.setAlertThreshold(request.getAlertThreshold());
            // Reset alert sent nếu threshold thay đổi
            budget.setAlertSent(false);
        }

        if (request.getIsRecurring() != null) {
            budget.setIsRecurring(request.getIsRecurring());
        }

        if (request.getRecurrenceType() != null) {
            budget.setRecurrenceType(request.getRecurrenceType());
        }

        if (request.getIsActive() != null) {
            budget.setIsActive(request.getIsActive());
        }

        if (request.getNotes() != null) {
            budget.setNotes(request.getNotes());
        }

        Budget savedBudget = budgetRepository.save(budget);

        // 3. Tính lại spent amount nếu thời gian thay đổi
        if (request.getStartDate() != null || request.getEndDate() != null) {
            recalculateSpentAmount(savedBudget.getBudgetId());
        }

        return savedBudget;
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByBudgetIdAndUser_UserId(budgetId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngân sách hoặc bạn không có quyền xóa"));

        budgetRepository.delete(budget);
    }

    @Override
    public BudgetDTO getBudgetDetails(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByBudgetIdAndUser_UserId(budgetId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngân sách"));

        return convertToDTO(budget);
    }

    @Override
    public List<BudgetDTO> getAllBudgets(Long userId) {
        List<Budget> budgets = budgetRepository.findByUser_UserId(userId);
        return budgets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetDTO> getActiveBudgets(Long userId) {
        List<Budget> budgets = budgetRepository.findCurrentActiveBudgets(userId);
        return budgets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetDTO> getBudgetsByWallet(Long userId, Long walletId) {
        // Kiểm tra quyền truy cập wallet
        if (!walletMemberRepository.existsByWallet_WalletIdAndUser_UserId(walletId, userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập ví này");
        }

        List<Budget> budgets = budgetRepository.findByUser_UserIdAndWallet_WalletId(userId, walletId);
        return budgets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetDTO> getBudgetsByCategory(Long userId, Long categoryId) {
        List<Budget> budgets = budgetRepository.findByUser_UserIdAndCategory_CategoryId(userId, categoryId);
        return budgets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BudgetSummaryDTO getBudgetSummary(Long userId) {
        List<Budget> allBudgets = budgetRepository.findByUser_UserId(userId);
        return calculateSummary(allBudgets);
    }

    @Override
    public BudgetSummaryDTO getBudgetSummaryByWallet(Long userId, Long walletId) {
        // Kiểm tra quyền truy cập
        if (!walletMemberRepository.existsByWallet_WalletIdAndUser_UserId(walletId, userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập ví này");
        }

        List<Budget> walletBudgets = budgetRepository.findByUser_UserIdAndWallet_WalletId(userId, walletId);
        return calculateSummary(walletBudgets);
    }

    @Override
    @Transactional
    public void updateSpentAmount(Long budgetId) {
        recalculateSpentAmount(budgetId);
    }

    @Override
    @Transactional
    public void updateSpentAmountsForTransaction(Long walletId, Long categoryId, LocalDate transactionDate) {
        // Tìm tất cả budgets liên quan đến transaction này
        List<Budget> relatedBudgets = budgetRepository.findByWallet_WalletId(walletId).stream()
                .filter(b -> b.getCategory().getCategoryId().equals(categoryId))
                .filter(b -> !transactionDate.isBefore(b.getStartDate()) && 
                           !transactionDate.isAfter(b.getEndDate()))
                .collect(Collectors.toList());

        // Tính lại spent amount cho từng budget
        for (Budget budget : relatedBudgets) {
            recalculateSpentAmount(budget.getBudgetId());
        }
    }

    @Override
    @Transactional
    public void recalculateSpentAmount(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget không tồn tại"));

        // Lấy transaction type "Chi tiêu"
        TransactionType expenseType = transactionTypeRepository.findByTypeName("Chi tiêu")
                .orElseThrow(() -> new RuntimeException("Transaction type 'Chi tiêu' không tồn tại"));

        // Tính tổng chi tiêu trong khoảng thời gian của budget
        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getWallet().getWalletId().equals(budget.getWallet().getWalletId()))
                .filter(t -> t.getCategory().getCategoryId().equals(budget.getCategory().getCategoryId()))
                .filter(t -> t.getTransactionType().getTypeId().equals(expenseType.getTypeId()))
                .filter(t -> {
                    LocalDate txDate = t.getTransactionDate().toLocalDate();
                    return !txDate.isBefore(budget.getStartDate()) && 
                           !txDate.isAfter(budget.getEndDate());
                })
                .collect(Collectors.toList());

        BigDecimal totalSpent = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        budget.setSpentAmount(totalSpent);
        budgetRepository.save(budget);
    }

    @Override
    @Transactional
    public List<BudgetAlertDTO> checkAndSendAlerts(Long userId) {
        List<Budget> budgetsNeedingAlert = budgetRepository.findCurrentActiveBudgets(userId).stream()
                .filter(Budget::shouldSendAlert)
                .collect(Collectors.toList());

        List<BudgetAlertDTO> alerts = new ArrayList<>();

        for (Budget budget : budgetsNeedingAlert) {
            String alertType;
            String message;

            if (budget.isOverBudget()) {
                alertType = "EXCEEDED";
                message = String.format(
                    "Ngân sách \"%s\" đã vượt %.2f%%. Đã chi: %s/%s %s",
                    budget.getBudgetName(),
                    budget.getPercentageUsed().doubleValue(),
                    budget.getSpentAmount(),
                    budget.getAmount(),
                    budget.getCurrencyCode()
                );
            } else if (budget.getPercentageUsed().compareTo(new BigDecimal("95")) >= 0) {
                alertType = "DANGER";
                message = String.format(
                    "Ngân sách \"%s\" sắp hết! Đã dùng %.2f%%. Còn lại: %s %s",
                    budget.getBudgetName(),
                    budget.getPercentageUsed().doubleValue(),
                    budget.getRemainingAmount(),
                    budget.getCurrencyCode()
                );
            } else {
                alertType = "WARNING";
                message = String.format(
                    "Ngân sách \"%s\" đã đạt %.2f%%. Hãy cân nhắc chi tiêu!",
                    budget.getBudgetName(),
                    budget.getPercentageUsed().doubleValue()
                );
            }

            BudgetAlertDTO alert = new BudgetAlertDTO(
                budget.getBudgetId(),
                budget.getBudgetName(),
                budget.getCategory().getCategoryName(),
                budget.getAmount(),
                budget.getSpentAmount(),
                budget.getPercentageUsed(),
                alertType,
                message
            );

            alerts.add(alert);

            // Đánh dấu đã gửi alert
            budget.setAlertSent(true);
            budgetRepository.save(budget);
        }

        return alerts;
    }

    @Override
    @Transactional
    public void renewRecurringBudgets() {
        List<Budget> recurringBudgets = budgetRepository.findRecurringBudgetsToRenew();

        for (Budget oldBudget : recurringBudgets) {
            // Tính toán period mới dựa trên recurrence type
            LocalDate newStartDate;
            LocalDate newEndDate;

            switch (oldBudget.getRecurrenceType()) {
                case "MONTHLY":
                    newStartDate = oldBudget.getEndDate().plusDays(1);
                    newEndDate = newStartDate.plusMonths(1).minusDays(1);
                    break;
                case "QUARTERLY":
                    newStartDate = oldBudget.getEndDate().plusDays(1);
                    newEndDate = newStartDate.plusMonths(3).minusDays(1);
                    break;
                case "YEARLY":
                    newStartDate = oldBudget.getEndDate().plusDays(1);
                    newEndDate = newStartDate.plusYears(1).minusDays(1);
                    break;
                default:
                    continue; // Skip nếu không hợp lệ
            }

            // Tạo budget mới
            Budget newBudget = new Budget();
            newBudget.setUser(oldBudget.getUser());
            newBudget.setWallet(oldBudget.getWallet());
            newBudget.setCategory(oldBudget.getCategory());
            newBudget.setBudgetName(oldBudget.getBudgetName());
            newBudget.setAmount(oldBudget.getAmount());
            newBudget.setCurrencyCode(oldBudget.getCurrencyCode());
            newBudget.setStartDate(newStartDate);
            newBudget.setEndDate(newEndDate);
            newBudget.setAlertThreshold(oldBudget.getAlertThreshold());
            newBudget.setIsRecurring(true);
            newBudget.setRecurrenceType(oldBudget.getRecurrenceType());
            newBudget.setNotes(oldBudget.getNotes());
            newBudget.setIsActive(true);
            newBudget.setSpentAmount(BigDecimal.ZERO);

            budgetRepository.save(newBudget);

            // Vô hiệu hóa budget cũ (optional - có thể giữ để xem lịch sử)
            oldBudget.setIsActive(false);
            budgetRepository.save(oldBudget);
        }
    }

    @Override
    @Transactional
    public void deactivateExpiredBudgets() {
        List<Budget> allBudgets = budgetRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Budget budget : allBudgets) {
            if (budget.getIsActive() && budget.getEndDate().isBefore(today)) {
                budget.setIsActive(false);
                budgetRepository.save(budget);
            }
        }
    }

    // ===== HELPER METHODS =====

    private BudgetDTO convertToDTO(Budget budget) {
        BudgetDTO dto = new BudgetDTO();

        dto.setBudgetId(budget.getBudgetId());
        dto.setUserId(budget.getUser().getUserId());
        dto.setWalletId(budget.getWallet().getWalletId());
        dto.setWalletName(budget.getWallet().getWalletName());
        dto.setCategoryId(budget.getCategory().getCategoryId());
        dto.setCategoryName(budget.getCategory().getCategoryName());
        dto.setCategoryIcon(budget.getCategory().getIcon());
        dto.setBudgetName(budget.getBudgetName());
        dto.setAmount(budget.getAmount());
        dto.setCurrencyCode(budget.getCurrencyCode());
        dto.setStartDate(budget.getStartDate());
        dto.setEndDate(budget.getEndDate());
        dto.setSpentAmount(budget.getSpentAmount());
        dto.setRemainingAmount(budget.getRemainingAmount());
        dto.setPercentageUsed(budget.getPercentageUsed());
        dto.setAlertThreshold(budget.getAlertThreshold());
        dto.setAlertSent(budget.getAlertSent());
        dto.setIsRecurring(budget.getIsRecurring());
        dto.setRecurrenceType(budget.getRecurrenceType());
        dto.setIsActive(budget.getIsActive());
        dto.setNotes(budget.getNotes());
        dto.setCreatedAt(budget.getCreatedAt());
        dto.setUpdatedAt(budget.getUpdatedAt());

        // Computed fields
        dto.setIsOverBudget(budget.isOverBudget());
        dto.setIsExpired(budget.isExpired());
        dto.setIsCurrentlyActive(budget.isCurrentlyActive());

        // Tính số ngày còn lại
        LocalDate today = LocalDate.now();
        if (!budget.isExpired()) {
            long daysRemaining = ChronoUnit.DAYS.between(today, budget.getEndDate());
            dto.setDaysRemaining((int) daysRemaining);
        } else {
            dto.setDaysRemaining(0);
        }

        // Xác định status
        if (budget.isOverBudget()) {
            dto.setStatus("EXCEEDED");
        } else if (budget.getPercentageUsed().compareTo(new BigDecimal("95")) >= 0) {
            dto.setStatus("DANGER");
        } else if (budget.getPercentageUsed().compareTo(budget.getAlertThreshold()) >= 0) {
            dto.setStatus("WARNING");
        } else {
            dto.setStatus("SAFE");
        }

        return dto;
    }

    private BudgetSummaryDTO calculateSummary(List<Budget> budgets) {
        BudgetSummaryDTO summary = new BudgetSummaryDTO();

        summary.setTotalBudgets(budgets.size());

        // Đếm các loại budgets
        int activeBudgets = (int) budgets.stream().filter(Budget::isCurrentlyActive).count();
        int expiredBudgets = (int) budgets.stream().filter(Budget::isExpired).count();
        int overBudgets = (int) budgets.stream().filter(Budget::isOverBudget).count();

        summary.setActiveBudgets(activeBudgets);
        summary.setExpiredBudgets(expiredBudgets);
        summary.setOverBudgets(overBudgets);

        // Tính tổng tiền (chỉ budgets đang active)
        List<Budget> currentBudgets = budgets.stream()
                .filter(Budget::isCurrentlyActive)
                .collect(Collectors.toList());

        BigDecimal totalBudget = currentBudgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = currentBudgets.stream()
                .map(Budget::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalBudget.subtract(totalSpent);

        summary.setTotalBudgetAmount(totalBudget);
        summary.setTotalSpentAmount(totalSpent);
        summary.setTotalRemainingAmount(totalRemaining);

        // Tính phần trăm tổng
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal overallPercentage = totalSpent
                    .multiply(new BigDecimal("100"))
                    .divide(totalBudget, 2, RoundingMode.HALF_UP);
            summary.setOverallPercentageUsed(overallPercentage);
        } else {
            summary.setOverallPercentageUsed(BigDecimal.ZERO);
        }

        // Currency code (lấy từ budget đầu tiên nếu có)
        if (!budgets.isEmpty()) {
            summary.setCurrencyCode(budgets.get(0).getCurrencyCode());
        }

        // Convert budgets to DTOs
        List<BudgetDTO> budgetDTOs = budgets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        summary.setBudgets(budgetDTOs);

        // Generate alerts
        List<BudgetAlertDTO> alerts = currentBudgets.stream()
                .filter(b -> b.getPercentageUsed().compareTo(b.getAlertThreshold()) >= 0)
                .map(b -> {
                    String alertType = b.isOverBudget() ? "EXCEEDED" : 
                                     b.getPercentageUsed().compareTo(new BigDecimal("95")) >= 0 ? "DANGER" : "WARNING";
                    String message = String.format("Ngân sách \"%s\" đã dùng %.2f%%", 
                                                  b.getBudgetName(), b.getPercentageUsed().doubleValue());
                    return new BudgetAlertDTO(
                        b.getBudgetId(),
                        b.getBudgetName(),
                        b.getCategory().getCategoryName(),
                        b.getAmount(),
                        b.getSpentAmount(),
                        b.getPercentageUsed(),
                        alertType,
                        message
                    );
                })
                .collect(Collectors.toList());
        summary.setAlerts(alerts);

        return summary;
    }
}

