/*
 * Schedule.java
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

import org.reactivestreams.Publisher;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Scheduling tools.
 *
 * @author Gleb Nikitenko
 * @since 19.06.19
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class Schedule {

  /** Executors. */
  public static final Executor
    MAIN_EXECUTOR = ExecutorServices.MAIN_EXECUTOR,
    WORK_EXECUTOR = ExecutorServices.WORK_EXECUTOR,
    IO_EXECUTOR = ExecutorServices.IO_EXECUTOR,
    IMMEDIATE_EXECUTOR = ExecutorServices.IMMEDIATE_EXECUTOR;

  /** Looper's. */
  public static final Looper
    MAIN_LOOPER = ExecutorServices.MAIN_LOOPER,
    WORK_LOOPER = ExecutorServices.WORK_LOOPER;

  /** Schedulers. */
  static final Scheduler
    MAIN_SCHEDULER = Schedulers.fromExecutorService(ExecutorServices.MAIN_EXECUTOR),
    WORK_SCHEDULER = Schedulers.fromExecutorService(ExecutorServices.WORK_EXECUTOR),
    IO_SCHEDULER = Schedulers.fromExecutorService(ExecutorServices.IO_EXECUTOR),
    IMMEDIATE_SCHEDULER = Schedulers.immediate();

  /**
   * The caller should be prevented from constructing objects of this class.
   * Also, this prevents even the native class from calling this constructor.
   **/
  private Schedule() {
    throw new AssertionError();
  }

  /**
   * @param publisher source publisher
   * @param <T>       data type
   *
   * @return transformed publisher
   */
  public static <T> Publisher<T> io_work(Publisher<T> publisher)
  {return transform(publisher, IO_SCHEDULER, WORK_SCHEDULER);}

  /**
   * @param publisher source publisher
   * @param <T>       data type
   *
   * @return transformed publisher
   */
  public static <T> Publisher<T> work_io(Publisher<T> publisher)
  {return transform(publisher, WORK_SCHEDULER, IO_SCHEDULER);}

  /**
   * @param publisher source publisher
   * @param <T>       data type
   *
   * @return transformed publisher
   */
  public static <T> Publisher<T> work_main(Publisher<T> publisher)
  {return transform(publisher, WORK_SCHEDULER, MAIN_SCHEDULER);}

  /**
   * @param publisher source publisher
   * @param <T>       data type
   *
   * @return transformed publisher
   */
  public static <T> Publisher<T> main_work(Publisher<T> publisher)
  {return transform(publisher, MAIN_SCHEDULER, WORK_SCHEDULER);}

  /**
   * @param publisher source publisher
   * @param <T>       data type
   *
   * @return transformed publisher
   */
  static <T> Publisher<T> io_main(Publisher<T> publisher)
  {return transform(publisher, IO_SCHEDULER, MAIN_SCHEDULER);}

  /**
   * @param publisher source publisher
   * @param <T>       data type
   *
   * @return transformed publisher
   */
  static <T> Publisher<T> main_io(Publisher<T> publisher)
  {return transform(publisher, MAIN_SCHEDULER, IO_SCHEDULER);}

  /**
   * @param publisher source publisher
   * @param <T>       data type
   *
   * @return transformed publisher
   */
  static <T> Publisher<T> io_io(Publisher<T> publisher) {
    return transform(publisher, IO_SCHEDULER, IO_SCHEDULER);
  }

  /**
   * @param publisher source publisher
   * @param scheduler subscribe scheduler
   * @param <T>       publisher type
   *
   * @return transformed publisher
   */
  @SuppressWarnings("unused")
  private static <T> Publisher<T> transform(Publisher<T> publisher,
                                            Scheduler scheduler) {
    return
      publisher instanceof Mono ?
        ((Mono<T>) publisher)
          .publishOn(scheduler) :
        publisher instanceof Flux ?
          ((Flux<T>) publisher)
            .publishOn(scheduler, true, 1) :
          publisher;
  }

  /**
   * @param publisher source publisher
   * @param subscribe subscribe scheduler
   * @param publish   publish scheduler
   * @param <T>       publisher type
   *
   * @return transformed publisher
   */
  private static <T> Publisher<T> transform(Publisher<T> publisher,
                                            Scheduler subscribe,
                                            Scheduler publish) {
    return
      publisher instanceof Mono ?
        ((Mono<T>) publisher)
          .subscribeOn(subscribe)
          .cancelOn(subscribe)
          .publishOn(publish) :
        publisher instanceof Flux ?
          ((Flux<T>) publisher)
            .subscribeOn(subscribe, false)
            .cancelOn(subscribe)
            .publishOn(publish, true, 1) :
          publisher;
  }

  /**
   * @param task   task runnable
   * @param period pings period (mills)
   *
   * @return disposable signal
   */
  public static Disposable io_ping(Runnable task, long period) {
    return IO_SCHEDULER.schedulePeriodically(task, period, period, TimeUnit.MILLISECONDS);
  }

  /** Check if current operation calls on IO-Thread */
  public static void trowIfNotIOThread() {
    final Thread thread = Thread.currentThread();
    if (MAIN_LOOPER.isCurrentThread() || WORK_LOOPER.isCurrentThread())
      throw new Error("Operation must be call on io only");
  }

  /** Check if current operation calls on Worker-Thread */
  public static void trowIfNotWorkerThread() {
    if (!WORK_LOOPER.isCurrentThread())
      throw new Error("Operation must be call on worker only");
  }

  /** Check if current operation calls on Main-Thread */
  public static void trowIfNotMainThread() {
    if (!MAIN_LOOPER.isCurrentThread())
      throw new Error("Operation must be call on main only");
  }

  /**
   * @param hook interrupt threadHook
   *
   * @return true if hook was attached, otherwise - false
   */
  private static boolean threadHook(Runnable hook)
  {return AndroidThread.hook(hook);}

}
