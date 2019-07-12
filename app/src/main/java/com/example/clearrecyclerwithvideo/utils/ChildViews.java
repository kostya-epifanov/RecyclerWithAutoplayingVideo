package com.example.clearrecyclerwithvideo.utils;

import android.view.View;
import android.view.ViewGroup;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A Child Views Stream designed for use by sources that traverse and split child
 * {@link View}'s maintained in an unmodifiable {@link ViewGroup}.
 */
public final class ChildViews implements Spliterator<View> {

  /** View Group. */
  private final ViewGroup mView;

  /** One past last index. */
  private final int mFence;

  /** Current index, modified on advance/split */
  private final AtomicInteger mIndex;

  /**
   * Constructs a new {@link ChildViews} spliterator
   *
   * @param view view
   * @param origin offset
   * @param fence limit
   */
  private ChildViews(ViewGroup view, int origin, int fence)
  {mView = view; mFence = fence; mIndex = new AtomicInteger(origin);}

  /** {@inheritDoc} */
  @Override
  public final boolean tryAdvance(Consumer<? super View> action) {
    final boolean result = estimateSize() > 0;
    if (result) action.accept(mView.getChildAt(mIndex.getAndIncrement()));
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public final ChildViews trySplit() {
    int was, now;
    do if (((was = mIndex.get()) >=
      (now = (was + mFence) >>> 1)))
      return null;
    while (!mIndex.compareAndSet(was, now));
    return new ChildViews(mView, was, now);
  }

  /** {@inheritDoc} */
  @Override
  public final long estimateSize()
  {return (long)(mFence - mIndex.get());}

  /** {@inheritDoc} */
  @Override
  public final int characteristics()
  {return ORDERED | IMMUTABLE | NONNULL | DISTINCT | SIZED;}

  /**
   * @param view parent view
   *
   * @return child views
   */
  public static Stream<View> sequential(View view) {return of(view, false);}

  /**
   * @param view parent view
   *
   * @return child views
   */
  public static Stream<View> parallel(View view) {return of(view, true);}

  /**
     * @param view parent view
     * @param parallel parallel
     *
     * @return child views
     */
  private static Stream<View> of(View view, boolean parallel) {
    if (view instanceof ViewGroup) {
      final ViewGroup group = (ViewGroup) view;
      final Spliterator<View> spliterator =
        new ChildViews(group, 0, group.getChildCount());
      return StreamSupport.stream(spliterator, parallel);
    } else return Stream.empty();
  }
}