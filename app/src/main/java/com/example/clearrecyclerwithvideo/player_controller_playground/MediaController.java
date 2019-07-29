package com.example.clearrecyclerwithvideo.player_controller_playground;

import android.content.Context;
import android.util.Log;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.example.clearrecyclerwithvideo.utils.player.WorkLooperClock;
import com.example.clearrecyclerwithvideo.utils.reactor.Schedule;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

import java.util.function.Consumer;

import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import static android.net.Uri.parse;

/**
 * @author Konstantin Epifanov
 * @since 26.07.2019
 */
public class MediaController {

  private static LruCache<String, Player> sCache = null;

  public static Mono<Player> getFromCache(@NonNull Context context, TextureView texture, @NonNull String url) {
    long time = System.currentTimeMillis();

    System.out.println("MediaController.getFromCache " + url);
    if (sCache == null) {
      sCache = createPlayersCache(context);
    }
    Mono<Player> mono = Mono.create((Consumer<MonoSink<Player>>) monoSink -> monoSink.success(sCache.get(url)))
      .transform(Schedule::work_main)
      /*.map(p -> {
        System.out.println("MediaController.getFromCache map 1" + p.hashCode());
        ExtendedExoPlayer player = (ExtendedExoPlayer) p;

        player.setVideoTextureView(texture);

        player.addVideoListener(new VideoListener() {
          @Override
          public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            ((PlayerTextureView) texture).initialize(width, height);
          }
        });

        System.out.println("MediaController.getFromCache map 2" + p.hashCode());

        return p;
      })*/
      .log();

    try {
      return mono;
    } finally {
      Log.d("DELAY", String.valueOf(time - System.currentTimeMillis()));
    }

  }

  static androidx.collection.LruCache<String, Player> createPlayersCache(@NonNull Context context) {
    TransferListener listener = new DefaultBandwidthMeter.Builder(context).build();
    DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, listener,
      new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "Webka"), listener));
    DefaultHlsDataSourceFactory hlsDataSourceFactory = new DefaultHlsDataSourceFactory(dataSourceFactory);
    HlsMediaSource.Factory factory = new HlsMediaSource.Factory(hlsDataSourceFactory);
    return new androidx.collection.LruCache<String, Player>(1) {
      private Player tempPlayer;

      @Override
      protected final Player create(@NonNull String key) {
        System.out.println("ExoHolder.create");
        ExoPlayer result = (ExoPlayer) getTempPlayer();

        if (result == null) {
          result = (ExoPlayer) getPlayer(context);
          result.setPlayWhenReady(true);
        }

        result.prepare(factory.createMediaSource(parse(key)));

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        return result;
      }

      @Override
      protected void entryRemoved(boolean evicted, @NonNull String key, @NonNull Player oldValue, @Nullable Player newValue) {
        System.out.println("ExoHolder.entryRemoved");
        setTempPlayer(oldValue);
        super.entryRemoved(evicted, key, oldValue, newValue);
      }

      Player getTempPlayer() {
        System.out.println("ExoHolder.getTempPlayer");
        try {
          return tempPlayer;
        } finally {
          tempPlayer = null;
        }
      }

      void setTempPlayer(Player tempPlayer) {
        System.out.println("ExoHolder.setTempPlayer");
        tempPlayer.stop();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        this.tempPlayer = tempPlayer;
      }
    };
  }

  private static Player getPlayer(@NonNull Context context) {
    System.out.println("MediaController.getPlayer init player");

    Schedule.trowIfNotWorkerThread();

    SimpleExoPlayer result =
      new ExtendedExoPlayer(
        context,
        new DefaultRenderersFactory(context),
        new DefaultTrackSelector(),
        new DefaultLoadControl(),
        null,
        new DefaultBandwidthMeter.Builder().build(),
        new AnalyticsCollector.Factory(),
        new WorkLooperClock(),
        Util.getLooper());

    result.setPlayWhenReady(true);
    result.setVolume(1f);

    return result;
  }

}
