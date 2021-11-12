package com.example.ehcache.config;

import com.example.ehcache.vo.DataVO;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;

/**
 * ehcache3 Programmatic configuration
 * 官方文档：https://www.ehcache.org/documentation/3.9/getting-started.html
 */
@Slf4j
@Configuration
public class EhcacheConfiguration {

    public static final String CACHE_NAME = "demo";

    @Value("${ehcache.read-from-xml}")
    private Boolean readFromXml;

    /**
     * 过期策略
     * no expiry
     * timeToLive
     * timeToIdle-this means cache mappings will expire after a fixed duration following the time they were last accessed
     * https://www.ehcache.org/documentation/3.9/expiry.html
     *
     * 存储位置选择：
     * 1.堆
     * 2.堆外-需要自己定义资源池
     * 3.磁盘
     * 4.集群
     * https://www.ehcache.org/documentation/3.9/tiering.html
     *
     * 驱逐策略：
     * 官方对ehcache3的驱逐策略给的资料较少，而且提示，驱逐时会降低效率。网上查资料有的说，在ehcache看来，所有的缓存对象都是等价的
     * https://www.ehcache.org/documentation/3.9/eviction-advisor.html
     * @return org.ehcache.CacheManager
     */
    @Bean
    public CacheManager cacheManager(CacheEventListener<Object, Object> cacheEventListener) {
        CacheManager result;
        if (readFromXml) {
            result = initCacheManagerFromXml();
        }else {
            result = initCacheManagerFromProgrammatic(cacheEventListener);
        }
        return result;
    }

    private CacheManager initCacheManagerFromXml() {
        URL resource = getClass().getResource("/ehcache.xml");
        Objects.requireNonNull(resource);
        XmlConfiguration xmlConfiguration = new XmlConfiguration(resource);
        CacheManager result = CacheManagerBuilder.newCacheManager(xmlConfiguration);
        result.init();
        return result;
    }

    public CacheManager initCacheManagerFromProgrammatic(CacheEventListener<Object, Object> cacheEventListener) {
        return CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(CACHE_NAME,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, DataVO.class, ResourcePoolsBuilder.heap(2))
                                // 过期策略只能选一种，存在多种，后面的覆盖前面的
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(30)))
                                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(2)))
                                .withExpiry(ExpiryPolicy.NO_EXPIRY)
                                // 配置监听器
                                .withService(initCacheEventListenerConfigurationBuilder(cacheEventListener)))
                .build(true);
    }

    /**
     * cache监听器
     * @param cacheEventListener
     * @return
     */
    private CacheEventListenerConfigurationBuilder initCacheEventListenerConfigurationBuilder(CacheEventListener<Object, Object> cacheEventListener) {
        return  CacheEventListenerConfigurationBuilder
                .newEventListenerConfiguration(cacheEventListener, EventType.CREATED, EventType.EXPIRED, EventType.UPDATED, EventType.REMOVED)
                .unordered()
                .asynchronous();
    }
}
