/*
 * Trident - A Multithreaded Server Alternative
 * Copyright 2014 The TridentSDK Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tridentsdk.server.concurrent;

import javax.annotation.concurrent.GuardedBy;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Synchronizes plugin calls with the ticking thread
 *
 * <p>This class is necessary because plugins that are not following the Trident threading model can mutate and break
 * the state consistency as described by the model. This is not good because plugins should not be expected to follow
 * the threading model.</p>
 *
 * <p>A demonstration of the state consistency being broken by conflicting thread-models:
 * <pre>{@code
 *      WeatherConditions cnd = world.weather();
 *      if (!cnd.isRaining()) {    // If it is not raining
 *          cnd.setRaining(true);  // start raining
 *      }
 *
 *      // Should return true!
 *      boolean stillRaining = cnd.isRaining();
 * }</pre>
 * The {@code stillRaining} variable can return false:
 * <pre>
 *      World Tick Thread ---> raining == false -----------------> raining = !true -> raining == false
 *      Plugin Thread -----> raining == false -> raining = true -----------------------> stillRaining == false
 * </pre>
 * Not only do we not provide a set raining method, this class will be used to hold off plugin notification of server
 * events until the entire tick has been processed.
 * </p>
 *
 * <p>Unless you want to severely mess up the server, do not call any methods in this class unless:
 * <ul>
 *     <li>You know what you are doing (unlikely)</li>
 *     <li>Developing Trident (in which case you need to check with Pierre to ensure you're doing it right)</li>
 * </ul></p>
 *
 * @author The TridentSDK Team
 */
public final class TickSync {
    private TickSync() {
    }

    private static final LongAdder expected = new LongAdder();
    private static final LongAdder complete = new LongAdder();

    private static volatile CountDownLatch latch = new CountDownLatch(1);

    @GuardedBy("taskLock")
    private static final Queue<Runnable> pluginTasks = new LinkedList<>();
    private static volatile Lock taskLock = new ReentrantLock();
    private static volatile Condition available = taskLock.newCondition();

    /**
     * Increments the expected updates counter
     */
    public static void increment() {
        expected.increment();
    }

    /**
     * Records that an update has occurred
     * <p>
     * <p>Signals the main thread to sync method to continue if the expected and update counters match</p>
     */
    public static void complete() {
        complete.increment();

        if (canProceed()) {
            latch.countDown();
        }
    }

    /**
     * Tests to see if the expected and update counters match
     *
     * @return {@code true} to indicate that the ticking can proceed
     */
    public static boolean canProceed() {
        return expected.sum() == complete.sum();
    }

    /**
     * Blocks the thread until this method is called again by a {@link #complete()} method
     */
    public static void awaitSync() {
        if (canProceed()) return;

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the counters and the blocking mechanisms for the next tick iteration
     */
    public static void reset() {
        expected.reset();
        complete.reset();
        latch = new CountDownLatch(1);

        Lock lock = taskLock;
        lock.lock();
        try {
            taskLock = new ReentrantLock();
            available = taskLock.newCondition();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Synchronizes the task for later execution once the tick completes
     *
     * @param pluginTask the task
     */
    public static void sync(Runnable pluginTask) {
        taskLock.lock();
        try {
            pluginTasks.add(pluginTask);
            available.signal(); // SignalAll not needed, only the main thread waits on this condition
        } finally {
            taskLock.unlock();
        }
    }

    /**
     * Waits for a task to become available, or blocks until waitNanos has elapsed, in which case {@code null}
     * will be returned
     *
     * @param waitNanos the nanos to wait for a task
     * @return a task, or {@code null} if there were none
     * @throws InterruptedException if the current thread was interrupted in waiting for a task
     */
    public static Runnable waitForTask(long waitNanos) throws InterruptedException {
        taskLock.lock();
        try {
            Runnable task;
            if ((task = pluginTasks.poll()) == null) {
                available.await(waitNanos, TimeUnit.NANOSECONDS);
                task = pluginTasks.poll();
            }

            return task;
        } finally {
            taskLock.unlock();
        }
    }

    /**
     * Obtains the next task in the queue
     *
     * @return the next task, or {@code null} if there were none
     */
    public static Runnable next() {
        taskLock.lock();
        try {
            return pluginTasks.poll();
        } finally {
            taskLock.unlock();
        }
    }

    /**
     * Obtains the tasks left in the queue
     *
     * @return the amount of tasks left
     */
    public static int left() {
        taskLock.lock();
        try {
            return pluginTasks.size();
        } finally {
            taskLock.unlock();
        }
    }
}