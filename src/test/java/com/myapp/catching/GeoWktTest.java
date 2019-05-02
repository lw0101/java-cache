package com.myapp.catching;

import com.myapp.catching.model.GeoWkt;
import com.myapp.catching.repository.GeoWktRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class GeoWktTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GeoWktRepository repository;

    @Value("classpath:json/test.json")
    private Resource resourceFile;

    @Test
    public void testFindByClassname() {

        entityManager.persist(new GeoWkt("class1", "gwkt1"));

        List<GeoWkt> geoWkts = repository.findByClassName("class1");
        assertEquals(1, geoWkts.size());

        assertThat(geoWkts).extracting(GeoWkt::getClassName).containsOnly("class1");

    }

}