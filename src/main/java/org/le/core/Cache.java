package org.le.core;

public interface Cache {

    void add(String key,Object value);

    Object get(String key);

}
