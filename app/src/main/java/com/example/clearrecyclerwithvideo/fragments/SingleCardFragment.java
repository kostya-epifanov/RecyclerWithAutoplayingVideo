package com.example.clearrecyclerwithvideo.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.clearrecyclerwithvideo.Constants;
import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.data.DataService;
import com.example.clearrecyclerwithvideo.utils.player.ExoHolder;
import com.example.clearrecyclerwithvideo.utils.reactor.Schedule;
import com.example.clearrecyclerwithvideo.view.PlayerTextureView;
import com.example.clearrecyclerwithvideo.view.playercardview2.PlayerCardView_2;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import reactor.core.Disposable;


/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class SingleCardFragment extends Fragment {

  private PlayerCardView_2 mCardView;
  private View mFade, mCheck, mTest;
  private View mInstant, mAcceptBomb, mCheckedBomb;

  private SimpleExoPlayer mPlayer;
  private PlayerTextureView mTextureTest;

  private Disposable mDisposable;

  private boolean isFaded = false;

  public static SingleCardFragment newInstance() {
    return new SingleCardFragment();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_single, container, false);

    mCardView = root.findViewById(R.id.include);
    mFade = root.findViewById(R.id.button_fade);
    mInstant = root.findViewById(R.id.button_instant);
    mCheck = root.findViewById(R.id.button_check);
    mAcceptBomb = root.findViewById(R.id.button_accept_bomb);
    mCheckedBomb = root.findViewById(R.id.button_checked_bomb);
    mTest = root.findViewById(R.id.button_test);

    mTextureTest = root.findViewById(R.id.texture_test);

    mFade.setOnClickListener(v -> fade());
    mInstant.setOnClickListener(v -> instant());
    mCheck.setOnClickListener(v -> check());
    mAcceptBomb.setOnClickListener(v -> bombAccept());
    mCheckedBomb.setOnClickListener(v -> bombCheck());

    mTest.setOnClickListener(v -> test(getContext()));

    /*
    Schedule.WORK_EXECUTOR.execute(() -> {
      System.out.println("PLAYER CREATE 1 START");
      mPlayer = (SimpleExoPlayer) ExoHolder.getPlayer(getContext());
      System.out.println("PLAYER CREATE 1 FINISHED");
    });
    */

    ExoHolder.setupCache(getContext());

    mDisposable =
      ExoHolder.getFromCache(getContext(), Constants.urls.get(0).getVideoUrl())
        .log()
        .subscribe(player -> {
          mPlayer = (SimpleExoPlayer) player;
          mCardView.setPlayer(mPlayer, true);
        });

    /*

    mPlayer.addVideoListener(new VideoListener() {
      @Override
      public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        System.out.println("SingleCardFragment.onVideoSizeChanged");
        Point point = new Point(width, height);
        mCardView.getTextureView().initialize(point.x, point.y);
      }

      @Override
      public void onRenderedFirstFrame() {
        System.out.println("SingleCardFragment.onRenderedFirstFrame");
        mCardView.getTextureView().setAlpha(1f);
        mPlayer.setVideoTextureView(mCardView.getTextureView());
      }
    });

    */

    return root;
  }

  private void test(Context context) {

    /*Schedule.WORK_EXECUTOR.execute(() -> {
      System.out.println("OTHER THREAD 1 START");
      mPlayer.prepare(ExoHolder.buildMediaSource(context, Uri.parse(Constants.urls.get(0).getVideoUrl())));
      mCardView.setPlayer(mPlayer, true);
      System.out.println("OTHER THREAD 1 FINISHED");
    });
*/
    // mPlayer.release();

    /*
    Schedule.WORK_EXECUTOR.execute(() -> {
      System.out.println("OTHER THREAD 2 START");
      mPlayer.release();
      System.out.println("OTHER THREAD 2 FINISHED");
    })
    ;*/


    // TEST 1: Start player on other thread, release on main:
    // RESULT 1: Стартует тред (ошибка 2 раза, но работает), потом релизит, затем тред кончается с "Ignoring messages sent after release"

    // TEST 2: Start on other, release on other
    // RESULT 2: 1 start - IllegalStateException - 1 finished, 2 start - release - finish

    // TEST 3: Без ошибок если плеер создан в том же лупере что и остальные

    // Ошибка:
    // result1: Player is accessed on the wrong thread.
    // See https://google.github.io/ExoPlayer/faqs.html#what-do-player-is-accessed-on-the-wrong-thread-warnings-mean
    // java.lang.IllegalStateException

    /*
    * What do “Player is accessed on the wrong thread” warnings mean?
      If you are seeing this warning, some code in your app is accessing SimpleExoPlayer on the wrong thread
      (check the reported stack trace!). ExoPlayer instances need to be accessed from a single thread only.
      In most cases, this should be the application’s main thread.
      For details, please read through the “Threading model” section of the ExoPlayer Javadoc.
      https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/ExoPlayer.html
    * */

  }

  private void fade() {
    isFaded = !isFaded;
    mCardView.forcedFade(isFaded);
  }

  private void instant() {
    mCardView.instantAlpha1f();
  }

  private void check() {
    mCardView.setChecked(!mCardView.isChecked());
  }

  private void bombAccept() {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(
      () -> mCardView.accept(DataService.getNextItem(Constants.urls)),
      0, 5000, TimeUnit.MILLISECONDS);
  }

  private void bombCheck() {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(this::check, 0, 2000, TimeUnit.MILLISECONDS);
  }

  @Override
  public void onDestroy() {
    mDisposable.dispose();
    super.onDestroy();
  }
}
