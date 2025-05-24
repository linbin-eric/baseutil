package com.jfirer.baseutil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiHashMap<K, V>
{
    private Map<K, Set<V>> map = new HashMap<>();

    public void put(K k, V v)
    {
        Set<V> vs = map.computeIfAbsent(k, c -> new HashSet<>());
        vs.add(v);
    }

    public Set<V> get(K key)
    {
        Set<V> vs = map.get(key);
        if (vs == null)
        {
            return Set.of();
        }
        return vs;
    }

    public void remove(K k)
    {
        map.remove(k);
    }

    public void removeItem(K k, V v)
    {
        Set<V> vs = map.get(k);
        if (vs == null)
        {
            return;
        }
        vs.remove(v);
    }

    public void clear()
    {
        map.clear();
    }
}
