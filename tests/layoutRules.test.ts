import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  computeHeaderZoom,
  computeHorizontalRackElongation,
  computeLayoutPlan,
  evaluatePostScrollSnap,
} from '../src/utils/layoutRules.ts';

describe('Dynamic Resizing & Conflict-Free Two-View Selection Rules', () => {
  it('Rule 1: scales header proportionally down to 0.70 (30% max reduction) and never below 0.70', () => {
    assert.equal(computeHeaderZoom(1400, 1180), 1.0);
    assert.ok(Math.abs(computeHeaderZoom(1000, 1180) - 1000 / 1180) < 0.001);
    assert.equal(computeHeaderZoom(600, 1180), 0.7);
  });

  it('Rule 2: elongates horizontal fader sections between 1.00 and 1.30 and never compresses (< 1.00)', () => {
    const widths = [120, 340, 74, 290, 120, 210];
    // Fits LFO (120) + gap (8) + DCO (340) + gap (8) + HPF (74) = 550 inside 640px -> elongates by 640/550 = 1.1636x
    const res1 = computeHorizontalRackElongation(640, widths, 8);
    assert.equal(res1.fittingSectionsCount, 3);
    assert.ok(res1.faderHorizElongation > 1.15 && res1.faderHorizElongation <= 1.3);

    // Even if usable width is huge for 3 cards before 4th fits, caps at 1.30
    const res2 = computeHorizontalRackElongation(820, widths, 8);
    assert.equal(res2.fittingSectionsCount, 3);
    assert.equal(res2.faderHorizElongation, 1.3);

    // Never compresses below 1.00
    const res3 = computeHorizontalRackElongation(100, widths, 8);
    assert.equal(res3.faderHorizElongation, 1.0);
  });

  it('Rule 3: disables fader compaction on phone widescreen or cramped edge-case resolutions (free of conflict)', () => {
    // Smartphone widescreen (e.g. 844x390) -> must keep static faders (canCompactFadersWithKeyboard = false)
    const phonePlan = computeLayoutPlan({
      viewportWidth: 844,
      viewportHeight: 390,
      headerHeight: 48,
      voicingBarHeight: 48,
      arpDrawerHeight: 0,
      presetsDrawerHeight: 0,
      keyboardHeight: 180,
      isKeyboardOpen: true,
      isKeyboardFocusMode: true,
      isJupiter8: false,
    });
    assert.equal(phonePlan.canCompactFadersWithKeyboard, false);
    assert.equal(phonePlan.effectiveKeyboardFocusMode, false);

    // Tablet / DeX (e.g. 1280x800) with keyboard open -> enables compaction cleanly
    const tabletPlan = computeLayoutPlan({
      viewportWidth: 1280,
      viewportHeight: 800,
      headerHeight: 54,
      voicingBarHeight: 54,
      arpDrawerHeight: 46,
      presetsDrawerHeight: 0,
      keyboardHeight: 210,
      isKeyboardOpen: true,
      isKeyboardFocusMode: true,
      isJupiter8: false,
    });
    assert.equal(tabletPlan.canCompactFadersWithKeyboard, true);
    assert.equal(tabletPlan.effectiveKeyboardFocusMode, true);
    assert.ok(tabletPlan.faderElongation >= 0.5 && tabletPlan.faderElongation <= 0.9);
  });

  it('Rule 4: post-scroll hysteresis prevents top/bottom oscillation and enforces Mandatory Header Safeguard', () => {
    // Starting at Top View (mode = false): small scroll (10px < 35% of 54px) snaps back to 0
    const stayTop = evaluatePostScrollSnap({
      scrollY: 10,
      maxScroll: 220,
      headerHeight: 54,
      canCompactFadersWithKeyboard: true,
      isKeyboardOpen: true,
      currentKeyboardFocusMode: false,
    });
    assert.equal(stayTop.nextKeyboardFocusMode, false);
    assert.equal(stayTop.targetScrollY, 0);

    // Scrolling past 35% of header (24px >= 19px) switches cleanly to Bottom View (mode = true, targetScrollY = 54)
    const goBottom = evaluatePostScrollSnap({
      scrollY: 24,
      maxScroll: 220,
      headerHeight: 54,
      canCompactFadersWithKeyboard: true,
      isKeyboardOpen: true,
      currentKeyboardFocusMode: false,
    });
    assert.equal(goBottom.nextKeyboardFocusMode, true);
    assert.equal(goBottom.targetScrollY, 54);

    // Once in Bottom View (mode = true) at scrollY = 54, it stays stable at 54 (no oscillation!)
    const stableBottom = evaluatePostScrollSnap({
      scrollY: 54,
      maxScroll: 54,
      headerHeight: 54,
      canCompactFadersWithKeyboard: true,
      isKeyboardOpen: true,
      currentKeyboardFocusMode: true,
    });
    assert.equal(stableBottom.nextKeyboardFocusMode, true);
    assert.equal(stableBottom.targetScrollY, null);

    // When scrolling up so Main Header appears (scrollY = 36 <= 75% of 54), Mandatory Header Safeguard switches to Top View (0)
    const returnTop = evaluatePostScrollSnap({
      scrollY: 36,
      maxScroll: 54,
      headerHeight: 54,
      canCompactFadersWithKeyboard: true,
      isKeyboardOpen: true,
      currentKeyboardFocusMode: true,
    });
    assert.equal(returnTop.nextKeyboardFocusMode, false);
    assert.equal(returnTop.targetScrollY, 0);
  });
});
