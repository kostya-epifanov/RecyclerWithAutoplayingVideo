/*
 * DrawableTarget.java
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

package com.example.clearrecyclerwithvideo.utils.android;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static android.graphics.Shader.TileMode.CLAMP;
import static java.lang.Math.round;
import static java.util.Optional.ofNullable;

/**
 * @author Nikitenko Gleb
 * @since 1.0, 22/09/2018
 */
public class DrawableTarget extends Drawable implements Target<Bitmap> {

  /** Rounded modes. */
  public static final int SQUARE = -1, CIRCLE = 0;
  /** The log cat tag. */
  private static final String TAG = "TARGET";
  /** Glide options. */
  private static final RequestOptions GLIDE_OPTIONS =
    RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)
      .skipMemoryCache(false).dontAnimate().dontTransform()
      .format(DecodeFormat.PREFER_RGB_565)
      .downsample(DownsampleStrategy.AT_LEAST);

  /** Drawable paint */
  private final Paint mPaint = new Paint
    (Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

  /** Rounded Mode. */
  private final int mRounded;
  /** Draw bounds. */
  private final RectF mDrawRect = new RectF();
  /** Outline rect. */
  private final Rect mOutlineRect = new Rect();
  /** Rounded values. */
  private final PointF mRoundPoint = new PointF();
  /** Intrinsic size. */
  private final Point mIntrinsicSize;

  /** Mirror matrix. */
  private final Matrix mMirrorMatrix = new Matrix();

  /** Glide request options. */
  private final RequestOptions mOptions;

  /** Auto-mirroring. */
  private boolean mAutoMirrored = false;

  /** Glide request. */
  @Nullable
  private Request mRequest;

  /** Image data. */
  @Nullable
  private byte[] mData = null;

  /** Current animation. */
  @Nullable
  private Runnable mAnimation = null;

  /** Animate this pass. */
  private boolean mFirstShow = false;

  /** Background color. */
  private int mBackgroundColor;

  /**
   * Constructs a new {@link DrawableTarget}.
   *
   * @param rounded rounded mode
   */
  public DrawableTarget
  (@NonNull Resources resources, int rounded, int width, int height, int color) {
    final DisplayMetrics metrics = resources.getDisplayMetrics();
    mRounded = rounded > CIRCLE ? round(metrics.density * (float) rounded) : rounded;
    width = width == -1 ? width : round(metrics.density * (float) width);
    height = height == -1 ? height : round(metrics.density * (float) height);
    mIntrinsicSize = new Point(width, height);
    mPaint.setAlpha(0);
    mOptions = GLIDE_OPTIONS.clone();
    //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    mBackgroundColor = color;
  }

  @NonNull
  public static Consumer<byte[]>
  textViewLeftCircle(@NonNull TextView view, int size) {
    final DrawableTarget result = new DrawableTarget
      (view.getResources(), CIRCLE, size, size, Color.TRANSPARENT);
    view.setCompoundDrawablesWithIntrinsicBounds(result, null, null, null);
    return result::setData;
  }

  private static RequestBuilder<Bitmap> load
    (@NonNull RequestBuilder<Bitmap> builder, @Nullable byte[] data) {
    return (data == null || sig(data) != 7526768903561293615L) ?
      builder.load(data) : builder.load(new String(data, Charset.defaultCharset()));
  }

  @SuppressWarnings("PointlessBitwiseExpression")
  private static long sig(@NonNull byte[] bytes) {
    return
      ((bytes[0] & 0xFFL) << 0x38) |
        ((bytes[1] & 0xFFL) << 0x30) |
        ((bytes[2] & 0xFFL) << 0x28) |
        ((bytes[3] & 0xFFL) << 0x20) |
        ((bytes[4] & 0xFFL) << 0x18) |
        ((bytes[5] & 0xFFL) << 0x10) |
        ((bytes[6] & 0xFFL) << 0x08) |
        ((bytes[7] & 0xFFL) << 0x00);
  }

  /**
   * @param drawable drawable instance
   *
   * @return glide request manager
   */
  @NonNull
  private static Optional<RequestManager> glide(@NonNull Drawable drawable) {
    return ofNullable(getView(drawable)).map(view ->
      Glide.with(view.getContext().getApplicationContext()));
  }

  /* @return related context */
  @Nullable
  private static View getView(@NonNull Drawable drawable) {
    final Callback callback = drawable.getCallback();
    return callback instanceof View ? (View) callback :
      (callback instanceof Drawable) ?
        getView((Drawable) callback) : null;
  }

  /**
   * @param consumer callbacks
   *
   * @return cancellation
   */
  @NonNull
  private static Runnable animator(@NonNull IntConsumer consumer) {
    final ValueAnimator[] animator = new ValueAnimator[]
      {ValueAnimator.ofInt(0, 255).setDuration(600)};
    final AnimatorUpdateListener updates = animation ->
      consumer.accept((int) animation.getAnimatedValue());
    final BiConsumer<AnimatorListener, Animator> finalizer = (list, anim) -> {
      animator[0] = null;
      ((ValueAnimator) anim).removeUpdateListener(updates);
      anim.removeListener(list);
      consumer.accept(-1);
    };
    animator[0].setInterpolator(new FastOutSlowInInterpolator());
    animator[0].addListener(new AnimatorListenerAdapter() {
      @Override
      public final void onAnimationEnd(Animator animation) {
        finalizer.accept(this, animation);
      }
    });
    animator[0].addUpdateListener(updates);
    try {
      return () -> ofNullable(animator[0]).ifPresent(ValueAnimator::end);
    } finally {
      animator[0].start();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void onLoadStarted(@Nullable Drawable drawable) {
    mFirstShow = true;
  }

  /** {@inheritDoc} */
  @Override
  public final void
  onLoadFailed(@Nullable Drawable drawable) {
    onLoadCleared(drawable);
  }

  /** {@inheritDoc} */
  @Override
  public final void onResourceReady
  (@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
    resource.prepareToDraw();
    mPaint
      .setShader(new BitmapShader(resource))
      .setLocalMatrix(BitmapShader.matrix(resource, mDrawRect));


    if (mFirstShow)
      mAnimation = animator(value -> {
        if (value != -1) {
          mPaint.setAlpha(value);
          invalidateSelf();
        } else mAnimation = null;
      });
    else {
      mPaint.setAlpha(255);
      invalidateSelf();
    }
    mFirstShow = false;
  }

  /** {@inheritDoc} */
  @Override
  public final void onLoadCleared(@Nullable Drawable placeholder) {
    final BitmapShader shader = (BitmapShader) mPaint.getShader();
    if (shader == null) return;
    mPaint.setShader(null);
    shader.setLocalMatrix(null);
  }

  /**
   * A method to retrieve the size of this target.
   *
   * @param callback The callback that must be called when the size of the target has been determined
   */
  @Override
  public final void getSize(@NonNull SizeReadyCallback callback) {
  }

  /**
   * Removes the given callback from the pending set if it's still retained.
   *
   * @param callback The callback to remove.
   */
  @Override
  public final void removeCallback(@NonNull SizeReadyCallback callback) {
  }

  /** {@inheritDoc} */
  @Nullable
  @Override
  public final Request getRequest() {
    return mRequest;
  }

  /** {@inheritDoc} */
  @Override
  public final void setRequest(@Nullable Request request) {
    mRequest = request;
  }

  /** {@inheritDoc} */
  @Override
  public final void onStart() {
  }

  /** {@inheritDoc} */
  @Override
  public final void onStop() {
  }

  /** {@inheritDoc} */
  @Override
  public final void onDestroy() {
  }

  /** {@inheritDoc} */
  @Override
  public final void draw(@NonNull Canvas canvas) {
    if (mBackgroundColor != Color.TRANSPARENT && mPaint.getAlpha() < 255)
      canvas.drawColor(mBackgroundColor);
    if (mPaint.getShader() != null)
      if (mRounded == SQUARE) canvas.drawRect(mDrawRect, mPaint);
      else canvas.drawRoundRect(mDrawRect, mRoundPoint.x, mRoundPoint.y, mPaint);
  }

  /** {@inheritDoc} */
  @Override
  public final int getAlpha() {
    return mPaint.getAlpha();
  }

  /** {@inheritDoc} */
  @Override
  public final void setAlpha(int alpha) {
    if (alpha == mPaint.getAlpha()) return;
    mPaint.setAlpha(alpha);
    invalidateSelf();
  }

  /** {@inheritDoc} */
  @Nullable
  @Override
  public final ColorFilter getColorFilter() {
    return mPaint.getColorFilter();
  }

  /** {@inheritDoc} */
  @Override
  public final void setColorFilter(@Nullable ColorFilter filter) {
    if (mPaint.getColorFilter() == filter) return;
    mPaint.setColorFilter(filter);
    invalidateSelf();
  }

  /** {@inheritDoc} */
  @Override
  public final int getOpacity() {
    return mRounded == SQUARE ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
  }

  /** {@inheritDoc} */
  @Override
  protected final void onBoundsChange(@NonNull Rect bounds) {
    if (mRounded == CIRCLE) mRoundPoint.set
      (bounds.width() * 0.5f, bounds.height() * 0.5f);
    else if (mRounded == SQUARE) mRoundPoint.set(0, 0);
    else mRoundPoint.set(mRounded, mRounded);
    mDrawRect.set(bounds);

    final DrawableTarget target = this;
    glide(target).ifPresent(glide -> glide.clear(target));

    invalidate();
  }

  /** @param data image data */
  public final void setData(@NonNull byte[] data) {
    if (mData == data) return;
    mData = data;
    invalidate();
  }

  /** Invalidate data and size */
  private void invalidate() {
    final Rect bounds = getBounds();
    final DrawableTarget target = this;
    glide(target).ifPresent(glide -> load(glide.asBitmap(), mData)
      .apply(mOptions.override(bounds.width(), bounds.height()))
      .into(target));
  }

  /** {@inheritDoc} */
  @Override
  public final int getIntrinsicWidth() {
    return mIntrinsicSize.x;
  }

  /** {@inheritDoc} */
  @Override
  public final int getIntrinsicHeight() {
    return mIntrinsicSize.y;
  }

  /** {@inheritDoc} */
  @Override
  public final void getOutline(@NonNull Outline outline) {
    mDrawRect.round(mOutlineRect);
    if (mRounded == SQUARE) outline.setRect(mOutlineRect);
    else outline.setRoundRect(mOutlineRect, mRoundPoint.x);
    final BitmapShader shader = (BitmapShader) mPaint.getShader();
    final boolean opaque = shader != null && !shader.hasAlpha;
    outline.setAlpha(opaque ? getAlpha() / 255.0f : 0.0f);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isFilterBitmap() {
    return mPaint.isFilterBitmap();
  }

  /** {@inheritDoc} */
  @Override
  public final void setFilterBitmap(boolean filter) {
    if (mPaint.isFilterBitmap() == filter) return;
    mPaint.setFilterBitmap(filter);
    invalidateSelf();
  }

  /** {@inheritDoc} */
  @SuppressWarnings("deprecation")
  @Override
  public final void setDither(boolean dither) {
    if (mPaint.isDither() == dither) return;
    mPaint.setDither(dither);
    invalidateSelf();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isAutoMirrored() {
    return mAutoMirrored;
  }

  /** {@inheritDoc} */
  @Override
  public final void setAutoMirrored(boolean mirrored) {
    if (mAutoMirrored == mirrored) return;
    mAutoMirrored = mirrored;
    mMirrorMatrix.reset();
    if (isAutoMirrored() && getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
      mMirrorMatrix.setTranslate(mDrawRect.right - mDrawRect.left, 0);
      mMirrorMatrix.setScale(-1, 1);
    }
    invalidateSelf();
  }

  /** Internal bitmap shader. */
  private static final class BitmapShader extends android.graphics.BitmapShader {

    /** Bitmap alpha. */
    final boolean hasAlpha;
    /** Bitmap. */
    private final Bitmap mBitmap;

    /**
     * Call this to createHandlerWrapper a new shader that will draw with a bitmap.
     *
     * @param bitmap The bitmap to use inside the shader
     */
    BitmapShader(@NonNull Bitmap bitmap) {
      super(bitmap, CLAMP, CLAMP);
      hasAlpha = bitmap.hasAlpha();
      mBitmap = bitmap;
    }

    /**
     * @param bitmap picture bitmap
     * @param bounds picture bounds
     *
     * @return fit matrix
     */
    @NonNull
    static Matrix matrix
    (@NonNull Bitmap bitmap, @NonNull RectF bounds) {
      return new Matrix() {{
        final float
          sw = bitmap.getWidth(),
          sh = bitmap.getHeight(),
          dw = bounds.width(),
          dh = bounds.height();

        float scale, dx = 0, dy = 0;

        if (sw * dh > dw * sh) {
          scale = dh / sh;
          dx = (dw - sw * scale) * 0.5f;
        } else {
          scale = dw / sw;
          dy = (dh - sh * scale) * 0.5f;
        }

        setScale(scale, scale);
        postTranslate(dx, dy);
      }};
    }

    @SuppressWarnings("unused")
    final void update(@NonNull RectF bounds) {
      setLocalMatrix(matrix(mBitmap, bounds));
    }

  }

}
