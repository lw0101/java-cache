package com.myapp.demo;


import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Rectangle;

import com.myapp.catching.model.EngineObject;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.sql.*;


public class PlainApplication {
    private static final Logger logger = LoggerFactory.getLogger(PlainApplication.class);

    static final int maxObjects = 1000000;


    private CacheManager cacheManager;

    public PlainApplication() {

        initCache();
    }

    public static void main(String[] args) {
        try {
            new PlainApplication().run(args);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    public void run(String... args) {
        try {
            buildRTreeWithCache();
            Thread.sleep(10000);
//            engineObjectService.hitCache(maxObjects);
            Thread.sleep(10000);
//            engineObjectService.clearCache();
//            engineObjectService.testClashes(maxObjects);
            Thread.sleep(10000);
//            cacheStatistics.printStats();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    public RTree<Long, Geometry> findAllForRowSet(int maxItems) throws Exception {
        String query = "select id, class_name, gwkt from engine_object order by id desc limit ?";
        RTree<Long, Geometry> rTree = RTree.create();
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)
        ) {

            pstmt.setInt(1, maxItems);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EngineObject eo = buildEngineObject(
                        rs.getLong("id"),
                        rs.getString("class_name"),
                        rs.getString("gwkt"));
                //cacheManager.getCache("engine-object",Long.class,Object.class).put(eo.getObjectId(), eo);
                rTree = addRteeNode(eo, rTree);
                eo = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rTree;
    }

    public void buildRTreeWithCache() throws Exception{
        long startTime, stopTime;
        logger.info("## Build rTree and cache JDBC ...");
        startTime = System.nanoTime();
        findAllForRowSet(maxObjects);
        stopTime = System.nanoTime();
        logger.info("## Build rTree and cache JDBC: {} s. Memory KB {} ",
                (stopTime - startTime) * 0.000000001f,
                getReallyUsedMemory() / 1024d );

    }





    public EngineObject buildEngineObject(Long id, String className, String wkt) throws Exception {
        EngineObject engineObject = new EngineObject();
        engineObject.setClassName(className);
        engineObject.setJtsGeom(getJtsGeom(wkt));
        engineObject.setObjectId(id);
        return engineObject;
    }

    public RTree<Long, Geometry> addRteeNode(EngineObject engineObject, RTree<Long, Geometry> rTree) {
        if (engineObject.getJtsGeom() != null) {
            Envelope jtsEnvelope = engineObject.getJtsGeom().getEnvelopeInternal();
            Rectangle rtreeEnv = Geometries.rectangle(
                    jtsEnvelope.getMinX(), jtsEnvelope.getMinY(),
                    jtsEnvelope.getMaxX(), jtsEnvelope.getMaxY());
            rTree = rTree.add(engineObject.getObjectId(), rtreeEnv);
        }
        return rTree;
    }
    /**
     * @param geomWkt
     * @return com.vividsolutions.jts.geom.Geometry
     * @throws ParseException
     */
    public com.vividsolutions.jts.geom.Geometry getJtsGeom(String geomWkt) throws ParseException {
        WKTReader jtsWktReader = new WKTReader();
        com.vividsolutions.jts.geom.Geometry jtsGeom = jtsWktReader.read(geomWkt);
        // jtsGeom.normalize();
        jtsWktReader = null;
        return jtsGeom;
    }

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:15432/test-gwkt-cache",
                    "postgres",
                    "password");
            logger.info("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return conn;
    }

    long getCurrentlyUsedMemory() {
        return
                ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() +
                        ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }
    long getGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) { sum +=  count; }
        }
        return sum;
    }
    long getReallyUsedMemory() {
        long before = getGcCount();
        System.gc();
        while (getGcCount() == before);
        return getCurrentlyUsedMemory();
    }

    private void initCache() {
        try {
            CachingProvider cachingProvider = Caching.getCachingProvider();
            cacheManager = cachingProvider.getCacheManager(
                    getClass().getClassLoader().getResource("ehcachev2.xml").toURI(),
                    getClass().getClassLoader());
            cacheManager.getCache("engine-object");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
