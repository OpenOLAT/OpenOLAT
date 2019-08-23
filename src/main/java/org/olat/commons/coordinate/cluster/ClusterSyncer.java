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
package org.olat.commons.coordinate.cluster;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.lock.pessimistic.PessimisticLockManager;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.Syncer;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.coordinate.util.DerivedStringSyncer;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Description:<br>
 * cluster mode implementation of the Syncer
 * 
 * <P>
 * Initial Date:  21.09.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterSyncer implements Syncer {

	private final boolean isClusterEnabled;

	private static final Logger log = Tracing.createLoggerFor(ClusterSyncer.class);
	private int executionTimeThreshold = 3000; // warn if the execution takes longer than three seconds
	private final ThreadLocal<ThreadLocalClusterSyncer> data = new ThreadLocal<>();
	private final PessimisticLockManager pessimisticLockManager;
	private final DB dbInstance;
	
	/**
	 * [used by spring]
	 * @param pessimisticLockManager
	 */
	private ClusterSyncer(PessimisticLockManager pessimisticLockManager) {
		this.setPessimisticLockManager(pessimisticLockManager);
	}

	@Autowired
	private ClusterSyncer(PessimisticLockManager pessimisticLockManager, DB dbInstance, @Value("${cluster.mode}") String clusterMode) {
		this.pessimisticLockManager = pessimisticLockManager;
		this.dbInstance = dbInstance;
		this.isClusterEnabled = "Cluster".equals(clusterMode);
	}
	
	/**
	 * @see org.olat.core.util.coordinate.Syncer#doInSync(org.olat.core.id.OLATResourceable, org.olat.core.util.coordinate.SyncerCallback)
	 */
	public <T> T doInSync(OLATResourceable ores, SyncerCallback<T> callback) {
		getData().setSyncObject(ores);// Store ores-object for assertAlreadyDoInSyncFor(ores)
		String asset = OresHelper.createStringRepresenting(ores);
		
		// 1. sync on vm (performance and net bandwith reason, and also for a fair per-node handling of db request) 
		// cluster:::: measure throughput with/without this sync
		// : maybe also measure if with a n-Semaphore (at most n concurrent accesses) throughput incs or decs
		long start = 0;
		boolean isDebug = log.isDebugEnabled();
		if (isDebug) start = System.currentTimeMillis();
	

		T res;
		Object syncObj = DerivedStringSyncer.getInstance().getSynchLockFor(ores);
		synchronized (syncObj) {//cluster_ok is per vm only. this synchronized is needed for multi-core processors to handle 
 			                      // memory-flushing from registers correctly. without this synchronized you could have different
			                      // states of (instance-/static-)fields in different cores
			getData().incrementAndCheckNestedLevelCounter();

			try {
				// 2. sync on cluster
				// acquire a db lock with select for update which blocks other db select for updates on the same record
				// until the transaction is committed or rollbacked
				if (isClusterEnabled) {
					getPessimisticLockManager().findOrPersistPLock(asset);
				}
	
				// now execute the task, which may or may not contain further db queries.
				res = callback.execute();
			} finally {
				getData().decrementNestedLevelCounter();				
			}
			
			//clear the thread local
			if(getData().getNestedLevel() == 0) {
				data.remove();
			}

			// we used to not do a commit here but delay that to the end of the dispatching-process. the comment
			// was: "the lock will be released after calling commit at the end of dispatching-process
			//       needed postcondition after the servlet has finished the request: a commit or rollback on the db to release the lock.
			//       otherwise the database will throw a "lock wait timeout exceeded" message after some time and thus release the lock."
			// but realizing that this can a) cause long locking phases and b) deadlocks between VMs
			// we decided to do a commit here and work with its consequence which is that everything that happened
			// prior to the doInSync call is also committed. This though corresponds to the OLAT 6.0.x model and
			// was acceptable there as well.
			//
			// NB: Several services calling this method require a commit to avoid phantom reads!
			dbInstance.commit();
		}
		if (isDebug) {
			long stop = System.currentTimeMillis();
			if (stop-start > executionTimeThreshold) {
				log.warn("execution time exceeded limit of "+executionTimeThreshold+": "+(stop-start), new AssertException("generate stacktrace"));
			}
		}
		return res;
	}
	
	/**
	 * @see org.olat.core.util.coordinate.Syncer#doInSync(org.olat.core.id.OLATResourceable, org.olat.core.util.coordinate.SyncerExecutor)
	 */
	public void doInSync(OLATResourceable ores, final SyncerExecutor executor) {
		// call the other doInSync variant to avoid duplicate code here
		doInSync(ores, new SyncerCallback<Object>() {

			public Object execute() {
				executor.execute();
				return null;
			}
			
		});
		
	}

	/**
	 * @see org.olat.core.util.coordinate.Syncer#assertAlreadyDoInSyncFor(org.olat.core.id.OLATResourceable)
	 */
	@Override
	public void assertAlreadyDoInSyncFor(OLATResourceable ores) {
		if (getData().getSyncObject() == null || !getData().isEquals(ores) || (getData().getNestedLevel() == 0) ) {
			throw new AssertException("This method must be called from doInSync block with ores=" + ores);
		}
	}

	/**
	 * [used by spring]
	 * @param executionTimeThreshold
	 */
	public void setExecutionTimeThreshold(int executionTimeThreshold) {
		this.executionTimeThreshold = executionTimeThreshold;
	}

	private void setData(ThreadLocalClusterSyncer data) {
		this.data.set(data);
	}

	private ThreadLocalClusterSyncer getData() {
		ThreadLocalClusterSyncer tld = data.get();
		if (tld == null) {
			tld = new ThreadLocalClusterSyncer();
			setData(tld);
		}
		return tld;
	}

	public PessimisticLockManager getPessimisticLockManager() {
		return pessimisticLockManager;
	}

	//////////////
	// Inner class
	//////////////
	/**
	 * A <b>ThreadLocalClusterSyncer</b> is used as a central place to store data on a per
	 * thread basis.
	 * 
	 * @author Christian Guretzki
	 */
	private class ThreadLocalClusterSyncer {
		private int nestedLevelCounter = 0;
		private OLATResourceable ores;
		
		protected void incrementAndCheckNestedLevelCounter() {
			nestedLevelCounter++;
			if (nestedLevelCounter > 1) {
				nestedLevelCounter--;
				throw new AssertException("ClusterSyncer: nested doInSync is not allowed");
			}
		}
		
		public int getNestedLevel() {
			return nestedLevelCounter;
		}

		protected void decrementNestedLevelCounter() {
			nestedLevelCounter--;
			if (nestedLevelCounter < 0) {
				throw new AssertException("ClusterSyncer nestedLevelCounter could not be < 0, do not call decrementNestedLevelCounter twice");
			}
		}
		
		protected OLATResourceable getSyncObject() {
			return ores;
		}
		
		protected void setSyncObject(OLATResourceable ores) {
			this.ores = ores;
		}
		
		protected boolean isEquals(OLATResourceable res) {
			if (!ores.getResourceableTypeName().equals(res.getResourceableTypeName())) {
				return false;
			}
			if (!ores.getResourceableId().equals(res.getResourceableId())) {
				return false;
			}
			return true;
		}
	}
	
}
