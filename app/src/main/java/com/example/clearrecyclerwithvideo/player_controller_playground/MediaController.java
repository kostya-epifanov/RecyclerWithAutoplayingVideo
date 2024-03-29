package com.example.clearrecyclerwithvideo.player_controller_playground;

import android.content.Context;
import android.os.Handler;
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
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
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

    Mono<Player> mono =
      Mono.create((Consumer<MonoSink<Player>>) monoSink -> monoSink.success(sCache.get(url)))
        .transform(Schedule::work_main);

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
    return new androidx.collection.LruCache<String, Player>(2) {
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

        /*try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }*/

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

        try {
          tempPlayer.stop();
        } catch (Exception e) {
          //
        }

        /*try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }*/

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
        new DefaultRenderersFactory(context) {

          @Override
          protected void buildVideoRenderers(Context context, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, long allowedVideoJoiningTimeMs, Handler eventHandler, VideoRendererEventListener eventListener, int extensionRendererMode, ArrayList<Renderer> out) {
            out.add(
              new ExtendedExoPlayer.CustomVideoRenderer(
                context,
                MediaCodecSelector.DEFAULT,
                allowedVideoJoiningTimeMs,
                drmSessionManager,
                /* playClearSamplesWithoutKeys= */ false,
                eventHandler,
                eventListener,
                MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY));

            if (extensionRendererMode == EXTENSION_RENDERER_MODE_OFF) {
              return;
            }
            int extensionRendererIndex = out.size();
            if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) {
              extensionRendererIndex--;
            }

            try {
              // Full class names used for constructor args so the LINT rule triggers if any of them move.
              // LINT.IfChange
              Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer");
              Constructor<?> constructor =
                clazz.getConstructor(
                  boolean.class,
                  long.class,
                  android.os.Handler.class,
                  com.google.android.exoplayer2.video.VideoRendererEventListener.class,
                  int.class);
              // LINT.ThenChange(../../../../../../../proguard-rules.txt)
              Renderer renderer =
                (Renderer)
                  constructor.newInstance(
                    true,
                    allowedVideoJoiningTimeMs,
                    eventHandler,
                    eventListener,
                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
              out.add(extensionRendererIndex++, renderer);
              com.google.android.exoplayer2.util.Log.i("TAG", "Loaded LibvpxVideoRenderer.");
            } catch (ClassNotFoundException e) {
              // Expected if the app was built without the extension.
            } catch (Exception e) {
              // The extension is present, but instantiation failed.
              throw new RuntimeException("Error instantiating VP9 extension", e);
            }
          }
        },

        new DefaultTrackSelector(),
        new DefaultLoadControl() {
          @Override
          public void onPrepared() {
            super.onPrepared();
            System.out.println("ON PREPAREED");
          }
        }, //TODO loadControl.onPrepared(); - реальный препэйр медиа соурса
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
