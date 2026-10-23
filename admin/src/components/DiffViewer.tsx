import React, { useMemo } from 'react';
import { diffWordsWithSpace } from 'diff';
import { PlusCircle, MinusCircle, CheckCircle2 } from 'lucide-react';

interface DiffViewerProps {
  originalText: string;
  revisedText: string;
  originalLabel?: string;
  revisedLabel?: string;
}

export const DiffViewer: React.FC<DiffViewerProps> = ({
  originalText,
  revisedText,
  originalLabel = 'Original Baseline',
  revisedLabel = 'Current Revision'
}) => {
  const diffParts = useMemo(() => {
    return diffWordsWithSpace(originalText || '', revisedText || '');
  }, [originalText, revisedText]);

  const stats = useMemo(() => {
    let addedWords = 0;
    let removedWords = 0;
    diffParts.forEach((part) => {
      const words = part.value.trim().split(/\s+/).filter(Boolean).length;
      if (part.added) addedWords += words;
      if (part.removed) removedWords += words;
    });
    return { addedWords, removedWords, isIdentical: addedWords === 0 && removedWords === 0 };
  }, [diffParts]);

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl overflow-hidden shadow-lg">
      <div className="bg-slate-800/60 px-4 py-2.5 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-300">Visual Narrative Diff</span>
          <span className="text-[11px] text-slate-500">• {originalLabel} vs {revisedLabel}</span>
        </div>
        <div className="flex items-center gap-3 text-xs">
          {stats.isIdentical ? (
            <span className="flex items-center gap-1 text-slate-400">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
              <span>Identical text</span>
            </span>
          ) : (
            <>
              <span className="flex items-center gap-1 text-emerald-400">
                <PlusCircle className="w-3.5 h-3.5" />
                <span>+{stats.addedWords} words</span>
              </span>
              <span className="flex items-center gap-1 text-red-400">
                <MinusCircle className="w-3.5 h-3.5" />
                <span>-{stats.removedWords} words</span>
              </span>
            </>
          )}
        </div>
      </div>

      <div className="p-4 leading-relaxed font-sans text-sm text-slate-200 whitespace-pre-wrap select-text">
        {diffParts.map((part, index) => {
          if (part.added) {
            return (
              <span key={index} className="diff-added">
                {part.value}
              </span>
            );
          }
          if (part.removed) {
            return (
              <span key={index} className="diff-removed">
                {part.value}
              </span>
            );
          }
          return (
            <span key={index} className="diff-unchanged">
              {part.value}
            </span>
          );
        })}
      </div>
    </div>
  );
};
