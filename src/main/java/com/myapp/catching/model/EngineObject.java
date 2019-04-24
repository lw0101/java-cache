package com.myapp.catching.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class EngineObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigDecimal id;

    private String className;

    @Column(columnDefinition="text")
    private String gwkt;

    public EngineObject(){}

    public EngineObject(String className, String gwkt){
        this.className = className;
        this.gwkt = gwkt;
    }

    @Override
    public String toString() {
        return "EngineObject " +
                " id:" + id +
                " className: " + className;

    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getGwkt() {
        return gwkt;
    }

    public void setGwkt(String gwkt) {
        this.gwkt = gwkt;
    }
}
