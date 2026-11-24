import React, { useState, useEffect } from 'react';
import { 
  BookOpen, 
  Search, 
  Plus, 
  Download, 
  ArrowUpRight, 
  ArrowDownLeft, 
  CreditCard, 
  CheckCircle2, 
  Clock, 
  X,
  Phone,
  MapPin,
  Calendar,
  AlertCircle
} from 'lucide-react';
import { Customer, LedgerEntry } from '../types';
import { api } from '../services/api';

interface DigitalKhataPageProps {
  onNotify: (msg: string, type?: 'success' | 'error' | 'info') => void;
  onRefreshDashboard?: () => void;
}

export const DigitalKhataPage: React.FC<DigitalKhataPageProps> = ({ onNotify, onRefreshDashboard }) => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [selectedCustomerId, setSelectedCustomerId] = useState<number | null>(null);
  const [timeline, setTimeline] = useState<LedgerEntry[]>([]);
  const [search, setSearch] = useState('');
  const [debtorsOnly, setDebtorsOnly] = useState(false);
  const [loading, setLoading] = useState(true);
  const [timelineLoading, setTimelineLoading] = useState(false);

  // Modals
  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
  const [paymentAmount, setPaymentAmount] = useState<number>(0);
  const [paymentMethod, setPaymentMethod] = useState<string>('CASH');
  const [paymentRef, setPaymentRef] = useState<string>('');
  const [paymentNotes, setPaymentNotes] = useState<string>('Partial udhaar settlement');

  const [isAdjustmentModalOpen, setIsAdjustmentModalOpen] = useState(false);
  const [adjType, setAdjType] = useState<string>('CREDIT_ADJUSTMENT');
  const [adjAmount, setAdjAmount] = useState<number>(0);
  const [adjDesc, setAdjDesc] = useState<string>('Manual ledger adjustment');

  useEffect(() => {
    loadCustomers();
  }, [search, debtorsOnly]);

  const loadCustomers = async () => {
    try {
      setLoading(true);
      const data = await api.getCustomers(1, search, debtorsOnly);
      setCustomers(data);
      if (data.length > 0 && !selectedCustomerId) {
        setSelectedCustomerId(data[0].id || null);
      }
    } catch (err: any) {
      onNotify('Failed to load khata accounts: ' + err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedCustomerId) {
      loadCustomerTimeline(selectedCustomerId);
    }
  }, [selectedCustomerId]);

  const loadCustomerTimeline = async (cId: number) => {
    try {
      setTimelineLoading(true);
      const data = await api.getCustomerLedgerTimeline(cId);
      setTimeline(data);
    } catch (err: any) {
      onNotify('Failed to fetch ledger transactions: ' + err.message, 'error');
    } finally {
      setTimelineLoading(false);
    }
  };

  const selectedCustomer = customers.find((c) => c.id === selectedCustomerId);

  const handleRecordPayment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCustomerId || paymentAmount <= 0) {
      onNotify('Enter a valid payment amount', 'error');
      return;
    }
    try {
      await api.recordKhataPayment({
        businessId: 1,
        customerId: selectedCustomerId,
        amount: paymentAmount,
        paymentMethod,
        referenceNumber: paymentRef,
        notes: paymentNotes,
      });

      onNotify('Payment recorded and balance deducted successfully', 'success');
      setIsPaymentModalOpen(false);
      setPaymentAmount(0);
      setPaymentRef('');

      // Refresh data
      await loadCustomers();
      if (selectedCustomerId) await loadCustomerTimeline(selectedCustomerId);
      if (onRefreshDashboard) onRefreshDashboard();
    } catch (err: any) {
      onNotify(err.message || 'Payment submission failed', 'error');
    }
  };

  const handleRecordAdjustment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCustomerId || adjAmount <= 0) {
      onNotify('Enter a valid adjustment amount', 'error');
      return;
    }
    try {
      await api.recordAdjustment({
        businessId: 1,
        customerId: selectedCustomerId,
        entryType: adjType,
        amount: adjAmount,
        description: adjDesc,
      });

      onNotify('Adjustment entry recorded successfully', 'success');
      setIsAdjustmentModalOpen(false);
      setAdjAmount(0);

      // Refresh data
      await loadCustomers();
      if (selectedCustomerId) await loadCustomerTimeline(selectedCustomerId);
      if (onRefreshDashboard) onRefreshDashboard();
    } catch (err: any) {
      onNotify(err.message || 'Adjustment failed', 'error');
    }
  };

  const totalOutstandingAll = customers.reduce((acc, c) => acc + (c.currentBalance && c.currentBalance > 0 ? c.currentBalance : 0), 0);

  return (
    <div className="p-6 max-w-[1600px] mx-auto grid grid-cols-1 lg:grid-cols-12 gap-6 h-[calc(100vh-4.5rem)]">
      {/* Left 4 Columns: Customer Khata Accounts Directory */}
      <div className="lg:col-span-4 flex flex-col bg-white rounded-3xl border border-slate-200 shadow-sm p-5 overflow-hidden">
        {/* Header & Filter */}
        <div className="pb-4 border-b border-slate-100 space-y-3">
          <div className="flex items-center justify-between">
            <h1 className="text-lg font-black text-slate-900 tracking-tight flex items-center gap-2">
              <BookOpen className="w-5 h-5 text-emerald-600" />
              <span>Digital Khata Accounts</span>
            </h1>
            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800">
              {customers.length} Accounts
            </span>
          </div>

          <div className="p-3 bg-gradient-to-r from-emerald-50 to-teal-50 rounded-2xl border border-emerald-100/80">
            <div className="text-[10px] font-bold text-emerald-700 uppercase tracking-wider">Total Market Receivables (Udhaar)</div>
            <div className="text-xl font-black text-emerald-900 mt-0.5">
              ₹{totalOutstandingAll.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </div>
          </div>

          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by name or phone..."
              className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-600"
            />
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setDebtorsOnly(!debtorsOnly)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                debtorsOnly
                  ? 'bg-amber-600 text-white shadow-sm'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {debtorsOnly ? '✓ Showing Due Accounts Only' : 'Show Only Outstanding Debtors'}
            </button>
          </div>
        </div>

        {/* Customer List */}
        <div className="flex-1 overflow-y-auto divide-y divide-slate-100 pt-2 pr-1">
          {customers.map((c) => {
            const isSelected = c.id === selectedCustomerId;
            const hasDue = c.currentBalance && c.currentBalance > 0;
            return (
              <div
                key={c.id}
                onClick={() => setSelectedCustomerId(c.id || null)}
                className={`p-3.5 rounded-2xl cursor-pointer transition-all ${
                  isSelected
                    ? 'bg-emerald-50/70 border border-emerald-300 shadow-sm'
                    : 'hover:bg-slate-50'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="font-bold text-xs text-slate-900">{c.name}</h3>
                    <p className="text-[11px] text-slate-500 font-mono mt-0.5">{c.phone}</p>
                  </div>
                  <div className="text-right">
                    <div className={`font-black text-xs ${hasDue ? 'text-amber-600' : 'text-slate-400'}`}>
                      ₹{Number(c.currentBalance || 0).toFixed(2)}
                    </div>
                    <span
                      className={`text-[9px] font-bold px-1.5 py-0.5 rounded-full ${
                        hasDue ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-500'
                      }`}
                    >
                      {hasDue ? 'Payment Due' : 'All Settled'}
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Right 8 Columns: Selected Customer Ledger Timeline */}
      <div className="lg:col-span-8 flex flex-col bg-white rounded-3xl border border-slate-200 shadow-sm p-6 overflow-hidden">
        {selectedCustomer ? (
          <>
            {/* Customer Banner & Actions */}
            <div className="pb-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <div className="flex items-center gap-3">
                  <div className="w-11 h-11 rounded-2xl bg-emerald-600 text-white font-black text-sm flex items-center justify-center shadow-md shadow-emerald-500/20">
                    {selectedCustomer.name.substring(0, 2).toUpperCase()}
                  </div>
                  <div>
                    <h2 className="text-lg font-black text-slate-900 tracking-tight">{selectedCustomer.name}</h2>
                    <div className="flex items-center gap-3 text-xs text-slate-500 mt-0.5">
                      <span className="flex items-center gap-1 font-mono">
                        <Phone className="w-3.5 h-3.5 text-slate-400" />
                        {selectedCustomer.phone}
                      </span>
                      {selectedCustomer.address && (
                        <span className="flex items-center gap-1">
                          <MapPin className="w-3.5 h-3.5 text-slate-400" />
                          {selectedCustomer.address}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              {/* Outstanding Balance Badge & Action Buttons */}
              <div className="flex items-center gap-3">
                <div className="px-4 py-2 bg-slate-50 border border-slate-200 rounded-2xl text-right">
                  <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Current Outstanding</div>
                  <div className="text-xl font-black text-amber-600">
                    ₹{Number(selectedCustomer.currentBalance || 0).toFixed(2)}
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => {
                      setPaymentAmount(selectedCustomer.currentBalance || 0);
                      setIsPaymentModalOpen(true);
                    }}
                    className="flex items-center gap-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-xs shadow-md shadow-emerald-500/20 transition-all"
                  >
                    <ArrowDownLeft className="w-4 h-4" />
                    <span>Record Payment</span>
                  </button>

                  <button
                    onClick={() => setIsAdjustmentModalOpen(true)}
                    className="p-2 border border-slate-200 hover:bg-slate-50 text-slate-600 rounded-xl text-xs font-semibold"
                    title="Manual Debit/Credit Adjustment"
                  >
                    Adjust
                  </button>

                  <a
                    href={`/api/khata/customer/${selectedCustomer.id}/statement`}
                    download
                    className="p-2 border border-slate-200 hover:bg-slate-50 text-slate-600 rounded-xl text-xs font-semibold"
                    title="Download Statement CSV"
                  >
                    <Download className="w-4 h-4" />
                  </a>
                </div>
              </div>
            </div>

            {/* Ledger Audit Trail Timeline */}
            <div className="flex-1 overflow-y-auto py-4 space-y-3 pr-1">
              <div className="flex items-center justify-between text-xs font-bold text-slate-400 uppercase tracking-wider px-2">
                <span>Transaction History & Audit Trail</span>
                <span>Running Balance</span>
              </div>

              {timelineLoading ? (
                <div className="py-12 text-center text-xs text-slate-400">Loading ledger timeline...</div>
              ) : timeline.length === 0 ? (
                <div className="py-12 text-center text-xs text-slate-400">
                  No credit or payment history recorded for this customer yet.
                </div>
              ) : (
                timeline.map((entry) => {
                  const isPayment = entry.entryType === 'PAYMENT_RECEIVED' || entry.entryType === 'DEBIT_ADJUSTMENT';
                  return (
                    <div
                      key={entry.id}
                      className="p-4 rounded-2xl border border-slate-100 bg-slate-50/70 hover:bg-white hover:border-slate-200 transition-all flex items-center justify-between gap-4"
                    >
                      <div className="flex items-center gap-3">
                        <div
                          className={`w-9 h-9 rounded-xl flex items-center justify-center font-bold shrink-0 ${
                            isPayment
                              ? 'bg-emerald-100 text-emerald-700'
                              : 'bg-amber-100 text-amber-700'
                          }`}
                        >
                          {isPayment ? <ArrowDownLeft className="w-4 h-4" /> : <ArrowUpRight className="w-4 h-4" />}
                        </div>
                        <div>
                          <div className="flex items-center gap-2">
                            <span className="font-extrabold text-xs text-slate-900">
                              {entry.entryType === 'CREDIT_SALE'
                                ? 'Credit Sale (Udhaar)'
                                : entry.entryType === 'PAYMENT_RECEIVED'
                                ? 'Payment Settlement'
                                : entry.entryType}
                            </span>
                            <span className="text-[10px] text-slate-400 font-mono">
                              {new Date(entry.createdAt).toLocaleDateString()} {new Date(entry.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          </div>
                          <p className="text-xs text-slate-500 mt-0.5">{entry.description}</p>
                        </div>
                      </div>

                      <div className="text-right shrink-0">
                        <div
                          className={`font-black text-sm ${
                            isPayment ? 'text-emerald-600' : 'text-amber-600'
                          }`}
                        >
                          {isPayment ? '-' : '+'}₹{Number(entry.amount).toFixed(2)}
                        </div>
                        <div className="text-[11px] text-slate-400 font-mono mt-0.5">
                          Bal: ₹{Number(entry.runningBalance).toFixed(2)}
                        </div>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </>
        ) : (
          <div className="h-full flex items-center justify-center text-center text-xs text-slate-400">
            Select a customer from the left to view ledger details.
          </div>
        )}
      </div>

      {/* Record Payment Modal */}
      {isPaymentModalOpen && selectedCustomer && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 shadow-2xl max-w-sm w-full border border-slate-100">
            <h3 className="font-bold text-sm text-slate-900 mb-1">Record Khata Payment</h3>
            <p className="text-xs text-slate-500 mb-4">Customer: {selectedCustomer.name}</p>

            <form onSubmit={handleRecordPayment} className="space-y-3.5 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Amount Received (₹) *</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={paymentAmount}
                  onChange={(e) => setPaymentAmount(parseFloat(e.target.value) || 0)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl font-bold text-base text-slate-900 focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Payment Method</label>
                <select
                  value={paymentMethod}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl font-semibold text-slate-800"
                >
                  <option value="CASH">Cash</option>
                  <option value="UPI">UPI / QR Code</option>
                  <option value="BANK_TRANSFER">Bank Transfer / NEFT</option>
                  <option value="CHEQUE">Cheque</option>
                </select>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Reference Number / UTR</label>
                <input
                  type="text"
                  value={paymentRef}
                  onChange={(e) => setPaymentRef(e.target.value)}
                  placeholder="e.g. UPI/402198"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl font-mono"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Notes / Description</label>
                <input
                  type="text"
                  value={paymentNotes}
                  onChange={(e) => setPaymentNotes(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl"
                />
              </div>

              <div className="pt-3 flex justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsPaymentModalOpen(false)}
                  className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl shadow-md shadow-emerald-500/20"
                >
                  Save Payment
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Manual Adjustment Modal */}
      {isAdjustmentModalOpen && selectedCustomer && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 shadow-2xl max-w-sm w-full border border-slate-100">
            <h3 className="font-bold text-sm text-slate-900 mb-1">Manual Ledger Adjustment</h3>
            <p className="text-xs text-slate-500 mb-4">Customer: {selectedCustomer.name}</p>

            <form onSubmit={handleRecordAdjustment} className="space-y-3.5 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Adjustment Type</label>
                <select
                  value={adjType}
                  onChange={(e) => setAdjType(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl font-semibold text-slate-800"
                >
                  <option value="CREDIT_ADJUSTMENT">Increase Debt (Customer Owes More)</option>
                  <option value="DEBIT_ADJUSTMENT">Decrease Debt (Discount / Waiver)</option>
                </select>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Amount (₹) *</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={adjAmount}
                  onChange={(e) => setAdjAmount(parseFloat(e.target.value) || 0)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl font-bold text-slate-900"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Reason / Description *</label>
                <input
                  type="text"
                  required
                  value={adjDesc}
                  onChange={(e) => setAdjDesc(e.target.value)}
                  placeholder="e.g. Previous balance reconciliation"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl"
                />
              </div>

              <div className="pt-3 flex justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsAdjustmentModalOpen(false)}
                  className="px-4 py-2 border border-slate-200 rounded-xl font-semibold text-slate-600"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl shadow-md"
                >
                  Apply Adjustment
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
