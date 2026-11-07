'use client';
import Link from 'next/link';
import { ShieldCheck, ArrowLeft, Lock, CheckCircle2 } from 'lucide-react';
import ThemeToggle from '@/components/ThemeToggle';

export default function PrivacyPolicyPage() {
  return (
    <div className="min-h-screen bg-[var(--bg-dark)] text-[var(--text-primary)] transition-colors duration-200">
      {/* Navbar */}
      <nav 
        className="border-b border-[var(--border-subtle)] px-6 py-4 flex items-center justify-between sticky top-0 z-30 transition-colors"
        style={{ background: 'var(--nav-bg)', backdropFilter: 'blur(16px)' }}
      >
        <div className="flex items-center gap-3">
          <Link 
            href="/dashboard"
            className="flex items-center gap-1.5 text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors py-1 px-2.5 rounded-lg border border-[var(--border-subtle)] bg-[var(--surface-card)] cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Dashboard</span>
          </Link>
          <span className="text-[var(--text-muted)]">/</span>
          <span className="font-extrabold gradient-text">Privacy Policy</span>
        </div>
        <ThemeToggle />
      </nav>

      <div className="max-w-4xl mx-auto px-6 py-12">
        {/* Header */}
        <div className="mb-10 text-center">
          <div className="w-16 h-16 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center mx-auto mb-4 text-emerald-500">
            <ShieldCheck className="w-8 h-8" />
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-[var(--text-primary)] mb-2">
            KidCurate Privacy Policy
          </h1>
          <p className="text-sm text-[var(--text-secondary)]">
            Last Updated: November 2026 • Designed for Families & COPPA Compliant
          </p>
        </div>

        {/* Core Promises Banner */}
        <div className="glass-card p-6 mb-8 border border-emerald-500/30 bg-emerald-500/5">
          <h2 className="text-base font-bold text-[var(--text-primary)] mb-3 flex items-center gap-2">
            <Lock className="w-5 h-5 text-emerald-500" />
            <span>Our Core Commitment to Children's Privacy</span>
          </h2>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs text-[var(--text-secondary)]">
            <div className="flex items-start gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
              <span><strong>Zero Tracking:</strong> No advertising IDs, location data, or behavioral profiling of children.</span>
            </div>
            <div className="flex items-start gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
              <span><strong>No Ads or Feeds:</strong> No recommendation rabbit holes, social comments, or targeted marketing.</span>
            </div>
            <div className="flex items-start gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
              <span><strong>Parent Controlled:</strong> Only authorized parents can curate playlists and manage child profiles.</span>
            </div>
          </div>
        </div>

        {/* Detailed Sections */}
        <div className="glass-card p-8 space-y-8 text-sm leading-relaxed text-[var(--text-secondary)] shadow-sm">
          <section>
            <h3 className="text-lg font-bold text-[var(--text-primary)] mb-2">1. Information We Collect</h3>
            <p className="mb-2">
              KidCurate is designed with strict data minimization principles:
            </p>
            <ul className="list-disc pl-5 space-y-1">
              <li><strong>Parent Account Information:</strong> When a parent registers, we collect the parent's name, email address, and an encrypted password hash. This is used solely to authenticate access to your family dashboard.</li>
              <li><strong>Child Profiles:</strong> Parents can create child profiles specifying a first name (or nickname), age, and an optional 4-digit PIN. We do not require or collect full legal names, birthdates, emails, phone numbers, or photos of children.</li>
              <li><strong>Watch Progress Data:</strong> We store watch time duration and story completion timestamps strictly to display screen time and watch history to parents.</li>
            </ul>
          </section>

          <section>
            <h3 className="text-lg font-bold text-[var(--text-primary)] mb-2">2. Compliance with COPPA & Google Play Families Policy</h3>
            <p>
              KidCurate complies fully with the <strong>Children's Online Privacy Protection Act (COPPA)</strong> and the <strong>Google Play Designed for Families Policy</strong>. We do not condition a child's participation in any learning activity on disclosing more personal information than is reasonably necessary. Parents have full authority to review, update, or permanently delete any profile data at any time from the Parent Zone.
            </p>
          </section>

          <section>
            <h3 className="text-lg font-bold text-[var(--text-primary)] mb-2">3. YouTube Embedded Player & Third-Party Services</h3>
            <p className="mb-2">
              KidCurate embeds video content using official YouTube Player APIs. When a child watches a video:
            </p>
            <ul className="list-disc pl-5 space-y-1">
              <li>Playback occurs inside an official YouTube embedded player sandbox.</li>
              <li>KidCurate does not download, store, or redistribute video content.</li>
              <li>YouTube's Terms of Service and Google Privacy Policy govern the underlying video delivery. For more details, review the <a href="https://www.youtube.com/t/terms" target="_blank" rel="noopener noreferrer" className="text-[var(--brand-primary)] underline">YouTube Terms of Service</a> and <a href="https://policies.google.com/privacy" target="_blank" rel="noopener noreferrer" className="text-[var(--brand-primary)] underline">Google Privacy Policy</a>.</li>
            </ul>
          </section>

          <section>
            <h3 className="text-lg font-bold text-[var(--text-primary)] mb-2">4. Data Sharing & Third Parties</h3>
            <p>
              We <strong>never sell, rent, monetize, or trade</strong> children's or parents' data with advertisers or third parties. We do not use third-party analytics SDKs, trackers, or behavioral ad networks inside the child-facing application.
            </p>
          </section>

          <section>
            <h3 className="text-lg font-bold text-[var(--text-primary)] mb-2">5. Data Retention & Deletion</h3>
            <p>
              Parents retain complete control over their family data. Deleting a child profile instantly removes all associated playlists and watch history from our database. Parents may request full account deletion at any time by contacting support.
            </p>
          </section>

          <section>
            <h3 className="text-lg font-bold text-[var(--text-primary)] mb-2">6. Contact Us</h3>
            <p>
              If you have any questions or concerns regarding our privacy practices or wish to review your child's data, please contact our Data Protection Officer at:
            </p>
            <p className="mt-2 font-mono text-xs text-[var(--brand-primary)]">
              privacy@kidcurate.app
            </p>
          </section>
        </div>
      </div>
    </div>
  );
}

