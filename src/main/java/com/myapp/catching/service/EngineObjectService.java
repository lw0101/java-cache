package com.myapp.catching.service;

import com.myapp.catching.model.EngineObject;
import com.myapp.catching.repository.EngineObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@CacheConfig(cacheNames = "EngineObjectService")
public class EngineObjectService {

    @Autowired
    EngineObjectRepository engineObjectRepository;

    private static final Logger logger = LoggerFactory.getLogger(EngineObjectService.class);

    @CacheEvict(allEntries = true)
    public void clearCache(){
        logger.info("Clearing Cache");
    }

    @Cacheable(key = "#p0")
    public EngineObject findById(Long id) {
        return engineObjectRepository.findById(id).get();
    }

    @Cacheable
    public List<EngineObject> findAll() {
        logger.info("findAll");
        return (List<EngineObject>) Optional.ofNullable(engineObjectRepository.findAll())
                .orElseThrow(() -> new RuntimeException("find All not found"));
    }

    @CachePut(key = "#p0.id")
    public EngineObject save(EngineObject engineObject) {
        return engineObjectRepository.save(engineObject);
    }

    @CacheEvict(key = "#p0")
    public Boolean delete(Long id) {
        return engineObjectRepository.existsById(id);
    }


}
