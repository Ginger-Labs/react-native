/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.uimanager;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import com.facebook.react.common.SingleThreadAsserter;

/**
 * Simple container class to keep track of {@link ReactShadowNode}s associated with a particular
 * UIManagerModule instance.
 */
public class ShadowNodeRegistry {

  private final SparseArray<ReactShadowNode> mTagsToCSSNodes;
  private final SparseBooleanArray mRootTags;
  private final SingleThreadAsserter mThreadAsserter;

  public ShadowNodeRegistry() {
    mTagsToCSSNodes = new SparseArray<>();
    mRootTags = new SparseBooleanArray();
    mThreadAsserter = new SingleThreadAsserter();
  }

  public synchronized void addRootNode(ReactShadowNode node) {
    mThreadAsserter.assertNow();
    int tag = node.getReactTag();
    mTagsToCSSNodes.put(tag, node);
    mRootTags.put(tag, true);
  }

  public synchronized void removeRootNode(int tag) {
    mThreadAsserter.assertNow();
    if (tag == View.NO_ID) {
      // This root node has already been removed (likely due to a threading issue caused by async js
      // execution). Ignore this root removal.
      return;
    }
    if (!mRootTags.get(tag)) {
      throw new IllegalViewOperationException(
          "View with tag " + tag + " is not registered as a root view");
    }

    mTagsToCSSNodes.remove(tag);
    mRootTags.delete(tag);
  }

  public synchronized void addNode(ReactShadowNode node) {
    mThreadAsserter.assertNow();
    mTagsToCSSNodes.put(node.getReactTag(), node);
  }

  public synchronized void removeNode(int tag) {
    mThreadAsserter.assertNow();
    if (mRootTags.get(tag)) {
      // Twobird: 0.63.2-gl006, fix synchronization view bug
      Log.e(this.getClass().getCanonicalName(), "Trying to remove root node " + tag + " without using removeRootNode!");
    }
    mTagsToCSSNodes.remove(tag);
  }

  public synchronized ReactShadowNode getNode(int tag) {
    mThreadAsserter.assertNow();
    return mTagsToCSSNodes.get(tag);
  }

  public synchronized boolean isRootNode(int tag) {
    mThreadAsserter.assertNow();
    return mRootTags.get(tag);
  }

  public synchronized int getRootNodeCount() {
    mThreadAsserter.assertNow();
    return mRootTags.size();
  }

  public synchronized int getRootTag(int index) {
    mThreadAsserter.assertNow();
    return mRootTags.keyAt(index);
  }
}
