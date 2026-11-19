/**
 * KidCurate Web Portal — Video Watch Progress Tracker
 *
 * Ensures accurate watch duration and completion reporting:
 * - Prevents seeking inflation (separates actual watch time from playhead position)
 * - Prevents scrub-to-end exploits (natural end requires >= 70% actual viewing)
 * - Threshold completion requires >= 85% actual viewing
 * - Ignores brief click bounces (< 3 seconds)
 * - Deduplicates frequent intervals to eliminate redundant API calls
 */

export interface ProgressState {
  lastReportedSeconds: number;
  completed: boolean;
  completionType: 'natural' | 'threshold' | 'incomplete';
}

export interface ProgressEvaluation {
  shouldReport: boolean;
  completed: boolean;
  completionType: 'natural' | 'threshold' | 'incomplete';
  sanitizedSeconds: number;
}

export class WatchProgressTracker {
  private cache: Map<string, ProgressState> = new Map();

  /**
   * Evaluates whether a progress update should be sent to the backend.
   * Returns sanitized watch duration, completion status, and completion type.
   */
  evaluateProgress(
    childId: string,
    youtubeId: string,
    actualWatchedSeconds: number,
    videoDurationSeconds: number,
    isNaturalEnd: boolean = false
  ): ProgressEvaluation {
    if (!childId || !youtubeId) {
      return { shouldReport: false, completed: false, completionType: 'incomplete', sanitizedSeconds: 0 };
    }

    const duration = Math.max(videoDurationSeconds || 0, 1);
    const clampedWatched = Math.min(Math.max(Math.floor(actualWatchedSeconds), 0), duration);

    // Filter out accidental clicks / immediate skips (< 3 seconds) unless natural end with valid watch time
    if (clampedWatched < 3 && !isNaturalEnd) {
      return { shouldReport: false, completed: false, completionType: 'incomplete', sanitizedSeconds: clampedWatched };
    }

    // Determine completion:
    // 1. Natural end: Video ended naturally AND child spent at least 70% of duration actively watching (guards against scrubbing to 2s before end)
    // 2. Threshold: Child spent at least 85% of duration actively watching
    let isCompleted = false;
    let compType: 'natural' | 'threshold' | 'incomplete' = 'incomplete';

    if (isNaturalEnd && clampedWatched >= Math.floor(duration * 0.70)) {
      isCompleted = true;
      compType = 'natural';
    } else if (duration > 0 && clampedWatched >= Math.floor(duration * 0.85)) {
      isCompleted = true;
      compType = 'threshold';
    }

    const key = `${childId}:${youtubeId}`;
    const previous = this.cache.get(key);

    if (previous) {
      // If already recorded as completed, do not send duplicate completion or downgrade to incomplete
      if (previous.completed && !isCompleted) {
        return { shouldReport: false, completed: true, completionType: previous.completionType, sanitizedSeconds: clampedWatched };
      }
      if (previous.completed && isCompleted) {
        return { shouldReport: false, completed: true, completionType: previous.completionType, sanitizedSeconds: clampedWatched };
      }
      // Deduplicate: avoid spamming backend if duration hasn't changed by at least 5 seconds
      if (Math.abs(previous.lastReportedSeconds - clampedWatched) < 5 && previous.completed === isCompleted) {
        return { shouldReport: false, completed: isCompleted, completionType: compType, sanitizedSeconds: clampedWatched };
      }
    }

    // Record latest state in tracker cache
    this.cache.set(key, {
      lastReportedSeconds: clampedWatched,
      completed: isCompleted,
      completionType: compType,
    });

    return {
      shouldReport: true,
      completed: isCompleted,
      completionType: compType,
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
