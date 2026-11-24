import { AuthResponse, Business, Customer, DashboardStats, Expense, GenericResponse, Invoice, LedgerEntry, Payment, Product, Supplier, User } from '../types';

const API_BASE = '/api';

async function request<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${endpoint}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!res.ok) {
    let errorMsg = `Server error: ${res.statusText}`;
    try {
      const errJson = await res.json();
      if (errJson.message) errorMsg = errJson.message;
    } catch {
      // fallback
    }
    throw new Error(errorMsg);
  }

  const json: GenericResponse<T> = await res.json();
  if (!json.success) {
    throw new Error(json.message || 'API request failed');
  }
  return json.data;
}

export const api = {
  // Authentication
  login: (credentials: { usernameOrEmail: string; password: string }) =>
    request<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
  register: (payload: { fullName: string; businessName: string; phone?: string; username: string; email: string; password: string }) =>
    request<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify(payload) }),

  // Dashboard
  getDashboardStats: (businessId = 1) => request<DashboardStats>(`/dashboard/stats?businessId=${businessId}`),
  getLiveLowStockAlerts: () => request<Product[]>('/dashboard/alerts/low-stock'),

  // Products
  getProducts: (businessId = 1, search?: string, categoryId?: number, sortBy?: string, ascending = true) => {
    const params = new URLSearchParams({ businessId: String(businessId) });
    if (search) params.append('search', search);
    if (categoryId) params.append('categoryId', String(categoryId));
    if (sortBy) {
      params.append('sortBy', sortBy);
      params.append('ascending', String(ascending));
    }
    return request<Product[]>(`/products?${params.toString()}`);
  },
  getProductById: (id: number) => request<Product>(`/products/${id}`),
  getLowStockProducts: (businessId = 1) => request<Product[]>(`/products/low-stock?businessId=${businessId}`),
  createProduct: (product: Product) => request<Product>('/products', { method: 'POST', body: JSON.stringify(product) }),
  updateProduct: (id: number, product: Product) => request<Product>(`/products/${id}`, { method: 'PUT', body: JSON.stringify(product) }),
  deleteProduct: (id: number) => request<boolean>(`/products/${id}`, { method: 'DELETE' }),

  // Customers
  getCustomers: (businessId = 1, search?: string, outstandingOnly = false) => {
    const params = new URLSearchParams({ businessId: String(businessId), outstandingOnly: String(outstandingOnly) });
    if (search) params.append('search', search);
    return request<Customer[]>(`/customers?${params.toString()}`);
  },
  getCustomerById: (id: number) => request<Customer>(`/customers/${id}`),
  createCustomer: (customer: Customer) => request<Customer>('/customers', { method: 'POST', body: JSON.stringify(customer) }),
  updateCustomer: (id: number, customer: Customer) => request<Customer>(`/customers/${id}`, { method: 'PUT', body: JSON.stringify(customer) }),
  deleteCustomer: (id: number) => request<boolean>(`/customers/${id}`, { method: 'DELETE' }),

  // POS Invoices
  getAllInvoices: (businessId = 1) => request<Invoice[]>(`/invoices?businessId=${businessId}`),
  getInvoiceById: (id: number) => request<Invoice>(`/invoices/${id}`),
  getInvoiceByNumber: (invoiceNumber: string) => request<Invoice>(`/invoices/number/${invoiceNumber}`),
  checkoutInvoice: (invoice: Invoice) => request<Invoice>('/invoices/checkout', { method: 'POST', body: JSON.stringify(invoice) }),

  // Digital Khata
  getCustomerLedgerTimeline: (customerId: number) => request<LedgerEntry[]>(`/khata/customer/${customerId}`),
  getRecentKhataActivity: (businessId = 1, limit = 15) => request<LedgerEntry[]>(`/khata/activity?businessId=${businessId}&limit=${limit}`),
  recordKhataPayment: (payload: { businessId: number; customerId: number; amount: number; paymentMethod: string; referenceNumber?: string; notes?: string }) =>
    request<Payment>('/khata/payment', { method: 'POST', body: JSON.stringify(payload) }),
  recordAdjustment: (payload: { businessId: number; customerId: number; entryType: string; amount: number; description: string }) =>
    request<LedgerEntry>('/khata/adjustment', { method: 'POST', body: JSON.stringify(payload) }),

  // Reports & Analytics
  getTopSellingProducts: (businessId = 1, limit = 5) => request<{ productName: string; totalRevenue: number }[]>(`/reports/top-products?businessId=${businessId}&limit=${limit}`),
  getSalesByPaymentMethod: (businessId = 1) => request<Record<string, number>>(`/reports/payment-methods?businessId=${businessId}`),
  getInventoryHealth: (businessId = 1) => request<any>(`/reports/inventory-health?businessId=${businessId}`),
  getFinancialSummary: (businessId = 1) => request<any>(`/reports/financial-summary?businessId=${businessId}`),
  getExpensesByCategory: (businessId = 1) => request<Record<string, number>>(`/reports/expenses-by-category?businessId=${businessId}`),

  // Suppliers
  getSuppliers: (businessId = 1, search?: string) => {
    const params = new URLSearchParams({ businessId: String(businessId) });
    if (search) params.append('search', search);
    return request<Supplier[]>(`/suppliers?${params.toString()}`);
  },
  createSupplier: (supplier: Supplier) => request<Supplier>('/suppliers', { method: 'POST', body: JSON.stringify(supplier) }),
  deleteSupplier: (id: number) => request<boolean>(`/suppliers/${id}`, { method: 'DELETE' }),

  // Expenses
  getExpenses: (businessId = 1, category?: string) => {
    const params = new URLSearchParams({ businessId: String(businessId) });
    if (category) params.append('category', category);
    return request<Expense[]>(`/expenses?${params.toString()}`);
  },
  createExpense: (expense: Expense) => request<Expense>('/expenses', { method: 'POST', body: JSON.stringify(expense) }),
  deleteExpense: (id: number) => request<boolean>(`/expenses/${id}`, { method: 'DELETE' }),

  // Business Profile & Settings
  getBusinessProfile: (businessId = 1) => request<Business>(`/settings/business?businessId=${businessId}`),
  updateBusinessProfile: (business: Business) => request<Business>('/settings/business', { method: 'PUT', body: JSON.stringify(business) }),
};
