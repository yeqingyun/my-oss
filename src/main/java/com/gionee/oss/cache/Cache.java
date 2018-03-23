package com.gionee.oss.cache;

/**
 * Created by yeqy on 2017/6/6.
 */
public interface Cache<K, V> {
    V getAndSysnc(K param);

    V get(K param);

    void init();

    void clear();

    boolean exist(K param);

    void set(K param, V data);

    void delete(K param);

}
