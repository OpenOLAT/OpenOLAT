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
package org.olat.core.util.coordinate.filer;

/**
 * Description:<br>
 * A fileTransaction is a unit of work which can be committed or rolled back. <br>
 * the work can consist of<br>
 * a) creating directories and putting files and subdirectories in it.<br>
 * or <br>
 * b) deleting directories (including recursively deleting subfiles and directories)<br>
 * <br><br>
 * Note: only directories that did not exist "in reality" can be rolled back, that is, only the creation of new directories is rollbackable,
 * not the adding of new files to existing directories.
 * <P>
 * Initial Date:  09.11.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface FileTransaction {
	/**
	 * 
	 * creates or returns a Directory representing a node/folder in the filesystem
	 * 
	 * @param ownerClass the manager class that is responsible for encapsulating -all- access to -all- files and folders which belong to the
	 * Directory (most often mapped by impls to a path such as /usr/local/olatdata/org.olat.MyManager_mysubnamespace/)
	 * @param subnamespace optional. subnamespace if more than one Directory is required for the same ownerClass. use only [a-z0-9] characters.
	 * @return the Directory associated with the ownerClass and the subnamespace
	 */
	public Directory getDirectory(Class ownerClass, String subnamespace) ;
	
	/**
	 * commits the current transaction, which means that 
	 * a) all Directories which reality-counterpart did not existed until now are now created (by renaming tmpfiles to the real names)<br>
	 * b) all deletes of directories are performed
	 *
	 */
	public void commit();
	
	/**
	 * rolls back the current transaction, which means that <br>
	 * a) deletion of directories will not be performed
	 * b) tmp files will be deleted right now (or, if the vm crashes, upon restart of the vm)
	 *
	 */
	public void rollback();
}
