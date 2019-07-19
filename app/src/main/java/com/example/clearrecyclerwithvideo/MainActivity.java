package com.example.clearrecyclerwithvideo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clearrecyclerwithvideo.fragments.RadioRecyclerFragment;
import com.example.clearrecyclerwithvideo.fragments.RecyclerFragment;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, RadioRecyclerFragment.newInstance())
      .addToBackStack(null)
      .commit();
  }

}
