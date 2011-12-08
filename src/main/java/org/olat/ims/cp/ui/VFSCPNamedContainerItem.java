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
* frentix GmbH, Switzerland, http://www.frentix.com
* <p>
*/
package org.olat.ims.cp.ui;

import java.util.List;

import org.olat.core.util.vfs.NamedLeaf;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.CPManagerImpl;
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
//fxdiff FXOLAT-125: virtual file system for CP
public class VFSCPNamedContainerItem extends NamedLeaf implements VFSContainer {
	
	private final CPTreeDataModel treeModel;
	private final CPManagerImpl cpMgm;
	private final ContentPackage cp;
	private final String ident;
	
	public VFSCPNamedContainerItem(String ident, String name, VFSLeaf delegate, ContentPackage cp, CPTreeDataModel treeModel) {
		super(name, delegate);
		
		this.cp = cp;
		this.ident = ident;
		cpMgm = (CPManagerImpl) CPManager.getInstance();
		this.treeModel = treeModel;
	}

	@Override
	public VFSItem resolve(String path) {
		VFSItem resolved = VFSManager.resolveFile(this, path);
		return resolved;
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
	public VFSStatus copyFrom(VFSItem source) {
		return VFSConstants.NO;
	}

	@Override
	public VFSContainer createChildContainer(String name) {
		return null;
	}

	@Override
	public VFSLeaf createChildLeaf(String name) {
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