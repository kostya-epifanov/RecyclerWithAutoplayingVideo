package com.example.clearrecyclerwithvideo.data;

import androidx.annotation.NonNull;

import com.example.clearrecyclerwithvideo.Constants;

import java.util.ArrayList;
import java.util.List;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author Konstantin Epifanov
 * @since 24.06.2019
 */
public class DataService {

  private final List<String> mPaginationList;


  public DataService() {
    mPaginationList = new ArrayList<>();
    for (int i = 0; i < Constants.ITEMS_TOTAL_SIZE; i++) {
      mPaginationList.add(String.valueOf(i));
    }
  }

  public Mono<Item[]> load(List<DataService.UrlHolder> urls, int offset, int size) {
    int limit = Math.min(mPaginationList.size(), offset + size);

    return Mono.fromCallable(() -> {
      Thread.sleep(Constants.API_DELAY);

      final List<String> list = mPaginationList.subList(offset, limit);

      Item[] result = new Item[list.size()];
      for (int i = 0; i < list.size(); i++) {
        int position = offset + i;
        String s = mPaginationList.get(position);
        result[i] = new Item(s, position, urls.get(i % urls.size()));
      }

      return result;
    })
      .subscribeOn(Schedulers.elastic())
      .publishOn(Schedulers.elastic());
  }

  public void addItem() {
    mPaginationList.add(0, String.valueOf(mPaginationList.size() + 1));
  }

  public void deleteItem() {
    mPaginationList.remove("2");
  }

  public void changeItem() {
    mPaginationList.set(2, "CHANGED");
  }

  public static List<Item> getItems(List<DataService.UrlHolder> urls) {
    List<Item> result = new ArrayList<>();
    for (int i = 0; i < urls.size(); i++) {
      result.add(new Item(String.valueOf(i), i, urls.get(i)));
    }
    return result;
  }

  private static int counter = 0;

  public static UrlHolder getNextItem(List<UrlHolder> urls) {
    if (counter == urls.size()) counter = 0;
    // if (++counter % 5 == 0) return null;
    return urls.get(counter++);
  }

  /**
   * @author Konstantin Epifanov
   * @since 10.07.2019
   */
  public static class UrlHolder {
    private String videoUrl;
    private String screenshotUrl;

    public UrlHolder(String videoUrl, String screenshotUrl) {
      this.videoUrl = videoUrl;
      this.screenshotUrl = screenshotUrl;
    }

    public String getVideoUrl() {
      return videoUrl;
    }

    public String getScreenshotUrl() {
      return screenshotUrl;
    }

    @NonNull
    @Override
    public String toString() {
      return "NotNull";
    }
  }
}
