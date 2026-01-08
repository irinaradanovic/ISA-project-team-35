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

    /*@Override
    public void onEvent(CacheEvent<? extends String, ? extends byte[]> cacheEvent) {
        switch (cacheEvent.getType()) {
            case CREATED:
                logger.info("Thumbnail added to cache: {}", cacheEvent.getKey());
                break;
            case UPDATED:
                logger.info("Thumbnail updated in cache: {}", cacheEvent.getKey());
                break;
            case REMOVED:
                logger.info("Thumbnail removed from cache: {}", cacheEvent.getKey());
                break;
            case EXPIRED:
                logger.info("Thumbnail expired from cache: {}", cacheEvent.getKey());
                break;
        }
    } */
}

