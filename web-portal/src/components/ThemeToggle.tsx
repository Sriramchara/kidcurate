'use client';
import { useEffect, useState } from 'react';
import { Sun, Moon } from 'lucide-react';

export default function ThemeToggle() {
  const [theme, setTheme] = useState<'light' | 'dark'>('dark');
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const stored = (localStorage.getItem('kc_theme') as 'light' | 'dark') ||
      (document.documentElement.getAttribute('data-theme') as 'light' | 'dark') ||
      'dark';
    setTheme(stored);
    applyTheme(stored);

    function onThemeChange(e: Event) {
      const customEvent = e as CustomEvent<'light' | 'dark'>;
      if (customEvent.detail && (customEvent.detail === 'light' || customEvent.detail === 'dark')) {
        setTheme(customEvent.detail);
      }
    }

    function onStorage(e: StorageEvent) {
      if (e.key === 'kc_theme' && (e.newValue === 'light' || e.newValue === 'dark')) {
        setTheme(e.newValue);
        applyTheme(e.newValue);
      }
    }

    window.addEventListener('kc-theme-change', onThemeChange);
    window.addEventListener('storage', onStorage);
    return () => {
      window.removeEventListener('kc-theme-change', onThemeChange);
      window.removeEventListener('storage', onStorage);
    };
  }, []);

  function applyTheme(target: 'light' | 'dark') {
    document.documentElement.setAttribute('data-theme', target);
    document.documentElement.classList.remove('light', 'dark');
    document.documentElement.classList.add(target);
    document.documentElement.style.colorScheme = target;
  }

  function handleSelect(next: 'light' | 'dark') {
    setTheme(next);
    applyTheme(next);
    localStorage.setItem('kc_theme', next);
    window.dispatchEvent(new CustomEvent('kc-theme-change', { detail: next }));
  }

  if (!mounted) {
    return (
      <div className="w-32 h-8 rounded-xl border border-[var(--border-subtle)] bg-[var(--surface-card)]" />
    );
  }

  return (
    <div 
      className="inline-flex items-center rounded-xl p-1 bg-[var(--surface-card)] border border-[var(--border-subtle)] shadow-xs"
      role="group"
      aria-label="Theme Switcher"
    >
      <button
        type="button"
        onClick={() => handleSelect('light')}
        title="Switch to Light Theme"
        className={`flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-bold transition-all cursor-pointer ${
          theme === 'light'
            ? 'bg-amber-500 text-white shadow-xs'
            : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
        }`}
      >
        <Sun className="w-3.5 h-3.5" />
        <span>Light</span>
      </button>

      <button
        type="button"
        onClick={() => handleSelect('dark')}
        title="Switch to Dark Theme"
        className={`flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-bold transition-all cursor-pointer ${
          theme === 'dark'
            ? 'bg-purple-600 text-white shadow-xs'
            : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
        }`}
      >
        <Moon className="w-3.5 h-3.5" />
        <span>Dark</span>
      </button>
    </div>
  );
}
