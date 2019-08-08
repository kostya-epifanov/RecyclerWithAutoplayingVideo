package com.example.clearrecyclerwithvideo.player_controller_playground;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.clearrecyclerwithvideo.Constants;
import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.view.PlayerTextureView;

import java.util.function.Consumer;

import reactor.core.Disposable;
import reactor.core.Disposables;

/**
 * @author Konstantin Epifanov
 * @since 29.07.2019
 */
public class ItemView3 extends FrameLayout implements Consumer<Integer> {

  private PlayerTextureView texture;

  private Disposable.Swap mSwapG = Disposables.swap();
  private Disposable.Swap mSwap = Disposables.swap();

  public ItemView3(@NonNull Context context) {
    this(context, null);
  }

  public ItemView3(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ItemView3(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    System.out.println("ItemView3.ItemView3: CONSTRUCTOR");
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    texture = findViewById(R.id.texture);
  }

  @Override
  public void accept(Integer integer) {
    if (integer == null) {
      System.out.println("=== === === ItemView3.accept NULL === === ===");
      mSwapG.update(Disposables.single());
      mSwap.update(Disposables.single());

    } else {
      System.out.println("=== === === ItemView3.accept NOT NULL === === ===");
      String url = Constants.getNextItem(Constants.urls2).getVideoUrl();
      mSwapG.update(
        MediaController.getFromCache(getContext(), texture, url)
          .subscribe(p -> {
            ExtendedExoPlayer player = (ExtendedExoPlayer) p;

            Runnable runnable = () ->
              mSwap.update(player.accept(new Surface(texture.getSurfaceTexture()),
                point -> texture.initialize(point.x, point.y)));

            if (!texture.isAvailable()) {
              post(runnable);
            } else {
              runnable.run();
            }
          }));
    }
  }
}
