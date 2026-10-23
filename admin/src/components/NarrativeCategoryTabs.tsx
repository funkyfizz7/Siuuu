import React from 'react';
import { EditableNarrativeItem } from '../types/narrative';
import { Sparkles, Clock, Activity, Gauge, Zap } from 'lucide-react';

interface NarrativeCategoryTabsProps {
  items: EditableNarrativeItem[];
  selectedCategory: string;
  selectedItemId: string | null;
  onSelectCategory: (cat: string) => void;
  onSelectItem: (item: EditableNarrativeItem) => void;
}

export const NarrativeCategoryTabs: React.FC<NarrativeCategoryTabsProps> = ({
  items,
  selectedCategory,
  selectedItemId,
  onSelectCategory,
  onSelectItem
}) => {
  const categories = [
    { id: 'Match Story', label: 'Match Story', icon: Sparkles },
    { id: 'Story Timeline', label: 'Story Timeline', icon: Clock },
    { id: 'Momentum Flow', label: 'Momentum Flow', icon: Activity },
    { id: 'Pressure Flow', label: 'Pressure Flow', icon: Gauge },
    { id: 'Turning Points', label: 'Turning Points', icon: Zap }
  ];

  const categoryItems = items.filter((item) => item.category === selectedCategory);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-lg flex flex-col h-full">
      {/* Category Tabs Header */}
      <div className="flex border-b border-slate-800 bg-slate-900/60 overflow-x-auto p-1.5 gap-1">
        {categories.map((cat) => {
          const Icon = cat.icon;
          const isSelected = selectedCategory === cat.id;
          const count = items.filter((i) => i.category === cat.id).length;
          const hasPublished = items.some((i) => i.category === cat.id && i.status === 'published');
          const hasDraft = items.some((i) => i.category === cat.id && i.status === 'draft');

          return (
            <button
              key={cat.id}
              onClick={() => onSelectCategory(cat.id)}
              className={`flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-medium whitespace-nowrap transition flex-1 justify-center ${
                isSelected
                  ? 'bg-amber-500 text-slate-950 font-bold shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`}
            >
              <Icon className="w-3.5 h-3.5" />
              <span>{cat.label}</span>
              <span
                className={`text-[10px] px-1.5 py-0.2 rounded-full font-mono ${
                  isSelected
                    ? 'bg-slate-950/20 text-slate-950 font-bold'
                    : 'bg-slate-800 text-slate-400'
                }`}
              >
                {count}
              </span>
              {hasPublished && !isSelected && (
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" title="Contains published revisions" />
              )}
              {hasDraft && !isSelected && (
                <span className="w-1.5 h-1.5 rounded-full bg-blue-400" title="Contains active drafts" />
              )}
            </button>
          );
        })}
      </div>

      {/* Narrative Items Sub-List */}
      <div className="overflow-y-auto divide-y divide-slate-800/60 flex-1 max-h-[calc(100vh-270px)]">
        {categoryItems.map((item) => {
          const isSelected = selectedItemId === item.id;
          const isPublished = item.status === 'published';
          const isDraft = item.status === 'draft';

          return (
            <button
              key={item.id}
              onClick={() => onSelectItem(item)}
              className={`w-full p-3.5 text-left transition flex flex-col gap-1.5 ${
                isSelected
                  ? 'bg-amber-500/10 border-l-4 border-amber-500'
                  : 'hover:bg-slate-800/40 border-l-4 border-transparent'
              }`}
            >
              <div className="flex items-center justify-between gap-2">
                <span className="text-xs font-semibold text-slate-200 truncate">{item.label}</span>
                <div className="flex items-center gap-1.5">
                  {isPublished && (
                    <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                      Published
                    </span>
                  )}
                  {isDraft && (
                    <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-blue-500/20 text-blue-400 border border-blue-500/30">
                      Draft
                    </span>
                  )}
                  {!isPublished && !isDraft && (
                    <span className="text-[10px] text-slate-500 font-mono">Original</span>
                  )}
                </div>
              </div>

              {item.subLabel && (
                <div className="text-[11px] text-amber-400/90 font-mono">{item.subLabel}</div>
              )}

              <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                {item.currentPublishedText || item.originalText}
              </p>
            </button>
          );
        })}

        {categoryItems.length === 0 && (
          <div className="p-8 text-center text-slate-500 text-xs">
            No narrative items found in this category for the selected match.
          </div>
        )}
      </div>
    </div>
  );
};
