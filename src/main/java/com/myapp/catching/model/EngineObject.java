package com.myapp.catching.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EngineObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String className;
	private long objectId;
	private com.vividsolutions.jts.geom.Geometry jtsGeom;
	private Map<String, Object> attributes;

	/**
	*
	*/
	public EngineObject() {
		//
	}

	/**
	 * @param className
	 * @param objectId
	 * @param jtsGeom
	 * @param attributes
	 */
	public EngineObject(String className, long objectId, com.vividsolutions.jts.geom.Geometry jtsGeom,
                        Map<String, Object> attributes) {
		this.className = className;
		this.objectId = objectId;
		this.jtsGeom = jtsGeom;
		this.attributes = attributes;
	}

	/**
	 * @return className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return objectId
	 */
	public long getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId
	 */
	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return jtsGeom
	 */
	public com.vividsolutions.jts.geom.Geometry getJtsGeom() {
		return jtsGeom;
	}

	/**
	 * @param jtsGeom
	 */
	public void setJtsGeom(com.vividsolutions.jts.geom.Geometry jtsGeom) {
		this.jtsGeom = jtsGeom;
	}

	/**
	 * @return attributes
	 */
	public Map<String, Object> getAttributes() {
		if (attributes == null) {
			attributes = new HashMap<String, Object>();
		}
		return attributes;
	}

	/**
	 * @param attributes
	 */
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @param propName
	 * @param value
	 */
	public void modifyObject(String propName, Object value) {
		//
	}

	@Override
	public final boolean equals(Object paramObject) {
		if (paramObject instanceof EngineObject && equals((EngineObject) paramObject)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean equals(EngineObject engineObject) {
		return className.equals(engineObject.getClassName()) && getObjectId() == engineObject.getObjectId();
	}

	/**
	 * @param attrName
	 * @return int
	 */
	public int getValueAsInt(String attrName) {
		Object value = attributes.get(attrName);
		if (value != null) {
			return (int) value;
		}
		return 0;
	}

	/**
	 * @param attrName
	 * @return long
	 */
	public long getValueAsLong(String attrName) {
		Object value = attributes.get(attrName);
		if (value != null) {
			return (long) value;
		}
		return 0;
	}

	/**
	 * @param attrName
	 * @return double
	 */
	public double getValueAsDouble(String attrName) {
		Object value = attributes.get(attrName);
		if (value != null) {
			return (double) value;
		}
		return 0.0;
	}

	/**
	 * @param attrName
	 * @return String
	 */
	public String getValueAsString(String attrName) {
		Object value = attributes.get(attrName);
		if (value != null) {
			String strValue = (String) value;
			return strValue.trim();
		}
		return null;
	}

}
