/**
 * Pure, deterministic, conflict-free dynamic resizing and two-view selection rules
 * shared conceptually between the Web (React/TS) and Android (Jetpack Compose) engines.
 *
 * Designed around 4 simple, non-overlapping rules:
 * 1. Header Proportional Zoom: clamp(viewportWidth / naturalHeaderWidth, 0.70, 1.00)
 * 2. Horizontal Rack Elongation: fit k >= 1 complete sections at 1.00x, elongate by clamp(usableWidth / sumWidth(k), 1.00, 1.30)
 * 3. Conflict-Free Compaction Guard: only enable dynamic fader compaction with keyboard when the viewport
 *    can strictly fit [minCompactedRack (50%) + 8px spacer + VoicingBar + OpenDrawers + minErgonomicKeyboard]
 *    without violating minimum heights. Otherwise, keep fader height 100% static during scroll (free of conflict).
 * 4. Post-Scroll Hysteresis Selection: never mutate fader height mid-scroll; evaluate Top vs Bottom view
 *    exclusively after scroll settles, using separated thresholds (35% down-trigger vs 75% up-trigger).
 */

export interface LayoutMetricsInput {
  viewportWidth: number;
  viewportHeight: number;
  headerHeight: number;
  voicingBarHeight: number;
  arpDrawerHeight: number;
  presetsDrawerHeight: number;
  keyboardHeight: number;
  isKeyboardOpen: boolean;
  isKeyboardFocusMode: boolean;
  isJupiter8: boolean;
  baseRackHeight?: number;
  naturalHeaderWidth?: number;
}

export interface ComputedLayoutPlan {
  /** Proportional zoom factor for the top header bar (0.70 .. 1.00) */
  headerZoomFactor: number;
  /** Horizontal elongation factor for rack sections (1.00 .. 1.30, never < 1.00) */
  faderHorizElongation: number;
  /** Number of complete rack sections that fit horizontally at 1.00x */
  fittingSectionsCount: number;
  /** True only when the resolution can cleanly compact faders + keyboard on 1 screen without conflict */
  canCompactFadersWithKeyboard: boolean;
  /** Effective keyboard focus mode (forced false when !canCompactFadersWithKeyboard or !isKeyboardOpen) */
  effectiveKeyboardFocusMode: boolean;
  /** Vertical fader elongation factor (0.50 .. 0.90 in View 2, or 0.82 .. 2.40 in View 1) */
  faderElongation: number;
  /** Exact target pixel height for the Synth Rack container so View 1 or View 2 fits 100% of the viewport */
  targetRackHeightPx: number;
  /** Whether Area 1 (Header + Rack + Voicing) should lock to 100% viewport height */
  lockArea1ToViewport: boolean;
}

export function clamp(val: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, val));
}

/**
 * Rule 1: Proportional Header Zoom (up to 30% reduction: 0.70 .. 1.00).
 * Never compresses below 0.70; if viewport is narrower, horizontal drag scroll takes over.
 */
export function computeHeaderZoom(viewportWidth: number, naturalHeaderWidth = 1180): number {
  if (viewportWidth <= 0 || naturalHeaderWidth <= 0) return 1.0;
  return clamp(viewportWidth / naturalHeaderWidth, 0.7, 1.0);
}

/**
 * Rule 2: Horizontal Section Elongation (1.00 .. 1.30, NEVER < 1.00).
 * Finds how many complete sections k >= 1 fit at 1.00x base width and elongates up to +30%.
 */
export function computeHorizontalRackElongation(
  usableRackWidth: number,
  cardBaseWidths: readonly number[],
  gapPx = 8
): { fittingSectionsCount: number; faderHorizElongation: number } {
  if (cardBaseWidths.length === 0 || usableRackWidth <= 0 || cardBaseWidths[0] <= 0) {
    return { fittingSectionsCount: 1, faderHorizElongation: 1.0 };
  }
  let k = 1;
  let accW = cardBaseWidths[0];
  for (let i = 1; i < cardBaseWidths.length; i++) {
    const nextW = accW + gapPx + cardBaseWidths[i];
    if (nextW <= usableRackWidth) {
      accW = nextW;
      k = i + 1;
    } else {
      break;
    }
  }
  const neededScale = usableRackWidth / Math.max(1, accW);
  return {
    fittingSectionsCount: k,
    faderHorizElongation: clamp(neededScale, 1.0, 1.3),
  };
}

/**
 * Rule 3: Conflict-Free Dynamic Layout Plan.
 * Guarantees that if a specific screen resolution or open-drawer combination cannot fit
 * both the compacted faders (>= 50% height) and the keyboard without conflict,
 * `canCompactFadersWithKeyboard` becomes `false` and the screen remains free of dynamic
 * mode-switching during scroll.
 */
export function computeLayoutPlan(input: LayoutMetricsInput): ComputedLayoutPlan {
  const vw = Math.max(1, input.viewportWidth);
  const vh = Math.max(1, input.viewportHeight);
  const headerH = Math.max(36, input.headerHeight);
  const voicingH = Math.max(36, input.voicingBarHeight);
  const arpH = Math.max(0, input.arpDrawerHeight);
  const presetsH = Math.max(0, input.presetsDrawerHeight);
  const kbH = Math.max(112, input.keyboardHeight);
  const baseRackH = input.baseRackHeight ?? 268;
  const naturalHeaderW = input.naturalHeaderWidth ?? 1180;

  // Rule 1: Header Zoom
  const headerZoomFactor = computeHeaderZoom(vw, naturalHeaderW);

  // Rule 2: Horizontal Elongation
  const usableRackW = Math.max(320, Math.min(1420, vw - 24));
  const estCardWidths = input.isJupiter8
    ? [170, 580, 74, 340, 165, 380, 110, 170, 250]
    : [120, 340, 74, 290, 120, 210, 110, 170, 250];
  const { fittingSectionsCount, faderHorizElongation } = computeHorizontalRackElongation(
    usableRackW,
    estCardWidths,
    8
  );

  // Rule 3: Strict Feasibility Guard for Two-View Compaction
  const aspectRatio = vw / vh;
  const isPhoneWidescreen = aspectRatio >= 1.85 && vh < 430 && vw < 960;
  const separatorsAndSpacerView2 = 14 + 8; // Top/Bottom rainbow stripes + 8px breathing spacer
  const minCompactedRackPx = Math.round(baseRackH * 0.5);
  const requiredView2Height =
    minCompactedRackPx + separatorsAndSpacerView2 + voicingH + presetsH + arpH + kbH;

  // Only allow dynamic fader compaction if NOT phone widescreen AND viewport height strictly accommodates View 2
  const canCompactFadersWithKeyboard =
    !isPhoneWidescreen && vh >= requiredView2Height && vh >= 340;

  const effectiveKeyboardFocusMode =
    canCompactFadersWithKeyboard && input.isKeyboardOpen && input.isKeyboardFocusMode;

  let faderElongation = 1.0;
  let targetRackHeightPx = baseRackH;

  if (effectiveKeyboardFocusMode) {
    // View 2 (Bottom Focus): Compacted Faders + 8px Spacer + VoicingBar + Open Drawers + Keyboard == 100% of vh
    const availableForCompactedRack =
      vh - separatorsAndSpacerView2 - voicingH - presetsH - arpH - kbH;
    faderElongation = clamp(availableForCompactedRack / baseRackH, 0.5, 0.9);
    targetRackHeightPx = Math.round(baseRackH * faderElongation);
  } else {
    // View 1 (Top Focus / Mandatory Header Safeguard): Header + Faders + VoicingBar (+ inline Presets if open) == 100% of vh
    const separatorsView1 = 14;
    const availableForRackView1 = vh - headerH - voicingH - presetsH - separatorsView1;
    faderElongation = clamp(availableForRackView1 / baseRackH, 0.82, 2.4);
    targetRackHeightPx = Math.round(baseRackH * faderElongation);
  }

  return {
    headerZoomFactor,
    faderHorizElongation,
    fittingSectionsCount,
    canCompactFadersWithKeyboard,
    effectiveKeyboardFocusMode,
    faderElongation,
    targetRackHeightPx,
    lockArea1ToViewport: !effectiveKeyboardFocusMode,
  };
}

export interface PostScrollDecisionInput {
  scrollY: number;
  maxScroll: number;
  headerHeight: number;
  canCompactFadersWithKeyboard: boolean;
  isKeyboardOpen: boolean;
  currentKeyboardFocusMode: boolean;
}

export interface PostScrollDecision {
  nextKeyboardFocusMode: boolean;
  targetScrollY: number | null;
}

/**
 * Rule 4: Deterministic Post-Scroll Selection with Hysteresis.
 * Evaluated ONLY when user scroll/drag has settled (never mid-scroll).
 * - Prevents oscillation/flicker by separating the downward activation threshold (35% of header)
 *   from the upward return threshold (75% of header).
 * - Enforces the Mandatory Main Header Safeguard: whenever returning to the top so the Main Header
 *   is visible (`scrollY <= headerHeight * 0.75` from View 2, or `scrollY < headerHeight * 0.35` from View 1),
 *   it forces `nextKeyboardFocusMode = false` and snaps cleanly to `targetScrollY = 0`.
 */
export function evaluatePostScrollSnap(input: PostScrollDecisionInput): PostScrollDecision {
  const {
    scrollY,
    maxScroll,
    headerHeight,
    canCompactFadersWithKeyboard,
    isKeyboardOpen,
    currentKeyboardFocusMode,
  } = input;

  const headerH = Math.max(36, headerHeight);

  // Case A: Dynamic Two-View Compaction is active and feasible
  if (canCompactFadersWithKeyboard && isKeyboardOpen) {
    const downTriggerPx = Math.max(14, Math.min(48, Math.round(headerH * 0.35)));
    const upTriggerPx = Math.max(downTriggerPx + 8, Math.round(headerH * 0.75));

    if (!currentKeyboardFocusMode) {
      if (scrollY >= downTriggerPx) {
        return {
          nextKeyboardFocusMode: true,
          targetScrollY: headerH,
        };
      }
      if (scrollY > 1) {
        return {
          nextKeyboardFocusMode: false,
          targetScrollY: 0,
        };
      }
      return { nextKeyboardFocusMode: false, targetScrollY: null };
    } else {
      // Currently in View 2 (Bottom / Keyboard Focus Mode)
      if (scrollY <= upTriggerPx) {
        // Main Header is appearing -> Mandatory Header Safeguard returns to View 1 at top (scrollY = 0)
        return {
          nextKeyboardFocusMode: false,
          targetScrollY: 0,
        };
      }
      const clampedHeaderY = Math.min(headerH, Math.max(0, maxScroll));
      if (Math.abs(scrollY - clampedHeaderY) > 2) {
        return {
          nextKeyboardFocusMode: true,
          targetScrollY: clampedHeaderY,
        };
      }
      return { nextKeyboardFocusMode: true, targetScrollY: null };
    }
  }

  // Case B: Static / Free Scroll Mode (Smartphone Widescreen, Keyboard closed, or Edge-case resolution)
  // Never changes fader height (`nextKeyboardFocusMode` is always `false`).
  if (maxScroll <= 6) {
    return { nextKeyboardFocusMode: false, targetScrollY: null };
  }

  // Mandatory Header Safeguard when near the top header, or soft bottom edge snap near maxScroll
  const topSnapZone = Math.min(Math.round(headerH * 0.85), Math.round(maxScroll * 0.35));
  if (scrollY > 1 && scrollY <= topSnapZone) {
    return { nextKeyboardFocusMode: false, targetScrollY: 0 };
  }

  const bottomSnapZone = Math.min(120, Math.max(20, Math.round(maxScroll * 0.25)));
  const distFromBottom = maxScroll - scrollY;
  if (distFromBottom > 1 && distFromBottom <= bottomSnapZone) {
    return { nextKeyboardFocusMode: false, targetScrollY: maxScroll };
  }

  return { nextKeyboardFocusMode: false, targetScrollY: null };
}
