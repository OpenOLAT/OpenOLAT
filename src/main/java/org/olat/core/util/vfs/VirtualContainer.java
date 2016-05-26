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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;

public class VirtualContainer extends AbstractVirtualContainer {

	private List<VFSItem> children;
	private VFSSecurityCallback secCallback = null;
	private VFSContainer parentContainer;
		
	public VirtualContainer(String name) {
		super(name);
		children = new ArrayList<VFSItem>();
	}	
	
	@Override
	public boolean exists() {
		return true;
	}

	/**
	 * Add a VFSItem to this CirtualContainer.
	 * @param vfsItem
	 */
	public void addItem(VFSItem vfsItem) {
		children.add(vfsItem);
	}
	
	public List<VFSItem> getItems() {
		return children;
	}

	public List<VFSItem> getItems(VFSItemFilter filter) {
		if (filter == null) {
			return children;
		} else {
			List<VFSItem> filtered = new ArrayList<VFSItem>(children.size());
			for (VFSItem vfsItem : children) {
				if (filter.accept(vfsItem)) {
					filtered.add(vfsItem);
				}
			}
			return filtered;
		}
	}

	public VFSStatus canWrite() {
		return VFSConstants.NO;
	}

	public VFSSecurityCallback getLocalSecurityCallback() {
		return secCallback;
	}

	public VFSContainer getParentContainer() {
		return parentContainer;
	}

	public boolean isSame(VFSItem vfsItem) {
		return (this == vfsItem);
	}

	public VFSItem resolve(String path) {
		return VFSManager.resolveFile(this, path);
	}

	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		this.secCallback = secCallback;
	}

	public void setParentContainer(VFSContainer parentContainer) {
		this.parentContainer = parentContainer;
	}

}
