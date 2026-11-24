# LEDGE PRO — Business Management, POS & Digital Khata Platform
> **"Your Business. One Smart Ledger."**
> 
> **B.E. Computer Science & Engineering | Semester 3 | Programming in Java Academic Project**  
> **Chitkara University, Punjab**

---

## 1. Project Overview & Vision

**LEDGE PRO** is a commercial-grade, full-stack business operating platform tailored specifically for Indian retailers, kirana store owners, electronics and clothing merchants, and wholesale distributors. 

Unlike basic academic CRUD projects, **LEDGE PRO** unites inventory control, rapid POS barcode/catalog billing, and a signature **Double-Entry Digital Customer Khata Ledger** into one unified, transactional engine. The platform is engineered with **Java 21** as the primary backend, utilizing pure JDBC, PreparedStatements, explicit ACID transactions, and Java Streams to handle all core business logic without hiding database persistence behind an ORM.

---

## 2. Java Course Syllabus Integration Matrix

This project was built from the ground up to visibly and rigorously demonstrate every core module taught in the **Programming in Java** curriculum:

| Syllabus Module | Implementation in LEDGE PRO | Core Classes & Interfaces |
| :--- | :--- | :--- |
| **Object-Oriented Programming (OOP)** | Inheritance hierarchy (`BaseEntity` &rarr; `Product`, `Customer`, `InvoiceItem`), abstraction, interfaces (`InvoiceDAO`, `KhataLedger`), encapsulation, method overloading & overriding (`compareTo`). | `BaseEntity`, `Product`, `Customer`, `Invoice`, `LedgerEntry`, `Payment`, `Business` |
| **Collections Framework** | In-memory O(1) catalog caching (`ConcurrentHashMap`), ordered ledger transaction sequences (`ArrayList`, `LinkedHashMap`), multi-criteria sorting using `Comparator`. | `ProductService`, `ProductDAOImpl`, `ReportAnalyticsService` |
| **Custom Exception Handling** | Hierarchical custom exceptions with structured error codes, multi-catch blocks, and `try-with-resources` ensuring zero JDBC connection leaks. | `LedgeProException`, `ProductNotFoundException`, `InsufficientStockException`, `DuplicateInvoiceException`, `InvalidPaymentException` |
| **Multithreading & Concurrency** | Dedicated background worker pool (`ThreadPoolExecutor`, `ExecutorService`) handling CPU-heavy CSV exports via `Callable<T>` and `Future<T>`, plus a background monitoring daemon thread (`LowStockMonitorService`) with `synchronized` thread safety. | `ReportThreadPoolManager`, `LowStockMonitorService`, `ExportController` |
| **JDBC & ACID Transactions** | Pure JDBC implementation utilizing `DriverManager`, `Connection`, `PreparedStatement`, `ResultSet`, and `DatabaseMetaData`. Explicit transactional boundaries with `conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()` ensuring that stock deduction, invoice insertion, and customer udhaar credit entries are completely atomic. | `DatabaseConnectionFactory`, `ProductDAOImpl`, `CustomerDAOImpl`, `InvoiceDAOImpl`, `LedgerDAOImpl` |
| **Stream API & Lambdas** | Declarative financial analytics processing pipelines: `filter()`, `map()`, `sorted()`, `distinct()`, `reduce()`, `Collectors.groupingBy()`, `Collectors.summarizingDouble()`. | `ReportAnalyticsService.java`, `ReportController.java` |
| **Generics** | Type-safe generic data access interfaces and uniform REST response envelopes. | `GenericDAO<T, ID>`, `GenericResponse<T>`, `GenericResult<T>` |
| **File I/O** | Product catalog import and export, customer statements generation using `BufferedReader`, `BufferedWriter`, `FileReader`, and `FileWriter` with try-with-resources. | `CsvUtils.java`, `ExportController.java` |

---

## 3. Core Application Modules

### Module A — Executive Business Dashboard
- Real-time KPI summaries calculated directly from the database: Today's Sales, Month Revenue, Total Orders, Active Catalog Items, Low Stock count, Total Customers, Outstanding Khata Receivables, Payments Collected, and Estimated Gross Profit.
- Live low-stock alert monitoring.
- Recent invoices and digital khata activity audit trail.

### Module B — Smart Inventory Management
- Add, Edit, Delete, Search, and Category filtering.
- Dynamic sorting by price, quantity, and name using Java `Comparator`.
- Stock movement audit trail preventing negative inventory.
- Instant stock valuation and low-stock warning indicators.
- CSV product export and batch import.

### Module C — POS Terminal & Atomic Invoicing
- Fast product search and click-to-cart interface.
- Automatic unit pricing, quantity spinners, subtotal, discount rates, and configurable GST tax (0%, 5%, 12%, 18%, 28%).
- Customer selector with integrated "+ New Customer" inline modal.
- Multi-channel settlement: **Cash**, **UPI / QR Code**, **Card**, **Credit / Khata (Udhaar)**, or **Split Payment**.
- **Atomic Transaction Guarantee**: Billing checks stock availability, inserts invoice & line items, reduces inventory quantity, logs movement audit, and writes to customer ledger — committing atomically or rolling back completely on error.
- Printable thermal receipt and standard A4 invoice layouts.

### Module D — Digital Customer Khata (Signature Feature)
- Replaces traditional paper bahi-khata registers with a permanent, double-entry style audit ledger.
- Customer directory with visual outstanding debt badges (`Due` vs `Clear`).
- Full chronological transaction timeline for each customer:
  - `CREDIT_SALE`: Records udhaar sale, references invoice #, increases running balance.
  - `PAYMENT_RECEIVED`: Records customer payment (Cash, UPI, Cheque), decreases balance.
  - `DEBIT_ADJUSTMENT` & `CREDIT_ADJUSTMENT`: Handles manual reconciliations and waivers.
- One-click customer statement export to CSV.

### Module E — Customer Relationship Management
- Add/edit customer profiles, mobile numbers, email, physical addresses, and opening balance.
- Total purchases and payment history tracking.

### Module F — Suppliers & Vendor Network
- Wholesale distributor registry with contact persons, phone numbers, addresses, and GSTIN.

### Module G — Financial Intelligence & Reports
- Stream API-driven analytics: Top-selling products sorted by revenue, Payment channel breakdown, and Store operating expense distribution.
- Clear separation between Gross Revenue, Cost of Goods Sold, Store Expenses, and Net Profit.

### Module H — Business Profile & Settings
- Commercial profile configuration: Store trade name, GSTIN, phone, address, currency (INR ₹), default tax rate, low-stock alert threshold, and invoice numbering prefix.

---

## 4. Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.5 (used strictly for HTTP routing & config; persistence is 100% pure JDBC & PreparedStatements)
- **Database**: Dual-Mode RDBMS (Official MySQL 8.x Community Server + High-performance local MySQL-compatible engine for zero-config evaluation)
- **Frontend**: React 18, TypeScript, Tailwind CSS, Lucide Icons, Vite
- **Testing**: JUnit 5, AssertJ, Spring Boot Test
- **Build Tools**: Apache Maven 3.9.9, Node.js 26.x, npm 12.x

---

## 5. Quick Start & Execution Guide

### Option 1: Run the Unified Full-Stack Application (One Command)
The built production React frontend is embedded directly within the Spring Boot web server:

```powershell
# Using the provided Maven runner:
.\mvn.bat spring-boot:run -f backend/pom.xml
```

Once started:
- Open your browser to: **`http://localhost:8080/`** (Accesses the full React dashboard)
- API Documentation & Health: **`http://localhost:8080/api/dashboard/stats`**

### Option 2: Run in Development Mode (Vite Hot-Reloading + Java API)
If modifying frontend components:

1. **Terminal 1 (Java 21 Backend)**:
   ```powershell
   .\mvn.bat spring-boot:run -f backend/pom.xml
   ```
2. **Terminal 2 (React Vite Dev Server)**:
   ```powershell
   cd frontend
   npm run dev
   ```
   Open **`http://localhost:5173/`**

### Running Automated Test Suite:
```powershell
.\mvn.bat test -f backend/pom.xml
```
*Executes all 5 transaction rollback, stock safety, and customer khata tests.*

---

## 6. Seed Data & Credentials

The application initializes with realistic store records for demonstration:
- **Default Store**: Sharma Supermart & Wholesale, Chandigarh (GSTIN: `07AAAAA0000A1Z5`)
- **Admin User**: Rajesh Sharma (`admin` / `admin123`)
- **Pre-loaded Products**: 10 FMCG and grocery items across 5 categories, including deliberately configured low-stock products to demonstrate real-time alerts.
- **Pre-loaded Khata Ledgers**: Customers with active udhaar balances (Sunil Mehra, Priya Aggarwal, Deepak Verma) and historical transaction timelines.
