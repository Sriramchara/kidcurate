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
    assert.equal(res.completionType, 'incomplete');
    assert.equal(res.sanitizedSeconds, 60);
  });

  test('rejects scrub-to-end exploit: natural end with only 10s actual watch time is NOT completed', () => {
    // Child scrubbed to 2 seconds before end of a 300s video and let it finish (10s actual watch time)
    const res = tracker.evaluateProgress('child_1', 'yt_123', 10, 300, true);
    assert.equal(res.shouldReport, true);
    assert.equal(res.completed, false, 'Scrubbed natural end with 10s/300s must NOT be marked completed');
    assert.equal(res.completionType, 'incomplete');
    assert.equal(res.sanitizedSeconds, 10);
  });

  test('marks completion when natural end event fires with legitimate watch time (>= 70%)', () => {
    // Video ended naturally after watching 240 seconds of 300 seconds (80% >= 70%)
    const res = tracker.evaluateProgress('child_1', 'yt_123', 240, 300, true);
    assert.equal(res.shouldReport, true);
    assert.equal(res.completed, true);
    assert.equal(res.completionType, 'natural');
    assert.equal(res.sanitizedSeconds, 240);
  });

  test('marks completion when user watched 85% or more of duration without natural end', () => {
    // 260s / 300s = 86.6% >= 85% threshold
    const res = tracker.evaluateProgress('child_1', 'yt_123', 260, 300, false);
    assert.equal(res.shouldReport, true);
    assert.equal(res.completed, true, '86% watched must be marked completed');
    assert.equal(res.completionType, 'threshold');
  });

  test('seeking near the end does not inflate actual watched seconds', () => {
    // User played for 15s, but scrubbed playhead to 295s: tracker receives actualWatchedSeconds = 15s
    const res = tracker.evaluateProgress('child_1', 'yt_123', 15, 300, false);
    assert.equal(res.shouldReport, true);
    assert.equal(res.completed, false, '15s actual watch time cannot be completed');
    assert.equal(res.sanitizedSeconds, 15);
  });

  test('deduplicates small progress updates (< 5 seconds delta)', () => {
    const first = tracker.evaluateProgress('child_1', 'yt_123', 30, 300, false);
    assert.equal(first.shouldReport, true);

    const second = tracker.evaluateProgress('child_1', 'yt_123', 32, 300, false);
    assert.equal(second.shouldReport, false, 'Redundant 2s heartbeat update must be suppressed');

    const third = tracker.evaluateProgress('child_1', 'yt_123', 40, 300, false);
    assert.equal(third.shouldReport, true, 'Significant progress advance must be reported');
  });

  test('prevents duplicate completion reporting once video is already marked completed', () => {
    const completedRes = tracker.evaluateProgress('child_1', 'yt_123', 280, 300, true);
    assert.equal(completedRes.shouldReport, true);
    assert.equal(completedRes.completed, true);

    const repeatRes = tracker.evaluateProgress('child_1', 'yt_123', 280, 300, true);
    assert.equal(repeatRes.shouldReport, false, 'Repeated completion event must be deduplicated');
  });

  test('clamps watched seconds to video duration and non-negative bounds', () => {
    const clampedMax = tracker.evaluateProgress('child_1', 'yt_123', 9999, 120, false);
    assert.equal(clampedMax.sanitizedSeconds, 120);

    const clampedMin = tracker.evaluateProgress('child_1', 'yt_456', -10, 120, false);
    assert.equal(clampedMin.sanitizedSeconds, 0);
  });
});
