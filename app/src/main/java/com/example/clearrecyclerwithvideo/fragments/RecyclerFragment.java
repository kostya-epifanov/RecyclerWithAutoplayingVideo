package com.example.clearrecyclerwithvideo.fragments;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clearrecyclerwithvideo.R;
import com.example.clearrecyclerwithvideo.data.DataService;
import com.example.clearrecyclerwithvideo.data.Item;
import com.example.clearrecyclerwithvideo.utils.ChildViews;
import com.example.clearrecyclerwithvideo.utils.Constants;
import com.example.clearrecyclerwithvideo.utils.Utils;

import java.util.Arrays;
import java.util.Comparator;

import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;

import static java.util.Objects.requireNonNull;


/**
 * @author Konstantin Epifanov
 * @since 09.07.2019
 */
public class RecyclerFragment extends Fragment {

  private static Checkable DUMMY_CHECKABLE = new Checkable() {
    @Override public void setChecked(boolean checked) { }
    @Override public boolean isChecked() {
      return false;
    }
    @Override public void toggle() { /*ignore*/ }
  };

  private RecyclerView mRecycler;
  private Checkable mActiveCard = DUMMY_CHECKABLE;

  private Disposable mScrollDisposable;



  public static RecyclerFragment newInstance() {
    return new RecyclerFragment();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_recycler, container, false);
    mRecycler = root.findViewById(R.id.recycler);

    mRecycler.setAdapter(Utils.getSimpleAdapter(LayoutInflater.from(getContext()), R.layout.item_player_card));
    mRecycler.setItemViewCacheSize(2);

    mRecycler.setLayoutManager(new LinearLayoutManager(getContext()) {
      @Override
      public boolean supportsPredictiveItemAnimations() {
        return false;
      }
    });

    mScrollDisposable =
      Utils.scrollEvents(mRecycler)
        //.sample(Duration.ofMillis(600))
        //.sampleTimeout(point -> PublisherUtils.delay(300, TimeUnit.MILLISECONDS, Schedulers.elastic()))
        //.publishOn(Schedulers.fromExecutor(this::runOnUiThread))\
        .subscribe(this::onScrolled);

    new DataService().load(Constants.urls2, 0, Constants.urls2.size())
      .publishOn(Schedulers.fromExecutor(r -> getActivity().runOnUiThread(r)))
      .subscribe(this::submitList);

    return root;
  }


  @SuppressWarnings("unchecked")
  public void submitList(Item[] items) {
    ((ListAdapter<Item, RecyclerView.ViewHolder>)
      requireNonNull(mRecycler.getAdapter())).submitList(Arrays.asList(items));
  }

  private void onScrolled(Point point) {
    //todo find vertical center (maybe onSizeChanged
    final int recyclerCY = mRecycler.getHeight() >> 1;

    ChildViews.parallel(mRecycler)
      .min(minDistComparator(recyclerCY))
      .ifPresent(closest -> activate((Checkable) closest));
  }

  private void activate(Checkable v) {
    if (mActiveCard == v) return;
    mActiveCard.setChecked(false);
    mActiveCard = v;
    mActiveCard.setChecked(true);
  }

  private Comparator<View> minDistComparator(int target) {
    return (o1, o2) -> Integer.compare(getDist(o1, target), getDist(o2, target));
  }

  private int getDist(View v, int target) {
    return Math.abs(((int) v.getY()) + v.getHeight() / 2 - target);
  }

  @Override
  public void onDestroy() {
    mScrollDisposable.dispose();
    super.onDestroy();
  }
}


/*
 * return Math.abs(((PlayerCardView) v).verticalCenter - target);
 * */