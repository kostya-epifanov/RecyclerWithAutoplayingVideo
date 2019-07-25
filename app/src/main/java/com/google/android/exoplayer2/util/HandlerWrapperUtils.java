package com.google.android.exoplayer2.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

/**
 * @author Konstantin Epifanov
 * @since 25.07.2019
 */
public class HandlerWrapperUtils {

  public static HandlerWrapper createHandlerWrapper(Looper looper, @Nullable Handler.Callback callback) {
    return new SystemHandlerWrapper(new Handler(looper, callback));
  }

}
