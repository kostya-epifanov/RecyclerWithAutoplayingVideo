package com.example.clearrecyclerwithvideo.utils.player;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.clearrecyclerwithvideo.utils.reactor.Schedule;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.util.HandlerWrapper;
import com.google.android.exoplayer2.util.HandlerWrapperUtils;

/**
 * @author Konstantin Epifanov
 * @since 25.07.2019
 * The implementation of {@link Clock} with custom looper.
 */
public final class WorkLooperClock implements Clock {

  @Override
  public long elapsedRealtime() {
    return android.os.SystemClock.elapsedRealtime();
  }

  @Override
  public long uptimeMillis() {
    return android.os.SystemClock.uptimeMillis();
  }

  @Override
  public void sleep(long sleepTimeMs) {
    android.os.SystemClock.sleep(sleepTimeMs);
  }

  @Override
  public HandlerWrapper createHandler(Looper looper, @Nullable Handler.Callback callback) {
    return HandlerWrapperUtils.createHandlerWrapper(Schedule.WORK_LOOPER, callback);
  }

}
