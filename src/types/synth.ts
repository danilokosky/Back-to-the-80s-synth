export interface HeraParameters {
  // DCO
  DCORange: number;          // 0: 16', 1: 8', 2: 4'
  LFOTrigMode: number;       // 0: Manual, 1: Auto
  DCOPWMMod: number;         // 0: Manual, 1: LFO, 2: Envelope
  DCOPitchModDepth: number;  // 0.0 - 1.0 (LFO pitch mod)
  DCOPWMDepth: number;       // 0.0 - 1.0
  DCOPulseLevel: number;     // 0.0 - 1.0
  DCOSawLevel: number;       // 0.0 - 1.0
  DCOSubLevel: number;       // 0.0 - 1.0
  DCONoiseLevel: number;     // 0.0 - 1.0

  // HPF
  HPF: number;               // 0.0 - 1.0 (4 positions: flat, 225Hz, 520Hz, 1220Hz)

  // VCF
  VCFCutoff: number;         // 0.0 - 1.0
  VCFResonance: number;      // 0.0 - 1.0
  VCFEnv: number;            // -1.0 - 1.0 (Bipolar envelope mod depth)
  VCFLFO: number;            // 0.0 - 1.0
  VCFKey: number;            // 0.0 - 1.0 (Keyboard tracking)
  VCFBendDepth: number;      // 0.0 - 1.0

  // LFO
  LFORate: number;           // 0.0 - 1.0 (0.3Hz - 22Hz)
  LFODelay: number;          // 0.0 - 1.0 (0.0s - 2.7s)

  // VCA
  VCAType: number;           // 0: Envelope, 1: Gate
  VCADepth: number;          // 0.0 - 1.0

  // Envelope
  ENVAttack: number;         // 0.0 - 1.0 (1ms - 3.25s)
  ENVDecay: number;          // 0.0 - 1.0 (2ms - 20s)
  ENVSustain: number;        // 0.0 - 1.0
  ENVRelease: number;        // 0.0 - 1.0 (2ms - 20s)

  // Master Volume (Global, mapped on Keys MIDI Channel)
  MasterVolume: number;      // 0.0 - 1.0

  // Chorus
  ChorusI: number;           // 0 or 1
  ChorusII: number;          // 0 or 1

  // Stereo Echo / Delay Effect
  DelayEnabled: number;      // 0 or 1
  DelayTime: number;         // 0.0 - 1.0 (50ms - 800ms)
  DelayFeedback: number;     // 0.0 - 1.0 (0% - 85%)
  DelayMix: number;          // 0.0 - 1.0 (Dry/Wet)

  // Global Post-Processing Stereo Reverb (Plate / Hall)
  ReverbEnabled: number;     // 0 or 1
  ReverbType: number;        // 0: Plate, 1: Hall
  ReverbTime: number;        // 0.0 - 1.0 (Pre-Delay & Room Size Time)
  ReverbDecay: number;       // 0.0 - 1.0 (RT60 Decay Feedback)
  ReverbMix: number;         // 0.0 - 1.0 (Dry/Wet)

  // Dual-Synth Architecture: Roland Jupiter-8 Extensions
  SynthModel: number;        // 0: JUNO-60 (6-Voice DCO), 1: JUPITER-8 (8-Voice Dual-VCO)
  JP8VoiceMode: number;      // 0: POLY 1, 1: POLY 2, 2: UNISON, 3: SOLO
  JP8LfoWave: number;        // 0: SINE, 1: TRI, 2: SAW, 3: RND (S&H)
  JP8Vco1Wave: number;       // 0: TRI, 1: SAW, 2: PULSE, 3: SQUARE
  JP8Vco2Wave: number;       // 0: SAW, 1: PULSE, 2: SINE/LOW
  JP8Vco2Range: number;      // 0: -1 OCT, 1: 0 (8'), 2: +1 OCT, 3: +5TH (+7st)
  JP8Vco2Detune: number;     // 0.0 - 1.0 (0.5 = 0 cents, 0.0 = -50c, 1.0 = +50c)
  JP8VcoSync: number;        // 0: OFF, 1: ON (Hard Sync VCO-1 <- VCO-2)
  JP8CrossMod: number;       // 0.0 - 1.0 (VCO-2 -> VCO-1 FM Cross-Modulation)
  JP8Vco1Level: number;      // 0.0 - 1.0 (VCO-1 Mixer Level)
  JP8Vco2Level: number;      // 0.0 - 1.0 (VCO-2 Mixer Level)
  JP8PitchEnvDepth: number;  // 0.0 - 1.0 (ENV-1 -> VCO Pitch Mod Depth)
  JP8PitchEnvTarget: number; // 0: VCO-2, 1: VCO-1, 2: BOTH
  JP8VcfSlope: number;       // 0: 24dB/oct (4-Pole), 1: 12dB/oct (2-Pole)
  JP8VcfEnvSource: number;   // 0: ENV-2, 1: ENV-1
  JP8VcaLfoDepth: number;    // 0.0 - 1.0 (LFO -> VCA AM Depth)
  JP8Env1Attack: number;     // 0.0 - 1.0
  JP8Env1Decay: number;      // 0.0 - 1.0
  JP8Env1Sustain: number;    // 0.0 - 1.0
  JP8Env1Release: number;    // 0.0 - 1.0
  JP8Env1Polarity: number;   // 0: NORMAL (+), 1: INVERT (-)
}

export const DEFAULT_PARAMETERS: HeraParameters = {
  DCORange: 1, // 8'
  LFOTrigMode: 1, // Auto
  DCOPWMMod: 1, // LFO
  DCOPitchModDepth: 0.0, // Default pitch mod 0 so notes are strictly in tune
  DCOPWMDepth: 0.3,
  DCOPulseLevel: 0.0,
  DCOSawLevel: 1.0,
  DCOSubLevel: 0.0,
  DCONoiseLevel: 0.0,

  HPF: 0.0,

  VCFCutoff: 0.65,
  VCFResonance: 0.0,
  VCFEnv: 0.0,
  VCFLFO: 0.0,
  VCFKey: 1.0,
  VCFBendDepth: 0.0,

  LFORate: 0.5,
  LFODelay: 0.0,

  VCAType: 0, // Envelope
  VCADepth: 0.6,

  ENVAttack: 0.35,
  ENVDecay: 0.1,
  ENVSustain: 1.0,
  ENVRelease: 0.45,

  MasterVolume: 0.85,

  ChorusI: 1,
  ChorusII: 0,

  DelayEnabled: 0,
  DelayTime: 0.35,
  DelayFeedback: 0.4,
  DelayMix: 0.35,

  ReverbEnabled: 0,
  ReverbType: 0, // 0 = PLATE, 1 = HALL
  ReverbTime: 0.35,
  ReverbDecay: 0.55,
  ReverbMix: 0.30,

  // Dual-Synth Jupiter-8 Defaults
  SynthModel: 0, // 0 = JUNO-60, 1 = JUPITER-8
  JP8VoiceMode: 0, // POLY 1 (8 Voices)
  JP8LfoWave: 0, // SINE
  JP8Vco1Wave: 1, // SAW
  JP8Vco2Wave: 0, // SAW
  JP8Vco2Range: 1, // 0 (Unison 8')
  JP8Vco2Detune: 0.56, // Slight warm analog detune (+6 cents)
  JP8VcoSync: 0, // OFF
  JP8CrossMod: 0.0,
  JP8Vco1Level: 0.85,
  JP8Vco2Level: 0.75,
  JP8PitchEnvDepth: 0.0,
  JP8PitchEnvTarget: 0, // VCO-2
  JP8VcfSlope: 0, // 24dB
  JP8VcfEnvSource: 0, // ENV-2
  JP8VcaLfoDepth: 0.0,
  JP8Env1Attack: 0.05,
  JP8Env1Decay: 0.35,
  JP8Env1Sustain: 0.2,
  JP8Env1Release: 0.3,
  JP8Env1Polarity: 0, // NORMAL (+)
};

export interface VoiceStatus {
  id: number;
  note: number;
  freq: number;
  active: boolean;
  startTime: number;
}
