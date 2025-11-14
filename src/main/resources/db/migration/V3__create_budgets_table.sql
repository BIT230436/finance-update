-- =====================================================
-- Migration: Tạo bảng budgets
-- Mục đích: Quản lý ngân sách chi tiêu theo tháng/kỳ
-- Ngày: 2024-11-14
-- =====================================================

CREATE TABLE IF NOT EXISTS budgets (
    budget_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- User & Wallet
    user_id BIGINT NOT NULL,
    wallet_id BIGINT NOT NULL,
    
    -- Category (chỉ cho Chi tiêu)
    category_id BIGINT NOT NULL,
    
    -- Budget Info
    budget_name VARCHAR(100) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    
    -- Period
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    
    -- Tracking
    spent_amount DECIMAL(15, 2) DEFAULT 0.00,
    remaining_amount DECIMAL(15, 2),
    percentage_used DECIMAL(5, 2) DEFAULT 0.00,
    
    -- Alert Settings
    alert_threshold DECIMAL(5, 2) DEFAULT 80.00,
    alert_sent BOOLEAN DEFAULT FALSE,
    
    -- Recurring
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_type VARCHAR(20), -- MONTHLY, QUARTERLY, YEARLY
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    
    -- Timestamps
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_wallet FOREIGN KEY (wallet_id) 
        REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) 
        REFERENCES categories(category_id) ON DELETE CASCADE,
    
    -- Unique Constraint: Không cho phép trùng budget cho cùng wallet, category và period
    CONSTRAINT uk_budget_period_category UNIQUE (user_id, wallet_id, category_id, start_date, end_date),
    
    -- Indexes for performance
    INDEX idx_user_wallet (user_id, wallet_id),
    INDEX idx_period (start_date, end_date),
    INDEX idx_category (category_id),
    INDEX idx_active (is_active),
    INDEX idx_recurring (is_recurring),
    INDEX idx_alert_sent (alert_sent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Verification queries
-- =====================================================

-- Kiểm tra bảng đã tạo thành công
SELECT COUNT(*) as total_budgets FROM budgets;

-- Xem cấu trúc bảng
DESCRIBE budgets;

-- =====================================================
-- Sample data (Optional - for testing)
-- =====================================================

-- NOTE: Uncomment để thêm dữ liệu mẫu khi test
-- INSERT INTO budgets (
--     user_id, 
--     wallet_id, 
--     category_id, 
--     budget_name, 
--     amount, 
--     currency_code, 
--     start_date, 
--     end_date
-- ) VALUES (
--     1, -- user_id (thay bằng user thật trong DB)
--     1, -- wallet_id
--     1, -- category_id (Ăn uống)
--     'Ngân sách Ăn uống tháng 11',
--     5000000,
--     'VND',
--     '2024-11-01',
--     '2024-11-30'
-- );

