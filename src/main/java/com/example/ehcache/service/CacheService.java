package com.example.ehcache.service;

import com.example.ehcache.tasks.CleanCacheTask;
import lombok.extern.java.Log;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log
public class CacheService {

    @Autowired
    private CacheManager cacheManager;

    /**
     * 除了使用withCache方式创建默认缓存之外，还可以使用createCache方式创建缓存
     * https://www.ehcache.org/documentation/3.10/getting-started.html#configuring-with-java
     * @return
     */
    public  <K, V> Cache<K, V> createCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        CacheConfiguration<K, V> cacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, valueType,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(50, MemoryUnit.MB))
                .withSizeOfMaxObjectGraph(100)
//                单个对象的大小
                .withSizeOfMaxObjectSize(10, MemoryUnit.KB)
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(5)))
                .withService(initCacheEventListenerConfigurationBuilder())
                .build();
        Cache<K, V> result = cacheManager.createCache(cacheName, cacheConfiguration);
        CleanCacheTask.CACHES.add(result);
        return result;
    }


    private CacheEventListenerConfigurationBuilder initCacheEventListenerConfigurationBuilder() {
        return  CacheEventListenerConfigurationBuilder
                .newEventListenerConfiguration(cacheEvent -> {
                    log.info("cache event type is: "+ cacheEvent.getType()+ " key is: " +cacheEvent.getKey());
                }, EventType.CREATED, EventType.EXPIRED, EventType.UPDATED, EventType.REMOVED)
                .unordered()
                .asynchronous();
    }


}
