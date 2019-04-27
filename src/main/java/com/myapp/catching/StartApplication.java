package com.myapp.catching;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.catching.model.EngineObject;
import com.myapp.catching.repository.EngineObjectRepository;
import com.myapp.catching.service.EngineObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;

import javax.cache.CacheManager;
import javax.cache.management.CacheStatisticsMXBean;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;


@SpringBootApplication
public class StartApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartApplication.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EngineObjectRepository engineObjectRepository;

    @Autowired
    private EngineObjectService engineObjectService;

    @Value("classpath:test-data.json")
    private Resource resourceFile;

    public static void main(String[] args) {
        SpringApplication.run(StartApplication.class, args);
    }

    @Override
    public void run(String... args) {

        logger.info("StartApplication Caching Example ...");
        logger.info("using cache manager: " + cacheManager.getClass().getName());

//        populateTestDB();

//        engineObjectService.clearCache();

        long startTime, stopTime;
        startTime = System.nanoTime();
        List<EngineObject> engineObjects = engineObjectService.findAll();
        stopTime = System.nanoTime();
        logger.info("\nfindAll from DB(): " + (stopTime - startTime) + " ns" );

        startTime = System.nanoTime();
        // engineObjectService.findAll();
        stopTime = System.nanoTime();
        logger.info("\nfindAll from cache(): " + (stopTime - startTime) + " ns" );

        Long count = 100L;

        for (EngineObject eo : engineObjects) {
            engineObjectService.findById(eo.getId());
            --count;
            if (count == 0L) break;
        }


        count = 50L;
        startTime = System.nanoTime();
        for (EngineObject eo : engineObjects) {
            engineObjectService.findById(eo.getId());
            --count;
            if (count == 0L) break;
        }
        stopTime = System.nanoTime();
        logger.info("\nloop: " + (stopTime - startTime) + " ns" );

        printStats();
//        System.out.println("\nfindById(1L)");
//        repository.findById(1l).ifPresent(x -> System.out.println(x));
//
//        System.out.println("\nfindByName('Node')");
//        repository.findByName("Node").forEach(x -> System.out.println(x));
        cacheManager.close();
    }

    private void printStats() {
        for (String name : cacheManager.getCacheNames()) {
            CacheStatisticsMXBean CacheStatBean = getCacheStatisticsMXBean(name);
            if (CacheStatBean != null) {
                logger.info("Cache hits #{} misses #{}", CacheStatBean.getCacheHits(), CacheStatBean.getCacheMisses());
                logger.info("Cache hits %{} misses %{}", CacheStatBean.getCacheHitPercentage(),
                        CacheStatBean.getCacheMissPercentage());
                logger.info("Cache gets #{}", CacheStatBean.getCacheGets());
                logger.info("Cache evictions #{}", CacheStatBean.getCacheEvictions());
                logger.info("Cache average get time {} milliseconds", CacheStatBean.getAverageGetTime());
            }

        }
    }


    private void populateTestDB() {
        logger.info("Populating DB with testing data");
        long startTime = System.nanoTime();

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<EngineObject> objJsonList = mapper.readValue(resourceFile.getFile(), new TypeReference<List<EngineObject>>(){});
            objJsonList.forEach((n) -> engineObjectRepository.save(n));

        } catch (Exception e) {
            e.printStackTrace();
        }
        long stopTime = System.nanoTime();
        logger.info("loading time: " + (stopTime - startTime) + " ns" );
    }

    public static CacheStatisticsMXBean getCacheStatisticsMXBean(final String cacheName) {
        final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = null;
        try {
            name = new ObjectName("*:type=CacheStatistics,*,Cache=" + cacheName);
        } catch (MalformedObjectNameException ex) {
            logger.error("Someting wrong with ObjectName {}", ex);
        }
        Set<ObjectName> beans = mbeanServer.queryNames(name, null);
        if (beans.isEmpty()) {
            logger.debug("Cache Statistics Bean not found");
            return null;
        }
        ObjectName[] objArray = beans.toArray(new ObjectName[beans.size()]);
        return JMX.newMBeanProxy(mbeanServer, objArray[0], CacheStatisticsMXBean.class);
    }
}