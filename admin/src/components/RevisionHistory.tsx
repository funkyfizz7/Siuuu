import React from 'react';
import { RevisionRecord } from '../types/narrative';
import { History, User, Clock, RotateCcw, CheckCircle, FileEdit, Undo2 } from 'lucide-react';

interface RevisionHistoryProps {
  revisions: RevisionRecord[];
  onRestore: (revision: RevisionRecord) => void;
}

export const RevisionHistory: React.FC<RevisionHistoryProps> = ({ revisions, onRestore }) => {
  if (!revisions || revisions.length === 0) {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 text-center text-slate-500 text-sm">
        <History className="w-6 h-6 mx-auto mb-2 text-slate-600" />
        <p>No revision history yet for this item.</p>
        <p className="text-xs text-slate-600 mt-1">Edits and publications will appear here.</p>
      </div>
    );
  }

  const sortedRevisions = [...revisions].reverse();

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-lg">
      <div className="bg-slate-800/60 px-4 py-2.5 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <History className="w-4 h-4 text-amber-400" />
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-300">
            Revision Audit Trail ({revisions.length})
          </span>
        </div>
      </div>

      <div className="divide-y divide-slate-800/60 max-h-72 overflow-y-auto">
        {sortedRevisions.map((rev, idx) => {
          const isPublished = rev.status === 'published';
          const isReverted = rev.status === 'reverted';
          const dateStr = new Date(rev.editedAt).toLocaleString(undefined, {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
          });

          return (
            <div key={rev.revisionId || idx} className="p-3.5 hover:bg-slate-800/30 transition flex flex-col gap-2">
              <div className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${
                      isPublished
                        ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                        : isReverted
                        ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                        : 'bg-blue-500/20 text-blue-400 border border-blue-500/30'
                    }`}
                  >
                    {isPublished ? (
                      <span className="flex items-center gap-1">
                        <CheckCircle className="w-2.5 h-2.5" /> Published
                      </span>
                    ) : isReverted ? (
                      <span className="flex items-center gap-1">
                        <Undo2 className="w-2.5 h-2.5" /> Reverted
                      </span>
                    ) : (
                      <span className="flex items-center gap-1">
                        <FileEdit className="w-2.5 h-2.5" /> Draft
                      </span>
                    )}
                  </span>
                  <span className="text-slate-400 flex items-center gap-1 font-mono text-[11px]">
                    <Clock className="w-3 h-3 text-slate-500" />
                    {dateStr}
                  </span>
                </div>

                <button
                  onClick={() => onRestore(rev)}
                  className="px-2 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white rounded text-xs flex items-center gap-1 transition border border-slate-700/60"
                  title="Load this revision text into the editor"
                >
                  <RotateCcw className="w-3 h-3 text-amber-400" />
                  <span>Restore</span>
                </button>
              </div>

              <div className="text-xs text-slate-300 bg-slate-950 p-2.5 rounded-lg border border-slate-800/80 font-sans line-clamp-3">
                {rev.text}
              </div>

              <div className="flex items-center justify-between text-[11px] text-slate-500">
                <span className="flex items-center gap-1">
                  <User className="w-3 h-3 text-slate-600" />
                  {rev.editedBy}
                </span>
                {rev.comment && <span className="italic text-slate-400">"{rev.comment}"</span>}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
