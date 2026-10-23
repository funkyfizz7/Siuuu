import React from 'react';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck, LogOut, Sparkles, BookOpen, User } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, signOut, isDemoMode, isFirebaseConfigured } = useAuth();

  return (
    <header className="border-b border-slate-800 bg-slate-900/90 backdrop-blur-md sticky top-0 z-50 px-6 py-3.5 flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-amber-500 to-amber-300 flex items-center justify-center shadow-lg shadow-amber-500/20">
          <BookOpen className="w-5 h-5 text-slate-950 font-bold" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <span className="font-bold text-lg tracking-tight text-white">PitchIQ</span>
            <span className="text-xs px-2 py-0.5 rounded-full font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/30">
              Narrative Editorial
            </span>
          </div>
          <p className="text-[11px] text-slate-400 -mt-0.5">Human Review & Correction Panel</p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        {!isFirebaseConfigured && (
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-amber-500/10 border border-amber-500/20 text-amber-300 text-xs">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Local Test Mode</span>
          </div>
        )}

        {user && user.role !== 'unauthorized' ? (
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2.5 bg-slate-800/80 border border-slate-700/60 px-3 py-1.5 rounded-lg">
              <div className="w-7 h-7 rounded-full bg-slate-700 flex items-center justify-center text-slate-300 text-xs font-semibold">
                {user.displayName ? user.displayName.charAt(0).toUpperCase() : <User className="w-3.5 h-3.5" />}
              </div>
              <div className="text-left">
                <div className="text-xs font-medium text-slate-200 leading-none">{user.displayName || user.email}</div>
                <div className="text-[10px] text-emerald-400 flex items-center gap-1 mt-0.5">
                  <ShieldCheck className="w-3 h-3" />
                  <span className="capitalize">{user.role}</span>
                </div>
              </div>
            </div>
            <button
              onClick={signOut}
              title="Sign Out"
              className="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition border border-slate-700/60"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        ) : null}
      </div>
    </header>
  );
};
