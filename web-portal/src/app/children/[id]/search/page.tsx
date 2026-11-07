'use client';
import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import api from '@/lib/api';
import ThemeToggle from '@/components/ThemeToggle';
import { 
  Search, 
  ArrowLeft, 
  Sparkles, 
  Play, 
  Plus, 
  Check, 
  X, 
  ShieldCheck, 
  AlertCircle, 
  Calendar, 
  FolderPlus,
  Loader2
} from 'lucide-react';

interface Video {
  youtube_id: string;
  title: string;
  channel_name: string;
  thumbnail_url: string;
  duration_seconds: number;
  educational_score?: number;
  safety_score?: number;
  overall_score?: number;
  ai_summary?: string;
  is_safe?: boolean;
  subject_tags?: string[];
  description?: string;
  is_licensed?: boolean;
  is_embeddable?: boolean;
}

interface Playlist {
  id: string;
  title: string;
  scheduled_date?: string;
  video_count: number;
  items: any[];
}

export default function VideoSearchPage() {
  const { id: childId } = useParams<{ id: string }>();
  const router = useRouter();
  const [query, setQuery] = useState('');
  const [age, setAge] = useState('');
  const [useAI, setUseAI] = useState(false);
  const [licenseFilter, setLicenseFilter] = useState<'all' | 'licensed' | 'unlicensed'>('all');
  const [results, setResults] = useState<Video[]>([]);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState('');
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [selectedPlaylist, setSelectedPlaylist] = useState('');
  const [adding, setAdding] = useState<string | null>(null);
  const [addedIds, setAddedIds] = useState<Set<string>>(new Set());
  const [previewVideo, setPreviewVideo] = useState<Video | null>(null);

  const sampleTopics = [
    "Moral Stories for Kids", 
    "Kindness & Honesty Tales", 
    "Cute Animal Friends", 
    "How To Draw Cartoons", 
    "Paper Airplane Crafts", 
    "Calm Bedtime Stories"
  ];

  useEffect(() => {
    api.listPlaylists(childId).then(setPlaylists).catch(() => {});
  }, [childId]);

  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    if (!query.trim()) return;
    setSearching(true);
    setError('');
    setResults([]);
    try {
      const data = await api.searchVideos(query, age ? parseInt(age) : undefined, useAI);
      setResults(data);
    } catch (err: any) {
      setError(err.message || 'Search failed');
    } finally {
      setSearching(false);
    }
  }

  async function addToPlaylist(video: Video) {
    if (!selectedPlaylist) {
      alert('Please select a playlist in the right sidebar first');
      return;
    }
    setAdding(video.youtube_id);
    try {
      await api.addVideoToPlaylist(selectedPlaylist, {
        youtube_id: video.youtube_id,
        title: video.title,
        channel_name: video.channel_name,
        thumbnail_url: video.thumbnail_url,
        duration_seconds: video.duration_seconds,
      });
      setAddedIds(prev => new Set(prev).add(video.youtube_id));
    } catch (err: any) {
      alert(err.message || 'Failed to add video to playlist');
    } finally {
      setAdding(null);
    }
  }

  function scoreColor(score?: number) {
    if (!score) return 'var(--text-muted)';
    if (score >= 80) return 'var(--brand-success)';
    if (score >= 60) return 'var(--brand-accent)';
    return '#EF4444';
  }

  function formatDuration(secs: number) {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
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
          <span className="font-extrabold gradient-text">Video Discovery</span>
        </div>
        <ThemeToggle />
      </nav>

      <div className="max-w-6xl mx-auto px-6 py-8">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Main Search Column */}
          <div className="flex-1 min-w-0">
            <div className="mb-6">
              <h1 className="text-2xl font-extrabold mb-1.5 flex items-center gap-2 text-[var(--text-primary)]">
                <Search className="w-6 h-6 text-[var(--brand-primary)]" />
                <span>Discover Safe & Wholesome Videos</span>
              </h1>
              <p className="text-[var(--text-secondary)] text-sm">
                Filter out vulgarity, adult innuendo, screamers, and toxic brainrot. Handpick pure, moral, and creative YouTube videos for your child.
              </p>
            </div>

            {/* Search Box */}
            <form onSubmit={handleSearch} className="glass-card p-5 mb-6 space-y-4 shadow-sm">
              <div className="flex gap-3">
                <div className="relative flex-1">
                  <Search className="w-4 h-4 text-[var(--text-muted)] absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={query}
                    onChange={e => setQuery(e.target.value)}
                    placeholder='e.g. "Panchatantra Moral Stories", "Draw Cute Puppy", "Bedtime Storybooks"'
                    className="input-field pl-10"
                  />
                </div>
                <button 
                  type="submit" 
                  disabled={searching}
                  className="btn-primary min-w-[120px] shadow-sm"
                >
                  {searching ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" />
                      <span>Searching</span>
                    </>
                  ) : (
                    <>
                      <Search className="w-4 h-4" />
                      <span>Search</span>
                    </>
                  )}
                </button>
              </div>

              {/* Filters */}
              <div className="flex flex-wrap items-center gap-6 pt-2 border-t border-[var(--border-subtle)] text-xs">
                <div className="flex items-center gap-2">
                  <label className="text-[var(--text-secondary)] font-medium">Child Age:</label>
                  <input 
                    type="number" 
                    value={age} 
                    onChange={e => setAge(e.target.value)}
                    placeholder="7" 
                    min="1" 
                    max="18" 
                    className="w-16 px-2.5 py-1.5 rounded-lg bg-[var(--surface-card)] border border-[var(--border-subtle)] text-center text-[var(--text-primary)] outline-none focus:border-[var(--brand-primary)]"
                  />
                </div>

                <label className="flex items-center gap-2.5 cursor-pointer select-none">
                  <div className="relative">
                    <input 
                      type="checkbox" 
                      checked={useAI} 
                      onChange={e => setUseAI(e.target.checked)} 
                      className="sr-only" 
                    />
                    <div 
                      className="w-9 h-5 rounded-full transition-colors" 
                      style={{ background: useAI ? 'var(--brand-primary)' : 'var(--border-subtle)' }}
                    >
                      <div 
                        className="w-3.5 h-3.5 rounded-full bg-white absolute top-0.75 transition-transform shadow-xs"
                        style={{ left: useAI ? '18px' : '3px' }} 
                      />
                    </div>
                  </div>
                  <span className="flex items-center gap-1 font-semibold text-[var(--text-secondary)]">
                    <Sparkles className="w-3.5 h-3.5 text-amber-500" />
                    <span>Gemini AI Scoring</span>
                  </span>
                </label>

                <div className="flex items-center gap-2 ml-auto">
                  <label className="text-[var(--text-secondary)]">Content:</label>
                  <select
                    value={licenseFilter}
                    onChange={e => setLicenseFilter(e.target.value as any)}
                    className="px-2.5 py-1.5 rounded-lg bg-[var(--surface-card)] border border-[var(--border-subtle)] text-[var(--text-primary)] outline-none cursor-pointer"
                  >
                    <option value="all">All Safe Content</option>
                    <option value="unlicensed">Creative / Unlicensed</option>
                    <option value="licensed">Licensed / Studio</option>
                  </select>
                </div>
              </div>
            </form>

            {/* Error Message */}
            {error && (
              <div className="flex items-center gap-2 text-rose-500 text-sm mb-4 p-3.5 rounded-xl border border-rose-500/30 bg-rose-500/10">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                <span>{error}</span>
              </div>
            )}

            {/* Loading */}
            {searching && (
              <div className="glass-card text-center py-16 text-[var(--text-secondary)]">
                <Loader2 className="w-10 h-10 animate-spin mx-auto text-[var(--brand-primary)] mb-3" />
                <p className="font-semibold text-[var(--text-primary)]">
                  {useAI ? 'Gemini AI is analyzing transcripts and content...' : 'Searching safe YouTube database...'}
                </p>
                <p className="text-xs text-[var(--text-muted)] mt-1">Filtering embeds and verifying child safety</p>
              </div>
            )}

            {/* Results Count & Source Notice */}
            {results.length > 0 && !searching && (
              <div className="flex items-center justify-between text-xs text-[var(--text-muted)] mb-3 px-1">
                <span>Found {results.length} safe educational videos</span>
                <span className="flex items-center gap-1 text-[var(--brand-primary)] font-medium">
                  <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
                  <span>Child Safe & Verified</span>
                </span>
              </div>
            )}

            {/* Results List */}
            <div className="space-y-3.5">
              {results.filter(video => {
                if (licenseFilter === 'licensed') return video.is_licensed === true;
                if (licenseFilter === 'unlicensed') return video.is_licensed === false;
                return true;
              }).map(video => (
                <div 
                  key={video.youtube_id} 
                  className="glass-card p-4 flex flex-col sm:flex-row gap-4 hover:border-[var(--brand-primary)] transition-all shadow-sm"
                >
                  {/* Thumbnail & Duration */}
                  <div className="relative flex-shrink-0 w-full sm:w-36 h-24 rounded-xl overflow-hidden bg-black/20">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img 
                      src={video.thumbnail_url} 
                      alt={video.title}
                      className="w-full h-full object-cover" 
                    />
                    <div className="absolute bottom-1.5 right-1.5 text-[11px] font-mono px-1.5 py-0.5 rounded bg-black/80 text-white">
                      {formatDuration(video.duration_seconds)}
                    </div>
                  </div>

                  {/* Details */}
                  <div className="flex-1 min-w-0">
                    <h3 className="font-semibold text-sm leading-snug mb-1 line-clamp-2 text-[var(--text-primary)]">
                      {video.title}
                    </h3>
                    <p className="text-xs text-[var(--text-secondary)] mb-2 font-medium">
                      {video.channel_name}
                    </p>

                    {/* AI Score Pills */}
                    {video.overall_score !== undefined && (
                      <div className="flex flex-wrap gap-2 mb-2.5">
                        <ScorePill label="Overall Score" value={video.overall_score} scoreColor={scoreColor} />
                        <ScorePill label="Educational" value={video.educational_score} scoreColor={scoreColor} />
                        <ScorePill label="Safety" value={video.safety_score} scoreColor={scoreColor} />
                      </div>
                    )}

                    {video.subject_tags && video.subject_tags.length > 0 && (
                      <div className="flex flex-wrap gap-1.5 mb-2">
                        {video.subject_tags.slice(0, 3).map(tag => (
                          <span 
                            key={tag} 
                            className="text-[11px] px-2.5 py-0.5 rounded-full bg-[var(--brand-primary)]/10 text-[var(--brand-primary)] border border-[var(--brand-primary)]/20 font-medium"
                          >
                            {tag}
                          </span>
                        ))}
                      </div>
                    )}

                    {video.ai_summary && (
                      <p className="text-xs text-[var(--text-muted)] line-clamp-2">
                        {video.ai_summary}
                      </p>
                    )}
                  </div>

                  {/* Actions Column */}
                  <div className="flex sm:flex-col items-center sm:items-end justify-between sm:justify-center gap-2 flex-shrink-0 pt-2 sm:pt-0 border-t sm:border-t-0 border-[var(--border-subtle)]">
                    <button
                      type="button"
                      onClick={() => setPreviewVideo(video)}
                      className="btn-secondary !py-1.5 !px-3 text-xs w-full sm:w-auto"
                    >
                      <Play className="w-3.5 h-3.5 text-[var(--brand-primary)]" />
                      <span>Preview</span>
                    </button>
                    
                    <button
                      onClick={() => addToPlaylist(video)}
                      disabled={adding === video.youtube_id || addedIds.has(video.youtube_id)}
                      className={`text-xs font-semibold px-3 py-1.5 rounded-xl transition-all inline-flex items-center gap-1.5 w-full sm:w-auto justify-center cursor-pointer ${
                        addedIds.has(video.youtube_id)
                          ? 'bg-emerald-500/20 text-emerald-500 border border-emerald-500/30'
                          : 'bg-[var(--brand-primary)] hover:bg-[var(--brand-primary-hover)] text-white shadow-xs'
                      }`}
                    >
                      {addedIds.has(video.youtube_id) ? (
                        <>
                          <Check className="w-3.5 h-3.5 text-emerald-500" />
                          <span>Added</span>
                        </>
                      ) : adding === video.youtube_id ? (
                        <>
                          <Loader2 className="w-3.5 h-3.5 animate-spin" />
                          <span>Adding</span>
                        </>
                      ) : (
                        <>
                          <Plus className="w-3.5 h-3.5" />
                          <span>Add</span>
                        </>
                      )}
                    </button>
                  </div>
                </div>
              ))}

              {/* Empty state suggestions */}
              {results.length === 0 && !searching && (
                <div className="glass-card p-10 text-center text-[var(--text-secondary)] shadow-sm">
                  <div className="w-14 h-14 rounded-2xl bg-[var(--brand-primary)]/10 border border-[var(--brand-primary)]/20 flex items-center justify-center mx-auto mb-4 text-[var(--brand-primary)]">
                    <Sparkles className="w-7 h-7" />
                  </div>
                  <h3 className="font-bold text-base text-[var(--text-primary)] mb-1.5">
                    Explore Safe Educational Topics
                  </h3>
                  <p className="text-xs text-[var(--text-muted)] max-w-md mx-auto mb-6">
                    Pick a popular subject below or enter a customized lesson query above to discover videos with AI comprehension scores.
                  </p>
                  <div className="flex flex-wrap justify-center gap-2.5 max-w-lg mx-auto">
                    {sampleTopics.map(topic => (
                      <button
                        key={topic}
                        onClick={() => {
                          setQuery(topic);
                          setSearching(true);
                          api.searchVideos(topic, age ? parseInt(age) : undefined, useAI)
                            .then(setResults)
                            .catch(e => setError(e.message))
                            .finally(() => setSearching(false));
                        }}
                        className="px-3.5 py-2 rounded-xl text-xs font-medium border border-[var(--border-subtle)] hover:border-[var(--brand-primary)] bg-[var(--surface-card)] hover:bg-[var(--surface-card-hover)] text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-all flex items-center gap-1.5 cursor-pointer"
                      >
                        <Sparkles className="w-3 h-3 text-amber-500" />
                        <span>{topic}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Right Sidebar: Playlist selector */}
          <div className="w-full lg:w-72 flex-shrink-0">
            <div className="glass-card p-5 sticky top-24 shadow-sm">
              <div className="flex items-center gap-2 mb-4 font-bold text-sm text-[var(--text-primary)]">
                <FolderPlus className="w-4 h-4 text-[var(--brand-primary)]" />
                <span>Assign to Playlist</span>
              </div>
              
              {playlists.length === 0 ? (
                <div className="text-xs text-[var(--text-muted)] text-center py-6">
                  <p className="mb-3">No playlists created yet</p>
                  <Link 
                    href={`/children/${childId}`}
                    className="btn-secondary !py-1.5 !px-3 text-xs text-[var(--brand-primary)]"
                  >
                    Create Playlist →
                  </Link>
                </div>
              ) : (
                <div className="space-y-2">
                  {playlists.map(pl => {
                    const isSelected = selectedPlaylist === pl.id;
                    return (
                      <button
                        key={pl.id}
                        type="button"
                        onClick={() => setSelectedPlaylist(pl.id)}
                        className={`w-full text-left p-3 rounded-xl text-xs transition-all border cursor-pointer ${
                          isSelected 
                            ? 'bg-[var(--brand-primary)]/15 border-[var(--brand-primary)] shadow-xs' 
                            : 'bg-[var(--surface-card)] border-[var(--border-subtle)] hover:border-[var(--brand-primary)]/50'
                        }`}
                      >
                        <div className="font-semibold text-[var(--text-primary)] text-sm line-clamp-1">{pl.title}</div>
                        <div className="flex items-center gap-2 text-[var(--text-secondary)] mt-1 font-medium">
                          <span className="flex items-center gap-1">
                            <Calendar className="w-3 h-3 text-amber-500" />
                            <span>{pl.scheduled_date ? pl.scheduled_date : 'Template'}</span>
                          </span>
                          <span>·</span>
                          <span>{pl.video_count} videos</span>
                        </div>
                      </button>
                    );
                  })}
                </div>
              )}

              <div className="mt-4 pt-4 border-t border-[var(--border-subtle)]">
                <Link
                  href={`/children/${childId}`}
                  className="block text-center text-xs text-[var(--brand-primary)] hover:underline font-medium transition-colors"
                >
                  Manage Playlists & Reorder →
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Video Preview Modal */}
      {previewVideo && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm fade-in-up"
          onClick={() => setPreviewVideo(null)}
        >
          <div
            className="glass-card !bg-[var(--bg-card)] w-full max-w-3xl overflow-hidden rounded-2xl p-5 border border-[var(--border-highlight)] shadow-2xl"
            onClick={e => e.stopPropagation()}
          >
            <div className="flex justify-between items-center mb-3.5">
              <h3 className="font-bold text-sm text-[var(--text-primary)] line-clamp-1 flex items-center gap-2">
                <Play className="w-4 h-4 text-[var(--brand-primary)]" />
                <span>{previewVideo.title}</span>
              </h3>
              <button
                onClick={() => setPreviewVideo(null)}
                className="text-[var(--text-muted)] hover:text-[var(--text-primary)] p-1 rounded-lg hover:bg-[var(--surface-card)] transition-colors cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="relative aspect-video w-full rounded-xl overflow-hidden bg-black mb-4 border border-[var(--border-subtle)]">
              <iframe
                src={`https://www.youtube-nocookie.com/embed/${previewVideo.youtube_id}?autoplay=1`}
                className="w-full h-full border-0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              />
            </div>

            <div className="flex items-center justify-between">
              <div className="text-xs text-[var(--text-secondary)]">
                Channel: <strong className="text-[var(--text-primary)]">{previewVideo.channel_name}</strong>
              </div>
              <div className="flex gap-2.5">
                <button
                  onClick={() => setPreviewVideo(null)}
                  className="btn-secondary !py-2 !px-4 text-xs"
                >
                  Close
                </button>
                <button
                  onClick={() => {
                    addToPlaylist(previewVideo);
                    setPreviewVideo(null);
                  }}
                  className="btn-primary !py-2 !px-4 text-xs"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Add to Playlist</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function ScorePill({ label, value, scoreColor }: { label: string; value?: number; scoreColor: (v?: number) => string }) {
  return (
    <div className="text-[11px] px-2.5 py-1 rounded-lg flex items-center gap-1.5 bg-[var(--surface-card)] border border-[var(--border-subtle)]">
      <span className="text-[var(--text-secondary)] font-medium">{label}:</span>
      <span className="font-bold font-mono" style={{ color: scoreColor(value) }}>
        {value ?? '—'}
      </span>
    </div>
  );
}
