package util;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Set;

public class BiHashMap<K, V> {

    private HashMap<K, V> forward = new HashMap<>();
    private HashMap<V, K> reverse = new HashMap<>();

    public synchronized void add(Pair<K, V> pair) {
        add(pair.getKey(), pair.getValue());
    }
    public synchronized void add(K key, V value) {
        forward.put(key, value);
        reverse.put(value, key);
    }
    public synchronized Set<K> keySet() {
        return forward.keySet();
    }
    public synchronized Set<V> valueSet() {
        return reverse.keySet();
    }
    public synchronized boolean containsKey(K key) {
        return forward.containsKey(key);
    }
    public synchronized boolean containsValue(V val) {
        return reverse.containsKey(val);
    }
    public synchronized V getForward(K key) {
        return forward.get(key);
    }
    public synchronized K getReverse(V value) {
        return reverse.get(value);
    }
    public synchronized void replaceForward(K oldKey, K newKey) {
        V value = forward.get(oldKey);
        reverse.replace(value, newKey);
        forward.remove(oldKey);
        forward.put(newKey, value);
    }
    public synchronized void replaceReverse(V oldVal, V newVal) {
        K key = reverse.get(oldVal);
        forward.replace(key, newVal);
        reverse.remove(oldVal);
        reverse.put(newVal, key);
    }
    public synchronized void reMapForward(K key, V value) {
        V oldValue = forward.get(key);
        forward.replace(key, value);
        reverse.remove(oldValue);
        reverse.put(value, key);
    }
    public synchronized void reMapReverse(V value, K key) {
        K oldKey = reverse.get(value);
        reverse.replace(value, key);
        forward.remove(oldKey);
        forward.put(key, value);
    }
    public synchronized void removeForward(K key) {
        reverse.remove(forward.remove(key));
    }
    public synchronized void removeReverse(V value) {
        forward.remove(reverse.remove(value));
    }

    @Override
    public String toString() {
        return forward.toString();
    }

    /**
     * This method will clone pointers to the referenced objects. This cloned BiHashMap should NOT be used
     *      to change values at the pointers. Changing what the pointers point to are fine.
     * @return a cloned BiHashMap
     */
    @Override
    public BiHashMap clone() {
        BiHashMap<K, V> bhm = new BiHashMap<>();
        for (K key : keySet()) {
            bhm.add(key, getForward(key));
        }
        return bhm;
    }
}
