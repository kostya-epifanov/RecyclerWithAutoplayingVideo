package com.example.clearrecyclerwithvideo.utils.player;

import android.content.Context;
import android.net.Uri;
import android.util.LruCache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.clearrecyclerwithvideo.utils.reactor.Schedule;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.ExoPlayerImplUtil;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

import java.util.function.Consumer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import static android.net.Uri.parse;

/**
 * @author Konstantin Epifanov
 * @since 05.07.2019
 */
public class ExoHolder {

  private static ExoHolder instance = null;
  private SimpleExoPlayer player;

  public static SimpleExoPlayer get(Context context) {
    return get(context, null);
  }

  public static SimpleExoPlayer get(Context context, String url) {
    if (instance == null) instance = new ExoHolder();
    initializePlayer(context, url);
    return instance.player;
  }

  private ExoHolder() {
  }

  private static void initializePlayer(Context context, String url) {
    if (instance.player == null) {
      instance.player = ExoPlayerFactory.newSimpleInstance(context);
      instance.player.setPlayWhenReady(true);
      instance.player.setVolume(0f);
    }

    if (url != null) {
      MediaSource mediaSource = buildMediaSource(context, Uri.parse(url));
      instance.player.prepare(mediaSource, true, false);
    }
  }

  public static MediaSource buildMediaSource(Context context, Uri uri) {
    return new HlsMediaSource
      .Factory(new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "polygon")))
      .setExtractorFactory(new DefaultHlsExtractorFactory())
      .createMediaSource(uri);
  }

  public static SimpleExoPlayer getPlayerByUrl(Context context, String url) {
    SimpleExoPlayer result;
    result = ExoPlayerFactory.newSimpleInstance(context);
    result.setPlayWhenReady(true);
    MediaSource mediaSource = buildMediaSource(context, Uri.parse(url));
    result.prepare(mediaSource, true, false);
    result.setVolume(0f);
    return result;
  }

  public static Player getPlayer(@NonNull Context context) {
    Schedule.trowIfNotWorkerThread();
    SimpleExoPlayer result = ExoPlayerImplUtil.createSimpleExoPlayer(
      context,
      new DefaultRenderersFactory(context),
      new DefaultTrackSelector(),
      new DefaultLoadControl(),
      new DefaultBandwidthMeter.Builder().build(),
      new AnalyticsCollector.Factory(),
      new WorkLooperClock(),
      Util.getLooper());

    result.setPlayWhenReady(true);
    result.setVolume(1f);
    return result;
  }


  public static Flux<Player> getPlayerAsync(@NonNull Context context) {
    return Flux
      .create((Consumer<FluxSink<Player>>) sink -> {
        Player player = getPlayer(context);
        sink
          .onDispose(player::release)
          .next(player);
      })
      .transform(Schedule::work_main);
  }

  public static Mono<Player> getFromCache(@NonNull Context context, @NonNull String url) {
    return Mono.create((Consumer<MonoSink<Player>>)
      monoSink -> monoSink.success(sCache.get(url)))
      .transform(Schedule::work_main);
  }

  static androidx.collection.LruCache<String, Player> players(@NonNull Context context) {
    TransferListener listener = new DefaultBandwidthMeter.Builder(context).build();

    DataSource.Factory factory2 = new DefaultDataSourceFactory(context, listener,
      new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "Webka"), listener));

    DefaultHlsDataSourceFactory factory1 = new DefaultHlsDataSourceFactory(factory2);

    HlsMediaSource.Factory factory = new HlsMediaSource.Factory(factory1);
    return new androidx.collection.LruCache<String, Player>(1) {
      @Override
      protected final Player create(@NonNull String key) {
        final ExoPlayer result = (ExoPlayer) getPlayer(context);
        result.setPlayWhenReady(true);
        result.prepare(factory.createMediaSource(parse(key)));
        return result;
      }

      @Override
      protected void entryRemoved(boolean evicted, @NonNull String key, @NonNull Player oldValue, @Nullable Player newValue) {
        oldValue.release();
        super.entryRemoved(evicted, key, oldValue, newValue);
      }
    };
  }

  private static androidx.collection.LruCache<String, Player> sCache = null;

  public static void setupCache(Context context) {
    sCache = players(context);
  }
}
