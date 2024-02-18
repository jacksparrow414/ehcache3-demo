package com.example.ehcache.controller;

import com.example.ehcache.config.EhcacheConfiguration;
import com.example.ehcache.service.CacheService;
import com.example.ehcache.vo.DataVO;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping(path = "/data")
public class CommonDataController {

    @Resource(name = "cacheManager")
    private CacheManager cacheManager;

    @Autowired
    private CacheService cacheService;

    /**
     * curl -X GET http://localhost:18080/ehcache3/data/8
     * @param id
     * @return
     */
    @GetMapping(path = "/{id}")
    public DataVO getCacheData(@PathVariable Long id) {
        Cache<Long, DataVO> cache = cacheManager.getCache(EhcacheConfiguration.CACHE_NAME, Long.class, DataVO.class);
        DataVO result;
        result = cache.get(id);
        if (Objects.isNull(result)) {
            result = getDataFromDbAndPutInCache(id);
        }
        return result;
    }

    private DataVO getDataFromDbAndPutInCache(Long id) {
        DataVO result = new DataVO();
        result.setId(id);
        result.setName("cache---" + id);
        result.setGender(Boolean.FALSE);
        Cache<Long, DataVO> cache = cacheManager.getCache(EhcacheConfiguration.CACHE_NAME, Long.class, DataVO.class);
        cache.put(id, result);
        return result;
    }

    /**
     * curl -X POST -H 'Content-Type: application/json' -d '{"name":"jack","gender":true}' http://localhost:18080/ehcache3/data
     * @param data
     * @return
     */
    @PostMapping
    public Long createDataVO(@RequestBody DataVO data) {
        Random random = new Random();
        Long result = random.nextLong();
        data.setId(result);
        Cache<Long, DataVO> cache = cacheManager.getCache(EhcacheConfiguration.CACHE_NAME, Long.class, DataVO.class);
        cache.put(result, data);
        return result;
    }

    /**
     * &需要转义,用\
     * curl -X PUT http://localhost:18080/ehcache3/data?id=8\&name=jack
     * @param id
     * @param name
     * @return
     */
    @PutMapping
    public DataVO updateDataVO(@RequestParam Long id, @RequestParam String name) {
        Cache<Long, DataVO> cache = cacheManager.getCache(EhcacheConfiguration.CACHE_NAME, Long.class, DataVO.class);
        DataVO result = cache.get(id);
        if (Objects.isNull(result)) {
            throw new RuntimeException("cache not exist");
        }
        result.setName(name);
        cache.put(id, result);
        return result;
    }

    /**
     * curl -X DELETE http://localhost:18080/ehcache3/data/8
     * @param id
     * @return
     */
    @DeleteMapping(path = "{id}")
    public String deleteCacheData(@PathVariable Long id) {
        Cache<Long, DataVO> cache = cacheManager.getCache(EhcacheConfiguration.CACHE_NAME, Long.class, DataVO.class);
        cache.remove(id);
        return "already deleted";
    }

    /**
     * curl -X POST http://localhost:18080/ehcache3/data/createCache?cacheName=cache2
     * @param cacheName
     * @return
     */
    @PostMapping(path = "/createCache")
    public String createCache(@RequestParam String cacheName) {
        Cache<Long, DataVO> cache = cacheService.createCache(cacheName, Long.class, DataVO.class);
        return "create cache success";
    }

    /**
     * curl -X GET http://localhost:18080/ehcache3/data/isCacheExist?cacheName=cache2
     * @param cacheName
     * @return
     */
    @GetMapping(path = "/isCacheExist")
    public boolean isCacheExist(@RequestParam String cacheName) {
        Cache<Long, DataVO> cache = cacheManager.getCache(cacheName, Long.class, DataVO.class);
        return Optional.ofNullable(cache).isPresent();
    }
}
