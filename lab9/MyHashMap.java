package lab9;

import java.util.*;

public class MyHashMap<K, V> implements Map61B<K, V>, Iterable<K> {
    private int size; // number of items (key-value pairs) in total
    private final double loadFactor; // ratio of items to number of boxes for resizing
    private ArrayList<Pair<K, V>>[] boxes; // hash table to place the lists of items

    /* a "Pair" is going to be the key-value pair for the map */
    private static class Pair<K, V> {
        private K key;
        private V value;
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public MyHashMap() {
        this.size = 0; // starts out with zero key-value pairs
        this.loadFactor = 10.0; // will resize when there are 10 times as many pairs as boxes
        this.boxes = (ArrayList<Pair<K, V>>[]) new ArrayList[8];
        // boxes is an array holding LinkedLists that hold Pairs

        // put a LinkedList that holds Pairs in each box
        for (int i = 0; i < this.boxes.length; i += 1) {
            this.boxes[i] = new ArrayList<Pair<K, V>>();
        }
    }

    public MyHashMap(int initialSize) {
        this.size = 0;
        this.loadFactor = 10.0;
        this.boxes = (ArrayList<Pair<K, V>>[]) new ArrayList[initialSize];
        for (int i = 0; i < this.boxes.length; i += 1) {
            this.boxes[i] = new ArrayList<Pair<K, V>>();
        }
    }

    public MyHashMap(int initialSize, double loadFactor) {
        this.size = 0;
        this.loadFactor = loadFactor;
        this.boxes = (ArrayList<Pair<K, V>>[]) new ArrayList[initialSize];
        for (int i = 0; i < this.boxes.length; i += 1) {
            this.boxes[i] = new ArrayList<Pair<K, V>>();
        }
    }

    /** Removes all mappings from this map. */
    public void clear() {
        this.size = 0;
        for (int i = 0; i < this.boxes.length; i += 1) {
            this.boxes[i] = new ArrayList<Pair<K, V>>();
        }
    }

    /* Returns true if this map contains a mapping for the specified key. */
    public boolean containsKey(K key) {
        return this.get(key) != null;
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    public V get(K key) {
        int box = ((key.hashCode() % this.boxes.length) + this.boxes.length) % this.boxes.length;
        for (int i = 0; i < boxes[box].size(); i += 1) {
            if (boxes[box].get(i).key.equals(key)) {
                return boxes[box].get(i).value;
            }
        }
        return null;
    }

    /* Returns the number of key-value mappings in this map. */
    public int size() {
        return this.size;
    }

    /* resize the array of boxes
       note: only requires resizing upwards
       further note: only resizing upwards shouldn't matter for this method  */
    public void resize(int capacity) {
        // set up the new boxes
        ArrayList<Pair<K, V>>[] newBoxes = (ArrayList<Pair<K, V>>[]) new ArrayList[capacity];
        for (int i = 0; i < capacity; i += 1) {
            newBoxes[i] = new ArrayList<Pair<K, V>>();
        }
        // take all the stuff from the original boxes and put them in the new ones
        for (ArrayList<Pair<K, V>> box : boxes) { // go through each box
            for (Pair<K, V> pair : box) { // place each pair in the correct new box
                int newBox = ((pair.key.hashCode() % capacity) + capacity) % capacity;
                newBoxes[newBox].add(pair);
            }
        }
        this.boxes = newBoxes;
    }

    /* Associates the specified value with the specified key in this map. */
    public void put(K key, V value) {
        int box = ((key.hashCode() % this.boxes.length) + this.boxes.length) % this.boxes.length; // choose the box for this key/Pair
        if (this.containsKey(key)) { // if the key is already there, update the value
            for (Pair<K, V> pair : boxes[box]) { // check the pairs and update the value for the correct key
                if (pair.key == key) {
                    pair.value = value;
                }
            }
        } else { // put the new key in since it's not in the map already
            Pair<K, V> thisPair = new Pair<K, V>(key, value); // create a Pair from the inputs
            this.boxes[box].add(thisPair);
            this.size += 1;
        }

        // double the number of boxes if there are now too many items
        if (this.size > this.boxes.length * this.loadFactor) {
            this.resize(this.boxes.length * 2);
        }
    }

    /* Returns a Set view of the keys contained in this map. */
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();
        for (ArrayList<Pair<K, V>> box : boxes) { // go through each box
            for (Pair<K, V> pair : box) { // adding all the keys from that box
                K ithKey = pair.key;
                keySet.add(ithKey);
            }
        }
        return keySet;
    }

    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException. */
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    public Iterator<K> iterator() {
        // return this.keySet().iterator(); THIS WAS MY OLD SOLUTION BEFORE CREATING MY OWN ITERATOR
        return this.new MyHashMapIterator();
    }

    private class MyHashMapIterator implements Iterator<K> {
        public Pair<K, V> currentPair;
        public int currentBox;
        public int boxIndex;
        public int counter;
        public MyHashMapIterator() {
            this.currentBox = 0;
            while (boxes[currentBox].isEmpty()) { // make sure to move currentBox to one that actually holds something
                currentBox += 1;
            }
            this.boxIndex = 0;
            this.currentPair = boxes[currentBox].get(boxIndex);
            this.counter = 0;
        }
        public boolean hasNext() {
            return this.counter < size; // if you've counted as large as the size then there are no more items
        }
        public K next() {
            if (!hasNext()) { // make sure there are still more things in the iterator
                throw new NoSuchElementException("No more items to return.");
            }

            K returnKey = this.currentPair.key; // set aside the return value
            if (this.boxIndex + 1 < boxes[currentBox].size()) { // if there are more things in this box, move to the next
                this.boxIndex += 1;
            } else { // move to the next nonempty box and reset the boxIndex to 0
                if (this.currentBox + 1 == boxes.length) {
                    // NO ACTION REQUIRED, YOU HAVE REACHED THE LAST ITEM IN THE ITERATOR
                } else {
                    this.currentBox += 1;
                    while (boxes[currentBox].isEmpty()) {
                        currentBox += 1;
                    }
                    this.boxIndex = 0;
                }
            } // you're now at the correct box and index for the next Pair
            this.currentPair = boxes[currentBox].get(boxIndex);
            counter += 1;
            return returnKey;
        }
    }

    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        for (int i = 0; i < 500; i += 1) {
            map.put("item" + i, i);
        }

        Iterator<String> a = map.iterator();

        for (int i = 0; i < 500; i += 1) {
            System.out.println(a.next());
        }
    }
}
