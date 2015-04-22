package org.le.core;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleMemaryCache implements Cache {
    private static Map<String, Object> cache = new ConcurrentHashMap<String, Object>();
    private static Cache instance = new SimpleMemaryCache();

    private SimpleMemaryCache() {

    }

    public static Cache newInstance() {
        return instance;
    }

    @Override
    public void add(String key, Object value) {
        if (StringUtils.isEmpty(key))
            return;
        cache.put(key, value);
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }
}
