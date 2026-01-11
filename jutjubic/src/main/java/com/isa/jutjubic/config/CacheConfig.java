package com.isa.jutjubic.config;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.context.annotation.Configuration;
import javax.cache.CacheManager;

@Configuration
public class CacheConfig implements JCacheManagerCustomizer {
    @Override
    public void customize(CacheManager cacheManager) {

    }
}