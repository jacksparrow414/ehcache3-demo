package com.example.ehcache.config;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.springframework.stereotype.Component;

/**
 * cache 监听器
 */
@Component
@Slf4j
public class CacheEventLogListener implements CacheEventListener<Object, Object> {

    @Override
    public void onEvent(CacheEvent<? extends Object, ? extends Object> cacheEvent) {
        log.info("cacheType is {}, key is {}, oldValue {}, newValue {}", cacheEvent.getType().toString(), cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
    }
}
