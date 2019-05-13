package com.myapp.catching.jdbc;

import com.myapp.catching.model.GeoWkt;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GeoWktRowMapper implements RowMapper<GeoWkt> {
    @Override
    public GeoWkt mapRow(ResultSet rs, int rowNum) throws SQLException {
        GeoWkt geoWkt = new GeoWkt();
        geoWkt.setId(rs.getLong("id"));
        geoWkt.setGwkt(rs.getString("gwkt"));
        geoWkt.setClassName(rs.getString("class_name"));
        return geoWkt;
    }
}