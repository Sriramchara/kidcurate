// Web Audio API & Haptic Feedback Engine for KidCurate
// Zero external asset dependencies; lightweight, instant & offline-ready.

let audioCtx: AudioContext | null = null;

function getAudioContext(): AudioContext | null {
  if (typeof window === 'undefined') return null;
  if (!audioCtx) {
    const AudioContextClass = window.AudioContext || (window as any).webkitAudioContext;
    if (AudioContextClass) {
      audioCtx = new AudioContextClass();
    }
  }
  if (audioCtx && audioCtx.state === 'suspended') {
    audioCtx.resume().catch(() => {});
  }
  return audioCtx;
}

/**
 * Gentle tactile click for numpad digits, keypads, and buttons.
 */
export function playTactileClick() {
  if (typeof window !== 'undefined' && 'vibrate' in navigator) {
    try {
      navigator.vibrate(8);
    } catch {}
  }

  const ctx = getAudioContext();
  if (!ctx) return;

  try {
    const now = ctx.currentTime;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = 'triangle';
    osc.frequency.setValueAtTime(620, now);
    osc.frequency.exponentialRampToValueAtTime(320, now + 0.03);

    gain.gain.setValueAtTime(0.08, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 0.03);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start(now);
    osc.stop(now + 0.03);
  } catch {}
}

/**
 * Playful, cheerful bubble pop when tapping a child avatar or profile card.
 */
export function playPop() {
  if (typeof window !== 'undefined' && 'vibrate' in navigator) {
    try {
      navigator.vibrate(15);
    } catch {}
  }

  const ctx = getAudioContext();
  if (!ctx) return;

  try {
    const now = ctx.currentTime;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = 'sine';
    // Friendly pitch-up bubble pop
    osc.frequency.setValueAtTime(360, now);
    osc.frequency.exponentialRampToValueAtTime(680, now + 0.055);

    gain.gain.setValueAtTime(0.12, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 0.06);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start(now);
    osc.stop(now + 0.06);
  } catch {}
}

/**
 * Magical chime fanfare when the child finishes all videos in their playlist!
 */
export function playCelebrationFanfare() {
  if (typeof window !== 'undefined' && 'vibrate' in navigator) {
    try {
      navigator.vibrate([30, 40, 30, 40, 60]);
    } catch {}
  }

  const ctx = getAudioContext();
  if (!ctx) return;

  try {
    const notes = [
      { freq: 523.25, time: 0.00 }, // C5
      { freq: 659.25, time: 0.12 }, // E5
      { freq: 783.99, time: 0.24 }, // G5
      { freq: 1046.50, time: 0.38 }, // C6
    ];

    const startTime = ctx.currentTime;

    notes.forEach(({ freq, time }) => {
      const noteTime = startTime + time;
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, noteTime);

      gain.gain.setValueAtTime(0.15, noteTime);
      gain.gain.exponentialRampToValueAtTime(0.001, noteTime + 0.45);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start(noteTime);
      osc.stop(noteTime + 0.45);
    });
  } catch {}
}

