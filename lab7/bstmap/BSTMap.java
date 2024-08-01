package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private BSTNode root;

    private int size;

    public BSTMap() {
        root = null;
        size = 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        BSTNode current = root;
        while (current != null) {
            if (current.getKey().equals(key)) {
                return true;
            }
            if (current.getKey().compareTo(key) > 0) {
                current = current.getLeft();
            } else {
                current = current.getRight();
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        BSTNode current = root;
        while (current != null) {
            if (current.getKey().equals(key)) {
                return current.getValue();
            }
            if (current.getKey().compareTo(key) > 0) {
                current = current.getLeft();
            } else {
                current = current.getRight();
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (root == null) {
            root = new BSTNode(key, value);
            size++;
            return;
        }
        BSTNode current = root;
        while (true) {
            if (current.getKey().equals(key)) {
                current.setValue(value);
                return;
            }
            if (current.getKey().compareTo(key) > 0) {
                if (current.getLeft() == null) {
                    current.setLeft(new BSTNode(key, value));
                    size++;
                    return;
                } else {
                    current = current.getLeft();
                }
            } else {
                if (current.getRight() == null) {
                    current.setRight(new BSTNode(key, value));
                    size++;
                    return;
                } else {
                    current = current.getRight();
                }
            }
        }
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    private void dfsPrintInOrder(BSTNode node) {
        if (node == null) {
            return;
        }
        dfsPrintInOrder(node.getLeft());
        System.out.print(node.getKey() + ", ");
        dfsPrintInOrder(node.getRight());
    }

    public void printInOrder() {
        System.out.print("[");
        dfsPrintInOrder(root);
        System.out.println("]");
    }

    private class BSTNode {
        private K key;
        private V value;
        private BSTNode left, right;

        public BSTNode(K key, V value, BSTNode left, BSTNode right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
        }

        public BSTNode(K key, V value) {
            this(key, value, null, null);
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public BSTNode getLeft() {
            return left;
        }

        public void setLeft(BSTNode left) {
            this.left = left;
        }

        public BSTNode getRight() {
            return right;
        }

        public void setRight(BSTNode right) {
            this.right = right;
        }
    }
}
