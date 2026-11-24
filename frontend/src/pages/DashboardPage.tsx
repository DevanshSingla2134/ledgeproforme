import React, { useState } from 'react';
import { 
  TrendingUp, 
  ShoppingBag, 
  Package, 
  AlertCircle, 
  Users, 
  CreditCard, 
  CheckCircle, 
  DollarSign,
  ArrowUpRight,
  Receipt,
  Clock,
  Sparkles
} from 'lucide-react';
import { DashboardStats } from '../types';
import { NavTab } from '../components/Sidebar';

interface DashboardPageProps {
  stats: DashboardStats | null;
  loading: boolean;
  onNavigate: (tab: NavTab) => void;
}

export const DashboardPage: React.FC<DashboardPageProps> = ({ stats, loading, onNavigate }) => {
  const [dateFilter, setDateFilter] = useState<'today' | '7days' | 'month'>('today');

  if (loading || !stats) {
    return (
      <div className="p-8 space-y-6">
        <div className="h-8 bg-slate-200 rounded-lg w-64 animate-pulse" />
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
            <div key={i} className="h-28 bg-slate-100 border border-slate-200 rounded-2xl animate-pulse" />
          ))}
        </div>
      </div>
    );
  }

  const kpis = [
    {
      title: "Today's Sales",
      value: `₹${Number(stats.totalSalesToday || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`,
      subtitle: 'Real-time POS checkout',
      icon: TrendingUp,
      color: 'text-blue-600',
      bg: 'bg-blue-50',
      border: 'border-blue-100',
    },
    {
      title: 'Monthly Revenue',
      value: `₹${Number(stats.totalRevenueThisMonth || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`,
      subtitle: 'Current billing cycle',
      icon: DollarSign,
      color: 'text-emerald-600',
      bg: 'bg-emerald-50',
      border: 'border-emerald-100',
    },
    {
      title: 'Estimated Gross Profit',
      value: `₹${Number(stats.estimatedGrossProfit || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`,
      subtitle: 'Revenue minus cost of goods',
      icon: Sparkles,
      color: 'text-indigo-600',
      bg: 'bg-indigo-50',
      border: 'border-indigo-100',
    },
    {
      title: 'Outstanding Khata (Udhaar)',
      value: `₹${Number(stats.outstandingKhataBalance || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`,
      subtitle: 'Customer receivables due',
      icon: CreditCard,
      color: 'text-amber-600',
      bg: 'bg-amber-50',
      border: 'border-amber-100',
    },
    {
      title: 'Total Orders',
      value: stats.totalOrders || 0,
      subtitle: 'Invoices generated',
      icon: ShoppingBag,
      color: 'text-slate-700',
      bg: 'bg-slate-50',
      border: 'border-slate-200',
    },
    {
      title: 'Catalog Products',
      value: stats.totalProducts || 0,
      subtitle: 'Active inventory SKUs',
      icon: Package,
      color: 'text-slate-700',
      bg: 'bg-slate-50',
      border: 'border-slate-200',
    },
    {
      title: 'Low Stock Alerts',
      value: stats.lowStockCount || 0,
      subtitle: 'Items below threshold',
      icon: AlertCircle,
      color: stats.lowStockCount > 0 ? 'text-red-600' : 'text-slate-500',
      bg: stats.lowStockCount > 0 ? 'bg-red-50' : 'bg-slate-50',
      border: stats.lowStockCount > 0 ? 'border-red-100' : 'border-slate-200',
      onClick: () => onNavigate('inventory'),
    },
    {
      title: 'Khata Payments Collected',
      value: `₹${Number(stats.paymentsReceived || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`,
      subtitle: 'Debt recovered from customers',
      icon: CheckCircle,
      color: 'text-teal-600',
      bg: 'bg-teal-50',
      border: 'border-teal-100',
    },
  ];

  return (
    <div className="p-8 space-y-8 max-w-7xl mx-auto">
      {/* Top Banner / Welcome */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Executive Dashboard</h1>
          <p className="text-xs text-slate-500 font-medium mt-1">
            Real-time business performance, inventory velocity, and digital khata receivables.
          </p>
        </div>

        {/* Date Filter Tabs */}
        <div className="flex items-center gap-1.5 p-1 bg-slate-200/70 rounded-xl text-xs font-semibold text-slate-600 w-fit">
          <button
            onClick={() => setDateFilter('today')}
            className={`px-3 py-1.5 rounded-lg transition-all ${
              dateFilter === 'today' ? 'bg-white text-slate-900 shadow-sm font-bold' : 'hover:text-slate-900'
            }`}
          >
            Today
          </button>
          <button
            onClick={() => setDateFilter('7days')}
            className={`px-3 py-1.5 rounded-lg transition-all ${
              dateFilter === '7days' ? 'bg-white text-slate-900 shadow-sm font-bold' : 'hover:text-slate-900'
            }`}
          >
            Last 7 Days
          </button>
          <button
            onClick={() => setDateFilter('month')}
            className={`px-3 py-1.5 rounded-lg transition-all ${
              dateFilter === 'month' ? 'bg-white text-slate-900 shadow-sm font-bold' : 'hover:text-slate-900'
            }`}
          >
            This Month
          </button>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {kpis.map((kpi, idx) => {
          const Icon = kpi.icon;
          return (
            <div
              key={idx}
              onClick={kpi.onClick}
              className={`p-5 rounded-2xl bg-white border ${kpi.border} shadow-sm hover:shadow-md transition-all ${
                kpi.onClick ? 'cursor-pointer hover:border-red-300' : ''
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{kpi.title}</span>
                <div className={`w-8 h-8 rounded-xl ${kpi.bg} flex items-center justify-center ${kpi.color}`}>
                  <Icon className="w-4 h-4" />
                </div>
              </div>
              <div className="mt-3">
                <div className="text-2xl font-black text-slate-900 tracking-tight">{kpi.value}</div>
                <div className="text-[11px] text-slate-400 font-medium mt-1">{kpi.subtitle}</div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Two Column Section: Recent Invoices & Recent Khata Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Invoices Card */}
        <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden flex flex-col">
          <div className="p-5 border-b border-slate-100 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <Receipt className="w-4 h-4 text-blue-600" />
              <h2 className="font-bold text-sm text-slate-900">Recent POS Invoices</h2>
            </div>
            <button
              onClick={() => onNavigate('billing')}
              className="text-xs font-semibold text-blue-600 hover:text-blue-700 flex items-center gap-1"
            >
              <span>Create Bill</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="divide-y divide-slate-100 flex-1">
            {stats.recentInvoices && stats.recentInvoices.length > 0 ? (
              stats.recentInvoices.map((inv) => (
                <div key={inv.id} className="p-4 flex items-center justify-between hover:bg-slate-50/80 transition-colors">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-xs font-bold text-slate-900">{inv.invoiceNumber}</span>
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                          inv.paymentMethod === 'KHATA'
                            ? 'bg-amber-100 text-amber-800'
                            : 'bg-emerald-100 text-emerald-800'
                        }`}
                      >
                        {inv.paymentMethod}
                      </span>
                    </div>
                    <p className="text-xs text-slate-500 mt-1">
                      {inv.customerName || 'Walk-in Counter Customer'}
                    </p>
                  </div>
                  <div className="text-right">
                    <div className="font-bold text-sm text-slate-900">
                      ₹{Number(inv.grandTotal).toFixed(2)}
                    </div>
                    <div className="text-[11px] text-slate-400">
                      {inv.createdAt ? new Date(inv.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Today'}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="p-8 text-center text-xs text-slate-400">No invoices recorded yet.</div>
            )}
          </div>
        </div>

        {/* Recent Digital Khata Ledger Timeline */}
        <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden flex flex-col">
          <div className="p-5 border-b border-slate-100 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <Clock className="w-4 h-4 text-emerald-600" />
              <h2 className="font-bold text-sm text-slate-900">Digital Khata Activity</h2>
            </div>
            <button
              onClick={() => onNavigate('khata')}
              className="text-xs font-semibold text-emerald-600 hover:text-emerald-700 flex items-center gap-1"
            >
              <span>View Full Ledger</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="divide-y divide-slate-100 flex-1">
            {stats.recentLedgerActivity && stats.recentLedgerActivity.length > 0 ? (
              stats.recentLedgerActivity.map((act) => (
                <div key={act.id} className="p-4 flex items-center justify-between hover:bg-slate-50/80 transition-colors">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-xs text-slate-800">{act.customerName}</span>
                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                          act.entryType === 'PAYMENT_RECEIVED'
                            ? 'bg-emerald-100 text-emerald-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {act.entryType === 'PAYMENT_RECEIVED' ? 'Payment In' : 'Credit Given'}
                      </span>
                    </div>
                    <p className="text-xs text-slate-500 mt-1 line-clamp-1">{act.description}</p>
                  </div>
                  <div className="text-right">
                    <div
                      className={`font-bold text-sm ${
                        act.entryType === 'PAYMENT_RECEIVED' ? 'text-emerald-600' : 'text-amber-600'
                      }`}
                    >
                      {act.entryType === 'PAYMENT_RECEIVED' ? '-' : '+'}₹{Number(act.amount).toFixed(2)}
                    </div>
                    <div className="text-[11px] text-slate-400">
                      Bal: ₹{Number(act.runningBalance).toFixed(2)}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="p-8 text-center text-xs text-slate-400">No khata transactions recorded yet.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
