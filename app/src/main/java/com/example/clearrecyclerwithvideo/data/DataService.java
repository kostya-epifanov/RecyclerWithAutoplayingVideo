package com.example.clearrecyclerwithvideo.data;

import com.example.clearrecyclerwithvideo.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author Konstantin Epifanov
 * @since 24.06.2019
 */
public class DataService {

  private final List<String> mPaginationList;

  public static List<UrlHolder> urls = Arrays.asList(
    new UrlHolder("https://media.webka.com/hls/vod/39.mp4/index.m3u8", "https://storage.webka.com/1/39?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/38.mp4/index.m3u8", "https://storage.webka.com/1/38?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/37.mp4/index.m3u8", "https://storage.webka.com/1/37?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/36.mp4/index.m3u8", "https://storage.webka.com/1/36?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/35.mp4/index.m3u8", "https://storage.webka.com/1/35?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/34.mp4/index.m3u8", "https://storage.webka.com/1/34?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/33.mp4/index.m3u8", "https://storage.webka.com/1/33?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/32.mp4/index.m3u8", "https://storage.webka.com/1/32?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/31.mp4/index.m3u8", "https://storage.webka.com/1/31?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/30.mp4/index.m3u8", "https://storage.webka.com/1/30?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/29.mp4/index.m3u8", "https://storage.webka.com/1/29?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/28.mp4/index.m3u8", "https://storage.webka.com/1/28?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/27.mp4/index.m3u8", "https://storage.webka.com/1/27?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/26.mp4/index.m3u8", "https://storage.webka.com/1/26?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/25.mp4/index.m3u8", "https://storage.webka.com/1/25?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/24.mp4/index.m3u8", "https://storage.webka.com/1/24?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/23.mp4/index.m3u8", "https://storage.webka.com/1/23?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/22.mp4/index.m3u8", "https://storage.webka.com/1/22?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/21.mp4/index.m3u8", "https://storage.webka.com/1/21?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/20.mp4/index.m3u8", "https://storage.webka.com/1/20?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/19.mp4/index.m3u8", "https://storage.webka.com/1/19?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/18.mp4/index.m3u8", "https://storage.webka.com/1/18?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/17.mp4/index.m3u8", "https://storage.webka.com/1/17?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/16.mp4/index.m3u8", "https://storage.webka.com/1/16?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/15.mp4/index.m3u8", "https://storage.webka.com/1/15?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/14.mp4/index.m3u8", "https://storage.webka.com/1/14?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/13.mp4/index.m3u8", "https://storage.webka.com/1/13?1562773242.jpeg"),
    new UrlHolder("https://media.webka.com/hls/vod/12.mp4/index.m3u8", "https://storage.webka.com/1/12?1562773242.jpeg")
  );

  public DataService() {
    mPaginationList = new ArrayList<>();
    for (int i = 0; i < Constants.ITEMS_TOTAL_SIZE; i++) {
      mPaginationList.add(String.valueOf(i));
    }
  }

  public Mono<Item[]> load(int offset, int size) {
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
  }
}
