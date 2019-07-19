package com.example.clearrecyclerwithvideo.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.data.DataService;
import com.example.clearrecyclerwithvideo.Constants;
import com.example.clearrecyclerwithvideo.view.PlayerCardView;


/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class SingleCardFragment extends Fragment {

  private PlayerCardView mCardView;
  private View mFade, mCheck;

  public static SingleCardFragment newInstance() {
    return new SingleCardFragment();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_single, container, false);

    mCardView = root.findViewById(R.id.container_item);
    mFade = root.findViewById(R.id.button_fade);
    mCheck = root.findViewById(R.id.button_check);

    mFade.setOnClickListener(v -> mCardView.forcedFade());
    mCheck.setOnClickListener(v -> mCardView.setChecked(!mCardView.isChecked()));

    mCardView.accept(DataService.getItems(Constants.urls2).get(0));

    return root;
  }

}
