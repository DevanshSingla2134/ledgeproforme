import React from 'react';
import { Search, PlusCircle, Bell, AlertTriangle, LogOut } from 'lucide-react';
import { NavTab } from './Sidebar';
import { Business, User } from '../types';

interface HeaderProps {
  onQuickPOS: () => void;
  lowStockCount: number;
  setActiveTab: (tab: NavTab) => void;
  searchQuery: string;
  setSearchQuery: (q: string) => void;
  currentUser: User | null;
  currentBusiness: Business | null;
  onLogout: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  onQuickPOS,
  lowStockCount,
  setActiveTab,
  searchQuery,
  setSearchQuery,
  currentUser,
  currentBusiness,
  onLogout,
}) => {
  const userInitials = currentUser?.fullName
    ? currentUser.fullName.split(' ').map((n) => n[0]).join('').substring(0, 2).toUpperCase()
    : 'RS';

  return (
    <header className="h-16 bg-white border-b border-slate-200 px-6 flex items-center justify-between sticky top-0 z-20">
      {/* Search Input */}
      <div className="flex items-center gap-3 w-80 lg:w-96">
        <div className="relative w-full">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search products, invoices, customers..."
            className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 transition-all placeholder:text-slate-400 text-slate-700"
          />
        </div>
      </div>

      {/* Action Controls */}
      <div className="flex items-center gap-3">
        {/* Low Stock Warning Pill */}
        {lowStockCount > 0 && (
          <button
            onClick={() => setActiveTab('inventory')}
            className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-amber-50 text-amber-800 border border-amber-200/80 text-xs font-semibold hover:bg-amber-100 transition-colors animate-pulse"
          >
            <AlertTriangle className="w-3.5 h-3.5 text-amber-600" />
            <span>{lowStockCount} Low Stock</span>
          </button>
        )}

        {/* Quick Sale POS Button */}
        <button
          onClick={onQuickPOS}
          className="flex items-center gap-1.5 px-3.5 py-2 bg-blue-600 hover:bg-blue-700 active:bg-blue-800 text-white font-bold text-xs rounded-xl shadow-md shadow-blue-500/20 transition-all hover:shadow-lg"
        >
          <PlusCircle className="w-4 h-4" />
          <span>New Sale (POS)</span>
        </button>

        {/* Notifications Icon */}
        <div className="w-9 h-9 rounded-xl border border-slate-200 flex items-center justify-center text-slate-500 hover:text-slate-800 hover:border-slate-300 cursor-pointer transition-colors relative">
          <Bell className="w-4 h-4" />
          {lowStockCount > 0 && (
            <span className="w-2 h-2 rounded-full bg-red-500 absolute top-2 right-2 ring-2 ring-white" />
          )}
        </div>

        {/* User Profile */}
        <div className="flex items-center gap-3 pl-3 border-l border-slate-200">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-slate-900 to-slate-700 text-white font-black text-xs flex items-center justify-center shadow-sm">
            {userInitials}
          </div>
          <div className="hidden md:block text-left">
            <p className="text-xs font-bold text-slate-800 leading-tight">
              {currentUser?.fullName || 'Rajesh Sharma'}
            </p>
            <p className="text-[10px] text-slate-400 font-medium truncate max-w-[130px]">
              {currentBusiness?.name || 'Sharma Supermart'}
            </p>
          </div>

          {/* Logout Button */}
          <button
            onClick={onLogout}
            className="p-2 border border-slate-200 hover:border-red-300 hover:bg-red-50 text-slate-500 hover:text-red-600 rounded-xl transition-colors ml-1"
            title="Sign Out / Logout"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};
