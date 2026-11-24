import React, { useState, useEffect } from 'react';
import { Sidebar, NavTab } from './components/Sidebar';
import { Header } from './components/Header';
import { Toast, ToastMessage } from './components/Toast';
import { AuthPage } from './pages/AuthPage';
import { DashboardPage } from './pages/DashboardPage';
import { InventoryPage } from './pages/InventoryPage';
import { POSBillingPage } from './pages/POSBillingPage';
import { DigitalKhataPage } from './pages/DigitalKhataPage';
import { CustomersPage } from './pages/CustomersPage';
import { SuppliersPage } from './pages/SuppliersPage';
import { ReportsPage } from './pages/ReportsPage';
import { SettingsPage } from './pages/SettingsPage';
import { AuthResponse, Business, DashboardStats, User } from './types';
import { api } from './services/api';

export const App: React.FC = () => {
  // Authentication State Gate:
  // If currentUser is null, ONLY AuthPage (Login/Register) is displayed.
  const [currentUser, setCurrentUser] = useState<User | null>(() => {
    try {
      const saved = localStorage.getItem('ledgepro_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  const [currentBusiness, setCurrentBusiness] = useState<Business | null>(() => {
    try {
      const saved = localStorage.getItem('ledgepro_business');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  const [activeTab, setActiveTab] = useState<NavTab>('dashboard');
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loadingStats, setLoadingStats] = useState(false);
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

  const handleLoginSuccess = (authData: AuthResponse) => {
    setCurrentUser(authData.user);
    setCurrentBusiness(authData.business);
    try {
      localStorage.setItem('ledgepro_user', JSON.stringify(authData.user));
      localStorage.setItem('ledgepro_business', JSON.stringify(authData.business));
      localStorage.setItem('ledgepro_token', authData.token);
    } catch (e) {
      console.warn('LocalStorage save error:', e);
    }
    setActiveTab('dashboard');
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setCurrentBusiness(null);
    try {
      localStorage.removeItem('ledgepro_user');
      localStorage.removeItem('ledgepro_business');
      localStorage.removeItem('ledgepro_token');
    } catch (e) {
      // ignore
    }
    showToast('You have been safely signed out.', 'info');
  };

  const loadDashboardStats = async () => {
    if (!currentUser) return;
    try {
      setLoadingStats(true);
      const bId = currentBusiness?.id || 1;
      const data = await api.getDashboardStats(bId);
      setStats(data);
    } catch (err: any) {
      console.error('Failed to load dashboard statistics:', err);
    } finally {
      setLoadingStats(false);
    }
  };

  useEffect(() => {
    if (currentUser) {
      loadDashboardStats();
      const interval = setInterval(loadDashboardStats, 30000);
      return () => clearInterval(interval);
    }
  }, [currentUser]);

  // =========================================================================
  // AUTHENTICATION GATE:
  // If not logged in, render ONLY the Login and Registration first page!
  // =========================================================================
  if (!currentUser) {
    return (
      <>
        <AuthPage onLoginSuccess={handleLoginSuccess} onNotify={showToast} />
        <Toast toasts={toasts} onDismiss={handleDismissToast} />
      </>
    );
  }

  // =========================================================================
  // MAIN AUTHENTICATED SaaS APPLICATION
  // =========================================================================
  return (
    <div className="flex h-screen bg-[#F8FAFC] text-slate-800 antialiased overflow-hidden select-none">
      {/* Sidebar Navigation */}
      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        lowStockCount={stats?.lowStockCount || 0}
        debtorCount={stats?.outstandingKhataBalance && stats.outstandingKhataBalance > 0 ? 1 : 0}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-screen overflow-hidden">
        {/* Top Header with Profile & Logout Button */}
        <Header
          onQuickPOS={() => setActiveTab('billing')}
          lowStockCount={stats?.lowStockCount || 0}
          setActiveTab={setActiveTab}
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          currentUser={currentUser}
          currentBusiness={currentBusiness}
          onLogout={handleLogout}
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
