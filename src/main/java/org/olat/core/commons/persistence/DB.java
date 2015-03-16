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
* <p>
*/ 

package org.olat.core.commons.persistence;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.stat.Statistics;
import org.hibernate.type.Type;
import org.infinispan.manager.EmbeddedCacheManager;
import org.olat.core.id.Persistable;

public interface DB {
	
	public boolean isMySQL();
	
	public boolean isPostgreSQL();
	
	public boolean isOracle();
	
	/**
	 * Close the database session.
	 */
	public void closeSession();

	/**
	 * Create a DBQuery
	 * 
	 * @param query
	 * @return DBQuery
	 */
	public DBQuery createQuery(String query);

	/**
	 * Delete an object.
	 * 
	 * @param object
	 */
	public void deleteObject(Object object);

	/**
	 * Find objects based on query
	 * 
	 * @param query
	 * @param value
	 * @param type
	 * @return List of results.
	 */
	public List find(String query, Object value, Type type);

	/**
	 * Find objects based on query
	 * 
	 * @param query
	 * @param values
	 * @param types
	 * @return List of results.
	 */
	public List find(String query, Object[] values, Type[] types);

	/**
	 * Find an object.
	 * 
	 * @param theClass
	 * @param key
	 * @return Object, if any found. or null otherwise
	 */
	public <U> U findObject(Class<U> theClass, Long key);

	/**
	 * Find objects based on query
	 * 
	 * @param query
	 * @return List of results.
	 */
	public List find(String query);

	/**
	 * Load an object.
	 * 
	 * @param theClass
	 * @param key
	 * @return Object.
	 */
	public <U> U loadObject(Class<U> theClass, Long key);

	/**
	 * Save an object.
	 * 
	 * @param object
	 */
	public void saveObject(Object object);

	/**
	 * Update an object.
	 * 
	 * @param object
	 */
	public void updateObject(Object object);

	/**
	 * Deletion query.
	 * 
	 * @param query
	 * @param value
	 * @param type
	 * @return nr of values deleted
	 */
	public abstract int delete(String query, Object value, Type type);

	/**
	 * Deletion query.
	 * 
	 * @param query
	 * @param values
	 * @param types
	 * @return nr of deleted rows
	 */
	public int delete(String query, Object[] values, Type[] types);

	/**
	 * see DB.loadObject(Persistable persistable, boolean forceReloadFromDB)
	 * 
	 * @param persistable
	 * @return the loaded object, never null
	 */
	public Persistable loadObject(Persistable persistable);

	/**
	 * loads an object if needed. this makes sense if you have an object which had
	 * been generated in a previous hibernate session AND you need to access a Set
	 * or a attribute which was defined as a proxy.
	 * 
	 * @param persistable the object which needs to be reloaded
	 * @param forceReloadFromDB if true, force a reload from the db (e.g. to catch
	 *          up to an object commited by another thread which is still in this
	 *          thread's session cache
	 * @return the loaded Object, never null
	 */
	public Persistable loadObject(Persistable persistable, boolean forceReloadFromDB);

	
	/**
	 * Call this to commit current changes.
	 */
	public void commit();
	
	/**
	 * Checks if the transaction needs to be committed and does so if this is the case,
	 * plus closes the connection in any case guaranteed.
	 * <p>
	 * Use this rather than commit() directly wherever possible!
	 */
	public void commitAndCloseSession();
  
	/**
	 * Calls rollback and closes the connection guaranteed.
	 * <p>
	 * Note that this method checks whether the connection and the transaction are open
	 * and if they're not, then this method doesn't do anything.
	 */
	public void rollbackAndCloseSession();
	
	/**
	 * Call this to rollback current changes.
	 */
	public void rollback();
  
	/**
	 * Statistics must be enabled first, when you want to use it. 
	 * @return Return Hibernates statistics object.
	 */
	public Statistics getStatistics();
	
	/**
	 * 
	 * @return The infinispan cache manager if it's configured
	 */
	public EmbeddedCacheManager getCacheContainer();
	
	public String getDbVendor();

	/**
	 * Call this to intermediate commit current changes.
	 * Use this method in startup process and bulk changes.
	 */
	public void intermediateCommit();

	/**
	 * @return True if any errors occured in the previous DB call.
	 */
	public boolean isError();

	/**
	 * 
	 * Return the current entity manager to work with JPA2
	 * 
	 * @return
	 */
	public EntityManager getCurrentEntityManager();
}