package com.example.clearrecyclerwithvideo.player_controller_playground;

import android.content.Context;
import android.graphics.Point;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.Optional;
import java.util.function.Consumer;

import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.DirectProcessor;

/**
 * @author Konstantin Epifanov
 * @since 29.07.2019
 */
public class ExtendedExoPlayer extends SimpleExoPlayer {

  protected ExtendedExoPlayer(Context context, RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, BandwidthMeter bandwidthMeter, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, Looper looper) {
    super(context, renderersFactory, trackSelector, loadControl, bandwidthMeter, drmSessionManager, looper);
  }

  protected ExtendedExoPlayer(Context context, RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, BandwidthMeter bandwidthMeter, AnalyticsCollector.Factory analyticsCollectorFactory, Looper looper) {
    super(context, renderersFactory, trackSelector, loadControl, drmSessionManager, bandwidthMeter, analyticsCollectorFactory, looper);
  }

  protected ExtendedExoPlayer(Context context, RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, BandwidthMeter bandwidthMeter, AnalyticsCollector.Factory analyticsCollectorFactory, Clock clock, Looper looper) {
    super(context, renderersFactory, trackSelector, loadControl, drmSessionManager, bandwidthMeter, analyticsCollectorFactory, clock, looper);
  }

  public final Disposable accept(@Nullable Surface surface, Consumer<Point> formatConsumer) {

    final Optional<MediaCodecVideoRenderer> videoRenderer = findMediaCodecVideoRenderer();
    videoRenderer.ifPresent(renderer -> setupSurface(renderer, surface));

    return Disposables.composite(
      () -> videoRenderer.ifPresent(renderer -> setupSurface(renderer, null)),
      videoRenderer
        .map(renderer -> ((CustomVideoRenderer) renderer).mFormatProcessor.subscribe(formatConsumer))
        .orElseGet(Disposables::single)
    );
  }

  private void setupSurface(MediaCodecVideoRenderer renderer, Surface surface) {
    try {
      renderer.handleMessage(C.MSG_SET_SURFACE, surface);
    } catch (ExoPlaybackException e) {
      throw new RuntimeException(e);
    }
  }

  private Optional<MediaCodecVideoRenderer> findMediaCodecVideoRenderer() {
    for (Renderer renderer : renderers)
      if (renderer instanceof MediaCodecVideoRenderer)
        return Optional.of((MediaCodecVideoRenderer) renderer);

    return Optional.empty();
  }

  protected static class CustomVideoRenderer extends MediaCodecVideoRenderer {

    private static final String TAG = "MediaCodecVideoRenderer";
    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_BOTTOM = "crop-bottom";
    private static final String KEY_CROP_TOP = "crop-top";

    final DirectProcessor<Point> mFormatProcessor = DirectProcessor.create();

    public CustomVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector) {
      super(context, mediaCodecSelector);
    }

    public CustomVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs) {
      super(context, mediaCodecSelector, allowedJoiningTimeMs);
    }

    public CustomVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, @Nullable Handler eventHandler, @Nullable VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
      super(context, mediaCodecSelector, allowedJoiningTimeMs, eventHandler, eventListener, maxDroppedFramesToNotify);
    }

    public CustomVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys, @Nullable Handler eventHandler, @Nullable VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
      super(context, mediaCodecSelector, allowedJoiningTimeMs, drmSessionManager, playClearSamplesWithoutKeys, eventHandler, eventListener, maxDroppedFramesToNotify);
    }

    @Override
    protected void onOutputFormatChanged(MediaCodec codec, MediaFormat outputFormat) {
      super.onOutputFormatChanged(codec, outputFormat);

      boolean hasCrop = outputFormat.containsKey(KEY_CROP_RIGHT)
        && outputFormat.containsKey(KEY_CROP_LEFT) && outputFormat.containsKey(KEY_CROP_BOTTOM)
        && outputFormat.containsKey(KEY_CROP_TOP);
      int width =
        hasCrop
          ? outputFormat.getInteger(KEY_CROP_RIGHT) - outputFormat.getInteger(KEY_CROP_LEFT) + 1
          : outputFormat.getInteger(MediaFormat.KEY_WIDTH);
      int height =
        hasCrop
          ? outputFormat.getInteger(KEY_CROP_BOTTOM) - outputFormat.getInteger(KEY_CROP_TOP) + 1
          : outputFormat.getInteger(MediaFormat.KEY_HEIGHT);

      System.out.println("CustomVideoRenderer.onOutputFormatChanged: SIZE " + width + "x" + height);
      mFormatProcessor.onNext(new Point(width, height));
    }
  }

  @Override
  public void prepare(MediaSource mediaSource) {
    super.prepare(mediaSource);
  }
}
