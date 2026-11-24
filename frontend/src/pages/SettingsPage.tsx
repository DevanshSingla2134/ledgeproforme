import React, { useState, useEffect } from 'react';
import { 
  Settings, 
  Store, 
  Receipt, 
  Percent, 
  Bell, 
  Check, 
  Globe 
} from 'lucide-react';
import { Business } from '../types';
import { api } from '../services/api';

interface SettingsPageProps {
  onNotify: (msg: string, type?: 'success' | 'error' | 'info') => void;
}

export const SettingsPage: React.FC<SettingsPageProps> = ({ onNotify }) => {
  const [business, setBusiness] = useState<Business | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      setLoading(true);
      const data = await api.getBusinessProfile();
      setBusiness(data);
    } catch (err: any) {
      onNotify('Failed to load business profile: ' + err.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!business) return;
    try {
      setSaving(true);
      await api.updateBusinessProfile(business);
      onNotify('Store profile and tax parameters updated successfully', 'success');
    } catch (err: any) {
      onNotify(err.message, 'error');
    } finally {
      setSaving(false);
    }
  };

  if (loading || !business) {
    return <div className="p-8 text-center text-xs text-slate-400">Loading business profile...</div>;
  }

  return (
    <div className="p-8 space-y-6 max-w-4xl mx-auto">
      <div>
        <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Business Profile & Operating Settings</h1>
        <p className="text-xs text-slate-500 font-medium mt-1">
          Store tax registration, invoice numbering series, and inventory alert parameters.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6 text-xs">
        {/* Profile Card */}
        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center gap-2 pb-3 border-b border-slate-100">
            <Store className="w-4 h-4 text-blue-600" />
            <h2 className="font-bold text-sm text-slate-900">Commercial Store Profile</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block font-bold text-slate-700 mb-1">Business / Store Trade Name *</label>
              <input
                type="text"
                required
                value={business.name || ''}
                onChange={(e) => setBusiness({ ...business, name: e.target.value })}
                className="w-full px-3.5 py-2.5 border border-slate-200 rounded-xl font-bold text-slate-900"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Brand Tagline</label>
              <input
                type="text"
                value={business.tagline || ''}
                onChange={(e) => setBusiness({ ...business, tagline: e.target.value })}
                className="w-full px-3.5 py-2.5 border border-slate-200 rounded-xl"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block font-bold text-slate-700 mb-1">GSTIN / Tax ID</label>
              <input
                type="text"
                value={business.taxId || ''}
                onChange={(e) => setBusiness({ ...business, taxId: e.target.value })}
                className="w-full px-3.5 py-2.5 border border-slate-200 rounded-xl font-mono uppercase"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Official Mobile *</label>
              <input
                type="text"
                required
                value={business.phone || ''}
                onChange={(e) => setBusiness({ ...business, phone: e.target.value })}
                className="w-full px-3.5 py-2.5 border border-slate-200 rounded-xl font-mono"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Email Address</label>
              <input
                type="email"
                value={business.email || ''}
                onChange={(e) => setBusiness({ ...business, email: e.target.value })}
                className="w-full px-3.5 py-2.5 border border-slate-200 rounded-xl"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold text-slate-700 mb-1">Store Physical Address</label>
            <textarea
              rows={2}
              value={business.address || ''}
              onChange={(e) => setBusiness({ ...business, address: e.target.value })}
              className="w-full px-3.5 py-2 border border-slate-200 rounded-xl"
            />
          </div>
        </div>

        {/* Invoicing & Billing Parameters */}
        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center gap-2 pb-3 border-b border-slate-100">
            <Receipt className="w-4 h-4 text-emerald-600" />
            <h2 className="font-bold text-sm text-slate-900">Invoicing & Tax Engine</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block font-bold text-slate-700 mb-1">Invoice Prefix</label>
              <input
                type="text"
                value={business.invoicePrefix || ''}
                onChange={(e) => setBusiness({ ...business, invoicePrefix: e.target.value })}
                className="w-full px-3.5 py-2.5 border border-slate-200 rounded-xl font-mono"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Default GST Rate (%)</label>
              <input
                type="number"
                step="0.01"
                value={business.taxRate || 18}
                onChange={(e) => setBusiness({ ...business, taxRate: parseFloat(e.target.value) || 0 })}
                className="w-full px-3.5 py-2.5 border border-slate-200 rounded-xl font-bold"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Low Stock Alert Threshold</label>
              <input
                type="number"
                value={business.lowStockThreshold || 10}
                onChange={(e) => setBusiness({ ...business, lowStockThreshold: parseInt(e.target.value) || 10 })}
                className="w-full px-3.5 py-2.5 border border-slate-200 rounded-xl font-bold"
              />
            </div>
          </div>
        </div>

        {/* Submit */}
        <div className="flex justify-end">
          <button
            type="submit"
            disabled={saving}
            className="flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-2xl shadow-lg shadow-blue-500/25 transition-all text-xs"
          >
            <Check className="w-4 h-4" />
            <span>{saving ? 'Saving...' : 'Update Settings'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};
