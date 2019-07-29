package com.example.clearrecyclerwithvideo.view.playercardview2;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.data.DataService;
import com.example.clearrecyclerwithvideo.utils.android.DrawableTarget;
import com.example.clearrecyclerwithvideo.utils.player.ExoHolder;
import com.example.clearrecyclerwithvideo.view.PlayerTextureView;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.function.Consumer;

import reactor.core.Disposable;
import reactor.core.Disposables;

/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class PlayerCardView_2 extends FrameLayout implements Consumer<DataService.UrlHolder>, Checkable {

  private PlayerTextureView mTextureView;
  private TextView mLabelView1, mLabelView2;
  private DrawableTarget mBackground;

  private DataService.UrlHolder mData = null;
  private boolean mIsChecked = false;

  private Disposable.Swap mSwap = null;

  private PlayerCardPresenter mPresenter;

  public PlayerCardView_2(Context context) {
    this(context, null);
  }

  public PlayerCardView_2(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PlayerCardView_2(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    System.out.println("CONSTRUCOR");

    mBackground = new DrawableTarget(getResources(), 16, -1, -1, Color.GREEN);
    setBackground(mBackground);

    setClipToOutline(true);

    mPresenter = new PlayerCardPresenter(this);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mTextureView = findViewById(R.id.texture);
    mLabelView1 = findViewById(R.id.label_user_info);
    mLabelView2 = findViewById(R.id.label_user_info_2);
  }

  public void forcedFade(boolean isFaded) {
    this.animate()
      .alpha(isFaded ? 0f : 1f)
      .setDuration(1000)
      .setListener(new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
          System.out.println("onAnimationStart = [" + animation.hashCode() + "]");
        }

        @Override
        public void onAnimationEnd(Animator animation) {
          System.out.println("onAnimationEnd = [" + animation.hashCode() + "]");
        }

        @Override
        public void onAnimationCancel(Animator animation) {
          System.out.println("onAnimationCancel = [" + animation.hashCode() + "]");
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
          System.out.println("onAnimationRepeat = [" + animation.hashCode() + "]");
        }
      })
      .start();
  }

  public void instantAlpha1f() {
    System.out.println("instantAlpha1f");
    this.setAlpha(1f);
  }

  @Override
  public void accept(DataService.UrlHolder item) {
    System.out.println("accept [" + item + "] " + hashCode());

    this.mData = item;

    if (mSwap != null) mSwap.dispose();
    mSwap = null;

    mTextureView.setAlpha(0f);

    if (item != null) {
      mSwap = Disposables.swap();
      mBackground.setData(item.getScreenshotUrl().getBytes());
    }

    mLabelView1.setText("ITEM: " + item);

    invalidateState(true);
  }

  @Override
  public void setChecked(boolean checked) {
    if (checked == mIsChecked) return;

    System.out.println("setChecked checked = [" + checked + "] " + this.hashCode());

    mIsChecked = checked;

    mLabelView2.setText(String.format("isChecked: " + mIsChecked));
    mPresenter.onCheckedStateChanged(mIsChecked);

    invalidateState(false);
  }

  private void invalidateState(boolean immediateFade) {
    System.out.println("invalidateState immediateFade = [" + immediateFade + "] " + hashCode());
    if (mSwap != null && mData != null) {
      System.out.println("swap update: active " + mIsChecked + " " + hashCode());
      mSwap.update(setPlayer(
        mIsChecked ? ExoHolder.get(getContext(), mData.getVideoUrl()) : null,
        immediateFade));
    }
  }

  public Disposable setPlayer(SimpleExoPlayer player, boolean immediateFade) {
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

    System.out.println("PlayerCardView_2.setPlayerInternal: " + player.hashCode());

    return Disposables.composite(
      getVideoSize(player, point -> texture.initialize(point.x, point.y)),
      //getFirstFrame(player, () -> swap.replace(executeAnimation(alphaTo(texture, true)))),
      getFirstFrame(player, () -> texture.setAlpha(1f)),
      setupTextureView(player, texture),
      swap
    );
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
    //player.setVideoTextureView(texture);
    return Disposables.composite((Disposable) () -> {
      System.out.println("PlayerCardView.setupTextureView: dispose");
      //player.setVideoTextureView(null);
    });
  }

  @Override
  public boolean isChecked() {
    return mIsChecked;
  }

  @Override
  public void toggle() {
    mIsChecked = !mIsChecked;
  }

  public PlayerTextureView getTextureView() {
    return mTextureView;
  }

}
