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
 * <p>
 */
package org.olat.commons.coordinate.singlevm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.SignOnOffEvent;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.LockEntry;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.coordinate.LockResultImpl;
import org.olat.core.util.coordinate.Locker;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * implementation of the olat system bus within one vm
 * 
 * <P>
 * Initial Date: 19.09.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
// Must be abstract because Spring configuration of method 'getPersistentLockManager' :
// to avoid circular reference method lookup is used for dependecy injection of persistent lock manager
public abstract class SingleVMLocker implements Locker, GenericEventListener {
	private static final OLog log = Tracing.createLoggerFor(SingleVMLocker.class);
	
	// lock for the persistent lock manager
	private final Object PERS_LOCK = new Object();

	private Map<String, LockEntry> locks = new HashMap<String, LockEntry>(); // key, lockentry

	private EventBus eventBus;
	
	/**
	 * [spring only]
	 */
	protected SingleVMLocker() {
		//
	}
	
	/**
	 * [used by spring]
	 */
	public void init() {
		eventBus.registerFor(this, null,
				OresHelper.createOLATResourceableType(UserSession.class));
	}

	/**
	 * [used by spring]
	 * @param eventBus
	 */
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/**
	 * @param ores the OLATResourceable to lock upon, e.g a repositoryentry or such
	 * @param identity the identity who tries to acquire the lock
	 * @param locksubkey null or any string to lock finer upon the resource (e.g. "authors", or "write", ...)  
	 * @return lock result
	 */
	public LockResult acquireLock(OLATResourceable ores, Identity identity, String locksubkey) {
		String derivedLockString = OresHelper.createStringRepresenting(ores, locksubkey);
		return acquireLock(derivedLockString, identity);
	}

	/**
	 * releases the lock. can also be called if the lock was not sucessfully
	 * acquired
	 * 
	 * @param le the LockResult received when locking
	 */
	public void releaseLock(LockResult lockResult) {
		// if the lock has not been acquired, then nothing is to be released -
		// return silently to make cleaning up easier
		if (!lockResult.isSuccess()) {
			return;
		}
		LockEntry le = ((LockResultImpl) lockResult).getLockEntry();
		releaseLockEntry(le);
	}

	/**
	 * @param ores
	 * @param locksubkey
	 * @return if the olatresourceable with the subkey is already locked by
	 *         someone (returns true even if locked by "myself")
	 */
	public boolean isLocked(OLATResourceable ores, String locksubkey) {
		String derivedLockString = OresHelper.createStringRepresenting(ores, locksubkey);
		return isLocked(derivedLockString);
	}

	private boolean isLocked(Object key) { 
		synchronized (locks) { //o_clusterOK by:fj, by definition we are in singleVM mode
			return locks.get(key) != null;
		}
	}

	/**
	 * aquires a lock
	 * 
	 * @param identity
	 *            the identity who wishes to obtain the lock
	 * @return the lockresult
	 */
	private LockResult acquireLock(String key, Identity identity) {
		LockResult lockResult;
		boolean logDebug = log.isDebug();
		synchronized (locks) { //o_clusterOK by:fj, by definition we are in singleVM mode
			LockEntry oldLockEntry = locks.get(key);
			if (oldLockEntry == null || identity.getName().equals(oldLockEntry.getOwner().getName())) {
				// no one has the lock aquired yet - or user reacquired its own
				// lock (e.g. in case of browser crash or such)
				LockEntry lockEntry = new LockEntry(key, System.currentTimeMillis(), identity);
				locks.put(key, lockEntry);
				lockResult = new LockResultImpl(true, lockEntry);
				if (logDebug) {
					String msg;
					if (oldLockEntry == null) {
						msg = "identity '" + identity.getName() + "' acquired lock on " + key;
					} else {
						msg = "identity '" + identity.getName() + "' re-acquired lock on " + key;
					}
					log.audit(msg);
				}
			} else {
				// already locked
				lockResult = new LockResultImpl(false, oldLockEntry);
			}
		}
		return lockResult;
	}

	public void releaseLockEntry(LockEntry lockEntry) {
		synchronized (locks) { //o_clusterOK by:fj, by definition we are in singleVM mode
			boolean removed = locks.values().remove(lockEntry);
			if (removed) {
				log.audit("identity '" + lockEntry.getOwner().getName() + "' released lock on "	+ lockEntry.getKey());
			} else {
				log.warn("identity '" + lockEntry.getOwner().getName() + "' tried to release lock a non-existing lock on "+ lockEntry.getKey());
			}
		}
	}

	/**
	 * receives all sign on / sign off events so it can release locks of users
	 * which have or are logged off
	 * 
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		SignOnOffEvent se = (SignOnOffEvent) event;
		if (!se.isSignOn()) { // it is a "logout" event - we are only interested in logout events
			String name = se.getIdentityName();
			// release all locks hold by the identity that has just logged out.
			synchronized (locks) { //o_clusterOK by:fj, by definition we are in singleVM mode
				for (Iterator<Entry<String, LockEntry>> iter = locks.entrySet().iterator(); iter.hasNext();) {
					Map.Entry<String, LockEntry> entry = iter.next();
					String key = entry.getKey();
					LockEntry le = entry.getValue();
					Identity owner = le.getOwner();
					if (owner.getName().equals(name)) {
						iter.remove();
						log.audit("identity '" + name + "' signed off and thus released lock on " + key);
					}
				}
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Lockmanager locks:" + locks.entrySet().toString();
	}

	
	public LockResult aquirePersistentLock(OLATResourceable ores, Identity ident, String locksubkey) {
		synchronized (PERS_LOCK) { //o_clusterOK by:fj, by definition we are in singleVM mode
			// delegate
			return getPersistentLockManager().aquirePersistentLock(ores, ident, locksubkey);
		}
	}

	/**
	 * 
	 * @see org.olat.core.util.coordinate.PersistentLockManager#releasePersistentLock(org.olat.core.util.coordinate.LockResult)
	 */
	public void releasePersistentLock(LockResult lockResult) {
		// if the lock has not been acquired, do nothing
		if (!lockResult.isSuccess())
			return;
		synchronized (PERS_LOCK) { //o_clusterOK by:fj, by definition we are in singleVM mode
			// delegate
			getPersistentLockManager().releasePersistentLock(lockResult);
		}
	}

	/* (non-Javadoc)
	 * @see org.olat.core.util.coordinate.Locker#adminOnlyGetLockEntries()
	 */
	public List<LockEntry> adminOnlyGetLockEntries() {
		return Collections.unmodifiableList(new ArrayList<LockEntry>(locks.values()));
	}

}
