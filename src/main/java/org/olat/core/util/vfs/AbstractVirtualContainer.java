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

import org.olat.core.util.vfs.filters.VFSItemFilter;


/**
 * Description:<br>
 * TODO: Felix Jost Class Description for VirtualContainer
 * 
 * <P>
 * Initial Date:  23.06.2005 <br>
 *
 * @author Felix Jost
 */
public abstract class AbstractVirtualContainer implements VFSContainer {

	private final String name;
	protected VFSItemFilter defaultFilter=null;

	/**
	 * @param name
	 */
	public AbstractVirtualContainer(String name) {
		this.name = name;
	}
	
	/**
	 * constructor for anynomous types
	 */
	public AbstractVirtualContainer() {
		this.name = null;
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSItem#canDelete()
	 */
	public VFSStatus canDelete() {
		return VFSConstants.NO;
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSItem#canRename()
	 */
	public VFSStatus canRename() {
		return VFSConstants.NO;
	}
	

	/**
	 * @see org.olat.core.util.vfs.VFSItem#canCopy()
	 */
	public VFSStatus canCopy() {
		return VFSConstants.NO;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#copyFrom(org.olat.core.util.vfs.VFSItem)
	 */
	public VFSStatus copyFrom(VFSItem vfsItem) {
		return VFSConstants.ERROR_FAILED;
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSItem#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSItem#getLastModified()
	 */
	public long getLastModified() {
		return VFSConstants.UNDEFINED;
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSItem#rename(java.lang.String)
	 */
	public VFSStatus rename(String newname) {
		throw new RuntimeException("unsupported");
	}


	/**
	 * @see org.olat.core.util.vfs.VFSItem#delete()
	 */
	public VFSStatus delete() {
		throw new RuntimeException("unsupported");
	}

	@Override
	public VFSStatus deleteSilently() {
		throw new RuntimeException("unsupported");
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#createChildContainer(java.lang.String)
	 */
	public VFSContainer createChildContainer(String name) {
		return null;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#createChildLeaf(java.lang.String)
	 */
	public VFSLeaf createChildLeaf(String name) {
		return null;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#setDefaultItemFilter(org.olat.core.util.vfs.filters.VFSItemFilter)
	 */
	public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
		this.defaultFilter = defaultFilter;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#getDefaultItemFilter()
	 */
	public VFSItemFilter getDefaultItemFilter() {
		return this.defaultFilter;
	}

}

