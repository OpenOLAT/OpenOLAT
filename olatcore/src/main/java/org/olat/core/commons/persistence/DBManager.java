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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.commons.persistence;

import java.sql.Connection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;

/**
 * 
 * @author Andreas Ch. Kapp
 * @author Christian Guretzki
 */
class DBManager extends BasicManager {
	private Exception lastError;
	private boolean error;
	private  DBSession dbSession = null;

	DBManager(Session hibernateSession) {
		setDbSession(new DBSession(hibernateSession));
	}
	
	/**
	 * @param e
	 */
	private void setError(Exception e) {
		setLastError(e);
		error = true;

	}

	/**
	 * Hibernate Update method. Use this in a transactional context by using
	 * your current transaction object.
	 * @param trx The current db transaction
	 * @param o Object to be updated
	 */	
	void updateObject(DBTransaction trx, Object o) {
		
		if (trx.isRolledBack() || trx.isCommitted()) { // some program bug
			throw new DBRuntimeException("cannot update in a transaction that is rolledback or committed " + o);
		}
		try {
			getSession().update(o);
			if (Tracing.isDebugEnabled(DBManager.class)) {
				Tracing.logDebug("update (trans "+trx.hashCode()+") class "+o.getClass().getName()+" = "+o.toString(),DBManager.class);	
			}									
		} catch (HibernateException e) { // we have some error
			trx.setErrorAndRollback(e);
			setError(e);
			throw new DBRuntimeException("Update object failed in transaction. Query: " +  o , e);
		}
	}
	
	/**
	 * Hibernate Delete method. Use this in a transactional context by using
	 * your current transaction object.
	 * @param trx The current db transaction
	 * @param o Object to be deleted
	 */	
	void deleteObject(DBTransaction trx, Object o) {
		
		if (trx.isRolledBack() || trx.isCommitted()) { // some program bug
			throw new DBRuntimeException("cannot delete in a transaction that is rolledback or committed " + o);
		}
		try {
			getSession().delete(o);
			if (Tracing.isDebugEnabled(DBManager.class)) {
				Tracing.logDebug("delete (trans "+trx.hashCode()+") class "+o.getClass().getName()+" = "+o.toString(),DBManager.class);	
			}
		} catch (HibernateException e) { // we have some error
			trx.setErrorAndRollback(e);
			setError(e);
			throw new DBRuntimeException("Delete of object failed: " + o, e);
		}
	}
	
	/**
	 * Hibernate Save method. Use this in a transactional context by using
	 * your current transaction object.
	 * @param trx The current db transaction
	 * @param o Object to be saved
	 */	
	void saveObject(DBTransaction trx, Object o) {
		Session hibernateSession = this.getSession();
		if (trx.isRolledBack() || trx.isCommitted()) { // some program bug
			throw new DBRuntimeException("cannot save in a transaction that is rolledback or committed: " + o);
		}
		try {
			hibernateSession.save(o);
			if (Tracing.isDebugEnabled(DBManager.class)) {
				Tracing.logDebug("save (trans "+trx.hashCode()+") class "+o.getClass().getName()+" = "+o.toString(),DBManager.class);	
			}						

		} catch (HibernateException e) { // we have some error
			trx.setErrorAndRollback(e);		
			throw new DBRuntimeException("Save failed in transaction. object: " +  o, e);
		}

	}

	/**
	 * Hibernate Load method. Use this in a transactional context by using
	 * your current transaction object.
	 * @param trx The current db transaction
	 * @param theClass The class for the object to be loaded
	 * @param pK The primary key for the object
	 */	
	Object loadObject(DBTransaction trx, Class theClass, Long pK) {
		Object o = null;
		try {
			o = getSession().load(theClass, pK);
			if (Tracing.isDebugEnabled(DBManager.class)) {
				Tracing.logDebug("load (res " +(o == null? "null": "ok")+")(trans "+trx.hashCode()+") key "+pK+" class "+theClass.getName(),DBManager.class);	
			}

		} catch (HibernateException e) {
			trx.setErrorAndRollback(e);
			String msg = "loadObject error: " + theClass + " " + pK + " ";
			throw new DBRuntimeException(msg, e);
		}
		return o;
	}
	
	/**
	 * Hibernate Find method. Use this in a transactional context by using
	 * your current transaction object.
	 * @param trx The current db transaction
	 * @param query The HQL query
	 * @param value The value to search for
	 * @param type The Hibernate datatype of the search value 
	 */	
	List find(DBTransaction trx, String query, Object value, Type type) {
		List li = null;
		try {
			boolean doLog = Tracing.isDebugEnabled(DBManager.class);
			long start = 0;
			if (doLog) start = System.currentTimeMillis();

			// old: li = this.getSession().find(query, value, type);
			Query qu = this.getSession().createQuery(query);
			qu.setParameter(0, value, type);
			li = qu.list();
			
			if (doLog) {
				long time = (System.currentTimeMillis() - start);
				logQuery("find (time "+time+", res " +(li == null? "null": ""+li.size())+")(trans "+trx.hashCode()+")", new Object[] {value}, new Type[] {type}, query);	
			}
		} catch (HibernateException e) {
			trx.setErrorAndRollback(e);
			String msg = "Find failed in transaction. Query: " +  query + " " + e;
			setError(e);
			throw new DBRuntimeException(msg, e);
		}
		return li;
	}

	/**
	 * Hibernate Find method. Use this in a transactional context by using
	 * your current transaction object.
	 * @param trx The current db transaction
	 * @param query The HQL query
	 * @param values The object array containing all search values 
	 * @param types The object array containing all Hibernate datatype of the search values 
	 */	
	List find(DBTransaction trx, String query, Object [] values, Type [] types) {
		List li = null;
		try {
			boolean doLog = Tracing.isDebugEnabled(DBManager.class);
			long start = 0;
			if (doLog) start = System.currentTimeMillis();
			// old: li = getSession().find(query, values, types);
			Query qu = this.getSession().createQuery(query);
			qu.setParameters(values, types);
			li = qu.list();
			
			
			if (doLog) {
				long time = (System.currentTimeMillis() - start);
				logQuery("find (time "+time+", res " +(li == null? "null": ""+li.size())+")(trans "+trx.hashCode()+")", values, types, query);	
			}			
	
		} catch (HibernateException e) {
			trx.setErrorAndRollback(e);
			String msg = "Find failed in transaction. Query: " +  query + " " + e;
			setError(e);
			throw new DBRuntimeException(msg, e);
		}
		return li;
	}
	
	/**
	 * Hibernate find method. Use this in a transactional context by using
	 * your current transaction object.
	 * @param trx The current db transaction
	 * @param query The HQL query
	 */	
	List find(DBTransaction trx, String query) {
		List li = null;
		try {
			boolean doLog = Tracing.isDebugEnabled(DBManager.class);
			long start = 0;
			if (doLog) start = System.currentTimeMillis();
			// old: li = getSession().find(query);
			Query qu = this.getSession().createQuery(query);
			li = qu.list();

			if (doLog) {
				long time = (System.currentTimeMillis() - start);
				logQuery("find (time "+time+", res " +(li == null? "null": ""+li.size())+")(trans "+trx.hashCode()+")", null, null, query);	
			}
		} catch (HibernateException e) {
			String msg = "Find in transaction failed: " + query + " " + e;
			trx.setErrorAndRollback(e);
			setError(e);
			throw new DBRuntimeException(msg, e);
		}
		return li;
	}

	DBSession getDbSession() {
		return dbSession;
	}
	
	Session getSession() {
		return getDbSession().getHibernateSession();
	}

	DBTransaction beginTransaction() {
		return this.getDbSession().beginDbTransaction();
	}
	
	/**
	 * @param query
	 * @return Hibernate Query object.
	 */
	DBQuery createQuery(String query) {
		Query q = null;
		DBQuery dbq = null;
		try {
			q = this.getSession().createQuery(query);
			dbq = new DBQueryImpl(q);
		} catch (HibernateException he) {
			setError(he);
			throw new DBRuntimeException("Error while creating DBQueryImpl: ", he);
		}
		return dbq;
	}
	
	/**
	 * @param session
	 */
	void setDbSession(DBSession session) {
		dbSession = session;
	}

	/**
	 * Hibernate Contains method
	 * @param object
	 * @return True if the Object instance is in the Hibernate Session.
	 */
	boolean contains(Object object) {
		return getDbSession().contains(object);
	}
	

	/**
	 * Hibernates Evict method
	 * @param object The object to be removed from hibernates session cache
	 */
	void evict(Object object) {
		try {
			getSession().evict(object);			
		} catch (Exception e) {
			setError(e);
			throw new DBRuntimeException("Error in evict() Object from Database. ", e);
		}
	}
	
	/**
	 * Hibernate refresh method
	 * @return object a persistent or detached instance
	 */
	void refresh(Object object) {
		try{
			getSession().refresh(object);
		} catch(Exception e) {
			setError(e);
			throw new DBRuntimeException("Error in refresh() Object from Database. ", e);
		}
	}

	boolean isError() {
		return error;
	}

	Connection getConnection() {
		try {
			return getSession().connection();
		} catch (HibernateException he) {
			setError(he);
			throw new DBRuntimeException("Error in getting sql.Connection.", he); 

		} 
	}
	
	int delete(String query, Object value, Type type) {
		int deleted = 0;
		try {
			// old: deleted = getSession().delete(query, value, type);
			Session si = getSession();
			Query qu = si.createQuery(query);
			qu.setParameter(0, value, type);
			List foundToDel = qu.list();
			int deletionCount = foundToDel.size();
			for (int i = 0; i < deletionCount; i++ ) {
				si.delete( foundToDel.get(i) );
			}
			////
			getSession().flush();
			
			if (Tracing.isDebugEnabled(DBManager.class)) {
				logQuery("delete", new Object[] {value}, new Type[] {type}, query);	
			}						
		} catch (HibernateException e) {
			setError(e);
			throw new DBRuntimeException ("Delete error. Query" + query + " Object: " + value, e);
		}
		return deleted;
	}
	
	int delete(DBTransaction trx, String query, Object value, Type type) {	
		int deleted = 0;
		
		if (trx.isRolledBack() || trx.isCommitted()) { // some program bug
			throw new DBRuntimeException("cannot delete in a transaction that is rolledback or committed " + value);
		}
		try {
			//old: deleted = getSession().delete(query, value, type);
			
			Session si = getSession();
			Query qu = si.createQuery(query);
			qu.setParameter(0, value, type);
			List foundToDel = qu.list();
			int deletionCount = foundToDel.size();
			for (int i = 0; i < deletionCount; i++ ) {
				si.delete( foundToDel.get(i) );
			}
			
			if (Tracing.isDebugEnabled(DBManager.class)) {
				logQuery("delete (trans "+trx.hashCode()+")",new Object[] {value}, new Type[] {type}, query);	
			}
		} catch (HibernateException e) { // we have some error
			trx.setErrorAndRollback(e);
			throw new DBRuntimeException ("Could not delete object: " + value, e);
		}
		return deleted;
	}

	int delete(DBTransaction trx, String query, Object[] values, Type[] types) {
		int deleted = 0;
		
		if (trx.isRolledBack() || trx.isCommitted()) { // some program bug
			throw new DBRuntimeException("cannot delete in a transaction that is rolledback or committed " + values);
		}
		try {
			//old: deleted = getSession().delete(query, values, types);
			Session si = getSession();
			Query qu = si.createQuery(query);
			qu.setParameters(values, types);
			List foundToDel = qu.list();
			deleted = foundToDel.size();
			for (int i = 0; i < deleted; i++ ) {
				si.delete( foundToDel.get(i) );
			}
			
			if (Tracing.isDebugEnabled(DBManager.class)) {
				logQuery("delete (trans "+trx.hashCode()+")", values, types, query);	
			}			
		} catch (HibernateException e) { // we have some error
			trx.setErrorAndRollback(e);
			throw new DBRuntimeException ("Could not delete object: " + values, e);
		}
		return deleted;
	}

	/**
	 * Find a Object by its identifier. 
	 * This method should be used instead of loadObject when it is ok to not find the object.
	 * In oposite to the loadObject method this method will return null and not throw a 
	 * DBRuntimeException when the object can't be loaded.
	 * @param theClass
	 * @param key
	 * @return an persistent object.
	 */
	Object findObject(Class theClass, Long key) {
	    //o_clusterREVIEW see Session.java
		 /* You should not use this method to determine if an instance exists (use <tt>get()</tt>
		 * instead). Use this only to retrieve an instance that you assume exists, where non-existence
		 * would be an actual error.
		 *
		 * @param theClass a persistent class
		 * @param id a valid identifier of an existing persistent instance of the class
		 * @return the persistent instance or proxy
		 * @throws HibernateException
		 */
		//public Object load(Class theClass, Serializable id) throws HibernateException;

		
		//try {
	    	//Object o = getSession().load(theClass, key);
	    	Object o = getSession().get(theClass, key);
	    	if (Tracing.isDebugEnabled(DBManager.class)) {
				Tracing.logDebug("findload (res " +(o == null? "null": "ok")+") key "+key+" class "+theClass.getName(),DBManager.class);	
			}			
            return o;
        //} catch (HibernateException e) {
         //   return null;
        //}
	}

	/**
	 * @return Exception if any
	 */
	Exception getLastError() {
		return lastError;
	}

	/**
	 * @param exception
	 */
	private void setLastError(Exception exception) {
		lastError = exception;
	}
	
	private void logQuery(String info, Object[] values, Type[] types, String query) {
		StringBuilder sb = new StringBuilder(info);
		sb.append(": args: ");
		if (values == null) {
			sb.append(" none");
		}
		else {
			for (int i = 0; i < values.length; i++) {
				Object val = values[i];
				Type type = types[i];
				sb.append((val == null? "NULL": val.toString())).append("(").append(type.getName()).append(" ), ");
			}
		}
		sb.append(", query: ").append(query);
		Tracing.logDebug(sb.toString(),DBManager.class);
	}
	
}