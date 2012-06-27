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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.jmx.StatisticsService;
import org.hibernate.stat.Statistics;
import org.hibernate.type.Type;
import org.olat.core.CoreSpringFactory;
import org.olat.core.configuration.Destroyable;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.LogDelegator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.testutils.codepoints.server.Codepoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.jmx.support.MBeanServerFactoryBean;

/**
 * A <b>DB </b> is a central place to get a Hibernate Session. It acts as a
 * facade to the database, transactions and Queries. The hibernateSession is
 * lazy loaded per thread.
 * 
 * @author Andreas Ch. Kapp
 * @author Christian Guretzki
 */
public class DBImpl extends LogDelegator implements DB, Destroyable {
	private static final int MAX_DB_ACCESS_COUNT = 500;
	private static DBImpl INSTANCE;
	private EntityManagerFactory emf = null;

	private final ThreadLocal<ThreadLocalData> data = new ThreadLocal<ThreadLocalData>();
	private OLog forcedLogger;
	// Max value for commit-counter, values over this limit will be logged.
	private static int maxCommitCounter = 10;

	/**
	 * [used by spring]
	 */
    private DBImpl() {
       INSTANCE = this;
    }

	/**
	 * A <b>ThreadLocalData</b> is used as a central place to store data on a per
	 * thread basis.
	 * 
	 * @author Andreas CH. Kapp
	 * @author Christian Guretzki
	 */
	private class ThreadLocalData {
		private DBManager manager;
		private boolean initialized = false;
		// count number of db access in beginTransaction, used to log warn 'to many db access in one transaction'
		private int accessCounter = 0;
		// count number of commit in db-session, used to log warn 'Call more than one commit in a db-session'
		private int commitCounter = 0;

		// transaction listeners
		private Set<ITransactionListener> transactionListeners_ = null;
		
		private ThreadLocalData() {
		// don't let any other class instantiate ThreadLocalData.
		}

		/**
		 * @return true if initialized.
		 */
		protected boolean isInitialized() {
			return initialized;
		}

		protected DBManager getManager() {
			return manager;
		}

		protected void setInitialized(boolean b) {
			initialized = b;
		}

		protected void setManager(DBManager manager) {
			this.manager = manager;
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
		
		protected void addTransactionListener(ITransactionListener txListener) {
			if (transactionListeners_==null) {
				transactionListeners_ = new HashSet<ITransactionListener>();
			}
			transactionListeners_.add(txListener);
		}
		
		protected void removeTransactionListener(ITransactionListener txListener) {
			if (transactionListeners_==null) {
				// can't remove then - never mind
				return;
			}
			transactionListeners_.remove(txListener);
		}
		
		protected void handleCommit(DB db) {
			if (transactionListeners_==null) {
				//  nobody to be notified
				return;
			}
			for (Iterator<ITransactionListener> it = transactionListeners_.iterator(); it.hasNext();) {
				ITransactionListener listener = it.next();
				try{
					listener.handleCommit(db);
				} catch(Exception e) {
					logWarn("ITransactionListener threw exception in handleCommit:", e);
				}
			}
		}

		protected void handleRollback(DB db) {
			if (transactionListeners_==null) {
				//  nobody to be notified
				return;
			}
			for (Iterator<ITransactionListener> it = transactionListeners_.iterator(); it.hasNext();) {
				ITransactionListener listener = it.next();
				try{
					listener.handleRollback(db);
				} catch(Exception e) {
					logWarn("ITransactionListener threw exception in hanldeRollback:", e);
				}
			}
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

	protected DBSession getDBSession() {
		DBManager dbm = getData().getManager();
		if (isLogDebugEnabled() && dbm == null) logDebug("DB manager ist null.", null); 
		return (dbm == null) ? null : dbm.getDbSession();
	}
	
	@Override
	public EntityManager getCurrentEntityManager() {
		DBImpl current = getInstance(true);
		DBManager dbm = current.getData().getManager();
		if (isLogDebugEnabled() && dbm == null) logDebug("DB manager ist null.", null); 
		return (dbm == null) ? null : dbm.getDbSession().getEntityManager();
	}

	protected void setManager(DBManager manager) {
		getData().setManager(manager);
	}

	DBManager getDBManager() {
		return getData().getManager();
	}

	boolean isConnectionOpen() {
		if ( (getData().getManager() == null) 
				|| (getData().getManager().getDbSession() == null) ) {
			return false;
		}
		return getData().getManager().getDbSession().isOpen();
	}
	
	DBTransaction getTransaction() {
		if ( (getData().getManager() == null) 
				|| (getData().getManager().getDbSession() == null) ) {
			return null;
		}
		return getData().getManager().getDbSession().getTransaction();
	}

	private void createSession() {
		DBSession dbs = getDBSession();
		if (dbs == null) {
			if (isLogDebugEnabled()) logDebug("createSession start...", null);
			EntityManager em = null;
			Codepoint.codepoint(DBImpl.class, "initializeSession");
			if (isLogDebugEnabled()) logDebug("initializeSession", null);
			try {
				em = emf.createEntityManager();
			} catch (HibernateException e) {
				logError("could not open database session!", e);
			}
			setManager(new DBManager(em));
			getData().resetAccessCounter();
			getData().resetCommitCounter();
		} else if (!dbs.isOpen()) {
			EntityManager em = null;
			try {
				em = emf.createEntityManager();
			} catch (HibernateException e) {
				logError("could not open database session!", e);
			}
			setManager(new DBManager(em));
			getData().resetAccessCounter();
			getData().resetCommitCounter();
		}
		setInitialized(true);
		if (isLogDebugEnabled()) logDebug("createSession end...", null);
	}


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
		if (isConnectionOpen() && hasTransaction() && getTransaction().isInTransaction() && !getTransaction().isRolledBack()) {
			if (Settings.isJUnitTest()) {
				if (isLogDebugEnabled()) logDebug("Call commit", null);
				getTransaction().commit(); 
				getData().handleCommit(this);
			} else {
				if (isLogDebugEnabled()) logDebug("Call commit", null);
				throw new AssertException("Close db-session with un-committed transaction");
			}
		}
		DBSession s = getDBSession();
		if (s != null) {
			Codepoint.codepoint(DBImpl.class, "closeSession");
			s.close();
			// OLAT-3652 related: on closeSession also set the transaction to null
			s.clearTransaction();
		}
		
		data.remove();
	}
	
	public void cleanUpSession() {
		if(data.get() == null) return;
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
		
		//OLAT-3621: paranoia check for error state: we need to catch errors at the earliest point possible. OLAT-3621 has a suspected situation
		//           where an earlier transaction failed and didn't clean up nicely. To check this, we introduce error checking in getInstance here
		DBTransaction transaction = INSTANCE.getTransaction();
		if (transaction!=null) {		
			// Filter Exception form async TaskExecutorThread, there are exception allowed
			if (transaction.isError() && !Thread.currentThread().getName().equals("TaskExecutorThread")) {
				INSTANCE.logWarn("getInstance: Transaction (still?) in Error state: "+transaction.getError(), new Exception("DBImpl begin transaction)", transaction.getError()));
			}
		}  
		
		// if module is not active we return a non-initialized instance and take
		// care that
		// the only cleanup-calls to db.closeSession do nothing
		if (initialize) {
			INSTANCE.createSession();
		}
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

    
  /**
   * Call this to begin a transaction .
   * @param logObject TODO
   */
  private void beginTransaction(Object logObject) {
	//OLAT-3621: paranoia check for error state: we need to catch errors at the earliest point possible. OLAT-3621 has a suspected situation
	//           where an earlier transaction failed and didn't clean up nicely. To check this, we introduce error checking in getInstance here
	DBTransaction transaction = INSTANCE.getTransaction();
	if (transaction!=null) {		
		// Filter Exception form async TaskExecutorThread, there are exception allowed
		if (transaction.isError() && !Thread.currentThread().getName().equals("TaskExecutorThread")) {
			INSTANCE.logWarn("beginTransaction: Transaction (still?) in Error state: "+transaction.getError(), new Exception("DBImpl begin transaction)", transaction.getError()));
		}
	}  
	createSession();
	
  	// TODO: 07.01.2009/cg ONLY FOR DEBUGGING 'too many db access in one transaction'
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
    if(isLogDebugEnabled()) {
    	logDebug("beginTransaction TEST getDBSession()=" + getDBSession(), null);
    }
    
    if (!hasTransaction() ) {
      // if no transaction exists, start a new one.
      getDBSession().beginDbTransaction();
      if(isLogDebugEnabled()) {
    	  logDebug("No transaction exists, start a new one.", null);
      }
    } else if (getTransaction() != null && getTransaction().isRolledBack() ) {
    	logError("Call beginTransaction but transaction is already rollbacked",null);
    }
    if(isLogDebugEnabled()) {
    	logDebug("beginTransaction TEST hasTransaction()=" + hasTransaction(), null);
    }
  }
  
	private boolean contains(Object object) {
		return getDBManager().contains(object);
	}

	/**
	 * Create a DBQuery
	 * 
	 * @param query
	 * @return DBQuery
	 */
	public DBQuery createQuery(String query) {
		beginTransaction(query);
		return getDBManager().createQuery(query);
	}

	/**
	 * Delete an object.
	 * 
	 * @param object
	 */
	public void deleteObject(Object object) {
		beginTransaction(object);
		getDBManager().deleteObject(getTransaction(), object);
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
		beginTransaction(query);
		return getDBManager().delete(getTransaction(), query, value, type);
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
		beginTransaction(query);
		return getDBManager().delete(getTransaction(), query, values, types);
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
		beginTransaction(query);
		return getDBManager().find(getTransaction(), query, value, type);
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
		beginTransaction(query);
		return getDBManager().find(getTransaction(), query, values, types);
	}

	/**
	 * Find objects based on query
	 * 
	 * @param query
	 * @return List of results.
	 */
	public List find(String query) {
		beginTransaction(query);
		return getDBManager().find(getTransaction(), query);
	}

	/**
	 * Find an object.
	 * 
	 * @param theClass
	 * @param key
	 * @return Object, if any found. Null, if non exist. 
	 */
	public <U> U findObject(Class<U> theClass, Long key) {
		beginTransaction(key);
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
		beginTransaction(key);
		return getDBManager().loadObject(getTransaction(), theClass, key);
	}

	/**
	 * Save an object.
	 * 
	 * @param object
	 */
	public void saveObject(Object object) {
		beginTransaction(object);
		getDBManager().saveObject(getTransaction(), object);
	}

	/**
	 * Update an object.
	 * 
	 * @param object
	 */
	public void updateObject(Object object) {
		beginTransaction(object);
		getDBManager().updateObject(getTransaction(), object);
	}

	/**
	 * Get any errors from a previous DB call.
	 * 
	 * @return Exception, if any.
	 */
	public Exception getError() {
		if (hasTransaction()) {
			return getTransaction().getError();
		} else {
			return getDBManager().getLastError();
		}
	}

	/**
	 * @return True if any errors occured in the previous DB call.
	 */
	public boolean isError() {
		if (hasTransaction()) {
			return getTransaction().isError();
		} else {
			return getDBManager()==null ? false : getDBManager().isError();
		}

	}

	boolean hasTransaction() {
		return null == getTransaction() ? false : getTransaction().isInTransaction();
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
		beginTransaction(persistable);
		Persistable ret;
		Class theClass = persistable.getClass();
		if (forceReloadFromDB) {
			// we want to reload it from the database.
			// there are 3 scenarios possible:
			// a) the object is not yet in the hibernate cache
			// b) the object is in the hibernate cache
			// c) the object is detached and there is an object with the same id in the hibernate cache
			
			if (contains(persistable)) {
				// case b - then we can use evict and load
				getDBManager().evict(persistable);
				return (Persistable) loadObject(theClass, persistable.getKey());
			} else {
				// case a or c - unfortunatelly we can't distinguish these two cases
				// and session.refresh(Object) doesn't work.
				// the only scenario that works is load/evict/load
				Persistable attachedObj = (Persistable) loadObject(theClass, persistable.getKey());
				getDBManager().evict(attachedObj);
				return (Persistable) loadObject(theClass, persistable.getKey());
			}
		} else if (!contains(persistable)) { 
			// forceReloadFromDB is false - hence it is OK to take it from the cache if it would be there
			// now this object directly is not in the cache, but it's possible that the object is detached
			// and there is an object with the same id in the hibernate cache.
			// therefore the following loadObject can either return it from the cache or load it from the DB
			return (Persistable) loadObject(theClass, persistable.getKey());
		} else { 
			// nothing to do, return the same object
			return persistable;
		}
	}

	@Override
	public void commitAndCloseSession() {
		try {
			if (needsCommit()) {
				commit();
			}
		} finally {
			try{
				// double check: is the transaction still open? if yes, is it not rolled-back? if yes, do a rollback now!
				if (isConnectionOpen() && hasTransaction() && getTransaction().isInTransaction() && !getTransaction().isRolledBack()) {
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
//		StackTraceElement[] elems = Thread.currentThread().getStackTrace();
//		if (elems!=null && elems.length>1) {
//			if (!elems[2].getClassName().equals("org.olat.commons.coordinate.cluster.ClusterSyncer") &&
//					!elems[2].getClassName().equals("org.olat.core.util.mtx.ManagedTransaction")) {
//				System.out.println("--> TRANSACTION RULES BROKEN");
//			}
//		}
		if (isLogDebugEnabled()) logDebug("commit start...", null);
		try {
			if (isConnectionOpen() && hasTransaction() && getTransaction().isInTransaction()) {
				if (isLogDebugEnabled()) logDebug("has Transaction and is in Transaction => commit", null);
				getData().incrementCommitCounter();
				if ( isLogDebugEnabled() ) {
					if ((maxCommitCounter != 0) && (getData().getCommitCounter() > maxCommitCounter) ) {
						logInfo("Call too many commit in a db-session, commitCounter=" + getData().getCommitCounter() +"; could be a performance problem" , null);
					}
				}
				getTransaction().commit();
				getData().handleCommit(this);
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
				if (hasTransaction() && !getTransaction().isRolledBack()) {
					getTransaction().rollback();
					getData().handleRollback(this);
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
			if (isConnectionOpen() && hasTransaction() && getTransaction().isInTransaction() ) {
				if (isLogDebugEnabled()) logDebug("Call rollback", null);
				getTransaction().rollback();
				getData().handleRollback(this);
			}
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
	 * Register StatisticsService as MBean for JMX support.
	 * @param mySessionFactory
	 */
	private void registerStatisticsServiceAsMBean(SessionFactory mySessionFactory) {
		if (mySessionFactory == null) {
			throw new AssertException("Can not register StatisticsService as MBean, SessionFactory is null");
		}
    try {
	    Hashtable<String, String> tb = new Hashtable<String, String>();
	    tb.put("type", "statistics");
	    tb.put("sessionFactory", "HibernateStatistics");
	    ObjectName on = new ObjectName("org.olat.core.persistance", tb);
	    MBeanServer server = (MBeanServer) CoreSpringFactory.getBean(MBeanServerFactoryBean.class);
	    StatisticsService stats = new StatisticsService();
	    stats.setSessionFactory(mySessionFactory);
	    server.registerMBean(stats, on);
    } catch (MalformedObjectNameException e) {
        logWarn("JMX-Error : Can not register as MBean, MalformedObjectNameException=", e);
    } catch (InstanceAlreadyExistsException e) {
        logWarn("JMX-Error : Can not register as MBean, InstanceAlreadyExistsException=", e);
    } catch (MBeanRegistrationException e) {
        logWarn("JMX-Error : Can not register as MBean, MBeanRegistrationException=", e);
    } catch (NotCompliantMBeanException e) {
        logWarn("JMX-Error : Can not register as MBean, NotCompliantMBeanException=", e);
    } catch (NoSuchBeanDefinitionException e) {
    	logWarn("JMX-Error : Can not register as MBean, NoSuchBeanDefinitionException=", e);
    }
	}

	/**
	 * @see org.olat.core.commons.persistence.DB#intermediateCommit()
	 */
	public void intermediateCommit() {
		this.commit();
		getData().handleCommit(this);
		this.closeSession();
	}
	
	public void addTransactionListener(ITransactionListener listener) {
		getData().addTransactionListener(listener);
	}
	
	public void removeTransactionListener(ITransactionListener listener) {
		getData().removeTransactionListener(listener);
	}

	/**
	 * @see org.olat.core.commons.persistence.DB#needsCommit()
	 */
	public boolean needsCommit() {
		// see closeSession() and OLAT-4318: more robustness with commit/rollback/close, therefore
		// we check if the connection is open at this stage at all
		return isConnectionOpen() && hasTransaction() && !getTransaction().isRolledBack() && getTransaction().isInTransaction();
	}

	/** temp debug only **/
	public void forceSetDebugLogLevel(boolean enabled) {
		if (!enabled) {
			forcedLogger = null;
			return;
		}
		forcedLogger = new OLog() {

			public void audit(String logMsg) {
				Tracing.logAudit(logMsg, DBImpl.class);
			}

			public void audit(String logMsg, String userObj) {
				Tracing.logAudit(logMsg, userObj, DBImpl.class);
			}

			public void debug(String logMsg, String userObj) {
				Tracing.logInfo(logMsg, userObj, DBImpl.class);
			}

			public void debug(String logMsg) {
				Tracing.logInfo(logMsg, DBImpl.class);
			}

			public void error(String logMsg, Throwable cause) {
				Tracing.logError(logMsg, cause, DBImpl.class);
			}

			public void error(String logMsg) {
				Tracing.logError(logMsg, DBImpl.class);
			}

			public void info(String logMsg, String userObject) {
				Tracing.logInfo(logMsg, userObject, DBImpl.class);
			}

			public void info(String logMsg) {
				Tracing.logInfo(logMsg, DBImpl.class);
			}

			public boolean isDebug() {
				return true;
			}

			public void warn(String logMsg, Throwable cause) {
				Tracing.logWarn(logMsg, cause, DBImpl.class);
			}

			public void warn(String logMsg) {
				Tracing.logWarn(logMsg, DBImpl.class);
			}
			
		};
	}
	
	protected OLog getLogger() {
		if (forcedLogger==null) {
			return super.getLogger();
		} else {
			return forcedLogger;
		}
	}
	
	/**
	 * [used by spring]
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		//this.sessionFactory = sessionFactory;
	}
	
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
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
				Tracing.logError("Could not unregister database driver.", e, this.getClass());
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
		
		beginTransaction(queryName);
		return getDBManager().createNamedQuery(queryName, dbVendor, vendorSpecific);
	}	
}
