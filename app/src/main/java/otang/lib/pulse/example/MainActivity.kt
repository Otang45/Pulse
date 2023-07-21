package otang.lib.pulse.example;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.Player;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.internal.EdgeToEdgeUtils;

import otang.lib.pulse.example.databinding.ActivityMainBinding;
import otang.pulse.lib.util.PulseConfig;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements Player.Listener {

    private ActivityMainBinding binding;
    private AudioPlayer player;
    private PulseConfig config;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdgeUtils.applyEdgeToEdge(getWindow(), true);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        config = binding.vv.getPulsePref();
        setContentView(binding.getRoot());
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO)) {
            EasyPermissions.requestPermissions(this, "Rationale", 10, Manifest.permission.RECORD_AUDIO);
        }
        // Player
        player = new AudioPlayer(this);
        player.setSource("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
        player.setListener(this);
        setupPreference();
        binding.bPlay.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        });
    }

    private void setupPreference() {
        binding.msCenter.setChecked(config.isCenterMirror());
        binding.msCenter.setOnCheckedChangeListener((c, b) -> {
            config.setCenterMirror(b);
        });
        binding.msEnabled.setChecked(config.isPulseEnabled());
        binding.msEnabled.setOnCheckedChangeListener((c, b) -> {
            config.setPulseEnabled(b);
        });
        binding.msLeft.setChecked(config.isLeftInLandscape());
        binding.msLeft.setOnCheckedChangeListener((c, b) -> {
            config.setLeftInLandscape(b);
        });
        binding.msRounded.setChecked(config.isSolidRounded());
        binding.msRounded.setOnCheckedChangeListener((c, b) -> {
            config.setSolidRounded(b);
        });
        binding.msSmooth.setChecked(config.isSmoothEnabled());
        binding.msSmooth.setOnCheckedChangeListener((c, b) -> {
            config.setSmoothEnabled(b);
        });
        binding.msVertical.setChecked(config.isVerticalMirror());
        binding.msVertical.setOnCheckedChangeListener((c, b) -> {
            config.setVerticalMirror(b);
        });
        binding.sColor.setValue(config.getPulseColor());
        binding.sColor.addOnChangeListener((s, i, u) -> {
            config.setPulseColor((int) i);
        });
        binding.sCount.setValue(config.getSolidLineCount());
        binding.sCount.addOnChangeListener((s, i, u) -> {
            config.setSolidLineCount((int) i);
        });
        binding.sDim.setValue(config.getFadingDim());
        binding.sDim.addOnChangeListener((s, i, u) -> {
            config.setFadingDim((int) i);
        });
        binding.sDiv.setValue(config.getFadingDiv());
        binding.sDiv.addOnChangeListener((s, i, u) -> {
            config.setFadingDiv((int) i);
        });
        binding.sEmpty.setValue(config.getEmptySize());
        binding.sEmpty.addOnChangeListener((s, i, u) -> {
            config.setEmptySize((int) i);
        });
        binding.sFudge.setValue(config.getSolidFudge());
        binding.sFudge.addOnChangeListener((s, i, u) -> {
            config.setFadingFudge((int) i);
            config.setSolidFudge((int) i);
        });
        binding.sGravity.setValue(config.getGravity());
        binding.sGravity.addOnChangeListener((s, i, u) -> {
            config.setGravity((int) i);
        });
        binding.sLava.setValue(config.getLavaSpeed());
        binding.sLava.addOnChangeListener((s, i, u) -> {
            config.setLavaSpeed((int) i);
        });
        binding.sOvacity.setValue(config.getSolidOvacity());
        binding.sOvacity.addOnChangeListener((s, i, u) -> {
            config.setSolidOvacity((int) i);
        });
        binding.sRender.setValue(config.getRenderStyle());
        binding.sRender.addOnChangeListener((s, i, u) -> {
            config.setRenderStyle((int) i);
        });
        binding.sFill.setValue(config.getFillSize());
        binding.sFill.addOnChangeListener((s, i, u) -> {
            config.setFillSize((int) i);
        });
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        binding.vv.setAudioSessionId(player.getAudioSessionId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
