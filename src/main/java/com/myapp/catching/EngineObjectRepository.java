package com.myapp.catching;

import com.myapp.catching.model.EngineObject;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.List;

public interface EngineObjectRepository extends CrudRepository <EngineObject, BigDecimal> {

    List<EngineObject> findByClassName(String className);
}
