import React from 'react';
import { useAuth } from '../context/AuthContext';
import { ShieldAlert, LogOut, Copy, Check } from 'lucide-react';

export const UnauthorizedScreen: React.FC = () => {
  const { user, signOut } = useAuth();
  const [copied, setCopied] = React.useState(false);

  const copyUid = () => {
    if (user?.uid) {
      navigator.clipboard.writeText(user.uid);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="min-h-[calc(100vh-65px)] flex items-center justify-center p-6 bg-slate-950">
      <div className="max-w-md w-full bg-slate-900 border border-red-500/30 rounded-2xl p-8 text-center shadow-2xl shadow-red-500/5">
        <div className="w-14 h-14 mx-auto rounded-2xl bg-red-500/10 border border-red-500/20 flex items-center justify-center text-red-400 mb-5">
          <ShieldAlert className="w-7 h-7" />
        </div>

        <h2 className="text-xl font-bold text-white mb-2">Access Denied</h2>
        <p className="text-sm text-slate-400 mb-6 leading-relaxed">
          The signed-in account is not on the PitchIQ Authorized Editors allowlist.
          To protect editorial integrity, public accounts cannot edit or publish narratives.
        </p>

        <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 text-left mb-6 space-y-2">
          <div className="text-xs text-slate-500 font-medium">SIGNED-IN ACCOUNT</div>
          <div className="text-sm text-slate-300 font-mono break-all">{user?.email || 'Unknown Email'}</div>
          
          <div className="text-xs text-slate-500 font-medium pt-2">FIREBASE UID FOR AUTHORIZATION</div>
          <div className="flex items-center justify-between gap-2 bg-slate-900 border border-slate-800 rounded-lg p-2">
            <span className="text-xs font-mono text-amber-300 break-all select-all">{user?.uid}</span>
            <button
              onClick={copyUid}
              className="p-1.5 hover:bg-slate-800 rounded text-slate-400 hover:text-white transition flex-shrink-0"
              title="Copy UID"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
            </button>
          </div>
        </div>

        <div className="text-xs text-slate-500 mb-6 text-left leading-relaxed">
          <strong className="text-slate-400 font-semibold">How to gain access:</strong>
          <br />
          Ask the PitchIQ Project Owner to add your UID to the Firestore{' '}
          <code className="text-amber-400 bg-slate-800 px-1 py-0.5 rounded">authorized_editors</code> collection with{' '}
          <code className="text-emerald-400 bg-slate-800 px-1 py-0.5 rounded">isActive: true</code>.
        </div>

        <button
          onClick={signOut}
          className="w-full py-2.5 px-4 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl font-medium text-sm transition flex items-center justify-center gap-2 border border-slate-700"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out / Switch Account</span>
        </button>
      </div>
    </div>
  );
};
