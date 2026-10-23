import React, { createContext, useContext, useEffect, useState } from 'react';
import { 
  signInWithPopup, 
  signInWithEmailAndPassword, 
  signOut as fbSignOut, 
  onAuthStateChanged,
  User as FirebaseUser
} from 'firebase/auth';
import { doc, getDoc } from 'firebase/firestore';
import { auth, db, googleProvider, isFirebaseConfigured } from '../firebase';
import { EditorUser } from '../types/narrative';

interface AuthContextType {
  user: EditorUser | null;
  firebaseUser: FirebaseUser | null;
  loading: boolean;
  isFirebaseConfigured: boolean;
  isDemoMode: boolean;
  signInWithGoogle: () => Promise<void>;
  signInWithEmail: (email: string, pass: string) => Promise<void>;
  signOut: () => Promise<void>;
  setDemoEditor: (role: 'admin' | 'editor') => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<EditorUser | null>(null);
  const [firebaseUser, setFirebaseUser] = useState<FirebaseUser | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [isDemoMode, setIsDemoMode] = useState<boolean>(!isFirebaseConfigured);

  useEffect(() => {
    if (!isFirebaseConfigured) {
      // In unconfigured environment, provide a local mock demo mode
      setLoading(false);
      return;
    }

    const unsubscribe = onAuthStateChanged(auth, async (fbUser) => {
      setFirebaseUser(fbUser);
      if (fbUser) {
        try {
          // Check server-side authorized_editors collection
          const editorRef = doc(db, 'authorized_editors', fbUser.uid);
          const editorSnap = await getDoc(editorRef);

          if (editorSnap.exists() && editorSnap.data().isActive === true) {
            const data = editorSnap.data();
            setUser({
              uid: fbUser.uid,
              email: fbUser.email,
              displayName: fbUser.displayName || fbUser.email?.split('@')[0] || 'Editor',
              role: data.role === 'admin' ? 'admin' : 'editor',
              isActive: true
            });
          } else {
            // Logged in with Google / Email, but not on the authorized editors list
            setUser({
              uid: fbUser.uid,
              email: fbUser.email,
              displayName: fbUser.displayName || fbUser.email,
              role: 'unauthorized',
              isActive: false
            });
          }
        } catch (err) {
          console.error('Error verifying editor authorization in Firestore:', err);
          setUser({
            uid: fbUser.uid,
            email: fbUser.email,
            displayName: fbUser.displayName,
            role: 'unauthorized',
            isActive: false
          });
        }
      } else {
        setUser(null);
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const signInWithGoogle = async () => {
    if (!isFirebaseConfigured) {
      setDemoEditor('admin');
      return;
    }
    setLoading(true);
    try {
      await signInWithPopup(auth, googleProvider);
    } catch (err: any) {
      console.error('Sign in error:', err);
      setLoading(false);
      throw err;
    }
  };

  const signInWithEmail = async (email: string, pass: string) => {
    if (!isFirebaseConfigured) {
      setDemoEditor('editor');
      return;
    }
    setLoading(true);
    try {
      await signInWithEmailAndPassword(auth, email, pass);
    } catch (err: any) {
      console.error('Sign in error:', err);
      setLoading(false);
      throw err;
    }
  };

  const signOut = async () => {
    if (isDemoMode && !isFirebaseConfigured) {
      setUser(null);
      return;
    }
    await fbSignOut(auth);
    setUser(null);
  };

  const setDemoEditor = (role: 'admin' | 'editor') => {
    setIsDemoMode(true);
    setUser({
      uid: 'demo_editor_local_uid',
      email: 'authorized.editor@pitchiq.internal',
      displayName: 'Lead Narrative Editor',
      role: role,
      isActive: true
    });
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        firebaseUser,
        loading,
        isFirebaseConfigured,
        isDemoMode,
        signInWithGoogle,
        signInWithEmail,
        signOut,
        setDemoEditor
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
