package com.example.clearrecyclerwithvideo.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.data.Item;
import com.example.clearrecyclerwithvideo.utils.DrawableTarget;
import com.example.clearrecyclerwithvideo.utils.ExoHolder;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.function.Consumer;

import reactor.core.Disposable;
import reactor.core.Disposables;

/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class PlayerCardView extends FrameLayout implements Consumer<Item>, Checkable {

  private Disposable.Swap mSwap = null;

  private PlayerTextureView mTextureView;
  private TextView mLabelView;
  private DrawableTarget background;

  private Item data = null;
  private boolean isActive = false;

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

  @Override
  public void accept(Item item) {
    System.out.println("accept item = [" + item + "]");

    this.data = item;

    if (mSwap != null) mSwap.dispose();
    mSwap = null;

    mTextureView.setAlpha(0f);

    if (item != null) {
      mSwap = Disposables.swap();
      mLabelView.setText(String.format("pos[%s]\nurl[%s]", item.getText(), item.getUrl()));
      setImageBackground(item.getBackgroundUrl());
    }

    invalidateState(true);
  }

  @Override
  public void setChecked(boolean checked) {
    if (checked == isActive) return;

    System.out.println("setChecked checked = [" + checked + "] " + this.hashCode());

    isActive = checked;

    invalidateState(false);
  }

  private void invalidateState(boolean immediateFade) {
    if (mSwap != null && data != null) {
      mSwap.update(setPlayer(isActive ? ExoHolder.get(getContext(), data.getUrl()) : null, immediateFade));
    }
  }

  public Disposable setPlayer(SimpleExoPlayer player, boolean immediateFade) {
    return player == null ?
      Disposables.single() : setPlayerInternal(player, mTextureView, immediateFade);
  }

  private static Disposable setPlayerInternal(@NonNull SimpleExoPlayer player, PlayerTextureView texture, boolean immediateFade){
    final Disposable.Swap swap = Disposables.swap();

    final Disposable d = immediateFade ? () -> texture.setAlpha(0f) : executeAnimation(alphaTo(texture, false));

    return Disposables.composite(
      getVideoSize(player, point -> texture.initialize(point.x, point.y)),
      getFirstFrame(player, () -> swap.update(executeAnimation(alphaTo(texture, true)))),
      setupTextureView(player, texture),
      d,
      //executeAnimation(alphaTo(texture, false)), //todo подумать
      //() -> texture.setAlpha(0f),
      swap
    );
  }

  private static ViewPropertyAnimator alphaTo(View view, boolean state) {
    System.out.println("PlayerCardView.alphaTo: (" +"): " + state);
    return view.animate()
      .alpha(state ? 1f : 0f)
      .setDuration(200);
  }

  private static Disposable executeAnimation(ViewPropertyAnimator animator) {
    animator.start();
    return Disposables.composite(animator::cancel);
  }

  private static Disposable getVideoSize(SimpleExoPlayer player, Consumer<Point> consumer) {
    final VideoListener listener = new VideoListener() {
      @Override
      public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        consumer.accept(new Point(width, height));
      }
    };

    player.addVideoListener(listener);
    return Disposables.composite((Disposable) () -> player.removeVideoListener(listener));
  }

  private static Disposable getFirstFrame(SimpleExoPlayer player, Runnable runnable) {
    final Disposable.Composite composite = Disposables.composite();
    final VideoListener listener = new VideoListener() {
      @Override
      public void onRenderedFirstFrame() {
        runnable.run();
        composite.dispose();
      }
    };

    player.addVideoListener(listener);
    composite.add(() -> player.removeVideoListener(listener));
    return composite;
  }

  private static Disposable setupTextureView(SimpleExoPlayer player, PlayerTextureView texture) {
    player.setVideoTextureView(texture);
    return Disposables.composite((Disposable) () -> player.setVideoTextureView(null));
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
