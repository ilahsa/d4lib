package net.d4.d4lib.util;


/**
 *
 */
public class NodeEnty1<K, V> {
    private K k;
    private V v;

    public NodeEnty1() {
    }

    public NodeEnty1(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K getKey() {
        return k;
    }

    public V getValue() {
        return v;
    }

    public void setK(K k) {
        this.k = k;
    }

    public void setV(V v) {
        this.v = v;
    }

    @Override
    public String toString() {
        return "NodeEnty{" + "k=" + k + ", v=" + v + '}';
    }

}
