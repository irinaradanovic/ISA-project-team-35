package com.isa.jutjubic.logger;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ThumbnailCacheLogger implements CacheEventListener<Object, Object> {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailCacheLogger.class);

    @Override
    public void onEvent(CacheEvent<?, ?> cacheEvent) {
        logger.info("CACHE EVENT: Key: {} | EventType: {}",
                cacheEvent.getKey(), cacheEvent.getType());
    }

}

