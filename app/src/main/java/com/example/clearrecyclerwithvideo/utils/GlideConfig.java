/*
 * GlideConfig.java
 * webka
 *
 * Copyright (C) 2019, Realtime Technologies Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the
 * property of Realtime Technologies Limited and its SUPPLIERS, if any.
 *
 * The intellectual and technical concepts contained herein are
 * proprietary to Realtime Technologies Limited and its suppliers and
 * may be covered by Russian Federation and Foreign Patents, patents
 * in process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Realtime Technologies Limited.
 */

package com.example.clearrecyclerwithvideo.utils;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;

/**
 * @author Nikitenko Gleb
 * @since 1.0, 09/10/2018
 */
@SuppressWarnings("unused")
@GlideModule
@Keep
public final class GlideConfig extends AppGlideModule {

  /** {@inheritDoc} */
  @Override
  public final void applyOptions
  (@NonNull Context context, @NonNull GlideBuilder builder) {
    builder.setMemorySizeCalculator(
      new MemorySizeCalculator.Builder(context)
        .setBitmapPoolScreens(6)
        .setMemoryCacheScreens(6)
    );
  }
}
