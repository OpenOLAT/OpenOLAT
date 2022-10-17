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
import java.util.Properties;

import jakarta.persistence.Cache;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.RollbackException;

import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.stat.Statistics;
import org.infinispan.hibernate.cache.v60.InfinispanRegionFactory;
import org.infinispan.manager.EmbeddedCacheManager;
import org.olat.core.configuration.Destroyable;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.Tracing;


/**
 * A <b>DB </b> is a central place to get a Entity Managers. It acts as a
 * facade to the database, transactions and Queries. The hibernateSession is
 * lazy loaded per thread.
 * 
 * @author Andreas Ch. Kapp
 * @author Christian Guretzki
 */
public class DBImpl implements DB, Destroyable {
	private static final Logger log = Tracing.createLoggerFor(DBImpl.class);
	private static final int MAX_DB_ACCESS_COUNT = 500;
	private static DBImpl INSTANCE;
	
	private String dbVendor;
	private static EntityManagerFactory emf;

	private final ThreadLocal<ThreadLocalData> data = new ThreadLocal<>();
	// Max value for commit-counter, values over this limit will be logged.
	private static int maxCommitCounter = 10;

	/**
	 * [used by spring]
	 */
	public DBImpl(Properties databaseProperties) {
		if(INSTANCE == null) {
			INSTANCE = this;
			try {
				emf = Persistence.createEntityManagerFactory("default", databaseProperties);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("", e);
				throw e;
			}
		}
	}
	
	protected static DBImpl getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean isMySQL() {
		return "mysql".equals(dbVendor);
	}

	@Override
	public boolean isPostgreSQL() {
		return "postgresql".equals(dbVendor);
	}

	@Override
	public boolean isOracle() {
		return "oracle".equals(dbVendor);
	}

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
	
	public void appendPostUpdateEventListener(PostUpdateEventListener listener) {
		SessionFactoryImpl sessionFactory = emf.unwrap(SessionFactoryImpl.class);
		EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
		registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(listener);
	}

	/**
	 * A <b>ThreadLocalData</b> is used as a central place to store data on a per
	 * thread basis.
	 * 
	 * @author Andreas CH. Kapp
	 * @author Christian Guretzki
	 */
	protected class ThreadLocalData {

		private boolean error;
		private Exception lastError;
		
		private boolean initialized = false;
		// count number of db access in beginTransaction, used to log warn 'to many db access in one transaction'
		private int accessCounter = 0;
		// count number of commit in db-session, used to log warn 'Call more than one commit in a db-session'
		private int commitCounter = 0;
		
		private EntityManager em;
		
		private ThreadLocalData() {
		// don't let any other class instantiate ThreadLocalData.
		}
		
		public EntityManager getEntityManager(boolean createIfNecessary) {
			if(em == null && createIfNecessary) {
				em = emf.createEntityManager();
			}
			return em;
		}
		
		public EntityManager renewEntityManager() {
			if(em != null && !em.isOpen()) {
				try {
					em.close();
				} catch (Exception e) {
					log.error("", e);
				}
				em = null;
			}
			return getEntityManager(true);
		}
		
		public void removeEntityManager() {
			em = null;
		}
		
		public boolean hasTransaction() {
			if(em != null && em.isOpen()) {
				EntityTransaction trx = em.getTransaction();
				return trx != null && trx.isActive();
			}
			return false;
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
			if(em != null && em.isOpen()) {
				EntityTransaction trx = em.getTransaction();
				if (trx != null && trx.isActive()) {
					return trx.getRollbackOnly();
				} 
			}
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
		ThreadLocalData tld = data.get();
		if (tld == null) {
			tld = new ThreadLocalData();
			setData(tld);
		}
		return tld;
	}
	
	@Override
	public EntityManager getCurrentEntityManager() {
		//if spring has already an entity manager in this thread bounded, return it
		EntityManager threadBoundedEm = getData().getEntityManager(true);
		if(threadBoundedEm != null && threadBoundedEm.isOpen()) {
			EntityTransaction trx = threadBoundedEm.getTransaction();
			//if not active begin a new one (possibly manual committed)
			if(!trx.isActive()) {
				trx.begin();
			}
			updateDataStatistics("entityManager");
			return threadBoundedEm;
		} else if(threadBoundedEm == null || !threadBoundedEm.isOpen()) {
			threadBoundedEm = getData().renewEntityManager();
		}
		
		EntityTransaction trx = threadBoundedEm.getTransaction();
		//if not active begin a new one (possibly manual committed)
		if(!trx.isActive()) {
			trx.begin();
		}
		updateDataStatistics("entityManager");
		return threadBoundedEm;
	}
	
	private Session getSession(EntityManager em) {
		return em.unwrap(Session.class);
	}
	
	private boolean unusableTrx(EntityTransaction trx) {
		return trx == null || !trx.isActive() || trx.getRollbackOnly();
	}
	
	private void updateDataStatistics(Object logObject) {
		if (getData().getAccessCounter() > MAX_DB_ACCESS_COUNT) {
			log.warn("beginTransaction bulk-change, too many db access for one transaction, could be a performance problem (add closeSession/createSession in loop) logObject=" + logObject);
			getData().resetAccessCounter();
		} else {
  			getData().incrementAccessCounter();
		}
    }

	/**
	 * Close the database session.
	 */
	@Override
	public void closeSession() {
		getData().resetAccessCounter();
		// Note: closeSession() now also checks if the connection is open at all
		//  in OLAT-4318 a situation is described where commit() fails and closeSession()
		//  is not called at all. that was due to a call to commit() with a session
		//  that was closed underneath by hibernate (not noticed by DBImpl).
		//  in order to be robust for any similar situation, we check if the 
		//  connection is open, otherwise we shouldn't worry about doing any commit/rollback anyway
		EntityManager s = getData().getEntityManager(false);
		if(s != null) {
			EntityTransaction trx = s.getTransaction();
			if(trx.isActive()) {
				try {
					trx.commit();
				} catch (RollbackException ex) {
					//possible if trx setRollbackonly
					log.warn("Close session with transaction set with setRollbackOnly", ex);
				} catch (Exception e) {
					log.error("", e);
					trx.rollback();
				}
			}
			s.close();
		}
		data.remove();
	}
  
	private boolean contains(Object object) {
		EntityManager em = getCurrentEntityManager();
		return em.contains(object);
	}

	/**
	 * Delete an object.
	 * 
	 * @param object
	 */
	@Override
	public void deleteObject(Object object) {
		EntityManager em = getCurrentEntityManager();
		EntityTransaction trx = em.getTransaction();
		if (unusableTrx(trx)) { // some program bug
			throw new DBRuntimeException("cannot delete in a transaction that is rolledback or committed " + object);
		}
		try {
			Object relaoded = em.merge(object);
			em.remove(relaoded);
			if (log.isDebugEnabled()) {
				log.debug("delete (trans "+trx.hashCode()+") class "+object.getClass().getName()+" = "+object.toString());	
			}
		} catch (HibernateException e) { // we have some error
			trx.setRollbackOnly();
			getData().setError(e);
			throw new DBRuntimeException("Delete of object failed: " + object, e);
		}
	}

	/**
	 * Save an object.
	 * 
	 * @param object
	 */
	@Override
	public void saveObject(Object object) {
		EntityManager em = getCurrentEntityManager();
		EntityTransaction trx = em.getTransaction();
		if (unusableTrx(trx)) { // some program bug
			throw new DBRuntimeException("cannot save in a transaction that is rolledback or committed: " + object);
		}
		try {
			em.persist(object);					
		} catch (Exception e) { // we have some error
			log.error("", e);
			trx.setRollbackOnly();
			getData().setError(e);
			throw new DBRuntimeException("Save failed in transaction. object: " +  object, e);
		}
	}

	/**
	 * Update an object.
	 * 
	 * @param object
	 */
	@Override
	public void updateObject(Object object) {
		EntityManager em = getCurrentEntityManager();
		EntityTransaction trx = em.getTransaction();
		if (unusableTrx(trx)) { // some program bug
			throw new DBRuntimeException("cannot update in a transaction that is rolledback or committed " + object);
		}
		try {
			getSession(em).update(object);								
		} catch (HibernateException e) { // we have some error
			trx.setRollbackOnly();
			getData().setError(e);
			throw new DBRuntimeException("Update object failed in transaction. Query: " +  object, e);
		}
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
	@Override
	public boolean isError() {
		return getData().isError();
	}

	private boolean hasTransaction() {
		return getData().hasTransaction();
	}

	/**
	 * see DB.loadObject(Persistable persistable, boolean forceReloadFromDB)
	 * 
	 * @param persistable
	 * @return the loaded object
	 */
	@Override
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
	@Override
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
				evict(em, persistable, getData());
				return em.find(theClass, persistable.getKey());
			} else {
				// case a or c - unfortunatelly we can't distinguish these two cases
				// and session.refresh(Object) doesn't work.
				// the only scenario that works is load/evict/load
				Persistable attachedObj = em.find(theClass, persistable.getKey());
				evict(em, attachedObj, getData());
				return em.find(theClass, persistable.getKey());
			}
		} else if (!contains(persistable)) { 
			// forceReloadFromDB is false - hence it is OK to take it from the cache if it would be there
			// now this object directly is not in the cache, but it's possible that the object is detached
			// and there is an object with the same id in the hibernate cache.
			// therefore the following loadObject can either return it from the cache or load it from the DB
			return em.find(theClass, persistable.getKey());
		} else { 
			// nothing to do, return the same object
			return persistable;
		}
	}
	
	private void evict(EntityManager em, Object object, ThreadLocalData localData) {
		try {
			getSession(em).evict(object);			
		} catch (Exception e) {
			localData.setError(e);
			throw new DBRuntimeException("Error in evict() Object from Database. ", e);
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
					log.error("commitAndCloseSession: commit seems to have failed, transaction still open. Doing a rollback!", new Exception("commitAndCloseSession"));
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
	@Override
	public void commit() {
		boolean debug = log.isDebugEnabled();
		if (debug) log.debug("commit start...");
		try {
			if (hasTransaction() && !isError()) {
				if (debug) log.debug("has Transaction and is in Transaction => commit");
				getData().incrementCommitCounter();
				if (debug) {
					if ((maxCommitCounter != 0) && (getData().getCommitCounter() > maxCommitCounter) ) {
						log.info("Call too many commit in a db-session, commitCounter=" + getData().getCommitCounter() +"; could be a performance problem");
					}
				}
				
				EntityTransaction trx = getCurrentEntityManager().getTransaction();
				if(trx != null) {
					trx.commit();
				}

				if (debug) log.debug("Commit DONE hasTransaction()=" + hasTransaction());
			} else if(hasTransaction() && isError()) {
				EntityTransaction trx = getCurrentEntityManager().getTransaction();
				if(trx != null && trx.isActive()) {
					throw new DBRuntimeException("Try to commit a transaction in error status");
				}
			} else {
				if (debug) log.debug("Call commit without starting transaction");
			}
		} catch (Error er) {
			log.error("Uncaught Error in DBImpl.commit.", er);
			throw er;
		} catch (Exception e) {
			// Filter Exception form async TaskExecutorThread, there are exception allowed
			if (!Thread.currentThread().getName().equals("TaskExecutorThread")) {
				log.warn("Caught Exception in DBImpl.commit.", e);
			}
			// Error when trying to commit
			try {
				if (hasTransaction()) {
					EntityTransaction trx = getCurrentEntityManager().getTransaction();
					if(trx != null && trx.isActive()) {
						if(trx.getRollbackOnly()) {
							try {
								trx.commit();
							} catch (RollbackException e1) {
								//we wait for this exception
							}
						} else {
							trx.rollback();
						}
					}
				}
			} catch (Error er) {
				log.error("Uncaught Error in DBImpl.commit.catch(Exception).", er);
				throw er;
			} catch (Exception ex) {
				log.warn("Could not rollback transaction after commit!", ex);
				throw new DBRuntimeException("rollback after commit failed", e);
			}
			throw new DBRuntimeException("commit failed, rollback transaction", e);
		}
	}
	
	/**
	 * Call this to rollback current changes.
	 */
	@Override
	public void rollback() {
		if (log.isDebugEnabled()) log.debug("rollback start...");
		try {
			// see closeSession() and OLAT-4318: more robustness with commit/rollback/close, therefore
			// we check if the connection is open at this stage at all
			EntityTransaction trx = getCurrentEntityManager().getTransaction();
			if(trx != null && trx.isActive()) {
				if(trx.getRollbackOnly()) {
					try {
						trx.commit();
					} catch (RollbackException e) {
						//we wait for this exception
					}
				} else {
					trx.rollback();
				}
			}

		} catch (Exception ex) {
			log.warn("Could not rollback transaction!",ex);
			throw new DBRuntimeException("rollback failed", ex);
		}		
	}

	/**
	 * Statistics must be enabled first, when you want to use it. 
	 * @return Return Hibernates statistics object.
	 */
	@Override
	public Statistics getStatistics() {
		if(emf instanceof SessionFactoryImplementor) {
			return ((SessionFactoryImplementor)emf).getStatistics();
		}
 		return null;
   }

	@Override
	public EmbeddedCacheManager getCacheContainer() {
		EmbeddedCacheManager cm;
		try {
			Cache cache = emf.getCache();
			InfinispanRegionFactory region = cache.unwrap(InfinispanRegionFactory.class);
			cm = region.getCacheManager();
		} catch (Exception e) {
			log.error("", e);
			cm = null;
		}
		return cm;
	}

	/**
	 * @see org.olat.core.commons.persistence.DB#intermediateCommit()
	 */
	@Override
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
				log.error("Could not unregister database driver.", e);
			}
		}
	}
}
