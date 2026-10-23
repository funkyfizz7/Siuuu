import React, { useState, useEffect } from 'react';
import { EditableNarrativeItem, RevisionRecord } from '../types/narrative';
import { DiffViewer } from './DiffViewer';
import { RevisionHistory } from './RevisionHistory';
import { useAuth } from '../context/AuthContext';
import { 
  Save, 
  Send, 
  RotateCcw, 
  Copy, 
  Check, 
  Eye, 
  Edit3, 
  History, 
  Sparkles,
  AlertCircle,
  FileCheck2
} from 'lucide-react';

interface NarrativeEditorProps {
  item: EditableNarrativeItem;
  onSaveDraft: (item: EditableNarrativeItem, text: string, comment?: string) => Promise<void>;
  onPublish: (item: EditableNarrativeItem, text: string, comment?: string) => Promise<void>;
  onRevert: (item: EditableNarrativeItem) => Promise<void>;
}

export const NarrativeEditor: React.FC<NarrativeEditorProps> = ({
  item,
  onSaveDraft,
  onPublish,
  onRevert
}) => {
  const { user } = useAuth();
  const [editText, setEditText] = useState(item.currentDraftText || item.currentPublishedText || item.originalText);
  const [editorialNote, setEditorialNote] = useState('');
  const [activeTab, setActiveTab] = useState<'editor' | 'diff' | 'history'>('editor');
  const [isSaving, setIsSaving] = useState(false);
  const [copiedOriginal, setCopiedOriginal] = useState(false);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  useEffect(() => {
    setEditText(item.currentDraftText || item.currentPublishedText || item.originalText);
    setEditorialNote('');
    setActiveTab('editor');
    setActionSuccess(null);
  }, [item.id, item.currentDraftText, item.currentPublishedText, item.originalText]);

  const copyOriginal = () => {
    navigator.clipboard.writeText(item.originalText);
    setCopiedOriginal(true);
    setTimeout(() => setCopiedOriginal(false), 2000);
  };

  const handleSaveDraft = async () => {
    if (!editText.trim()) return;
    setIsSaving(true);
    try {
      await onSaveDraft(item, editText.trim(), editorialNote.trim() || undefined);
      setActionSuccess('Draft saved successfully!');
      setTimeout(() => setActionSuccess(null), 3000);
    } catch (err: any) {
      alert(`Failed to save draft: ${err.message}`);
    } finally {
      setIsSaving(false);
    }
  };

  const handlePublish = async () => {
    if (!editText.trim()) return;
    const confirmPublish = window.confirm(
      'Are you sure you want to publish this revision? It will immediately override the narrative in the PitchIQ app.'
    );
    if (!confirmPublish) return;

    setIsSaving(true);
    try {
      await onPublish(item, editText.trim(), editorialNote.trim() || undefined);
      setActionSuccess('Revision published successfully to PitchIQ!');
      setTimeout(() => setActionSuccess(null), 3000);
    } catch (err: any) {
      alert(`Failed to publish: ${err.message}`);
    } finally {
      setIsSaving(false);
    }
  };

  const handleRevert = async () => {
    const confirmRevert = window.confirm(
      'Are you sure you want to revert this narrative back to the original baseline? All active overrides will be removed.'
    );
    if (!confirmRevert) return;

    setIsSaving(true);
    try {
      await onRevert(item);
      setEditText(item.originalText);
      setActionSuccess('Reverted back to baseline original text.');
      setTimeout(() => setActionSuccess(null), 3000);
    } catch (err: any) {
      alert(`Failed to revert: ${err.message}`);
    } finally {
      setIsSaving(false);
    }
  };

  const handleRestoreRevision = (rev: RevisionRecord) => {
    setEditText(rev.text);
    setActiveTab('editor');
    setActionSuccess(`Restored text from revision by ${rev.editedBy}`);
    setTimeout(() => setActionSuccess(null), 3000);
  };

  const wordCount = editText.trim().split(/\s+/).filter(Boolean).length;
  const charCount = editText.length;
  const isModifiedFromOriginal = editText.trim() !== item.originalText.trim();
  const isModifiedFromPublished = editText.trim() !== (item.currentPublishedText || item.originalText).trim();

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-xl flex flex-col h-full">
      {/* Header Bar */}
      <div className="p-4 border-b border-slate-800 bg-slate-900/80 flex flex-wrap items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-sm font-bold text-white tracking-tight">{item.label}</h2>
            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-amber-400 border border-slate-700">
              {item.id}
            </span>
            {item.status === 'published' && (
              <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                Published Live
              </span>
            )}
            {item.status === 'draft' && (
              <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-blue-500/20 text-blue-400 border border-blue-500/30">
                Draft Saved
              </span>
            )}
          </div>
          <div className="text-xs text-slate-400 mt-0.5 flex items-center gap-2">
            <span>Category: <strong className="text-slate-300">{item.category}</strong></span>
            {item.subLabel && <span>• <span className="text-amber-400/90 font-mono">{item.subLabel}</span></span>}
          </div>
        </div>

        {/* View Switcher Tabs */}
        <div className="flex items-center bg-slate-950 p-1 rounded-lg border border-slate-800 text-xs">
          <button
            onClick={() => setActiveTab('editor')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md font-medium transition ${
              activeTab === 'editor'
                ? 'bg-amber-500 text-slate-950 font-bold'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Edit3 className="w-3.5 h-3.5" />
            <span>Editor</span>
          </button>
          <button
            onClick={() => setActiveTab('diff')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md font-medium transition ${
              activeTab === 'diff'
                ? 'bg-amber-500 text-slate-950 font-bold'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Eye className="w-3.5 h-3.5" />
            <span>Diff Preview</span>
          </button>
          <button
            onClick={() => setActiveTab('history')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md font-medium transition ${
              activeTab === 'history'
                ? 'bg-amber-500 text-slate-950 font-bold'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <History className="w-3.5 h-3.5" />
            <span>History ({item.revisionHistory?.length || 0})</span>
          </button>
        </div>
      </div>

      {actionSuccess && (
        <div className="px-4 py-2 bg-emerald-500/15 border-b border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2 animate-fadeIn">
          <FileCheck2 className="w-4 h-4 text-emerald-400 flex-shrink-0" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {/* Main Content Area */}
      <div className="p-4 overflow-y-auto flex-1 space-y-4 max-h-[calc(100vh-320px)]">
        {/* Original Baseline Reference Card */}
        <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 relative group">
          <div className="flex items-center justify-between mb-1.5">
            <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
              <Sparkles className="w-3 h-3 text-amber-400" />
              Original Bundled Warehouse Text (Baseline)
            </span>
            <button
              onClick={copyOriginal}
              className="text-[11px] text-slate-400 hover:text-white flex items-center gap-1 px-2 py-0.5 rounded bg-slate-900 border border-slate-800 transition"
              title="Copy original text"
            >
              {copiedOriginal ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
              <span>{copiedOriginal ? 'Copied' : 'Copy'}</span>
            </button>
          </div>
          <p className="text-xs text-slate-300 leading-relaxed font-sans select-text">
            {item.originalText}
          </p>
        </div>

        {/* Tab 1: Editor View */}
        {activeTab === 'editor' && (
          <div className="space-y-3">
            <div className="flex items-center justify-between text-xs text-slate-400">
              <span className="font-semibold text-slate-300 uppercase tracking-wider text-[11px]">
                Editorial Working Draft
              </span>
              <div className="flex items-center gap-3 font-mono text-[11px]">
                <span>{wordCount} words</span>
                <span>•</span>
                <span>{charCount} characters</span>
              </div>
            </div>

            <textarea
              value={editText}
              onChange={(e) => setEditText(e.target.value)}
              rows={6}
              placeholder="Write revised cricket narrative text here..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3.5 text-sm text-slate-100 placeholder-slate-600 focus:outline-none focus:border-amber-500/60 transition resize-y leading-relaxed font-sans shadow-inner"
            />

            {/* Change Note */}
            <div>
              <label className="block text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-1">
                Editorial Change Note / Rationale (Optional)
              </label>
              <input
                type="text"
                value={editorialNote}
                onChange={(e) => setEditorialNote(e.target.value)}
                placeholder="e.g. Corrected bowler name, clarified match situation in session 3..."
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-200 placeholder-slate-600 focus:outline-none focus:border-amber-500/50 transition font-sans"
              />
            </div>
          </div>
        )}

        {/* Tab 2: Diff Preview View */}
        {activeTab === 'diff' && (
          <div className="space-y-4">
            <DiffViewer
              originalText={item.originalText}
              revisedText={editText}
              originalLabel="Original Baseline"
              revisedLabel="Your Active Revision"
            />
            {item.currentPublishedText && item.currentPublishedText !== item.originalText && (
              <DiffViewer
                originalText={item.currentPublishedText}
                revisedText={editText}
                originalLabel="Currently Published Live"
                revisedLabel="Your Active Revision"
              />
            )}
          </div>
        )}

        {/* Tab 3: History View */}
        {activeTab === 'history' && (
          <RevisionHistory
            revisions={item.revisionHistory || []}
            onRestore={handleRestoreRevision}
          />
        )}
      </div>

      {/* Action Footer Bar */}
      <div className="p-4 border-t border-slate-800 bg-slate-900/90 flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          {item.status !== 'baseline' && (
            <button
              onClick={handleRevert}
              disabled={isSaving}
              className="px-3 py-2 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30 text-xs font-medium transition flex items-center gap-1.5 disabled:opacity-50"
              title="Remove override and revert to baseline"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Revert to Baseline</span>
            </button>
          )}

          <button
            onClick={() => setEditText(item.currentDraftText || item.currentPublishedText || item.originalText)}
            disabled={!isModifiedFromPublished && !isModifiedFromOriginal}
            className="px-3 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white border border-slate-700/60 text-xs font-medium transition disabled:opacity-40"
          >
            Reset
          </button>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleSaveDraft}
            disabled={isSaving || !editText.trim()}
            className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-semibold transition flex items-center gap-1.5 disabled:opacity-50 shadow"
          >
            <Save className="w-3.5 h-3.5 text-blue-400" />
            <span>{isSaving ? 'Saving...' : 'Save Draft'}</span>
          </button>

          <button
            onClick={handlePublish}
            disabled={isSaving || !editText.trim()}
            className="px-5 py-2 rounded-lg bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs transition flex items-center gap-1.5 shadow-lg shadow-amber-500/20 disabled:opacity-50"
          >
            <Send className="w-3.5 h-3.5 text-slate-950 font-bold" />
            <span>{isSaving ? 'Publishing...' : 'Publish to PitchIQ'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
