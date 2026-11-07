'use client';
import React from 'react';
import {
  Compass,
  Sparkles,
  Rocket,
  Waves,
  Flame,
  Smile,
  Shield,
  Palette,
  Telescope,
  Star,
  Heart
} from 'lucide-react';

export interface ThematicAvatarProps {
  name?: string;
  avatar?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
}

export function getAvatarConfig(name: string = '', avatarKey: string = '') {
  const normName = name.trim().toLowerCase();
  const normAvatar = (avatarKey || '').trim().toLowerCase();

  // 1. Explicit matches for existing children
  if (normName === 'aarav' || normAvatar.includes('🦁') || normAvatar.includes('compass') || normAvatar.includes('explorer')) {
    return {
      label: 'Explorer',
      Icon: Compass,
      bgGradient: 'from-amber-400/20 via-orange-400/15 to-amber-500/25 dark:from-amber-500/20 dark:to-orange-600/20',
      borderColor: 'border-amber-400/60 dark:border-amber-400/40',
      iconColor: 'text-amber-500 dark:text-amber-300',
      ringShadow: 'shadow-[0_0_15px_rgba(245,158,11,0.25)]',
    };
  }

  if (normName === 'maya' || normAvatar.includes('🦄') || normAvatar.includes('sparkle') || normAvatar.includes('magic')) {
    return {
      label: 'Sparkle',
      Icon: Sparkles,
      bgGradient: 'from-purple-400/20 via-fuchsia-400/15 to-pink-500/25 dark:from-purple-500/20 dark:to-fuchsia-600/20',
      borderColor: 'border-purple-400/60 dark:border-purple-400/40',
      iconColor: 'text-purple-500 dark:text-purple-300',
      ringShadow: 'shadow-[0_0_15px_rgba(168,85,247,0.25)]',
    };
  }

  if (normName === 'leo' || normAvatar.includes('🚀') || normAvatar.includes('rocket') || normAvatar.includes('space')) {
    return {
      label: 'Rocket',
      Icon: Rocket,
      bgGradient: 'from-sky-400/20 via-blue-400/15 to-indigo-500/25 dark:from-sky-500/20 dark:to-blue-600/20',
      borderColor: 'border-sky-400/60 dark:border-sky-400/40',
      iconColor: 'text-sky-500 dark:text-sky-300',
      ringShadow: 'shadow-[0_0_15px_rgba(14,165,233,0.25)]',
    };
  }

  if (normAvatar.includes('🐬') || normAvatar.includes('waves') || normAvatar.includes('ocean')) {
    return {
      label: 'Ocean',
      Icon: Waves,
      bgGradient: 'from-cyan-400/20 to-teal-500/25 dark:from-cyan-500/20 dark:to-teal-600/20',
      borderColor: 'border-cyan-400/60 dark:border-cyan-400/40',
      iconColor: 'text-cyan-500 dark:text-cyan-300',
      ringShadow: 'shadow-[0_0_15px_rgba(6,182,212,0.25)]',
    };
  }

  if (normAvatar.includes('🦊') || normAvatar.includes('flame') || normAvatar.includes('blaze')) {
    return {
      label: 'Blaze',
      Icon: Flame,
      bgGradient: 'from-orange-400/20 to-red-500/25 dark:from-orange-500/20 dark:to-red-600/20',
      borderColor: 'border-orange-400/60 dark:border-orange-400/40',
      iconColor: 'text-orange-500 dark:text-orange-300',
      ringShadow: 'shadow-[0_0_15px_rgba(249,115,22,0.25)]',
    };
  }

  if (normAvatar.includes('🐘') || normAvatar.includes('shield') || normAvatar.includes('guardian')) {
    return {
      label: 'Guardian',
      Icon: Shield,
      bgGradient: 'from-emerald-400/20 to-green-500/25 dark:from-emerald-500/20 dark:to-green-600/20',
      borderColor: 'border-emerald-400/60 dark:border-emerald-400/40',
      iconColor: 'text-emerald-500 dark:text-emerald-300',
      ringShadow: 'shadow-[0_0_15px_rgba(16,185,129,0.25)]',
    };
  }

  if (normAvatar.includes('🦋') || normAvatar.includes('palette') || normAvatar.includes('artist')) {
    return {
      label: 'Artist',
      Icon: Palette,
      bgGradient: 'from-rose-400/20 to-pink-500/25 dark:from-rose-500/20 dark:to-pink-600/20',
      borderColor: 'border-rose-400/60 dark:border-rose-400/40',
      iconColor: 'text-rose-500 dark:text-rose-300',
      ringShadow: 'shadow-[0_0_15px_rgba(244,63,94,0.25)]',
    };
  }

  if (normAvatar.includes('🌟') || normAvatar.includes('star')) {
    return {
      label: 'Superstar',
      Icon: Star,
      bgGradient: 'from-yellow-400/20 to-amber-500/25 dark:from-yellow-500/20 dark:to-amber-600/20',
      borderColor: 'border-yellow-400/60 dark:border-yellow-400/40',
      iconColor: 'text-yellow-500 dark:text-yellow-300',
      ringShadow: 'shadow-[0_0_15px_rgba(234,179,8,0.25)]',
    };
  }

  if (normAvatar.includes('telescope') || normAvatar.includes('cosmos')) {
    return {
      label: 'Cosmos',
      Icon: Telescope,
      bgGradient: 'from-violet-400/20 to-indigo-500/25 dark:from-violet-500/20 dark:to-indigo-600/20',
      borderColor: 'border-violet-400/60 dark:border-violet-400/40',
      iconColor: 'text-violet-500 dark:text-violet-300',
      ringShadow: 'shadow-[0_0_15px_rgba(139,92,246,0.25)]',
    };
  }

  // Default: Friendly Buddy
  return {
    label: 'Buddy',
    Icon: Smile,
    bgGradient: 'from-amber-400/20 via-yellow-400/15 to-orange-500/25 dark:from-amber-500/20 dark:to-orange-600/20',
    borderColor: 'border-amber-400/60 dark:border-amber-400/40',
    iconColor: 'text-amber-500 dark:text-amber-300',
    ringShadow: 'shadow-[0_0_15px_rgba(245,158,11,0.25)]',
  };
}

export const THEMATIC_BADGES = [
  { key: 'compass', label: 'Explorer', Icon: Compass },
  { key: 'sparkles', label: 'Sparkle', Icon: Sparkles },
  { key: 'rocket', label: 'Rocket', Icon: Rocket },
  { key: 'heart', label: 'Kind Heart', Icon: Heart },
  { key: 'smile', label: 'Happy Pal', Icon: Smile },
  { key: 'waves', label: 'Ocean', Icon: Waves },
  { key: 'flame', label: 'Blaze', Icon: Flame },
  { key: 'shield', label: 'Guardian', Icon: Shield },
  { key: 'palette', label: 'Artist', Icon: Palette },
  { key: 'star', label: 'Superstar', Icon: Star },
];

export default function ThematicAvatar({
  name = '',
  avatar = '',
  size = 'md',
  className = '',
}: ThematicAvatarProps) {
  const config = getAvatarConfig(name, avatar);
  const IconComponent = config.Icon;

  const sizeClasses = {
    sm: 'w-9 h-9 border',
    md: 'w-12 h-12 border-2',
    lg: 'w-14 h-14 border-2',
    xl: 'w-18 h-18 border-2',
  }[size];

  const iconSizes = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-7 h-7',
    xl: 'w-9 h-9',
  }[size];

  return (
    <div
      className={`relative rounded-2xl bg-gradient-to-br ${config.bgGradient} ${config.borderColor} ${config.ringShadow} flex items-center justify-center shrink-0 transition-transform duration-200 ${sizeClasses} ${className}`}
    >
      <IconComponent className={`${iconSizes} ${config.iconColor}`} />
    </div>
  );
}
