package com.example.clearrecyclerwithvideo.view;

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

import java.text.MessageFormat;
import java.util.function.Consumer;

import reactor.core.Disposable;
import reactor.core.Disposables;

/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class PlayerCardView extends FrameLayout implements Consumer<Item>, Checkable {

  private PlayerTextureView mTextureView;
  private TextView mLabelView;
  private DrawableTarget mBackground;

  private Item mDataItem = null;
  private boolean mIsActive = false;

  private Disposable.Swap mSwap = null;

/*  private Runnable mPendingCheckedRunnable = null;*/

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

    mBackground = new DrawableTarget(getResources(), 16, -1, -1, Color.GREEN);
    setBackground(mBackground);

    setClipToOutline(true);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mTextureView = findViewById(R.id.texture);
    mLabelView = findViewById(R.id.label_user_info);
  }

  @Override
  public void accept(Item item) {
    System.out.println("accept [" + item + "] " + hashCode());

    this.mDataItem = item;

    if (mSwap != null) mSwap.dispose();
    mSwap = null;

    mTextureView.setAlpha(0f);

    if (item != null) {
      mSwap = Disposables.swap();
      mLabelView.setText(MessageFormat.format("HASH: {0}", hashCode()));
      mBackground.setData(item.getBackgroundUrl().getBytes());
      //mLabelView.setText(String.format("pos[%s]\nurl[%s]", item.getText(), item.getUrl()));
    }

    invalidateState(true);
  }

  @Override
  public void setChecked(boolean checked) {
    if (checked == mIsActive) return;

    System.out.println("setChecked checked = [" + checked + "] " + this.hashCode());

    mIsActive = checked;
    invalidateState(false);
  }

  private void invalidateState(boolean immediateFade) {
    System.out.println("invalidateState immediateFade = [" + immediateFade + "] " + hashCode());
    if (mSwap != null && mDataItem != null) {
      System.out.println("swap update: active " + mIsActive + " " + hashCode());
      mSwap.update(setPlayer(
        mIsActive ? ExoHolder.get(getContext(), mDataItem.getUrl()) : null,
        immediateFade));
    }
  }

  private Disposable setPlayer(SimpleExoPlayer player, boolean immediateFade) {
    System.out.println("setPlayer: p.isNull = [" + (player == null) + "], im_f = [" + immediateFade + "] " + hashCode());

    if (player == null) {
      //mTextureView.setAlpha(0f);
      /*if (immediateFade) mTextureView.setAlpha(0f);
      else executeAnimation(alphaTo(mTextureView, false));
      //TODO анимация мгновенно канселтся после начала. выяснить причину.*/
    }

    return player == null ?
      Disposables.single() : setPlayerInternal(player, mTextureView);
  }

  private static Disposable setPlayerInternal(@NonNull SimpleExoPlayer player, PlayerTextureView texture) {
    final Disposable.Swap swap = Disposables.swap();

    return Disposables.composite(
      getVideoSize(player, point -> texture.initialize(point.x, point.y)),
      getFirstFrame(player, () -> swap.update(executeAnimation(alphaTo(texture, true)))),
      setupTextureView(player, texture),
      swap
    );
  }

  private static ViewPropertyAnimator alphaTo(View view, boolean state) {
    System.out.println("PlayerCardView.alphaTo: (" + view.getParent().hashCode() + "): " + state);
    return view.animate()
      .alpha(state ? 1f : 0f)
      .setDuration(200);
  }

  private static Disposable executeAnimation(ViewPropertyAnimator animator) {
    animator.start();
    return Disposables.composite(() -> {
      System.out.println("animation CANCEL");
      animator.cancel();
    });
  }

  private static Disposable getVideoSize(SimpleExoPlayer player, Consumer<Point> consumer) {
    final VideoListener listener = new VideoListener() {
      @Override
      public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        consumer.accept(new Point(width, height));
      }
    };

    player.addVideoListener(listener);
    return Disposables.composite((Disposable) () -> {
      System.out.println("PlayerCardView.getVideoSize: dispose");
      player.removeVideoListener(listener);
    });
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
    composite.add(() -> {
      System.out.println("PlayerCardView.getFirstFrame: dispose");
      player.removeVideoListener(listener);
    });
    return composite;
  }

  private static Disposable setupTextureView(SimpleExoPlayer player, PlayerTextureView texture) {
    player.setVideoTextureView(texture);
    return Disposables.composite((Disposable) () -> {
      System.out.println("PlayerCardView.setupTextureView: dispose");
      player.setVideoTextureView(null);
    });
  }

  @Override
  public boolean isChecked() {
    return mIsActive;
  }

  @Override
  public void toggle() {
    mIsActive = !mIsActive;
  }

  public void forcedFade() {
    executeAnimation(alphaTo(mTextureView, false));
  }

  /*
  *
  @Override
  public void setChecked(boolean checked) {
    if (checked) setCheckedInternal(true, 200);
    else setCheckedInternal(false, 0);
  }

  private void setCheckedInternal(boolean checked, long delay) {
    removeCallbacks(mPendingCheckedRunnable);
    postDelayed(
      mPendingCheckedRunnable = () -> {
        if (checked == mIsActive) return;
        System.out.println("setChecked checked = [" + checked + "] " + this.hashCode());
        mIsActive = checked;
        invalidateState(false);
      }, delay);
  }
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
