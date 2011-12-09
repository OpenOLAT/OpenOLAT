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

import org.olat.core.id.OLATResourceable;

/**
 * Description:<br>
 * interface for synchronizing access to protected regions
 * 
 * <P>
 * Initial Date:  17.09.2007 <br>
 * @author felix
 */
public interface Syncer {

	/**
	 * guarantees that the code r is running serialized in regard to other code with the same resourceable
	 * 
	 * @param ores the resourceable to sync upon
	 * @param action the code to be executed in the protected region (it is executed synchronously, that is, within the same thread as the caller of this method)
	 * @return the Object that the SynceCallback returns (may be null). This can used to transfer results back to the caller. 
	 * 
	 */
	public <T> T doInSync(OLATResourceable ores, SyncerCallback<T> action);
	
	/**
	 * same as <code>doInSync(OLATResourceable ores, SyncerCallback<T> action)</code>, but without return object.
	 * @param ores
	 * @param action
	 */
	public void doInSync(OLATResourceable ores, SyncerExecutor action);

	/**
	 * Check if already in synchronized block for certain olat-resource
	 * @param ores
	 */
	public void assertAlreadyDoInSyncFor(OLATResourceable ores);
	
}
