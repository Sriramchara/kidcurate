'use client';
import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import api from '@/lib/api';
import ThemeToggle from '@/components/ThemeToggle';
import { 
  ArrowLeft, 
  Clock, 
  ShieldCheck, 
  CheckCircle2, 
  BarChart3, 
  Sparkles,
  Search,
  Heart
} from 'lucide-react';

interface ProgressSummary {
  total_videos_watched: number;
  total_watch_time_seconds: number;
  completed_videos: number;
  this_week_seconds: number;
  favorite_subjects: string[];
}

interface HistoryItem {
  youtube_id: string;
  title: string;
  thumbnail_url: string;
  watched_seconds: number;
  duration_seconds: number;
  completed: boolean;
  watched_at?: string;
}

export default function ReportsDashboard() {
  const { id: childId } = useParams<{ id: string }>();
  const router = useRouter();
  const [summary, setSummary] = useState<ProgressSummary | null>(null);
  const [history, setHistory] = useState<HistoryItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [sumData, histData] = await Promise.all([
          api.getChildProgress(childId),
          api.getWatchHistory(childId).catch(() => ({ history: [] })),
        ]);
        setSummary(sumData);
        setHistory(histData.history || []);
      } catch (err) {
        console.error("Failed to load progress", err);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [childId]);

  function formatDuration(secs: number) {
    if (!secs) return "0 min";
    const h = Math.floor(secs / 3600);
    const m = Math.floor((secs % 3600) / 60);
    if (h > 0) return `${h}h ${m}m`;
    return `${m} min`;
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--bg-dark)] text-[var(--text-secondary)]">
        <div className="flex items-center gap-2 font-medium">
          <span className="w-2.5 h-2.5 rounded-full bg-[var(--brand-primary)] animate-ping" />
          <span>Loading learning analytics...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[var(--bg-dark)] text-[var(--text-primary)] transition-colors duration-200">
      {/* Navbar */}
      <nav className="glass-nav px-6 py-4 flex items-center justify-between sticky top-0 z-30 transition-colors">
        <div className="flex items-center gap-3">
          <button 
            onClick={() => router.push(`/children/${childId}`)} 
            className="flex items-center gap-1.5 text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors py-1 px-2.5 rounded-lg border border-[var(--border-subtle)] bg-[var(--surface-card)] cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Child Hub</span>
          </button>
          <span className="text-[var(--text-muted)]">/</span>
          <span className="font-extrabold gradient-text">Watch Time & Safety Reports</span>
        </div>
        <ThemeToggle />
      </nav>

      <div className="max-w-4xl mx-auto px-6 py-10">
        <div className="mb-8">
          <h1 className="text-3xl font-extrabold text-[var(--text-primary)] flex items-center gap-3">
            <BarChart3 className="w-8 h-8 text-[var(--brand-primary)]" />
            <span>Safe Watch Time & Content Reports</span>
          </h1>
          <p className="text-[var(--text-secondary)] text-sm mt-1">
            Track daily screen time, completed wholesome stories, and favorite categories.
          </p>
        </div>

        {summary ? (
          <div className="space-y-6">
            {/* Top Metric Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="glass-card p-6 flex flex-col justify-center items-center text-center shadow-sm">
                <div className="w-14 h-14 rounded-2xl bg-[var(--brand-primary)]/10 border border-[var(--brand-primary)]/20 flex items-center justify-center mb-4 text-[var(--brand-primary)]">
                  <Clock className="w-7 h-7" />
                </div>
                <h3 className="text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1">
                  Total Safe Watch Time
                </h3>
                <div className="text-4xl font-extrabold text-[var(--brand-primary)] font-mono">
                  {formatDuration(summary.total_watch_time_seconds)}
                </div>
                
                {/* Progress bar */}
                <div className="w-full mt-5 bg-[var(--surface-card)] rounded-full h-2.5 overflow-hidden border border-[var(--border-subtle)]">
                  <div
                    className="bg-gradient-to-r from-[var(--brand-primary)] to-[var(--brand-accent)] h-2.5 rounded-full transition-all duration-500"
                    style={{ width: `${Math.min(100, Math.round((summary.this_week_seconds / 3600) * 100))}%` }}
                  />
                </div>
                <p className="text-xs text-[var(--text-muted)] mt-2.5">
                  This week: <strong className="text-[var(--text-primary)]">{formatDuration(summary.this_week_seconds)}</strong>
                </p>
              </div>

              <div className="glass-card p-6 flex flex-col justify-center items-center text-center shadow-sm">
                <div className="w-14 h-14 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center mb-4 text-emerald-500">
                  <ShieldCheck className="w-7 h-7" />
                </div>
                <h3 className="text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1">
                  Stories Completed
                </h3>
                <div className="text-4xl font-extrabold text-emerald-500 font-mono">
                  {summary.total_videos_watched > 0
                    ? `${Math.round((summary.completed_videos / summary.total_videos_watched) * 100)}%`
                    : '0%'}
                </div>
                
                {/* Progress bar */}
                <div className="w-full mt-5 bg-[var(--surface-card)] rounded-full h-2.5 overflow-hidden border border-[var(--border-subtle)]">
                  <div
                    className="bg-gradient-to-r from-emerald-500 to-teal-400 h-2.5 rounded-full transition-all duration-500"
                    style={{
                      width: `${summary.total_videos_watched > 0 ? Math.round((summary.completed_videos / summary.total_videos_watched) * 100) : 0}%`,
                    }}
                  />
                </div>
                <p className="text-xs text-[var(--text-muted)] mt-2.5">
                  <strong className="text-[var(--text-primary)]">{summary.completed_videos}</strong> completed / {summary.total_videos_watched} started
                </p>
              </div>
            </div>

            {/* Favorite Subjects */}
            <div className="glass-card p-6 shadow-sm">
              <h3 className="text-base font-bold text-[var(--text-primary)] mb-3 flex items-center gap-2">
                <Heart className="w-5 h-5 text-rose-500" />
                <span>Favorite Categories & Values</span>
              </h3>
              {summary.favorite_subjects && summary.favorite_subjects.length > 0 ? (
                <div className="flex flex-wrap gap-2.5">
                  {summary.favorite_subjects.map((sub, idx) => (
                    <span 
                      key={idx} 
                      className="px-3.5 py-1.5 rounded-xl text-xs font-semibold bg-[var(--brand-primary)]/10 text-[var(--brand-primary)] border border-[var(--brand-primary)]/20 flex items-center gap-1.5"
                    >
                      <Sparkles className="w-3.5 h-3.5 text-amber-500" />
                      <span>{sub}</span>
                    </span>
                  ))}
                </div>
              ) : (
                <p className="text-[var(--text-muted)] text-xs">
                  As your child watches more videos, top subjects and areas of interest will appear here.
                </p>
              )}
            </div>

            {/* Recent Watch History */}
            <div className="glass-card p-6 shadow-sm">
              <h3 className="text-base font-bold text-[var(--text-primary)] mb-4 flex items-center gap-2">
                <Clock className="w-5 h-5 text-[var(--brand-primary)]" />
                <span>Recent Safe Watch History</span>
              </h3>
              {history.length > 0 ? (
                <div className="divide-y divide-[var(--border-subtle)]">
                  {history.map((item, idx) => (
                    <div key={idx} className="py-3.5 flex items-center gap-4">
                      {item.thumbnail_url && (
                        /* eslint-disable-next-line @next/next/no-img-element */
                        <img
                          src={item.thumbnail_url}
                          alt={item.title}
                          className="w-20 h-13 object-cover rounded-xl flex-shrink-0 bg-black/20 border border-[var(--border-subtle)]"
                        />
                      )}
                      <div className="flex-1 min-w-0">
                        <h4 className="font-semibold text-sm text-[var(--text-primary)] line-clamp-1">{item.title}</h4>
                        <div className="text-xs text-[var(--text-muted)] mt-1 font-medium">
                          Watched {formatDuration(item.watched_seconds)} of {formatDuration(item.duration_seconds)}
                          {item.watched_at && ` · ${new Date(item.watched_at).toLocaleDateString()}`}
                        </div>
                      </div>
                      <span
                        className={`text-xs px-2.5 py-1 rounded-full font-semibold flex-shrink-0 border flex items-center gap-1 ${
                          item.completed 
                            ? 'bg-emerald-500/15 text-emerald-500 border-emerald-500/30' 
                            : 'bg-amber-500/15 text-amber-500 border-amber-500/30'
                        }`}
                      >
                        {item.completed ? (
                          <>
                            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
                            <span>Completed</span>
                          </>
                        ) : (
                          <>
                            <Clock className="w-3.5 h-3.5 text-amber-500" />
                            <span>In Progress</span>
                          </>
                        )}
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-10 text-[var(--text-muted)] text-sm">
                  <p className="mb-3">No videos watched yet.</p>
                  <Link
                    href={`/children/${childId}/search`}
                    className="btn-primary !py-2 !px-4 text-xs"
                  >
                    <Search className="w-3.5 h-3.5" />
                    <span>Add Videos to Playlist</span>
                  </Link>
                </div>
              )}
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}
