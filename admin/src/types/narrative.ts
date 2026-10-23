export type NarrativeType =
  | 'summary_point'
  | 'summary_story'
  | 'timeline_item'
  | 'collapse'
  | 'recovery'
  | 'momentum_story'
  | 'pressure_explanation'
  | 'pressure_verdict'
  | 'turning_point'
  | 'verdict_node';

export interface RevisionRecord {
  revisionId: string;
  text: string;
  status: 'draft' | 'published' | 'reverted';
  editedBy: string; // editor email or UID
  editedAt: string; // ISO 8601 string
  comment?: string;
}

export interface NarrativeOverrideDocument {
  id: string; // {matchId}_{narrativeType}_{itemKey}
  matchId: number;
  narrativeType: NarrativeType;
  itemKey: string;
  originalText: string;
  publishedText: string;
  draftText?: string;
  status: 'draft' | 'published' | 'reverted';
  updatedAt: string;
  updatedBy: string;
  publishedAt?: string | null;
  publishedBy?: string | null;
  revisionHistory: RevisionRecord[];
}

export interface MatchIndexItem {
  matchId: number;
  matchTitle: string;
  seriesHeader?: string;
  testSubHeader?: string;
  team1: string;
  team1Short?: string;
  team1Score?: string;
  team2: string;
  team2Short?: string;
  team2Score?: string;
  winner: string;
  victoryMargin?: string;
  venue: string;
  season: string;
  file: string;
}

export interface EditableNarrativeItem {
  id: string; // stable identifier: {matchId}_{narrativeType}_{itemKey}
  matchId: number;
  narrativeType: NarrativeType;
  itemKey: string;
  label: string;
  category: 'Match Story' | 'Story Timeline' | 'Momentum Flow' | 'Pressure Flow' | 'Turning Points';
  subLabel?: string;
  originalText: string;
  currentPublishedText: string;
  currentDraftText?: string;
  status: 'baseline' | 'draft' | 'published';
  lastEditedBy?: string;
  lastEditedAt?: string;
  revisionHistory: RevisionRecord[];
}

export interface EditorUser {
  uid: string;
  email: string | null;
  displayName: string | null;
  role: 'admin' | 'editor' | 'unauthorized';
  isActive: boolean;
}
