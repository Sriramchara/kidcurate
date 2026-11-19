/**
 * KidCurate Web Portal — Video Watch Progress Tracker
 *
 * Ensures accurate watch duration and completion reporting:
 * - Prevents false completions (requires >= 85% duration or natural end event)
 * - Ignores brief click bounces (< 3 seconds)
 * - Deduplicates frequent intervals to eliminate redundant API calls
 */

export interface ProgressState {
  lastReportedSeconds: number;
  completed: boolean;
}

export class WatchProgressTracker {
  private cache: Map<string, ProgressState> = new Map();

  /**
   * Evaluates whether a progress update should be sent to the backend.
   * Returns sanitized watch duration and completion status.
   */
  evaluateProgress(
    childId: string,
    youtubeId: string,
    watchedSeconds: number,
    videoDurationSeconds: number,
    isNaturalEnd: boolean = false
  ): { shouldReport: boolean; completed: boolean; sanitizedSeconds: number } {
    if (!childId || !youtubeId) {
      return { shouldReport: false, completed: false, sanitizedSeconds: 0 };
    }

    const duration = Math.max(videoDurationSeconds || 0, 1);
    const clampedWatched = Math.min(Math.max(Math.floor(watchedSeconds), 0), duration);

    // Filter out accidental clicks / immediate skips (< 3 seconds) unless natural end
    if (clampedWatched < 3 && !isNaturalEnd) {
      return { shouldReport: false, completed: false, sanitizedSeconds: clampedWatched };
    }

    // Determine completion: natural video end or watched at least 85% of total runtime
    const isCompleted = isNaturalEnd || (duration > 0 && clampedWatched >= Math.floor(duration * 0.85));

    const key = `${childId}:${youtubeId}`;
    const previous = this.cache.get(key);

    if (previous) {
      // If already recorded as completed, do not send duplicate completion or downgrade to incomplete
      if (previous.completed && !isCompleted) {
        return { shouldReport: false, completed: true, sanitizedSeconds: clampedWatched };
      }
      if (previous.completed && isCompleted) {
        return { shouldReport: false, completed: true, sanitizedSeconds: clampedWatched };
      }
      // Deduplicate: avoid spamming backend if duration hasn't changed by at least 5 seconds
      if (Math.abs(previous.lastReportedSeconds - clampedWatched) < 5 && previous.completed === isCompleted) {
        return { shouldReport: false, completed: isCompleted, sanitizedSeconds: clampedWatched };
      }
    }

    // Record latest state in tracker cache
    this.cache.set(key, {
      lastReportedSeconds: clampedWatched,
      completed: isCompleted,
    });

    return {
      shouldReport: true,
      completed: isCompleted,
      sanitizedSeconds: clampedWatched,
    };
  }

  resetChild(childId: string): void {
    for (const key of this.cache.keys()) {
      if (key.startsWith(`${childId}:`)) {
        this.cache.delete(key);
      }
    }
  }

  clear(): void {
    this.cache.clear();
  }
}

export const globalProgressTracker = new WatchProgressTracker();
