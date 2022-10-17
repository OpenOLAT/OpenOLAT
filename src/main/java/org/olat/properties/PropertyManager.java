/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.properties;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.user.UserDataDeletable;

/**
 * Initial Date:  Mar 10, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class PropertyManager implements UserDataDeletable {

	private static final Logger log = Tracing.createLoggerFor(PropertyManager.class);
	private static PropertyManager INSTANCE;

	/**
	 * [used by spring]
	 */
	private PropertyManager() {
		INSTANCE = this;
	}

	/**
	 * @return Singleton.
	 */
	public static PropertyManager getInstance() { return INSTANCE; }

	/**
	 * Creates a new Property
	 * @param identity
	 * @param group
	 * @param olatResourceable
	 * @param category
	 * @param name
	 * @param floatValue
	 * @param longValue
	 * @param stringValue
	 * @param textValue
	 * @return property instance.
	 */
	public Property createPropertyInstance(Identity identity, BusinessGroup group, OLATResourceable olatResourceable, 
		String category, String name,	Float floatValue, Long longValue, String stringValue, String textValue) {
		
		Property p = new Property();
		p.setIdentity(identity);
		p.setGrp(group);
		if (olatResourceable != null) {
			p.setResourceTypeName(olatResourceable.getResourceableTypeName());
			p.setResourceTypeId(olatResourceable.getResourceableId());
		}
		p.setCategory(category);
		p.setName(name);
		p.setFloatValue(floatValue);
		p.setLongValue(longValue);
		p.setStringValue(stringValue);
		p.setTextValue(textValue);
		return p;
	}
	
	/**
	 * Create a user proprety. Grp, course and node a re set to null.
	 * 
	 * @param identity
	 * @param category
	 * @param name
	 * @param floatValue
	 * @param longValue
	 * @param stringValue
	 * @param textValue
	 * @return property instance limited to a specific user.
	 */
	public Property createUserPropertyInstance(Identity identity, String category, String name,
		Float floatValue, Long longValue, String stringValue, String textValue) {
		return createPropertyInstance(identity, null, null, category, name, floatValue, longValue, stringValue, textValue);
	}
	
	/**
	 * Deletes a property on the database
	 * @param p the property
	 */
	public void deleteProperty(Property p) {
		DBFactory.getInstance().deleteObject(p);
	}
	
	/**
	 * Save a property
	 * @param p
	 */
	public void saveProperty(Property p) {
		p.setLastModified(new Date());
		DBFactory.getInstance().saveObject(p);
	}

	/**
	 * Update a property
	 * @param p
	 */
	public void updateProperty(Property p) {
		p.setLastModified(new Date());
		DBFactory.getInstance().updateObject(p);
	}

	/**
	 * Find a user property.
	 * 
	 * @param identity
	 * @param category
	 * @param name
	 * @return Found property or null if no match.
	 */
	public Property findUserProperty(Identity identity, String category, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(Property.class.getName()).append(" as v ")
		  .append(" inner join fetch v.identity identity ")
		  .append(" where identity.key=:identityKey and v.category=:cat and v.name=:name and v.grp is null and v.resourceTypeName is null and v.resourceTypeId is null");
		
		List<Property> props = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("cat", category)
				.setParameter("name", name)
				.getResultList();

		if (props == null || props.size() != 1) {
			if(log.isDebugEnabled()) log.debug("Could not find property: " + name);
			return null;
		}
		return props.get(0);
	}
	
	/**
	 * Return all the properties with the specified name and category
	 * 
	 * @param identity
	 * @param category
	 * @param name
	 * @return
	 */
	public List<Property> findAllUserProperties(IdentityRef identity, String category, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(Property.class.getName()).append(" as v ")
		  .append(" inner join v.identity identity ")
		  .append(" where identity.key=:identityKey and v.category=:cat and v.name=:name");
		
		return DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("cat", category)
				.setParameter("name", name)
				.getResultList();
	}

	
	/**
	 * Generic method. Returns a list of Property objects. This is an inexact match i.e. parameters with null values
	 * will not be included in the query.
	 * 
	 * @param identity
	 * @param grp
	 * @param resourceable
	 * @param category
	 * @param name
	 * @return a list of Property objects
	 */
	public List<Property> listProperties(Identity identity, BusinessGroup grp, OLATResourceable resourceable, String category, String name) {
		if (resourceable == null) {
			return listProperties(identity, grp, null, null, category, name);
		} else {
			return listProperties(identity, grp, resourceable.getResourceableTypeName(), resourceable.getResourceableId(), category, name);
		}
	}
	
	public int countProperties(Identity identity, BusinessGroup grp, OLATResourceable resourceable, String category, String name) {
		if (resourceable == null) {
			return countProperties(identity, grp, null, null, category, name, null, null);
		} else {
			return countProperties(identity, grp, resourceable.getResourceableTypeName(), resourceable.getResourceableId(), category, name, null, null);
		}
	}
	
	/**
	 * Only to use if no OLATResourceable Object is available.
	 * @param identity
	 * @param grp
	 * @param resourceTypeName
	 * @param resourceTypeId
	 * @param category
	 * @param name
	 * @return a list of Property objects
	 */
	public List<Property> listProperties(Identity identity, BusinessGroup grp, String resourceTypeName, Long resourceTypeId, String category, String name) {
		return listProperties(identity, grp, resourceTypeName, resourceTypeId, category, name, null, null);
	}

	public int countProperties(Identity identity, BusinessGroup grp, String resourceTypeName, Long resourceTypeId,
			String category, String name, Long longValue, String stringValue) {
		TypedQuery<Number> query = createQueryListProperties(identity, grp, resourceTypeName, resourceTypeId,
				category, name, longValue, stringValue, Number.class);
		return query.getSingleResult().intValue();
	}

	/**
	 * Only to use if no OLATResourceable Object is available.
	 * @param identity
	 * @param grp
	 * @param resourceTypeName
	 * @param resourceTypeId
	 * @param category
	 * @param name
	 * @param longValue
	 * @param stringValue
	 * @return a list of Property objects
	 */
	public List<Property> listProperties(Identity identity, BusinessGroup grp, String resourceTypeName, Long resourceTypeId,
			String category, String name, Long longValue, String stringValue) {
		TypedQuery<Property> query = createQueryListProperties(identity, grp, resourceTypeName, resourceTypeId,
				category, name, longValue, stringValue, Property.class);
		return query.getResultList();
	}
	
	/**
	 * 
	 * @param identity
	 * @param grp
	 * @param resourceTypeName
	 * @param resourceTypeId
	 * @param category
	 * @param name
	 * @param longValue
	 * @param stringValue
	 * @param resultClass Only Number and Property are acceptable
	 * @return
	 */
	private <U> TypedQuery<U> createQueryListProperties(Identity identity, BusinessGroup grp, String resourceTypeName, Long resourceTypeId,
			String category, String name, Long longValue, String stringValue, Class<U> resultClass) {
		
		StringBuilder sb = new StringBuilder();
		if(Number.class.equals(resultClass)) {
			sb.append("select count(v) from ").append(Property.class.getName()).append(" as v ");
			if (identity != null) {
				sb.append(" inner join v.identity identity ");
			}
			if (grp != null) {
				sb.append(" inner join v.grp grp ");
			}
		} else {
			sb.append("select v from ").append(Property.class.getName()).append(" as v ");
			if (identity != null) {
				sb.append(" inner join fetch v.identity identity ");
			}
			if (grp != null) {
				sb.append(" inner join fetch v.grp grp ");
			}
		}
		sb.append(" where ");

		boolean and = false;
		if (identity != null) {
			and = and(sb, and);
			sb.append("identity.key=:identityKey");
		}
		if (grp != null) {
			and = and(sb, and);
			sb.append("grp.key=:groupKey");
		}
		if (resourceTypeName != null) {
			and = and(sb, and);
			sb.append("v.resourceTypeName=:resName");
		}
		if (resourceTypeId != null) {
			and = and(sb, and);
			sb.append(" v.resourceTypeId=:resId");
		}
		if (category != null) {
			and = and(sb, and);
			sb.append("v.category=:cat");
		}
		if (name != null) {
			and = and(sb, and);
			sb.append("v.name=:name");
		}
		if (longValue != null) {
			and = and(sb, and);
			sb.append("v.longValue=:long");			
		}
		if (stringValue != null) {
			and = and(sb, and);
			sb.append("v.stringValue=:string");			
		}
		
		TypedQuery<U> queryProps = DBFactory.getInstance().getCurrentEntityManager().createQuery(sb.toString(), resultClass);
		if (identity != null) {
			queryProps.setParameter("identityKey", identity.getKey());
		}
		if (grp != null) {
			queryProps.setParameter("groupKey", grp.getKey());
		}
		if (resourceTypeName != null) {
			queryProps.setParameter("resName", resourceTypeName);
		}
		if (resourceTypeId != null) {
			queryProps.setParameter("resId", resourceTypeId);
		}	
		if (category != null) {
			queryProps.setParameter("cat", category);
		}
		if (name != null) {
			queryProps.setParameter("name", name);
		}
		if (longValue != null) {
			queryProps.setParameter("long", longValue);
		}
		if (stringValue != null) {
			queryProps.setParameter("string", stringValue);
		}
		return queryProps;
	}
	
	/**
	 * deletes properties. IMPORTANT: if an argument is null, then it will be not considered in the delete statement, which means not only the record having a "null" value will be deleted, but all. 
	 * @param identity
	 * @param grp
	 * @param resourceable
	 * @param category
	 * @param name
	 */
	public int deleteProperties(Identity identity, BusinessGroup grp, OLATResourceable resourceable, String category, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(Property.class.getName()).append(" as v where ");

		boolean and = false;
		if (identity != null) {
			and = and(sb, and);
			sb.append("identity.key=:identityKey");
		}
		if (grp != null) {
			and = and(sb, and);
			sb.append("v.grp.key=:groupKey");
		}
		if (resourceable != null) {
			and = and(sb, and);
			sb.append("v.resourceTypeName=:resName and v.resourceTypeId");
			if (resourceable.getResourceableId() == null) {
				sb.append(" is null");
			} else {
				sb.append(" =:resId");
			}
		}
		if (category != null) {
			and = and(sb, and);
			sb.append("v.category=:cat");
		}
		if (name != null) {
			and = and(sb, and);
			sb.append("v.name=:name");
		}
		
		Query queryProps = DBFactory.getInstance().getCurrentEntityManager().createQuery(sb.toString());
		if (identity != null) {
			queryProps.setParameter("identityKey", identity.getKey());
		}
		if (grp != null) {
			queryProps.setParameter("groupKey", grp.getKey());
		}
		if (resourceable != null) {
			queryProps.setParameter("resName", resourceable.getResourceableTypeName());
			if (resourceable.getResourceableId() != null) {
				queryProps.setParameter("resId", resourceable.getResourceableId());
			}
		}
		if (category != null) {
			queryProps.setParameter("cat", category);
		}
		if (name != null) {
			queryProps.setParameter("name", name);
		}
		
		return queryProps.executeUpdate();
	}

	
	/**
	 * Generic find method. Returns a list of Property objects. This is an exact match i.e. if you pass-on null values,
	 * null values will be included in the query.
	 * 
	 * @param identity
	 * @param grp
	 * @param resourceable
	 * @param category
	 * @param name
	 * @return a list of Property objects.
	 */
	public List<Property> findProperties(Identity identity, BusinessGroup grp, OLATResourceable resourceable, String category, String name) {
		if (resourceable == null) 
			return findProperties(identity, grp, null, null, category, name);
		else
			return findProperties(identity, grp, resourceable.getResourceableTypeName(), resourceable.getResourceableId(), category, name);
	}

	/**
	 * Only to use if no OLATResourceable Object is available.
	 * @param identity
	 * @param grp
	 * @param resourceTypeName
	 * @param resourceTypeId
	 * @param category
	 * @param name
	 * @return List of properties
	 */
	public List<Property> findProperties(Identity identity, BusinessGroup grp, String resourceTypeName, Long resourceTypeId, String category, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(Property.class.getName()).append(" as v ");
		  
		if (identity != null) {
			sb.append(" inner join fetch v.identity identity where identity.key=:identityKey");
		} else {
			sb.append(" where v.identity is null");
		}

		sb.append(" and v.grp ");
		if (grp != null) sb.append(".key=:groupKey");
		else sb.append(" is null");
		
		sb.append(" and v.resourceTypeName ");
		if (resourceTypeName != null) sb.append("=:resName");
		else sb.append(" is null");

		sb.append(" and v.resourceTypeId");
		if (resourceTypeId != null) sb.append("=:resId");
		else sb.append(" is null");
		
		sb.append(" and v.category");
		if (category != null) sb.append("=:cat");
		else sb.append(" is null");
		
		sb.append(" and v.name");
		if (name != null) sb.append("=:name");
		else sb.append(" is null");
		
		TypedQuery<Property> queryProps = DBFactory.getInstance().getCurrentEntityManager().createQuery(sb.toString(), Property.class);
		if (identity != null) {
			queryProps.setParameter("identityKey", identity.getKey());
		}
		if (grp != null) {
			queryProps.setParameter("groupKey", grp.getKey());
		}
		if (resourceTypeName != null) {
			queryProps.setParameter("resName", resourceTypeName);
		}
		if (resourceTypeId != null) {
			queryProps.setParameter("resId", resourceTypeId);
		}
		if (category != null) {
			queryProps.setParameter("cat", category);
		}
		if (name != null) {
			queryProps.setParameter("name", name);
		} 
		return queryProps.getResultList();
	}

	/**
	 * Get a list of identities that have properties given the restricting values
	 * @param resourceable Search restricted to this resourcable
	 * @param category Search restricted to this property category
	 * @param name Search restricted to this property name
	 * @param matchNullValues true: null values in the above restricting values will be 
	 * added as null values to the query; false: null values in the restricting values will 
	 * be ignored in the query
	 * @return List of identities
	 */
	public List<Identity> findIdentitiesWithProperty(OLATResourceable resourceable, String category, String name, boolean matchNullValues) {
		if (resourceable == null) 
			return findIdentitiesWithProperty(null, null, category, name, matchNullValues);
		else
			return findIdentitiesWithProperty(resourceable.getResourceableTypeName(), resourceable.getResourceableId(), category, name, matchNullValues);
	}

	/**
	 * Get a list of identities that have properties given the restricting values
	 * @param resourceTypeName Search restricted to this resource type name
	 * @param resourceTypeId Search restricted to this resource type id
	 * @param category Search restricted to this property category
	 * @param name Search restricted to this property name
	 * @param matchNullValues true: null values in the above restricting values will be 
	 * added as null values to the query; false: null values in the restricting values will 
	 * be ignored in the query
	 * @return List of identities
	 */
	public List<Identity> findIdentitiesWithProperty(String resourceTypeName, Long resourceTypeId, String category, String name, boolean matchNullValues) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct i from ").append(Property.class.getName()).append(" as p")
		  .append(" inner join p.identity i");
		
		boolean and = false;
		if (resourceTypeName != null) {
			and = appendAnd(sb, "p.resourceTypeName=:resName", and);
		} else if (matchNullValues) {
			and = appendAnd(sb, "p.resourceTypeName is null", and);
		}
	
		if (resourceTypeId != null) {
			and = appendAnd(sb, "p.resourceTypeId =:resId", and);
		} else if (matchNullValues) {
			and = appendAnd(sb, "p.resourceTypeId is null", and);
		}

		if (category != null) {
			and = appendAnd(sb, "p.category=:cat", and);
		} else if (matchNullValues) {
			and = appendAnd(sb, "p.category is null", and);
		}

		if (name != null) {
			and = appendAnd(sb, "p.name=:name", and);
		} else if (matchNullValues) {
			and = appendAnd(sb, "p.name is null", and);
		}
		
		TypedQuery<Identity> queryIdentities = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class);
		if (resourceTypeName != null) {
			queryIdentities.setParameter("resName", resourceTypeName);
		}
		if (resourceTypeId != null) {
			queryIdentities.setParameter("resId", resourceTypeId);
		}
		if (category != null) {
			queryIdentities.setParameter("cat", category);
		}
		if (name != null) {
			queryIdentities.setParameter("name", name);
		}
		return queryIdentities.getResultList();
	}
	
	/**
	 * Generic find method.
	 * 
	 * @param identity
	 * @param grp
	 * @param resourceable
	 * @param category
	 * @param name
	 * @return Property if found or null
	 * @throws AssertException if more than one match found
	 */
	public Property findProperty(Identity identity, BusinessGroup grp, OLATResourceable resourceable, String category, String name) {
		
		List<Property> props = findProperties(identity, grp, resourceable, category, name);
		if (props == null || props.size() == 0) {
			if(log.isDebugEnabled()) log.debug("Could not find property: " + name);
			return null;
		}
		else if (props.size() > 1) {
		    throw new AssertException("findProperty found more than one properties for identity::" + identity
		            + ", group::" + grp + ", resourceable::" + resourceable + ", category::" + category 
		            + ", name::" + name);
		}
		return props.get(0);
	}
	
	public void appendTextProperty(Identity identity, BusinessGroup grp,
			OLATResourceable resourceable, String category, String name, String textValue) {
		if(DBFactory.getInstance().isMySQL()) {
			synchronized(this) {// without produce dead lock in MySQL
				executeAppendTextProperty(identity, grp, resourceable,  category, name, textValue);
			}
		} else {
			executeAppendTextProperty(identity, grp, resourceable,  category, name, textValue);
		}
	}
	
	private void executeAppendTextProperty(Identity identity, BusinessGroup grp,
			OLATResourceable resourceable, String category, String name, String textValue) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("update ").append(Property.class.getName()).append(" v ")
		  .append(" set v.textValue=concat(v.textValue,:text), lastModified=:now");
		  
		if (identity != null) {
			sb.and().append(" v.identity.key=:identityKey");
		} else if(grp != null) {
			sb.and().append(" v.grp.key=:groupKey");
		}

		sb
		  .and().append("v.resourceTypeName=:resName")
		  .and().append("v.resourceTypeId=:resId")
		  .and().append("v.category=:cat")
		  .and().append("v.name=:name");
		
		Query query = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("resName", resourceable.getResourceableTypeName())
				.setParameter("resId", resourceable.getResourceableId())
				.setParameter("cat", category)
				.setParameter("name", name)
				.setParameter("text", textValue)
				.setParameter("now", new Date());
		if (identity != null) {
			query.setParameter("identityKey", identity.getKey());
		} else if(grp != null) {
			query.setParameter("groupKey", grp.getKey());
		}

		int row = query
				.executeUpdate();
		DBFactory.getInstance().commit();
		if(row == 0) {
			Property prop = createPropertyInstance(identity, grp, resourceable, category, name, null, null, null, textValue);
			saveProperty(prop);
			DBFactory.getInstance().commit();
		}
	}
	
	/**
	 * The query is an exact match where null value are NOT allowed.
	 * @param businessGroup
	 * @param resourceable
	 * @param category
	 * @param name
	 * @return
	 */
	public Property findProperty(BusinessGroupRef businessGroup, OLATResourceable resourceable, String category, String name) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select p from ").append(Property.class.getName()).append(" as p")
		  .append(" where p.category=:category and p.name=:name")
		  .append(" and p.grp.key=:groupKey")
		  .append(" and p.resourceTypeName=:resourceTypeName and p.resourceTypeId=:resourceableId");

		List<Property> properties = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setParameter("groupKey", businessGroup.getKey())
				.setParameter("resourceTypeName", resourceable.getResourceableTypeName())
				.setParameter("resourceableId", resourceable.getResourceableId())
				.setParameter("category", category)
				.setParameter("name", name)
				.getResultList();
		
		return properties.isEmpty() ? null : properties.get(0);
	}
	
	/**
	 * 
	 * @param identities
	 * @param resourceable
	 * @param category
	 * @param name
	 * @return
	 */
	public List<Property> findProperties(List<Identity> identities, OLATResourceable resourceable, String category, String name) {
		if(identities == null || identities.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder query = new StringBuilder();
		query.append("select p from ").append(Property.class.getName()).append(" as p")
			.append(" inner join fetch p.identity identity ")
			.append(" where identity in (:identities)");
		if (resourceable != null) {
			query.append(" and p.resourceTypeName=:resourceTypeName and p.resourceTypeId=:resourceableId");
		}
		if (category != null) {
			query.append(" and p.category=:category");
		}
		if (name != null) {
			query.append(" and p.name=:name");
		}
		
		TypedQuery<Property> dbQuery = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(query.toString(), Property.class)
				.setParameter("identities", identities);;
		if (resourceable != null) {
			dbQuery.setParameter("resourceTypeName", resourceable.getResourceableTypeName());
			dbQuery.setParameter("resourceableId", resourceable.getResourceableId());
		}
		if (category != null) {
			dbQuery.setParameter("category", category);
		}
		if (name != null) {
			dbQuery.setParameter("name", name);
		}
		List<Property> props = dbQuery.getResultList();
		return props;
	}
	
	/**
	 * 
	 * @param resourceables
	 * @param category
	 * @param name
	 * @return
	 */
	public List<Property> findProperties(String resourceableTypeName, List<Long> resourceableIds, String category, String name) {
		if(resourceableIds == null || resourceableIds.isEmpty() || !StringHelper.containsNonWhitespace(resourceableTypeName)) {
			return Collections.emptyList();
		}

		StringBuilder query = new StringBuilder();
		query.append("select p from ").append(Property.class.getName()).append(" as p")
			.append(" where p.resourceTypeName=:resourceTypeName and p.resourceTypeId in (:resourceableIds)");

		if (category != null) {
			query.append(" and p.category=:category");
		}
		if (name != null) {
			query.append(" and p.name=:name");
		}
		
		TypedQuery<Property> dbQuery = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(query.toString(), Property.class)
				.setParameter("resourceTypeName", resourceableTypeName)
				.setParameter("resourceableIds", resourceableIds);
		if (category != null) {
			dbQuery.setParameter("category", category);
		}
		if (name != null) {
			dbQuery.setParameter("name", name);
		}

		List<Property> props = dbQuery.getResultList();
		return props;
	}
	
	/**
	 * Gets the property by long value.
	 */
	public Property getPropertyByLongValue (long longValue, String name) {
		StringBuilder query = new StringBuilder();
		query.append("select p from ")
		.append(Property.class.getName())
		.append(" as p")
		.append(" where p.longValue=:longValue");
		
		if (name != null) {
			query.append(" and p.name=:name");
		}
		
		TypedQuery<Property> dbQuery = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(query.toString(), Property.class)
				.setParameter("longValue", longValue);
		
		if (name != null) {
			dbQuery.setParameter("name", name);
		}
		
		List<Property> properties = dbQuery.setMaxResults(1).getResultList();
		
		if (properties.size() > 0) {
			return properties.get(0);
		} else {
			return null;
		}				
	}
	
	
	/**
	 * @return a list of all available resource type names
	 */
	public List<String> getAllResourceTypeNames() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct v.resourceTypeName from ").append(Property.class.getName()).append(" as v where v.resourceTypeName is not null");
		return DBFactory.getInstance().getCurrentEntityManager().createQuery(sb.toString(), String.class).getResultList();
	}

	@Override
	public int deleteUserDataPriority() {
		// delete with low priority
		return 110;
	}
	/**
	 * Delete all properties of a certain identity.
	 * @see org.olat.user.UserDataDeletable#deleteUserData(org.olat.core.id.Identity)
	 */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		deleteProperties(identity, null, null, null, null);
		log.debug("All properties deleted for identity={}", identity);
	}

	public Property createProperty() {
		return new Property();
	}

	private boolean and(StringBuilder sb, boolean and) {
		if(and) sb.append(" and ");
		return true;
	}
	
	private boolean appendAnd(StringBuilder sb, String content, boolean and) {
		if(and) sb.append(" and ");
		else sb.append(" where ");
		sb.append(content).append(" ");
		return true;
	}
}
