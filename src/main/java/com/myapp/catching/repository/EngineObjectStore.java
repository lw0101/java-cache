package com.myapp.catching.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EngineObjectStore {
    private static final Logger log = LoggerFactory.getLogger(EngineObjectStore.class);

    @Autowired
    private javax.cache.CacheManager cacheManager;


}
