/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.dao.metadata;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.UnhandledException;
import org.beangle.collection.CollectUtils;
import org.beangle.dao.Entity;
import org.testng.internal.PropertyUtils;

public class EntityType extends AbstractType {

	private String entityName;

	private Class<?> entityClass;

	private Map<String, Type> propertyTypes = CollectUtils.newHashMap();

	private String idPropertyName;

	public EntityType() {
		super();
	}

	public EntityType(String entityName, Class<?> entityClass) {
		super();
		this.entityName = entityName;
		this.entityClass = entityClass;
	}

	public EntityType(Class<?> entityClass) {
		super();
		this.entityClass = entityClass;
		this.entityName = entityClass.getName();
	}

	public boolean isEntityType() {
		return true;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public Map<String, Type> getPropertyTypes() {
		return propertyTypes;
	}

	public void setPropertyTypes(Map<String, Type> propertyTypes) {
		this.propertyTypes = propertyTypes;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * Get the type of a particular (named) property
	 */
	public Type getPropertyType(String property) {
		Type type = (Type) propertyTypes.get(property);
		if (null == type) {
			Class<?> propertyType = ReflectHelper.getProperty(entityClass, property);
			if (null != propertyType) {
				if (Entity.class.isAssignableFrom(propertyType)) {
					type = new EntityType(propertyType);
				}
				if (propertyType.isInterface()) {
					type = Model.getEntityType(propertyType.getName());
				}
				if (null == type) {
					type = new IdentifierType(propertyType);
				}
			}
		}
		if (null == type) {
			logger.error("{} doesn't contains property {}", entityName, property);
		}
		return type;
	}

	/**
	 * The name of the entity
	 */
	public String getEntityName() {
		return entityName;
	}

	public String getName() {
		return entityName;
	}

	public Class<?> getReturnedClass() {
		return entityClass;
	}

	public String getIdPropertyName() {
		return idPropertyName;
	}

	public void setIdPropertyName(String idName) {
		this.idPropertyName = idName;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Serializable> getIdClass() {
		if (null == this.idPropertyName) return null;
		else try {
			return (Class<? extends Serializable>) PropertyUtils.getPropertyType(entityClass, idPropertyName);
		} catch (Exception e) {
			throw new UnhandledException(e);
		}
	}
}
