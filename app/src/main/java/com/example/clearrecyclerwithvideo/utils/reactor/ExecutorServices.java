/*
 * ExecutorServices.java
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

import android.os.Looper;
import android.os.Process;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Gleb Nikitenko
 * @since 20.03.19
 */
final class ExecutorServices {

  /** Thread Names. */
  private static final String
    IO_NAME = prefix("io"),
    WORK_NAME = prefix("work"),
    MAIN_NAME = prefix("main");

  /** Thread Priorities. */
  private static final int
    IO_THREAD = Thread.NORM_PRIORITY, WORK_THREAD = Thread.NORM_PRIORITY,
    IO_PROCESS = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE,
    WORK_PROCESS = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE;

  /** Thread Factories. */
  private static final ThreadFactory
    IO_FACTORY = AndroidThread.factory(IO_NAME, IO_THREAD, IO_PROCESS, true),
    WORK_FACTORY = AndroidThread.factory(WORK_NAME, WORK_THREAD, WORK_PROCESS, false);

  /** Looper's. */
  static final Looper
    MAIN_LOOPER = setLooperName(Looper.getMainLooper(), MAIN_NAME),
    WORK_LOOPER = setLooperName(newLooper(WORK_FACTORY), WORK_NAME);

  /** Executor services. */
  static final ExecutorService
    MAIN_EXECUTOR = new LooperExecutor(MAIN_LOOPER),
    WORK_EXECUTOR = new LooperExecutor(WORK_LOOPER),
    IO_EXECUTOR = createIO(IO_FACTORY, true),
    IMMEDIATE_EXECUTOR = new ImmediateExecutor();

  /**
   * @param name the name of thread
   *
   * @return full prefixed thread name
   */
  private static String prefix(String name) {
    return /*"thread-" + */name;
  }

  /**
   * @param factory thread factory
   *
   * @return thread looper
   */
  @SuppressWarnings("SameParameterValue")
  private static Looper newLooper(ThreadFactory factory) {
    return new CompletableFuture<Looper>() {{
      factory.newThread(() -> {
          Looper.prepare();
          complete(Looper.myLooper());
          Looper.loop();
        }
      ).start();
    }}.join();
  }

  /**
   * @param looper source looper
   * @param name   the name of thread
   *
   * @return named looper
   */
  private static Looper setLooperName(Looper looper, String name)
  {try {return looper;} finally {looper.getThread().setName(name);}}

  /**
   * @param factory   thread factory
   * @param scheduled scheduled mode
   *
   * @return executor service
   */
  @SuppressWarnings("SameParameterValue")
  private static ExecutorService createIO(ThreadFactory factory, boolean scheduled) {
    final int core = 0; final long time = 30L; final TimeUnit unit = SECONDS;
    final ThreadPoolExecutor result = !scheduled ? new ThreadPoolExecutor(core,
      Integer.MAX_VALUE, time, unit, new LinkedBlockingQueue<>(1/*28*/), factory) :
      new ScheduledThreadPoolExecutor(core + 5, factory);
    result.allowCoreThreadTimeOut(false);
    result.setKeepAliveTime(time, unit);
    return result;
  }
}
