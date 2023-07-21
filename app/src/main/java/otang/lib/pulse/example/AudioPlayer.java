package otang.lib.pulse.example;

import android.content.Context;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import java.util.ArrayList;
import java.util.List;

public class AudioPlayer {

  private Context context;
  private ExoPlayer exoPlayer;

  public AudioPlayer(Context ctx) {
    this.context = ctx;
    init();
  }

  void init() {
    exoPlayer = new ExoPlayer.Builder(context).build();
  }

  public void setSource(String url) {
    exoPlayer.setMediaItem(MediaItem.fromUri(url));
    exoPlayer.prepare();
    exoPlayer.setPlayWhenReady(true);
  }

  public void setSource(ArrayList<String> urls) {
    List<MediaItem> items = new ArrayList<>();
    for (String url : urls) {
      items.add(MediaItem.fromUri(url));
    }
    exoPlayer.setMediaItems(items);
    exoPlayer.prepare();
  }

  public void setListener(Player.Listener listener) {
    exoPlayer.addListener(listener);
  }

  public int getAudioSessionId() {
    if (exoPlayer != null) {
      return exoPlayer.getAudioSessionId();
    } else {
      return 0;
    }
  }

  public void play() {
    exoPlayer.setPlayWhenReady(true);
  }

  public void play(int position) {
    exoPlayer.seekTo(position, 0);
    play();
  }

  public void pause() {
    exoPlayer.setPlayWhenReady(false);
  }

  public void stop() {
    exoPlayer.stop();
  }

  public boolean isPlaying() {
    return exoPlayer != null && exoPlayer.isPlaying();
  }
}
