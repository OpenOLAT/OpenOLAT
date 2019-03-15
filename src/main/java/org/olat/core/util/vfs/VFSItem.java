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

package org.olat.core.util.vfs;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;


/**
 * <P>
 * Initial Date:  23.06.2005 <br>
 *
 * @author Felix Jost
 */
public interface VFSItem {
	
	/**
	 * a relative path. the implementation will search all its children. 
	 * e.g. bla/blu/bli.txt this vfsitem's name must thus be 'bla'
	 * 
	 * @param path
	 * @return
	 * 
	 */
	public VFSItem resolve(String path);
	

	/**
	 * Get the parent of this vfsItem.
	 * @return
	 */
	public VFSContainer getParentContainer();

	/**
	 * Sets the parent container.
	 * -> DO NOT CALL THIS METHOD... USED ONLY INTERNALLY!
	 * 
	 * @param parentContainer
	 */
	public void setParentContainer(VFSContainer parentContainer);
	
	/**
	 * The relative path is normalized the following way: if the file is not in
	 * "bcroot" the returned path is null, the path starts always with "/", directories
	 * doesn't have a leading "/".
	 * 
	 * @return The relative path to "bcroot" or null if the file is not located in "bcroot". 
	 */
	public String getRelPath();

	/**
	 * @param newname e.g test.txt or myfolder (no path prepended)
	 * @return status
	 */
	public VFSStatus rename(String newname);
	
	/**
	 * Deletes the item. If the item is a container, all children will be deleted recursively
	 * @return status
	 */
	public VFSStatus delete();
	
	/**
	 * Delete the items and by pass versioning if enabled
	 * @return
	 */
	public VFSStatus deleteSilently();

	/**
	 * @return True if the file exists
	 */
	public boolean exists();
	
	/**
	 * @return if can write
	 */
	public VFSStatus canRename();

	/**
	 * @return if can delete
	 */
	public VFSStatus canDelete();

	/**
	 * @return the name
	 * 
	 */
	public String getName();
	
	/**
	 * Return the last modified date of this item or -1 if not available.
	 * @return
	 */
	public long getLastModified();
	
	/**
	 * @return true if this container (and all its children recursively) can be copied to some other place. 
	 * this is normally true for containers with a physical
	 * implementation, but not for virtual/named containers
	 */
	public VFSStatus canCopy();
	
	/**
	 * 
	 * @return true if a child can be added at all(only files and folder cab be copied to). the operation may still fail because e.g. of quota limitation.
	 */
	public VFSStatus canWrite();
	
	/**
	 * @return true if the file has or can save some metadata informations
	 */
	public VFSStatus canMeta();
	
	/**
	 * @return The metadata if the item can metadata or null if not
	 */
	public VFSMetadata getMetaInfo();
	
	/**
	 * Get the local security callback for this item.
	 * NOTE: Usually you would not need to access local security callbacks
	 * directly. You'd rather want to get the inherited security callback
	 * of this item via getInheritedSecurityCallback. This will get you
	 * the local security callback if set aswell but also any inherited
	 * security callbacks from parent containers.
	 * @see VFSItem#getInheritedSecurityCallback()
	 * @return SecurityCallback if any, or null if not set.
	 */
	public VFSSecurityCallback getLocalSecurityCallback();
	
	/**
	 * Set a custom security callback for this item.
	 * @param secCallback
	 */
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback);
	
	/**
	 * Test if this is the same item as ourselves.
	 * 
	 * @param vfsItem
	 * @return
	 */
	public boolean isSame(VFSItem vfsItem);

}

