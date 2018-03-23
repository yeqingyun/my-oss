package com.gionee.oss.task;

import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.cache.Cache;

/**
 * Created by yeqy on 2017/6/23.
 */
public class TripartSystemCacheFlushTask implements Runnable {
    private Cache<String, OssTripartiteSystem> systemKeyCache;

    public TripartSystemCacheFlushTask(Cache<String, OssTripartiteSystem> systemKeyCache) {
        this.systemKeyCache = systemKeyCache;
    }

    @Override
    public void run() {
        systemKeyCache.init();
    }
}
