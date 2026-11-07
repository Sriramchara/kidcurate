import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({ subsets: ["latin"], variable: "--font-inter" });

export const metadata: Metadata = {
  title: "KidCurate — Parent-Controlled Learning Platform",
  description:
    "Curate and schedule YouTube educational videos for your children. No ads, no algorithm, no endless scrolling.",
  keywords: ["kids learning", "YouTube for kids", "educational videos", "parental control"],
  manifest: "/manifest.json",
  appleWebApp: {
    capable: true,
    statusBarStyle: "black-translucent",
    title: "KidCurate",
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" data-theme="dark" className={`${inter.variable} dark`} suppressHydrationWarning>
      <head>
        <script
          dangerouslySetInnerHTML={{
            __html: `
              try {
                const t = localStorage.getItem('kc_theme') || 'dark';
                document.documentElement.setAttribute('data-theme', t);
                document.documentElement.classList.remove('light', 'dark');
                document.documentElement.classList.add(t);
                document.documentElement.style.colorScheme = t;

                // Dismiss Netlify HUD / Badge permanently
                localStorage.setItem('nl-hud:public:v1', 'hidden');
                localStorage.setItem('nl-hud:owner-private:v1', 'hidden');
              } catch (e) {}

              // Auto-remove any injected badge frames
              if (typeof window !== 'undefined') {
                const stripBadges = () => {
                  ['nl-badge-frame', 'nl-hud-frame'].forEach(id => {
                    const el = document.getElementById(id);
                    if (el) el.remove();
                  });
                  document.querySelectorAll('iframe[title*="Netlify"]').forEach(el => el.remove());
                };
                window.addEventListener('DOMContentLoaded', stripBadges);
                window.addEventListener('load', stripBadges);
                const obs = new MutationObserver(stripBadges);
                obs.observe(document.documentElement, { childList: true, subtree: true });
              }
            `,
          }}
        />
      </head>
      <body className="antialiased min-h-screen relative overflow-x-hidden" style={{ backgroundColor: 'var(--bg-dark)', color: 'var(--text-primary)' }}>
        {/* Ambient Glassmorphic Aurora Mesh */}
        <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden" aria-hidden="true">
          <div className="absolute -top-40 -left-40 w-[600px] h-[600px] rounded-full bg-gradient-to-br from-indigo-500/15 to-blue-600/10 blur-[140px]" />
          <div className="absolute top-1/4 -right-40 w-[550px] h-[550px] rounded-full bg-gradient-to-br from-amber-400/12 to-orange-400/8 blur-[150px]" />
          <div className="absolute -bottom-40 left-1/3 w-[600px] h-[600px] rounded-full bg-gradient-to-tr from-sky-400/12 to-indigo-500/10 blur-[150px]" />
        </div>
        <div className="relative z-10">
          {children}
        </div>
      </body>
    </html>
  );
}
