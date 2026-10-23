import React, { useState, useMemo } from 'react';
import matchesIndexRaw from '../data/matches_index.json';
import { MatchIndexItem } from '../types/narrative';
import { Search, Trophy, Calendar, MapPin, ChevronRight } from 'lucide-react';

interface MatchSelectorProps {
  selectedMatchId: number | null;
  onSelectMatch: (match: MatchIndexItem) => void;
}

export const MatchSelector: React.FC<MatchSelectorProps> = ({ selectedMatchId, onSelectMatch }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedSeason, setSelectedSeason] = useState<string>('all');

  const matches: MatchIndexItem[] = useMemo(() => {
    return (matchesIndexRaw as any[]).map((m) => ({
      matchId: Number(m.match_id || m.matchId),
      matchTitle: m.match_title || m.matchTitle || `${m.team1} vs ${m.team2}`,
      seriesHeader: m.series_header || m.seriesHeader || '',
      testSubHeader: m.test_sub_header || m.testSubHeader || '',
      team1: m.team1 || '',
      team1Short: m.team1_short || m.team1Short || '',
      team1Score: m.team1_score || m.team1Score || '',
      team2: m.team2 || '',
      team2Short: m.team2_short || m.team2Short || '',
      team2Score: m.team2_score || m.team2Score || '',
      winner: m.winner || '',
      victoryMargin: m.victory_margin || m.victoryMargin || '',
      venue: m.venue || '',
      season: String(m.season || ''),
      file: (m.file ? String(m.file).replace(/^matches\//, '') : `match_${m.match_id || m.matchId}.json`)
    }));
  }, []);

  const seasons = useMemo(() => {
    const s = new Set<string>();
    matches.forEach((m) => {
      if (m.season) s.add(m.season);
    });
    return Array.from(s).sort().reverse();
  }, [matches]);

  const filteredMatches = useMemo(() => {
    const term = searchTerm.toLowerCase().trim();
    return matches.filter((m) => {
      const matchSeason = selectedSeason === 'all' || m.season === selectedSeason;
      if (!matchSeason) return false;
      if (!term) return true;

      return (
        m.matchId.toString().includes(term) ||
        m.matchTitle.toLowerCase().includes(term) ||
        m.team1.toLowerCase().includes(term) ||
        m.team2.toLowerCase().includes(term) ||
        m.venue.toLowerCase().includes(term) ||
        m.winner.toLowerCase().includes(term) ||
        (m.seriesHeader ? m.seriesHeader.toLowerCase().includes(term) : false)
      );
    });
  }, [matches, searchTerm, selectedSeason]);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden flex flex-col h-full shadow-lg">
      <div className="p-4 border-b border-slate-800 space-y-3 bg-slate-900/60">
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-300">
            Select Match ({matches.length} Total)
          </span>
          <span className="text-xs text-amber-400 font-mono">
            {filteredMatches.length} Found
          </span>
        </div>

        {/* Search Input */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search teams, venue, ID, series..."
            className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-amber-500/50 transition"
          />
        </div>

        {/* Season Filter Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs">
          <button
            onClick={() => setSelectedSeason('all')}
            className={`px-2.5 py-1 rounded-md text-[11px] font-medium transition ${
              selectedSeason === 'all'
                ? 'bg-amber-500 text-slate-950 font-bold'
                : 'bg-slate-800 text-slate-400 hover:text-white'
            }`}
          >
            All Seasons
          </button>
          {seasons.map((s) => (
            <button
              key={s}
              onClick={() => setSelectedSeason(s)}
              className={`px-2.5 py-1 rounded-md text-[11px] font-medium whitespace-nowrap transition ${
                selectedSeason === s
                  ? 'bg-amber-500 text-slate-950 font-bold'
                  : 'bg-slate-800 text-slate-400 hover:text-white'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {/* Matches List */}
      <div className="overflow-y-auto divide-y divide-slate-800/60 flex-1 max-h-[calc(100vh-270px)]">
        {filteredMatches.map((m) => {
          const isSelected = selectedMatchId === m.matchId;

          return (
            <button
              key={m.matchId}
              onClick={() => onSelectMatch(m)}
              className={`w-full p-3.5 text-left transition flex items-center justify-between gap-3 ${
                isSelected
                  ? 'bg-amber-500/10 border-l-4 border-amber-500'
                  : 'hover:bg-slate-800/40 border-l-4 border-transparent'
              }`}
            >
              <div className="space-y-1 min-w-0">
                <div className="flex items-center gap-2">
                  <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700/60">
                    #{m.matchId}
                  </span>
                  <span className="text-xs font-semibold text-slate-200 truncate">
                    {m.team1} vs {m.team2}
                  </span>
                </div>

                <div className="text-[11px] text-slate-400 flex items-center gap-2 truncate">
                  <span className="flex items-center gap-1">
                    <MapPin className="w-3 h-3 text-slate-500 flex-shrink-0" />
                    {m.venue}
                  </span>
                  <span>•</span>
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3 h-3 text-slate-500 flex-shrink-0" />
                    {m.season}
                  </span>
                </div>

                {m.winner && (
                  <div className="text-[11px] text-emerald-400/90 flex items-center gap-1 font-medium truncate">
                    <Trophy className="w-3 h-3 text-amber-400 flex-shrink-0" />
                    <span>{m.winner} ({m.victoryMargin || 'Won'})</span>
                  </div>
                )}
              </div>

              <ChevronRight
                className={`w-4 h-4 flex-shrink-0 transition ${
                  isSelected ? 'text-amber-400 translate-x-0.5' : 'text-slate-600'
                }`}
              />
            </button>
          );
        })}

        {filteredMatches.length === 0 && (
          <div className="p-8 text-center text-slate-500 text-xs">
            No matches matching your search criteria.
          </div>
        )}
      </div>
    </div>
  );
};
