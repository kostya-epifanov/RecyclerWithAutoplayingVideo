package com.example.clearrecyclerwithvideo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.example.clearrecyclerwithvideo.R;

import java.util.function.Consumer;


/**
 * @author Konstantin Epifanov
 * @since 19.07.2019
 */
public class CheckableView extends AppCompatTextView implements Consumer<Integer>, Checkable {

  private Integer pos;
  private boolean isChecked;

  public CheckableView(Context context) {
    this(context, null);
  }

  public CheckableView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CheckableView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
  }

  @Override
  public void accept(Integer integer) {
    pos = integer;
    setText(String.valueOf(integer));
  }

  @Override
  public void setChecked(boolean checked) {
    System.out.println("CheckableView.setChecked: " + pos + " " + checked);
    isChecked = checked;
    setBackgroundColor(ContextCompat.getColor(getContext(), checked? R.color.colorAccent : R.color.colorPrimaryDark));
  }

  @Override
  public boolean isChecked() {
    return isChecked;
  }

  @Override
  public void toggle() {
    isChecked = !isChecked;
  }
}
