import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import org.apache.commons.jcs.access.CacheAccess;
import org.postgis.PGgeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.management.CacheStatisticsMXBean;
import javax.cache.spi.CachingProvider;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.sql.*;
import java.util.*;


public class PlainApplication {
    private static final Logger logger = LoggerFactory.getLogger(PlainApplication.class);

    static final int maxObjectsLoaded = 1000000;

    static final int geomSrid = 27700;

    static final int numberOfCachesUsed = 4;

    static final String cacheName = "engine-object";

    // eh or jcs
    static final String currentCacheImplementation = "eh";

    private CacheManager cacheManager;

    private CacheAccess<Long, EngineObject> jcscache;

    private RTree<Long, com.github.davidmoten.rtree.geometry.Geometry> rTree;

    private Connection dbConn = null;

    static PostGISGeomToJtsGeomTranslator translator = new PostGISGeomToJtsGeomTranslator();

    public PlainApplication() {
        if (currentCacheImplementation == "eh") {
            initEhCache();
        }
        if (currentCacheImplementation == "jcs") {
            initJcsCache();
        }
    }

    public static void main(String[] args) { new PlainApplication().run(args); }

    public void run(String... args) {
        try (Connection conn = this.connect();)
        {
            this.dbConn = conn;
            buildRTreeWithCache();
            Thread.sleep(10000);
            //hitCache();
            Thread.sleep(10000);
            testQueryByGeometries();
//            engineObjectService.clearCache();
//            engineObjectService.testClashes(maxObjectsLoaded);
            Thread.sleep(10000);
            printStats();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    public void buildRTreeWithCache() throws Exception{
        long startTime, stopTime;
        logger.info("## Build rTree and cache JDBC ...");
        startTime = System.nanoTime();

        findAllForRowSet(maxObjectsLoaded);

        stopTime = System.nanoTime();
        logger.info("## Build rTree and cache JDBC: {} s. Memory KB {} ",
                (stopTime - startTime) * 0.000000001f,
                getReallyUsedMemory() / 1024d );

    }

    public RTree<Long, Geometry> findAllForRowSet(int maxItems) throws Exception {
        String query = "select id, class_name, geom from engine_object order by id desc limit ?";
        rTree = RTree.create();
        try (PreparedStatement pstmt = dbConn.prepareStatement(query)) {


            pstmt.setInt(1, maxItems);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {

                EngineObject eo = buildEngineObject(rs);

                put(eo.getObjectId(), eo);
                rTree = addRteeNode(eo, rTree);
                eo = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rTree;
    }

    public EngineObject buildEngineObject(ResultSet rs) throws Exception {
        EngineObject eo = new EngineObject();
        eo.setClassName(rs.getString("class_name"));
        eo.setObjectId(rs.getLong("id"));


        Object dbGeom = rs.getObject("geom");
        com.vividsolutions.jts.geom.Geometry jtsGeom = null;
        if (dbGeom != null) {
            if (dbGeom instanceof PGgeometry) {
                jtsGeom = translator.translatePostgis2Jts((PGgeometry) dbGeom);
                jtsGeom.setSRID(geomSrid);
            }
        }

        eo.setJtsGeom(jtsGeom);
        return eo;

    }
    public void hitCache() {
        logger.info("## Hitting cache ..." );
        String query = "select id from engine_object order by id desc";

        try (PreparedStatement pstmt = dbConn.prepareStatement(query)) {

            int fetchedFromCache = 0, reads = 0;
            long stopTime = 0, startTime = 0;
            startTime = System.nanoTime();

            Long key;
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                key = rs.getLong("id");
                //if (cache.containsKey(key )) ++fetchedFromCache;
                EngineObject engineObject = get(key);
                if (engineObject != null) {
                    ++fetchedFromCache;
                }
                ++reads;
            }

            stopTime = System.nanoTime();
            logger.info("## Testing Cache, reads: {}, hits: {}, time: {} ",
                    reads,
                    fetchedFromCache,
                    (stopTime - startTime) * 0.000000001f );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void testQueryByGeometries() throws Exception {
        logger.info("## QueryByGeometries ..." );
        String query = "select id, geom from engine_object order by id desc limit 100000";

        try (PreparedStatement pstmt = dbConn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object dbGeom = rs.getObject("geom");

                com.vividsolutions.jts.geom.Geometry jtsGeom = null;
                if (dbGeom != null) {
                    if (dbGeom instanceof PGgeometry) {
                        jtsGeom = translator.translatePostgis2Jts((PGgeometry) dbGeom);
                        jtsGeom.setSRID(geomSrid);
                    }
                }

                List<EngineObject> engineObjects = null;
                if (jtsGeom != null) {
                    engineObjects = queryByGeometry(jtsGeom);
                }

                assert (engineObjects != null && !engineObjects.isEmpty());
            }
            logger.info("## QueryByGeometries done" );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public List<EngineObject> queryByGeometry(com.vividsolutions.jts.geom.Geometry queryFeatureJtsGeom) throws Exception {

        List<EngineObject> queryResults = new ArrayList<EngineObject>();

        PreparedGeometry preparedGeom = PreparedGeometryFactory.prepare(queryFeatureJtsGeom);

        Iterable<Entry<Long, com.github.davidmoten.rtree.geometry.Geometry>> it = null;

        if (queryFeatureJtsGeom instanceof Point) {
            Point jtsPoint = (Point) queryFeatureJtsGeom;
            com.github.davidmoten.rtree.geometry.Point queryRTreePoint = Geometries.point(jtsPoint.getX(),
                    jtsPoint.getY());
            it = rTree.search(queryRTreePoint).toBlocking().toIterable();
        } else {
            Envelope jtsEnv = queryFeatureJtsGeom.getEnvelopeInternal();
            Rectangle queryRTreeEnv = Geometries.rectangle(jtsEnv.getMinX(), jtsEnv.getMinY(), jtsEnv.getMaxX(),
                    jtsEnv.getMaxY());
            it = rTree.search(queryRTreeEnv).toBlocking().toIterable();
        }

        return getIntersectedEngineObjectsFromCache(queryResults, preparedGeom, it);
    }

    private List<EngineObject> getIntersectedEngineObjectsFromCache(List<EngineObject> queryResults, PreparedGeometry preparedGeom, Iterable<Entry<Long, Geometry>> it) throws Exception {
        for (Entry<Long, Geometry> entry : it) {
            Long engineObjectId = entry.value();
            EngineObject engineObject = get(engineObjectId);
            if (engineObject !=null && preparedGeom.intersects(engineObject.getJtsGeom())) {
                queryResults.add(engineObject);
            }
        }
        return queryResults;
    }

    private List<EngineObject> getIntersectedEngineObjectsFromDB(List<EngineObject> queryResults, PreparedGeometry preparedGeom, Iterable<Entry<Long, Geometry>> it) throws Exception {
        String query = "select id, class_name, geom from engine_object where id = ?";
        EngineObject eo = null;

        PostGISGeomToJtsGeomTranslator translator = new PostGISGeomToJtsGeomTranslator();

        try (PreparedStatement pstmt = dbConn.prepareStatement(query)) {

            for (Entry<Long, Geometry> entry : it) {
                Long engineObjectId = entry.value();
                pstmt.setLong(1, engineObjectId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    eo = buildEngineObject(rs);
                }

                if (eo != null && preparedGeom.intersects(eo.getJtsGeom())) {
                    queryResults.add(eo);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return queryResults;
    }


    private RTree<Long, Geometry> addRteeNode(EngineObject engineObject, RTree<Long, Geometry> rTree) {
        if (engineObject.getJtsGeom() != null) {
            Envelope jtsEnvelope = engineObject.getJtsGeom().getEnvelopeInternal();
            Rectangle rtreeEnv = Geometries.rectangle(
                    jtsEnvelope.getMinX(), jtsEnvelope.getMinY(),
                    jtsEnvelope.getMaxX(), jtsEnvelope.getMaxY());
            rTree = rTree.add(engineObject.getObjectId(), rtreeEnv);
        }
        return rTree;
    }


    private Connection connect() {
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

    private void initEhCache() {
        try {
//            PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
//                    .with(CacheManagerBuilder.persistence(new File(getStoragePath(), "myData")))
//                    .withCache("engine-object",
//                            CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
//                                    ResourcePoolsBuilder.newResourcePoolsBuilder()
//                                            .heap(10, EntryUnit.ENTRIES)
//                                            .offheap(1, MemoryUnit.MB)
//                                            .disk(20, MemoryUnit.MB, true)
//                            )
//                    ).build(true);
            CachingProvider cachingProvider = Caching.getCachingProvider();
            cacheManager = cachingProvider.getCacheManager(
                    getClass().getClassLoader().getResource("ehcache.xml").toURI(),
                    getClass().getClassLoader());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void initJcsCache() {
        jcscache = org.apache.commons.jcs.JCS.getInstance("engine-object");
        //JCS.getInstance("engine-object");
//        try (InputStream input = getClass().getClassLoader().getResourceAsStream("jcscache.properties")) {
//
//            Properties prop = new Properties();
//
//            if (input == null) {
//                logger.error("Sorry, unable to find config.properties");
//                return;
//            }
//
//            //load a properties file from class path, inside static method
//            prop.load(input);
//
//            CompositeCacheManager ccm = CompositeCacheManager.getUnconfiguredInstance();
//            ccm.configure(prop);
//            //get the property value and print it out
//            System.out.println(prop.getProperty("db.url"));
//            System.out.println(prop.getProperty("db.user"));
//            System.out.println(prop.getProperty("db.password"));
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }

    }

    private void put(Long id, EngineObject eo) {
        if (currentCacheImplementation == "eh") {
            int cacheNumber = id.intValue() % numberOfCachesUsed + 1;
            cacheManager.getCache(cacheName + cacheNumber).put(id,eo);
        }
        if (currentCacheImplementation == "jcs") {
            jcscache.put(id,eo);
        }

    }

    private EngineObject get(Long id) {
        if (currentCacheImplementation == "eh") {
            int cacheNumber = id.intValue() % numberOfCachesUsed + 1;

            return (EngineObject)cacheManager.getCache(cacheName+cacheNumber).get(id);
        }
        if (currentCacheImplementation == "jcs") {
            return jcscache.get(id);
        }
        return null;
    }

    private long getCurrentlyUsedMemory() {
        return
                ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() +
                        ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }
    private long getGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) { sum +=  count; }
        }
        return sum;
    }
    private long getReallyUsedMemory() {
        long before = getGcCount();
        System.gc();
        while (getGcCount() == before);
        return getCurrentlyUsedMemory();
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
