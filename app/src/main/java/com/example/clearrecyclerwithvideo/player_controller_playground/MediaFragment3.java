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
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clearrecyclerwithvideo.Constants;
import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.data.DataService;
import com.example.clearrecyclerwithvideo.utils.Utils;
import com.example.clearrecyclerwithvideo.utils.android.RadioLayoutManager;
import com.example.clearrecyclerwithvideo.utils.reactor.Schedule;
import com.example.clearrecyclerwithvideo.view.PlayerTextureView;

import java.util.ArrayList;
import java.util.List;

import reactor.core.Disposable;

import static java.util.Objects.requireNonNull;


/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class MediaFragment3 extends Fragment {

  private RecyclerView mRecyclerView;

  public static MediaFragment3 newInstance() {
    return new MediaFragment3();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_media_3, container, false);

    (root.findViewById(R.id.button_test)).setOnClickListener(v -> test());

    mRecyclerView = root.findViewById(R.id.recycler);
    mRecyclerView.setAdapter(Utils.getSimpleAdapter(LayoutInflater.from(getContext()), R.layout.item_texture));
    mRecyclerView.setLayoutManager(new RadioLayoutManager(getContext()));
    mRecyclerView.setItemViewCacheSize(0);

    submitList(getList());

    return root;
  }

  @SuppressWarnings("unchecked")
  public void submitList(List<Integer> items) {
    ((ListAdapter<Integer, RecyclerView.ViewHolder>)
      requireNonNull(mRecyclerView.getAdapter())).submitList(items);
  }

  private List<Integer> getList() {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      list.add(i);
    }
    return list;
  }

  private void test() {

  }


}