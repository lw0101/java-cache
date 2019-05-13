package com.myapp.catching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;


@SpringBootApplication
public class StartApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartApplication.class);

//    @Autowired
//    private CacheManager cacheManager;
//
//    @Autowired
//    private GeoWktRepository geoWktRepository;
//
//    @Autowired
//    private EngineObjectService engineObjectService;



    @Value("classpath:test-data.json")
    private Resource resourceFile;

    public static final int maxObjects = 1000000;

    public static void main(String[] args) {
        SpringApplication.run(StartApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            logger.info("## StartApplication Caching Example ...");
//            logger.info("## using cache manager: " + cacheManager.getClass().getName());

//        populateTestDB();

//            engineObjectService.clearCache();
            logger.info("## Initial memory KB: " + getReallyUsedMemory() / 1024d);
//            loadEngineObjectsCache(maxObjects);
//            hitCacheEO(maxObjects);
//            buildTree(engineObjectService.findAll(maxObjects));
            buildRTreeWithCache();


//            printStats();
//            cacheManager.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
//
//    private void loadEngineObjectsCache(int totalLoad) {
//        long startTime, stopTime;
//
//        startTime = System.nanoTime();
//        List<EngineObject> objects = engineObjectService.findAll(totalLoad);
//        stopTime = System.nanoTime();
//
//        logger.info("## findAll EngineObject from DB(), {} items: {} s. Memory KB {} ",
//                objects.size(),
//                (stopTime - startTime) * 0.000000001f,
//                getReallyUsedMemory() / 1024d );
//
//        startTime = System.nanoTime();
//        objects.forEach(x -> {
//            cacheManager.getCache("engine-object").put(x.getObjectId(), x);
//        });
//        stopTime = System.nanoTime();
//        logger.info("## Loaded objects in cache: {} s. Memory KB {} ",
//                (stopTime - startTime) * 0.000000001f,
//                getReallyUsedMemory() / 1024d );
//    }
//
    public void buildRTreeWithCache() throws Exception{
        long startTime, stopTime;
        startTime = System.nanoTime();
        // engineObjectService.buildRTreeWithCache(maxObjects);
        stopTime = System.nanoTime();
        logger.info("## Build rTree and cache JDBC: {} s. Memory KB {} ",
                (stopTime - startTime) * 0.000000001f,
                getReallyUsedMemory() / 1024d );

    }
//
//    public void buildTree (List<EngineObject> engineObjects) {
//        long startTime, stopTime;
//        startTime = System.nanoTime();
//        engineObjectService.getRTree(engineObjects);
//        stopTime = System.nanoTime();
//        logger.info("## Build rTree: {} s. Memory KB {} ",
//                (stopTime - startTime) * 0.000000001f,
//                getReallyUsedMemory() / 1024d );
//
//    }
//
//
//    private void hitCacheEO(int maxObjects) throws Exception{
//
//        long startTime;
//        long stopTime;
//
//        startTime = System.nanoTime();
//        List<EngineObject> items = engineObjectService.findAll(maxObjects);
//        for (EngineObject e : items) {
//            engineObjectService.findById(e.getObjectId());
//        }
//        stopTime = System.nanoTime();
//
//        logger.info("## Loop 1: {} s. Memory KB {} ",
//                (stopTime - startTime) * 0.000000001f,
//                getReallyUsedMemory() / 1024d );
//
//        startTime = System.nanoTime();
//        for (EngineObject e : items) {
//            engineObjectService.findById(e.getObjectId());
//        }
//        stopTime = System.nanoTime();
//        logger.info("## Loop 2: {} s. Memory KB {} ",
//                (stopTime - startTime) * 0.000000001f,
//                getReallyUsedMemory() / 1024d );
//    }
//
//    private void printStats() {
//        for (String name : cacheManager.getCacheNames()) {
//            CacheStatisticsMXBean CacheStatBean = getCacheStatisticsMXBean(name);
//            if (CacheStatBean != null) {
//                logger.info("Cache hits #{} misses #{}", CacheStatBean.getCacheHits(), CacheStatBean.getCacheMisses());
//                logger.info("Cache hits %{} misses %{}", CacheStatBean.getCacheHitPercentage(),
//                        CacheStatBean.getCacheMissPercentage());
//                logger.info("Cache gets #{}", CacheStatBean.getCacheGets());
//                logger.info("Cache evictions #{}", CacheStatBean.getCacheEvictions());
//                logger.info("Cache average get time {} milliseconds", CacheStatBean.getAverageGetTime());
//            }
//
//        }
//    }
//

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
//
//    private void populateTestDB() {
//        logger.info("Populating DB with testing data");
//        long startTime = System.nanoTime();
//
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            List<GeoWkt> objJsonList = mapper.readValue(resourceFile.getFile(), new TypeReference<List<GeoWkt>>(){});
//            objJsonList.forEach((n) -> geoWktRepository.save(n));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        long stopTime = System.nanoTime();
//        logger.info("loading time: " + (stopTime - startTime) + " ns" );
//    }
//
//    public static CacheStatisticsMXBean getCacheStatisticsMXBean(final String cacheName) {
//        final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
//        ObjectName name = null;
//        try {
//            name = new ObjectName("*:type=CacheStatistics,*,Cache=" + cacheName);
//        } catch (MalformedObjectNameException ex) {
//            logger.error("Someting wrong with ObjectName {}", ex);
//        }
//        Set<ObjectName> beans = mbeanServer.queryNames(name, null);
//        if (beans.isEmpty()) {
//            logger.debug("Cache Statistics Bean not found");
//            return null;
//        }
//        ObjectName[] objArray = beans.toArray(new ObjectName[beans.size()]);
//        return JMX.newMBeanProxy(mbeanServer, objArray[0], CacheStatisticsMXBean.class);
//    }
//

}