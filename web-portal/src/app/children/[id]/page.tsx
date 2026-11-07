'use client';
import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import api from '@/lib/api';
import ThemeToggle from '@/components/ThemeToggle';
import ThematicAvatar from '@/components/ThematicAvatar';
import { 
  ArrowLeft, 
  Sparkles, 
  BarChart3, 
  Search, 
  Plus, 
  Play, 
  Trash2, 
  ChevronUp, 
  ChevronDown, 
  Calendar, 
  ListVideo, 
  Clock, 
  BookOpen,
  GripVertical,
  CheckCircle2,
  Wand2,
  X
} from 'lucide-react';

interface VideoItem {
  item_id: string;
  position: number;
  youtube_id: string;
  title: string;
  channel_name: string;
  thumbnail_url: string;
  duration_seconds?: number;
  overall_score?: number;
}

interface Playlist {
  id: string;
  title: string;
  description?: string;
  scheduled_date?: string;
  is_active: boolean;
  total_duration_seconds: number;
  video_count: number;
  items: VideoItem[];
}

interface Child {
  id: string;
  name: string;
  age: number;
  avatar: string;
}

export default function ChildDetailPage() {
  const { id: childId } = useParams<{ id: string }>();
  const router = useRouter();
  const [child, setChild] = useState<Child | null>(null);
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [selectedPlaylist, setSelectedPlaylist] = useState<Playlist | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showCurriculumModal, setShowCurriculumModal] = useState(false);
  const [previewVideo, setPreviewVideo] = useState<{ id: string; title: string } | null>(null);
  const [loading, setLoading] = useState(true);
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null);
  const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);

  function handleDragStart(idx: number) {
    setDraggedIndex(idx);
  }

  function handleDragOver(e: React.DragEvent, idx: number) {
    e.preventDefault();
    setDragOverIndex(idx);
  }

  async function handleDrop(targetIdx: number) {
    if (draggedIndex === null || draggedIndex === targetIdx || !selectedPlaylist) return;
    const items = [...selectedPlaylist.items];
    const [moved] = items.splice(draggedIndex, 1);
    items.splice(targetIdx, 0, moved);

    const updated = { ...selectedPlaylist, items };
    setSelectedPlaylist(updated);
    setPlaylists(prev => prev.map(p => p.id === selectedPlaylist.id ? updated : p));
    setDraggedIndex(null);
    setDragOverIndex(null);

    try {
      const orderedIds = items.map(it => it.item_id);
      await api.reorderPlaylist(selectedPlaylist.id, orderedIds);
    } catch (err: any) {
      console.error('Failed to reorder playlist:', err);
    }
  }

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [childId]);

  async function loadData() {
    try {
      const [kids, lists] = await Promise.all([
        api.listChildren(),
        api.listPlaylists(childId),
      ]);
      const c = kids.find((k: Child) => k.id === childId);
      setChild(c || null);
      setPlaylists(lists);
      if (lists.length > 0 && !selectedPlaylist) {
        setSelectedPlaylist(lists[0]);
      }
    } catch {
      router.push('/dashboard');
    } finally {
      setLoading(false);
    }
  }

  async function removeVideo(playlistId: string, itemId: string) {
    try {
      const updated = await api.removeVideoFromPlaylist(playlistId, itemId);
      setPlaylists(prev => prev.map(p => p.id === playlistId ? updated : p));
      if (selectedPlaylist?.id === playlistId) setSelectedPlaylist(updated);
    } catch (err: any) {
      alert(err.message);
    }
  }

  async function moveVideo(playlistId: string, items: VideoItem[], fromIdx: number, toIdx: number) {
    const reordered = [...items];
    const [moved] = reordered.splice(fromIdx, 1);
    reordered.splice(toIdx, 0, moved);
    const orderedIds = reordered.map(i => i.item_id);
    try {
      const updated = await api.reorderPlaylist(playlistId, orderedIds);
      setPlaylists(prev => prev.map(p => p.id === playlistId ? updated : p));
      if (selectedPlaylist?.id === playlistId) setSelectedPlaylist(updated);
    } catch {}
  }

  function formatDuration(secs: number) {
    const m = Math.floor(secs / 60);
    return `${m} min`;
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--bg-dark)] text-[var(--text-secondary)]">
        <div className="flex items-center gap-2 font-medium">
          <span className="w-2.5 h-2.5 rounded-full bg-[var(--brand-primary)] animate-ping" />
          <span>Loading Child Hub...</span>
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
            onClick={() => router.push('/dashboard')} 
            className="flex items-center gap-1.5 text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors py-1 px-2.5 rounded-lg border border-[var(--border-subtle)] bg-[var(--surface-card)] cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Dashboard</span>
          </button>
          <span className="text-[var(--text-muted)]">/</span>
          <span className="font-extrabold gradient-text">Child Hub</span>
        </div>
        <ThemeToggle />
      </nav>

      <div className="max-w-6xl mx-auto px-6 py-8">
        {/* Child header */}
        {child && (
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 mb-8 pb-6 border-b border-[var(--border-subtle)]">
            <div className="flex items-center gap-4">
              <ThematicAvatar name={child.name} avatar={child.avatar} size="xl" />
              <div>
                <h1 className="text-2xl font-extrabold text-[var(--text-primary)]">{child.name}'s Adventure Hub</h1>
                <p className="text-[var(--text-secondary)] text-xs mt-1 font-medium">
                  Age {child.age} · {playlists.length} curated {playlists.length === 1 ? 'playlist' : 'playlists'}
                </p>
              </div>
            </div>
            <div className="flex gap-2.5 flex-wrap">
              <button 
                onClick={() => setShowCurriculumModal(true)}
                className="btn-primary !py-2 !px-3.5 text-xs shadow-sm"
              >
                <Sparkles className="w-3.5 h-3.5 text-amber-300" />
                <span>AI Story & Quest Planner</span>
              </button>
              <Link 
                href={`/children/${childId}/reports`}
                className="btn-secondary !py-2 !px-3.5 text-xs"
              >
                <BarChart3 className="w-3.5 h-3.5 text-[var(--brand-primary)]" />
                <span>Reports</span>
              </Link>
              <Link 
                href={`/children/${childId}/search`}
                className="btn-primary !py-2 !px-3.5 text-xs shadow-sm"
              >
                <Search className="w-3.5 h-3.5" />
                <span>Add Videos</span>
              </Link>
              <button 
                onClick={() => setShowCreateModal(true)}
                className="btn-secondary !py-2 !px-3.5 text-xs"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>New Playlist</span>
              </button>
            </div>
          </div>
        )}


        <div className="flex flex-col md:flex-row gap-6">
          {/* Left: playlist list */}
          <div className="w-full md:w-72 flex-shrink-0 space-y-3">
            <div className="flex items-center gap-2 text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider px-1">
              <ListVideo className="w-4 h-4 text-[var(--brand-primary)]" />
              <span>Playlists</span>
            </div>
            {playlists.length === 0 ? (
              <div className="glass-card p-6 text-center text-xs text-[var(--text-muted)]">
                <p>No playlists created yet.</p>
                <button 
                  onClick={() => setShowCreateModal(true)} 
                  className="text-[var(--brand-primary)] hover:underline mt-2 inline-flex items-center gap-1 font-medium cursor-pointer"
                >
                  <Plus className="w-3 h-3" />
                  <span>Create First Playlist</span>
                </button>
              </div>
            ) : (
              playlists.map(pl => {
                const isSelected = selectedPlaylist?.id === pl.id;
                return (
                  <button 
                    key={pl.id}
                    onClick={() => setSelectedPlaylist(pl)}
                    className={`w-full text-left p-3.5 rounded-xl transition-all border cursor-pointer ${
                      isSelected 
                        ? 'bg-[var(--brand-primary)]/15 border-[var(--brand-primary)] shadow-sm' 
                        : 'bg-[var(--surface-card)] border-[var(--border-subtle)] hover:border-[var(--brand-primary)]/50'
                    }`}
                  >
                    <div className="font-semibold text-sm text-[var(--text-primary)] line-clamp-1">{pl.title}</div>
                    <div className="flex items-center gap-2 text-[var(--text-secondary)] text-xs mt-1 font-medium">
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3 h-3 text-amber-500" />
                        <span>{pl.scheduled_date ? pl.scheduled_date : 'Template'}</span>
                      </span>
                      <span>·</span>
                      <span>{pl.video_count} videos</span>
                    </div>
                  </button>
                );
              })
            )}
          </div>

          {/* Right: selected playlist detail */}
          <div className="flex-1 min-w-0">
            {!selectedPlaylist ? (
              <div className="glass-card p-16 text-center text-[var(--text-secondary)]">
                <ListVideo className="w-12 h-12 text-[var(--brand-primary)] mx-auto mb-3 opacity-60" />
                <p className="font-semibold text-[var(--text-primary)]">Select a playlist on the left</p>
                <p className="text-xs text-[var(--text-muted)] mt-1">View lessons, drag to reorder, or preview videos</p>
              </div>
            ) : (
              <div className="glass-card p-6">
                <div className="flex items-center justify-between mb-6 pb-4 border-b border-[var(--border-subtle)]">
                  <div>
                    <h2 className="text-xl font-bold text-[var(--text-primary)]">{selectedPlaylist.title}</h2>
                    {selectedPlaylist.scheduled_date ? (
                      <p className="text-xs text-amber-500 font-medium flex items-center gap-1 mt-1">
                        <Calendar className="w-3.5 h-3.5" />
                        <span>Scheduled: {selectedPlaylist.scheduled_date}</span>
                      </p>
                    ) : (
                      <p className="text-xs text-[var(--text-muted)] mt-1">Reusable Template Playlist</p>
                    )}
                  </div>
                  <div className="flex items-center gap-2 text-xs font-semibold px-3 py-1.5 rounded-xl bg-[var(--surface-card)] border border-[var(--border-subtle)]">
                    <Clock className="w-3.5 h-3.5 text-[var(--brand-primary)]" />
                    <span className="text-[var(--text-primary)]">{selectedPlaylist.video_count} videos</span>
                    <span className="text-[var(--text-muted)]">·</span>
                    <span className="text-amber-500 font-mono">{formatDuration(selectedPlaylist.total_duration_seconds)}</span>
                  </div>
                </div>

                {selectedPlaylist.items.length === 0 ? (
                  <div className="text-center py-14 text-[var(--text-muted)]">
                    <div className="w-12 h-12 rounded-xl bg-[var(--brand-primary)]/10 border border-[var(--brand-primary)]/20 flex items-center justify-center mx-auto mb-3 text-[var(--brand-primary)]">
                      <BookOpen className="w-6 h-6" />
                    </div>
                    <p className="text-sm font-semibold text-[var(--text-primary)] mb-1">No videos in this playlist</p>
                    <p className="text-xs text-[var(--text-secondary)] mb-4">Discover wholesome videos or generate an AI story quest</p>
                    <Link 
                      href={`/children/${childId}/search`}
                      className="btn-primary !py-2 !px-4 text-xs inline-flex items-center gap-1.5"
                    >
                      <Search className="w-3.5 h-3.5" />
                      <span>Search & Add Videos</span>
                    </Link>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {selectedPlaylist.items.map((item, idx) => (
                      <div 
                        key={item.item_id}
                        draggable
                        onDragStart={() => handleDragStart(idx)}
                        onDragOver={(e) => handleDragOver(e, idx)}
                        onDrop={() => handleDrop(idx)}
                        className={`flex items-center gap-3.5 p-3.5 rounded-xl bg-[var(--surface-card)] border transition-all cursor-grab active:cursor-grabbing ${
                          dragOverIndex === idx
                            ? 'border-[var(--brand-primary)] ring-2 ring-[var(--brand-primary)]/20 scale-[1.01]'
                            : 'border-[var(--border-subtle)] hover:border-[var(--brand-primary)]/50'
                        }`}
                      >
                        {/* Drag Handle & Position */}
                        <div className="flex items-center gap-1.5 flex-shrink-0">
                          <GripVertical className="w-4 h-4 text-[var(--text-muted)] hover:text-[var(--text-primary)] cursor-grab" />
                          <div className="text-[var(--text-muted)] font-mono font-bold text-xs w-4 text-center">{idx + 1}</div>
                        </div>

                        {/* Move buttons */}
                        <div className="flex flex-col gap-0.5 flex-shrink-0">
                          <button
                            onClick={() => moveVideo(selectedPlaylist.id, selectedPlaylist.items, idx, idx - 1)}
                            disabled={idx === 0}
                            className="p-1 rounded text-[var(--text-secondary)] hover:text-[var(--text-primary)] disabled:opacity-20 hover:bg-[var(--border-subtle)] transition-colors cursor-pointer"
                          >
                            <ChevronUp className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => moveVideo(selectedPlaylist.id, selectedPlaylist.items, idx, idx + 1)}
                            disabled={idx === selectedPlaylist.items.length - 1}
                            className="p-1 rounded text-[var(--text-secondary)] hover:text-[var(--text-primary)] disabled:opacity-20 hover:bg-[var(--border-subtle)] transition-colors cursor-pointer"
                          >
                            <ChevronDown className="w-3.5 h-3.5" />
                          </button>
                        </div>

                        {/* Thumbnail */}
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img 
                          src={item.thumbnail_url} 
                          alt={item.title}
                          className="rounded-lg object-cover flex-shrink-0 w-24 h-15 bg-black/20" 
                        />

                        {/* Info */}
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-semibold text-[var(--text-primary)] line-clamp-1">{item.title}</p>
                          <p className="text-xs text-[var(--text-secondary)] mt-0.5 font-medium">{item.channel_name}</p>
                          {item.duration_seconds && (
                            <p className="text-xs text-amber-500 font-mono mt-0.5">
                              {Math.floor(item.duration_seconds / 60)}:{(item.duration_seconds % 60).toString().padStart(2, '0')}
                            </p>
                          )}
                        </div>

                        {/* Score */}
                        {item.overall_score && (
                          <div className="text-center flex-shrink-0">
                            <div 
                              className="text-lg font-extrabold"
                              style={{ color: item.overall_score >= 80 ? 'var(--brand-success)' : item.overall_score >= 60 ? 'var(--brand-accent)' : '#EF4444' }}
                            >
                              {item.overall_score}
                            </div>
                            <div className="text-xs text-[var(--text-muted)]">score</div>
                          </div>
                        )}

                        {/* Actions */}
                        <div className="flex items-center gap-2 flex-shrink-0">
                          <button
                            type="button"
                            onClick={() => setPreviewVideo({ id: item.youtube_id, title: item.title })}
                            className="btn-secondary !py-1 !px-2.5 text-xs inline-flex items-center gap-1 text-[var(--brand-primary)]"
                            title="Preview Video"
                          >
                            <Play className="w-3 h-3 text-[var(--brand-primary)]" />
                            <span>Play</span>
                          </button>
                          <button
                            onClick={() => removeVideo(selectedPlaylist.id, item.item_id)}
                            className="p-1.5 rounded-lg text-[var(--text-muted)] hover:text-rose-500 hover:bg-rose-500/10 transition-colors cursor-pointer"
                            title="Remove from playlist"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Video Preview Modal */}
      {previewVideo && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm"
          onClick={() => setPreviewVideo(null)}
        >
          <div
            className="glass-card !bg-[var(--bg-card)] w-full max-w-3xl overflow-hidden rounded-2xl p-5 border border-[var(--border-highlight)] shadow-2xl"
            onClick={e => e.stopPropagation()}
          >
            <div className="flex justify-between items-center mb-3">
              <h3 className="font-bold text-sm text-[var(--text-primary)] line-clamp-1">{previewVideo.title}</h3>
              <button
                onClick={() => setPreviewVideo(null)}
                className="text-[var(--text-muted)] hover:text-[var(--text-primary)] p-1.5 rounded-lg hover:bg-[var(--surface-card)] transition-colors cursor-pointer"
                title="Close Preview"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <div className="relative aspect-video w-full rounded-xl overflow-hidden bg-black">
              <iframe
                src={`https://www.youtube-nocookie.com/embed/${previewVideo.id}?autoplay=1`}
                className="w-full h-full border-0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              />
            </div>
          </div>
        </div>
      )}

      {showCreateModal && (
        <CreatePlaylistModal
          childId={childId}
          onClose={() => setShowCreateModal(false)}
          onCreated={() => { setShowCreateModal(false); loadData(); }}
        />
      )}

      {showCurriculumModal && child && (
        <CurriculumModal
          child={child}
          onClose={() => setShowCurriculumModal(false)}
          onCurriculumApplied={() => { loadData(); }}
        />
      )}
    </div>
  );
}

function CreatePlaylistModal({ childId, onClose, onCreated }: { childId: string; onClose: () => void; onCreated: () => void }) {
  const [title, setTitle] = useState('');
  const [date, setDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await api.createPlaylist({
        child_id: childId,
        title,
        scheduled_date: date || null,
      });
      onCreated();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div 
      className="fixed inset-0 flex items-center justify-center z-50 p-4 bg-black/60 backdrop-blur-sm fade-in-up"
    >
      <div className="glass-card !bg-[var(--bg-card)] p-8 w-full max-w-md border border-[var(--border-highlight)] shadow-2xl">
        <h2 className="text-xl font-bold mb-6 text-[var(--text-primary)]">Create Playlist</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm text-[var(--text-secondary)] mb-1 font-medium">Playlist Title</label>
            <input 
              type="text" 
              value={title} 
              onChange={e => setTitle(e.target.value)} 
              required
              placeholder="e.g. Science & Space Explorers"
              className="input-field"
            />
          </div>
          <div>
            <label className="block text-sm text-[var(--text-secondary)] mb-1 font-medium">Schedule Date (optional)</label>
            <input 
              type="date" 
              value={date} 
              onChange={e => setDate(e.target.value)}
              className="input-field"
            />
          </div>
          {error && <p className="text-rose-500 text-sm font-medium">{error}</p>}
          <div className="flex gap-3 pt-3">
            <button 
              type="button" 
              onClick={onClose}
              className="flex-1 btn-secondary text-sm"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              disabled={loading}
              className="flex-1 btn-primary text-sm"
            >
              {loading ? 'Creating...' : 'Create Playlist'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function CurriculumModal({ child, onClose, onCurriculumApplied }: { child: Child; onClose: () => void; onCurriculumApplied?: () => void }) {
  const [subject, setSubject] = useState('');
  const [goals, setGoals] = useState('');
  const [days, setDays] = useState(3);
  const [loading, setLoading] = useState(false);
  const [applying, setApplying] = useState(false);
  const [appliedSuccess, setAppliedSuccess] = useState(false);
  const [curriculum, setCurriculum] = useState<any | null>(null);
  const [error, setError] = useState('');

  async function handleApplyCurriculum() {
    if (!curriculum) return;
    setApplying(true);
    setError('');
    try {
      await api.applyCurriculum(child.id, curriculum);
      setAppliedSuccess(true);
      if (onCurriculumApplied) onCurriculumApplied();
    } catch (err: any) {
      setError(err.message || 'Failed to auto-create playlists');
    } finally {
      setApplying(false);
    }
  }

  async function handleGenerate(e: React.FormEvent) {
    e.preventDefault();
    if (!subject.trim()) return;
    setLoading(true);
    setError('');
    try {
      const res = await api.generateCurriculum({
        child_age: child.age,
        subject,
        goals,
        num_days: Number(days),
      });
      setCurriculum(res);
    } catch (err: any) {
      setError(err.message || 'Failed to generate curriculum');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div 
      className="fixed inset-0 flex items-center justify-center z-50 p-4 bg-black/60 backdrop-blur-sm fade-in-up"
    >
      <div className="glass-card !bg-[var(--bg-card)] p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto border border-[var(--border-highlight)] shadow-2xl">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-[var(--text-primary)] flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-amber-400" /> 
            <span>AI Adventure & Activity Planner for {child.name}</span>
          </h2>
          <button 
            onClick={onClose} 
            className="text-[var(--text-muted)] hover:text-[var(--text-primary)] p-1.5 rounded-lg hover:bg-[var(--surface-card)] transition-colors cursor-pointer"
            title="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {!curriculum ? (
          <form onSubmit={handleGenerate} className="space-y-4">
            <p className="text-xs text-[var(--text-secondary)]">
              Powered by AI. Enter any activity, hobby, or curiosity your child loves (crafts, drawing, science magic, sports, cooking, building), and AI will craft a fun, wholesome multi-day quest!
            </p>
            <div>
              <label className="block text-sm text-[var(--text-secondary)] mb-1 font-medium">Activity / Curiosity Topic</label>
              <input 
                type="text" 
                value={subject} 
                onChange={e => setSubject(e.target.value)} 
                required
                placeholder="e.g. Moral Stories on Kindness & Honesty, How to Draw Cartoons, Paper Glider Origami"
                className="input-field"
              />
            </div>
            <div>
              <label className="block text-sm text-[var(--text-secondary)] mb-1 font-medium">Special Request / Focus (Optional)</label>
              <input 
                type="text" 
                value={goals} 
                onChange={e => setGoals(e.target.value)}
                placeholder="e.g. Include hands-on paper folding or physical drawing challenges"
                className="input-field"
              />
            </div>
            <div>
              <label className="block text-sm text-[var(--text-secondary)] mb-1 font-medium">Quest Duration</label>
              <select 
                value={days} 
                onChange={e => setDays(Number(e.target.value))}
                className="input-field cursor-pointer"
              >
                <option value={3}>3-Day Fun Mini-Quest</option>
                <option value={5}>5-Day Weekly Adventure</option>
                <option value={7}>7-Day Exploration Journey</option>
              </select>
            </div>
            {error && <p className="text-rose-500 text-sm font-medium">{error}</p>}
            <div className="flex gap-3 pt-3">
              <button 
                type="button" 
                onClick={onClose}
                className="flex-1 btn-secondary text-sm"
              >
                Cancel
              </button>
              <button 
                type="submit" 
                disabled={loading}
                className="flex-1 btn-primary text-sm shadow-sm"
              >
                {loading ? 'Creating Adventure...' : 'Build Activity Quest'}
              </button>
            </div>
          </form>
        ) : (
          <div className="space-y-4">
            <div className="p-4 rounded-xl bg-[var(--surface-card)] border border-[var(--border-subtle)]">
              <h3 className="font-bold text-[var(--text-primary)] mb-1 text-sm">{curriculum.subject}</h3>
              <p className="text-xs text-[var(--text-secondary)]">{curriculum.summary || 'Wholesome multi-day story quest'}</p>
            </div>
            <div className="space-y-3">
              {curriculum.days?.map((d: any, i: number) => (
                <div key={i} className="p-4 rounded-xl bg-[var(--surface-card)] border border-[var(--border-subtle)]">
                  <div className="text-xs font-bold text-[var(--brand-primary)] uppercase tracking-wider mb-1">Day {d.day || i + 1}: {d.title}</div>
                  <p className="text-xs text-[var(--text-secondary)] mb-2">{d.objective}</p>
                  <div className="flex items-center gap-2">
                    <span className="text-[11px] text-[var(--text-muted)] font-medium">Search Keyword:</span>
                    <span className="text-xs font-mono font-bold text-[var(--text-primary)] bg-[var(--bg-dark)] px-2 py-0.5 rounded border border-[var(--border-subtle)]">
                      {d.youtube_query || d.title}
                    </span>
                  </div>
                </div>
              ))}
            </div>
            <div className="flex flex-col sm:flex-row gap-2 pt-2">
              <button 
                type="button" 
                onClick={() => { setCurriculum(null); setAppliedSuccess(false); }} 
                className="btn-secondary text-xs"
              >
                Generate Another
              </button>

              {appliedSuccess ? (
                <div className="flex-1 p-2 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-500 font-bold text-xs flex items-center justify-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4" />
                  <span>Playlists Created & Scheduled!</span>
                </div>
              ) : (
                <button 
                  type="button" 
                  onClick={handleApplyCurriculum} 
                  disabled={applying}
                  className="flex-1 btn-primary text-xs shadow-md flex items-center justify-center gap-1.5"
                >
                  <Wand2 className="w-3.5 h-3.5 text-amber-300" />
                  <span>{applying ? 'Finding Videos & Building...' : 'Auto-Build Playlists from Roadmap'}</span>
                </button>
              )}

              <button 
                type="button" 
                onClick={onClose} 
                className="btn-secondary text-xs"
              >
                Close
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
