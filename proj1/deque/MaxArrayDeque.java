package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> comparator) {
        super();
        this.comparator = comparator;
    }

    public T max(Comparator<T> comparator) {
        if (isEmpty()) {
            return null;
        }
        T max = get(0);
        for (T i : this) {
            if (comparator.compare(i, max) > 0) {
                max = i;
            }
        }
        return max;
    }

    public T max() {
        return max(comparator);
    }
}
