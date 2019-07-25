/*
 * AndroidThread.java
 * webka
 *
 * Copyright (C) 2019, Realtime Technologies Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the
 * property of Realtime Technologies Limited and its SUPPLIERS, if any.
 *
 * The intellectual and technical concepts contained herein are
 * proprietary to Realtime Technologies Limited and its suppliers and
 * may be covered by Russian Federation and Foreign Patents, patents
 * in process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Realtime Technologies Limited.
 */

package com.example.clearrecyclerwithvideo.utils.reactor;

import android.os.Process;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Extended Android-Thread.
 *
 * @author Gleb Nikitenko
 * @since 20.03.19
 */
@SuppressWarnings({"WeakerAccess", "unused"})
final class AndroidThread extends Thread {

  /** Process priority */
  private final int mPriority;

  /** Interruption listener. */
  private volatile Runnable mHook = null;

  /** {@inheritDoc} */
  public AndroidThread(int priority) {
    mPriority = priority;
  }

  /** {@inheritDoc} */
  public AndroidThread(Runnable runnable, int priority) {
    super(runnable);
    mPriority = priority;
  }

  /** {@inheritDoc} */
  public AndroidThread(ThreadGroup group, Runnable runnable, int priority) {
    super(group, runnable);
    mPriority = priority;
  }

  /** {@inheritDoc} */
  public AndroidThread(String name, int priority) {
    super(name);
    mPriority = priority;
  }

  /** {@inheritDoc} */
  public AndroidThread(ThreadGroup group, String name, int priority) {
    super(group, name);
    mPriority = priority;
  }

  /** {@inheritDoc} */
  public AndroidThread(Runnable runnable, String name, int priority) {
    super(runnable, name);
    mPriority = priority;
  }

  /** {@inheritDoc} */
  public AndroidThread
  (ThreadGroup group, Runnable runnable, String name, int priority) {
    super(group, runnable, name);
    mPriority = priority;
  }

  /** {@inheritDoc} */
  public AndroidThread
  (ThreadGroup group, Runnable runnable, String name, long stack, int priority) {
    super(group, runnable, name, stack);
    mPriority = priority;
  }

  /**
   * @param hook interrupt threadHook
   *
   * @return true if threadHook was attached, otherwise - false
   */
  static boolean hook(Runnable hook) {
    final Thread thread = Thread.currentThread();
    final boolean result = thread instanceof AndroidThread;
    if (result) ((AndroidThread) thread).mHook = hook;
    return result;
  }

  /**
   * @param name    thread-name prefix
   * @param thread  java-thread priority
   * @param process android-process priority
   *
   * @return thread factory
   */
  static ThreadFactory factory
  (String name, int thread, int process, boolean multi) {
    final SecurityManager security = System.getSecurityManager();
    final ThreadGroup group = security != null ?
      security.getThreadGroup() : Thread.currentThread().getThreadGroup();
    final AtomicInteger number = new AtomicInteger(0);
    return runnable -> {
      final String tName = multi ? name + "-" + number.getAndIncrement() : name;
      final Thread result = new AndroidThread(group, runnable, tName, 0, process);
      result.setDaemon(false);
      result.setPriority(thread);
      return result;
    };
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    Process.setThreadPriority(mPriority);
    super.run();
  }

  /** {@inheritDoc} */
  @Override
  public final void interrupt() {
    final Runnable hook = mHook;
    if (hook != null) hook.run();
    super.interrupt();
  }
}
