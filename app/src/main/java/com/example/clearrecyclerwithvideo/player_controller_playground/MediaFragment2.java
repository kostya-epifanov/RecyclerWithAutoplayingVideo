package com.example.clearrecyclerwithvideo.player_controller_playground;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.clearrecyclerwithvideo.Constants;
import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.view.PlayerTextureView;

import reactor.core.Disposable;
import reactor.core.Disposables;


/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class MediaFragment2 extends Fragment {

  public static final int SLEEP = 3000;

  private PlayerTextureView mTexture1, mTexture2, mTexture3, mTexture4;

  private Disposable.Swap mSwapG = Disposables.swap();
  private Disposable.Swap mSwap = Disposables.swap();

  public static MediaFragment2 newInstance() {
    return new MediaFragment2();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_media_2, container, false);

    mTexture1 = root.findViewById(R.id.player_1);
    mTexture2 = root.findViewById(R.id.player_2);
    mTexture3 = root.findViewById(R.id.player_3);
    mTexture4 = root.findViewById(R.id.player_4);

    (root.findViewById(R.id.button_test_1)).setOnClickListener(v -> test_1());
    (root.findViewById(R.id.button_test_2)).setOnClickListener(v -> test_2());
    (root.findViewById(R.id.button_test_3)).setOnClickListener(v -> test_3());
    (root.findViewById(R.id.button_test_4)).setOnClickListener(v -> test_4());

    return root;
  }

  private void test_1() {
    mSwapG.update(MediaController
      .getFromCache(getContext(), mTexture1, Constants.urls2.get(0).getVideoUrl())
      .subscribe(p -> {
        ExtendedExoPlayer player = (ExtendedExoPlayer) p;

        mSwap.replace(player.accept(new Surface(mTexture1.getSurfaceTexture()),
          point -> mTexture1.initialize(point.x, point.y)));
      })
    );
  }

  private void test_2() {
    mSwapG.update(MediaController
      .getFromCache(getContext(), mTexture2, Constants.urls2.get(1).getVideoUrl())
      .subscribe(p -> {
        ExtendedExoPlayer player = (ExtendedExoPlayer) p;

        mSwap.replace(player.accept(new Surface(mTexture2.getSurfaceTexture()),
          point -> mTexture2.initialize(point.x, point.y)));
      })
    );
  }

  private void test_3() {
    mSwapG.update(MediaController
      .getFromCache(getContext(), mTexture3, Constants.urls2.get(2).getVideoUrl())
      .subscribe(p -> {
        ExtendedExoPlayer player = (ExtendedExoPlayer) p;

        mSwap.replace(player.accept(new Surface(mTexture3.getSurfaceTexture()),
          point -> mTexture3.initialize(point.x, point.y)));
      })
    );
  }

  private void test_4() {
    mSwapG.update(MediaController
      .getFromCache(getContext(), mTexture4, Constants.urls2.get(3).getVideoUrl())
      .subscribe(p -> {
        ExtendedExoPlayer player = (ExtendedExoPlayer) p;

        mSwap.replace(player.accept(new Surface(mTexture4.getSurfaceTexture()),
          point -> mTexture4.initialize(point.x, point.y)));
      })
    );
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}