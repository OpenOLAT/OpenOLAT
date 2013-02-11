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

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.stat.Statistics;
import org.hibernate.type.Type;
import org.olat.core.configuration.Destroyable;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.LogDelegator;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A <b>DB </b> is a central place to get a Entity Managers. It acts as a
 * facade to the database, transactions and Queries. The hibernateSession is
 * lazy loaded per thread.
 * 
 * @author Andreas Ch. Kapp
 * @author Christian Guretzki
 */
public class DBImpl extends LogDelegator implements DB, Destroyable {
	private static final int MAX_DB_ACCESS_COUNT = 500;
	private static DBImpl INSTANCE;
	
	private final DBManager dbManagerDelegate = new DBManager();
	
	private EntityManagerFactory emf;
	private PlatformTransactionManager txManager;

	private final ThreadLocal<ThreadLocalData> data = new ThreadLocal<ThreadLocalData>();
	// Max value for commit-counter, values over this limit will be logged.
	private static int maxCommitCounter = 10;

	/**
	 * [used by spring]
	 */
	private DBImpl() {
		INSTANCE = this;
	}
    
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}
	
	public void setTxManager(PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}

	/**
	 * A <b>ThreadLocalData</b> is used as a central place to store data on a per
	 * thread basis.
	 * 
	 * @author Andreas CH. Kapp
	 * @author Christian Guretzki
	 */
	protected static class ThreadLocalData {

		private boolean error;
		private Exception lastError;
		
		private boolean initialized = false;
		// count number of db access in beginTransaction, used to log warn 'to many db access in one transaction'
		private int accessCounter = 0;
		// count number of commit in db-session, used to log warn 'Call more than one commit in a db-session'
		private int commitCounter = 0;
		
		private ThreadLocalData() {
		// don't let any other class instantiate ThreadLocalData.
		}

		/**
		 * @return true if initialized.
		 */
		protected boolean isInitialized() {
			return initialized;
		}

		protected void setInitialized(boolean b) {
			initialized = b;
		}

		public boolean isError() {
			return error;
		}

		public void setError(boolean error) {
			this.error = error;
		}

		public Exception getLastError() {
			return lastError;
		}

		public void setError(Exception ex) {
			this.lastError = ex;
			this.error = true;
		}

		protected void incrementAccessCounter() {
			this.accessCounter++;
		}
		
		protected int getAccessCounter() {
			return this.accessCounter;
		}
		
		protected void resetAccessCounter() {
			this.accessCounter = 0;
		}	

		protected void incrementCommitCounter() {
			this.commitCounter++;
		}
		
		protected int getCommitCounter() {
			return this.commitCounter;
		}

		protected void resetCommitCounter() {
			this.commitCounter = 0;
		}
	}

	private void setData(ThreadLocalData data) {
		this.data.set(data);
	}

	private ThreadLocalData getData() {
		ThreadLocalData tld = (ThreadLocalData) data.get();
		if (tld == null) {
			tld = new ThreadLocalData();
			setData(tld);
		}
		return tld;
	}
	
	@Override
	public EntityManager getCurrentEntityManager() {
		//if spring has already an entity manager in this thread bounded, return it
		EntityManager threadBoundedEm = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		if(threadBoundedEm != null && threadBoundedEm.isOpen()) {
			EntityTransaction trx = threadBoundedEm.getTransaction();
			//if not active begin a new one (possibly manual committed)
			if(!trx.isActive()) {
				trx.begin();
			}
			updateDataStatistics(threadBoundedEm, "entityManager");
			return threadBoundedEm;
		}
		EntityManager em = getEntityManager();
		updateDataStatistics(em, "entityManager");
		return em;
	}
	
	private EntityManager getEntityManager() {
		EntityManager txEm = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		if(txEm == null) {
			if(txManager != null) {
				DefaultTransactionDefinition def = new DefaultTransactionDefinition();
				txManager.getTransaction(def);
				txEm = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
			} else {
				txEm = emf.createEntityManager();
			}
		} else if(!txEm.isOpen()) {
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			txManager.getTransaction(def);
			txEm = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		} else {
			EntityTransaction trx = txEm.getTransaction();
			//if not active begin a new one (possibly manual committed)
			if(!trx.isActive()) {
				trx.begin();
			}
		}
		return txEm;
	}

  private void updateDataStatistics(EntityManager em, Object logObject) {
		/*
  	//OLAT-3621: paranoia check for error state: we need to catch errors at the earliest point possible. OLAT-3621 has a suspected situation
		//           where an earlier transaction failed and didn't clean up nicely. To check this, we introduce error checking in getInstance here
	  
		if (transaction != null && transaction.isActive() && transaction.getRollbackOnly()
				&& !Thread.currentThread().getName().equals("TaskExecutorThread")) {
			INSTANCE.logWarn("beginTransaction: Transaction (still?) in Error state: "+transaction, new Exception("DBImpl begin transaction)"));
		}
		*/
	
  	// increment only non-cachable query 
  	if (logObject instanceof String ) {
  		String query = (String) logObject;
  		query = query.trim();
  		if (   !query.startsWith("select count(poi) from org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi, org.olat.basesecurity.PolicyImpl as poi,")
  		    && !query.startsWith("select count(grp) from org.olat.group.BusinessGroupImpl as grp")
  		    && !query.startsWith("select count(sgmsi) from  org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi") ) {
  			// it is no of cached queries
  			getData().incrementAccessCounter();  			
  		}
  	} else {
  		getData().incrementAccessCounter();
  	}

    if (getData().getAccessCounter() > MAX_DB_ACCESS_COUNT) {
    	logWarn("beginTransaction bulk-change, too many db access for one transaction, could be a performance problem (add closeSession/createSession in loop) logObject=" + logObject, null);
    	getData().resetAccessCounter();
    }
    
    EntityTransaction transaction = em.getTransaction();
    if(transaction == null) {
    	logError("Call beginTransaction but transaction is null", null);
    } else if (transaction.isActive() && transaction.getRollbackOnly()) {
    	logError("Call beginTransaction but transaction is already rollbacked", null);
    } else if (!transaction.isActive()) {
    	logError("Call beginTransaction but transaction is already completed", null);
    }
  }
  /*
	private void createSession2() {
		ThreadLocalData data = getData();
		if (data == null) {
			if (isLogDebugEnabled()) logDebug("createSession start...", null);
			data.resetAccessCounter();
			data.resetCommitCounter();
		}
		setInitialized(true);
		if (isLogDebugEnabled()) logDebug("createSession end...", null);
	}*/

	/**
	 * Close the database session.
	 */
	public void closeSession() {
		

		getData().resetAccessCounter();
		// Note: closeSession() now also checks if the connection is open at all
		//  in OLAT-4318 a situation is described where commit() fails and closeSession()
		//  is not called at all. that was due to a call to commit() with a session
		//  that was closed underneath by hibernate (not noticed by DBImpl).
		//  in order to be robust for any similar situation, we check if the 
		//  connection is open, otherwise we shouldn't worry about doing any commit/rollback anyway
		

		//commit
		//getCurrentEntityManager();
		EntityManager s = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		if(s != null) {
			EntityTransaction trx = s.getTransaction();
			if(trx.isActive()) {
				if(trx.getRollbackOnly()) {
					trx.rollback();
				} else {
					try {
						trx.commit();
					} catch (Exception e) {
						logError("", e);
						trx.rollback();
					}
				}
			}
	
			TransactionSynchronizationManager.clear();
			EntityManagerFactoryUtils.closeEntityManager(s);
			Map<Object,Object> map = TransactionSynchronizationManager.getResourceMap();
			if(map.containsKey(emf)) {
				TransactionSynchronizationManager.unbindResource(emf);
			}
		}
		data.remove();
	}
	
	public void cleanUpSession() {
		closeSession();
	}

	protected static DBImpl getInstance() {
	  return getInstance(true);
  }
	/**
	 * Get the DB instance. Initialisation is performed if flag is true.
	 * 
	 * @param initialize
	 * @return the DB instance.
	 */
	protected static DBImpl getInstance(boolean initialize) {
		/*
		//OLAT-3621: paranoia check for error state: we need to catch errors at the earliest point possible. OLAT-3621 has a suspected situation
		//           where an earlier transaction failed and didn't clean up nicely. To check this, we introduce error checking in getInstance here
		EntityTransaction transaction = INSTANCE.getTransaction();
		if (transaction!=null) {		
			// Filter Exception form async TaskExecutorThread, there are exception allowed
			if (transaction.isActive() && transaction.getRollbackOnly() && !Thread.currentThread().getName().equals("TaskExecutorThread")) {
				INSTANCE.logWarn("getInstance: Transaction (still?) in Error state: "+transaction, new Exception("DBImpl begin transaction)"));
			}
		}  
		
		// if module is not active we return a non-initialized instance and take
		// care that
		// the only cleanup-calls to db.closeSession do nothing
		if (initialize) {
			INSTANCE.createSession();
		}*/
		return INSTANCE;
	}

	/**
	 * Get db instance without checking transaction state
	 * @return
	 */
	protected static DBImpl getInstanceForClosing() {
		return INSTANCE;
	}

	/**
	 * @return true if tread is initialized.
	 */
	boolean threadLocalsInitialized() {
		return getData().isInitialized();
	}

	private void setInitialized(boolean initialized) {
		getData().setInitialized(initialized);

	}

	boolean isInitialized() {
		return getData().isInitialized();
	}
  
	private boolean contains(Object object) {
		EntityManager em = getCurrentEntityManager();
		return dbManagerDelegate.contains(em, object);
	}

	/**
	 * Create a DBQuery
	 * 
	 * @param query
	 * @return DBQuery
	 */
	public DBQuery createQuery(String query) {
		EntityManager em = getCurrentEntityManager();
		return dbManagerDelegate.createQuery(em, query, getData());
	}

	/**
	 * Delete an object.
	 * 
	 * @param object
	 */
	public void deleteObject(Object object) {
		EntityManager em = getCurrentEntityManager();
		dbManagerDelegate.deleteObject(em, object, getData());
	}

	/**
	 * Deletion query.
	 * 
	 * @param query
	 * @param value
	 * @param type
	 * @return nr of deleted rows
	 */
	public int delete(String query, Object value, Type type) {
		EntityManager em = getCurrentEntityManager();
		return dbManagerDelegate.delete(em, query, value, type);
	}

	/**
	 * Deletion query.
	 * 
	 * @param query
	 * @param values
	 * @param types
	 * @return nr of deleted rows
	 */
	public int delete(String query, Object[] values, Type[] types) {
		EntityManager em = getCurrentEntityManager();
		return dbManagerDelegate.delete(em, query, values, types);
	}

	/**
	 * Find objects based on query
	 * 
	 * @param query
	 * @param value
	 * @param type
	 * @return List of results.
	 */
	public List find(String query, Object value, Type type) {
		EntityManager em = getCurrentEntityManager();
		return dbManagerDelegate.find(em, query, value, type, getData());
	}

	/**
	 * Find objects based on query
	 * 
	 * @param query
	 * @param values
	 * @param types
	 * @return List of results.
	 */
	public List find(String query, Object[] values, Type[] types) {
		EntityManager em = getCurrentEntityManager();
		return dbManagerDelegate.find(em , query, values, types, getData());
	}

	/**
	 * Find objects based on query
	 * 
	 * @param query
	 * @return List of results.
	 */
	public List find(String query) {
		EntityManager em = getCurrentEntityManager();
		return dbManagerDelegate.find(em , query, getData());
	}

	/**
	 * Find an object.
	 * 
	 * @param theClass
	 * @param key
	 * @return Object, if any found. Null, if non exist. 
	 */
	public <U> U findObject(Class<U> theClass, Long key) {
		return getCurrentEntityManager().find(theClass, key);
	}
	
	/**
	 * Load an object.
	 * 
	 * @param theClass
	 * @param key
	 * @return Object.
	 */
	public <U> U loadObject(Class<U> theClass, Long key) {
		EntityManager em = getCurrentEntityManager();
		return dbManagerDelegate.loadObject(em , theClass, key);
	}

	/**
	 * Save an object.
	 * 
	 * @param object
	 */
	public void saveObject(Object object) {
		EntityManager em = getCurrentEntityManager();
		dbManagerDelegate.saveObject(em, object, getData());
	}

	/**
	 * Update an object.
	 * 
	 * @param object
	 */
	public void updateObject(Object object) {
		EntityManager em = getCurrentEntityManager();
		dbManagerDelegate.updateObject(em, object, getData());
	}

	/**
	 * Get any errors from a previous DB call.
	 * 
	 * @return Exception, if any.
	 */
	public Exception getError() {
		return getData().getLastError();
	}

	/**
	 * @return True if any errors occured in the previous DB call.
	 */
	public boolean isError() {
		//EntityTransaction trx = getCurrentEntityManager().getTransaction();
		EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		if(em != null && em.isOpen()) {
			EntityTransaction trx = em.getTransaction();
			if (trx != null && trx.isActive()) {
				return trx.getRollbackOnly();
			} 
		}
		return getData() == null ? false : getData().isError();
	}

	boolean hasTransaction() {
		//EntityManager em = getCurrentEntityManager();
		EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		if(em != null && em.isOpen()) {
			EntityTransaction trx = em.getTransaction();
			return trx != null && trx.isActive();
		}
		return false;
	}

	/**
	 * see DB.loadObject(Persistable persistable, boolean forceReloadFromDB)
	 * 
	 * @param persistable
	 * @return the loaded object
	 */
	public Persistable loadObject(Persistable persistable) {
		return loadObject(persistable, false);
	}

	/**
	 * loads an object if needed. this makes sense if you have an object which had
	 * been generated in a previous hibernate session AND you need to access a Set
	 * or a attribute which was defined as a proxy.
	 * 
	 * @param persistable the object which needs to be reloaded
	 * @param forceReloadFromDB if true, force a reload from the db (e.g. to catch
	 *          up to an object commited by another thread which is still in this
	 *          thread's session cache
	 * @return the loaded Object
	 */
	public Persistable loadObject(Persistable persistable, boolean forceReloadFromDB) {
		if (persistable == null) throw new AssertException("persistable must not be null");

		EntityManager em = getCurrentEntityManager();
		Class<? extends Persistable> theClass = persistable.getClass();
		if (forceReloadFromDB) {
			// we want to reload it from the database.
			// there are 3 scenarios possible:
			// a) the object is not yet in the hibernate cache
			// b) the object is in the hibernate cache
			// c) the object is detached and there is an object with the same id in the hibernate cache
			
			if (contains(persistable)) {
				// case b - then we can use evict and load
				dbManagerDelegate.evict(em, persistable, getData());
				return loadObject(theClass, persistable.getKey());
			} else {
				// case a or c - unfortunatelly we can't distinguish these two cases
				// and session.refresh(Object) doesn't work.
				// the only scenario that works is load/evict/load
				Persistable attachedObj = (Persistable) loadObject(theClass, persistable.getKey());
				dbManagerDelegate.evict(em, attachedObj, getData());
				return loadObject(theClass, persistable.getKey());
			}
		} else if (!contains(persistable)) { 
			// forceReloadFromDB is false - hence it is OK to take it from the cache if it would be there
			// now this object directly is not in the cache, but it's possible that the object is detached
			// and there is an object with the same id in the hibernate cache.
			// therefore the following loadObject can either return it from the cache or load it from the DB
			return loadObject(theClass, persistable.getKey());
		} else { 
			// nothing to do, return the same object
			return persistable;
		}
	}

	@Override
	public void commitAndCloseSession() {
		try {
			commit();
		} finally {
			try{
				// double check: is the transaction still open? if yes, is it not rolled-back? if yes, do a rollback now!
				if (hasTransaction() && isError()) {
					getLogger().error("commitAndCloseSession: commit seems to have failed, transaction still open. Doing a rollback!", new Exception("commitAndCloseSession"));
					rollback();
				}
			} finally {
				closeSession();
			}
		}
	}
	
	@Override
	public void rollbackAndCloseSession() {
		try {
			rollback();
		} finally {
			closeSession();
		}
	}

	/**
	 * Call this to commit a transaction opened by beginTransaction().
	 */
	public void commit() {
		if (isLogDebugEnabled()) logDebug("commit start...", null);
		try {
			if (hasTransaction() && !isError()) {
				if (isLogDebugEnabled()) logDebug("has Transaction and is in Transaction => commit", null);
				getData().incrementCommitCounter();
				if ( isLogDebugEnabled() ) {
					if ((maxCommitCounter != 0) && (getData().getCommitCounter() > maxCommitCounter) ) {
						logInfo("Call too many commit in a db-session, commitCounter=" + getData().getCommitCounter() +"; could be a performance problem" , null);
					}
				}
				
				EntityTransaction trx = getCurrentEntityManager().getTransaction();
				if(trx != null) {
					trx.commit();
				}

				if (isLogDebugEnabled()) logDebug("Commit DONE hasTransaction()=" + hasTransaction(), null);
			} else {
				if (isLogDebugEnabled()) logDebug("Call commit without starting transaction", null );
			}
		} catch (Error er) {
			logError("Uncaught Error in DBImpl.commit.", er);
			throw er;
		} catch (Exception e) {
			// Filter Exception form async TaskExecutorThread, there are exception allowed
			if (!Thread.currentThread().getName().equals("TaskExecutorThread")) {
				logWarn("Caught Exception in DBImpl.commit.", e);
			}
			// Error when trying to commit
			try {
				if (hasTransaction()) {
					TransactionStatus status = txManager.getTransaction(null);
					txManager.rollback(status);
					//getTransaction().rollback();
				}
			} catch (Error er) {
				logError("Uncaught Error in DBImpl.commit.catch(Exception).", er);
				throw er;
			} catch (Exception ex) {
				logWarn("Could not rollback transaction after commit!", ex);
				throw new DBRuntimeException("rollback after commit failed", e);
			}
			throw new DBRuntimeException("commit failed, rollback transaction", e);
		}
	}
	
	/**
	 * Call this to rollback current changes.
	 */
	public void rollback() {
		if (isLogDebugEnabled()) logDebug("rollback start...", null);
		try {
			// see closeSession() and OLAT-4318: more robustness with commit/rollback/close, therefore
			// we check if the connection is open at this stage at all

			TransactionStatus status = txManager.getTransaction(null);
			txManager.rollback(status);
			//getTransaction().rollback();

		} catch (Exception ex) {
			logWarn("Could not rollback transaction!",ex);
			throw new DBRuntimeException("rollback failed", ex);
		}		
	}

	/**
	 * Statistics must be enabled first, when you want to use it. 
	 * @return Return Hibernates statistics object.
	 */
	public Statistics getStatistics() {
		if(emf instanceof HibernateEntityManagerFactory) {
			return ((HibernateEntityManagerFactory)emf).getSessionFactory().getStatistics();
		}
 		return null;
   }

	/**
	 * @see org.olat.core.commons.persistence.DB#intermediateCommit()
	 */
	public void intermediateCommit() {
		commit();
		closeSession();
	}

	@Override
	public void destroy() {
		//clean up registered drivers to prevent messages like
		// The web application [/olat] registered the JBDC driver [com.mysql.Driver] but failed to unregister...
		Enumeration<Driver> registeredDrivers = DriverManager.getDrivers();
		while(registeredDrivers.hasMoreElements()) {
			try {
				DriverManager.deregisterDriver(registeredDrivers.nextElement());
			} catch (SQLException e) {
				logError("Could not unregister database driver.", e);
			}
		}
	}
	
	//
	// fxdiff qti-statistics (praktikum MK)
	// Extensions used for native SQL queries
	//
	private String dbVendor = null;
	
	@Override
	public String getDbVendor() {
		return dbVendor;
	}
	/**
	 * [used by spring]
	 * @param dbVendor
	 */
	public void setDbVendor(String dbVendor) {
		this.dbVendor = dbVendor;
	}

	/**
	 * Create a named hibernate query for the given name. Optionally the database
	 * vendor is prepended to load database specific queries if available. Use
	 * this only when absolutely necessary.
	 * 
	 * @param queryName The query name
	 * @param vendorSpecific true: prepend the database vendor name to the query
	 *          name, e.g. mysql_queryName; false: use queryName as is
	 * @return the query or NULL if no such named query exists
	 */
	public DBQuery createNamedQuery(final String queryName, boolean vendorSpecific) {
		if (queryName == null) {
			throw new AssertException("queryName must not be NULL");
		}
		
		EntityManager em = getCurrentEntityManager();
		return dbManagerDelegate.createNamedQuery(em, queryName, dbVendor, vendorSpecific);
	}	
}
