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
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.filters.VFSItemFilter;


/**
 * <P>
 * Initial Date:  23.06.2005 <br>
 *
 * @author Felix Jost
 */
public abstract class AbstractVirtualContainer implements VFSContainer {

	private final String name;
	protected VFSItemFilter defaultFilter;

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

	@Override
	public VFSStatus canDelete() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canRename() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canCopy() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus copyFrom(VFSItem vfsItem, Identity savedBy) {
		return VFSConstants.ERROR_FAILED;
	}

	@Override
	public VFSStatus copyContentOf(VFSContainer container, Identity savedBy) {
		return VFSConstants.NO;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getLastModified() {
		return VFSConstants.UNDEFINED;
	}

	@Override
	public VFSStatus rename(String newname) {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus delete() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus deleteSilently() {
		return VFSConstants.NO;
	}

	@Override
	public VFSContainer createChildContainer(String child) {
		return null;
	}

	@Override
	public VFSLeaf createChildLeaf(String child) {
		return null;
	}

	@Override
	public VFSStatus canMeta() {
		return VFSConstants.NO;
	}
	
	@Override
	public VFSStatus canVersion() {
		return VFSConstants.NO;
	}

	@Override
	public VFSMetadata getMetaInfo() {
		return null;
	}

	@Override
	public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
		this.defaultFilter = defaultFilter;
	}

	@Override
	public VFSItemFilter getDefaultItemFilter() {
		return this.defaultFilter;
	}
}

