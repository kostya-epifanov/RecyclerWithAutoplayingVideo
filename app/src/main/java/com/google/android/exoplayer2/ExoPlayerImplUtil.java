package com.google.android.exoplayer2;

import android.content.Context;
import android.os.Looper;

import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.util.Clock;

/**
 * @author Konstantin Epifanov
 * @since 25.07.2019
 */
public class ExoPlayerImplUtil {

  public static SimpleExoPlayer createSimpleExoPlayer(Context context,
                                                      RenderersFactory renderersFactory, TrackSelector trackSelector,
                                                      LoadControl loadControl, BandwidthMeter bandwidthMeter,
                                                      AnalyticsCollector.Factory analyticsCollaborator, Clock clock, Looper looper) {
    return new SimpleExoPlayer(context, renderersFactory, trackSelector, loadControl,null, bandwidthMeter, analyticsCollaborator, clock, looper);
  }

}
