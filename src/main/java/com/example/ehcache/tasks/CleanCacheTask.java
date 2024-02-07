package com.example.ehcache.tasks;

import org.ehcache.Cache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

@Component
public class CleanCacheTask {

    public static final Vector<Cache> CACHES = new Vector<>();

    /**
     * 清缓存的原理很简单，直接遍历cache，重新访问每一项，如果过期了，就会被清理掉
     */
    @Scheduled(fixedDelay = 5 , timeUnit = TimeUnit.MINUTES)
    private void cleanCache() {
        CACHES.forEach(cacheName -> {
            Iterator iterator = cacheName.iterator();
            while (iterator.hasNext()) {
                iterator.next();
            }
        });
    }
}
