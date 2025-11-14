package com.example.financeapp.controller;

import com.example.financeapp.dto.*;
import com.example.financeapp.entity.Budget;
import com.example.financeapp.entity.User;
import com.example.financeapp.repository.UserRepository;
import com.example.financeapp.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý ngân sách chi tiêu
 */
@RestController
@RequestMapping("/budgets")
@CrossOrigin(origins = "*")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Helper method lấy userId từ JWT
     */
    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin user từ token"));
    }

    // ============ CRUD OPERATIONS ============

    /**
     * Tạo ngân sách mới
     * POST /budgets
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            Budget budget = budgetService.createBudget(userId, request);

            res.put("message", "Tạo ngân sách thành công");
            res.put("budget", budgetService.getBudgetDetails(userId, budget.getBudgetId()));
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * Cập nhật ngân sách
     * PUT /budgets/{budgetId}
     */
    @PutMapping("/{budgetId}")
    public ResponseEntity<Map<String, Object>> updateBudget(
            @PathVariable Long budgetId,
            @Valid @RequestBody UpdateBudgetRequest request) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            Budget budget = budgetService.updateBudget(userId, budgetId, request);

            res.put("message", "Cập nhật ngân sách thành công");
            res.put("budget", budgetService.getBudgetDetails(userId, budget.getBudgetId()));
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * Xóa ngân sách
     * DELETE /budgets/{budgetId}
     */
    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Map<String, Object>> deleteBudget(@PathVariable Long budgetId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            budgetService.deleteBudget(userId, budgetId);

            res.put("message", "Xóa ngân sách thành công");
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * Lấy chi tiết ngân sách
     * GET /budgets/{budgetId}
     */
    @GetMapping("/{budgetId}")
    public ResponseEntity<Map<String, Object>> getBudgetDetails(@PathVariable Long budgetId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            BudgetDTO budget = budgetService.getBudgetDetails(userId, budgetId);

            res.put("budget", budget);
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(404).body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    // ============ LIST OPERATIONS ============

    /**
     * Lấy tất cả ngân sách của user
     * GET /budgets
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBudgets(
            @RequestParam(required = false) String filter) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            List<BudgetDTO> budgets;

            if ("active".equalsIgnoreCase(filter)) {
                budgets = budgetService.getActiveBudgets(userId);
            } else {
                budgets = budgetService.getAllBudgets(userId);
            }

            res.put("budgets", budgets);
            res.put("total", budgets.size());
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * Lấy ngân sách theo ví
     * GET /budgets/wallet/{walletId}
     */
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<Map<String, Object>> getBudgetsByWallet(@PathVariable Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            List<BudgetDTO> budgets = budgetService.getBudgetsByWallet(userId, walletId);

            res.put("budgets", budgets);
            res.put("total", budgets.size());
            res.put("walletId", walletId);
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * Lấy ngân sách theo danh mục
     * GET /budgets/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getBudgetsByCategory(@PathVariable Long categoryId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            List<BudgetDTO> budgets = budgetService.getBudgetsByCategory(userId, categoryId);

            res.put("budgets", budgets);
            res.put("total", budgets.size());
            res.put("categoryId", categoryId);
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    // ============ SUMMARY & REPORTS ============

    /**
     * Lấy tổng quan ngân sách của user
     * GET /budgets/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getBudgetSummary(
            @RequestParam(required = false) Long walletId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            BudgetSummaryDTO summary;

            if (walletId != null) {
                summary = budgetService.getBudgetSummaryByWallet(userId, walletId);
            } else {
                summary = budgetService.getBudgetSummary(userId);
            }

            res.put("summary", summary);
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    // ============ ALERTS ============

    /**
     * Kiểm tra và lấy danh sách cảnh báo ngân sách
     * GET /budgets/alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getAlerts() {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            List<BudgetAlertDTO> alerts = budgetService.checkAndSendAlerts(userId);

            res.put("alerts", alerts);
            res.put("total", alerts.size());
            res.put("hasAlerts", !alerts.isEmpty());
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    // ============ ACTIONS ============

    /**
     * Tính lại số tiền đã chi cho ngân sách
     * POST /budgets/{budgetId}/recalculate
     */
    @PostMapping("/{budgetId}/recalculate")
    public ResponseEntity<Map<String, Object>> recalculateSpentAmount(@PathVariable Long budgetId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            
            // Kiểm tra quyền
            budgetService.getBudgetDetails(userId, budgetId);
            
            // Tính lại
            budgetService.recalculateSpentAmount(budgetId);

            res.put("message", "Tính lại số tiền đã chi thành công");
            res.put("budget", budgetService.getBudgetDetails(userId, budgetId));
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa ngân sách
     * PATCH /budgets/{budgetId}/toggle-active
     */
    @PatchMapping("/{budgetId}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleActive(@PathVariable Long budgetId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long userId = getCurrentUserId();
            BudgetDTO currentBudget = budgetService.getBudgetDetails(userId, budgetId);

            UpdateBudgetRequest request = new UpdateBudgetRequest();
            request.setIsActive(!currentBudget.getIsActive());

            budgetService.updateBudget(userId, budgetId, request);

            res.put("message", currentBudget.getIsActive() ? 
                "Đã vô hiệu hóa ngân sách" : "Đã kích hoạt ngân sách");
            res.put("budget", budgetService.getBudgetDetails(userId, budgetId));
            return ResponseEntity.ok(res);

        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("error", "Lỗi máy chủ nội bộ: " + e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }
}

