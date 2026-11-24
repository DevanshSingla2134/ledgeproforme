export interface User {
  id: number;
  username: string;
  fullName: string;
  email: string;
  role: string;
  createdAt?: string;
}

export interface AuthResponse {
  authenticated: boolean;
  token: string;
  user: User;
  business: Business;
  message: string;
}

export interface GenericResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
  timestamp: string;
}

export interface Product {
  id?: number;
  businessId?: number;
  sku: string;
  barcode?: string;
  name: string;
  categoryId?: number;
  categoryName?: string;
  supplierId?: number;
  supplierName?: string;
  costPrice: number;
  sellingPrice: number;
  quantity: number;
  minStockLevel: number;
  description?: string;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Customer {
  id?: number;
  businessId?: number;
  name: string;
  phone: string;
  email?: string;
  address?: string;
  openingBalance?: number;
  currentBalance?: number;
  totalPurchases?: number;
  totalPayments?: number;
  lastTransactionDate?: string;
  createdAt?: string;
}

export interface InvoiceItem {
  id?: number;
  invoiceId?: number;
  productId?: number;
  productName: string;
  sku?: string;
  quantity: number;
  costPrice?: number;
  unitPrice: number;
  totalPrice: number;
}

export interface Invoice {
  id?: number;
  businessId?: number;
  invoiceNumber?: string;
  customerId?: number | null;
  customerName?: string;
  customerPhone?: string;
  subtotal: number;
  discountRate: number;
  discountAmount: number;
  taxRate: number;
  taxAmount: number;
  grandTotal: number;
  paidAmount: number;
  balanceAmount: number;
  paymentMethod: 'CASH' | 'UPI' | 'CARD' | 'KHATA' | 'SPLIT';
  status?: 'PAID' | 'PARTIAL' | 'UNPAID';
  notes?: string;
  items: InvoiceItem[];
  createdAt?: string;
}

export interface LedgerEntry {
  id: number;
  businessId: number;
  customerId: number;
  customerName: string;
  entryType: 'CREDIT_SALE' | 'PAYMENT_RECEIVED' | 'DEBIT_ADJUSTMENT' | 'CREDIT_ADJUSTMENT';
  amount: number;
  runningBalance: number;
  referenceInvoiceId?: number | null;
  description: string;
  createdAt: string;
}

export interface Payment {
  id?: number;
  businessId: number;
  customerId?: number;
  invoiceId?: number;
  amount: number;
  paymentMethod: string;
  referenceNumber?: string;
  notes?: string;
  createdAt?: string;
}

export interface Supplier {
  id?: number;
  businessId?: number;
  name: string;
  contactPerson?: string;
  phone: string;
  email?: string;
  gstin?: string;
  address?: string;
  createdAt?: string;
}

export interface Expense {
  id?: number;
  businessId?: number;
  category: string;
  amount: number;
  description?: string;
  expenseDate?: string;
  paymentMethod?: string;
}

export interface DashboardStats {
  totalSalesToday: number;
  totalRevenueThisMonth: number;
  totalOrders: number;
  totalProducts: number;
  lowStockCount: number;
  totalCustomers: number;
  outstandingKhataBalance: number;
  paymentsReceived: number;
  estimatedGrossProfit: number;
  totalExpenses: number;
  recentInvoices: Invoice[];
  recentLedgerActivity: LedgerEntry[];
  lowStockProducts: Product[];
}

export interface Business {
  id?: number;
  userId?: number;
  name: string;
  tagline: string;
  taxId: string;
  phone: string;
  email: string;
  address: string;
  currency: string;
  currencySymbol: string;
  invoicePrefix: string;
  nextInvoiceNumber: number;
  taxRate: number;
  lowStockThreshold: number;
}
