package com.myapp.catching.service;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.myapp.catching.model.EngineObject;
import com.myapp.catching.model.GeoWkt;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EngineObjectBuilder {

    private final WKTReader jtsWktReader;

    private RTree<Long, Geometry> rTree;

    /**
     *
     */
    public EngineObjectBuilder() {

        this.jtsWktReader = new WKTReader();
    }
    /**
     * @param geomWkt
     * @return com.vividsolutions.jts.geom.Geometry
     * @throws ParseException
     */
    public com.vividsolutions.jts.geom.Geometry getJtsGeom(StringBuffer geomWkt) throws ParseException {
        com.vividsolutions.jts.geom.Geometry jtsGeom = jtsWktReader.read(geomWkt.toString());
        // jtsGeom.normalize();
        return jtsGeom;
    }

    public Rectangle getRectangleFromWkt(GeoWkt geoWkt) throws Exception {
        com.vividsolutions.jts.geom.Geometry geometry = getJtsGeom(new StringBuffer(geoWkt.getGwkt()));
        Rectangle rectangle = null;

        if (geometry != null) {
            rectangle = getRectangle(geometry);
        }

        return rectangle;
    }

    Rectangle getRectangle(com.vividsolutions.jts.geom.Geometry jtsGeom){
        Envelope jtsEnvelope = jtsGeom.getEnvelopeInternal();
        Rectangle rtreeEnv = Geometries.rectangle(jtsEnvelope.getMinX(), jtsEnvelope.getMinY(),
                jtsEnvelope.getMaxX(), jtsEnvelope.getMaxY());
        return rtreeEnv;
    }

    EngineObject buildEngineObject(GeoWkt geoWkt) throws Exception {
        EngineObject engineObject = new EngineObject();
        engineObject.setClassName(geoWkt.getClassName());
        com.vividsolutions.jts.geom.Geometry geometry = getJtsGeom(new StringBuffer(geoWkt.getGwkt()));
        engineObject.setJtsGeom(geometry);
        engineObject.setObjectId(geoWkt.getId());
        return engineObject;
    }

    EngineObject buildEngineObject(Long id, String className, String wkt) throws Exception {
        EngineObject engineObject = new EngineObject();
        engineObject.setClassName(className);
        com.vividsolutions.jts.geom.Geometry geometry = getJtsGeom(new StringBuffer(wkt));
        engineObject.setJtsGeom(geometry);
        engineObject.setObjectId(id);
        return engineObject;
    }

    public RTree<Long, Geometry> buildRTree(List<EngineObject> engineObjectList) {
        int successCount = 0;
        RTree<Long, Geometry> rTree = RTree.create();

        for (EngineObject engineObject : engineObjectList) {
            if (engineObject.getJtsGeom() != null) {
                Envelope jtsEnvelope = engineObject.getJtsGeom().getEnvelopeInternal();
                Rectangle rtreeEnv = Geometries.rectangle(
                        jtsEnvelope.getMinX(), jtsEnvelope.getMinY(),
                        jtsEnvelope.getMaxX(), jtsEnvelope.getMaxY());
                rTree = rTree.add(engineObject.getObjectId(), rtreeEnv);

                successCount++;
            }
        }
        return rTree;
    }

    public RTree<Long, Geometry> initRtee() {
        return RTree.create();
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
}
