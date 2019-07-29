package com.example.clearrecyclerwithvideo.player_controller_playground;

import android.content.Context;
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
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;

import java.util.function.Consumer;

/**
 * @author Konstantin Epifanov
 * @since 29.07.2019
 */
public class ExtendedExoPlayer extends SimpleExoPlayer implements Consumer<Surface> {

  protected ExtendedExoPlayer(Context context, RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, BandwidthMeter bandwidthMeter, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, Looper looper) {
    super(context, renderersFactory, trackSelector, loadControl, bandwidthMeter, drmSessionManager, looper);
  }

  protected ExtendedExoPlayer(Context context, RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, BandwidthMeter bandwidthMeter, AnalyticsCollector.Factory analyticsCollectorFactory, Looper looper) {
    super(context, renderersFactory, trackSelector, loadControl, drmSessionManager, bandwidthMeter, analyticsCollectorFactory, looper);
  }

  protected ExtendedExoPlayer(Context context, RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, BandwidthMeter bandwidthMeter, AnalyticsCollector.Factory analyticsCollectorFactory, Clock clock, Looper looper) {
    super(context, renderersFactory, trackSelector, loadControl, drmSessionManager, bandwidthMeter, analyticsCollectorFactory, clock, looper);
  }

  @Override
  public final void accept(@Nullable Surface surface) {
    for (Renderer renderer : renderers)
      if (renderer instanceof MediaCodecVideoRenderer) {
        try {
          renderer.handleMessage(C.MSG_SET_SURFACE, surface);
        } catch (ExoPlaybackException e) {
          e.printStackTrace();
        }
      }
  }
}
