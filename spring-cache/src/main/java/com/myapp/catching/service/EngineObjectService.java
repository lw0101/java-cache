package com.myapp.catching.service;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.myapp.catching.jdbc.GeoWktJdbcRepository;
import com.myapp.catching.model.EngineObject;
import com.myapp.catching.model.GeoWkt;
import com.myapp.catching.repository.GeoWktRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.List;
import java.util.stream.Collectors;


@Service
@CacheConfig(cacheNames = "engine-object")
public class EngineObjectService {
    private static final Logger logger = LoggerFactory.getLogger(EngineObjectService.class);

    @Autowired
    GeoWktRepository geoWktRepository;

    @Autowired
    GeoWktJdbcRepository wktJdbcRepository;

    @Autowired
    EngineObjectBuilder engineObjectBuilder;

    @Autowired
    private CacheManager cacheManager;

    @CacheEvict(allEntries = true)
    public void clearCache(){
        logger.info("Clearing Cache");
    }

    @Cacheable(key = "#p0")
    public EngineObject findById(Long id) {
        try {
            return engineObjectBuilder.buildEngineObject(geoWktRepository.findById(id).get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Cacheable
    public List<EngineObject> findAll(int maxItems) {
        logger.info("findAll");
        List<GeoWkt> geoWkts = geoWktRepository.findAll(PageRequest.of(0, maxItems)).getContent();

        return geoWkts.stream().map(e -> {
            try {
                return engineObjectBuilder.buildEngineObject(e);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
    }

    public RTree<Long, Geometry> getRTree(List<EngineObject> engineObjectList) {
        return engineObjectBuilder.buildRTree(engineObjectList);
    }

    public RTree<Long, Geometry> buildRTreeWithCache(int maxItems) throws Exception {
        Cache<Object, Object> cache = cacheManager.getCache("engine-object");
        RTree<Long, Geometry> rTree = engineObjectBuilder.initRtee();

        SqlRowSet rs = wktJdbcRepository.findAllForRowSet(maxItems);
        while (rs.next()) {
            EngineObject eo = engineObjectBuilder.buildEngineObject(
                    rs.getLong("id"),
                    rs.getString("class_name"),
                    rs.getString("gwkt"));
            cache.put(eo.getObjectId(), eo);
            rTree = engineObjectBuilder.addRteeNode(eo, rTree);
        }

        return rTree;
    }
}
