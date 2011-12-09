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
package org.olat.commons.coordinate.singlevm;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.Syncer;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.coordinate.util.DerivedStringSyncer;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description:<br>
 * implementation of the Syncer for the singleVM mode.
 * 
 * <P>
 * Initial Date: 17.09.2007 <br>
 * 
 * @author felix
 * @deprecated USE ONLY ClusterSyncer because SingleVMSyncer synchronized only the execute block, ClusterSyncer synchronized until commit
 * 
 */
public class SingleVMSyncer implements Syncer {

	private final ThreadLocal<ThreadLocalSingleVMSyncer> data = new ThreadLocal<ThreadLocalSingleVMSyncer>();

	/**
	 * constructed only by the SingleVMCoordinator
	 *
	 */
	private SingleVMSyncer() {
		Tracing.logWarn("!!! SingleVMSyncer is deprecated, USE ONLY ClusterSyncer !!!",this.getClass());
	}
	
	/**
	 * @see org.olat.core.util.coordinate.Syncer#doInSync(org.olat.core.id.OLATResourceable,
	 *      java.lang.Runnable)
	 */
	public <T> T doInSync(OLATResourceable ores, SyncerCallback<T> callback) {
		Codepoint.hierarchicalCodepoint(SingleVMSyncer.class, "doInSync-before-sync", 1);
		try{
			synchronized (DerivedStringSyncer.getInstance().getSynchLockFor(ores)) { 
				Codepoint.hierarchicalCodepoint(SingleVMSyncer.class, "doInSync-in-sync",1 );
				getData().incrementAndCheckNestedLevelCounter();
				T res;
				try {
					//o_clusterOK by:fj: since by definition we are in singlevm mode here
					// cluster::::: change to also using pessimistic lock since db.beginSingleTransaction was removed
					res = callback.execute();
					// o_clusterOK by:cg fixed by using ClusterSyncer for single-vm mode too
					//  [   commit can be removed after redesign for clustering, 
					//      or instead of SingleVMSyncer use MultiVMSyncer
					//      Commit need as workaround for SingleVMMode, because lock must be hold to commit call
					//      DerivedStringSyncer holds the lock only in this synchronized-block !
					//      => Refactor using Coordinator.doInManagedBlock() {.....} to control commit/rollback 
					//  ]
				} finally {
					getData().decrementNestedLevelCounter();				
				}
				DBFactory.getInstance().commit();
				return res;
			}
		} finally {
			Codepoint.hierarchicalCodepoint(SingleVMSyncer.class, "doInSync-after-sync",1);
		}
	}
	
	public void doInSync(OLATResourceable ores, SyncerExecutor executor) {
		Codepoint.hierarchicalCodepoint(SingleVMSyncer.class, "doInSync-before-sync", 1);
		try{
			synchronized (DerivedStringSyncer.getInstance().getSynchLockFor(ores)) { 
				Codepoint.hierarchicalCodepoint(SingleVMSyncer.class, "doInSync-in-sync",1 );
				//o_clusterOK by:fj: since by definition we are in singlevm mode here
				// cluster::::: change to also using pessimistic lock since db.beginSingleTransaction was removed
				executor.execute();
				// o_clusterOK by:cg fixed by using ClusterSyncer for single-vm mode too
				//  [   commit can be removed after redesign for clustering, 
				//      or instead of SingleVMSyncer use MultiVMSyncer
				//      Commit need as workaround for SingleVMMode, because lock must be hold to commit call
				//      DerivedStringSyncer holds the lock only in this synchronized-block !
				//      => Refactor using Coordinator.doInManagedBlock() {.....} to control commit/rollback 
				//  ]
				DBFactory.getInstance().commit();
			}
		} finally {
			Codepoint.hierarchicalCodepoint(SingleVMSyncer.class, "doInSync-after-sync",1);
		}
	}

	public void assertAlreadyDoInSyncFor(OLATResourceable ores) {
		if (!getData().isEquals(ores) || (getData().getNestedLevel() == 0) ) {
			throw new AssertException("This method must be called from doInSync block with ores=" + ores);
		}
	}

	private void setData(ThreadLocalSingleVMSyncer data) {
		this.data.set(data);
	}

	private ThreadLocalSingleVMSyncer getData() {
		ThreadLocalSingleVMSyncer tld = data.get();
		if (tld == null) {
			tld = new ThreadLocalSingleVMSyncer();
			setData(tld);
		}
		return tld;
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
	private class ThreadLocalSingleVMSyncer {
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
		
		protected void setSyncObject(OLATResourceable ores) {
			this.ores = ores;
		}
		
		protected boolean isEquals(OLATResourceable ores) {
			if (!this.ores.getResourceableTypeName().equals(ores.getResourceableTypeName())) {
				return false;
			}
			if (!this.ores.getResourceableId().equals(ores.getResourceableId())) {
				return false;
			}
			return true;
		}
	}

}
