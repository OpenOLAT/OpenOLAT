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

	private final List<VFSItem> children = new ArrayList<>();
	private VFSSecurityCallback secCallback = null;
	private VFSContainer parentContainer;
		
	public VirtualContainer(String name) {
		super(name);
	}	
	
	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	/**
	 * Add a VFSItem to this CirtualContainer.
	 * @param vfsItem
	 */
	public void addItem(VFSItem vfsItem) {
		children.add(vfsItem);
	}

	@Override
	public List<VFSItem> getItems() {
		return children;
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if (filter == null) {
			return children;
		} else {
			List<VFSItem> filtered = new ArrayList<>(children.size());
			for (VFSItem vfsItem : children) {
				if (filter.accept(vfsItem)) {
					filtered.add(vfsItem);
				}
			}
			return filtered;
		}
	}

	@Override
	public VFSStatus canWrite() {
		return VFSConstants.NO;
	}

	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return secCallback;
	}

	@Override
	public VFSContainer getParentContainer() {
		return parentContainer;
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		return (this == vfsItem);
	}

	@Override
	public String getRelPath() {
		return null;
	}

	@Override
	public boolean isInPath(String path) {
		return false;
	}

	@Override
	public VFSItem resolve(String path) {
		if(path != null && path.length() > 1 && path.startsWith("/")) {
			String childName = VFSManager.extractChild(path);
			String nextPath = path.substring(childName.length() + 1);
			// simple optimized case
			for (VFSItem container:children) {
				if (container.getName().equals(childName)) {
					VFSItem vfsItem = container.resolve(nextPath);
					// set default filter on resolved file if it is a container
					if (vfsItem != null && vfsItem instanceof VFSContainer) {
						VFSContainer resolvedContainer = (VFSContainer) vfsItem;
						resolvedContainer.setDefaultItemFilter(defaultFilter);
					}
					return vfsItem;
				}
			}
		}
		return VFSManager.resolveFile(this, path);
	}

	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		this.secCallback = secCallback;
	}

	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		this.parentContainer = parentContainer;
	}
}