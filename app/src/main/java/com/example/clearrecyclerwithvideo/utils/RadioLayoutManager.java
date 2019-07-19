package com.example.clearrecyclerwithvideo.utils;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Konstantin Epifanov
 * @since 19.07.2019
 */
public class RadioLayoutManager extends LinearLayoutManager {

  private Checkable mActiveCard = DUMMY_CHECKABLE;
  private static Checkable DUMMY_CHECKABLE = new Checkable() {
    @Override
    public void setChecked(boolean checked) {
    }

    @Override
    public boolean isChecked() {
      return false;
    }

    @Override
    public void toggle() { /*ignore*/ }
  };

  private View mIndicator;

  private Supplier<Integer> getRecyclerHeight;
  private int mSmartCenterY = 0;

  public RadioLayoutManager(Context context) {
    super(context);
  }

  public RadioLayoutManager(Context context, View indicator) {
    super(context);
    this.mIndicator = indicator;
  }

  @Override
  public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {

    int i = super.scrollVerticallyBy(dy, recycler, state);

    if (getRecyclerHeight != null) {
      mSmartCenterY = getSmartCenterY(dy, getRecyclerHeight.get());

      if (mSmartCenterY == -1) deactivate(mActiveCard.isChecked());
      else onScrolled(mSmartCenterY);

      if (mIndicator != null) {
        mIndicator.setTranslationY(mSmartCenterY);
      }
    }

    return i;

  }

  @Override
  public void onScrollStateChanged(int state) {
    super.onScrollStateChanged(state);
    if (state == 0 && mSmartCenterY == -1 && getRecyclerHeight != null) {
      onScrolled(getRecyclerHeight.get() / 2);
    }
  }

  private void onScrolled(int smartCenterY) {
    childs()
      .filter(view -> view instanceof Checkable)
      .min(minDistComparator(smartCenterY))
      .ifPresent(closest -> activate((Checkable) closest));
  }

  private void activate(Checkable v) {
    if (mActiveCard == v) return;
    mActiveCard.setChecked(false);
    mActiveCard = v;
    mActiveCard.setChecked(true);
  }

  private void deactivate(boolean isChecked) {
    if (!isChecked) return;
    mActiveCard.setChecked(false);
  }

  private Comparator<View> minDistComparator(int target) {
    return (o1, o2) -> Integer.compare(getDist(o1, target), getDist(o2, target));
  }

  private int getDist(View v, int target) {
    return Math.abs(((int) v.getY()) + v.getHeight() / 2 - target);
  }

  private Stream<View> childs() {
    List<View> list = new ArrayList<>();
    for (int i = 0; i < getChildCount(); i++) {
      list.add(getChildAt(i));
    }
    return list.stream();
  }

  private int getSmartCenterY(int dy, int height) {
    int centerY = height / 2;
    int smartCenter = centerY + dy * 10;
    if (smartCenter < 0 || smartCenter > height) return -1;
    return smartCenter;
  }

  @Override
  public void onAttachedToWindow(RecyclerView view) {
    super.onAttachedToWindow(view);
    getRecyclerHeight = view::getHeight;
  }

  @Override
  public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
    getRecyclerHeight = null;
    super.onDetachedFromWindow(view, recycler);
  }
}
