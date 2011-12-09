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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.ITransactionListener;

/**
 * ThreadLock - Object 
 * responsible for the coordination of synchronized(syncObj),
 * findOrPersistPLock and DB.commit/DB.rollback.
 * <p>
 * Main raison d'etre of this class is the following problem:
 * <ul>
 *  <li>Thread A enters the doInSync block</li>
 *  <li>Thread A aquires plock of OLATResourceable-1</li>
 *  <li>Thread A leaves the doInSync block</li>
 *  <li>Thread B enters the doInSync block</li>
 *  <li>Thread B wants to aquire plock of OLATResourceable-1 but needs to wait since Thread A hasn't committed or rolled back yet</li>
 *  <li>Thread A (still uncommitted) enters the doInSync block again (what a nasty boy) but cannot enter the synchronized block since Thread B owns it</li>
 *  <li>hence we have a deadlock between a VM-Object-monitor and the Database</li>
 * </ul>
 * There are multiple ways of fixing this including the following:
 * <ul>
 *  <li>Introduced the long discussed ManagedBlock which would at the highest possible level manage
 *      transactions - i.e. only if a thread is inside a ManagedBlock (which can be assured via a ThreadLocal flag
 *      which the ManagedBlock can set) can any Database access be done - all other Database access would
 *      throw an AssertionError. The ManagedBlock would also do the actual commit - thus releasing any
 *      PLocks. The ManagedBlock could then take notice of the fact that a thread acquired the PLock
 *		and hasn't released it yet (also known as 'VM-side-plock-hashmap'). When another thread in 
 *      another ManagedBlock would want to acquire a PLock, the ManagedBlock could make sure that it
 *      doesn't hold the synchronization objct while waiting for the PLock - instead it could wait
 *      on a ManagedBlock side synchronization object or any other suitable thing.<br>
 *      The downside of the ManagedBlock is that its introduction into OLAT would require changes in 
 *      many places and is hence not the cheapest solution. Also, if the plock-synchronization coordination
 *      is the only reason for the ManagedBlock then it might be overkill.</li>
 *  <li>Make sure that the monitor on the DerivedStringSyncer's syncobj is not held while waiting for
 *      the plock - at least not if the plock is held by the same VM. This requires additional knowledge
 *      of which plocks are currently held in the VM. And that's exactly the job of this new PLockSyncCoordinator</li>
 * </ul>
 * The PLockSyncCoordinator keeps track of each plock that is acquired - by being informed by the 
 * ClusterSyncer about acquisition and by being informed by the DBImpl (via the newly introduced
 * ITransactionListener) about commit or rollbacks. As it 
 * <P>
 * Initial Date:  19.08.2008 <br>
 * @author Stefan
 */
public class PLockSyncCoordinator implements ITransactionListener {

	private static final Map<String,PLockSyncCoordinator> plocks_ = new HashMap<String, PLockSyncCoordinator>();
	
	private final Set<String> acquiredPLocks_ = new HashSet<String>();
	
	private final Thread th_;
	
	public PLockSyncCoordinator() {
		th_ = Thread.currentThread();
	}
	
	public void plockAcquiring(String asset) {
		org.olat.core.logging.Tracing.logWarn("plockAcquiring("+asset+") check... ", this.getClass());
		// look in the vm-wide plocks_ map first - as this is more common
		synchronized(plocks_) {
			while(true) {
				PLockSyncCoordinator coordinator = plocks_.get(asset);
				if (coordinator==this) {
					// then we already acquired the plock. nothing more to do
					return;
				} else if (coordinator==null) {
					// great, you're the only one in this VM to grab this plock - go ahead!
					org.olat.core.logging.Tracing.logWarn("plockAcquiring("+asset+") OK ", this.getClass());
					acquiredPLocks_.add(asset);
					plocks_.put(asset, this);
					DBFactory.getInstance(false).addTransactionListener(this);
					return;
				} else {
					org.olat.core.logging.Tracing.logWarn("waitForAssetReleased("+asset+")", this.getClass());
					try {
						plocks_.wait(250);
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
		}
	}

	private void clearAllPLocks(DB db) {
		org.olat.core.logging.Tracing.logWarn("clearAllPLocks()", this.getClass());
		db.removeTransactionListener(this);
		synchronized(plocks_) {
			for (Iterator<String> it = acquiredPLocks_.iterator(); it.hasNext();) {
				String asset = it.next();
				org.olat.core.logging.Tracing.logWarn("clearAllPLocks: "+asset+" ...", this.getClass());

				PLockSyncCoordinator coordinator = plocks_.remove(asset);
				if (coordinator!=this) {
					// then houston we have a problem
					org.olat.core.logging.Tracing.logError("clearAllPLocks: "+asset+" HAD WRONG COORDINATOR!!!!! wanted "+this+" had "+coordinator, this.getClass());
				}
			}
			acquiredPLocks_.clear();
			plocks_.notifyAll();
		}
	}
	
	public void handleCommit(DB db) {
		if (th_!=Thread.currentThread()) {
			org.olat.core.logging.Tracing.logError("handleCommit: WRONG THREAD...!!!!! wanted "+th_+" had "+Thread.currentThread(), this.getClass());
		}
		clearAllPLocks(db);
	}

	public void handleRollback(DB db) {
		if (th_!=Thread.currentThread()) {
			org.olat.core.logging.Tracing.logError("handleCommit: WRONG THREAD...!!!!! wanted "+th_+" had "+Thread.currentThread(), this.getClass());
		}
		clearAllPLocks(db);
	}

}
