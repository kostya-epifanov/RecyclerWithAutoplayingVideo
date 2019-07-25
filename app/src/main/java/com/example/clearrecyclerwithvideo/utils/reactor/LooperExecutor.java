/*
 * LooperExecutor.java
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

import android.os.Handler;
import android.os.Looper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Consumer;

/**
 * {@link ScheduledExecutorService} wrapper under android {@link Looper}.
 *
 * @author Gleb Nikitenko
 * @since 20.03.19
 */
final class LooperExecutor
  extends AbstractExecutorService
  implements ScheduledExecutorService {

  /** Default time unit. */
  private static final TimeUnit TIME_UNIT = TimeUnit.NANOSECONDS;

  /** Android Handler. */
  private final Handler mHandler;

  /** Looper. */
  private final Looper mLooper;

  /** Thread. */
  private final Thread mThread;

  /** Errors consumer. */
  private final Consumer<Throwable> mErrors;

  /** States. */
  private static final long
    NOT_TERMINATED = 0L,
    TERMINATED = 1L;

  /** State updater. */
  private static final
  AtomicLongFieldUpdater<LooperExecutor> TERMINATED_UPDATER =
    AtomicLongFieldUpdater.newUpdater(LooperExecutor.class, "mTerminated");

  /** Terminated state. */
  private volatile long mTerminated = NOT_TERMINATED;

  /**
   * Constructs a new {@link LooperExecutor}.
   *
   * @param looper base looper
   */
  LooperExecutor(Looper looper) {
    mThread = (mLooper = looper).getThread();
    mHandler = new Handler(mLooper);
    mErrors = e -> {if ((e = e.getCause()) instanceof Error) throw (Error) e;};
  }

  /** {@inheritDoc} */
  @Override
  protected final <T> RunnableFuture<T> newTaskFor(Callable<T> call) {
    return new ScheduledTask<>(call, 0, TIME_UNIT, mHandler::removeCallbacks, mErrors);
  }

  /** {@inheritDoc} */
  @Override
  protected final <T> RunnableFuture<T> newTaskFor(Runnable run, T val) {
    return new ScheduledTask<>(Executors.callable(run, val), 0, TIME_UNIT, mHandler::removeCallbacks, mErrors);
  }

  /** {@inheritDoc} */
  @Override
  public final ScheduledFuture<?> schedule
  (Runnable command, long delay, TimeUnit unit) {
    final ScheduledTask<Object> result = new ScheduledTask<>
      (Executors.callable(command), delay, unit, mHandler::removeCallbacks, mErrors);
    post(result, unit.toMillis(delay));
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public final <V> ScheduledFuture<V> schedule
  (Callable<V> callable, long delay, TimeUnit unit) {
    final ScheduledTask<V> result = new ScheduledTask<>
      (callable, delay, unit, mHandler::removeCallbacks, mErrors);
    post(result, unit.toMillis(delay));
    return result;
  }

  /**
   * @param task  runnable task
   * @param delay task delay
   */
  private void post(Runnable task, long delay) {
    if (!mHandler.postDelayed(task, delay))
      mErrors.accept(new Throwable(new Error("Looper was terminated")));
  }

  /** {@inheritDoc} */
  @Override
  public final ScheduledFuture<?> scheduleAtFixedRate
  (Runnable command, long initial, long period, TimeUnit unit) {
    return schedule(command, initial, unit);
  }

  /** {@inheritDoc} */
  @Override
  public final ScheduledFuture<?> scheduleWithFixedDelay
  (Runnable command, long initial, long delay, TimeUnit unit) {
    return schedule(command, initial, unit);
  }

  /** {@inheritDoc} */
  @Override
  public final void shutdown() {
    if (setTerminated(TERMINATED))
      mLooper.quitSafely();
  }

  /** {@inheritDoc} */
  @Override
  public final List<Runnable> shutdownNow() {
    shutdown();
    return Collections.emptyList();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isShutdown() {
    return TERMINATED_UPDATER.get(this) == TERMINATED;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isTerminated() {
    return isShutdown() && !mThread.isAlive();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(time);
    while (true) {
      if (isTerminated()) return true;
      else if (nanos <= 0) {return false;}
      else {
        final long now = System.nanoTime();
        TIME_UNIT.timedJoin(mThread, nanos);
        nanos -= System.nanoTime() - now;
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void execute(Runnable command) {
    schedule(command, 0, TIME_UNIT);
  }

  /**
   * @param value new terminated state
   *
   * @return true if state was changed
   */
  private boolean setTerminated(@SuppressWarnings("SameParameterValue") long value) {
    long terminated;
    do if ((terminated = TERMINATED_UPDATER.get(this)) == value) return false;
    while (!TERMINATED_UPDATER.compareAndSet(this, terminated, value)); return true;
  }

  /**
   * @param runnable source task
   * @param cancel cancel callback
   * @param failed error callback
   */
  private static void checkResult(Runnable runnable, Consumer<Runnable> cancel, Consumer<Throwable> failed) {
    if (!(runnable instanceof Future)) return;
    try {((Future)runnable).get();}
    catch (CancellationException exception)
    {if (cancel != null) cancel.accept(runnable);}
    catch (ExecutionException | InterruptedException exception)
    {if (failed != null) failed.accept(exception);}
  }

  /**
   * Scheduled task.
   *
   * @param <T> type of result
   */
  private static final class ScheduledTask<T>
    implements RunnableScheduledFuture<T> {

    /** Execution time. */
    private final long mTime;

    /** Future Task. */
    private final FutureTask<T> mFutureTask;

    /**
     * Constructs a new {@link ScheduledTask}.
     *
     * @param call callable task
     * @param time schedule delay
     * @param unit schedule time-unit
     * @param cancel cancellation callback
     * @param onFailed errors callback
     */
    ScheduledTask(Callable<T> call, long time, TimeUnit unit,
                  Consumer<Runnable> cancel,
                  Consumer<Throwable> onFailed) {
      mFutureTask = new FutureTask<T>(call)
      {@Override protected final void done()
        {checkResult(this, cancel, onFailed);}};
      mTime = unit.toNanos(time);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isPeriodic() {return false;}

    /** {@inheritDoc} */
    public final long getDelay(TimeUnit unit) {
      return unit.convert(mTime - System.nanoTime(), TIME_UNIT);
    }

    /** {@inheritDoc} */
    public final int compareTo(Delayed other) {
      if (other == this) return 0;
      if (other instanceof ScheduledTask) {
        final ScheduledTask<?> x = (ScheduledTask<?>) other;
        final long diff = mTime - x.mTime;
        if (diff < 0) return -1;
        else if (diff > 0) return 1;
        else return 1;
      }
      final long diff = getDelay(TIME_UNIT) - other.getDelay(TIME_UNIT);
      return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
    }

    /** {@inheritDoc} */
    @Override
    public final void run() {
      mFutureTask.run();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean cancel(boolean interrupt) {
      return mFutureTask.cancel(interrupt);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isCancelled() {
      return mFutureTask.isCancelled();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isDone() {
      return mFutureTask.isDone();
    }

    /** {@inheritDoc} */
    @Override
    public final T get() throws ExecutionException, InterruptedException {
      return mFutureTask.get();
    }

    /** {@inheritDoc} */
    @Override
    public final T get(long time, TimeUnit unit)
      throws ExecutionException, InterruptedException, TimeoutException {
      return mFutureTask.get(time, unit);
    }
  }
}
