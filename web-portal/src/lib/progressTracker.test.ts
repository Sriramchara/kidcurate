import test, { describe, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { WatchProgressTracker } from './progressTracker.ts';

describe('Web Video Playback Progress Reporting — WatchProgressTracker', () => {
  let tracker: WatchProgressTracker;

  beforeEach(() => {
    tracker = new WatchProgressTracker();
  });

  test('ignores brief click/skips under 3 seconds to avoid noise', () => {
    const res = tracker.evaluateProgress('child_1', 'yt_123', 2, 300, false);
    assert.equal(res.shouldReport, false);
  });

  test('records legitimate partial watch progress without false completion', () => {
    // Watched 60 seconds of a 300-second (5-minute) video
    const res = tracker.evaluateProgress('child_1', 'yt_123', 60, 300, false);
    assert.equal(res.shouldReport, true);
    assert.equal(res.completed, false, '60s / 300s must NOT be marked completed');
    assert.equal(res.sanitizedSeconds, 60);
  });

  test('marks completion when natural end event fires', () => {
    // Video ended naturally at 295 seconds
    const res = tracker.evaluateProgress('child_1', 'yt_123', 295, 300, true);
    assert.equal(res.shouldReport, true);
    assert.equal(res.completed, true);
    assert.equal(res.sanitizedSeconds, 295);
  });

  test('marks completion when user watched 85% or more of duration', () => {
    // 260s / 300s = 86.6% >= 85% threshold
    const res = tracker.evaluateProgress('child_1', 'yt_123', 260, 300, false);
    assert.equal(res.shouldReport, true);
    assert.equal(res.completed, true, '86% watched must be marked completed');
  });

  test('deduplicates small progress updates (< 5 seconds delta)', () => {
    // First update: 30 seconds
    const first = tracker.evaluateProgress('child_1', 'yt_123', 30, 300, false);
    assert.equal(first.shouldReport, true);

    // Second update: 32 seconds (only 2s difference, same status)
    const second = tracker.evaluateProgress('child_1', 'yt_123', 32, 300, false);
    assert.equal(second.shouldReport, false, 'Redundant 2s heartbeat update must be suppressed');

    // Third update: 40 seconds (10s difference)
    const third = tracker.evaluateProgress('child_1', 'yt_123', 40, 300, false);
    assert.equal(third.shouldReport, true, 'Significant progress advance must be reported');
  });

  test('prevents duplicate completion reporting once video is already marked completed', () => {
    // Video marked completed
    const completedRes = tracker.evaluateProgress('child_1', 'yt_123', 300, 300, true);
    assert.equal(completedRes.shouldReport, true);
    assert.equal(completedRes.completed, true);

    // Subsequent call for the same video
    const repeatRes = tracker.evaluateProgress('child_1', 'yt_123', 300, 300, true);
    assert.equal(repeatRes.shouldReport, false, 'Repeated completion event must be deduplicated');
  });

  test('clamps watched seconds to video duration and non-negative bounds', () => {
    const clampedMax = tracker.evaluateProgress('child_1', 'yt_123', 9999, 120, false);
    assert.equal(clampedMax.sanitizedSeconds, 120);

    const clampedMin = tracker.evaluateProgress('child_1', 'yt_456', -10, 120, false);
    assert.equal(clampedMin.sanitizedSeconds, 0);
  });
});
