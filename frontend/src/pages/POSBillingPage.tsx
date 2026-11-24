import React, { useState, useEffect } from 'react';
import { 
  Search, 
  Plus, 
  Minus, 
  Trash2, 
  Receipt, 
  CreditCard, 
  QrCode, 
  Banknote, 
  BookOpen, 
  UserPlus, 
  Printer, 
  X,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';
import { Customer, Invoice, InvoiceItem, Product } from '../types';
import { api } from '../services/api';

interface POSBillingPageProps {
  onNotify: (msg: string, type?: 'success' | 'error' | 'info') => void;
  onRefreshDashboard?: () => void;
}

export const POSBillingPage: React.FC<POSBillingPageProps> = ({ onNotify, onRefreshDashboard }) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [productSearch, setProductSearch] = useState('');
  const [customerSearch, setCustomerSearch] = useState('');

  // Cart State
  const [cartItems, setCartItems] = useState<InvoiceItem[]>([]);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [discountRate, setDiscountRate] = useState<number>(0);
  const [taxRate, setTaxRate] = useState<number>(18); // Default 18% GST
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'UPI' | 'CARD' | 'KHATA' | 'SPLIT'>('CASH');
  const [paidAmount, setPaidAmount] = useState<number>(0);
  const [notes, setNotes] = useState<string>('');

  // Checkout State & Printable Invoice Modal
  const [isProcessing, setIsProcessing] = useState(false);
  const [completedInvoice, setCompletedInvoice] = useState<Invoice | null>(null);
  const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false);

  // Quick Customer Create Modal
  const [isCustomerModalOpen, setIsCustomerModalOpen] = useState(false);
  const [newCustName, setNewCustName] = useState('');
  const [newCustPhone, setNewCustPhone] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [prodData, custData] = await Promise.all([
        api.getProducts(1),
        api.getCustomers(1),
      ]);
      setProducts(prodData);
      setCustomers(custData);
    } catch (err: any) {
      onNotify('Error loading POS catalog: ' + err.message, 'error');
    }
  };

  const handleAddToCart = (product: Product) => {
    if (product.quantity <= 0) {
      onNotify(`'${product.name}' is out of stock!`, 'error');
      return;
    }

    setCartItems((prev) => {
      const existing = prev.find((item) => item.productId === product.id);
      if (existing) {
        if (existing.quantity >= product.quantity) {
          onNotify(`Cannot add more than ${product.quantity} available units of ${product.name}`, 'error');
          return prev;
        }
        return prev.map((item) =>
          item.productId === product.id
            ? { ...item, quantity: item.quantity + 1, totalPrice: (item.quantity + 1) * item.unitPrice }
            : item
        );
      }
      return [
        ...prev,
        {
          productId: product.id,
          productName: product.name,
          sku: product.sku,
          quantity: 1,
          costPrice: product.costPrice,
          unitPrice: product.sellingPrice,
          totalPrice: product.sellingPrice,
        },
      ];
    });
  };

  const handleUpdateQuantity = (productId: number | undefined, delta: number) => {
    if (!productId) return;
    setCartItems((prev) =>
      prev
        .map((item) => {
          if (item.productId === productId) {
            const product = products.find((p) => p.id === productId);
            const newQty = item.quantity + delta;
            if (product && newQty > product.quantity) {
              onNotify(`Only ${product.quantity} units in stock.`, 'error');
              return item;
            }
            if (newQty <= 0) return null;
            return { ...item, quantity: newQty, totalPrice: newQty * item.unitPrice };
          }
          return item;
        })
        .filter(Boolean) as InvoiceItem[]
    );
  };

  const handleRemoveFromCart = (productId: number | undefined) => {
    setCartItems((prev) => prev.filter((item) => item.productId !== productId));
  };

  // Calculations
  const subtotal = cartItems.reduce((acc, item) => acc + item.totalPrice, 0);
  const discountAmount = (subtotal * discountRate) / 100;
  const taxableAmount = Math.max(0, subtotal - discountAmount);
  const taxAmount = (taxableAmount * taxRate) / 100;
  const grandTotal = taxableAmount + taxAmount;

  // Sync paidAmount when payment method changes
  useEffect(() => {
    if (paymentMethod === 'KHATA') {
      setPaidAmount(0); // Full udhaar by default
    } else if (paymentMethod === 'SPLIT') {
      setPaidAmount(Math.round(grandTotal / 2));
    } else {
      setPaidAmount(grandTotal);
    }
  }, [paymentMethod, grandTotal]);

  const handleCreateQuickCustomer = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCustName || !newCustPhone) {
      onNotify('Name and Phone are required', 'error');
      return;
    }
    try {
      const created = await api.createCustomer({
        name: newCustName,
        phone: newCustPhone,
        openingBalance: 0,
      });
      setCustomers((prev) => [...prev, created]);
      setSelectedCustomer(created);
      setIsCustomerModalOpen(false);
      setNewCustName('');
      setNewCustPhone('');
      onNotify('Customer registered and selected', 'success');
    } catch (err: any) {
      onNotify(err.message, 'error');
    }
  };

  const handleCompleteSale = async () => {
    if (cartItems.length === 0) {
      onNotify('Cart is empty. Add products to continue.', 'error');
      return;
    }

    if (paymentMethod === 'KHATA' && !selectedCustomer) {
      onNotify('Please select a customer for Khata / Udhaar credit billing.', 'error');
      return;
    }

    try {
      setIsProcessing(true);
      const invoicePayload: Invoice = {
        businessId: 1,
        customerId: selectedCustomer?.id || null,
        customerName: selectedCustomer?.name,
        customerPhone: selectedCustomer?.phone,
        subtotal,
        discountRate,
        discountAmount,
        taxRate,
        taxAmount,
        grandTotal,
        paidAmount,
        balanceAmount: Math.max(0, grandTotal - paidAmount),
        paymentMethod,
        notes,
        items: cartItems,
      };

      const result = await api.checkoutInvoice(invoicePayload);
      setCompletedInvoice(result);
      setIsInvoiceModalOpen(true);
      onNotify('Transaction committed! Stock and Khata updated atomically.', 'success');

      // Clear cart
      setCartItems([]);
      setSelectedCustomer(null);
      setDiscountRate(0);
      setNotes('');
      loadData(); // refresh stock numbers
      if (onRefreshDashboard) onRefreshDashboard();
    } catch (err: any) {
      onNotify(err.message || 'Billing transaction failed', 'error');
    } finally {
      setIsProcessing(false);
    }
  };

  const filteredProducts = products.filter(
    (p) =>
      p.name.toLowerCase().includes(productSearch.toLowerCase()) ||
      p.sku.toLowerCase().includes(productSearch.toLowerCase()) ||
      (p.barcode && p.barcode.includes(productSearch))
  );

  return (
    <div className="p-6 max-w-[1600px] mx-auto grid grid-cols-1 lg:grid-cols-12 gap-6 h-[calc(100vh-4.5rem)]">
      {/* Left 7 Columns: Product Catalog & Fast Picker */}
      <div className="lg:col-span-7 flex flex-col bg-white rounded-3xl border border-slate-200 shadow-sm p-6 overflow-hidden">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100">
          <div>
            <h1 className="text-xl font-black text-slate-900 tracking-tight flex items-center gap-2">
              <Receipt className="w-5 h-5 text-blue-600" />
              <span>Point of Sale Terminal</span>
            </h1>
            <p className="text-xs text-slate-400 font-medium">Click items to add to billing cart</p>
          </div>

          <div className="relative w-72">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={productSearch}
              onChange={(e) => setProductSearch(e.target.value)}
              placeholder="Search products or scan barcode..."
              className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600"
            />
          </div>
        </div>

        {/* Product Cards Grid */}
        <div className="flex-1 overflow-y-auto pt-4 pr-1 grid grid-cols-2 sm:grid-cols-3 gap-3.5">
          {filteredProducts.map((p) => {
            const inCart = cartItems.find((i) => i.productId === p.id);
            const isOutOfStock = p.quantity <= 0;
            return (
              <div
                key={p.id}
                onClick={() => !isOutOfStock && handleAddToCart(p)}
                className={`p-3.5 rounded-2xl border transition-all text-left flex flex-col justify-between select-none ${
                  isOutOfStock
                    ? 'opacity-50 bg-slate-100 border-slate-200 cursor-not-allowed'
                    : inCart
                    ? 'bg-blue-50/60 border-blue-400 shadow-sm cursor-pointer'
                    : 'bg-white border-slate-200 hover:border-blue-300 hover:shadow-md cursor-pointer'
                }`}
              >
                <div>
                  <div className="flex items-start justify-between gap-1">
                    <span className="font-mono text-[10px] text-slate-400 font-bold">{p.sku}</span>
                    <span
                      className={`text-[9px] font-extrabold px-1.5 py-0.5 rounded-full ${
                        isOutOfStock
                          ? 'bg-red-100 text-red-700'
                          : p.quantity <= p.minStockLevel
                          ? 'bg-amber-100 text-amber-800'
                          : 'bg-slate-100 text-slate-600'
                      }`}
                    >
                      {p.quantity} left
                    </span>
                  </div>
                  <h3 className="font-bold text-xs text-slate-900 mt-1.5 line-clamp-2 leading-snug">
                    {p.name}
                  </h3>
                </div>

                <div className="mt-3 pt-2 border-t border-slate-100 flex items-center justify-between">
                  <span className="font-black text-sm text-slate-900">₹{Number(p.sellingPrice).toFixed(2)}</span>
                  {inCart && (
                    <span className="w-5 h-5 rounded-full bg-blue-600 text-white font-extrabold text-[10px] flex items-center justify-center">
                      {inCart.quantity}
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Right 5 Columns: Billing Cart, Customer & Checkout */}
      <div className="lg:col-span-5 flex flex-col bg-white rounded-3xl border border-slate-200 shadow-sm p-6 overflow-hidden">
        {/* Customer Selector Bar */}
        <div className="pb-4 border-b border-slate-100 flex items-center justify-between gap-2">
          <div className="flex-1">
            <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-1">
              Billed Customer {paymentMethod === 'KHATA' && <span className="text-red-500">*</span>}
            </label>
            <select
              value={selectedCustomer?.id || ''}
              onChange={(e) => {
                const cId = Number(e.target.value);
                const found = customers.find((c) => c.id === cId) || null;
                setSelectedCustomer(found);
              }}
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-xs font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
            >
              <option value="">Walk-in Retail Customer</option>
              {customers.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} ({c.phone}) {c.currentBalance && c.currentBalance > 0 ? `• Due ₹${c.currentBalance}` : ''}
                </option>
              ))}
            </select>
          </div>

          <button
            onClick={() => setIsCustomerModalOpen(true)}
            className="mt-4 p-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-bold transition-colors"
            title="Register New Customer"
          >
            <UserPlus className="w-4 h-4" />
          </button>
        </div>

        {/* Cart Items List */}
        <div className="flex-1 overflow-y-auto py-3 space-y-2 pr-1">
          {cartItems.length === 0 ? (
            <div className="h-full flex flex-col items-center justify-center text-center p-8 text-slate-400">
              <Receipt className="w-12 h-12 text-slate-200 mb-2" />
              <p className="text-xs font-medium">Cart is empty.</p>
              <p className="text-[11px] text-slate-400">Select products on the left to add items.</p>
            </div>
          ) : (
            cartItems.map((item) => (
              <div key={item.productId} className="p-3 bg-slate-50/80 rounded-2xl border border-slate-200/80 flex items-center justify-between gap-2">
                <div className="flex-1 min-w-0">
                  <h4 className="font-bold text-xs text-slate-900 truncate">{item.productName}</h4>
                  <div className="text-[11px] text-slate-500 mt-0.5">
                    ₹{Number(item.unitPrice).toFixed(2)} &times; {item.quantity} = <span className="font-bold text-slate-800">₹{Number(item.totalPrice).toFixed(2)}</span>
                  </div>
                </div>

                <div className="flex items-center gap-1.5">
                  <button
                    onClick={() => handleUpdateQuantity(item.productId, -1)}
                    className="w-6 h-6 rounded-lg bg-white border border-slate-200 flex items-center justify-center text-slate-600 hover:bg-slate-100 font-bold"
                  >
                    <Minus className="w-3 h-3" />
                  </button>
                  <span className="font-extrabold text-xs text-slate-900 w-5 text-center">{item.quantity}</span>
                  <button
                    onClick={() => handleUpdateQuantity(item.productId, 1)}
                    className="w-6 h-6 rounded-lg bg-white border border-slate-200 flex items-center justify-center text-slate-600 hover:bg-slate-100 font-bold"
                  >
                    <Plus className="w-3 h-3" />
                  </button>
                  <button
                    onClick={() => handleRemoveFromCart(item.productId)}
                    className="p-1 text-slate-400 hover:text-red-600 transition-colors ml-1"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Bill Calculations & Payment Options */}
        <div className="pt-3 border-t border-slate-100 space-y-3">
          <div className="space-y-1.5 text-xs text-slate-600">
            <div className="flex justify-between">
              <span>Cart Subtotal</span>
              <span className="font-bold text-slate-900">₹{subtotal.toFixed(2)}</span>
            </div>

            <div className="flex items-center justify-between gap-2">
              <span className="flex items-center gap-1">
                Discount
                <input
                  type="number"
                  min="0"
                  max="100"
                  value={discountRate}
                  onChange={(e) => setDiscountRate(Math.max(0, Math.min(100, parseFloat(e.target.value) || 0)))}
                  className="w-12 px-1 py-0.5 border border-slate-200 rounded text-center font-bold text-xs"
                />
                %
              </span>
              <span className="text-red-600 font-medium">-₹{discountAmount.toFixed(2)}</span>
            </div>

            <div className="flex items-center justify-between gap-2">
              <span className="flex items-center gap-1">
                GST Tax
                <select
                  value={taxRate}
                  onChange={(e) => setTaxRate(parseFloat(e.target.value) || 0)}
                  className="px-1 py-0.5 border border-slate-200 rounded font-bold text-xs"
                >
                  <option value="0">0%</option>
                  <option value="5">5%</option>
                  <option value="12">12%</option>
                  <option value="18">18%</option>
                  <option value="28">28%</option>
                </select>
              </span>
              <span className="font-medium text-slate-700">+₹{taxAmount.toFixed(2)}</span>
            </div>

            <div className="pt-2 border-t border-slate-200 flex justify-between items-center">
              <span className="font-extrabold text-sm text-slate-900">Grand Total</span>
              <span className="font-black text-xl text-blue-600">₹{grandTotal.toFixed(2)}</span>
            </div>
          </div>

          {/* Payment Method Selector */}
          <div>
            <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-1.5">
              Payment Method
            </label>
            <div className="grid grid-cols-4 gap-1.5">
              {[
                { id: 'CASH', label: 'Cash', icon: Banknote },
                { id: 'UPI', label: 'UPI / QR', icon: QrCode },
                { id: 'CARD', label: 'Card', icon: CreditCard },
                { id: 'KHATA', label: 'Credit (Khata)', icon: BookOpen },
              ].map((m) => {
                const Icon = m.icon;
                const isSelected = paymentMethod === m.id;
                return (
                  <button
                    key={m.id}
                    onClick={() => setPaymentMethod(m.id as any)}
                    className={`py-2 px-1.5 rounded-xl border text-[11px] font-bold flex flex-col items-center gap-1 transition-all ${
                      isSelected
                        ? 'bg-blue-600 text-white border-blue-600 shadow-md shadow-blue-500/20'
                        : 'bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100'
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                    <span>{m.label}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Complete Checkout Button */}
          <button
            onClick={handleCompleteSale}
            disabled={isProcessing || cartItems.length === 0}
            className="w-full py-3.5 bg-blue-600 hover:bg-blue-700 active:bg-blue-800 disabled:opacity-50 text-white font-extrabold text-sm rounded-2xl shadow-lg shadow-blue-600/25 flex items-center justify-center gap-2 transition-all"
          >
            <CheckCircle2 className="w-4 h-4" />
            <span>{isProcessing ? 'Processing Transaction...' : `Complete Sale • ₹${grandTotal.toFixed(2)}`}</span>
          </button>
        </div>
      </div>

      {/* Printable Invoice Modal */}
      {isInvoiceModalOpen && completedInvoice && (
        <div className="fixed inset-0 z-50 bg-slate-900/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl shadow-2xl max-w-xl w-full max-h-[90vh] flex flex-col overflow-hidden border border-slate-100">
            <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50">
              <div className="flex items-center gap-2">
                <Receipt className="w-5 h-5 text-blue-600" />
                <span className="font-bold text-sm text-slate-900">Invoice Generated Successfully</span>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => window.print()}
                  className="flex items-center gap-1 px-3 py-1.5 bg-blue-600 text-white font-bold rounded-xl text-xs hover:bg-blue-700 transition-colors shadow-sm"
                >
                  <Printer className="w-3.5 h-3.5" />
                  <span>Print Receipt</span>
                </button>
                <button
                  onClick={() => setIsInvoiceModalOpen(false)}
                  className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>

            {/* Printable Receipt Layout */}
            <div id="printable-invoice" className="p-8 overflow-y-auto space-y-6 text-xs text-slate-800 font-sans">
              {/* Store Header */}
              <div className="text-center space-y-1 pb-4 border-b border-slate-200">
                <h1 className="text-xl font-black text-slate-900 uppercase tracking-tight">Sharma Supermart & Wholesale</h1>
                <p className="text-[11px] text-slate-500">Your Business. One Smart Ledger.</p>
                <p className="text-[11px] text-slate-500">Shop 14, Main Market, Sector 17, Chandigarh | Phone: +91 98765 43210</p>
                <p className="font-mono text-[10px] text-slate-400 font-bold">GSTIN: 07AAAAA0000A1Z5</p>
              </div>

              {/* Invoice Meta */}
              <div className="flex justify-between items-start text-xs">
                <div>
                  <span className="text-slate-400 uppercase tracking-wider text-[10px] block font-bold">Billed To</span>
                  <div className="font-bold text-slate-900 mt-0.5">{completedInvoice.customerName || 'Walk-in Customer'}</div>
                  {completedInvoice.customerPhone && (
                    <div className="text-slate-500 font-mono text-[11px]">{completedInvoice.customerPhone}</div>
                  )}
                </div>
                <div className="text-right">
                  <span className="text-slate-400 uppercase tracking-wider text-[10px] block font-bold">Invoice Details</span>
                  <div className="font-mono font-bold text-slate-900 text-xs mt-0.5">{completedInvoice.invoiceNumber}</div>
                  <div className="text-slate-500 text-[11px]">
                    {completedInvoice.createdAt ? new Date(completedInvoice.createdAt).toLocaleString() : new Date().toLocaleString()}
                  </div>
                </div>
              </div>

              {/* Items Table */}
              <table className="w-full text-left text-xs border-t border-b border-slate-200">
                <thead className="text-[10px] font-bold uppercase tracking-wider text-slate-400 border-b border-slate-100">
                  <tr>
                    <th className="py-2">Item</th>
                    <th className="py-2 text-center">Qty</th>
                    <th className="py-2 text-right">Price</th>
                    <th className="py-2 text-right">Total</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {completedInvoice.items.map((it, idx) => (
                    <tr key={idx}>
                      <td className="py-2 font-medium">{it.productName}</td>
                      <td className="py-2 text-center font-bold">{it.quantity}</td>
                      <td className="py-2 text-right">₹{Number(it.unitPrice).toFixed(2)}</td>
                      <td className="py-2 text-right font-bold">₹{Number(it.totalPrice).toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {/* Totals Summary */}
              <div className="space-y-1.5 text-right text-xs">
                <div>Subtotal: <span className="font-bold">₹{Number(completedInvoice.subtotal).toFixed(2)}</span></div>
                {completedInvoice.discountAmount > 0 && (
                  <div>Discount ({completedInvoice.discountRate}%): <span className="text-red-600 font-medium">-₹{Number(completedInvoice.discountAmount).toFixed(2)}</span></div>
                )}
                {completedInvoice.taxAmount > 0 && (
                  <div>GST Tax ({completedInvoice.taxRate}%): <span className="font-medium">+₹{Number(completedInvoice.taxAmount).toFixed(2)}</span></div>
                )}
                <div className="pt-2 border-t border-slate-200 text-sm font-black text-slate-900">
                  Grand Total: ₹{Number(completedInvoice.grandTotal).toFixed(2)}
                </div>
                <div className="text-slate-600 font-medium">
                  Amount Paid ({completedInvoice.paymentMethod}): ₹{Number(completedInvoice.paidAmount).toFixed(2)}
                </div>
                {completedInvoice.balanceAmount > 0 && (
                  <div className="text-amber-600 font-extrabold">
                    Balance Added to Khata (Udhaar): ₹{Number(completedInvoice.balanceAmount).toFixed(2)}
                  </div>
                )}
              </div>

              {/* Footer Notice */}
              <div className="text-center pt-4 border-t border-dashed border-slate-200 text-[10px] text-slate-400 space-y-1">
                <p>Thank you for shopping with Sharma Supermart!</p>
                <p className="font-mono">Powered by LEDGE PRO &bull; Chitkara University Java S3 Project</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Quick Customer Modal */}
      {isCustomerModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 shadow-2xl max-w-sm w-full border border-slate-100">
            <h3 className="font-bold text-sm text-slate-900 mb-4">Register New Customer</h3>
            <form onSubmit={handleCreateQuickCustomer} className="space-y-3 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Customer Full Name *</label>
                <input
                  type="text"
                  required
                  value={newCustName}
                  onChange={(e) => setNewCustName(e.target.value)}
                  placeholder="e.g. Ramesh Kumar"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl"
                />
              </div>
              <div>
                <label className="block font-bold text-slate-700 mb-1">Mobile Number *</label>
                <input
                  type="text"
                  required
                  value={newCustPhone}
                  onChange={(e) => setNewCustPhone(e.target.value)}
                  placeholder="+91 98..."
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl"
                />
              </div>
              <div className="pt-3 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsCustomerModalOpen(false)}
                  className="px-3 py-1.5 border border-slate-200 rounded-xl font-semibold text-slate-600"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-1.5 bg-blue-600 text-white rounded-xl font-bold shadow-sm"
                >
                  Save & Select
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
