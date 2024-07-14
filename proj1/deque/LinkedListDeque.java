package deque;

public class LinkedListDeque<T> implements Deque<T> {
    private class Node {
        T data;
        Node prev;
        Node next;

        public Node(T data) {
            this.data = data;
            prev = null;
            next = null;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    public LinkedListDeque() {
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        Node node = new Node(item);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        size++;
    }

    @Override
    public void addLast(T item) {
        Node node = new Node(item);
        if (head == null) {
            head = node;
        } else {
            tail.next = node;
            node.prev = tail;
        }
        tail = node;
        size++;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (Node node = head; node != null; node = node.next) {
            System.out.print(node.data + " ");
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (head == null) {
            return null;
        }
        size--;
        if (size == 0) {
            T item = head.data;
            head = tail = null;
            return item;
        }
        Node node = head;
        head = head.next;
        head.prev = node.next = null;
        return node.data;
    }

    @Override
    public T removeLast() {
        if (tail == null) {
            return null;
        }
        size--;
        if (size == 0) {
            T item = head.data;
            head = tail = null;
            return item;
        }
        Node node = tail;
        tail = tail.prev;
        tail.next = node.prev = null;
        return node.data;
    }

    @Override
    public T get(int index) {
        try {
            Node node = head;
            for (int i = 0; i < index; i++) {
                node = node.next;
            }
            return node.data;
        } catch (NullPointerException e) {
            return null;
        }
    }
}
