package com.example.clearrecyclerwithvideo;

import com.example.clearrecyclerwithvideo.data.DataService;

import java.util.Arrays;
import java.util.List;

/**
 * @author Konstantin Epifanov
 * @since 26.06.2019
 */
public class Constants {

  public static final int API_DELAY = 500;
  public static final int ITEMS_TOTAL_SIZE = 50; // 100
  public static final int PAGE_SIZE = 50; // 10

  public static List<DataService.UrlHolder> urls = Arrays.asList(
      new DataService.UrlHolder("https://media.webka.com/hls/vod/39.mp4/index.m3u8", "https://storage.webka.com/1/39?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/38.mp4/index.m3u8", "https://storage.webka.com/1/38?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/37.mp4/index.m3u8", "https://storage.webka.com/1/37?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/36.mp4/index.m3u8", "https://storage.webka.com/1/36?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/35.mp4/index.m3u8", "https://storage.webka.com/1/35?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/34.mp4/index.m3u8", "https://storage.webka.com/1/34?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/33.mp4/index.m3u8", "https://storage.webka.com/1/33?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/32.mp4/index.m3u8", "https://storage.webka.com/1/32?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/31.mp4/index.m3u8", "https://storage.webka.com/1/31?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/30.mp4/index.m3u8", "https://storage.webka.com/1/30?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/29.mp4/index.m3u8", "https://storage.webka.com/1/29?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/28.mp4/index.m3u8", "https://storage.webka.com/1/28?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/27.mp4/index.m3u8", "https://storage.webka.com/1/27?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/26.mp4/index.m3u8", "https://storage.webka.com/1/26?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/25.mp4/index.m3u8", "https://storage.webka.com/1/25?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/24.mp4/index.m3u8", "https://storage.webka.com/1/24?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/23.mp4/index.m3u8", "https://storage.webka.com/1/23?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/22.mp4/index.m3u8", "https://storage.webka.com/1/22?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/21.mp4/index.m3u8", "https://storage.webka.com/1/21?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/20.mp4/index.m3u8", "https://storage.webka.com/1/20?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/19.mp4/index.m3u8", "https://storage.webka.com/1/19?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/18.mp4/index.m3u8", "https://storage.webka.com/1/18?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/17.mp4/index.m3u8", "https://storage.webka.com/1/17?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/16.mp4/index.m3u8", "https://storage.webka.com/1/16?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/15.mp4/index.m3u8", "https://storage.webka.com/1/15?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/14.mp4/index.m3u8", "https://storage.webka.com/1/14?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/13.mp4/index.m3u8", "https://storage.webka.com/1/13?1562773242.jpeg"),
    new DataService.UrlHolder("https://media.webka.com/hls/vod/12.mp4/index.m3u8", "https://storage.webka.com/1/12?1562773242.jpeg")
  );
  public static List<DataService.UrlHolder> urls2 = Arrays.asList(
    new DataService.UrlHolder("https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8", "https://images.unsplash.com/photo-1446292267125-fecb4ecbf1a5?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1050&q=80"),
    new DataService.UrlHolder("https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8", "https://images.unsplash.com/photo-1535127022272-dbe7ee35cf33?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1050&q=80"),
    new DataService.UrlHolder("https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8", "https://images.unsplash.com/photo-1507919981044-3b672b208db9?ixlib=rb-1.2.1&auto=format&fit=crop&w=1050&q=80"),
    new DataService.UrlHolder("https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8", "https://images.unsplash.com/photo-1507415329510-9e85a4183e73?ixlib=rb-1.2.1&auto=format&fit=crop&w=1050&q=80"),
    new DataService.UrlHolder("https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8", "https://images.unsplash.com/photo-1513624954087-ca7109c0f710?ixlib=rb-1.2.1&auto=format&fit=crop&w=1050&q=80")
  );
}
