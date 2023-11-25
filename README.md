# About
Pulse is custom Audio Visualizer for android, ported from [Derpfest-AOSP](https://github.com/DerpFest-AOSP) rom.
# Usage
> build.gradle \
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.otang45/Pulse/badge.svg?style=flat)](https://central.sonatype.com/namespace/io.github.otang45)
```gradle
implementation 'io.github.otang45:Pulse:$version'
```
> AndroidManifest.xml
```xml
<!-- For using audioSessionId = 0 -->
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<!-- Required Permission -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```
> xml layout
```xml
<otang.pulse.lib.VisualizerView
    android:id="@+id/visualizer_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
> Set audio session id
```kt
binding.visualizerView.setAudioSessionId(audioSessionId)
```

# Configuration
### Manual
> Using PulseConfig
```kt
// Initialize PulseConfig
var config = PulseConfig(this)

config.isCenterMirror // Boolean def = false
config.isPulseEnabled // Boolean def = true
config.isLeftInLandscape // Boolean def = false
config.isSolidRounded // Boolean def = true
config.isSmoothEnabled // Boolean def = true
config.isVerticalMirror // Boolean def = false
config.pulseColor // Int def = 0 (0 = Primary, 1 = Custom, 2 = LavaLamp)
config.solidLineCount // Int def = 32
config.fadingDim // Int def = 14
config.fadingDiv // Int def = 16
config.emptySize // Int def = 1
config.fadingFudge // Int def = 5
config.solidFudge // Int def = 5
config.gravity // Int def = 0 (0 = Bottom, 1 = Top, 2 = Center)
config.lavaSpeed // Int def = 10000
config.solidOvacity // Int def = 200
config.renderStyle // Int def = 1 (0 = Legacy, 1 = Solid)
config.fillSize // Int def = 4
config.customColor // ColorInt def = -0x6d000001
```

### Using Preference
> Use this key for preference
```xml
android:key="pref_pulse" <!-- SwitchPreference -->
android:key="pref_pulse_left" <!-- SwitchPreference -->
android:key="pref_pulse_render" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_gravity" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_center_mirrored" <!-- SwitchPreference -->
android:key="pref_pulse_vertical_mirror" <!-- SwitchPreference -->
android:key="pref_pulse_smooth" <!-- SwitchPreference -->
android:key="pref_pulse_color" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_color_custom" <!-- ColorPreference -->
android:key="pref_pulse_lava_speed" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_fading_dimen" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_fading_div" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_fill_size" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_empty_size" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_fading_fudge" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_rounded" <!-- SwitchPreference -->
android:key="pref_pulse_solid_ovacity" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_line_count" <!-- ListPreference / SeekPreference -->
android:key="pref_pulse_solid_fudge" <!-- ListPreference / SeekPreference -->
```

### Example
> See [Sample](https://github.com/Otang45/Pulse/tree/master/app)