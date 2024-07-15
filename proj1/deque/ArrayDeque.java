package deque;

public class ArrayDeque<T> implements Deque<T> {
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
        int distance = data.length / 4 * 3;
        if (head < tail) {
            for (int i = head + 1; i < tail; i++) {
                newData[i - head - 1] = data[i];
            }
        } else {
            System.arraycopy(data, 0, newData, 0, tail);
            System.arraycopy(data, head + 1, newData, head + 1 - distance,
                    data.length - (head + 1));
        }
        head -= distance;
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

    @Override
    public boolean isEmpty() {
        return size == 0;
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
        if (head < tail) {
            for (int i = head + 1; i < tail; i++) {
                System.out.print(data[i] + " ");
            }
        } else {
            for (int i = head + 1; i < data.length; i++) {
                System.out.print(data[i] + " ");
            }
            for (int i = 0; i < tail; i++) {
                System.out.print(data[i] + " ");
            }
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        if (size == data.length / 4 && data.length >= 16) {
            resizeIfLessThanQuarter();
        }
        head++;
        if (head == data.length) {
            head -= data.length;
        }
        size--;
        return data[head];
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        if (size == data.length / 4 && data.length >= 16) {
            resizeIfLessThanQuarter();
        }
        tail--;
        if (tail < 0) {
            tail += data.length;
        }
        size--;
        return data[tail];
    }

    @Override
    public T get(int index) {
        if (index >= size) {
            return null;
        }
        return data[(index + head + 1) % data.length];
    }
}
