'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { 
  ShieldCheck, SkipForward, SkipBack, 
  CheckCircle2, Sparkles, Lock, Star
} from 'lucide-react';
import ThematicAvatar from '@/components/ThematicAvatar';
import ThemeToggle from '@/components/ThemeToggle';
import api from '@/lib/api';
import { playPop, playTactileClick, playCelebrationFanfare } from '@/lib/haptics';
import ConfettiCanvas from '@/components/ConfettiCanvas';
import { globalProgressTracker } from '@/lib/progressTracker';

interface Child {
  id: string;
  name: string;
  age: number;
  avatar: string;
  pin: string;
}

interface VideoInPlaylist {
  item_id: string;
  position: number;
  youtube_id: string;
  title: string;
  channel_name: string;
  thumbnail_url: string;
  duration_seconds: number;
}

interface Playlist {
  id: string;
  title: string;
  description?: string;
  items: VideoInPlaylist[];
  total_duration_seconds: number;
}

export default function ChildWatchPage() {
  const router = useRouter();
  const [children, setChildren] = useState<Child[]>([]);
  const [selectedChild, setSelectedChild] = useState<Child | null>(null);
  const [pinInput, setPinInput] = useState('');
  const [pinError, setPinError] = useState(false);
  const [showPinModal, setShowPinModal] = useState<Child | null>(null);

  const [playlist, setPlaylist] = useState<Playlist | null>(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [isCompleted, setIsCompleted] = useState(false);

  // Exit Verification
  const [showExitPinModal, setShowExitPinModal] = useState(false);
  const [exitPinInput, setExitPinInput] = useState('');
  const [exitPinError, setExitPinError] = useState(false);
  const [isVerifyingPin, setIsVerifyingPin] = useState(false);
  const [isVerifyingExit, setIsVerifyingExit] = useState(false);

  useEffect(() => {
    loadChildren();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadChildren() {
    setIsLoading(true);
    try {
      const data = await api.listChildren();
      setChildren(data);
    } catch {
      // If unauthenticated, redirect to parent login
      router.push('/');
    } finally {
      setIsLoading(false);
    }
  }

  async function handleSelectChild(child: Child) {
    playPop();
    setShowPinModal(child);
    setPinInput('');
    setPinError(false);
  }

  async function handlePinSubmit(e?: React.FormEvent) {
    if (e) e.preventDefault();
    if (!showPinModal) return;
    setIsVerifyingPin(true);
    setPinError(false);
    try {
      // Backend validates child PIN securely with rate-limiting
      await api.verifyChildPin(showPinModal.id, pinInput);
      const child = showPinModal;
      setShowPinModal(null);
      unlockChild(child);
    } catch {
      setPinError(true);
      setPinInput('');
    } finally {
      setIsVerifyingPin(false);
    }
  }

  async function unlockChild(child: Child) {
    setSelectedChild(child);
    setIsLoading(true);
    setIsCompleted(false);
    try {
      const res = await api.getTodaysPlaylist(child.id);
      if (res && res.playlist) {
        setPlaylist(res.playlist);
        setCurrentIndex(0);
      } else {
        setPlaylist(null);
      }
    } catch {
      setPlaylist(null);
    } finally {
      setIsLoading(false);
    }
  }

  const iframeRef = useRef<HTMLIFrameElement | null>(null);
  const actualWatchedSecondsRef = useRef<number>(0);
  const playbackPositionRef = useRef<number>(0);
  const isPlayingRef = useRef<boolean>(false);
  const activeSessionKeyRef = useRef<string>('');
  const activeVideoRef = useRef<VideoInPlaylist | null>(null);

  const currentVideo = playlist?.items?.[currentIndex];
  activeVideoRef.current = currentVideo || null;

  const flushProgressForVideo = useCallback((video: VideoInPlaylist | null, watchedSecs: number, isNaturalEnd: boolean) => {
    if (!selectedChild || !video) return;

    const duration = video.duration_seconds || 120;
    const evalResult = globalProgressTracker.evaluateProgress(
      selectedChild.id,
      video.youtube_id,
      watchedSecs,
      duration,
      isNaturalEnd
    );

    if (evalResult.shouldReport) {
      api.reportWatchProgress({
        child_id: selectedChild.id,
        youtube_id: video.youtube_id,
        playlist_item_id: video.item_id,
        watched_seconds: evalResult.sanitizedSeconds,
        completed: evalResult.completed,
      }).catch((err) => {
        console.warn('Watch progress report network error:', err);
      });
    }
  }, [selectedChild]);

  const flushCurrentProgress = useCallback((isNaturalEnd: boolean = false) => {
    flushProgressForVideo(activeVideoRef.current, actualWatchedSecondsRef.current, isNaturalEnd);
  }, [flushProgressForVideo]);

  // Trusted YouTube origin whitelist
  const TRUSTED_YOUTUBE_ORIGINS = useRef(new Set([
    'https://www.youtube.com',
    'https://www.youtube-nocookie.com',
  ]));

  // Track playback time via heartbeat interval & validated YouTube API postMessage listener
  useEffect(() => {
    if (!currentVideo) return;

    // Unique session instance key to prevent late events leaking across video transitions
    const sessionKey = `${currentVideo.youtube_id}_${Date.now()}`;
    activeSessionKeyRef.current = sessionKey;
    actualWatchedSecondsRef.current = 0;
    playbackPositionRef.current = 0;
    isPlayingRef.current = false;

    // Heartbeat ticker: increments actualWatchedSeconds ONLY when video is actively PLAYING and tab is visible
    const ticker = setInterval(() => {
      if (
        isPlayingRef.current &&
        typeof document !== 'undefined' &&
        document.visibilityState === 'visible' &&
        !document.hidden
      ) {
        actualWatchedSecondsRef.current += 1;
      }
    }, 1000);

    // Hardened YouTube HTML5 Player postMessage listener
    const handleMessage = (event: MessageEvent) => {
      // 1. Strict Origin Validation
      if (!TRUSTED_YOUTUBE_ORIGINS.current.has(event.origin)) {
        return;
      }

      // 2. Strict Player Instance Matching (WindowProxy source must equal active iframe contentWindow)
      if (!iframeRef.current || event.source !== iframeRef.current.contentWindow) {
        return;
      }

      // 3. Discard late events belonging to a previous video instance
      if (activeSessionKeyRef.current !== sessionKey) {
        return;
      }

      try {
        const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
        if (!data || typeof data !== 'object') return;

        // infoDelivery event: records playhead position for scrubbing info without inflating watched duration
        if (data.event === 'infoDelivery' && data.info && typeof data.info.currentTime === 'number') {
          playbackPositionRef.current = Math.floor(data.info.currentTime);
        }

        // onStateChange event:
        // 1 = PLAYING, 2 = PAUSED, 3 = BUFFERING, 0 = ENDED
        if (data.event === 'onStateChange') {
          if (data.info === 1) {
            isPlayingRef.current = true;
          } else if (data.info === 2 || data.info === 3) {
            isPlayingRef.current = false;
          } else if (data.info === 0) {
            isPlayingRef.current = false;
            handleVideoEnded();
          }
        }
      } catch {}
    };

    window.addEventListener('message', handleMessage);

    const snapshotVideo = currentVideo;
    return () => {
      // Invalidate instance token immediately so no in-flight messages can touch the next video
      activeSessionKeyRef.current = '';
      isPlayingRef.current = false;
      clearInterval(ticker);
      window.removeEventListener('message', handleMessage);

      // Flush progress for departing video snapshot
      const snapshotWatched = actualWatchedSecondsRef.current;
      if (snapshotVideo && snapshotWatched >= 3) {
        flushProgressForVideo(snapshotVideo, snapshotWatched, false);
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentIndex, currentVideo?.youtube_id, flushProgressForVideo]);

  // Flush progress before unload/page exit
  useEffect(() => {
    const handleBeforeUnload = () => {
      flushCurrentProgress(false);
    };
    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [flushCurrentProgress]);

  function handleVideoEnded() {
    flushCurrentProgress(true);
    if (!playlist) return;
    if (currentIndex + 1 < playlist.items.length) {
      setCurrentIndex(prev => prev + 1);
    } else {
      setIsCompleted(true);
      playCelebrationFanfare();
    }
  }

  function handlePrevious() {
    flushCurrentProgress(false);
    setCurrentIndex(prev => Math.max(0, prev - 1));
  }

  function handleNext() {
    flushCurrentProgress(false);
    if (!playlist) return;
    if (currentIndex + 1 < playlist.items.length) {
      setCurrentIndex(prev => prev + 1);
    } else {
      handleVideoEnded();
    }
  }

  function handleSelectQueueItem(idx: number) {
    if (idx === currentIndex) return;
    flushCurrentProgress(false);
    setCurrentIndex(idx);
  }

  async function handleExitSubmit(e?: React.FormEvent) {
    if (e) e.preventDefault();
    flushCurrentProgress(false);
    setIsVerifyingExit(true);
    setExitPinError(false);

    const savedUser = typeof window !== 'undefined' ? localStorage.getItem('kc_user') : null;
    const parentEmail = savedUser ? JSON.parse(savedUser).email : '';

    if (parentEmail) {
      try {
        await api.verifyParentPassword(parentEmail, exitPinInput);
        setShowExitPinModal(false);
        router.push('/dashboard');
        return;
      } catch {}
    } else {
      setShowExitPinModal(false);
      router.push('/dashboard');
      return;
    }

    setExitPinError(true);
    setExitPinInput('');
    setIsVerifyingExit(false);
  }


  // 1. Loading State
  if (isLoading && !selectedChild) {
    return (
      <div className="min-h-screen bg-[var(--bg-dark)] flex items-center justify-center p-6">
        <div className="text-center space-y-4">
          <div className="w-12 h-12 rounded-full border-4 border-amber-500 border-t-transparent animate-spin mx-auto" />
          <p className="text-sm text-[var(--text-secondary)] font-medium">Loading Safe Sanctuary...</p>
        </div>
      </div>
    );
  }

  // 2. Profile Selection Screen (Computers, Macs, Tablets, iPads)
  if (!selectedChild) {
    return (
      <div className="min-h-screen bg-[var(--bg-dark)] text-[var(--text-primary)] flex flex-col justify-between p-6">
        <header className="flex items-center justify-between max-w-5xl mx-auto w-full pt-4">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-500">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <span className="font-extrabold text-base gradient-text">KidCurate Sanctuary</span>
              <span className="text-[11px] block text-emerald-500 font-semibold">100% Vulgarity-Free</span>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <Link 
              href="/dashboard"
              className="text-xs text-[var(--text-secondary)] hover:text-[var(--text-primary)] px-3 py-1.5 rounded-lg border border-[var(--border-subtle)] bg-[var(--surface-card)] transition-colors flex items-center gap-1.5"
            >
              <Lock className="w-3.5 h-3.5 text-amber-500" />
              <span>Parent Dashboard</span>
            </Link>
            <ThemeToggle />
          </div>
        </header>

        <main className="max-w-3xl mx-auto w-full text-center py-12">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-500 text-xs font-bold mb-6">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Pure Entertainment • Zero Ads • No Brainrot</span>
          </div>

          <h1 className="text-3xl sm:text-5xl font-black text-[var(--text-primary)] tracking-tight mb-4">
            Who's ready to watch?
          </h1>
          <p className="text-sm sm:text-base text-[var(--text-secondary)] max-w-md mx-auto mb-10">
            Pick your profile to jump into today's parent-approved wholesome stories and creative fun!
          </p>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-6 max-w-2xl mx-auto">
            {children.map(child => (
              <button
                key={child.id}
                onClick={() => handleSelectChild(child)}
                className="group flex flex-col items-center p-6 rounded-3xl glass-card hover:border-amber-400 hover:shadow-xl hover:-translate-y-1 transition-all cursor-pointer"
              >
                <div className="relative mb-4">
                  <ThematicAvatar name={child.name} avatar={child.avatar} size="lg" />
                  <div className="absolute -bottom-1 -right-1 w-6 h-6 rounded-full bg-emerald-500 border-2 border-[var(--surface-card)] flex items-center justify-center text-white">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                  </div>
                </div>
                <h3 className="font-extrabold text-base text-[var(--text-primary)] group-hover:text-amber-500 transition-colors">
                  {child.name}
                </h3>
                <span className="text-xs text-[var(--text-muted)] mt-1">Age {child.age}</span>
              </button>
            ))}
          </div>
        </main>

        <footer className="text-center text-xs text-[var(--text-muted)] pb-4">
          Safe Kids Entertainment • Powered by KidCurate
        </footer>

        {/* Child PIN Modal */}
        {showPinModal && (
          <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-md flex items-center justify-center p-4">
            <div className="glass-panel p-8 max-w-sm w-full text-center shadow-2xl">
              <ThematicAvatar name={showPinModal.name} avatar={showPinModal.avatar} size="lg" className="mx-auto mb-4" />
              <h2 className="text-xl font-extrabold text-[var(--text-primary)] mb-1">
                Enter PIN for {showPinModal.name}
              </h2>
              <p className="text-xs text-[var(--text-secondary)] mb-6">4-digit security code</p>

              <form onSubmit={handlePinSubmit} className="space-y-4">
                <input
                  type="password"
                  maxLength={4}
                  autoFocus
                  pattern="[0-9]*"
                  inputMode="numeric"
                  value={pinInput}
                  onChange={e => { playTactileClick(); setPinInput(e.target.value); setPinError(false); }}
                  placeholder="••••"
                  className="w-36 text-center text-3xl tracking-widest font-mono py-3 rounded-2xl border-2 border-[var(--border-subtle)] focus:border-amber-400 bg-[var(--bg-dark)] outline-none mx-auto block"
                />
                {pinError && <p className="text-xs text-rose-500 font-semibold">Incorrect PIN. Please try again.</p>}

                <div className="flex gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setShowPinModal(null)}
                    className="flex-1 btn-secondary text-xs"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={isVerifyingPin || pinInput.length < 4}
                    className="flex-1 btn-primary text-xs !bg-amber-500 hover:!bg-amber-600 shadow-md font-bold disabled:opacity-50"
                  >
                    {isVerifyingPin ? 'Verifying...' : 'Unlock & Play'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    );
  }

  // 3. Child Sanctuary Video Player (Theater Mode for iPad, Mac, PC, Tablet, iPhone)
  return (
    <div className="min-h-screen bg-[var(--bg-dark)] text-[var(--text-primary)] flex flex-col justify-between transition-colors duration-200">
      {/* Top Sanctuary Bar */}
      <nav className="px-6 py-4 border-b border-[var(--border-subtle)] flex items-center justify-between bg-[var(--nav-bg)] backdrop-blur-md sticky top-0 z-40 transition-colors">
        <div className="flex items-center gap-3">
          <ThematicAvatar name={selectedChild.name} avatar={selectedChild.avatar} size="sm" />
          <div>
            <span className="font-extrabold text-sm text-[var(--text-primary)] flex items-center gap-1.5">
              <span>{selectedChild.name}'s Sanctuary</span>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-500 border border-emerald-500/30">
                100% Safe
              </span>
            </span>
            <p className="text-[11px] text-[var(--text-secondary)]">
              {playlist ? `${playlist.title} • Story ${currentIndex + 1} of ${playlist.items.length}` : 'Today\'s Watch Time'}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowExitPinModal(true)}
            className="flex items-center gap-1.5 text-xs text-[var(--text-secondary)] hover:text-[var(--text-primary)] bg-[var(--surface-card)] hover:bg-[var(--surface-card-hover)] border border-[var(--border-subtle)] px-3 py-1.5 rounded-xl transition-all cursor-pointer font-medium"
          >
            <Lock className="w-3.5 h-3.5 text-amber-500" />
            <span>Parent Exit</span>
          </button>
        </div>
      </nav>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl mx-auto w-full p-4 sm:p-6 lg:p-8 flex flex-col justify-center">
        {!playlist || playlist.items.length === 0 ? (
          /* Empty Playlist View */
          <div className="max-w-md mx-auto text-center py-16 px-6 rounded-3xl glass-card">
            <ThematicAvatar name={selectedChild.name} avatar={selectedChild.avatar} size="xl" className="mx-auto mb-4" />
            <h2 className="text-2xl font-black mb-2 text-[var(--text-primary)]">Welcome, {selectedChild.name}!</h2>
            <p className="text-sm text-[var(--text-secondary)] mb-6">
              No playlist chosen for today yet. Parents can choose or curate today's wholesome stories anytime!
            </p>
            <button
              onClick={() => setShowExitPinModal(true)}
              className="btn-primary !bg-amber-500 hover:!bg-amber-600 text-sm font-bold shadow-lg inline-flex items-center gap-2"
            >
              <Lock className="w-4 h-4" />
              <span>Parent: Choose Stories Now</span>
            </button>
          </div>
        ) : isCompleted ? (
          /* Finish Celebration Banner with Confetti & Twinkling Stars */
          <>
            <ConfettiCanvas duration={7000} />
            <div className="max-w-lg mx-auto text-center py-16 px-8 rounded-3xl glass-panel relative z-10 animate-fade-in">
              <div className="w-20 h-20 rounded-full bg-amber-500/20 border-2 border-amber-400 flex items-center justify-center mx-auto mb-6 text-amber-400 animate-bounce">
                <Star className="w-10 h-10 fill-amber-400" />
              </div>
              <h2 className="text-3xl sm:text-4xl font-black mb-3 text-[var(--text-primary)]">All Done for Today!</h2>
              <p className="text-sm text-[var(--text-secondary)] max-w-md mx-auto mb-8">
                Great job! You enjoyed pure, wholesome stories with kindness and heart. Come back tomorrow for more adventures!
              </p>
              <div className="flex flex-col sm:flex-row gap-3 justify-center">
                <button
                  onClick={() => { playTactileClick(); setCurrentIndex(0); setIsCompleted(false); }}
                  className="btn-secondary text-xs font-bold py-3 px-5 rounded-2xl"
                >
                  Replay Stories
                </button>
                <button
                  onClick={() => { playTactileClick(); setShowExitPinModal(true); }}
                  className="btn-primary text-xs !bg-emerald-500 hover:!bg-emerald-600 font-bold py-3 px-5 rounded-2xl shadow-lg"
                >
                  Finish & Go Home
                </button>
              </div>
            </div>
          </>
        ) : (
          /* Active Video Player with Side/Bottom Queue */
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
            {/* 16:9 Widescreen Video Player */}
            <div className="lg:col-span-2 space-y-4">
              <div className="relative aspect-video rounded-3xl overflow-hidden bg-black border-2 border-[var(--border-subtle)] shadow-2xl">
                {currentVideo && (
                  <iframe
                    ref={iframeRef}
                    key={currentVideo.youtube_id}
                    src={`https://www.youtube-nocookie.com/embed/${currentVideo.youtube_id}?autoplay=1&rel=0&modestbranding=1&controls=1&playsinline=1&enablejsapi=1&origin=${typeof window !== 'undefined' ? window.location.origin : ''}`}
                    title={currentVideo.title}
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    sandbox="allow-scripts allow-same-origin allow-presentation"
                    allowFullScreen
                    className="w-full h-full border-0"
                  />
                )}
              </div>

              {/* Title & Controls Bar */}
              <div className="p-4 rounded-2xl glass-card flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h1 className="text-base sm:text-lg font-bold text-[var(--text-primary)] line-clamp-1">
                    {currentVideo?.title}
                  </h1>
                  <p className="text-xs text-[var(--text-secondary)] mt-0.5">{currentVideo?.channel_name}</p>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  {currentIndex > 0 && (
                    <button
                      onClick={handlePrevious}
                      className="p-2.5 rounded-xl btn-secondary transition-colors cursor-pointer"
                      title="Previous Story"
                    >
                      <SkipBack className="w-4 h-4" />
                    </button>
                  )}
                  {currentIndex + 1 < playlist.items.length ? (
                    <button
                      onClick={handleNext}
                      className="btn-primary !py-2 !px-4 text-xs !bg-amber-500 hover:!bg-amber-600 font-bold inline-flex items-center gap-1.5 shadow-md"
                    >
                      <span>Play Next Story</span>
                      <SkipForward className="w-3.5 h-3.5" />
                    </button>
                  ) : (
                    <button
                      onClick={handleVideoEnded}
                      className="btn-primary !py-2 !px-4 text-xs !bg-emerald-500 hover:!bg-emerald-600 font-bold inline-flex items-center gap-1.5 shadow-md"
                    >
                      <span>Finish Today's Stories</span>
                      <CheckCircle2 className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
              </div>
            </div>

            {/* Side Story Queue */}
            <div className="space-y-4">
              <div className="p-4 rounded-2xl glass-card">
                <div className="flex items-center justify-between mb-3">
                  <h3 className="text-xs font-extrabold uppercase tracking-wider text-amber-500 flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5" />
                    <span>Today's Story Queue</span>
                  </h3>
                  <span className="text-xs text-[var(--text-muted)] font-mono">
                    {playlist.items.length} Stories
                  </span>
                </div>

                <div className="space-y-2.5 max-h-[460px] overflow-y-auto pr-1">
                  {playlist.items.map((item, idx) => {
                    const isCurrent = idx === currentIndex;
                    return (
                      <button
                        key={item.item_id || item.youtube_id}
                        onClick={() => handleSelectQueueItem(idx)}
                        className={`w-full text-left p-2.5 rounded-xl border transition-all flex items-center gap-3 cursor-pointer group ${
                          isCurrent
                            ? 'bg-amber-500/15 border-amber-400 shadow-md'
                            : 'bg-[var(--surface-card)] border-[var(--border-subtle)] hover:bg-[var(--surface-card-hover)]'
                        }`}
                      >
                        <div className="relative w-24 aspect-video rounded-lg overflow-hidden bg-black shrink-0">
                          {/* eslint-disable-next-line @next/next/no-img-element */}
                          <img
                            src={item.thumbnail_url || `https://img.youtube.com/vi/${item.youtube_id}/hqdefault.jpg`}
                            alt={item.title}
                            className="w-full h-full object-cover"
                          />
                          {isCurrent ? (
                            <div className="absolute inset-0 bg-amber-500/40 flex items-center justify-center">
                              <span className="text-[9px] font-black tracking-wider px-1.5 py-0.5 rounded bg-black text-amber-400">
                                PLAYING
                              </span>
                            </div>
                          ) : (
                            <div className="absolute bottom-1 right-1 px-1 rounded bg-black/80 text-[9px] font-mono font-bold text-white">
                              {Math.floor(item.duration_seconds / 60)}:{(item.duration_seconds % 60).toString().padStart(2, '0')}
                            </div>
                          )}
                        </div>

                        <div className="flex-1 min-w-0">
                          <h4 className={`text-xs font-bold line-clamp-2 leading-snug ${isCurrent ? 'text-amber-500' : 'text-[var(--text-primary)]'}`}>
                            {item.title}
                          </h4>
                          <span className="text-[10px] text-[var(--text-muted)] block mt-1 truncate">
                            {item.channel_name}
                          </span>
                        </div>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Safe Sanctuary Assurance Card */}
              <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-emerald-500/20 text-emerald-500 flex items-center justify-center shrink-0">
                  <ShieldCheck className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-emerald-600 dark:text-emerald-400">KidCurate Sanctuary</h4>
                  <p className="text-[11px] text-emerald-700/80 dark:text-emerald-300/80">
                    Filtered from bad language, screamers & adult humor. Zero ads.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Parent Exit Modal */}
      {showExitPinModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="glass-panel p-8 max-w-sm w-full text-center shadow-2xl text-[var(--text-primary)]">
            <div className="w-12 h-12 rounded-2xl bg-amber-500/10 text-amber-500 border border-amber-500/20 flex items-center justify-center mx-auto mb-4">
              <Lock className="w-6 h-6" />
            </div>
            <h2 className="text-xl font-extrabold mb-1">Parent Gate</h2>
            <p className="text-xs text-[var(--text-secondary)] mb-6">
              Enter your parent account password to exit to dashboard
            </p>

            <form onSubmit={handleExitSubmit} className="space-y-4">
              <input
                type="password"
                autoFocus
                value={exitPinInput}
                onChange={e => { playTactileClick(); setExitPinInput(e.target.value); setExitPinError(false); }}
                placeholder="Parent Password"
                className="w-full text-center text-sm py-3 px-4 rounded-2xl border-2 border-[var(--border-subtle)] focus:border-amber-400 bg-[var(--bg-dark)] outline-none"
              />
              {exitPinError && (
                <p className="text-xs text-rose-500 font-semibold">
                  Incorrect parent password. Please try again.
                </p>
              )}

              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowExitPinModal(false);
                    setExitPinInput('');
                    setExitPinError(false);
                  }}
                  className="flex-1 btn-secondary text-xs"
                >
                  Stay in Player
                </button>
                <button
                  type="submit"
                  disabled={isVerifyingExit || !exitPinInput}
                  className="flex-1 btn-primary text-xs !bg-amber-500 hover:!bg-amber-600 shadow-md font-bold disabled:opacity-50"
                >
                  {isVerifyingExit ? 'Checking...' : 'Exit to Dashboard'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

