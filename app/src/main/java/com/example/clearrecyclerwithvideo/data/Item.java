package com.example.clearrecyclerwithvideo.data;

import android.graphics.Color;

import java.util.Objects;

/**
 * @author Konstantin Epifanov
 * @since 24.06.2019
 */
public class Item implements Cloneable {

  private String id;
  private int hash;

  private int position;
  private String text;
  private int color;

  private String url;
  private String backgroundUrl;

  public Item(String id, int position, DataService.UrlHolder url) {
    this.id = id;
    this.hash = id.hashCode();
    this.position = position;

    this.url = url.getVideoUrl();
    this.backgroundUrl = url.getScreenshotUrl();

    this.text = "id:" + id + " - pos:" + position;
    color = Color.GRAY;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Item item = (Item) o;
    return getPosition() == item.getPosition() &&
      getColor() == item.getColor() &&
      Objects.equals(getText(), item.getText());
  }

  @Override
  public int hashCode() {
    return hash;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getId() {
    return id;
  }

  public int getHash() {
    return hash;
  }

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }

  public int getPosition() {
    return position;
  }

  public String getUrl() {
    return url;
  }

  public String getBackgroundUrl() {
    return backgroundUrl;
  }

  @Override
  public String toString() {
    return "Item{" +
      "position=" + position +
      '}';
  }
}
