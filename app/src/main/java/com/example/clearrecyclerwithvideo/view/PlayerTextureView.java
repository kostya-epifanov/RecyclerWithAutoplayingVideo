package com.example.clearrecyclerwithvideo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.example.clearrecyclerwithvideo.utils.RatioKeeper;


public final class PlayerTextureView extends TextureView {

  /** Aspect Ratio Keeper. */
  private final RatioKeeper mRatioKeeper =
    new RatioKeeper(this::setTransform);

  public PlayerTextureView(Context context) {
    super(context);
  }

  public PlayerTextureView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public PlayerTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public PlayerTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public void initialize(int width, int height) {
    mRatioKeeper.videoSize(width, height);
  }

  /** {@inheritDoc} */
  @Override
  protected final void onSizeChanged(int nw, int nh, int ow, int oh) {
    mRatioKeeper.viewPort(nw, nh);
    super.onSizeChanged(nw, nh, ow, oh);
  }

  /** {@inheritDoc} */
  @Override
  public final void setScaleX(float value) {
    mRatioKeeper.scaleX(value);
    super.setScaleX(value);
  }

  @Override
  public final void setScaleY(float value) {
    mRatioKeeper.scaleY(value);
    super.setScaleY(value);
  }


}
