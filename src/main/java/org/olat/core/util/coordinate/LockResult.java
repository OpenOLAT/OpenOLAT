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
package org.olat.core.util.coordinate;

import org.olat.core.id.Identity;

/**
 * Description:<br>
 * Interface to describe the status after trying to acquire a Lock 
 * 
 * <P>
 * Initial Date:  19.09.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface LockResult {

	/**
	 * 
	 * @return true when the lock could be acquired, false when the lock was already acquired by another person
	 */
	public boolean isSuccess();
	
	/**
	 * @return true if the same user lock the same resource but in different window
	 */
	public boolean isDifferentWindows();
	
	/**
	 * 
	 * @return the owner of the lock.
	 */
	public Identity getOwner();
	
	/**
	 * 
	 * @return the time (as in System.currentTimeMilis()) when the lock was acquired.
	 */
	public long getLockAquiredTime();
	
	/**
	 * @return The lock entry if any
	 */
	public LockEntry getLockEntry();
}
