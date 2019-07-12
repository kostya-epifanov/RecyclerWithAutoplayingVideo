package com.example.clearrecyclerwithvideo.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * @author Konstantin Epifanov
 * @since 24.06.2019
 */
public class Utils {

  /** Returns random dummy image URL */
  public static String getRandomImageUrl(int pagingOffset) {
    pagingOffset += 10;
    return "https://picsum.photos/id/" + pagingOffset + "/600/800";
  }

  public static Flux<View> toFlux(View view) {
    return Flux.create(sink -> {
      view.setOnClickListener(sink::next);

      sink.onDispose(new Disposable() {
        @Override
        public void dispose() {
          view.setOnClickListener(null);
        }

        @Override
        public boolean isDisposed() {
          return !view.hasOnClickListeners();
        }
      });
    });
  }

  public static Flux<Point> scrollEvents(RecyclerView recycler) {
    return Flux.create(sink ->
      sink.onDispose(scrollToDisposable(recycler,
        new RecyclerView.OnScrollListener() {
          {
            recycler.addOnScrollListener(this);
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            sink.next(new Point(dx, dy));
          }

        })), FluxSink.OverflowStrategy.IGNORE);
  }

  public static Disposable scrollToDisposable(RecyclerView recycler, RecyclerView.OnScrollListener listener) {
    return Disposables.composite(() -> recycler.removeOnScrollListener(listener));
  }

  public static int getRandomColor() {
    return Color.rgb(
      (int) (Math.random() * 255),
      (int) (Math.random() * 255),
      (int) (Math.random() * 255)
    );
  }

  @SuppressWarnings("SameParameterValue")
  public static <T> ListAdapter<T, RecyclerView.ViewHolder>
  getSimpleAdapter(@NonNull LayoutInflater inflater, @LayoutRes int layout) {
    return new ListAdapter<T, RecyclerView.ViewHolder>(
      new AsyncDifferConfig.Builder<>
        (new DiffUtil.ItemCallback<T>() {
          @SuppressWarnings("NullableProblems")
          @Override
          public final boolean
          areItemsTheSame(T oldItem, T newItem) {
            return oldItem.hashCode() == newItem.hashCode();
          }

          @SuppressWarnings("NullableProblems")
          @Override
          public final boolean
          areContentsTheSame(T oldItem, T newItem) {
            return Objects.equals(oldItem, newItem);
          }
        }).setBackgroundThreadExecutor(Runnable::run)
        .build()
    ) {
      {
        setHasStableIds(false);
      }

      @SuppressWarnings("NullableProblems")
      @Override
      public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        return new RecyclerView.ViewHolder(inflater.inflate(type, parent, false)) {
        };
      }

      @SuppressWarnings({"unchecked", "NullableProblems"})
      @Override
      public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Consumer<T>) holder.itemView).accept(getItem(position));
      }

      @Override
      public final int getItemViewType(int position) {
        return layout;
      }

      @SuppressWarnings({"unchecked"})
      @Override
      public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        ((Consumer<T>) holder.itemView).accept(null);
        System.out.println("TRANZ: " + holder.itemView.hasTransientState());
      }

    };
  }

  public static int getScreenWidthInDp(Context context) {
    Configuration config = context.getResources().getConfiguration();
    return config.screenWidthDp;
  }

  public static int getScreenHeightInDp(Context context) {
    Configuration config = context.getResources().getConfiguration();
    return config.screenHeightDp;
  }

  public static float getScreenWidthInPx(Context context) {
    Configuration config = context.getResources().getConfiguration();
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, config.screenWidthDp, context.getResources().getDisplayMetrics());
  }

  public static float getScreenHeightInPx(Context context) {
    Configuration config = context.getResources().getConfiguration();
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, config.screenHeightDp, context.getResources().getDisplayMetrics());
  }

}
