import React from 'react';
import { 
  LayoutDashboard, 
  Package, 
  Receipt, 
  BookOpen, 
  Users, 
  Truck, 
  BarChart3, 
  Settings,
  ChevronRight,
  Sparkles
} from 'lucide-react';

import { Business } from '../types';

export type NavTab = 'dashboard' | 'inventory' | 'billing' | 'khata' | 'customers' | 'suppliers' | 'reports' | 'settings';

interface SidebarProps {
  activeTab: NavTab;
  setActiveTab: (tab: NavTab) => void;
  lowStockCount?: number;
  debtorCount?: number;
  currentBusiness?: Business | null;
}

export const Sidebar: React.FC<SidebarProps> = ({ 
  activeTab, 
  setActiveTab, 
  lowStockCount = 0,
  debtorCount = 0,
  currentBusiness
}) => {
  const navItems = [
    { id: 'dashboard' as NavTab, label: 'Dashboard', icon: LayoutDashboard },
    { id: 'inventory' as NavTab, label: 'Inventory', icon: Package, badge: lowStockCount > 0 ? `${lowStockCount} Low` : undefined, badgeColor: 'bg-amber-500/20 text-amber-400' },
    { id: 'billing' as NavTab, label: 'POS Billing', icon: Receipt, highlight: true },
    { id: 'khata' as NavTab, label: 'Digital Khata', icon: BookOpen, badge: debtorCount > 0 ? `${debtorCount} Due` : undefined, badgeColor: 'bg-emerald-500/20 text-emerald-400' },
    { id: 'customers' as NavTab, label: 'Customers', icon: Users },
    { id: 'suppliers' as NavTab, label: 'Suppliers', icon: Truck },
    { id: 'reports' as NavTab, label: 'Analytics & Reports', icon: BarChart3 },
    { id: 'settings' as NavTab, label: 'Settings', icon: Settings },
  ];

  return (
    <aside className="w-64 bg-[#0F172A] text-slate-300 flex flex-col border-r border-slate-800 shrink-0 select-none">
      {/* Brand Header */}
      <div className="p-5 border-b border-slate-800/80 flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-500 flex items-center justify-center text-white font-black text-xl shadow-lg shadow-blue-500/20">
          LP
        </div>
        <div>
          <div className="flex items-center gap-1.5">
            <span className="font-extrabold text-lg tracking-tight text-white">LEDGE</span>
            <span className="text-xs font-bold uppercase tracking-wider px-1.5 py-0.5 rounded bg-blue-600/30 text-blue-400 border border-blue-500/30">PRO</span>
          </div>
          <p className="text-[11px] text-slate-400 font-medium">Smart Retail & Khata OS</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        <div className="px-3 pb-2 text-[10px] font-bold uppercase tracking-wider text-slate-500">
          Main Modules
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all duration-150 group ${
                isActive
                  ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/25 font-semibold'
                  : item.highlight
                  ? 'hover:bg-slate-800/80 text-blue-400 font-semibold'
                  : 'hover:bg-slate-800/60 text-slate-400 hover:text-slate-100'
              }`}
            >
              <div className="flex items-center gap-3">
                <Icon className={`w-4 h-4 transition-transform group-hover:scale-110 ${isActive ? 'text-white' : item.highlight ? 'text-blue-400' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </div>
              <div className="flex items-center gap-1.5">
                {item.badge && (
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${item.badgeColor || 'bg-slate-800 text-slate-300'}`}>
                    {item.badge}
                  </span>
                )}
                {isActive && <ChevronRight className="w-3.5 h-3.5 text-blue-200" />}
              </div>
            </button>
          );
        })}
      </nav>

      {/* Security Status Badge */}
      <div className="p-3 mx-3 mb-4 rounded-xl bg-slate-900/90 border border-slate-800/80 text-xs">
        <div className="flex items-center gap-1.5 text-blue-400 font-bold mb-1">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Independent Store Ledger</span>
        </div>
        <p className="text-[11px] text-slate-400 leading-relaxed">
          Private Storefront &bull; Zero Leakage<br />
          Encrypted POS &amp; Khata Records
        </p>
      </div>

      {/* Business Info Footer */}
      <div className="p-3.5 border-t border-slate-800/80 bg-slate-950/60 flex items-center justify-between text-xs">
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
          <span className="text-slate-300 font-medium truncate max-w-[140px]">
            {currentBusiness?.name || 'My Store'}
          </span>
        </div>
        <span className="text-[10px] text-slate-500 font-mono">v1.0-PRO</span>
      </div>
    </aside>
  );
};
