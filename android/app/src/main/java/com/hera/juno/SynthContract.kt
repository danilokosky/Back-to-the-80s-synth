package com.hera.juno

import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import kotlin.math.roundToInt

object NativeSynthBridge {
    init {
        try {
            System.loadLibrary("hera_juno")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun initEngine(): Boolean
    external fun stopEngine()
    external fun noteOn(note: Int, velocity: Float)
    external fun noteOff(note: Int)
    external fun allNotesOff()
    external fun setParameter(paramId: Int, value: Float)
    external fun getParameter(paramId: Int): Float
    external fun setPitchBend(bend: Float, modWheel: Float)
    external fun setArpConfig(enabled: Boolean, mode: Int, range: Int, bpm: Float, rateDiv: Int)
    external fun getActiveVoicesCount(): Int
    external fun getWaveformBuffer(outArray: FloatArray)
}

enum class MidiSection(val key: String, val title: String, val subtitle: String) {
    KEYS("keys", "Teclas & Volume Master", "Notas, PitchBend, Master, VCA e Levels DCO"),
    LFO("lfo", "LFO (Vibrato)", "Seção LFO (MIDI Learn / CC)"),
    DCO("dco", "DCO / VCO (Oscilador)", "Seção DCO (MIDI Learn / CC)"),
    HPF("hpf", "HPF (Filtro Passa-Alta)", "Seção HPF (MIDI Learn / CC)"),
    VCF("vcf", "VCF (Filtro Principal)", "Seção VCF (MIDI Learn / CC)"),
    VCA("vca", "VCA (Amplificador)", "Seção VCA (MIDI Learn / CC)"),
    ENV("env", "ENV (Envelope ADSR)", "Seção ENV (MIDI Learn / CC)"),
    CHORUS("chorus", "Chorus BBD", "Seção CHORUS (MIDI Learn / CC)"),
    DELAY("delay", "Echo Delay", "Seção DELAY (MIDI Learn / CC)"),
    REVERB("reverb", "Reverb Global", "Seção REVERB (MIDI Learn / CC)"),
    ARP_PRESETS("arpPresets", "Arpejador & Presets", "Seção ARP/PRG (MIDI Learn / CC)")
}

enum class SynthParam(
    val id: Int,
    val key: String,
    val label: String,
    val section: MidiSection,
    val defaultVal: Float,
    val isSwitch: Boolean = false
) {
    DCORange(0, "DCORange", "RANGE", MidiSection.DCO, 1.0f, true),
    DCOSawLevel(1, "DCOSawLevel", "SAW", MidiSection.DCO, 1.0f),
    DCOPulseLevel(2, "DCOPulseLevel", "PULSE", MidiSection.DCO, 0.0f),
    DCOSubLevel(3, "DCOSubLevel", "SUB", MidiSection.DCO, 0.0f),
    DCONoiseLevel(4, "DCONoiseLevel", "NOISE", MidiSection.DCO, 0.0f),
    DCOPWMDepth(5, "DCOPWMDepth", "PWM", MidiSection.DCO, 0.3f),
    DCOPWMMod(6, "DCOPWMMod", "PWM MOD", MidiSection.DCO, 1.0f, true),
    DCOPitchModDepth(7, "DCOPitchModDepth", "LFO", MidiSection.DCO, 0.0f),
    HPF(8, "HPF", "FREQ", MidiSection.HPF, 0.0f),
    VCFCutoff(9, "VCFCutoff", "FREQ", MidiSection.VCF, 0.7f),
    VCFResonance(10, "VCFResonance", "RES", MidiSection.VCF, 0.0f),
    VCFEnv(11, "VCFEnv", "ENV", MidiSection.VCF, 0.0f),
    VCFLFO(12, "VCFLFO", "LFO", MidiSection.VCF, 0.0f),
    VCFKey(13, "VCFKey", "KYBD", MidiSection.VCF, 1.0f),
    VCFBendDepth(14, "VCFBendDepth", "BEND", MidiSection.VCF, 0.0f),
    VCAType(15, "VCAType", "TYPE", MidiSection.VCA, 0.0f, true),
    VCADepth(16, "VCADepth", "LEVEL", MidiSection.VCA, 0.5f),
    ENVAttack(17, "ENVAttack", "A", MidiSection.ENV, 0.4f),
    ENVDecay(18, "ENVDecay", "D", MidiSection.ENV, 0.0f),
    ENVSustain(19, "ENVSustain", "S", MidiSection.ENV, 1.0f),
    ENVRelease(20, "ENVRelease", "R", MidiSection.ENV, 0.45f),
    LFORate(21, "LFORate", "RATE", MidiSection.LFO, 0.6f),
    LFODelay(22, "LFODelay", "VIB DEL", MidiSection.LFO, 0.0f),
    LFOTrigMode(23, "LFOTrigMode", "LFO TRIG", MidiSection.LFO, 1.0f, true),
    LFOManualTrig(24, "LFOManualTrig", "MAN TRIG", MidiSection.LFO, 0.0f, true),
    ChorusI(25, "ChorusI", "CHORUS I", MidiSection.CHORUS, 1.0f, true),
    ChorusII(26, "ChorusII", "CHORUS II", MidiSection.CHORUS, 0.0f, true),
    DelayEnabled(27, "DelayEnabled", "DELAY ON", MidiSection.DELAY, 0.0f, true),
    DelayTime(28, "DelayTime", "TIME", MidiSection.DELAY, 0.35f),
    DelayFeedback(29, "DelayFeedback", "F-BACK", MidiSection.DELAY, 0.35f),
    DelayMix(30, "DelayMix", "MIX", MidiSection.DELAY, 0.25f),
    ReverbEnabled(31, "ReverbEnabled", "REVERB ON", MidiSection.REVERB, 0.0f, true),
    ReverbType(32, "ReverbType", "MODE", MidiSection.REVERB, 0.0f, true),
    ReverbTime(33, "ReverbTime", "TIME", MidiSection.REVERB, 0.35f),
    ReverbDecay(34, "ReverbDecay", "DECAY", MidiSection.REVERB, 0.55f),
    ReverbMix(35, "ReverbMix", "MIX", MidiSection.REVERB, 0.25f),
    MasterVolume(36, "MasterVolume", "MASTER", MidiSection.KEYS, 0.8f),
    PresetPrev(37, "PresetPrev", "PRG ◀", MidiSection.ARP_PRESETS, 0.0f, true),
    PresetNext(38, "PresetNext", "PRG ▶", MidiSection.ARP_PRESETS, 0.0f, true),
    PresetSelect(39, "PresetSelect", "CUSTOM PRG", MidiSection.ARP_PRESETS, 0.0f, true),
    PitchWheel(40, "PitchWheel", "PITCH", MidiSection.KEYS, 0.0f),
    ModWheel(41, "ModWheel", "MOD", MidiSection.KEYS, 0.0f),
    SynthModel(42, "SynthModel", "MODEL", MidiSection.KEYS, 0.0f, true),
    JP8VoiceMode(43, "JP8VoiceMode", "ASSIGN", MidiSection.DCO, 0.0f, true),
    JP8LfoWave(44, "JP8LfoWave", "LFO WV", MidiSection.LFO, 0.0f, true),
    JP8Vco1Wave(45, "JP8Vco1Wave", "VCO1 WV", MidiSection.DCO, 1.0f, true),
    JP8Vco2Wave(46, "JP8Vco2Wave", "VCO2 WV", MidiSection.DCO, 0.0f, true),
    JP8Vco2Range(47, "JP8Vco2Range", "VCO2 OCT", MidiSection.DCO, 1.0f, true),
    JP8Vco2Detune(48, "JP8Vco2Detune", "DETUNE", MidiSection.DCO, 0.14f),
    JP8VcoSync(49, "JP8VcoSync", "SYNC", MidiSection.DCO, 0.0f, true),
    JP8CrossMod(50, "JP8CrossMod", "X-MOD", MidiSection.DCO, 0.0f),
    JP8Vco1Level(51, "JP8Vco1Level", "VCO-1", MidiSection.DCO, 0.90f),
    JP8Vco2Level(52, "JP8Vco2Level", "VCO-2", MidiSection.DCO, 0.75f),
    JP8PitchEnvDepth(53, "JP8PitchEnvDepth", "P-ENV1", MidiSection.DCO, 0.0f),
    JP8PitchEnvTarget(54, "JP8PitchEnvTarget", "P-DEST", MidiSection.DCO, 0.0f, true),
    JP8VcfSlope(55, "JP8VcfSlope", "SLOPE", MidiSection.VCF, 0.0f, true),
    JP8VcfEnvSource(56, "JP8VcfEnvSource", "ENV SRC", MidiSection.VCF, 0.0f, true),
    JP8VcaLfoDepth(57, "JP8VcaLfoDepth", "LFO AM", MidiSection.VCA, 0.0f),
    JP8Env1Attack(58, "JP8Env1Attack", "A1", MidiSection.ENV, 0.01f),
    JP8Env1Decay(59, "JP8Env1Decay", "D1", MidiSection.ENV, 0.35f),
    JP8Env1Sustain(60, "JP8Env1Sustain", "S1", MidiSection.ENV, 0.0f),
    JP8Env1Release(61, "JP8Env1Release", "R1", MidiSection.ENV, 0.25f),
    JP8Env1Polarity(62, "JP8Env1Polarity", "POL1", MidiSection.ENV, 0.0f, true);

    companion object {
        private val byKey = entries.associateBy { it.key }
        fun fromKey(key: String): SynthParam? = byKey[key]
    }
}

data class HeraPreset(
    val id: Int,
    val name: String,
    val isUserPreset: Boolean = false,
    val values: Map<SynthParam, Float>
)

object PresetRepository {
    private const val PREFS_NAME = "hera_user_presets_prefs"
    private const val KEY_USER_PRESETS_XML = "user_presets_xml_list"

    private fun p(
        id: Int,
        name: String,
        chorusI: Float,
        chorusII: Float,
        noise: Float,
        pwmDepth: Float,
        pwmMod: Float,
        pitchMod: Float,
        pulse: Float,
        range: Float,
        saw: Float,
        sub: Float,
        a: Float,
        d: Float,
        r: Float,
        s: Float,
        hpf: Float,
        lfoDelay: Float,
        lfoRate: Float,
        lfoTrig: Float,
        vcaDepth: Float,
        vcaType: Float,
        vcfBend: Float,
        vcfCutoff: Float,
        vcfEnv: Float,
        vcfKey: Float,
        vcfLfo: Float,
        vcfRes: Float
    ): HeraPreset {
        val normRange = when {
            range <= 0.6f -> 0.0f
            range <= 1.4f -> 1.0f
            else -> 2.0f
        }
        return HeraPreset(
            id = id,
            name = name,
            isUserPreset = false,
            values = mapOf(
                SynthParam.ChorusI to chorusI,
                SynthParam.ChorusII to chorusII,
                SynthParam.DCONoiseLevel to noise,
                SynthParam.DCOPWMDepth to pwmDepth,
                SynthParam.DCOPWMMod to pwmMod,
                SynthParam.DCOPitchModDepth to pitchMod,
                SynthParam.DCOPulseLevel to pulse,
                SynthParam.DCORange to normRange,
                SynthParam.DCOSawLevel to saw,
                SynthParam.DCOSubLevel to sub,
                SynthParam.ENVAttack to a,
                SynthParam.ENVDecay to d,
                SynthParam.ENVRelease to r,
                SynthParam.ENVSustain to s,
                SynthParam.HPF to hpf,
                SynthParam.LFODelay to lfoDelay,
                SynthParam.LFORate to lfoRate,
                SynthParam.LFOTrigMode to lfoTrig,
                SynthParam.VCADepth to vcaDepth,
                SynthParam.VCAType to vcaType,
                SynthParam.VCFBendDepth to vcfBend,
                SynthParam.VCFCutoff to vcfCutoff,
                SynthParam.VCFEnv to vcfEnv.coerceIn(0f, 1f),
                SynthParam.VCFKey to vcfKey,
                SynthParam.VCFLFO to vcfLfo,
                SynthParam.VCFResonance to vcfRes,
                SynthParam.DelayEnabled to 0f,
                SynthParam.DelayTime to 0.35f,
                SynthParam.DelayFeedback to 0.35f,
                SynthParam.DelayMix to 0.25f,
                SynthParam.ReverbEnabled to 0f,
                SynthParam.ReverbType to 0f,
                SynthParam.ReverbTime to 0.35f,
                SynthParam.ReverbDecay to 0.55f,
                SynthParam.ReverbMix to 0.25f
            )
        )
    }

    val FACTORY_PRESETS: List<HeraPreset> = listOf(
        p(0, "Strings 1", 1f, 0f, 0f, 0.3f, 1f, 0f, 0f, 1f, 1f, 0f, 0.4f, 0f, 0.45f, 1f, 0f, 0f, 0.6f, 1f, 0.5f, 0f, 0f, 0.7f, 0f, 1f, 0f, 0f),
        p(1, "Strings 2", 0f, 1f, 0f, 0.6f, 1f, 0f, 1f, 1f, 1f, 0f, 0.4f, 0f, 0.45f, 1f, 0f, 0f, 0.4f, 1f, 0.3f, 0f, 0f, 0.7f, 0f, 1f, 0f, 0f),
        p(2, "Strings 3", 0f, 1f, 0f, 0.7f, 1f, 0f, 1f, 1f, 1f, 1f, 0.3f, 0f, 0.6f, 1f, 0f, 0.8f, 0.3f, 0f, 0.3f, 0f, 0f, 0.5f, 0f, 1f, 0f, 0f),
        p(3, "Organ 1", 1f, 0f, 0f, 0.5f, 0f, 0f, 1f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0.8f, 0.2f, 1f, 0.5f, 1f, 0f, 0.4f, 0.45f, 1f, 0f, 0.6f),
        p(4, "Organ 2", 1f, 0f, 0f, 0.55f, 1f, 0f, 1f, 1f, 0f, 0.8f, 0f, 0.1f, 0.1f, 0f, 0f, 0.4f, 0.5f, 1f, 0.5f, 1f, 0f, 0.35f, 0.4f, 1f, 0f, 0.55f),
        p(5, "Organ 3", 0f, 1f, 0f, 0.55f, 1f, 0f, 1f, 2f, 0f, 0.8f, 0f, 0.1f, 0.1f, 0f, 0f, 0.4f, 0.5f, 1f, 0.5f, 1f, 0f, 0.35f, 0.35f, 1f, 0f, 0.55f),
        p(6, "Brass", 1f, 0f, 0f, 0f, 0f, 0.15f, 0f, 1f, 1f, 0f, 0.25f, 0.4f, 0.2f, 0.6f, 0f, 0.65f, 0.5f, 1f, 0.7f, 0f, 0f, 0f, 0.85f, 0.4f, 0f, 0f),
        p(7, "Phase Brass", 1f, 0f, 0f, 1f, 2f, 0f, 1f, 1f, 1f, 0f, 0.2f, 0.4f, 0.3f, 0.4f, 0f, 0f, 0.6f, 1f, 0.4f, 1f, 0f, 0.3f, 0.55f, 1f, 0f, 0.1f),
        p(8, "Piano 1", 0f, 0f, 0f, 0.6f, 0f, 0.45f, 1f, 1f, 0f, 0f, 0f, 0.8f, 0.3f, 0.15f, 0f, 0.3f, 0.6f, 0f, 0.7f, 0f, 0f, 0.1f, 0.7f, 0.4f, 0f, 0f),
        p(9, "Piano 2", 0f, 0f, 0f, 0.4f, 0f, 0f, 1f, 2f, 0f, 0.45f, 0f, 0.75f, 0.35f, 0f, 0f, 0f, 0.4f, 1f, 0.8f, 0f, 0f, 0.35f, 0.25f, 0.8f, 0.2f, 0f),
        p(10, "Celesta", 0f, 0f, 0f, 0.5f, 2f, 0f, 1f, 1f, 1f, 0f, 0f, 0.65f, 0.55f, 0.2f, 0.35f, 0.6f, 0.35f, 1f, 0.6f, 0f, 0f, 0.35f, 0f, 1f, 0f, 0.8f),
        p(11, "Mellow Piano", 1f, 0f, 0f, 0.5f, 0f, 0f, 1f, 1f, 0f, 0f, 0.1f, 0.75f, 0.85f, 0.2f, 0f, 0f, 0.5f, 0f, 0.7f, 0f, 0f, 0.3f, 0.25f, 0.9f, 0.1f, 0f),
        p(12, "Harpsichord 1", 1f, 0f, 0f, 0.3f, 0f, 0f, 1f, 2f, 0f, 0.7f, 0f, 0.6f, 0.25f, 0.35f, 0.35f, 0.4f, 0.5f, 1f, 0.4f, 0f, 0f, 0.3f, 0.5f, 0.7f, 0f, 0f),
        p(13, "Harpsichord 2", 0f, 1f, 0f, 0.2f, 0f, 0f, 1f, 2f, 0f, 0.85f, 0f, 0.5f, 0.5f, 0.15f, 0.35f, 0.6f, 0.55f, 1f, 0.5f, 0f, 0f, 0.5f, 0.3f, 1f, 0f, 0.25f),
        p(14, "Guitar", 0f, 0f, 0f, 0.6f, 0f, 0f, 1f, 1f, 0f, 0f, 0f, 0.55f, 0.65f, 0.35f, 0.65f, 0.6f, 0.6f, 1f, 0.9f, 0f, 0f, 0.3f, 0.45f, 0.5f, 0.15f, 0f),
        p(15, "Synthesizer Harp", 1f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 1f, 0f, 0f, 0.55f, 0.5f, 0.3f, 0f, 0.8f, 0.3f, 0f, 0.6f, 0f, 0f, 0.3f, 0.5f, 0.8f, 0f, 0f),
        p(16, "Bass 1", 1f, 0f, 0f, 0.5f, 0f, 0f, 1f, 0f, 1f, 0.3f, 0f, 0.4f, 0.25f, 0.1f, 0f, 0.6f, 0.5f, 1f, 0.5f, 1f, 0f, 0.3f, 0.35f, 0f, 0f, 0.25f),
        p(17, "Bass 2", 1f, 0f, 0f, 0.5f, 0f, 0f, 1f, 0f, 1f, 0f, 0f, 0.3f, 0.25f, 0.35f, 0f, 0.6f, 0.5f, 1f, 0.4f, 1f, 0f, 0.3f, 0.45f, 0.5f, 0f, 0.5f),
        p(18, "Clavichord 1", 1f, 0f, 0f, 0.8f, 0f, 0.4f, 1f, 0f, 0f, 0f, 0f, 0.5f, 0.15f, 0.35f, 0f, 0.25f, 0.6f, 0f, 0.7f, 0f, 0f, 0f, 0.8f, 0.6f, 0f, 0.3f),
        p(19, "Clavichord 2", 0f, 0f, 0f, 0.8f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0.45f, 0.2f, 0.2f, 0.35f, 0f, 0.1f, 1f, 1f, 0f, 0f, 0.55f, 0.2f, 0.7f, 0.25f, 0.7f),
        p(20, "Pizzicato Sound 1", 1f, 0f, 0f, 0.35f, 0f, 0f, 1f, 1f, 0f, 0f, 0f, 0.2f, 0.55f, 0.35f, 0f, 0.6f, 0.5f, 1f, 0.8f, 0f, 0f, 0.45f, 0.3f, 1f, 0.3f, 0.3f),
        p(21, "Pizzicato Sound 2", 0f, 1f, 0f, 0.2f, 0f, 0f, 1f, 2f, 0f, 0.3f, 0f, 0.3f, 0.4f, 0.3f, 0f, 0.6f, 0.5f, 1f, 0.6f, 0f, 0f, 0.5f, 0.3f, 1f, 0f, 0.3f),
        p(22, "Xylophone", 0f, 0f, 0f, 0.5f, 0f, 0f, 0f, 2f, 0f, 1f, 0f, 0.35f, 0.35f, 0f, 0.35f, 0f, 0.5f, 1f, 1f, 0f, 0f, 0.4f, 0.3f, 0.6f, 0f, 0.5f),
        p(23, "Glockenspiel", 0f, 0f, 0f, 0f, 0f, 0f, 1f, 2f, 0f, 0f, 0f, 0.3f, 0.5f, 0.25f, 0.35f, 0f, 0.5f, 1f, 0.9f, 0f, 0f, 0.45f, 0.3f, 0.6f, 0f, 0.5f),
        p(24, "Violine", 0f, 0f, 0f, 0f, 1f, 0.2f, 0f, 1f, 1f, 0f, 0.4f, 0f, 0.4f, 1f, 0.35f, 0.6f, 0.6f, 1f, 0.7f, 0f, 0f, 0.65f, 0f, 1f, 0f, 0f),
        p(25, "Trumpet", 0f, 0f, 0f, 0f, 0f, 0.15f, 0f, 1f, 1f, 0f, 0.25f, 0.4f, 0.2f, 0.6f, 0f, 0.65f, 0.25f, 1f, 0.7f, 0f, 0f, 0f, 0.85f, 0.4f, 0f, 0f),
        p(26, "Horn", 0f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 1f, 0f, 0.4f, 0.5f, 0.3f, 0.6f, 0f, 0.7f, 0.25f, 1f, 0.7f, 0f, 0f, 0.2f, 0.55f, 0.4f, 0.2f, 0f),
        p(27, "Tuba", 0f, 0f, 0f, 0f, 0f, 0.15f, 0f, 0f, 1f, 0f, 0.3f, 0.4f, 0.3f, 0.4f, 0f, 0.7f, 0.25f, 1f, 1f, 0f, 0f, 0.15f, 0.6f, 0.4f, 0f, 0f),
        p(28, "Flute", 0f, 0f, 0.15f, 0f, 0f, 0f, 0f, 2f, 1f, 0f, 0.2f, 0.6f, 0.25f, 0.5f, 0.35f, 0.5f, 0.55f, 1f, 1f, 0f, 0f, 0.5f, 0f, 0.6f, 0.2f, 0f),
        p(29, "Clarinet", 0f, 0f, 0f, 0f, 0f, 0.15f, 1f, 1f, 0f, 0f, 0.25f, 0.6f, 0.25f, 0.6f, 0.35f, 0.65f, 0.5f, 1f, 0.6f, 0f, 0f, 0.5f, 0.25f, 0.6f, 0f, 0.3f),
        p(30, "Oboe", 0f, 0f, 0f, 0.65f, 0f, 0.15f, 1f, 1f, 0f, 0f, 0.2f, 0.6f, 0.25f, 0.6f, 1f, 0.65f, 0.55f, 1f, 1f, 0f, 0f, 0.45f, 0.25f, 0.5f, 0f, 0.5f),
        p(31, "English Horn", 0f, 0f, 0f, 0.65f, 0f, 0.2f, 1f, 0f, 0f, 0f, 0.2f, 0.6f, 0.25f, 0.6f, 1f, 0.7f, 0.5f, 1f, 1f, 0f, 0f, 0.5f, 0f, 0.5f, 0.15f, 0.7f),
        p(32, "Funny Cat", 0f, 0f, 0f, 0f, 0f, 0.3f, 0f, 1f, 1f, 0f, 0.25f, 0.4f, 0.1f, 1f, 0.35f, 0.2f, 0.6f, 0f, 0.8f, 0f, 0f, 0.15f, 0.5f, 0.5f, 0.2f, 0.75f),
        p(33, "Wah Brass", 0f, 0f, 0f, 0f, 0f, 0.3f, 0f, 1f, 1f, 0f, 0.3f, 0.3f, 0.2f, 0.4f, 0f, 0.2f, 0.6f, 0f, 0.7f, 1f, 0f, 0.3f, 0.45f, 0.6f, 0f, 0.7f),
        p(34, "Phase Combination", 1f, 0f, 0f, 0.8f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 0.7f, 0.2f, 0.2f, 0f, 0.2f, 0.6f, 0f, 0.3f, 0f, 0f, 0.6f, 0.3f, 0.2f, 0f, 0.2f),
        p(35, "Reed 1", 1f, 0f, 0f, 0f, 0f, 0.4f, 1f, 1f, 0f, 0f, 0f, 0.85f, 0.1f, 0.5f, 0f, 0.2f, 0.6f, 0f, 0.6f, 1f, 0f, 0.1f, 0.7f, 0.5f, 0f, 0.6f),
        p(36, "Popcorn", 0f, 0f, 0f, 0f, 0f, 0f, 0f, 2f, 0f, 1f, 0f, 0.3f, 0f, 0.2f, 0.35f, 0f, 0f, 1f, 0.8f, 0f, 0f, 0.25f, 0.55f, 1f, 0f, 0.2f),
        p(37, "Reed 2", 1f, 0f, 0f, 0f, 0f, 0f, 0f, 2f, 0f, 1f, 0f, 0.55f, 0.6f, 0.3f, 0f, 0.8f, 0.3f, 0f, 0.5f, 0f, 0f, 0.2f, 0.6f, 0.8f, 0f, 0f),
        p(38, "Reed 3", 0f, 0f, 0f, 0.5f, 0f, 0.2f, 0f, 2f, 0f, 1f, 0.25f, 0f, 0.2f, 1f, 0f, 0.2f, 0.6f, 0f, 0.6f, 0f, 0f, 0.3f, 0.3f, 1f, 0f, 0.2f),
        p(39, "PWM Chorus", 0f, 1f, 0f, 0.5f, 1f, 0f, 1f, 1f, 0f, 1f, 0.3f, 0f, 0.4f, 1f, 0f, 0f, 0.3f, 0f, 0.2f, 0f, 0f, 0.8f, 0f, 1f, 0f, 0f),
        p(40, "Synthesizer Organ", 0f, 1f, 0f, 0.65f, 0f, 0f, 1f, 1f, 0f, 0.75f, 0f, 0.2f, 0.25f, 0.5f, 0f, 0.6f, 0.45f, 1f, 0.5f, 1f, 0f, 0.25f, 0.5f, 0.7f, 0.2f, 0f),
        p(41, "Effect Sound 1", 1f, 0f, 0f, 1f, 0f, 0.15f, 1f, 2f, 1f, 0.7f, 0f, 0.5f, 0.55f, 0f, 0f, 0.6f, 0.45f, 1f, 0.7f, 0f, 0f, 0.65f, 0f, 0.7f, 0f, 0f),
        p(42, "Effect Sound 2", 1f, 0f, 0f, 0.3f, 1f, 0f, 0f, 1f, 1f, 0.65f, 0.65f, 0.55f, 0.65f, 0.2f, 0f, 0.9f, 0.55f, 1f, 0.4f, 1f, 0f, 0.65f, 0f, 0.1f, 0f, 0.3f),
        p(43, "Space Harp", 1f, 0f, 0f, 0f, 2f, 0.2f, 0f, 1f, 1f, 0f, 0f, 0.8f, 0.9f, 0.8f, 0f, 0f, 0.55f, 1f, 0.6f, 0f, 0f, 0.65f, 0.55f, 1f, 0f, 0.5f),
        p(44, "Funk", 1f, 0f, 0f, 0.6f, 0f, 0f, 1f, 1f, 1f, 1f, 0.6f, 0.5f, 0f, 0f, 0f, 0.25f, 0.3f, 1f, 0.2f, 1f, 0f, 0.75f, 0f, 0.45f, 0f, 0.6f),
        p(45, "Space Sound 1", 1f, 0f, 0f, 0.45f, 0f, 0.2f, 1f, 1f, 0f, 1f, 0f, 0.8f, 0.3f, 0f, 0f, 0.7f, 0.6f, 1f, 0.3f, 1f, 0f, 0.65f, 0f, 1f, 0f, 0.7f),
        p(46, "Mysterious Invention", 0f, 0f, 0f, 0.8f, 2f, 0.2f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 0.8f, 0.6f, 1f, 0.5f, 0f, 0f, 0.8f, 0f, 0f, 0.25f, 0.7f),
        p(47, "Space Sound 2", 1f, 0f, 0f, 0.6f, 0f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 0f, 0.3f, 0.3f, 1f, 0.2f, 0f, 0f, 0.2f, 0.6f, 1f, 0f, 0.85f),
        p(48, "Percussive Sound 1", 0f, 0f, 1f, 0f, 2f, 0f, 0f, 2f, 0f, 0f, 0f, 0.3f, 0.4f, 0f, 0.35f, 0f, 0f, 1f, 1f, 0f, 0f, 0.4f, 0.15f, 1f, 0f, 1f),
        p(49, "Percussive Sound 2", 0f, 0f, 0f, 0f, 2f, 0f, 0f, 1f, 0f, 0f, 0f, 0.3f, 0.4f, 0f, 0.35f, 0f, 0f, 1f, 1f, 0f, 0f, 0.5f, 0f, 1f, 0f, 1f),
        p(50, "Whistle", 0f, 0f, 0.2f, 0f, 0f, 0f, 0f, 2f, 0f, 0f, 0.3f, 0f, 0.1f, 1f, 0.35f, 0.5f, 0.55f, 1f, 0.8f, 0f, 0f, 0.35f, 0.15f, 1f, 0.2f, 1f),
        p(51, "Effect Sound 3", 0f, 0f, 0f, 0f, 2f, 0f, 0f, 1f, 0f, 0f, 0f, 0.4f, 0.7f, 0.55f, 0.35f, 0.4f, 0.55f, 1f, 1f, 0f, 0f, 0.35f, 0f, 1f, 0.2f, 1f),
        p(52, "UFO", 1f, 0f, 0.2f, 0f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0.6f, 0.8f, 1f, 0f, 0f, 0.6f, 1f, 0.4f, 0f, 0f, 0f, 0.7f, 1f, 0.4f, 1f),
        p(53, "Space Sound 3", 1f, 0f, 0.2f, 0f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 1f, 0.8f, 0f, 0f, 0f, 0.6f, 1f, 0.5f, 0f, 0f, 0.5f, 0f, 1f, 0f, 1f),
        p(54, "Surf", 0f, 0f, 1f, 0f, 2f, 0f, 0f, 1f, 0f, 0f, 0f, 0.4f, 0.8f, 1f, 0f, 0f, 0f, 1f, 0.9f, 0f, 0f, 0.6f, 0f, 1f, 0.6f, 0f),
        p(55, "Synthesizer Drum", 0f, 0f, 0f, 0f, 2f, 0f, 0f, 1f, 0f, 0f, 0f, 0.5f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0.6f, 0f, 0f, 0.2f, 0.4f, 1f, 0f, 1f)
    )

    private fun jp(
        id: Int,
        name: String,
        voiceMode: Float = 0f,
        vco1Wave: Float = 1f,
        vco2Wave: Float = 0f,
        vco2Range: Float = 1f,
        detune: Float = 0.14f,
        sync: Float = 0f,
        xMod: Float = 0f,
        vco1Lvl: Float = 0.9f,
        vco2Lvl: Float = 0.8f,
        pitchEnv: Float = 0f,
        pitchDest: Float = 0f,
        slope12dB: Float = 0f,
        vcfEnvSrc: Float = 0f,
        cutoff: Float = 0.55f,
        res: Float = 0.2f,
        vcfEnv: Float = 0.45f,
        vcfKey: Float = 0.7f,
        a1: Float = 0.01f,
        d1: Float = 0.35f,
        s1: Float = 0f,
        r1: Float = 0.25f,
        a2: Float = 0.08f,
        d2: Float = 0.45f,
        s2: Float = 0.75f,
        r2: Float = 0.35f,
        chorusI: Float = 1f,
        chorusII: Float = 0f,
        delayOn: Float = 0f
    ): HeraPreset {
        return HeraPreset(
            id = id,
            name = name,
            isUserPreset = false,
            values = mapOf(
                SynthParam.SynthModel to 1f,
                SynthParam.JP8VoiceMode to voiceMode,
                SynthParam.JP8LfoWave to 0f,
                SynthParam.JP8Vco1Wave to vco1Wave,
                SynthParam.JP8Vco2Wave to vco2Wave,
                SynthParam.JP8Vco2Range to vco2Range,
                SynthParam.JP8Vco2Detune to detune,
                SynthParam.JP8VcoSync to sync,
                SynthParam.JP8CrossMod to xMod,
                SynthParam.JP8Vco1Level to vco1Lvl,
                SynthParam.JP8Vco2Level to vco2Lvl,
                SynthParam.JP8PitchEnvDepth to pitchEnv,
                SynthParam.JP8PitchEnvTarget to pitchDest,
                SynthParam.JP8VcfSlope to slope12dB,
                SynthParam.JP8VcfEnvSource to vcfEnvSrc,
                SynthParam.JP8VcaLfoDepth to 0f,
                SynthParam.JP8Env1Attack to a1,
                SynthParam.JP8Env1Decay to d1,
                SynthParam.JP8Env1Sustain to s1,
                SynthParam.JP8Env1Release to r1,
                SynthParam.JP8Env1Polarity to 0f,
                SynthParam.DCORange to 1f,
                SynthParam.DCOSawLevel to vco1Lvl,
                SynthParam.DCOPulseLevel to 0f,
                SynthParam.DCOSubLevel to 0f,
                SynthParam.DCONoiseLevel to 0f,
                SynthParam.DCOPWMDepth to 0.35f,
                SynthParam.DCOPWMMod to 1f,
                SynthParam.DCOPitchModDepth to 0.04f,
                SynthParam.HPF to 0f,
                SynthParam.VCFCutoff to cutoff,
                SynthParam.VCFResonance to res,
                SynthParam.VCFEnv to vcfEnv,
                SynthParam.VCFLFO to 0f,
                SynthParam.VCFKey to vcfKey,
                SynthParam.VCFBendDepth to 0f,
                SynthParam.VCAType to 0f,
                SynthParam.VCADepth to 0.8f,
                SynthParam.ENVAttack to a2,
                SynthParam.ENVDecay to d2,
                SynthParam.ENVSustain to s2,
                SynthParam.ENVRelease to r2,
                SynthParam.LFORate to 0.55f,
                SynthParam.LFODelay to 0.35f,
                SynthParam.LFOTrigMode to 1f,
                SynthParam.ChorusI to chorusI,
                SynthParam.ChorusII to chorusII,
                SynthParam.DelayEnabled to delayOn,
                SynthParam.DelayTime to 0.36f,
                SynthParam.DelayFeedback to 0.35f,
                SynthParam.DelayMix to 0.25f,
                SynthParam.ReverbEnabled to 1f,
                SynthParam.ReverbType to 1f,
                SynthParam.ReverbTime to 0.45f,
                SynthParam.ReverbDecay to 0.6f,
                SynthParam.ReverbMix to 0.28f
            )
        )
    }

    val JUPITER8_FACTORY_PRESETS: List<HeraPreset> = listOf(
        jp(100, "JP-8 Brass 1 (1981)", slope12dB = 1f, detune = 0.16f, cutoff = 0.28f, res = 0.18f, vcfEnv = 0.68f, a2 = 0.09f, d2 = 0.48f, s2 = 0.72f, r2 = 0.32f),
        jp(101, "Jump 1984 Poly Brass", slope12dB = 0f, vco1Wave = 1f, vco2Wave = 1f, detune = 0.22f, cutoff = 0.58f, res = 0.15f, vcfEnv = 0.52f, a2 = 0.01f, d2 = 0.44f, s2 = 0.78f, r2 = 0.28f, chorusI = 1f, chorusII = 1f),
        jp(102, "Hard Sync Screamer Lead", voiceMode = 3f, vco2Range = 2f, sync = 1f, pitchEnv = 0.72f, pitchDest = 0f, cutoff = 0.68f, res = 0.35f, vcfEnv = 0.35f, a1 = 0.01f, d1 = 0.48f, s1 = 0.1f, r1 = 0.3f, a2 = 0.005f, d2 = 0.5f, s2 = 0.85f, r2 = 0.32f, delayOn = 1f),
        jp(103, "Blade Runner CS/JP Myth", slope12dB = 1f, detune = 0.24f, cutoff = 0.24f, res = 0.22f, vcfEnv = 0.62f, a2 = 0.48f, d2 = 0.68f, s2 = 0.82f, r2 = 0.76f, chorusI = 1f, delayOn = 1f),
        jp(104, "Lush Jupiter Strings 1", slope12dB = 1f, vco1Wave = 1f, vco2Wave = 1f, detune = 0.18f, cutoff = 0.62f, res = 0.1f, vcfEnv = 0.22f, a2 = 0.42f, d2 = 0.55f, s2 = 0.92f, r2 = 0.62f, chorusI = 0f, chorusII = 1f),
        jp(105, "Cross-Mod DX/JP Bell", vco1Wave = 0f, vco2Wave = 2f, vco2Range = 3f, xMod = 0.54f, cutoff = 0.76f, res = 0.18f, vcfEnv = 0.4f, a2 = 0.005f, d2 = 0.62f, s2 = 0.08f, r2 = 0.68f, delayOn = 1f),
        jp(106, "Thriller 1982 Unison Bass", voiceMode = 2f, vco1Wave = 1f, vco2Wave = 1f, vco2Range = 0f, detune = 0.14f, cutoff = 0.24f, res = 0.38f, vcfEnv = 0.66f, a2 = 0.004f, d2 = 0.34f, s2 = 0.28f, r2 = 0.18f, chorusI = 0f),
        jp(107, "Duran Rio 16th Arp Pluck", slope12dB = 1f, vco1Wave = 2f, vco2Wave = 0f, vco2Range = 2f, cutoff = 0.34f, res = 0.42f, vcfEnv = 0.64f, a2 = 0.004f, d2 = 0.26f, s2 = 0.15f, r2 = 0.22f, delayOn = 1f),
        jp(108, "Dual VCO 5th Interval Pad", slope12dB = 1f, vco2Range = 3f, detune = 0.12f, cutoff = 0.46f, res = 0.2f, vcfEnv = 0.38f, a2 = 0.38f, d2 = 0.64f, s2 = 0.85f, r2 = 0.66f, chorusI = 1f, chorusII = 1f),
        jp(109, "Cars Sync Sweep Hook", sync = 1f, vco2Range = 2f, pitchEnv = 0.58f, cutoff = 0.72f, res = 0.28f, vcfEnv = 0.32f, a1 = 0.02f, d1 = 0.38f, s1 = 0f, r1 = 0.24f, a2 = 0.005f, d2 = 0.45f, s2 = 0.8f, r2 = 0.26f),
        jp(110, "Warm Analog Poly Comp", vco1Wave = 2f, vco2Wave = 0f, detune = 0.15f, cutoff = 0.38f, res = 0.24f, vcfEnv = 0.54f, a2 = 0.008f, d2 = 0.38f, s2 = 0.42f, r2 = 0.3f),
        jp(111, "Jupiter Hollow Glass", slope12dB = 1f, vco1Wave = 3f, vco2Wave = 2f, vco2Range = 2f, xMod = 0.22f, cutoff = 0.64f, res = 0.32f, vcfEnv = 0.35f, a2 = 0.02f, d2 = 0.54f, s2 = 0.48f, r2 = 0.56f, delayOn = 1f),
        jp(112, "8-Voice Orchestra Tutti", slope12dB = 0f, detune = 0.2f, cutoff = 0.48f, res = 0.15f, vcfEnv = 0.45f, a2 = 0.28f, d2 = 0.56f, s2 = 0.88f, r2 = 0.58f, chorusI = 1f, chorusII = 1f),
        jp(113, "Miami Vice Keys 1985", slope12dB = 1f, vco1Wave = 2f, vco2Wave = 1f, vco2Range = 2f, xMod = 0.16f, cutoff = 0.44f, res = 0.22f, vcfEnv = 0.5f, a2 = 0.006f, d2 = 0.48f, s2 = 0.35f, r2 = 0.44f, delayOn = 1f),
        jp(114, "Howard Jones Sync Bass", voiceMode = 3f, sync = 1f, vco2Range = 1f, pitchEnv = 0.46f, cutoff = 0.36f, res = 0.4f, vcfEnv = 0.58f, a1 = 0.005f, d1 = 0.25f, a2 = 0.004f, d2 = 0.32f, s2 = 0.4f, r2 = 0.18f),
        jp(115, "Toto Africa Brass Ensemble", slope12dB = 1f, detune = 0.18f, cutoff = 0.32f, res = 0.16f, vcfEnv = 0.56f, a2 = 0.11f, d2 = 0.46f, s2 = 0.68f, r2 = 0.36f)
    )

    fun serializePresetToXml(name: String, params: Map<SynthParam, Float>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        val escapedName = name.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
        sb.append("<HeraPreset name=\"").append(escapedName).append("\" version=\"1.0\">\n")

        for (param in SynthParam.entries) {
            if (param == SynthParam.MasterVolume || param == SynthParam.LFOManualTrig) continue
            val v = params[param] ?: param.defaultVal
            val formatted = if (param == SynthParam.DCORange) {
                when (v.roundToInt()) {
                    0 -> "0.5"
                    1 -> "1.0"
                    else -> "2.0"
                }
            } else {
                v.toString()
            }
            sb.append("  <Param id=\"").append(param.key).append("\" value=\"").append(formatted).append("\"/>\n")
        }
        sb.append("</HeraPreset>\n")
        return sb.toString()
    }

    fun parsePresetFromXml(xmlContent: String, fallbackId: Int = 100): HeraPreset? {
        return try {
            val parser = Xml.newPullParser()
            parser.setInput(StringReader(xmlContent))
            var presetName = "User Preset"
            val values = mutableMapOf<SynthParam, Float>()
            for (p in SynthParam.entries) {
                values[p] = p.defaultVal
            }

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "HeraPreset", "Preset", "preset", "PROGRAM", "program" -> {
                            val n = parser.getAttributeValue(null, "name")
                            if (!n.isNullOrBlank()) presetName = n
                        }
                        "Param", "param", "PARAM" -> {
                            val idAttr = parser.getAttributeValue(null, "id")
                                ?: parser.getAttributeValue(null, "name")
                            val valAttr = parser.getAttributeValue(null, "value")
                            if (idAttr != null && valAttr != null) {
                                val param = SynthParam.fromKey(idAttr)
                                val num = valAttr.toFloatOrNull()
                                if (param != null && num != null) {
                                    values[param] = if (param == SynthParam.DCORange) {
                                        when {
                                            num <= 0.6f -> 0f
                                            num <= 1.4f -> 1f
                                            else -> 2f
                                        }
                                    } else {
                                        num.coerceIn(0f, 2f)
                                    }
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
            HeraPreset(id = fallbackId, name = presetName, isUserPreset = true, values = values)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadUserPresets(context: Context): List<HeraPreset> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getStringSet(KEY_USER_PRESETS_XML, emptySet()) ?: emptySet()
        return raw.mapIndexedNotNull { idx, xml ->
            parsePresetFromXml(xml, 100 + idx)
        }.sortedBy { it.name }
    }

    fun saveUserPreset(context: Context, preset: HeraPreset): List<HeraPreset> {
        val current = loadUserPresets(context).toMutableList()
        current.removeAll { it.name.equals(preset.name, ignoreCase = true) }
        current.add(preset.copy(isUserPreset = true))
        val xmlSet = current.map { serializePresetToXml(it.name, it.values) }.toSet()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_USER_PRESETS_XML, xmlSet)
            .apply()
        return loadUserPresets(context)
    }

    fun deleteUserPreset(context: Context, presetName: String): List<HeraPreset> {
        val current = loadUserPresets(context).toMutableList()
        current.removeAll { it.name.equals(presetName, ignoreCase = true) }
        val xmlSet = current.map { serializePresetToXml(it.name, it.values) }.toSet()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_USER_PRESETS_XML, xmlSet)
            .apply()
        return loadUserPresets(context)
    }
}
