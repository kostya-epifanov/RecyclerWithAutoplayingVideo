package com.google.android.exoplayer2.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.Nullable;

/**
 * @author Konstantin Epifanov
 * @since 25.07.2019
 */
public class HandlerWrapperUtils {

  public static HandlerWrapper createHandlerWrapper(Looper looper, @Nullable Handler.Callback callback) {
    return new SystemHandlerWrapper(new Handler(looper, callback){
      @Override
      public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        //System.out.println("MESSAGE: " + msg.what);
        if (msg.what == 2) return super.sendMessageAtTime(msg, uptimeMillis);
        return callback.handleMessage(msg);
      }
    });
  }

}
