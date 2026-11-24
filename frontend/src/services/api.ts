import { AuthResponse, Business, Customer, DashboardStats, Expense, GenericResponse, Invoice, LedgerEntry, Payment, Product, Supplier, User } from '../types';

const API_BASE = '/api';

export function getActiveBusinessId(): number {
  try {
    const saved = localStorage.getItem('ledgepro_business');
    if (saved) {
      const b = JSON.parse(saved);
      if (b && b.id) return Number(b.id);
    }
  } catch {}
  return 1;
}

async function request<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const activeBusinessId = getActiveBusinessId();
  const token = localStorage.getItem('ledgepro_token');

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'X-Business-Id': String(activeBusinessId),
    ...(options?.headers as Record<string, string> || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
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
  getDashboardStats: (businessId = getActiveBusinessId()) => 
    request<DashboardStats>(`/dashboard/stats?businessId=${businessId}`),
  getLiveLowStockAlerts: (businessId = getActiveBusinessId()) => 
    request<Product[]>(`/dashboard/alerts/low-stock?businessId=${businessId}`),

  // Products
  getProducts: (businessId = getActiveBusinessId(), search?: string, categoryId?: number, sortBy?: string, ascending = true) => {
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
  getLowStockProducts: (businessId = getActiveBusinessId()) => 
    request<Product[]>(`/products/low-stock?businessId=${businessId}`),
  createProduct: (product: Product) => {
    product.businessId = product.businessId || getActiveBusinessId();
    return request<Product>('/products', { method: 'POST', body: JSON.stringify(product) });
  },
  updateProduct: (id: number, product: Product) => {
    product.businessId = product.businessId || getActiveBusinessId();
    return request<Product>(`/products/${id}`, { method: 'PUT', body: JSON.stringify(product) });
  },
  deleteProduct: (id: number) => request<boolean>(`/products/${id}`, { method: 'DELETE' }),

  // Customers
  getCustomers: (businessId = getActiveBusinessId(), search?: string, outstandingOnly = false) => {
    const params = new URLSearchParams({ businessId: String(businessId), outstandingOnly: String(outstandingOnly) });
    if (search) params.append('search', search);
    return request<Customer[]>(`/customers?${params.toString()}`);
  },
  getCustomerById: (id: number) => request<Customer>(`/customers/${id}`),
  createCustomer: (customer: Customer) => {
    customer.businessId = customer.businessId || getActiveBusinessId();
    return request<Customer>('/customers', { method: 'POST', body: JSON.stringify(customer) });
  },
  updateCustomer: (id: number, customer: Customer) => {
    customer.businessId = customer.businessId || getActiveBusinessId();
    return request<Customer>(`/customers/${id}`, { method: 'PUT', body: JSON.stringify(customer) });
  },
  deleteCustomer: (id: number) => request<boolean>(`/customers/${id}`, { method: 'DELETE' }),

  // POS Invoices
  getAllInvoices: (businessId = getActiveBusinessId()) => 
    request<Invoice[]>(`/invoices?businessId=${businessId}`),
  getInvoiceById: (id: number) => request<Invoice>(`/invoices/${id}`),
  getInvoiceByNumber: (invoiceNumber: string) => request<Invoice>(`/invoices/number/${invoiceNumber}`),
  checkoutInvoice: (invoice: Invoice) => {
    invoice.businessId = invoice.businessId || getActiveBusinessId();
    return request<Invoice>('/invoices/checkout', { method: 'POST', body: JSON.stringify(invoice) });
  },

  // Digital Khata
  getCustomerLedgerTimeline: (customerId: number) => request<LedgerEntry[]>(`/khata/customer/${customerId}`),
  getRecentKhataActivity: (businessId = getActiveBusinessId(), limit = 15) => 
    request<LedgerEntry[]>(`/khata/activity?businessId=${businessId}&limit=${limit}`),
  recordKhataPayment: (payload: { businessId?: number; customerId: number; amount: number; paymentMethod: string; referenceNumber?: string; notes?: string }) => {
    payload.businessId = payload.businessId || getActiveBusinessId();
    return request<Payment>('/khata/payment', { method: 'POST', body: JSON.stringify(payload) });
  },
  recordAdjustment: (payload: { businessId?: number; customerId: number; entryType: string; amount: number; description: string }) => {
    payload.businessId = payload.businessId || getActiveBusinessId();
    return request<LedgerEntry>('/khata/adjustment', { method: 'POST', body: JSON.stringify(payload) });
  },

  // Reports & Analytics
  getTopSellingProducts: (businessId = getActiveBusinessId(), limit = 5) => 
    request<{ productName: string; totalRevenue: number }[]>(`/reports/top-products?businessId=${businessId}&limit=${limit}`),
  getSalesByPaymentMethod: (businessId = getActiveBusinessId()) => 
    request<Record<string, number>>(`/reports/payment-methods?businessId=${businessId}`),
  getInventoryHealth: (businessId = getActiveBusinessId()) => 
    request<any>(`/reports/inventory-health?businessId=${businessId}`),
  getFinancialSummary: (businessId = getActiveBusinessId()) => 
    request<any>(`/reports/financial-summary?businessId=${businessId}`),
  getExpensesByCategory: (businessId = getActiveBusinessId()) => 
    request<Record<string, number>>(`/reports/expenses-by-category?businessId=${businessId}`),

  // Suppliers
  getSuppliers: (businessId = getActiveBusinessId(), search?: string) => {
    const params = new URLSearchParams({ businessId: String(businessId) });
    if (search) params.append('search', search);
    return request<Supplier[]>(`/suppliers?${params.toString()}`);
  },
  createSupplier: (supplier: Supplier) => {
    supplier.businessId = supplier.businessId || getActiveBusinessId();
    return request<Supplier>('/suppliers', { method: 'POST', body: JSON.stringify(supplier) });
  },
  deleteSupplier: (id: number) => request<boolean>(`/suppliers/${id}`, { method: 'DELETE' }),

  // Expenses
  getExpenses: (businessId = getActiveBusinessId(), category?: string) => {
    const params = new URLSearchParams({ businessId: String(businessId) });
    if (category) params.append('category', category);
    return request<Expense[]>(`/expenses?${params.toString()}`);
  },
  createExpense: (expense: Expense) => {
    expense.businessId = expense.businessId || getActiveBusinessId();
    return request<Expense>('/expenses', { method: 'POST', body: JSON.stringify(expense) });
  },
  deleteExpense: (id: number) => request<boolean>(`/expenses/${id}`, { method: 'DELETE' }),

  // Business Profile & Settings
  getBusinessProfile: (businessId = getActiveBusinessId()) => 
    request<Business>(`/settings/business?businessId=${businessId}`),
  updateBusinessProfile: (business: Business) => {
    business.id = business.id || getActiveBusinessId();
    return request<Business>('/settings/business', { method: 'PUT', body: JSON.stringify(business) });
  },
};
