package com.example.clearrecyclerwithvideo.utils;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

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

  private ExoHolder() { }

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

}
