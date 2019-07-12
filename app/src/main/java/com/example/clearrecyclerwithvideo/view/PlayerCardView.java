package com.example.clearrecyclerwithvideo.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.data.Item;
import com.example.clearrecyclerwithvideo.utils.DrawableTarget;
import com.example.clearrecyclerwithvideo.utils.ExoHolder;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.function.Consumer;

/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class PlayerCardView extends FrameLayout implements Consumer<Item>, Checkable {

  private PlayerTextureView mTextureView;
  private TextView mLabelView;
  private DrawableTarget background;

  private Item data = null;
  private boolean isActive = false;

  private static final Runnable DUMMY_CLEANER = () -> {
  };
  private Runnable cleaner = DUMMY_CLEANER;

/*  public int verticalCenter = 0;
  private float cachedTranslationY = getTranslationY();
  private int cachedTop = getTop();
  private int cachedBottom = getBottom();*/

  public PlayerCardView(Context context) {
    this(context, null);
  }

  public PlayerCardView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PlayerCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    System.out.println("CONSTRUCOR");

    background = new DrawableTarget(getResources(), 16, -1, -1, Color.GREEN);
    setBackground(background);

    setClipToOutline(true);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mTextureView = findViewById(R.id.texture);
    mLabelView = findViewById(R.id.label_user_info);
  }

  public void setImageBackground(String url) {
    background.setData(url.getBytes());
  }

  public PlayerTextureView getTextureView() {
    return mTextureView;
  }

  public void setPlayer(SimpleExoPlayer player) {
    cleaner.run();
    cleaner = DUMMY_CLEANER;

    if (player != null) {

      VideoListener listener = new VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
          mTextureView.initialize(width, height);
        }

        @Override
        public void onRenderedFirstFrame() {
          mTextureView.setAlpha(1f);
        }
      };

      player.addVideoListener(listener);
      player.setVideoTextureView(mTextureView);

      cleaner = () -> {
        player.removeVideoListener(listener);
        mTextureView.setAlpha(0f);
      };
    }
  }

  @Override
  public void accept(Item item) {
    this.data = item;
    mLabelView.setText(String.format("pos[%s]\nurl[%s]", item.getText(), item.getUrl()));
    setImageBackground(item.getBackgroundUrl());
  }

  @Override
  public void setChecked(boolean checked) {
    if (checked == isActive) return;
    System.out.println("setChecked checked = [" + checked + "] " + this.hashCode());
    isActive = checked;
    setPlayer(isActive ? ExoHolder.get(getContext(), data.getUrl()) : null);
  }

  @Override
  public boolean isChecked() {
    return isActive;
  }

  @Override
  public void toggle() {
    isActive = !isActive;
  }

  /*
   * */

  /** {@inheritDoc} *//*
  @Override
  public void offsetTopAndBottom(int offset) {
    final boolean lower = getTop() > 0;
    final boolean upper = getBottom() < 0;

    super.offsetTopAndBottom(offset);

    final boolean newInline = getTop() < 0 && getBottom() > 0;

    if (newInline) {
      if (lower) setTranslationY(-getTop());
      else if (upper) setTranslationY(-getTop());
      else setTranslationY(getTranslationY() - offset);
    } else {
      setTranslationY(0);
    }
  }

  @Override public void setTranslationY(float translationY) {
    super.setTranslationY(translationY);
    float a = (1.5f * (3000.0f - getBottom()) / 1000.0f) - 0.75f;
    mLabelView.setAlpha(a);
  }
  */

/*  @Override
  public void setTranslationY(float translationY) {
    super.setTranslationY(translationY);
    if (cachedTranslationY != translationY) {
      cachedTranslationY = translationY;
      invalidateVerticalCenter();
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    final int top = getTop();
    final int bottom = getBottom();
    if (cachedTop != top || cachedBottom != bottom) {
      cachedTop = top;
      cachedBottom = bottom;
      invalidateVerticalCenter();
    }
  }

  private void invalidateVerticalCenter() {
    System.out.println("N verticalCenter: " + verticalCenter);
    System.out.println("N cachedTranslationY: " + cachedTranslationY);
    System.out.println("N cachedTop: " + cachedTop);
    System.out.println("N cachedBottom: " + cachedBottom);
    int r = (cachedBottom - cachedTop) >> 1;
    verticalCenter = Math.round(cachedTop + cachedTranslationY + r);
    System.out.println("N new verticalCenter: " + verticalCenter);
  }*/

}
