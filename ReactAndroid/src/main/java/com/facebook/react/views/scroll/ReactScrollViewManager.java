/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.views.scroll;

import android.graphics.Color;
import androidx.core.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.uimanager.DisplayMetricsHolder;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.ReactClippingViewGroupHelper;
import com.facebook.react.uimanager.Spacing;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.ViewProps;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.annotations.ReactPropGroup;
import com.facebook.react.views.view.ReactViewGroup;
import com.facebook.yoga.YogaConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * View manager for {@link ReactScrollView} components.
 *
 * <p>Note that {@link ReactScrollView} and {@link ReactScrollView} are exposed to JS
 * as a single ScrollView component, configured via the {@code horizontal} boolean property.
 */
@ReactModule(name = ReactScrollViewManager.REACT_CLASS)
public class ReactScrollViewManager
    extends ViewGroupManager<ReactScrollView>
    implements ReactScrollViewCommandHelper.ScrollCommandHandler<ReactScrollView> {

  public static final String REACT_CLASS = "RCTScrollView";

  private static final int[] SPACING_TYPES = {
      Spacing.ALL, Spacing.LEFT, Spacing.RIGHT, Spacing.TOP, Spacing.BOTTOM,
  };

  private @Nullable FpsListener mFpsListener = null;

  public ReactScrollViewManager() {
    this(null);
  }

  public ReactScrollViewManager(@Nullable FpsListener fpsListener) {
    mFpsListener = fpsListener;
  }

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public ReactScrollView createViewInstance(ThemedReactContext context) {
    return new ReactScrollView(context, mFpsListener);
  }

  @ReactProp(name = "scrollEnabled", defaultBoolean = true)
  public void setScrollEnabled(ReactScrollView view, boolean value) {
    view.setScrollEnabled(value);
  }

  @ReactProp(name = "showsVerticalScrollIndicator")
  public void setShowsVerticalScrollIndicator(ReactScrollView view, boolean value) {
    view.setVerticalScrollBarEnabled(value);
  }

  @ReactProp(name = "decelerationRate")
  public void setDecelerationRate(ReactScrollView view, float decelerationRate) {
    view.setDecelerationRate(decelerationRate);
  }

  @ReactProp(name = "snapToInterval")
  public void setSnapToInterval(ReactScrollView view, float snapToInterval) {
    // snapToInterval needs to be exposed as a float because of the Javascript interface.
    DisplayMetrics screenDisplayMetrics = DisplayMetricsHolder.getScreenDisplayMetrics();
    view.setSnapInterval((int) (snapToInterval * screenDisplayMetrics.density));
  }

  @ReactProp(name = "snapToOffsets")
  public void setSnapToOffsets(ReactScrollView view, @Nullable ReadableArray snapToOffsets) {
    DisplayMetrics screenDisplayMetrics = DisplayMetricsHolder.getScreenDisplayMetrics();
    List<Integer> offsets = new ArrayList<Integer>();
    for (int i = 0; i < snapToOffsets.size(); i++) {
      offsets.add((int) (snapToOffsets.getDouble(i) * screenDisplayMetrics.density));
    }
    view.setSnapOffsets(offsets);
  }

  @ReactProp(name = "snapToStart")
  public void setSnapToStart(ReactScrollView view, boolean snapToStart) {
    view.setSnapToStart(snapToStart);
  }

  @ReactProp(name = "snapToEnd")
  public void setSnapToEnd(ReactScrollView view, boolean snapToEnd) {
    view.setSnapToEnd(snapToEnd);
  }

  @ReactProp(name = ReactClippingViewGroupHelper.PROP_REMOVE_CLIPPED_SUBVIEWS)
  public void setRemoveClippedSubviews(ReactScrollView view, boolean removeClippedSubviews) {
    view.setRemoveClippedSubviews(removeClippedSubviews);
  }

  /**
   * Computing momentum events is potentially expensive since we post a runnable on the UI thread
   * to see when it is done.  We only do that if {@param sendMomentumEvents} is set to true.  This
   * is handled automatically in js by checking if there is a listener on the momentum events.
   *
   * @param view
   * @param sendMomentumEvents
   */
  @ReactProp(name = "sendMomentumEvents")
  public void setSendMomentumEvents(ReactScrollView view, boolean sendMomentumEvents) {
    view.setSendMomentumEvents(sendMomentumEvents);
  }

  /**
   * Tag used for logging scroll performance on this scroll view. Will force momentum events to be
   * turned on (see setSendMomentumEvents).
   *
   * @param view
   * @param scrollPerfTag
   */
  @ReactProp(name = "scrollPerfTag")
  public void setScrollPerfTag(ReactScrollView view, @Nullable String scrollPerfTag) {
    view.setScrollPerfTag(scrollPerfTag);
  }

  @ReactProp(name = "pagingEnabled")
  public void setPagingEnabled(ReactScrollView view, boolean pagingEnabled) {
    view.setPagingEnabled(pagingEnabled);
  }

  /**
   * When set, fills the rest of the scrollview with a color to avoid setting a background and
   * creating unnecessary overdraw.
   * @param view
   * @param color
   */
  @ReactProp(name = "endFillColor", defaultInt = Color.TRANSPARENT, customType = "Color")
  public void setBottomFillColor(ReactScrollView view, int color) {
    view.setEndFillColor(color);
  }

  /**
   * Controls overScroll behaviour
   */
  @ReactProp(name = "overScrollMode")
  public void setOverScrollMode(ReactScrollView view, String value) {
    view.setOverScrollMode(ReactScrollViewHelper.parseOverScrollMode(value));
  }

  @ReactProp(name = "nestedScrollEnabled")
  public void setNestedScrollEnabled(ReactScrollView view, boolean value) {
    ViewCompat.setNestedScrollingEnabled(view, value);
  }

  @ReactProp(name = "chatBehavior")
  public void setChatBehavior(ReactScrollView view, boolean value) {
    view.setChatBehavior(value);
  }

  @Override
  public @Nullable Map<String, Integer> getCommandsMap() {
    return ReactScrollViewCommandHelper.getCommandsMap();
  }

  @Override
  public void receiveCommand(
      ReactScrollView scrollView,
      int commandId,
      @Nullable ReadableArray args) {
    ReactScrollViewCommandHelper.receiveCommand(this, scrollView, commandId, args);
  }

  @Override
  public void flashScrollIndicators(ReactScrollView scrollView) {
    scrollView.flashScrollIndicators();
  }

  @Override
  public void scrollToIndex(ReactScrollView scrollView, int index, boolean animated) {
    Log.e(this.getName(), "scrollToIndex: index:" + index);
    View child = getSubChildAtTotalIndex(scrollView, index);
    if (child == null) {
      Log.e(this.getName(), "scrollToIndex: skipping because getSubChildAtTotalIndex: " + index + " returned null");
      return;
    }
    int scrollTo = child.getBottom();
    Log.e(this.getName(), "scrollToIndex: scrollTo: " + scrollTo + ", child: " + child.toString());
    Log.e(this.getName(), "scrollToIndex: content height: " + scrollView.getContentView().getHeight());
    if (animated) {
      scrollView.smoothScrollTo(0, scrollTo);
    } else {
      scrollView.scrollTo(0, scrollTo);
    }
  }

  private View getSubChildAtTotalIndex(ReactScrollView scrollView, int index) {
    int groupCount = scrollView.getChildCount();
    int count = 0;
    // Iterate the groups.
    for (int i = 0; i < groupCount; i++) {
      ReactViewGroup group = (ReactViewGroup) scrollView.getChildAt(i);
      int childCount = group.getChildCount();
      // Iterate the inner groups.
      for (int j = 0; j < childCount; j++){
        if (count == index) {
          return group.getChildAt(j);
        } else {
          count++;
        }
      }
    }
    return null;
  }

  @Override
  public void scrollTo(
      ReactScrollView scrollView, ReactScrollViewCommandHelper.ScrollToCommandData data) {
    if (data.mAnimated) {
      scrollView.smoothScrollTo(data.mDestX, data.mDestY);
    } else {
      scrollView.scrollTo(data.mDestX, data.mDestY);
    }
  }
  @ReactPropGroup(names = {
      ViewProps.BORDER_RADIUS,
      ViewProps.BORDER_TOP_LEFT_RADIUS,
      ViewProps.BORDER_TOP_RIGHT_RADIUS,
      ViewProps.BORDER_BOTTOM_RIGHT_RADIUS,
      ViewProps.BORDER_BOTTOM_LEFT_RADIUS
  }, defaultFloat = YogaConstants.UNDEFINED)
  public void setBorderRadius(ReactScrollView view, int index, float borderRadius) {
    if (!YogaConstants.isUndefined(borderRadius)) {
      borderRadius = PixelUtil.toPixelFromDIP(borderRadius);
    }

    if (index == 0) {
      view.setBorderRadius(borderRadius);
    } else {
      view.setBorderRadius(borderRadius, index - 1);
    }
  }

  @ReactProp(name = "borderStyle")
  public void setBorderStyle(ReactScrollView view, @Nullable String borderStyle) {
    view.setBorderStyle(borderStyle);
  }

  @ReactPropGroup(names = {
      ViewProps.BORDER_WIDTH,
      ViewProps.BORDER_LEFT_WIDTH,
      ViewProps.BORDER_RIGHT_WIDTH,
      ViewProps.BORDER_TOP_WIDTH,
      ViewProps.BORDER_BOTTOM_WIDTH,
  }, defaultFloat = YogaConstants.UNDEFINED)
  public void setBorderWidth(ReactScrollView view, int index, float width) {
    if (!YogaConstants.isUndefined(width)) {
      width = PixelUtil.toPixelFromDIP(width);
    }
    view.setBorderWidth(SPACING_TYPES[index], width);
  }

  @ReactPropGroup(names = {
      "borderColor", "borderLeftColor", "borderRightColor", "borderTopColor", "borderBottomColor"
  }, customType = "Color")
  public void setBorderColor(ReactScrollView view, int index, Integer color) {
    float rgbComponent =
        color == null ? YogaConstants.UNDEFINED : (float) (color & 0x00FFFFFF);
    float alphaComponent = color == null ? YogaConstants.UNDEFINED : (float) (color >>> 24);
    view.setBorderColor(SPACING_TYPES[index], rgbComponent, alphaComponent);
  }

  @ReactProp(name = "overflow")
  public void setOverflow(ReactScrollView view, @Nullable String overflow) {
    view.setOverflow(overflow);
  }

  @Override
  public void scrollToEnd(
      ReactScrollView scrollView,
      ReactScrollViewCommandHelper.ScrollToEndCommandData data) {
    // ScrollView always has one child - the scrollable area
    int bottom =
      scrollView.getChildAt(0).getHeight() + scrollView.getPaddingBottom();
    if (data.mAnimated) {
      scrollView.smoothScrollTo(scrollView.getScrollX(), bottom);
    } else {
      scrollView.scrollTo(scrollView.getScrollX(), bottom);
    }
  }

  @ReactProp(name = "persistentScrollbar")
  public void setPersistentScrollbar(ReactScrollView view, boolean value) {
    view.setScrollbarFadingEnabled(!value);
  }

  @Override
  public @Nullable Map<String, Object> getExportedCustomDirectEventTypeConstants() {
    return createExportedCustomDirectEventTypeConstants();
  }

  public static Map<String, Object> createExportedCustomDirectEventTypeConstants() {
    return MapBuilder.<String, Object>builder()
        .put(ScrollEventType.getJSEventName(ScrollEventType.SCROLL), MapBuilder.of("registrationName", "onScroll"))
        .put(ScrollEventType.getJSEventName(ScrollEventType.BEGIN_DRAG), MapBuilder.of("registrationName", "onScrollBeginDrag"))
        .put(ScrollEventType.getJSEventName(ScrollEventType.END_DRAG), MapBuilder.of("registrationName", "onScrollEndDrag"))
        .put(ScrollEventType.getJSEventName(ScrollEventType.MOMENTUM_BEGIN), MapBuilder.of("registrationName", "onMomentumScrollBegin"))
        .put(ScrollEventType.getJSEventName(ScrollEventType.MOMENTUM_END), MapBuilder.of("registrationName", "onMomentumScrollEnd"))
        .build();
  }
}
