import React, { useState, useEffect } from 'react';
import { 
  BarChart3, 
  TrendingUp, 
  DollarSign, 
  Sparkles, 
  PieChart, 
  Download, 
  Calendar,
  AlertCircle
} from 'lucide-react';
import { api } from '../services/api';

interface ReportsPageProps {
  onNotify: (msg: string, type?: 'success' | 'error' | 'info') => void;
}

export const ReportsPage: React.FC<ReportsPageProps> = ({ onNotify }) => {
  const [loading, setLoading] = useState(true);
  const [financialSummary, setFinancialSummary] = useState<any>(null);
  const [topProducts, setTopProducts] = useState<{ productName: string; totalRevenue: number }[]>([]);
  const [paymentMethods, setPaymentMethods] = useState<Record<string, number>>({});
  const [inventoryHealth, setInventoryHealth] = useState<any>(null);
  const [expensesByCategory, setExpensesByCategory] = useState<Record<string, number>>({});

  useEffect(() => {
    loadReports();
  }, []);

  const loadReports = async () => {
    try {
      setLoading(true);
      const [fin, top, pm, inv, exp] = await Promise.all([
        api.getFinancialSummary(),
        api.getTopSellingProducts(undefined, 6),
        api.getSalesByPaymentMethod(),
        api.getInventoryHealth(),
        api.getExpensesByCategory(),
      ]);
      setFinancialSummary(fin);
      setTopProducts(top);
      setPaymentMethods(pm);
      setInventoryHealth(inv);
      setExpensesByCategory(exp);
    } catch (err: any) {
      onNotify('Failed to generate report streams: ' + err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="p-8 text-center py-20 text-xs text-slate-400">
        Aggregating financial ledger data using Java 21 Stream API & Collectors...
      </div>
    );
  }

  const revenue = financialSummary?.monthlyRevenue || 0;
  const grossProfit = financialSummary?.estimatedGrossProfit || 0;
  const expenses = financialSummary?.totalExpenses || 0;
  const netProfit = financialSummary?.netProfit || (grossProfit - expenses);

  return (
    <div className="p-8 space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Financial Intelligence & Analytics</h1>
          <p className="text-xs text-slate-500 font-medium mt-1">
            Processed with Java 21 Stream API: Distinct grouping, summarizing, and profit margin analysis.
          </p>
        </div>

        <a
          href="/api/export/products/csv"
          download
          className="flex items-center gap-2 px-4 py-2 bg-white border border-slate-300 rounded-xl text-xs font-semibold text-slate-700 shadow-sm hover:bg-slate-50"
        >
          <Download className="w-4 h-4 text-slate-500" />
          <span>Download Audit Report (CSV)</span>
        </a>
      </div>

      {/* P&L Financial Cards (Clearly Distinguishing Revenue vs Profit vs Expenses) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 rounded-2xl bg-white border border-slate-200 shadow-sm">
          <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Monthly Gross Revenue</span>
          <div className="text-2xl font-black text-slate-900 mt-1">
            ₹{Number(revenue).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <span className="text-[11px] text-slate-400 mt-1 block">Total billing turnover</span>
        </div>

        <div className="p-5 rounded-2xl bg-white border border-indigo-100 shadow-sm bg-gradient-to-br from-indigo-50/50 to-white">
          <span className="text-[10px] font-bold uppercase tracking-wider text-indigo-700">Estimated Gross Profit</span>
          <div className="text-2xl font-black text-indigo-900 mt-1">
            ₹{Number(grossProfit).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <span className="text-[11px] text-indigo-500 mt-1 block">Selling Price &minus; Cost of Goods</span>
        </div>

        <div className="p-5 rounded-2xl bg-white border border-red-100 shadow-sm bg-gradient-to-br from-red-50/50 to-white">
          <span className="text-[10px] font-bold uppercase tracking-wider text-red-700">Store Operating Expenses</span>
          <div className="text-2xl font-black text-red-900 mt-1">
            ₹{Number(expenses).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <span className="text-[11px] text-red-500 mt-1 block">Rent, electricity, packaging</span>
        </div>

        <div className="p-5 rounded-2xl bg-white border border-emerald-100 shadow-sm bg-gradient-to-br from-emerald-50/50 to-white">
          <span className="text-[10px] font-bold uppercase tracking-wider text-emerald-700">Estimated Net Profit</span>
          <div className="text-2xl font-black text-emerald-900 mt-1">
            ₹{Number(netProfit).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <span className="text-[11px] text-emerald-600 font-bold mt-1 block">
            Margin: {financialSummary?.profitMarginPercent || 0}%
          </span>
        </div>
      </div>

      {/* Two Column Visual Breakdowns */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Top-Selling Products by Revenue */}
        <div className="bg-white rounded-3xl border border-slate-200 shadow-sm p-6 space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <h2 className="font-bold text-sm text-slate-900 flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-blue-600" />
              <span>Top-Selling Products by Turnover</span>
            </h2>
            <span className="text-[10px] font-mono text-slate-400">Stream.sorted()</span>
          </div>

          <div className="space-y-3">
            {topProducts.length === 0 ? (
              <div className="py-8 text-center text-xs text-slate-400">No sale transactions recorded.</div>
            ) : (
              topProducts.map((p, idx) => {
                const maxRev = topProducts[0]?.totalRevenue || 1;
                const percentage = Math.min(100, Math.round((p.totalRevenue / maxRev) * 100));
                return (
                  <div key={idx} className="space-y-1">
                    <div className="flex justify-between text-xs">
                      <span className="font-bold text-slate-800">{p.productName}</span>
                      <span className="font-extrabold text-slate-900">₹{Number(p.totalRevenue).toFixed(2)}</span>
                    </div>
                    <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                      <div
                        className="bg-blue-600 h-2 rounded-full transition-all duration-500"
                        style={{ width: `${percentage}%` }}
                      />
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Sales by Payment Method Breakdown */}
        <div className="bg-white rounded-3xl border border-slate-200 shadow-sm p-6 space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <h2 className="font-bold text-sm text-slate-900 flex items-center gap-2">
              <PieChart className="w-4 h-4 text-emerald-600" />
              <span>Payment Channel Breakdown</span>
            </h2>
            <span className="text-[10px] font-mono text-slate-400">Collectors.groupingBy()</span>
          </div>

          <div className="grid grid-cols-2 gap-3">
            {Object.entries(paymentMethods).map(([method, amt]) => (
              <div key={method} className="p-4 rounded-2xl bg-slate-50 border border-slate-200/80">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">{method}</span>
                <div className="text-lg font-black text-slate-900 mt-1">₹{Number(amt).toFixed(2)}</div>
              </div>
            ))}
          </div>

          {/* Operating Expense Breakdown */}
          <div className="pt-4 border-t border-slate-100">
            <h3 className="text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">Operating Expenses by Head</h3>
            <div className="grid grid-cols-3 gap-2">
              {Object.entries(expensesByCategory).map(([cat, amt]) => (
                <div key={cat} className="p-2.5 rounded-xl bg-red-50/50 border border-red-100 text-xs">
                  <span className="text-slate-500 text-[10px] font-bold uppercase">{cat}</span>
                  <div className="font-black text-red-900 mt-0.5">₹{Number(amt).toFixed(2)}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
