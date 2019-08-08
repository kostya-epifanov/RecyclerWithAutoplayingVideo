package com.example.clearrecyclerwithvideo.player_controller_playground;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.clearrecyclerwithvideo.Constants;
import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.utils.reactor.Schedule;
import com.example.clearrecyclerwithvideo.view.PlayerTextureView;

import reactor.core.Disposable;


/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class MediaFragment extends Fragment {

  private PlayerTextureView mTexture1;
  private PlayerTextureView mTexture2;

  private View mButtonTest;

  private Disposable mDisposable;

  public static MediaFragment newInstance() {
    return new MediaFragment();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_media, container, false);

    mTexture1 = root.findViewById(R.id.player_1);
    mTexture2 = root.findViewById(R.id.player_2);

    mButtonTest = root.findViewById(R.id.button_test);
    mButtonTest.setOnClickListener(v -> test());

    mTexture1.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
      @Override
      public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        System.out.println("onSurfaceTextureAvailable surface = [" + surface + "], width = [" + width + "], height = [" + height + "]");

      }

      @Override
      public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        System.out.println(" ======================== ");
        System.out.println("onSurfaceTextureSizeChanged surface = [" + surface + "], width = [" + width + "], height = [" + height + "]");
        System.out.println(" ======================== ");
      }

      @Override
      public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        System.out.println("onSurfaceTextureDestroyed surface = [" + surface + "]");
        return false;
      }

      @Override
      public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        System.out.println("onSurfaceTextureUpdated surface = [" + surface + "]");

      }
    });

    return root;
  }

  private void test() {
    String videoUrl = Constants.getNextItem(Constants.urls).getVideoUrl();
    Schedule.WORK_EXECUTOR.execute(() ->
      MediaController
        .getFromCache(getContext(), mTexture1, videoUrl)
        .subscribe(p -> {
          ExtendedExoPlayer player = (ExtendedExoPlayer) p;
          System.out.println("MediaFragment.test 1 " + System.currentTimeMillis());

          player.accept(new Surface(mTexture1.getSurfaceTexture()),
            point -> mTexture1.initialize(point.x, point.y)); //нужен релиз

          System.out.println("MediaFragment.test 2 " + System.currentTimeMillis());

          System.out.println("MediaFragment.test player: ПОЛУЧЕН " + hashCode());
        })
    );
  }

}