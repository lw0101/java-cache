package com.myapp.catching;

import com.myapp.catching.model.EngineObject;
import com.myapp.catching.repository.EngineObjectRepository;
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
public class EngineObjectTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EngineObjectRepository repository;

    @Value("classpath:json/test.json")
    private Resource resourceFile;

    @Test
    public void testFindByClassname() {

        entityManager.persist(new EngineObject("class1", "gwkt1"));

        List<EngineObject> engineObjects = repository.findByClassName("class1");
        assertEquals(1, engineObjects.size());

        assertThat(engineObjects).extracting(EngineObject::getClassName).containsOnly("class1");

    }

}