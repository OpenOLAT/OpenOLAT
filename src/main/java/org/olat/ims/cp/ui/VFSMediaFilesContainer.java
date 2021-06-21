/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.cp.ui;

import java.util.List;

import org.olat.core.util.vfs.AbstractVirtualContainer;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * 
 * Description:<br>
 * This container has special isSame implementation
 * 
 * <P>
 * Initial Date:  4 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class VFSMediaFilesContainer extends AbstractVirtualContainer implements VFSContainer {
	
	private final VFSContainer rootContainer;
	private VFSSecurityCallback secCallback;

	
	public VFSMediaFilesContainer(String name, VFSContainer rootContainer) {
		super(name);
		this.rootContainer = rootContainer;
		this.rootContainer.setParentContainer(null);
	}
	
	@Override
	public boolean exists() {
		return rootContainer != null && rootContainer.exists();
	}
	
	@Override
	public boolean isHidden() {
		return rootContainer != null && rootContainer.isHidden();
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		return this == vfsItem || rootContainer.isSame(vfsItem);
	}
	
	@Override
	public VFSItem resolve(String path) {
		return rootContainer.resolve(path);
	}

	@Override
	public List<VFSItem> getItems() {
		return rootContainer.getItems();
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		return rootContainer.getItems(filter);
	}
	
	@Override
	public String getRelPath() {
		return rootContainer.getRelPath();
	}

	@Override
	public boolean isInPath(String path) {
		return rootContainer.isInPath(path);
	}

	@Override
	public VFSContainer getParentContainer() {
		return null;
	}

	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		//
	}

	@Override
	public VFSStatus canWrite() {
		return VFSConstants.NO;
	}
	
	@Override
	public VFSItemFilter getDefaultItemFilter() {
		return rootContainer.getDefaultItemFilter();
	}

	@Override
	public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
		rootContainer.setDefaultItemFilter(defaultFilter);
	}

	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return secCallback;
	}

	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		this.secCallback = secCallback;
	}
}
