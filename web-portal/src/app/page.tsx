'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import api from '@/lib/api';
import ThemeToggle from '@/components/ThemeToggle';
import { Shield, BookOpen, BarChart3, KeyRound, ArrowLeft, CheckCircle2 } from 'lucide-react';

export default function HomePage() {
  const [mode, setMode] = useState<'login' | 'register' | 'forgot'>('login');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  // Forgot password state
  const [forgotStage, setForgotStage] = useState<'request' | 'reset'>('request');
  const [forgotEmail, setForgotEmail] = useState('');
  const [resetCode, setResetCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [codeHint, setCodeHint] = useState('');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setSuccessMsg('');
    setLoading(true);
    try {
      const res = mode === 'login'
        ? await api.login(email, password)
        : await api.register(name, email, password);
      localStorage.setItem('kc_token', res.access_token);
      localStorage.setItem('kc_user', JSON.stringify({ id: res.user_id, name: res.name, email: res.email }));
      router.push('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Something went wrong');
    } finally {
      setLoading(false);
    }
  }

  async function handleRequestResetCode(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setSuccessMsg('');
    setLoading(true);
    try {
      const res = await api.forgotPassword(forgotEmail.trim());
      setSuccessMsg(res.message || 'Verification code generated.');
      if (res.reset_code) {
        setCodeHint(res.reset_code);
        setResetCode(res.reset_code);
      }
      setForgotStage('reset');
    } catch (err: any) {
      setError(err.message || 'Failed to request reset code');
    } finally {
      setLoading(false);
    }
  }

  async function handleResetPassword(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setSuccessMsg('');

    if (newPassword.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      const res = await api.resetPassword(forgotEmail.trim(), resetCode.trim(), newPassword);
      setSuccessMsg(res.message || 'Password successfully reset!');
      // Prefill login email and switch to login
      setEmail(forgotEmail.trim());
      setPassword('');
      setResetCode('');
      setNewPassword('');
      setConfirmPassword('');
      setCodeHint('');
      setTimeout(() => {
        setMode('login');
        setForgotStage('request');
      }, 1500);
    } catch (err: any) {
      setError(err.message || 'Failed to reset password');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen flex items-center justify-center p-4 relative overflow-hidden bg-[var(--bg-dark)] text-[var(--text-primary)] transition-colors duration-200">
      <div className="absolute top-6 right-6 z-20">
        <ThemeToggle />
      </div>
      {/* Subtle background glow */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none opacity-40">
        <div 
          className="absolute -top-40 -right-40 w-96 h-96 rounded-full opacity-20"
          style={{ background: 'radial-gradient(circle, var(--brand-primary), transparent)' }} 
        />
        <div 
          className="absolute -bottom-40 -left-40 w-96 h-96 rounded-full opacity-20"
          style={{ background: 'radial-gradient(circle, var(--brand-accent), transparent)' }} 
        />
      </div>

      <div className="w-full max-w-md fade-in-up z-10">
        {/* Logo */}
        <div className="text-center mb-8 flex flex-col items-center">
          <Image src="/logo.png" alt="KidCurate Logo" width={64} height={64} className="mb-3 object-contain rounded-2xl shadow-lg" priority />
          <h1 className="text-4xl font-extrabold gradient-text tracking-tight">KidCurate</h1>
          <p className="text-[var(--text-secondary)] mt-2 text-sm font-medium">
            Parent-controlled YouTube learning for children
          </p>
        </div>

        {/* Card */}
        <div className="glass-card p-8 shadow-xl">
          {mode !== 'forgot' ? (
            <>
              {/* Toggle */}
              <div className="flex rounded-xl overflow-hidden mb-6 bg-[var(--surface-card)] p-1 border border-[var(--border-subtle)]">
                {(['login', 'register'] as const).map(m => (
                  <button 
                    key={m} 
                    onClick={() => { setMode(m); setError(''); setSuccessMsg(''); }}
                    className={`flex-1 py-2.5 text-xs font-bold rounded-lg transition-all duration-200 cursor-pointer ${
                      mode === m 
                        ? 'bg-[var(--brand-primary)] text-white shadow-sm' 
                        : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                    }`}
                  >
                    {m === 'login' ? 'Sign In' : 'Create Account'}
                  </button>
                ))}
              </div>

              <form onSubmit={handleSubmit} className="space-y-4">
                {mode === 'register' && (
                  <div>
                    <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
                      Your Name
                    </label>
                    <input
                      type="text" 
                      required 
                      value={name} 
                      onChange={e => setName(e.target.value)}
                      placeholder="e.g. Priya Kumar"
                      className="input-field"
                    />
                  </div>
                )}
                <div>
                  <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
                    Email
                  </label>
                  <input
                    type="email" 
                    required 
                    value={email} 
                    onChange={e => setEmail(e.target.value)}
                    placeholder="parent@example.com"
                    className="input-field"
                  />
                </div>
                <div>
                  <div className="flex justify-between items-center mb-1.5">
                    <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider">
                      Password
                    </label>
                    {mode === 'login' && (
                      <button
                        type="button"
                        onClick={() => {
                          setMode('forgot');
                          setForgotStage('request');
                          setForgotEmail(email);
                          setError('');
                          setSuccessMsg('');
                        }}
                        className="text-xs font-semibold text-[var(--brand-primary)] hover:underline cursor-pointer"
                      >
                        Forgot Password?
                      </button>
                    )}
                  </div>
                  <input
                    type="password" 
                    required 
                    value={password} 
                    onChange={e => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="input-field"
                  />
                </div>

                {error && (
                  <div className="text-rose-500 text-xs text-center px-4 py-2.5 rounded-xl border border-rose-500/20 bg-rose-500/10 font-medium">
                    {error}
                  </div>
                )}

                {successMsg && (
                  <div className="text-emerald-500 text-xs text-center px-4 py-2.5 rounded-xl border border-emerald-500/20 bg-emerald-500/10 font-medium">
                    {successMsg}
                  </div>
                )}

                <button 
                  type="submit" 
                  disabled={loading}
                  className="w-full btn-primary !py-3 font-bold text-sm shadow-md mt-2"
                >
                  {loading ? 'Please wait...' : mode === 'login' ? 'Sign In →' : 'Create Account →'}
                </button>
              </form>
            </>
          ) : (
            <div>
              {/* Forgot Password Header */}
              <div className="flex items-center gap-2 mb-6">
                <button
                  type="button"
                  onClick={() => {
                    setMode('login');
                    setError('');
                    setSuccessMsg('');
                  }}
                  className="p-1.5 rounded-xl hover:bg-[var(--surface-card)] text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors"
                >
                  <ArrowLeft className="w-5 h-5" />
                </button>
                <div className="flex items-center gap-2">
                  <KeyRound className="w-5 h-5 text-[var(--brand-primary)]" />
                  <h2 className="text-lg font-bold">Reset Password</h2>
                </div>
              </div>

              {forgotStage === 'request' ? (
                <form onSubmit={handleRequestResetCode} className="space-y-4">
                  <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
                    Enter the email address registered with your parent account. We'll generate a 6-digit verification code to reset your password.
                  </p>

                  <div>
                    <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
                      Parent Email
                    </label>
                    <input
                      type="email"
                      required
                      value={forgotEmail}
                      onChange={e => setForgotEmail(e.target.value)}
                      placeholder="parent@example.com"
                      className="input-field"
                      autoFocus
                    />
                  </div>

                  {error && (
                    <div className="text-rose-500 text-xs text-center px-4 py-2.5 rounded-xl border border-rose-500/20 bg-rose-500/10 font-medium">
                      {error}
                    </div>
                  )}

                  <button
                    type="submit"
                    disabled={loading || !forgotEmail}
                    className="w-full btn-primary !py-3 font-bold text-sm shadow-md mt-2"
                  >
                    {loading ? 'Sending Code...' : 'Get Verification Code →'}
                  </button>

                  <div className="text-center pt-2">
                    <button
                      type="button"
                      onClick={() => setMode('login')}
                      className="text-xs text-[var(--text-muted)] hover:text-[var(--text-primary)] underline cursor-pointer"
                    >
                      Back to Sign In
                    </button>
                  </div>
                </form>
              ) : (
                <form onSubmit={handleResetPassword} className="space-y-4">
                  <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-xs text-emerald-400 flex items-start gap-2">
                    <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
                    <div>
                      <span>Verification code generated for <strong>{forgotEmail}</strong>.</span>
                      {codeHint && (
                        <div className="mt-1 font-mono font-bold text-amber-300">
                          Your code: {codeHint}
                        </div>
                      )}
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
                      6-Digit Code
                    </label>
                    <input
                      type="text"
                      required
                      maxLength={6}
                      value={resetCode}
                      onChange={e => setResetCode(e.target.value)}
                      placeholder="123456"
                      className="input-field font-mono tracking-widest text-center text-lg"
                      autoFocus
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
                      New Password
                    </label>
                    <input
                      type="password"
                      required
                      minLength={6}
                      value={newPassword}
                      onChange={e => setNewPassword(e.target.value)}
                      placeholder="At least 6 characters"
                      className="input-field"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-[var(--text-secondary)] uppercase tracking-wider mb-1.5">
                      Confirm New Password
                    </label>
                    <input
                      type="password"
                      required
                      minLength={6}
                      value={confirmPassword}
                      onChange={e => setConfirmPassword(e.target.value)}
                      placeholder="Repeat new password"
                      className="input-field"
                    />
                  </div>

                  {error && (
                    <div className="text-rose-500 text-xs text-center px-4 py-2.5 rounded-xl border border-rose-500/20 bg-rose-500/10 font-medium">
                      {error}
                    </div>
                  )}

                  {successMsg && (
                    <div className="text-emerald-500 text-xs text-center px-4 py-2.5 rounded-xl border border-emerald-500/20 bg-emerald-500/10 font-medium">
                      {successMsg}
                    </div>
                  )}

                  <button
                    type="submit"
                    disabled={loading || !resetCode || !newPassword || !confirmPassword}
                    className="w-full btn-primary !py-3 font-bold text-sm shadow-md mt-2"
                  >
                    {loading ? 'Updating Password...' : 'Save New Password & Log In'}
                  </button>

                  <div className="flex justify-between items-center text-xs pt-2">
                    <button
                      type="button"
                      onClick={() => setForgotStage('request')}
                      className="text-[var(--text-muted)] hover:text-[var(--text-primary)] cursor-pointer"
                    >
                      Resend Code
                    </button>
                    <button
                      type="button"
                      onClick={() => setMode('login')}
                      className="text-[var(--text-muted)] hover:text-[var(--text-primary)] underline cursor-pointer"
                    >
                      Back to Sign In
                    </button>
                  </div>
                </form>
              )}
            </div>
          )}

          <p className="text-center text-xs text-[var(--text-muted)] mt-6">
            Your child's learning, your control. Safe. Ad-free. Focused.
          </p>
        </div>

        {/* Features */}
        <div className="grid grid-cols-3 gap-3 mt-6 text-center">
          {[
            { icon: <Shield className="w-5 h-5 mb-1.5 text-[var(--brand-primary)] mx-auto" />, label: 'Ad-Free\nContent' },
            { icon: <BookOpen className="w-5 h-5 mb-1.5 text-[var(--brand-primary)] mx-auto" />, label: 'Wholesome\nStories' },
            { icon: <BarChart3 className="w-5 h-5 mb-1.5 text-[var(--brand-primary)] mx-auto" />, label: 'Parent\nCurated' },
          ].map((f, i) => (
            <div key={i} className="glass-card p-3 shadow-xs">
              {f.icon}
              <div className="text-[11px] text-[var(--text-secondary)] font-medium whitespace-pre-line leading-tight">{f.label}</div>
            </div>
          ))}
        </div>

        {/* Footer */}
        <footer className="mt-8 text-center text-xs text-[var(--text-muted)] flex items-center justify-center gap-3">
          <span>KidCurate © 2026</span>
          <span>•</span>
          <Link href="/privacy" className="hover:text-[var(--brand-primary)] transition-colors underline">
            Privacy Policy & COPPA
          </Link>
        </footer>
      </div>
    </main>
  );
}
