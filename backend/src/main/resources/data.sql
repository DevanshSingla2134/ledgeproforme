-- ============================================================
-- LEDGE PRO - Initial Seed Data for Demo & Evaluation
-- ============================================================

-- 1. Default Admin User (Password is hashed or demo sha256)
INSERT INTO users (id, username, password_hash, full_name, email, role) 
VALUES (1, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Rajesh Sharma', 'rajesh@sharmaretails.in', 'ADMIN');

-- 2. Business Profile: Sharma Supermart & Wholesale
INSERT INTO businesses (id, user_id, name, tagline, tax_id, phone, email, address, currency, currency_symbol, invoice_prefix, next_invoice_number, tax_rate, low_stock_threshold)
VALUES (1, 1, 'Sharma Supermart & Wholesale', 'Your Business. One Smart Ledger.', '07AAAAA0000A1Z5', '+91 98765 43210', 'contact@sharmaretails.in', 'Shop 14, Main Market, Sector 17, Chandigarh - 160017', 'INR', '₹', 'LP-2026-', 1005, 18.00, 10);

-- 3. Categories
INSERT INTO categories (id, business_id, name, description) VALUES
(1, 1, 'Groceries & Staples', 'Daily grocery items, grains, spices and packaged essentials'),
(2, 1, 'Personal Care', 'Soaps, shampoos, oral care, and skin hygiene'),
(3, 1, 'Beverages & Dairy', 'Packaged milk, tea, coffee, juices, and soft drinks'),
(4, 1, 'Snacks & Packaged Food', 'Biscuits, noodles, chips, chocolates'),
(5, 1, 'Household Essentials', 'Detergents, cleaners, mosquito repellents');

-- 4. Suppliers
INSERT INTO suppliers (id, business_id, name, contact_person, phone, email, gstin, address) VALUES
(1, 1, 'Hindustan Fast Consumer Goods Ltd', 'Vikas Gupta', '+91 98111 22334', 'orders@hfcgdist.com', '07AAACH1234D1Z2', 'Industrial Area Phase 1, Chandigarh'),
(2, 1, 'Amrit Dairy & Agro Suppliers', 'Gurpreet Singh', '+91 98222 33445', 'amritdairy@agro.in', '07AABCA5678E1Z1', 'Mohali Phase 7, Punjab'),
(3, 1, 'ITC Distribution Network', 'Manish Verma', '+91 98333 44556', 'manish.itc@fmcgagency.com', '07AABCI9012F1Z8', 'Panchkula Sector 2, Haryana');

-- 5. Products (With variety of stock levels including low-stock items to demonstrate alerts)
INSERT INTO products (id, business_id, sku, barcode, name, category_id, supplier_id, cost_price, selling_price, quantity, min_stock_level, description) VALUES
(1, 1, 'GRO-AAS-001', '8901030010012', 'Aashirvaad Shudh Chakki Atta 10kg', 1, 1, 380.00, 440.00, 45, 10, '100% whole wheat atta with 0% maida'),
(2, 1, 'GRO-BAS-002', '8901030010029', 'India Gate Basmati Rice Super 5kg', 1, 1, 520.00, 625.00, 28, 8, 'Premium aromatic long grain basmati rice'),
(3, 1, 'GRO-OIL-003', '8901030010036', 'Fortune Sunlite Refined Sunflower Oil 1L', 1, 1, 120.00, 145.00, 60, 15, 'Refined cooking oil enriched with vitamins A & D'),
(4, 1, 'BEV-TEA-004', '8901030010043', 'Tata Tea Gold Leaf Tea 500g', 3, 3, 260.00, 310.00, 34, 10, 'Rich aroma and strength blend tea'),
(5, 1, 'BEV-MIL-005', '8901030010050', 'Amul Taaza Homogenised Toned Milk 1L', 3, 2, 60.00, 72.00, 6, 12, 'Long life toned milk - [LOW STOCK DEMO]'),
(6, 1, 'PER-DOV-006', '8901030010067', 'Dove Moisturizing Beauty Bathing Soap 125g (Pack of 3)', 2, 1, 165.00, 210.00, 22, 5, 'Gentle cleanser with 1/4th moisturizing cream'),
(7, 1, 'PER-COL-007', '8901030010074', 'Colgate MaxFresh Peppermint Toothpaste 150g', 2, 1, 95.00, 125.00, 4, 10, 'Cooling crystals toothpaste - [LOW STOCK DEMO]'),
(8, 1, 'SNK-MAG-008', '8901030010081', 'Maggi 2-Minute Masala Instant Noodles 420g', 4, 3, 72.00, 90.00, 50, 15, 'Special masala instant noodles 6-pack'),
(9, 1, 'SNK-PAR-009', '8901030010098', 'Parle-G Gold Glucose Biscuits 1kg', 4, 1, 90.00, 115.00, 40, 12, 'Classic glucose biscuits'),
(10, 1, 'HOU-SUR-010', '8901030010104', 'Surf Excel Easy Wash Detergent Powder 1kg', 5, 1, 110.00, 138.00, 18, 8, 'Superior laundry detergent powder');

-- 6. Customers (With realistic Udhaar Khata balances)
INSERT INTO customers (id, business_id, name, phone, email, address, opening_balance, current_balance) VALUES
(1, 1, 'Sunil Mehra', '+91 98888 11111', 'sunil.mehra@gmail.com', 'House 204, Sector 15-A, Chandigarh', 0.00, 1250.00),
(2, 1, 'Priya Aggarwal', '+91 98888 22222', 'priya.agg@yahoo.co.in', 'Flat 402, Royal Enclave, Mohali', 500.00, 2840.00),
(3, 1, 'Amitabh Joshi', '+91 98888 33333', 'amitabh.joshi@outlook.com', 'SCO 45, Sector 22, Chandigarh', 0.00, 0.00),
(4, 1, 'Deepak Verma', '+91 98888 44444', 'deepak.verma@hotmail.com', 'House 88, Sector 18, Panchkula', 0.00, 3450.00),
(5, 1, 'Kavita Chawla', '+91 98888 55555', 'kavita.c@gmail.com', 'House 512, Sector 35-D, Chandigarh', 0.00, 420.00);

-- 7. Invoices (Demonstrating Cash, UPI, and Digital Khata Credit Sales)
INSERT INTO invoices (id, business_id, invoice_number, customer_id, subtotal, discount_rate, discount_amount, tax_rate, tax_amount, grand_total, paid_amount, balance_amount, payment_method, status, notes) VALUES
(1, 1, 'LP-2026-1001', 1, 1250.00, 0.00, 0.00, 0.00, 0.00, 1250.00, 0.00, 1250.00, 'KHATA', 'UNPAID', 'Udhaar sale added to Sunil Mehra ledger'),
(2, 1, 'LP-2026-1002', 3, 1845.00, 5.00, 92.25, 0.00, 0.00, 1752.75, 1752.75, 0.00, 'UPI', 'PAID', 'Paid via GooglePay UPI ref: UPI/402198'),
(3, 1, 'LP-2026-1003', 2, 2840.00, 0.00, 0.00, 0.00, 0.00, 2840.00, 0.00, 2840.00, 'KHATA', 'UNPAID', 'Credit sale monthly ration billed to Priya Aggarwal'),
(4, 1, 'LP-2026-1004', NULL, 690.00, 0.00, 0.00, 0.00, 0.00, 690.00, 690.00, 0.00, 'CASH', 'PAID', 'Direct walk-in counter retail sale');

-- 8. Invoice Items
INSERT INTO invoice_items (id, invoice_id, product_id, product_name, sku, quantity, cost_price, unit_price, total_price) VALUES
(1, 1, 1, 'Aashirvaad Shudh Chakki Atta 10kg', 'GRO-AAS-001', 2, 380.00, 440.00, 880.00),
(2, 1, 4, 'Tata Tea Gold Leaf Tea 500g', 'BEV-TEA-004', 1, 260.00, 310.00, 310.00),
(3, 1, 9, 'Parle-G Gold Glucose Biscuits 1kg', 'SNK-PAR-009', 1, 90.00, 60.00, 60.00),

(4, 2, 2, 'India Gate Basmati Rice Super 5kg', 'GRO-BAS-002', 2, 520.00, 625.00, 1250.00),
(5, 2, 3, 'Fortune Sunlite Refined Sunflower Oil 1L', 'GRO-OIL-003', 4, 120.00, 145.00, 580.00),

(6, 3, 1, 'Aashirvaad Shudh Chakki Atta 10kg', 'GRO-AAS-001', 4, 380.00, 440.00, 1760.00),
(7, 3, 2, 'India Gate Basmati Rice Super 5kg', 'GRO-BAS-002', 1, 520.00, 625.00, 625.00),
(8, 3, 8, 'Maggi 2-Minute Masala Instant Noodles 420g', 'SNK-MAG-008', 5, 72.00, 91.00, 455.00),

(9, 4, 6, 'Dove Moisturizing Beauty Bathing Soap 125g (Pack of 3)', 'PER-DOV-006', 2, 165.00, 210.00, 420.00),
(10, 4, 10, 'Surf Excel Easy Wash Detergent Powder 1kg', 'HOU-SUR-010', 2, 110.00, 135.00, 270.00);

-- 9. Payments (Direct settlements)
INSERT INTO payments (id, business_id, customer_id, invoice_id, amount, payment_method, reference_number, notes) VALUES
(1, 1, 3, 2, 1752.75, 'UPI', 'UPI/402198', 'Immediate payment settlement on billing'),
(2, 1, NULL, 4, 690.00, 'CASH', 'COUNTER-CASH', 'Walk-in cash counter sale');

-- 10. Digital Khata Ledger Entries (Signature Feature Demonstration)
INSERT INTO ledger_entries (id, business_id, customer_id, entry_type, amount, running_balance, reference_invoice_id, description) VALUES
(1, 1, 1, 'CREDIT_SALE', 1250.00, 1250.00, 1, 'Credit purchase on Invoice #LP-2026-1001'),
(2, 1, 2, 'CREDIT_SALE', 2840.00, 3340.00, 3, 'Monthly grocery credit on Invoice #LP-2026-1003'),
(3, 1, 2, 'PAYMENT_RECEIVED', 500.00, 2840.00, NULL, 'Cash deposit received against previous opening balance'),
(4, 1, 4, 'CREDIT_SALE', 4500.00, 4500.00, NULL, 'Bulk dry fruits purchase on udhaar'),
(5, 1, 4, 'PAYMENT_RECEIVED', 1050.00, 3450.00, NULL, 'Part-payment received via PhonePe UPI'),
(6, 1, 5, 'CREDIT_SALE', 420.00, 420.00, NULL, 'Daily dairy and bakery items udhaar');

-- 11. Expenses (To demonstrate real Revenue vs Profit calculation)
INSERT INTO expenses (id, business_id, category, amount, description, payment_method) VALUES
(1, 1, 'Rent', 25000.00, 'Shop commercial lease rent for November', 'BANK_TRANSFER'),
(2, 1, 'Utilities', 4200.00, 'Electricity bill for commercial air-conditioning and cold storage', 'UPI'),
(3, 1, 'Transport', 1500.00, 'Goods carriage tempo fare from wholesale mandi', 'CASH'),
(4, 1, 'Packaging', 1200.00, 'Biodegradable carry bags and thermal billing rolls', 'CASH');

-- 12. Stock Movements Audit Trail
INSERT INTO stock_movements (id, business_id, product_id, movement_type, quantity_changed, resulting_quantity, reference_type, reference_id, notes) VALUES
(1, 1, 1, 'SALE', -2, 45, 'INVOICE', 1, 'POS sale on invoice #LP-2026-1001'),
(2, 1, 4, 'SALE', -1, 34, 'INVOICE', 1, 'POS sale on invoice #LP-2026-1001'),
(3, 1, 2, 'SALE', -2, 28, 'INVOICE', 2, 'POS sale on invoice #LP-2026-1002'),
(4, 1, 3, 'SALE', -4, 60, 'INVOICE', 2, 'POS sale on invoice #LP-2026-1002'),
(5, 1, 1, 'SALE', -4, 41, 'INVOICE', 3, 'POS sale on invoice #LP-2026-1003'),
(6, 1, 5, 'ADJUSTMENT', -6, 6, 'MANUAL', NULL, 'Stock verification low count alert');
