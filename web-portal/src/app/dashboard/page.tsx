'use client';
import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { 
  Rocket, 
  Plus, 
  Search, 
  ListVideo, 
  Clock, 
  CheckCircle2, 
  LogOut, 
  User, 
  ChevronRight,
  Pencil,
  X,
  Play,
  Settings,
  Mail,
  ShieldCheck,
  KeyRound,
  Trash2,
  AlertTriangle
} from 'lucide-react';
import api from '@/lib/api';
import ThemeToggle from '@/components/ThemeToggle';
import ThematicAvatar, { THEMATIC_BADGES } from '@/components/ThematicAvatar';
import { playPop, playTactileClick } from '@/lib/haptics';

interface Child {
  id: string;
  name: string;
  age: number;
  avatar: string;
}

interface ProgressSummary {
  total_videos_watched: number;
  total_watch_time_seconds: number;
  completed_videos: number;
  this_week_seconds: number;
}

interface ParentUser {
  id?: string;
  name: string;
  email?: string;
}

export default function DashboardPage() {
  const router = useRouter();
  const [user, setUser] = useState<ParentUser | null>(null);
  const [children, setChildren] = useState<Child[]>([]);
  const [progressMap, setProgressMap] = useState<Record<string, ProgressSummary>>({});
  const [showAddChild, setShowAddChild] = useState(false);
  const [editingChild, setEditingChild] = useState<Child | null>(null);
  const [showAccountSettings, setShowAccountSettings] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem('kc_user');
    if (!stored || !localStorage.getItem('kc_token')) {
      router.push('/');
      return;
    }
    setUser(JSON.parse(stored));
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadData() {
    try {
      const kids = await api.listChildren();
      setChildren(kids);
      const prog: Record<string, ProgressSummary> = {};
      for (const child of kids) {
        try {
          prog[child.id] = await api.getChildProgress(child.id);
        } catch {}
      }
      setProgressMap(prog);
    } catch {
      router.push('/');
    } finally {
      setLoading(false);
    }
  }

  function formatTime(seconds: number) {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    if (h > 0) return `${h}h ${m}m`;
    return `${m}m`;
  }

  function logout() {
    localStorage.removeItem('kc_token');
    localStorage.removeItem('kc_user');
    router.push('/');
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--bg-dark)]">
        <div className="text-[var(--text-secondary)] font-medium flex items-center gap-2">
          <span className="w-2.5 h-2.5 rounded-full bg-[var(--brand-primary)] animate-ping" />
          <span>Loading dashboard...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[var(--bg-dark)] text-[var(--text-primary)] transition-colors duration-200">
      {/* Navbar */}
      <nav className="glass-nav px-6 py-4 flex items-center justify-between sticky top-0 z-30 transition-colors">
        <div className="flex items-center gap-3">
          <Image src="/logo.png" alt="KidCurate" width={32} height={32} className="rounded-lg shadow-md" />
          <span className="font-extrabold text-lg gradient-text tracking-tight">KidCurate</span>
        </div>
        <div className="flex items-center gap-3">
          <ThemeToggle />
          <button
            onClick={() => { playTactileClick(); setShowAccountSettings(true); }}
            className="flex items-center gap-2 text-xs font-semibold px-2.5 py-1.5 rounded-lg border border-[var(--border-subtle)] bg-[var(--surface-card)] text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:border-[var(--brand-primary)] transition-all cursor-pointer shadow-sm"
            title="Account Settings (Name, Email, Password)"
          >
            <div className="w-5 h-5 rounded-full bg-[var(--brand-primary)]/20 text-[var(--brand-primary)] flex items-center justify-center font-bold text-[10px]">
              {user?.name ? user.name.charAt(0).toUpperCase() : <User className="w-3 h-3" />}
            </div>
            <span className="hidden sm:inline">
              Welcome, <strong className="text-[var(--text-primary)]">{user?.name?.split(' ')[0]}</strong>
            </span>
            <Settings className="w-3.5 h-3.5 text-[var(--text-muted)]" />
          </button>
          <button 
            onClick={logout}
            className="flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-lg border border-[var(--border-subtle)] bg-[var(--surface-card)] text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:border-[var(--brand-primary)] transition-all cursor-pointer font-medium"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Sign Out</span>
          </button>
        </div>
      </nav>

      <div className="max-w-6xl mx-auto px-6 py-10">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-extrabold text-[var(--text-primary)] flex items-center gap-3">
              <span>Your Children Profiles</span>
              <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-[var(--brand-primary)]/10 text-[var(--brand-primary)] border border-[var(--brand-primary)]/20">
                {children.length} {children.length === 1 ? 'child' : 'children'}
              </span>
            </h1>
            <p className="text-[var(--text-secondary)] text-sm mt-1">
              Select a profile below to manage playlists, explore wholesome educational videos, or check screen time.
            </p>
          </div>
          <div className="flex items-center gap-3 self-start sm:self-auto flex-wrap">
            <Link
              href="/watch"
              className="btn-primary !bg-emerald-500 hover:!bg-emerald-600 shadow-md inline-flex items-center gap-2 text-xs font-bold py-2 px-3.5"
            >
              <Play className="w-4 h-4 fill-white" />
              <span>Launch Child Sanctuary (iPad/Mac/PC)</span>
            </Link>
            <button
              onClick={() => setShowAddChild(true)}
              className="btn-secondary text-xs shadow-sm py-2 px-3.5 inline-flex items-center gap-1.5"
            >
              <Plus className="w-4 h-4" />
              <span>Add Child</span>
            </button>
          </div>
        </div>

        {/* Children Grid */}
        {children.length === 0 ? (
          <div className="glass-card p-12 text-center max-w-lg mx-auto">
            <div className="w-16 h-16 rounded-2xl bg-[var(--brand-primary)]/10 border border-[var(--brand-primary)]/20 flex items-center justify-center mx-auto mb-4 text-[var(--brand-primary)]">
              <Rocket className="w-8 h-8" />
            </div>
            <h2 className="text-xl font-bold mb-2 text-[var(--text-primary)]">Create Your First Child Profile</h2>
            <p className="text-[var(--text-secondary)] text-sm mb-6">
              Set up fun, wholesome daily learning routines with curated, safe YouTube videos.
            </p>
            <button
              onClick={() => { playTactileClick(); setShowAddChild(true); }}
              className="btn-primary"
            >
              <Plus className="w-4 h-4" />
              <span>Create Child Profile</span>
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {children.map(child => {
              const prog = progressMap[child.id];
              return (
                <ChildCard
                  key={child.id}
                  child={child}
                  progress={prog}
                  formatTime={formatTime}
                  onEdit={c => setEditingChild(c)}
                />
              );
            })}

            {/* Add Child Dashed Card */}
            <button
              onClick={() => { playTactileClick(); setShowAddChild(true); }}
              className="glass-card p-8 flex flex-col items-center justify-center gap-3 border-2 border-dashed border-[var(--border-subtle)] hover:border-[var(--brand-primary)] bg-[var(--surface-card)]/40 hover:bg-[var(--surface-card)] transition-all cursor-pointer min-h-[220px] group rounded-2xl"
            >
              <div className="w-12 h-12 rounded-xl bg-[var(--brand-primary)]/10 border border-[var(--brand-primary)]/20 flex items-center justify-center text-[var(--brand-primary)] group-hover:scale-110 transition-transform">
                <Plus className="w-6 h-6" />
              </div>
              <span className="text-sm font-bold text-[var(--text-primary)] group-hover:text-[var(--brand-primary)] transition-colors">
                Add Another Child
              </span>
            </button>
          </div>
        )}
      </div>

      {showAddChild && (
        <AddChildModal
          onClose={() => setShowAddChild(false)}
          onAdded={() => { setShowAddChild(false); loadData(); }}
        />
      )}

      {editingChild && (
        <EditChildModal
          child={editingChild}
          onClose={() => setEditingChild(null)}
          onUpdated={() => { setEditingChild(null); loadData(); }}
        />
      )}

      {showAccountSettings && user && (
        <AccountSettingsModal
          user={user}
          onClose={() => setShowAccountSettings(false)}
          onUserUpdated={(updated) => {
            setUser(updated);
            localStorage.setItem('kc_user', JSON.stringify(updated));
          }}
        />
      )}
    </div>
  );
}

const ChildCard = React.memo(function ChildCard({ child, progress, formatTime, onEdit }: {
  child: Child;
  progress?: ProgressSummary;
  formatTime: (s: number) => string;
  onEdit: (c: Child) => void;
}) {
  const router = useRouter();
  return (
    <div
      className="glass-card p-6 cursor-pointer hover:border-[var(--brand-primary)] transition-all group relative flex flex-col justify-between shadow-sm hover:shadow-lg"
      onClick={() => { playPop(); router.push(`/children/${child.id}`); }}
    >
      <div>
        <div className="flex items-center justify-between mb-5">
          <div className="flex items-center gap-3.5">
            <ThematicAvatar 
              name={child.name} 
              avatar={child.avatar} 
              size="lg" 
              className="group-hover:scale-105 transition-transform" 
            />
            <div>
              <h3 className="text-xl font-bold text-[var(--text-primary)] group-hover:text-[var(--brand-primary)] transition-colors">
                {child.name}
              </h3>
              <p className="text-[var(--text-secondary)] text-xs mt-0.5 font-medium">Age {child.age}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={e => {
                e.stopPropagation();
                playTactileClick();
                onEdit(child);
              }}
              title="Edit Profile"
              className="p-1.5 rounded-lg border border-[var(--border-subtle)] bg-[var(--surface-card)] text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:border-[var(--brand-primary)] transition-all cursor-pointer"
            >
              <Pencil className="w-3.5 h-3.5" />
            </button>
            <ChevronRight className="w-5 h-5 text-[var(--text-muted)] group-hover:text-[var(--brand-primary)] transition-colors" />
          </div>
        </div>

        {progress ? (
          <div className="grid grid-cols-2 gap-2.5 mb-5">
            <div className="rounded-xl p-3 bg-[var(--surface-card)] border border-[var(--border-subtle)] text-center">
              <div className="flex items-center justify-center gap-1 text-[11px] text-[var(--text-secondary)] mb-1 font-medium">
                <Clock className="w-3 h-3 text-[var(--brand-primary)]" />
                <span>This Week</span>
              </div>
              <div className="text-sm font-bold text-[var(--text-primary)] font-mono">{formatTime(progress.this_week_seconds)}</div>
            </div>
            <div className="rounded-xl p-3 bg-[var(--surface-card)] border border-[var(--border-subtle)] text-center">
              <div className="flex items-center justify-center gap-1 text-[11px] text-[var(--text-secondary)] mb-1 font-medium">
                <CheckCircle2 className="w-3 h-3 text-emerald-500" />
                <span>Completed</span>
              </div>
              <div className="text-sm font-bold text-[var(--text-primary)] font-mono">{progress.completed_videos} videos</div>
            </div>
          </div>
        ) : (
          <div className="p-3 rounded-xl bg-[var(--surface-card)] border border-[var(--border-subtle)] text-center text-xs text-[var(--text-muted)] mb-5">
            No active playlists scheduled yet
          </div>
        )}
      </div>

      <div className="flex gap-2 pt-3 border-t border-[var(--border-subtle)]">
        <Link
          href={`/children/${child.id}/search`}
          className="flex-1 btn-primary !py-2 !px-3 text-xs justify-center"
          onClick={e => e.stopPropagation()}
        >
          <Search className="w-3.5 h-3.5" />
          <span>Add Videos</span>
        </Link>
        <Link
          href={`/children/${child.id}`}
          className="flex-1 btn-secondary !py-2 !px-3 text-xs justify-center"
          onClick={e => e.stopPropagation()}
        >
          <ListVideo className="w-3.5 h-3.5 text-[var(--brand-primary)]" />
          <span>Playlists</span>
        </Link>
      </div>
    </div>
  );
});

function AddChildModal({ onClose, onAdded }: { onClose: () => void; onAdded: () => void }) {
  const [name, setName] = useState('');
  const [age, setAge] = useState('');
  const [avatar, setAvatar] = useState('compass');
  const [pin, setPin] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (pin.length !== 4 || !/^\d{4}$/.test(pin)) {
      setError('PIN must be exactly 4 digits');
      return;
    }
    setLoading(true);
    setError('');
    try {
      await api.createChild({ name, age: parseInt(age), avatar, pin });
      onAdded();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 p-4 bg-black/60 backdrop-blur-md fade-in-up">
      <div className="glass-panel p-7 w-full max-w-md shadow-2xl">
        <div className="flex justify-between items-center mb-5">
          <h2 className="text-lg font-bold text-[var(--text-primary)] flex items-center gap-2">
            <User className="w-5 h-5 text-[var(--brand-primary)]" />
            <span>Add Child Profile</span>
          </h2>
          <button 
            onClick={onClose} 
            className="text-[var(--text-muted)] hover:text-[var(--text-primary)] p-1.5 rounded-lg hover:bg-[var(--surface-card)] transition-colors cursor-pointer"
            title="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-2">
              Character Badge
            </label>
            <div className="grid grid-cols-5 gap-2">
              {THEMATIC_BADGES.map(b => {
                const isSelected = avatar === b.key;
                const BadgeIcon = b.Icon;
                return (
                  <button
                    key={b.key}
                    type="button"
                    onClick={() => { playPop(); setAvatar(b.key); }}
                    title={b.label}
                    className={`flex flex-col items-center justify-center gap-1 p-2 rounded-xl transition-all border cursor-pointer ${
                      isSelected 
                        ? 'bg-[var(--brand-primary)]/15 border-[var(--brand-primary)] text-[var(--brand-primary)] scale-105 shadow-sm' 
                        : 'bg-[var(--surface-card)] border-[var(--border-subtle)] text-[var(--text-secondary)] hover:border-[var(--brand-primary)]/50'
                    }`}
                  >
                    <BadgeIcon className="w-5 h-5" />
                    <span className="text-[10px] font-medium leading-none">{b.label}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
              Child's Name
            </label>
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Aarav"
              required
              className="input-field"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
              Age (Years)
            </label>
            <input
              type="number"
              value={age}
              onChange={e => setAge(e.target.value)}
              placeholder="7"
              required
              min="1"
              max="18"
              className="input-field"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
              4-digit Login PIN (for Android App)
            </label>
            <input
              type="password"
              value={pin}
              onChange={e => setPin(e.target.value)}
              placeholder="••••"
              maxLength={4}
              required
              className="input-field font-mono text-center tracking-widest text-lg"
            />
          </div>

          {error && <p className="text-rose-500 text-xs font-medium">{error}</p>}

          <div className="flex gap-3 pt-3">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 btn-secondary text-xs"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 btn-primary text-xs"
            >
              {loading ? 'Creating Profile...' : 'Save Profile'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function EditChildModal({ child, onClose, onUpdated }: { child: Child; onClose: () => void; onUpdated: () => void }) {
  const [name, setName] = useState(child.name);
  const [age, setAge] = useState(child.age.toString());
  const [avatar, setAvatar] = useState(child.avatar || 'compass');
  const [pin, setPin] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting, setDeleting] = useState(false);

  async function handleDelete() {
    setDeleting(true);
    try {
      await api.deleteChild(child.id);
      playPop();
      onUpdated();
    } catch (err: any) {
      setError(err.message || 'Failed to delete profile');
    } finally {
      setDeleting(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (pin && (pin.length !== 4 || !/^\d{4}$/.test(pin))) {
      setError('PIN must be exactly 4 digits');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const data: any = { name, age: parseInt(age), avatar };
      if (pin) data.pin = pin;
      await api.updateChild(child.id, data);
      onUpdated();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 p-4 bg-black/60 backdrop-blur-md fade-in-up">
      <div className="glass-panel p-7 w-full max-w-md shadow-2xl">
        <div className="flex justify-between items-center mb-5">
          <h2 className="text-lg font-bold text-[var(--text-primary)] flex items-center gap-2">
            <Pencil className="w-5 h-5 text-[var(--brand-primary)]" />
            <span>Edit {child.name}'s Profile</span>
          </h2>
          <button 
            onClick={onClose} 
            className="text-[var(--text-muted)] hover:text-[var(--text-primary)] p-1.5 rounded-lg hover:bg-[var(--surface-card)] transition-colors cursor-pointer"
            title="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-2">
              Character Badge
            </label>
            <div className="grid grid-cols-5 gap-2">
              {THEMATIC_BADGES.map(b => {
                const isSelected = avatar === b.key || avatar === b.label.toLowerCase();
                const BadgeIcon = b.Icon;
                return (
                  <button
                    key={b.key}
                    type="button"
                    onClick={() => { playPop(); setAvatar(b.key); }}
                    title={b.label}
                    className={`flex flex-col items-center justify-center gap-1 p-2 rounded-xl transition-all border cursor-pointer ${
                      isSelected 
                        ? 'bg-[var(--brand-primary)]/15 border-[var(--brand-primary)] text-[var(--brand-primary)] scale-105 shadow-sm' 
                        : 'bg-[var(--surface-card)] border-[var(--border-subtle)] text-[var(--text-secondary)] hover:border-[var(--brand-primary)]/50'
                    }`}
                  >
                    <BadgeIcon className="w-5 h-5" />
                    <span className="text-[10px] font-medium leading-none">{b.label}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
              Child's Name
            </label>
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Aarav"
              required
              className="input-field"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
              Age (Years)
            </label>
            <input
              type="number"
              value={age}
              onChange={e => setAge(e.target.value)}
              required
              min="1"
              max="18"
              className="input-field"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
              New 4-digit PIN (optional, leave blank to keep current)
            </label>
            <input
              type="password"
              value={pin}
              onChange={e => setPin(e.target.value)}
              placeholder="••••"
              maxLength={4}
              className="input-field font-mono text-center tracking-widest text-lg"
            />
          </div>

          {error && <p className="text-rose-500 text-xs font-medium">{error}</p>}

          <div className="flex gap-3 pt-3">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 btn-secondary text-xs"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 btn-primary text-xs"
            >
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>

          {confirmDelete ? (
            <div className="mt-4 pt-3 border-t border-[var(--border-subtle)] bg-rose-500/10 p-3 rounded-xl border border-rose-500/20">
              <p className="text-xs text-rose-400 font-semibold mb-2">Delete {child.name}'s profile and all playlists?</p>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setConfirmDelete(false)}
                  className="flex-1 btn-secondary text-xs py-1"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  disabled={deleting}
                  onClick={handleDelete}
                  className="flex-1 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-lg text-xs py-1 transition-colors"
                >
                  {deleting ? 'Deleting...' : 'Yes, Delete'}
                </button>
              </div>
            </div>
          ) : (
            <div className="mt-4 pt-3 border-t border-[var(--border-subtle)] flex justify-end">
              <button
                type="button"
                onClick={() => setConfirmDelete(true)}
                className="text-xs text-rose-400 hover:text-rose-300 transition-colors flex items-center gap-1.5 cursor-pointer"
              >
                <Trash2 className="w-3.5 h-3.5" />
                <span>Delete Profile</span>
              </button>
            </div>
          )}
        </form>
      </div>
    </div>
  );
}

function AccountSettingsModal({
  user,
  onClose,
  onUserUpdated,
}: {
  user: ParentUser;
  onClose: () => void;
  onUserUpdated: (u: ParentUser) => void;
}) {
  const [name, setName] = useState(user.name || '');
  const [email, setEmail] = useState(user.email || '');
  const [showPasswordSection, setShowPasswordSection] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [confirmDeleteAccount, setConfirmDeleteAccount] = useState(false);
  const [deletingAccount, setDeletingAccount] = useState(false);
  const router = useRouter();

  async function handleDeleteAccount() {
    setDeletingAccount(true);
    try {
      await api.deleteAccount();
      localStorage.removeItem('kc_token');
      localStorage.removeItem('kc_user');
      router.push('/');
    } catch (err: any) {
      setError(err.message || 'Failed to delete account.');
      setDeletingAccount(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (name.trim().length < 2) {
      setError('Name must be at least 2 characters long.');
      return;
    }

    if (showPasswordSection || newPassword) {
      if (!currentPassword) {
        setError('Please enter your current password to confirm changes.');
        return;
      }
      if (newPassword.length < 6) {
        setError('New password must be at least 6 characters long.');
        return;
      }
      if (newPassword !== confirmPassword) {
        setError('New passwords do not match.');
        return;
      }
    }

    setLoading(true);
    try {
      const payload: {
        name?: string;
        email?: string;
        current_password?: string;
        new_password?: string;
      } = {
        name: name.trim(),
        email: email.trim().toLowerCase(),
      };

      if (newPassword) {
        payload.current_password = currentPassword;
        payload.new_password = newPassword;
      }

      const res = await api.updateProfile(payload);
      if (res.access_token) {
        localStorage.setItem('kc_token', res.access_token);
      }
      onUserUpdated({ id: res.id, name: res.name, email: res.email });
      playPop();
      setSuccess(res.message || 'Profile updated successfully!');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setShowPasswordSection(false);
      setTimeout(() => {
        onClose();
      }, 1200);
    } catch (err: any) {
      setError(err.message || 'Failed to update profile.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 p-4 bg-black/60 backdrop-blur-md fade-in-up">
      <div className="glass-panel p-6 sm:p-7 w-full max-w-lg shadow-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-5 pb-3 border-b border-[var(--border-subtle)]">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-[var(--brand-primary)]/10 border border-[var(--brand-primary)]/20 flex items-center justify-center text-[var(--brand-primary)]">
              <Settings className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[var(--text-primary)]">Account Settings</h2>
              <p className="text-xs text-[var(--text-secondary)]">Manage your name, email, and parent password</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-[var(--text-muted)] hover:text-[var(--text-primary)] hover:bg-[var(--surface-card)] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs flex items-center gap-2">
              <span className="font-semibold">Error:</span> {error}
            </div>
          )}

          {success && (
            <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs flex items-center gap-2">
              <ShieldCheck className="w-4 h-4" />
              <span>{success}</span>
            </div>
          )}

          {/* Full Name */}
          <div>
            <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
              <User className="w-3.5 h-3.5 text-[var(--brand-primary)]" />
              <span>Parent / Full Name</span>
            </label>
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Sarah Connor"
              required
              className="input-field"
            />
          </div>

          {/* Email Address */}
          <div>
            <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
              <Mail className="w-3.5 h-3.5 text-[var(--brand-primary)]" />
              <span>Email Address</span>
            </label>
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="parent@example.com"
              required
              className="input-field"
            />
          </div>

          {/* Password Security Section */}
          <div className="pt-2 border-t border-[var(--border-subtle)]">
            <button
              type="button"
              onClick={() => {
                setShowPasswordSection(!showPasswordSection);
                setError('');
              }}
              className="w-full flex items-center justify-between p-2.5 rounded-xl bg-[var(--surface-card)] border border-[var(--border-subtle)] hover:border-[var(--brand-primary)] transition-all text-xs font-semibold text-[var(--text-primary)] cursor-pointer"
            >
              <span className="flex items-center gap-2">
                <KeyRound className="w-4 h-4 text-amber-400" />
                <span>Change Parent Password</span>
              </span>
              <span className="text-[11px] text-[var(--brand-primary)] font-medium">
                {showPasswordSection ? 'Hide' : 'Change Password'}
              </span>
            </button>

            {showPasswordSection && (
              <div className="mt-3 space-y-3 p-3.5 rounded-xl bg-[var(--surface-card)]/60 border border-[var(--border-subtle)] fade-in-up">
                <div>
                  <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1">
                    Current Password
                  </label>
                  <input
                    type="password"
                    value={currentPassword}
                    onChange={e => setCurrentPassword(e.target.value)}
                    placeholder="Enter current password"
                    className="input-field text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1">
                    New Password (min 6 characters)
                  </label>
                  <input
                    type="password"
                    value={newPassword}
                    onChange={e => setNewPassword(e.target.value)}
                    placeholder="Enter new password"
                    className="input-field text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1">
                    Confirm New Password
                  </label>
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={e => setConfirmPassword(e.target.value)}
                    placeholder="Confirm new password"
                    className="input-field text-sm"
                  />
                </div>
              </div>
            )}
          </div>

          <div className="flex gap-3 pt-3">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 btn-secondary text-xs"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 btn-primary text-xs flex items-center justify-center gap-2"
            >
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>{loading ? 'Saving...' : 'Save Profile Changes'}</span>
            </button>
          </div>

          {confirmDeleteAccount ? (
            <div className="mt-5 pt-4 border-t border-rose-500/20 bg-rose-500/10 p-4 rounded-xl border border-rose-500/30">
              <p className="text-xs text-rose-400 font-bold mb-1 flex items-center gap-1.5">
                <AlertTriangle className="w-4 h-4 text-rose-500" />
                <span>Permanently Delete Entire Account?</span>
              </p>
              <p className="text-xs text-[var(--text-muted)] mb-3">
                This will permanently delete your account, all child profiles, playlists, and watch history. This cannot be undone.
              </p>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setConfirmDeleteAccount(false)}
                  className="flex-1 btn-secondary text-xs py-1.5"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  disabled={deletingAccount}
                  onClick={handleDeleteAccount}
                  className="flex-1 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-lg text-xs py-1.5 transition-colors"
                >
                  {deletingAccount ? 'Deleting Everything...' : 'Delete My Account'}
                </button>
              </div>
            </div>
          ) : (
            <div className="mt-5 pt-4 border-t border-[var(--border-subtle)] flex items-center justify-between">
              <div>
                <p className="text-xs font-semibold text-rose-400">Delete Account</p>
                <p className="text-[11px] text-[var(--text-muted)]">Permanently erase your account and all children data</p>
              </div>
              <button
                type="button"
                onClick={() => setConfirmDeleteAccount(true)}
                className="text-xs text-rose-400 hover:text-rose-300 font-semibold px-3 py-1.5 rounded-lg border border-rose-500/30 hover:bg-rose-500/10 transition-colors cursor-pointer"
              >
                Delete Account
              </button>
            </div>
          )}
        </form>
      </div>
    </div>
  );
}
