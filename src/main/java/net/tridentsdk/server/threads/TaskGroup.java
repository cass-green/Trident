package net.tridentsdk.server.threads;

import com.google.common.collect.Iterators;
import net.tridentsdk.factory.ExecutorFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Groups items so each does not need to be submitted, and rather batched processed
 * <p>
 * <p>This class is marked non-thread safe because it holds a state and should not be shared
 * while being built.</p>
 * <p>
 * <p>Example:
 * <pre><code>
 *     TaskGroup.process(items).every(16).with(executor).using(() -> { ... });
 * </code></pre></p>
 *
 * @author The TridentSDK Team
 */
@NotThreadSafe
public class TaskGroup<T> {
    private final Collection<T> items;
    private int per;
    private ExecutorFactory factory;

    private TaskGroup(Collection<T> items) {
        this.items = items;
    }

    // Building methods
    // #functionalprogramming

    /**
     * Processes the items specified
     *
     * @param items the items to process
     */
    public static <T> TaskGroup<T> process(Collection<T> items) {
        return new TaskGroup<>(items);
    }

    /**
     * Creates a new TaskGroup which groups items in batches of the specified size
     *
     * @param tasks the batch size
     * @return the task group
     */
    public TaskGroup<T> every(int tasks) {
        this.per = tasks;
        return this;
    }

    /**
     * Runs each group of items on the factory given
     *
     * @param factory the executor to process on
     */
    public TaskGroup<T> with(ExecutorFactory factory) {
        this.factory = factory;
        return this;
    }

    /**
     * Partitions the items and processes them
     *
     * @param consumer the action to process each item with
     */
    public void using(Consumer<T> consumer) {
        Iterator<T> iterator = items.iterator();

        // Figure out how many we need, and add one if there are remainders
        int partitions = (items.size() / per) + (((items.size() % per) != 0) ? 1 : 0);
        Iterator<List<T>> parted = Iterators.partition(iterator, partitions);

        while (parted.hasNext()) {
            List<T> list = parted.next();
            factory.execute(() -> list.forEach(consumer::accept));
        }
    }
}