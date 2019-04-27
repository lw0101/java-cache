package com.myapp.catching;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CachingConfig {
}
//https://stackoverflow.com/questions/53474435/place-ehcache-3-ehcache-xml-outside-of-the-springboot-2-spring-5-projects-ja