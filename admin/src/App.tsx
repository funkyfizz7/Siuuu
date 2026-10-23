import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { UnauthorizedScreen } from './components/UnauthorizedScreen';
import { MatchSelector } from './components/MatchSelector';
import { NarrativeCategoryTabs } from './components/NarrativeCategoryTabs';
import { NarrativeEditor } from './components/NarrativeEditor';
import { MatchIndexItem, EditableNarrativeItem, NarrativeOverrideDocument, RevisionRecord } from './types/narrative';
import { db, isFirebaseConfigured } from './firebase';
import { 
  collection, 
  query, 
  where, 
  getDocs, 
  doc, 
  setDoc, 
  deleteDoc, 
  serverTimestamp 
} from 'firebase/firestore';
import { 
  LogIn, 
  Sparkles, 
  AlertCircle, 
  Loader2, 
  CheckCircle2, 
  Info,
  Shield
} from 'lucide-react';

export const App: React.FC = () => {
  const { user, loading, isDemoMode, isFirebaseConfigured, signInWithGoogle, signInWithEmail, setDemoEditor } = useAuth();
  
  const [selectedMatch, setSelectedMatch] = useState<MatchIndexItem | null>(null);
  const [matchData, setMatchData] = useState<any | null>(null);
  const [narrativeItems, setNarrativeItems] = useState<EditableNarrativeItem[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('Match Story');
  const [selectedItem, setSelectedItem] = useState<EditableNarrativeItem | null>(null);
  const [isLoadingMatch, setIsLoadingMatch] = useState<boolean>(false);
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [loginError, setLoginError] = useState<string | null>(null);
  const [isSigningIn, setIsSigningIn] = useState(false);

  // Load Match JSON and merge with Firestore Overrides
  const loadMatchNarratives = useCallback(async (match: MatchIndexItem) => {
    setIsLoadingMatch(true);
    try {
      // 1. Fetch baseline JSON from static warehouse assets
      let fileName = match.file || `match_${match.matchId}.json`;
      if (fileName.startsWith('matches/')) {
        fileName = fileName.substring('matches/'.length);
      }
      const fetchUrl = `/matches/${fileName}`;
      const res = await fetch(fetchUrl);
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: Failed to load warehouse match data for match ${match.matchId} (${fetchUrl})`);
      }
      const contentType = res.headers.get('content-type');
      if (contentType && contentType.includes('text/html')) {
        throw new Error(`Expected JSON but received HTML from ${fetchUrl}. File may not exist in public/matches/.`);
      }
      const data = await res.json();
      setMatchData(data);

      // 2. Fetch published and draft overrides from Firestore or localStorage (demo mode)
      const overridesMap = new Map<string, NarrativeOverrideDocument>();

      if (isFirebaseConfigured) {
        try {
          const q = query(
            collection(db, 'match_narrative_overrides'),
            where('matchId', '==', match.matchId)
          );
          const snap = await getDocs(q);
          snap.forEach((d) => {
            overridesMap.set(d.id, { id: d.id, ...d.data() } as NarrativeOverrideDocument);
          });
        } catch (fErr) {
          console.warn('Could not query Firestore overrides, using local storage fallback:', fErr);
        }
      }

      // Check localStorage for offline demo overrides
      const localKey = `pitchiq_overrides_${match.matchId}`;
      const savedLocal = localStorage.getItem(localKey);
      if (savedLocal) {
        try {
          const parsed = JSON.parse(savedLocal) as Record<string, NarrativeOverrideDocument>;
          Object.entries(parsed).forEach(([k, v]) => {
            if (!overridesMap.has(k)) overridesMap.set(k, v);
          });
        } catch (e) {
          console.error('Error parsing local overrides:', e);
        }
      }

      // 3. Extract and build all verified narrative items based on existing warehouse schema
      const items: EditableNarrativeItem[] = [];

      // A. MATCH STORY (Summary Points & Match Story)
      const summaryPoints = data.summaryPoints || data.summaryTab?.summaryPoints || [];
      summaryPoints.forEach((sp: string, idx: number) => {
        const id = `${match.matchId}_summary_point_${idx}`;
        const ov = overridesMap.get(id);
        items.push({
          id,
          matchId: match.matchId,
          narrativeType: 'summary_point',
          itemKey: `${idx}`,
          label: `Summary Point #${idx + 1}`,
          category: 'Match Story',
          subLabel: `Key Takeaway ${idx + 1}`,
          originalText: sp,
          currentPublishedText: ov?.status === 'published' ? ov.publishedText : '',
          currentDraftText: ov?.draftText || '',
          status: ov?.status === 'published' ? 'published' : ov?.status === 'draft' ? 'draft' : 'baseline',
          lastEditedBy: ov?.updatedBy,
          lastEditedAt: ov?.updatedAt,
          revisionHistory: ov?.revisionHistory || []
        });
      });

      const matchStory = data.matchStory || data.summaryTab?.matchStory || '';
      if (matchStory) {
        const id = `${match.matchId}_summary_story_main`;
        const ov = overridesMap.get(id);
        items.push({
          id,
          matchId: match.matchId,
          narrativeType: 'summary_story',
          itemKey: 'main',
          label: 'Overall Match Story',
          category: 'Match Story',
          subLabel: 'Summary Paragraph',
          originalText: matchStory,
          currentPublishedText: ov?.status === 'published' ? ov.publishedText : '',
          currentDraftText: ov?.draftText || '',
          status: ov?.status === 'published' ? 'published' : ov?.status === 'draft' ? 'draft' : 'baseline',
          lastEditedBy: ov?.updatedBy,
          lastEditedAt: ov?.updatedAt,
          revisionHistory: ov?.revisionHistory || []
        });
      }

      // B. STORY TIMELINE (Day/Session timeline, Collapse, Recovery)
      const timeline = data.storyTab?.timeline || [];
      timeline.forEach((tl: any, idx: number) => {
        const day = tl.dayNumber || tl.day_number || 1;
        const session = tl.sessionOfDay || tl.session_of_day || idx + 1;
        const key = `d${day}_s${session}`;
        const id = `${match.matchId}_timeline_item_${key}`;
        const ov = overridesMap.get(id);

        items.push({
          id,
          matchId: match.matchId,
          narrativeType: 'timeline_item',
          itemKey: key,
          label: `Day ${day} • Session ${session}: ${tl.headline || 'Timeline Segment'}`,
          category: 'Story Timeline',
          subLabel: tl.keyMoment ? `Moment: ${tl.keyMoment}` : undefined,
          originalText: tl.summary || '',
          currentPublishedText: ov?.status === 'published' ? ov.publishedText : '',
          currentDraftText: ov?.draftText || '',
          status: ov?.status === 'published' ? 'published' : ov?.status === 'draft' ? 'draft' : 'baseline',
          lastEditedBy: ov?.updatedBy,
          lastEditedAt: ov?.updatedAt,
          revisionHistory: ov?.revisionHistory || []
        });
      });

      const primaryCollapse = data.storyTab?.primaryCollapse;
      if (primaryCollapse && primaryCollapse.explanation) {
        const id = `${match.matchId}_collapse_primary`;
        const ov = overridesMap.get(id);
        items.push({
          id,
          matchId: match.matchId,
          narrativeType: 'collapse',
          itemKey: 'primary',
          label: `Biggest Collapse: ${primaryCollapse.classification || 'Collapse Event'}`,
          category: 'Story Timeline',
          subLabel: `Innings ${primaryCollapse.innings || 1} • ${primaryCollapse.team || ''}`,
          originalText: primaryCollapse.explanation,
          currentPublishedText: ov?.status === 'published' ? ov.publishedText : '',
          currentDraftText: ov?.draftText || '',
          status: ov?.status === 'published' ? 'published' : ov?.status === 'draft' ? 'draft' : 'baseline',
          lastEditedBy: ov?.updatedBy,
          lastEditedAt: ov?.updatedAt,
          revisionHistory: ov?.revisionHistory || []
        });
      }

      const primaryRecovery = data.storyTab?.primaryRecovery;
      if (primaryRecovery && primaryRecovery.explanation) {
        const id = `${match.matchId}_recovery_primary`;
        const ov = overridesMap.get(id);
        items.push({
          id,
          matchId: match.matchId,
          narrativeType: 'recovery',
          itemKey: 'primary',
          label: `Biggest Recovery: ${primaryRecovery.classification || 'Rescue Stand'}`,
          category: 'Story Timeline',
          subLabel: `Innings ${primaryRecovery.innings || 1} • ${primaryRecovery.team || ''}`,
          originalText: primaryRecovery.explanation,
          currentPublishedText: ov?.status === 'published' ? ov.publishedText : '',
          currentDraftText: ov?.draftText || '',
          status: ov?.status === 'published' ? 'published' : ov?.status === 'draft' ? 'draft' : 'baseline',
          lastEditedBy: ov?.updatedBy,
          lastEditedAt: ov?.updatedAt,
          revisionHistory: ov?.revisionHistory || []
        });
      }

      // C. MOMENTUM FLOW (Session Context Stories)
      const momentumPoints = data.momentumTab?.points || [];
      momentumPoints.forEach((p: any) => {
        const sIdx = p.sessionIndex || p.session_index || 1;
        const id = `${match.matchId}_momentum_story_s${sIdx}`;
        const ov = overridesMap.get(id);
        const day = p.dayNumber || p.day_number || 1;
        const sOfDay = p.sessionOfDay || p.session_of_day || 1;

        items.push({
          id,
          matchId: match.matchId,
          narrativeType: 'momentum_story',
          itemKey: `s${sIdx}`,
          label: `Session ${sIdx} (Day ${day} • Sess ${sOfDay}): ${p.sessionTitle || p.battingTeam || ''}`,
          category: 'Momentum Flow',
          subLabel: `Momentum: ${p.momentumLabel || 'Balanced'} • Swing ${p.momentumSwing || 0.0}`,
          originalText: p.contextStory || p.narrative || '',
          currentPublishedText: ov?.status === 'published' ? ov.publishedText : '',
          currentDraftText: ov?.draftText || '',
          status: ov?.status === 'published' ? 'published' : ov?.status === 'draft' ? 'draft' : 'baseline',
          lastEditedBy: ov?.updatedBy,
          lastEditedAt: ov?.updatedAt,
          revisionHistory: ov?.revisionHistory || []
        });
      });

      // D. PRESSURE FLOW (Session Explanations and Verdicts)
      const pressurePoints = data.pressureTab?.pressurePoints || [];
      pressurePoints.forEach((pp: any) => {
        const sIdx = pp.sessionIndex || pp.session_index || 1;
        const day = pp.dayNumber || pp.day_number || 1;
        const sOfDay = pp.sessionOfDay || pp.session_of_day || 1;

        if (pp.explanation) {
          const id = `${match.matchId}_pressure_explanation_s${sIdx}`;
          const ov = overridesMap.get(id);
          items.push({
            id,
            matchId: match.matchId,
            narrativeType: 'pressure_explanation',
            itemKey: `s${sIdx}`,
            label: `Session ${sIdx} (Day ${day} • Sess ${sOfDay}) Pressure Explanation`,
            category: 'Pressure Flow',
            subLabel: `Zone: ${pp.pressureZone || 'Medium'} • Score: ${pp.pressureScore || 0}`,
            originalText: pp.explanation,
            currentPublishedText: ov?.status === 'published' ? ov.publishedText : '',
            currentDraftText: ov?.draftText || '',
            status: ov?.status === 'published' ? 'published' : ov?.status === 'draft' ? 'draft' : 'baseline',
            lastEditedBy: ov?.updatedBy,
            lastEditedAt: ov?.updatedAt,
            revisionHistory: ov?.revisionHistory || []
          });
        }

        if (pp.verdict) {
          const id = `${match.matchId}_pressure_verdict_s${sIdx}`;
          const ov = overridesMap.get(id);
          items.push({
            id,
            matchId: match.matchId,
            narrativeType: 'pressure_verdict',
            itemKey: `s${sIdx}`,
            label: `Session ${sIdx} (Day ${day} • Sess ${sOfDay}) Session Verdict`,
            category: 'Pressure Flow',
            subLabel: 'Key Takeaway',
            originalText: pp.verdict,
            currentPublishedText: ov?.status === 'published' ? ov.publishedText : '',
            currentDraftText: ov?.draftText || '',
            status: ov?.status === 'published' ? 'published' : ov?.status === 'draft' ? 'draft' : 'baseline',
            lastEditedBy: ov?.updatedBy,
            lastEditedAt: ov?.updatedAt,
            revisionHistory: ov?.revisionHistory || []
          });
        }
      });

      // E. TURNING POINTS & MATCH VERDICT CHAIN
      const turningPoints = data.turningPointsTab?.topTurningPoints || [];
      turningPoints.forEach((tp: any, idx: number) => {
        const rank = tp.rank || idx + 1;
        const id = `${match.matchId}_turning_point_${rank}`;
        const ov = overridesMap.get(id);

        items.push({
          id,
          matchId: match.matchId,
          narrativeType: 'turning_point',
          itemKey: `${rank}`,
          label: `Turning Point #${rank}: ${tp.title || tp.eventType || 'Event'}`,
          category: 'Turning Points',
          subLabel: `${tp.player || ''} (${tp.team || ''}) • Over ${tp.overNo || 0.0}`,
          originalText: tp.description || '',
          currentPublishedText: ov?.status === 'published' ? ov.publishedText : '',
          currentDraftText: ov?.draftText || '',
          status: ov?.status === 'published' ? 'published' : ov?.status === 'draft' ? 'draft' : 'baseline',
          lastEditedBy: ov?.updatedBy,
          lastEditedAt: ov?.updatedAt,
          revisionHistory: ov?.revisionHistory || []
        });
      });

      const verdictChain = data.turningPointsTab?.matchVerdictChain || [];
      verdictChain.forEach((vc: any, idx: number) => {
        const step = vc.stepIndex || idx + 1;
        const id = `${match.matchId}_verdict_node_${step}`;
        const ov = overridesMap.get(id);

        items.push({
          id,
          matchId: match.matchId,
          narrativeType: 'verdict_node',
          itemKey: `${step}`,
          label: `Verdict Phase ${step}: ${vc.headline || vc.phase || 'Phase'}`,
          category: 'Turning Points',
          subLabel: vc.scoreSnippet || undefined,
          originalText: vc.description || '',
          currentPublishedText: ov?.status === 'published' ? ov.publishedText : '',
          currentDraftText: ov?.draftText || '',
          status: ov?.status === 'published' ? 'published' : ov?.status === 'draft' ? 'draft' : 'baseline',
          lastEditedBy: ov?.updatedBy,
          lastEditedAt: ov?.updatedAt,
          revisionHistory: ov?.revisionHistory || []
        });
      });

      setNarrativeItems(items);
      if (items.length > 0) {
        setSelectedItem(items[0]);
      }
    } catch (err: any) {
      console.error('Error loading match narratives:', err);
      alert(`Could not load match data: ${err.message}`);
    } finally {
      setIsLoadingMatch(false);
    }
  }, [isFirebaseConfigured]);

  // Handle Match Selection
  const handleSelectMatch = (match: MatchIndexItem) => {
    setSelectedMatch(match);
    loadMatchNarratives(match);
  };

  // Save Draft Handler
  const handleSaveDraft = async (item: EditableNarrativeItem, draftText: string, comment?: string) => {
    const revision: RevisionRecord = {
      revisionId: `rev_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
      text: draftText,
      status: 'draft',
      editedBy: user?.email || user?.displayName || 'Authorized Editor',
      editedAt: new Date().toISOString(),
      comment
    };

    const updatedDoc: NarrativeOverrideDocument = {
      id: item.id,
      matchId: item.matchId,
      narrativeType: item.narrativeType,
      itemKey: item.itemKey,
      originalText: item.originalText,
      publishedText: item.currentPublishedText,
      draftText: draftText,
      status: 'draft',
      updatedAt: revision.editedAt,
      updatedBy: revision.editedBy,
      revisionHistory: [...(item.revisionHistory || []), revision]
    };

    // 1. Save to Firestore if configured
    if (isFirebaseConfigured) {
      const docRef = doc(db, 'match_narrative_overrides', item.id);
      await setDoc(docRef, updatedDoc, { merge: true });
    }

    // 2. Save to localStorage for offline cache / demo mode
    const localKey = `pitchiq_overrides_${item.matchId}`;
    const curLocal = JSON.parse(localStorage.getItem(localKey) || '{}');
    curLocal[item.id] = updatedDoc;
    localStorage.setItem(localKey, JSON.stringify(curLocal));

    // 3. Update React State
    setNarrativeItems((prev) =>
      prev.map((i) =>
        i.id === item.id
          ? {
              ...i,
              currentDraftText: draftText,
              status: 'draft',
              lastEditedBy: revision.editedBy,
              lastEditedAt: revision.editedAt,
              revisionHistory: updatedDoc.revisionHistory
            }
          : i
      )
    );
    setSelectedItem((prev) =>
      prev && prev.id === item.id
        ? {
            ...prev,
            currentDraftText: draftText,
            status: 'draft',
            lastEditedBy: revision.editedBy,
            lastEditedAt: revision.editedAt,
            revisionHistory: updatedDoc.revisionHistory
          }
        : prev
    );
  };

  // Publish Revision Handler
  const handlePublish = async (item: EditableNarrativeItem, publishedText: string, comment?: string) => {
    const now = new Date().toISOString();
    const editorIdentifier = user?.email || user?.displayName || 'Authorized Editor';

    const revision: RevisionRecord = {
      revisionId: `rev_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
      text: publishedText,
      status: 'published',
      editedBy: editorIdentifier,
      editedAt: now,
      comment
    };

    const updatedDoc: NarrativeOverrideDocument = {
      id: item.id,
      matchId: item.matchId,
      narrativeType: item.narrativeType,
      itemKey: item.itemKey,
      originalText: item.originalText,
      publishedText: publishedText,
      draftText: publishedText,
      status: 'published',
      updatedAt: now,
      updatedBy: editorIdentifier,
      publishedAt: now,
      publishedBy: editorIdentifier,
      revisionHistory: [...(item.revisionHistory || []), revision]
    };

    // 1. Save to local bridge server (updates project warehouse asset file and serves overrides)
    try {
      await fetch('/api/publish-override', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ overrideDoc: updatedDoc })
      });
    } catch (bridgeErr) {
      console.warn('Local bridge server not available:', bridgeErr);
    }

    // 2. Save to Firestore if configured
    if (isFirebaseConfigured) {
      try {
        const docRef = doc(db, 'match_narrative_overrides', item.id);
        await setDoc(docRef, updatedDoc, { merge: true });
      } catch (fErr) {
        console.error('Firestore save error:', fErr);
      }
    }

    // 3. Save to localStorage for browser persistence
    const localKey = `pitchiq_overrides_${item.matchId}`;
    const curLocal = JSON.parse(localStorage.getItem(localKey) || '{}');
    curLocal[item.id] = updatedDoc;
    localStorage.setItem(localKey, JSON.stringify(curLocal));

    // 3. Update React State
    setNarrativeItems((prev) =>
      prev.map((i) =>
        i.id === item.id
          ? {
              ...i,
              currentPublishedText: publishedText,
              currentDraftText: publishedText,
              status: 'published',
              lastEditedBy: editorIdentifier,
              lastEditedAt: now,
              revisionHistory: updatedDoc.revisionHistory
            }
          : i
      )
    );
    setSelectedItem((prev) =>
      prev && prev.id === item.id
        ? {
            ...prev,
            currentPublishedText: publishedText,
            currentDraftText: publishedText,
            status: 'published',
            lastEditedBy: editorIdentifier,
            lastEditedAt: now,
            revisionHistory: updatedDoc.revisionHistory
          }
        : prev
    );
  };

  // Revert Override Handler
  const handleRevert = async (item: EditableNarrativeItem) => {
    const now = new Date().toISOString();
    const editorIdentifier = user?.email || user?.displayName || 'Authorized Editor';

    const revision: RevisionRecord = {
      revisionId: `rev_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
      text: item.originalText,
      status: 'reverted',
      editedBy: editorIdentifier,
      editedAt: now,
      comment: 'Reverted to original warehouse baseline.'
    };

    // 1. Revert on local bridge server
    try {
      await fetch('/api/revert-override', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ item })
      });
    } catch (e) {}

    // 2. Revert in Firestore if configured
    if (isFirebaseConfigured) {
      try {
        const docRef = doc(db, 'match_narrative_overrides', item.id);
        await deleteDoc(docRef);
      } catch (e) {
        console.error('Error deleting doc from Firestore:', e);
      }
    }

    // 3. Clear from localStorage
    const localKey = `pitchiq_overrides_${item.matchId}`;
    const curLocal = JSON.parse(localStorage.getItem(localKey) || '{}');
    delete curLocal[item.id];
    localStorage.setItem(localKey, JSON.stringify(curLocal));

    // Update React State
    setNarrativeItems((prev) =>
      prev.map((i) =>
        i.id === item.id
          ? {
              ...i,
              currentPublishedText: '',
              currentDraftText: '',
              status: 'baseline',
              lastEditedBy: editorIdentifier,
              lastEditedAt: now,
              revisionHistory: [...(item.revisionHistory || []), revision]
            }
          : i
      )
    );
    setSelectedItem((prev) =>
      prev && prev.id === item.id
        ? {
            ...prev,
            currentPublishedText: '',
            currentDraftText: '',
            status: 'baseline',
            lastEditedBy: editorIdentifier,
            lastEditedAt: now,
            revisionHistory: [...(prev.revisionHistory || []), revision]
          }
        : prev
    );
  };

  // Login Handlers
  const handleEmailSignIn = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!loginEmail || !loginPassword) return;
    setIsSigningIn(true);
    setLoginError(null);
    try {
      await signInWithEmail(loginEmail, loginPassword);
    } catch (err: any) {
      setLoginError(err.message || 'Failed to sign in. Verify credentials.');
    } finally {
      setIsSigningIn(false);
    }
  };

  // Automatically select first match on initial load
  useEffect(() => {
    if (user && user.role !== 'unauthorized' && !selectedMatch) {
      handleSelectMatch({
        matchId: 1152846,
        matchTitle: 'Australia vs England (2019)',
        team1: 'Australia',
        team2: 'England',
        winner: 'Australia',
        victoryMargin: 'Australia won by 251 runs',
        venue: 'Edgbaston',
        season: '2019',
        file: 'match_1152846.json'
      });
    }
  }, [user]);

  // Loading state
  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center text-slate-400">
        <Loader2 className="w-8 h-8 animate-spin text-amber-500" />
      </div>
    );
  }

  // Unauthorized Account state
  if (user && user.role === 'unauthorized') {
    return (
      <>
        <Navbar />
        <UnauthorizedScreen />
      </>
    );
  }

  // Unauthenticated Sign-In Screen
  if (!user) {
    return (
      <div className="min-h-screen bg-slate-950 flex flex-col">
        <Navbar />
        <div className="flex-1 flex items-center justify-center p-6">
          <div className="max-w-md w-full bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-2xl space-y-6">
            <div className="text-center space-y-2">
              <div className="w-12 h-12 mx-auto rounded-2xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
                <Shield className="w-6 h-6" />
              </div>
              <h1 className="text-xl font-bold text-white tracking-tight">Editor Authentication</h1>
              <p className="text-xs text-slate-400">
                Sign in with an authorized editor account to review and publish PitchIQ cricket narratives.
              </p>
            </div>

            {loginError && (
              <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                <span>{loginError}</span>
              </div>
            )}

            {/* Google Sign In */}
            <button
              onClick={() => signInWithGoogle()}
              className="w-full py-2.5 px-4 bg-white hover:bg-slate-100 text-slate-900 rounded-xl font-semibold text-xs transition flex items-center justify-center gap-2 shadow"
            >
              <svg className="w-4 h-4" viewBox="0 0 24 24">
                <path
                  fill="#4285F4"
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                />
                <path
                  fill="#34A853"
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                />
                <path
                  fill="#FBBC05"
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                />
                <path
                  fill="#EA4335"
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                />
              </svg>
              <span>Sign in with Google Workspace</span>
            </button>

            <div className="flex items-center gap-3 text-slate-600 text-xs">
              <div className="flex-1 border-t border-slate-800" />
              <span>or email</span>
              <div className="flex-1 border-t border-slate-800" />
            </div>

            {/* Email / Password Form */}
            <form onSubmit={handleEmailSignIn} className="space-y-3">
              <div>
                <input
                  type="email"
                  value={loginEmail}
                  onChange={(e) => setLoginEmail(e.target.value)}
                  placeholder="editor@pitchiq.com"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-200 placeholder-slate-600 focus:outline-none focus:border-amber-500/50 transition"
                />
              </div>
              <div>
                <input
                  type="password"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  placeholder="Password"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-200 placeholder-slate-600 focus:outline-none focus:border-amber-500/50 transition"
                />
              </div>
              <button
                type="submit"
                disabled={isSigningIn}
                className="w-full py-2 px-4 bg-slate-800 hover:bg-slate-700 text-white rounded-lg font-semibold text-xs transition border border-slate-700 disabled:opacity-50"
              >
                {isSigningIn ? 'Signing in...' : 'Sign In with Email'}
              </button>
            </form>

            {/* Local Test Mode Button */}
            {!isFirebaseConfigured && (
              <div className="pt-2 border-t border-slate-800/80">
                <button
                  type="button"
                  onClick={() => setDemoEditor('admin')}
                  className="w-full py-2.5 px-4 bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 border border-amber-500/30 rounded-xl font-semibold text-xs transition flex items-center justify-center gap-2"
                >
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>Launch in Local Sandbox Mode</span>
                </button>
                <p className="text-[11px] text-slate-500 text-center mt-2 leading-tight">
                  Allows testing the complete workflow offline before linking a Firebase project.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  // Authenticated Editorial Workspace
  return (
    <div className="min-h-screen bg-slate-950 flex flex-col text-slate-100">
      <Navbar />

      <main className="flex-1 p-4 grid grid-cols-1 lg:grid-cols-12 gap-4 max-w-[1700px] w-full mx-auto">
        {/* Left Column: Match Selector (Col 1 to 3) */}
        <div className="lg:col-span-3 h-[calc(100vh-95px)]">
          <MatchSelector
            selectedMatchId={selectedMatch?.matchId || null}
            onSelectMatch={handleSelectMatch}
          />
        </div>

        {/* Middle Column: Narrative Categories & Items (Col 4 to 7) */}
        <div className="lg:col-span-4 h-[calc(100vh-95px)]">
          {isLoadingMatch ? (
            <div className="bg-slate-900 border border-slate-800 rounded-xl h-full flex flex-col items-center justify-center text-slate-400 gap-3">
              <Loader2 className="w-7 h-7 animate-spin text-amber-500" />
              <span className="text-xs">Loading match intelligence & narratives...</span>
            </div>
          ) : (
            <NarrativeCategoryTabs
              items={narrativeItems}
              selectedCategory={selectedCategory}
              selectedItemId={selectedItem?.id || null}
              onSelectCategory={setSelectedCategory}
              onSelectItem={(item) => setSelectedItem(item)}
            />
          )}
        </div>

        {/* Right Column: Narrative Editor & Diff Workspace (Col 8 to 12) */}
        <div className="lg:col-span-5 h-[calc(100vh-95px)]">
          {selectedItem ? (
            <NarrativeEditor
              key={selectedItem.id}
              item={selectedItem}
              onSaveDraft={handleSaveDraft}
              onPublish={handlePublish}
              onRevert={handleRevert}
            />
          ) : (
            <div className="bg-slate-900 border border-slate-800 rounded-xl h-full flex flex-col items-center justify-center text-slate-500 text-xs p-6 text-center">
              <Info className="w-8 h-8 mb-2 text-slate-600" />
              <p>Select a narrative item from the category list to begin reviewing or editing.</p>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};
