package com.myapp.catching.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "engine_object")
public class GeoWkt implements Serializable {

    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String className;

    @Column(columnDefinition="text")
    private String gwkt;
    public GeoWkt(){}

    public GeoWkt(String className, String gwkt){
        this.className = className;
        this.gwkt = gwkt;
    }

    @Override
    public String toString() {
        return "GeoWkt " +
                " id:" + id +
                " className: " + className;

    }

    public Long getId() {
        return id;
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


    public void setId(Long id) {
        this.id = id;
    }
}
