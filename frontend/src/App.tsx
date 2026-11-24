import React, { useState, useEffect } from 'react';
import { Sidebar, NavTab } from './components/Sidebar';
import { Header } from './components/Header';
import { Toast, ToastMessage } from './components/Toast';
import { DashboardPage } from './pages/DashboardPage';
import { InventoryPage } from './pages/InventoryPage';
import { POSBillingPage } from './pages/POSBillingPage';
import { DigitalKhataPage } from './pages/DigitalKhataPage';
import { CustomersPage } from './pages/CustomersPage';
import { SuppliersPage } from './pages/SuppliersPage';
import { ReportsPage } from './pages/ReportsPage';
import { SettingsPage } from './pages/SettingsPage';
import { DashboardStats } from './types';
import { api } from './services/api';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<NavTab>('dashboard');
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loadingStats, setLoadingStats] = useState(true);
  const [toasts, setToasts] = useState<ToastMessage[]>([]);
  const [searchQuery, setSearchQuery] = useState('');

  const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
    const id = Math.random().toString(36).substring(2, 9);
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4000);
  };

  const handleDismissToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  const loadDashboardStats = async () => {
    try {
      setLoadingStats(true);
      const data = await api.getDashboardStats(1);
      setStats(data);
    } catch (err: any) {
      console.error('Failed to load dashboard statistics:', err);
    } finally {
      setLoadingStats(false);
    }
  };

  useEffect(() => {
    loadDashboardStats();
    // Poll dashboard stats periodically
    const interval = setInterval(loadDashboardStats, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="flex h-screen bg-[#F8FAFC] text-slate-800 antialiased overflow-hidden select-none">
      {/* Professional Sidebar Navigation */}
      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        lowStockCount={stats?.lowStockCount || 0}
        debtorCount={stats?.outstandingKhataBalance && stats.outstandingKhataBalance > 0 ? 1 : 0}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-screen overflow-hidden">
        {/* Top Header */}
        <Header
          onQuickPOS={() => setActiveTab('billing')}
          lowStockCount={stats?.lowStockCount || 0}
          setActiveTab={setActiveTab}
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
        />

        {/* Viewport Router */}
        <main className="flex-1 overflow-y-auto">
          {activeTab === 'dashboard' && (
            <DashboardPage
              stats={stats}
              loading={loadingStats}
              onNavigate={(tab) => setActiveTab(tab)}
            />
          )}

          {activeTab === 'inventory' && (
            <InventoryPage onNotify={showToast} />
          )}

          {activeTab === 'billing' && (
            <POSBillingPage
              onNotify={showToast}
              onRefreshDashboard={loadDashboardStats}
            />
          )}

          {activeTab === 'khata' && (
            <DigitalKhataPage
              onNotify={showToast}
              onRefreshDashboard={loadDashboardStats}
            />
          )}

          {activeTab === 'customers' && (
            <CustomersPage onNotify={showToast} />
          )}

          {activeTab === 'suppliers' && (
            <SuppliersPage onNotify={showToast} />
          )}

          {activeTab === 'reports' && (
            <ReportsPage onNotify={showToast} />
          )}

          {activeTab === 'settings' && (
            <SettingsPage onNotify={showToast} />
          )}
        </main>
      </div>

      {/* Global Toast Notifications */}
      <Toast toasts={toasts} onDismiss={handleDismissToast} />
    </div>
  );
};

export default App;
