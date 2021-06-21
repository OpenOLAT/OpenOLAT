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

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.NamedLeaf;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.ims.cp.CPTreeDataModel;
import org.olat.ims.cp.ContentPackage;


/**
 * 
 * Description:<br>
 * This is an hybrid leaf - container as the CP allows a page to have children.
 * 
 * <P>
 * Initial Date:  5 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class VFSCPNamedContainerItem extends NamedLeaf implements VFSContainer {
	
	private final CPTreeDataModel treeModel;
	private final ContentPackage cp;
	private final String ident;
	
	public VFSCPNamedContainerItem(String ident, String name, VFSLeaf delegate, ContentPackage cp, CPTreeDataModel treeModel) {
		super(name, delegate);
		
		this.cp = cp;
		this.ident = ident;
		this.treeModel = treeModel;
	}

	@Override
	public VFSItem resolve(String path) {
		return VFSManager.resolveFile(this, path);
	}

	@Override
	public List<VFSItem> getItems() {
		return VFSCPContainer.getItems(cp, treeModel, ident);
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		return getItems();
	}

	@Override
	public boolean isInPath(String path) {
		return false;
	}

	@Override
	public VFSStatus copyFrom(VFSItem source, Identity savedBy) {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus copyContentOf(VFSContainer container, Identity savedBy) {
		return VFSConstants.NO;
	}

	@Override
	public VFSContainer createChildContainer(String containerName) {
		return null;
	}

	@Override
	public VFSLeaf createChildLeaf(String leafName) {
		return null;
	}
	
	@Override
	public VFSItemFilter getDefaultItemFilter() {
		return null;
	}

	@Override
	public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
		//
	}
}