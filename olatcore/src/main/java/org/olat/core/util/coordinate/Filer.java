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
* Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.util.coordinate;

import org.olat.core.util.coordinate.filer.FileTransaction;

/**
 * Description:<br>
 * cluster:: this is work in progress
 * @deprecated work in progress
 * a helper to organize files.
 * a FileTransaction is essentially a file system directory without the user knowing the absolute path.
 * <br><br>
 * a FileTransaction is a unit of work (as a db transaction) that can either be committed or rolled back.
 * this is a very very simple form of a transactional filesystem.
 * <br><br>
 * a FileTransaction does however -not- provide<br>
 * - a locking utility<br>
 * - quota management <br>
 * - a vfs (virtual file system)<br>
 * - synchronizing access to files and directories 
 * <P>
 * Initial Date:  09.11.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface Filer {
	
	/**
	 * 
	 * @return the FileTransaction associated with the current thread.
	 * make sure that at the end of dispatching, either commit() or rollback() is called, so that the FileTransaction is in a determined state for the next thread that uses it.
	 */
	public FileTransaction getCurrentFileTransaction();
}
