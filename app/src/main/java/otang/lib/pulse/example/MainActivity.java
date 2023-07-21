package otang.lib.pulse.example;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.internal.EdgeToEdgeUtils;
import otang.lib.pulse.example.databinding.ActivityMainBinding;
import otang.pulse.lib.PulseControllerImpl;

public class MainActivity extends AppCompatActivity implements Player.Listener {

  private ActivityMainBinding binding;
  private AudioPlayer player;
  private PulseControllerImpl impl;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdgeUtils.applyEdgeToEdge(getWindow(), true);
    DynamicColors.applyToActivityIfAvailable(this);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setSupportActionBar(binding.toolbar);
    // Pulse
    impl = new PulseControllerImpl(this, binding.content.vv);
    // Player
    player = new AudioPlayer(this);
    player.setSource("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
    player.setListener(this);
    binding.fab.setOnClickListener(
        v -> {
          if (player.isPlaying()) {
            player.pause();
          } else {
            player.play();
          }
        });
  }

  @Override
  public void onPlayWhenReadyChanged(boolean arg0, int arg1) {
    if (arg0) {
      impl.setAudioSessionId(player.getAudioSessionId());
    } else {
      impl.release();
    }
    // TODO: Implement this method
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.binding = null;
  }
}
