# 💰 TÍNH NĂNG QUẢN LÝ NGÂN SÁCH (BUDGET MANAGEMENT)

## 📋 TỔNG QUAN

Tính năng Budget Management cho phép người dùng:
- ✅ Tạo ngân sách chi tiêu theo tháng hoặc kỳ cụ thể
- ✅ Theo dõi chi tiêu thực tế so với ngân sách đã đặt
- ✅ Nhận cảnh báo khi gần vượt hoặc vượt ngân sách
- ✅ Hỗ trợ ngân sách lặp lại hàng tháng/quý/năm
- ✅ Xem báo cáo tổng quan và phân tích chi tiêu

---

## 🏗️ KIẾN TRÚC

### **Files đã tạo:**

```
src/main/java/com/example/financeapp/
├── entity/
│   └── Budget.java                          # Entity chính
├── repository/
│   └── BudgetRepository.java                # JPA Repository
├── dto/
│   ├── CreateBudgetRequest.java             # DTO tạo budget
│   ├── UpdateBudgetRequest.java             # DTO cập nhật budget
│   ├── BudgetDTO.java                       # DTO trả về chi tiết
│   ├── BudgetSummaryDTO.java                # DTO tổng hợp
│   └── BudgetAlertDTO.java                  # DTO cảnh báo
├── service/
│   ├── BudgetService.java                   # Interface
│   └── impl/
│       └── BudgetServiceImpl.java           # Implementation
└── controller/
    └── BudgetController.java                # REST Controller

src/main/resources/db/migration/
└── V3__create_budgets_table.sql             # Database migration
```

### **Files đã cập nhật:**

```
src/main/java/com/example/financeapp/
├── service/impl/
│   └── TransactionServiceImpl.java          # Thêm auto-update budget
└── security/
    └── WebSecurityConfig.java               # Thêm budget endpoints
```

---

## 📊 DATABASE SCHEMA

### **Budget Table:**

```sql
CREATE TABLE budgets (
    budget_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    wallet_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    budget_name VARCHAR(100) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    spent_amount DECIMAL(15, 2) DEFAULT 0.00,
    remaining_amount DECIMAL(15, 2),
    percentage_used DECIMAL(5, 2) DEFAULT 0.00,
    alert_threshold DECIMAL(5, 2) DEFAULT 80.00,
    alert_sent BOOLEAN DEFAULT FALSE,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_type VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    
    UNIQUE (user_id, wallet_id, category_id, start_date, end_date)
);
```

### **Relationships:**
- Budget → User (Many-to-One)
- Budget → Wallet (Many-to-One)
- Budget → Category (Many-to-One, chỉ "Chi tiêu")

---

## 🌐 API ENDPOINTS

### **1. CRUD Operations**

#### **Tạo ngân sách mới**
```http
POST /budgets
Authorization: Bearer {token}
Content-Type: application/json

{
  "walletId": 1,
  "categoryId": 1,
  "budgetName": "Ngân sách Ăn uống tháng 11",
  "amount": 5000000,
  "startDate": "2024-11-01",
  "endDate": "2024-11-30",
  "alertThreshold": 80.00,
  "isRecurring": false,
  "recurrenceType": null,
  "notes": "Giới hạn chi tiêu ăn uống"
}

Response 200:
{
  "message": "Tạo ngân sách thành công",
  "budget": {
    "budgetId": 1,
    "budgetName": "Ngân sách Ăn uống tháng 11",
    "amount": 5000000,
    "spentAmount": 0,
    "remainingAmount": 5000000,
    "percentageUsed": 0,
    "status": "SAFE",
    "isCurrentlyActive": true,
    "daysRemaining": 16,
    ...
  }
}
```

#### **Cập nhật ngân sách**
```http
PUT /budgets/{budgetId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "budgetName": "Ngân sách Ăn uống (Đã điều chỉnh)",
  "amount": 6000000,
  "alertThreshold": 85.00,
  "notes": "Tăng ngân sách do có thêm cuộc họp"
}

Response 200:
{
  "message": "Cập nhật ngân sách thành công",
  "budget": { ... }
}
```

#### **Xóa ngân sách**
```http
DELETE /budgets/{budgetId}
Authorization: Bearer {token}

Response 200:
{
  "message": "Xóa ngân sách thành công"
}
```

#### **Lấy chi tiết ngân sách**
```http
GET /budgets/{budgetId}
Authorization: Bearer {token}

Response 200:
{
  "budget": {
    "budgetId": 1,
    "budgetName": "Ngân sách Ăn uống tháng 11",
    "walletName": "Ví VND",
    "categoryName": "Ăn uống",
    "amount": 5000000,
    "spentAmount": 3200000,
    "remainingAmount": 1800000,
    "percentageUsed": 64.00,
    "currencyCode": "VND",
    "status": "SAFE",
    "isOverBudget": false,
    "isExpired": false,
    "isCurrentlyActive": true,
    "daysRemaining": 16,
    "startDate": "2024-11-01",
    "endDate": "2024-11-30"
  }
}
```

### **2. List Operations**

#### **Lấy tất cả ngân sách**
```http
GET /budgets
Authorization: Bearer {token}

Response 200:
{
  "budgets": [ ... ],
  "total": 5
}
```

#### **Lấy ngân sách đang active**
```http
GET /budgets?filter=active
Authorization: Bearer {token}

Response 200:
{
  "budgets": [ ... ],
  "total": 3
}
```

#### **Lấy ngân sách theo ví**
```http
GET /budgets/wallet/{walletId}
Authorization: Bearer {token}

Response 200:
{
  "budgets": [ ... ],
  "total": 2,
  "walletId": 1
}
```

#### **Lấy ngân sách theo danh mục**
```http
GET /budgets/category/{categoryId}
Authorization: Bearer {token}

Response 200:
{
  "budgets": [ ... ],
  "total": 1,
  "categoryId": 1
}
```

### **3. Summary & Reports**

#### **Lấy tổng quan ngân sách**
```http
GET /budgets/summary
Authorization: Bearer {token}

Response 200:
{
  "summary": {
    "totalBudgets": 5,
    "activeBudgets": 3,
    "expiredBudgets": 2,
    "overBudgets": 1,
    "totalBudgetAmount": 15000000,
    "totalSpentAmount": 12000000,
    "totalRemainingAmount": 3000000,
    "overallPercentageUsed": 80.00,
    "currencyCode": "VND",
    "budgets": [ ... ],
    "alerts": [
      {
        "budgetId": 2,
        "budgetName": "Ngân sách Di chuyển",
        "categoryName": "Di chuyển",
        "amount": 2000000,
        "spentAmount": 1900000,
        "percentageUsed": 95.00,
        "alertType": "DANGER",
        "message": "Ngân sách \"Di chuyển\" sắp hết! Đã dùng 95.00%. Còn lại: 100000 VND"
      }
    ]
  }
}
```

#### **Lấy tổng quan theo ví**
```http
GET /budgets/summary?walletId=1
Authorization: Bearer {token}

Response 200:
{
  "summary": { ... }
}
```

### **4. Alerts**

#### **Lấy danh sách cảnh báo**
```http
GET /budgets/alerts
Authorization: Bearer {token}

Response 200:
{
  "alerts": [
    {
      "budgetId": 2,
      "budgetName": "Ngân sách Di chuyển",
      "categoryName": "Di chuyển",
      "alertType": "DANGER",
      "message": "Ngân sách sắp hết! Đã dùng 95%"
    },
    {
      "budgetId": 3,
      "budgetName": "Ngân sách Giải trí",
      "categoryName": "Giải trí",
      "alertType": "EXCEEDED",
      "message": "Ngân sách đã vượt 105%"
    }
  ],
  "total": 2,
  "hasAlerts": true
}
```

### **5. Actions**

#### **Tính lại số tiền đã chi**
```http
POST /budgets/{budgetId}/recalculate
Authorization: Bearer {token}

Response 200:
{
  "message": "Tính lại số tiền đã chi thành công",
  "budget": { ... }
}
```

#### **Kích hoạt/Vô hiệu hóa ngân sách**
```http
PATCH /budgets/{budgetId}/toggle-active
Authorization: Bearer {token}

Response 200:
{
  "message": "Đã kích hoạt ngân sách",
  "budget": { ... }
}
```

---

## 🎯 BUSINESS LOGIC

### **1. Validation Rules:**

#### **Khi tạo budget:**
- ✅ User phải có quyền truy cập wallet
- ✅ Category phải là loại "Chi tiêu"
- ✅ End date phải sau start date
- ✅ Không cho phép tạo budget trùng (cùng wallet, category, period)
- ✅ Amount phải > 0
- ✅ Nếu recurring = true thì phải có recurrenceType

#### **Khi cập nhật budget:**
- ✅ Chỉ owner của budget mới được update
- ✅ End date phải sau start date
- ✅ Nếu thay đổi threshold → reset alertSent flag
- ✅ Nếu thay đổi period → tự động recalculate spent amount

#### **Khi xóa budget:**
- ✅ Chỉ owner mới được xóa
- ✅ Xóa vĩnh viễn không thể hoàn tác

### **2. Auto-Update Logic:**

#### **Khi tạo transaction "Chi tiêu":**
```java
1. Transaction được tạo và lưu vào DB
2. Tự động tìm tất cả budgets liên quan:
   - Cùng wallet
   - Cùng category
   - Transaction date nằm trong [startDate, endDate]
3. Tính lại spent_amount cho từng budget:
   - SUM(amount) của tất cả transactions "Chi tiêu" trong period
4. Cập nhật:
   - spentAmount
   - remainingAmount = amount - spentAmount
   - percentageUsed = (spentAmount / amount) * 100
5. Kiểm tra alert:
   - Nếu percentageUsed >= alertThreshold → tạo alert
```

### **3. Status Logic:**

Budget có 4 trạng thái:

```
- SAFE: percentageUsed < alertThreshold
- WARNING: percentageUsed >= alertThreshold && < 95%
- DANGER: percentageUsed >= 95% && spentAmount <= amount
- EXCEEDED: spentAmount > amount
```

### **4. Recurring Budget Logic:**

```java
// Chạy scheduled task hàng ngày
1. Tìm budgets có:
   - isRecurring = true
   - endDate < today
   - isActive = true

2. Tính period mới:
   - MONTHLY: +1 tháng
   - QUARTERLY: +3 tháng
   - YEARLY: +1 năm

3. Tạo budget mới với:
   - Cùng amount, category, settings
   - Period mới
   - spentAmount = 0
   - alertSent = false

4. Vô hiệu hóa budget cũ (isActive = false)
```

### **5. Alert System:**

```java
Alert Types:
- WARNING: 80% <= percentageUsed < 95%
- DANGER: 95% <= percentageUsed < 100%
- EXCEEDED: percentageUsed >= 100%

Alert Logic:
1. Kiểm tra percentageUsed >= alertThreshold
2. Kiểm tra alertSent = false (chưa gửi)
3. Tạo alert message tương ứng
4. Đánh dấu alertSent = true
5. Trả về list alerts cho frontend
```

---

## 🔄 INTEGRATION

### **1. Transaction Integration:**

File: `TransactionServiceImpl.java`

```java
// Sau khi tạo transaction "Chi tiêu"
if ("Chi tiêu".equals(typeName)) {
    budgetService.updateSpentAmountsForTransaction(
        wallet.getWalletId(),
        category.getCategoryId(),
        transactionDate.toLocalDate()
    );
}
```

### **2. Security Integration:**

File: `WebSecurityConfig.java`

```java
.requestMatchers("/budgets/**").authenticated()
```

---

## 📱 USE CASES

### **Use Case 1: Tạo ngân sách tháng**
```
User: Tôi muốn giới hạn chi tiêu ăn uống 5 triệu/tháng
System: 
  1. Tạo budget với amount = 5,000,000 VND
  2. Period: 01/11/2024 - 30/11/2024
  3. Category: "Ăn uống"
  4. Alert threshold: 80% (4 triệu)
```

### **Use Case 2: Theo dõi chi tiêu**
```
User: Tạo transaction "Chi tiêu" 100,000 VND cho "Ăn uống"
System:
  1. Tạo transaction
  2. Trừ tiền khỏi wallet
  3. Tự động cập nhật budget:
     - spentAmount = 100,000
     - remainingAmount = 4,900,000
     - percentageUsed = 2%
```

### **Use Case 3: Nhận cảnh báo**
```
Scenario: User đã chi 4.2 triệu / 5 triệu (84%)
System:
  1. Phát hiện percentageUsed (84%) >= alertThreshold (80%)
  2. Tạo alert type "WARNING"
  3. Message: "Ngân sách Ăn uống đã đạt 84%. Hãy cân nhắc chi tiêu!"
  4. Hiển thị trên dashboard
```

### **Use Case 4: Vượt ngân sách**
```
Scenario: User đã chi 5.5 triệu / 5 triệu (110%)
System:
  1. Status = "EXCEEDED"
  2. isOverBudget = true
  3. Alert type = "EXCEEDED"
  4. Message: "Ngân sách đã vượt 110%"
  5. Vẫn cho phép transaction (không block)
```

### **Use Case 5: Ngân sách lặp lại**
```
User: Tạo ngân sách lặp lại hàng tháng
System:
  1. Tạo budget với isRecurring = true, recurrenceType = "MONTHLY"
  2. Sau 30/11:
     - Tự động tạo budget mới cho 01/12 - 31/12
     - Vô hiệu hóa budget cũ
     - Reset spentAmount = 0
```

---

## 📈 DASHBOARD METRICS

Frontend có thể sử dụng API `/budgets/summary` để hiển thị:

### **Tổng quan:**
- Tổng số ngân sách
- Số ngân sách đang active
- Số ngân sách đã hết hạn
- Số ngân sách bị vượt

### **Số liệu:**
- Tổng ngân sách: 15,000,000 VND
- Đã chi: 12,000,000 VND
- Còn lại: 3,000,000 VND
- Phần trăm: 80%

### **Cảnh báo:**
- Danh sách budgets cần chú ý
- Alert type và message
- Action buttons (xem chi tiết, điều chỉnh)

### **Charts:**
- Pie chart: Phân bổ ngân sách theo category
- Bar chart: So sánh spent vs budget
- Line chart: Xu hướng chi tiêu theo ngày

---

## 🔧 MAINTENANCE

### **Scheduled Tasks (Cần triển khai):**

```java
@Scheduled(cron = "0 0 1 * * *") // Chạy 1h sáng mỗi ngày
public void dailyBudgetMaintenance() {
    // 1. Vô hiệu hóa budgets đã hết hạn
    budgetService.deactivateExpiredBudgets();
    
    // 2. Tạo budgets mới cho recurring
    budgetService.renewRecurringBudgets();
    
    // 3. Kiểm tra và gửi alerts
    // (hoặc có thể check realtime khi user vào app)
}
```

### **Performance Optimization:**

1. **Index trên các cột thường query:**
   - (user_id, wallet_id)
   - (start_date, end_date)
   - category_id
   - is_active

2. **Caching (optional):**
   - Cache budget summary trong 5 phút
   - Invalidate khi có transaction mới

3. **Lazy loading:**
   - Budget entity dùng FetchType.LAZY cho relations

---

## ✅ TESTING CHECKLIST

### **Unit Tests:**
- [ ] Create budget with valid data
- [ ] Create budget with invalid data (throws exception)
- [ ] Create duplicate budget (throws exception)
- [ ] Update budget
- [ ] Delete budget
- [ ] Calculate spent amount correctly
- [ ] Alert threshold logic
- [ ] Status calculation
- [ ] Recurring budget generation

### **Integration Tests:**
- [ ] Transaction tự động update budget
- [ ] Budget không update khi transaction là "Thu nhập"
- [ ] Multiple budgets update correctly
- [ ] Alert system works end-to-end

### **API Tests:**
- [ ] All CRUD endpoints
- [ ] Authorization (chỉ owner access được)
- [ ] Validation errors return 400
- [ ] Not found returns 404

---

## 🚀 DEPLOYMENT

### **Database Migration:**
```bash
# Chạy migration
mvn flyway:migrate

# Hoặc tự động khi start app (nếu dùng Flyway)
mvn spring-boot:run
```

### **Verify:**
```sql
-- Kiểm tra bảng đã tạo
DESCRIBE budgets;

-- Kiểm tra indexes
SHOW INDEX FROM budgets;

-- Test insert
INSERT INTO budgets (...) VALUES (...);
```

---

## 📚 FUTURE ENHANCEMENTS

### **Phase 2:**
- [ ] Shared budget (chia sẻ ngân sách với người khác)
- [ ] Budget templates (mẫu ngân sách có sẵn)
- [ ] Budget goals (mục tiêu tiết kiệm)
- [ ] Email/SMS alerts
- [ ] Export budget reports (PDF, Excel)
- [ ] AI suggestions (đề xuất ngân sách dựa trên lịch sử)

### **Phase 3:**
- [ ] Multi-currency budget (ngân sách đa tiền tệ)
- [ ] Budget rollover (chuyển số dư sang tháng sau)
- [ ] Budget approval flow (duyệt ngân sách)
- [ ] Budget analytics dashboard
- [ ] Mobile push notifications

---

## 🎓 BEST PRACTICES

### **Cho User:**
1. Đặt alertThreshold hợp lý (70-80%)
2. Review budgets thường xuyên
3. Điều chỉnh budget khi cần
4. Sử dụng recurring cho budgets cố định
5. Không tạo quá nhiều budgets overlap

### **Cho Developer:**
1. Luôn validate input
2. Handle exceptions gracefully
3. Log important actions
4. Optimize queries với indexes
5. Cache khi cần thiết
6. Write tests đầy đủ

---

## 📞 SUPPORT

Nếu có vấn đề, kiểm tra:
1. Database migration đã chạy chưa
2. Security config đã include /budgets/** chưa
3. Transaction service đã integrate chưa
4. Lỗi validation ở request body
5. Log server để debug

---

**Tác giả:** AI Assistant  
**Ngày tạo:** 14/11/2024  
**Version:** 1.0.0

