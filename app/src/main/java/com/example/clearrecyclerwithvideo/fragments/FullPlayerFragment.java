package com.example.clearrecyclerwithvideo.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.view.PlayerTextureView;


/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class FullPlayerFragment extends Fragment {

  private PlayerTextureView mPlayer;

  public static FullPlayerFragment newInstance() {
    return new FullPlayerFragment();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_full_player, container, false);
    mPlayer = root.findViewById(R.id.player_full);
    return root;
  }

}
