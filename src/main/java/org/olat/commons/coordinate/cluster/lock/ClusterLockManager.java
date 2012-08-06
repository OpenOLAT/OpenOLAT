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
package org.olat.commons.coordinate.cluster.lock;

import java.util.List;

import org.hibernate.type.StandardBasicTypes;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;

/**
 * 
 * Description:<br>
 * Provides the database implementation for the Locker (used only in cluster mode)
 * 
 * <P>
 * Initial Date:  10.12.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterLockManager extends BasicManager {
	private static ClusterLockManager INSTANCE;
	
	/**
	 * [spring]
	 */
	private ClusterLockManager() {
		INSTANCE = this;
	}
	
	/**
	 * to be used only by the cluster package and subpackages 
	 * @return
	 */
	public static ClusterLockManager getInstance() {
		return INSTANCE;
	}
	
	LockImpl findLock(String asset) {
		Tracing.logInfo("findLock: "+asset+" START", getClass());
		DBQuery q = DBFactory.getInstance().createQuery(
				"select alock from org.olat.commons.coordinate.cluster.lock.LockImpl as alock inner join fetch alock.owner where alock.asset = :asset");
		q.setParameter("asset", asset);
		List res = q.list();
		if (res.size() == 0) {
			Tracing.logInfo("findLock: null END", getClass());
			return null; 
		} else {
			Tracing.logInfo("findLock: "+res.get(0)+" END", getClass());
			return (LockImpl) res.get(0);
		}
	}
		
	LockImpl createLockImpl(String asset, Identity owner) {
		Tracing.logInfo("createLockImpl: "+asset+" by "+ owner, getClass());
		return new LockImpl(asset, owner);
	}
	
	void saveLock(LockImpl alock) {
		Tracing.logInfo("saveLock: "+alock+" START", getClass());
		DBFactory.getInstance().saveObject(alock);
		Tracing.logInfo("saveLock: "+alock+" END", getClass());
	}

	void deleteLock(LockImpl li) {
		Tracing.logInfo("deleteLock: "+li+" START", getClass());
		DBFactory.getInstance().deleteObject(li);		
		Tracing.logInfo("deleteLock: "+li+" END", getClass());
	}
	
	@SuppressWarnings("unchecked")
	List<LockImpl> getAllLocks() {
		Tracing.logInfo("getAllLocks START", getClass());
		DBQuery q = DBFactory.getInstance().createQuery(
				"select alock from org.olat.commons.coordinate.cluster.lock.LockImpl as alock inner join fetch alock.owner");
		List<LockImpl> res = q.list();
		Tracing.logInfo("getAllLocks END. res.length:"+ (res==null ? "null" : res.size()), getClass());
		return res;
	}

	/**
	 * @param identName the name of the identity to release all locks for (only the non-persistent locks in cluster mode, -not- the persistent locks!)
	 */
	public void releaseAllLocksFor(String identName) {
		Tracing.logInfo("releaseAllLocksFor: "+identName+" START", getClass());
		Identity ident = BaseSecurityManager.getInstance().findIdentityByName(identName);
				
		DBFactory.getInstance().delete("from org.olat.commons.coordinate.cluster.lock.LockImpl as alock inner join fetch " +
				"alock.owner as owner where owner.key = ?", ident.getKey(), StandardBasicTypes.LONG);
		// cluster:: can we save a query (and is it appropriate considering encapsulation) 
		// here by saying: alock.owner as owner where owner.name = ? (using identName parameter)
		Tracing.logInfo("releaseAllLocksFor: "+identName+" END", getClass());
	}

}
