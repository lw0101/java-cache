package com.myapp.catching.repository;

import com.myapp.catching.model.GeoWkt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeoWktRepository extends JpaRepository<GeoWkt, Long> {

    List<GeoWkt> findByClassName(String className);
}
