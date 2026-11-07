'use client';
import React, { useEffect, useRef } from 'react';

interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  size: number;
  color: string;
  rotation: number;
  rotationSpeed: number;
  shape: 'ribbon' | 'circle' | 'star';
  twinklePhase: number;
  twinkleSpeed: number;
}

const PALETTE = [
  '#F59E0B', // Amber
  '#10B981', // Emerald
  '#EC4899', // Pink
  '#8B5CF6', // Purple
  '#38BDF8', // Sky
  '#F43F5E', // Rose
  '#FBBF24', // Warm Gold
  '#A78BFA', // Soft Lavender
];

export default function ConfettiCanvas({ duration = 5000 }: { duration?: number }) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };
    window.addEventListener('resize', handleResize);

    // Create 75 cheerful particles
    const count = 75;
    const particles: Particle[] = Array.from({ length: count }, () => {
      const isStar = Math.random() < 0.35;
      const isCircle = !isStar && Math.random() < 0.3;
      return {
        x: Math.random() * width,
        y: -20 - Math.random() * (height * 0.5),
        vx: (Math.random() - 0.5) * 2.5,
        vy: 2.0 + Math.random() * 3.5,
        size: isStar ? 10 + Math.random() * 8 : 8 + Math.random() * 8,
        color: PALETTE[Math.floor(Math.random() * PALETTE.length)],
        rotation: Math.random() * Math.PI * 2,
        rotationSpeed: (Math.random() - 0.5) * 0.08,
        shape: isStar ? 'star' : isCircle ? 'circle' : 'ribbon',
        twinklePhase: Math.random() * Math.PI * 2,
        twinkleSpeed: 0.04 + Math.random() * 0.06,
      };
    });

    let animationFrameId: number;
    const startTime = performance.now();

    function drawStar(c: CanvasRenderingContext2D, cx: number, cy: number, spikes: number, outerRadius: number, innerRadius: number) {
      let rot = (Math.PI / 2) * 3;
      let x = cx;
      let y = cy;
      const step = Math.PI / spikes;

      c.beginPath();
      c.moveTo(cx, cy - outerRadius);
      for (let i = 0; i < spikes; i++) {
        x = cx + Math.cos(rot) * outerRadius;
        y = cy + Math.sin(rot) * outerRadius;
        c.lineTo(x, y);
        rot += step;

        x = cx + Math.cos(rot) * innerRadius;
        y = cy + Math.sin(rot) * innerRadius;
        c.lineTo(x, y);
        rot += step;
      }
      c.lineTo(cx, cy - outerRadius);
      c.closePath();
      c.fill();
    }

    function render(currentTime: number) {
      if (!ctx || !canvas) return;
      ctx.clearRect(0, 0, width, height);

      const elapsed = currentTime - startTime;
      const fadeOut = elapsed > duration - 1000 ? Math.max(0, (duration - elapsed) / 1000) : 1;

      particles.forEach(p => {
        p.x += p.vx + Math.sin(p.y * 0.02) * 0.8;
        p.y += p.vy;
        p.rotation += p.rotationSpeed;
        p.twinklePhase += p.twinkleSpeed;

        // Wrap around bottom
        if (p.y > height + 30) {
          p.y = -20;
          p.x = Math.random() * width;
        }

        ctx.save();
        ctx.translate(p.x, p.y);
        ctx.rotate(p.rotation);

        const twinkleAlpha = (0.6 + 0.4 * Math.sin(p.twinklePhase)) * fadeOut;
        ctx.globalAlpha = twinkleAlpha;
        ctx.fillStyle = p.color;

        if (p.shape === 'star') {
          drawStar(ctx, 0, 0, 5, p.size, p.size * 0.45);
        } else if (p.shape === 'circle') {
          ctx.beginPath();
          ctx.arc(0, 0, p.size / 2, 0, Math.PI * 2);
          ctx.fill();
        } else {
          // 3D Tumble ribbon
          const flipScale = Math.cos(p.rotation * 1.5);
          ctx.scale(1, flipScale);
          ctx.fillRect(-p.size / 2, -p.size / 4, p.size, p.size / 2);
        }

        ctx.restore();
      });

      if (elapsed < duration) {
        animationFrameId = requestAnimationFrame(render);
      } else {
        ctx.clearRect(0, 0, width, height);
      }
    }

    animationFrameId = requestAnimationFrame(render);

    return () => {
      window.removeEventListener('resize', handleResize);
      cancelAnimationFrame(animationFrameId);
    };
  }, [duration]);

  return (
    <canvas
      ref={canvasRef}
      className="fixed inset-0 pointer-events-none z-50 w-full h-full"
      style={{ pointerEvents: 'none' }}
    />
  );
}

