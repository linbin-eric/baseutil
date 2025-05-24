package com.jfirer.baseutil;

import java.util.HashMap;
import java.util.Map;

public class HashBiMap<K, V>
{
    private Map<K, V> map     = new HashMap<>();
    private Map<V, K> reverse = new HashMap<>();

    public void put(K k, V v)
    {
        map.put(k, v);
        reverse.put(v, k);
    }

    public void remove(K k)
    {
        V removed = map.remove(k);
        if (removed != null)
        {
            reverse.remove(removed);
        }
    }

    public void remove(K k, V v)
    {
        boolean remove = map.remove(k, v);
        if (remove)
        {
            reverse.remove(v);
        }
    }

    public Map<V, K> reverse()
    {
        return reverse;
    }

    public void clear()
    {
        map.clear();
        reverse.clear();
    }
}
