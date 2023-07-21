package otang.pulse.lib.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class PrefUtils {

  public static final String PREF_PULSE = "pref_pulse";
  public static final String PREF_PULSE_LEFT = "pref_pulse_left";
  public static final String PREF_PULSE_RENDER = "pref_pulse_render";
  public static final String PREF_PULSE_GRAVITY = "pref_pulse_gravity";
  public static final String PREF_PULSE_CENTER_MIRRORED = "pref_pulse_center_mirrored";
  public static final String PREF_PULSE_VERTICAL_MIRROR = "pref_pulse_vertical_mirror";
  public static final String PREF_PULSE_SMOOTH = "pref_pulse_smooth";
  public static final String PREF_PULSE_COLOR = "pref_pulse_color";
  public static final String PREF_PULSE_COLOR_CUSTOM = "pref_pulse_color_custom";
  public static final String PREF_PULSE_LAVA_SPEED = "pref_pulse_lava_speed";
  public static final String PREF_PULSE_FADING_DIMEN = "pref_pulse_fading_dimen";
  public static final String PREF_PULSE_FADING_DIV = "pref_pulse_fading_div";
  public static final String PREF_PULSE_FILL_SIZE = "pref_pulse_fill_size";
  public static final String PREF_PULSE_EMPTY_SIZE = "pref_pulse_empty_size";
  public static final String PREF_PULSE_FADING_FUDGE = "pref_pulse_fading_fudge";
  public static final String PREF_PULSE_ROUNDED = "pref_pulse_rounded";
  public static final String PREF_PULSE_SOLID_OVACITY = "pref_pulse_solid_ovacity";
  public static final String PREF_PULSE_LINE_COUNT = "pref_pulse_line_count";
  public static final String PREF_PULSE_SOLID_FUDGE = "pref_pulse_solid_fudge";
  public static final String PREF_CAT_PULSE_FADING = "pref_cat_pulse_fading_block";
  public static final String PREF_CAT_PULSE_SOLID = "pref_pulse_cat_solid_lines";

  private Context context;
  private SharedPreferences pref;
  private SharedPreferences.Editor editor;

  public PrefUtils(Context ctx) {
    this.context = ctx;
    this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
    this.editor = pref.edit();
  }

  public void saveAs(String key, boolean def) {
    editor.putBoolean(key, def).commit();
  }

  public void saveAs(String key, float def) {
    editor.putFloat(key, def).commit();
  }

  public void saveAs(String key, int def) {
    editor.putInt(key, def).commit();
  }

  public void saveAs(String key, long def) {
    editor.putLong(key, def).commit();
  }

  public void saveAs(String key, String def) {
    editor.putString(key, def).commit();
  }

  public boolean getBoolean(String key, boolean def) {
    return pref.getBoolean(key, def);
  }

  public boolean getBooleanS(String key) {
    return pref.getInt(key, 0) == 1;
  }

  public float getFloat(String key, float def) {
    return pref.getFloat(key, def);
  }

  public int getInt(String key, int def) {
    return pref.getInt(key, def);
  }

  public long getLong(String key, long def) {
    return pref.getLong(key, def);
  }

  public String getString(String key, String def) {
    return pref.getString(key, def);
  }

  public void clear() {
    editor.clear().commit();
  }

  public void remove(String key) {
    editor.remove(key).commit();
  }

  public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
    pref.registerOnSharedPreferenceChangeListener(listener);
  }

  // Custom
  public int getPulseRender() {
    return Integer.valueOf(pref.getString(PREF_PULSE_RENDER, "1"));
  }

  public int getPulseColor() {
    return Integer.valueOf(pref.getString(PREF_PULSE_COLOR, "0"));
  }
}
