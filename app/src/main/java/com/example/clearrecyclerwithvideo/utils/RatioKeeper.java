package com.example.clearrecyclerwithvideo.utils;

import android.graphics.Matrix;

import java.util.function.Consumer;

/**
 * Aspect Ratio Keeper.
 *
 * @author Gleb Nikitenko
 * @since 10.07.19
 */
public final class RatioKeeper {

  private static final int VIDEO_WIDTH = 0;
  private static final int VIDEO_HEIGHT = 1;
  private static final int VIEW_WIDTH = 2;
  private static final int VIEW_HEIGHT = 3;
  private static final int SCALE_X = 4;
  private static final int SCALE_Y = 5;

  /** Transformation Matrix. */
  private final Matrix mMatrix = new Matrix();

  /** Sizes container */
  private final float[] mSizes;

  /** Matrix applier. */
  private final Consumer<Matrix> mApplier;

  /**
   * Constructs a new {@link RatioKeeper}.
   *
   * @param applier matrix applier.
   */
  public RatioKeeper(Consumer<Matrix> applier) {
    mSizes = new float[]{0f, 0f, 0f, 0f, 1f, 1f};
    mApplier = applier;
  }

  /**
   * @param width  video width
   * @param height video height
   */
  public final void videoSize(int width, int height) {
    if ((int) mSizes[VIDEO_WIDTH] != width || (int) mSizes[VIDEO_HEIGHT] != height) {
      mSizes[VIDEO_WIDTH] = width;
      mSizes[VIDEO_HEIGHT] = height;
      invalidate();
    }
  }

  /**
   * @param width  view width
   * @param height view height
   */
  public final void viewPort(int width, int height) {
    if ((int) mSizes[VIEW_WIDTH] != width || (int) mSizes[VIEW_HEIGHT] != height) {
      mSizes[VIEW_WIDTH] = width;
      mSizes[VIEW_HEIGHT] = height;
      invalidate();
    }
  }

  /** @param value horizontal scale */
  public final void scaleX(float value) {
    if (mSizes[SCALE_X] != value) {
      mSizes[SCALE_X] = value;
      invalidate();
    }
  }

  /** @param value vertical scale */
  public final void scaleY(float value) {
    if (mSizes[SCALE_Y] != value) {
      mSizes[SCALE_Y] = value;
      invalidate();
    }
  }

  /** Invalidate the matrix */
  private void invalidate() {

    // Backup sizes before fit
    final float
      videoWidth = mSizes[VIDEO_WIDTH],
      videoHeight = mSizes[VIDEO_HEIGHT],
      viewWidth = mSizes[VIEW_WIDTH],
      viewHeight = mSizes[VIEW_HEIGHT];

    // Calc fitting for matrix
    fit(mSizes);

    // Apply fitting on matrix
    mMatrix.setScale(
      mSizes[VIDEO_WIDTH],
      mSizes[VIDEO_HEIGHT],
      mSizes[VIEW_WIDTH],
      mSizes[VIEW_HEIGHT]
    );

    // Restore sizes back to transform params
    mSizes[VIDEO_WIDTH] = videoWidth;
    mSizes[VIDEO_HEIGHT] = videoHeight;
    mSizes[VIEW_WIDTH] = viewWidth;
    mSizes[VIEW_HEIGHT] = viewHeight;

    // Apply new modified matrix for rendering
    mApplier.accept(mMatrix);
  }

  /** @param value floats container */
  private static void fit(float[] value) {
    float a = value[VIDEO_WIDTH] * value[VIEW_HEIGHT];
    float b = value[VIDEO_HEIGHT] * value[VIEW_WIDTH];

    value[VIDEO_WIDTH] = value[SCALE_X];
    value[VIDEO_HEIGHT] = value[SCALE_Y];

    value[VIEW_WIDTH] *= 0.5f;
    value[VIEW_HEIGHT] *= 0.5f;

    if (a > b) value[VIDEO_WIDTH] = a / b;
    else value[VIDEO_HEIGHT] = b / a;
  }
}
