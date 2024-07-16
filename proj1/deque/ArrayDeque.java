package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] data;
    private int size;
    private int head;
    private int tail;
    private static final int INITIAL_CAPACITY = 8;

    public ArrayDeque() {
        data = (T[]) new Object[INITIAL_CAPACITY];
        head = data.length - 1;
        tail = 0;
        size = 0;
    }

    private void resizeIfFull() {
        T[] newData = (T[]) new Object[data.length * 2];
        if (tail == 0) {
            System.arraycopy(data, 0, newData, 0, data.length);
            tail += data.length;
        } else {
            System.arraycopy(data, 0, newData, 0, tail);
            System.arraycopy(data, head + 1, newData, head + 1 + data.length,
                    data.length - (head + 1));
        }
        head += data.length;
        data = newData;
    }

    private void resizeIfLessThanQuarter() {
        T[] newData = (T[]) new Object[data.length / 4];
        int threeQuarters = data.length / 4 * 3;
        if (head < tail) {
            for (int i = head + 1; i < tail; i++) {
                newData[i - head - 1] = data[i];
            }
            head = newData.length - 1;
            tail = 0;
        } else {
            System.arraycopy(data, 0, newData, 0, tail);
            System.arraycopy(data, head + 1, newData, head + 1 - threeQuarters,
                    data.length - (head + 1));
            head -= threeQuarters;
            if (head < 0) {
                head += newData.length;
            }
        }
        data = newData;
    }

    @Override
    public void addFirst(T item) {
        if (isFull()) {
            resizeIfFull();
        }
        data[head] = item;
        head--;
        if (head < 0) {
            head += data.length;
        }
        size++;
    }

    @Override
    public void addLast(T item) {
        if (isFull()) {
            resizeIfFull();
        }
        data[tail] = item;
        tail++;
        if (tail == data.length) {
            tail -= data.length;
        }
        size++;
    }

    private boolean isFull() {
        return size == data.length;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        head++;
        if (head == data.length) {
            head -= data.length;
        }
        size--;
        T item = data[head];
        if (size == data.length / 4 && data.length >= 16) {
            resizeIfLessThanQuarter();
        }
        return item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        tail--;
        if (tail < 0) {
            tail += data.length;
        }
        size--;
        T item = data[tail];
        if (size == data.length / 4 && data.length >= 16) {
            resizeIfLessThanQuarter();
        }
        return item;
    }

    @Override
    public T get(int index) {
        if (index >= size) {
            return null;
        }
        return data[(index + head + 1) % data.length];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int passed;

        ArrayDequeIterator() {
            passed = 0;
        }

        @Override
        public boolean hasNext() {
            return passed < size;
        }

        @Override
        public T next() {
            return get(passed++);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<?> that = (Deque<?>) o;
        if (size != that.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!get(i).equals(that.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        ArrayDeque<Integer> deque = new ArrayDeque<Integer>();
        int cnt = 32;
        for (int i = 0; i < cnt; i++) {
            deque.addFirst(i);
        }
        for (int i = 0; i < cnt - 8; i++) {
            System.out.println(deque.removeFirst());
        }
        for (int i = 0; i < cnt; i++) {
            deque.addLast(i);
        }
        for (int i = 0; i < cnt + 8; i++) {
            System.out.println(deque.removeLast());
        }
    }
}
