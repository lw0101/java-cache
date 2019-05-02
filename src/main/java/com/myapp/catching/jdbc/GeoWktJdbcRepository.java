package com.myapp.catching.jdbc;

import com.myapp.catching.model.GeoWkt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GeoWktJdbcRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public int getTotalCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM engine_object", Integer.class);
    }


    public List<GeoWkt> findAll(int maxItems) {
        String query = "select * from engine_object limit ?";
        List<GeoWkt> geoWkts = jdbcTemplate.query(
                query, new Object[] { maxItems }, new GeoWktRowMapper());
        return geoWkts;
    }

    public SqlRowSet findAllForRowSet(int maxItems) {
        String query = "select id, class_name, gwkt from engine_object limit ?";
        return jdbcTemplate.queryForRowSet(query, new Object[] { maxItems });
    }

    public GeoWkt findById(Long id) {
        String query = "select * from engine_object where id = ?";
        GeoWkt geoWkt = (GeoWkt)jdbcTemplate.queryForObject(
                query, new Object[] { id },
                new GeoWktRowMapper());
        return geoWkt;
    }
}
