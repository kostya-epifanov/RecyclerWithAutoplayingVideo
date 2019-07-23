package com.example.clearrecyclerwithvideo.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.utils.Utils;
import com.example.clearrecyclerwithvideo.utils.RadioLayoutManager;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;


/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class RadioRecyclerFragment extends Fragment {

  private RecyclerView mRecycler;

  public static RadioRecyclerFragment newInstance() {
    return new RadioRecyclerFragment();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_checkable_recycler, container, false);
    mRecycler = root.findViewById(R.id.recycler);

    mRecycler.setAdapter(Utils.getSimpleAdapter(LayoutInflater.from(getContext()), R.layout.item_checkable));

    mRecycler.setLayoutManager(new RadioLayoutManager(getContext(),
      root.findViewById(R.id.indicator), root.findViewById(R.id.checkbox_elastic)));

    submitList(getList());

    return root;
  }

  @SuppressWarnings("unchecked")
  public void submitList(List<Integer> items) {
    ((ListAdapter<Integer, RecyclerView.ViewHolder>)
      requireNonNull(mRecycler.getAdapter())).submitList(items);
  }

  private List<Integer> getList() {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      list.add(i);
    }
    return list;
  }

}
