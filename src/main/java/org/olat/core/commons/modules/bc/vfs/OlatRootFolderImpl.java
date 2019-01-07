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

package org.olat.core.commons.modules.bc.vfs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaInfoFactory;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;

public class OlatRootFolderImpl extends LocalFolderImpl implements OlatRelPathImpl, MetaTagged {

	private String folderRelPath;
	
	public OlatRootFolderImpl(String folderRelPath, VFSContainer parent) {
		super(new File(FolderConfig.getCanonicalRoot() + folderRelPath), parent);
		this.folderRelPath = folderRelPath;
	}

	/**
	 * Wrap all LocalImpls to OlatRootImpls
	 *
	 * @see org.olat.core.util.vfs.VFSContainer#createChildContainer(java.lang.String)
	 */
	@Override
	public OlatRootFolderImpl createChildContainer(String name) {
		VFSItem result = super.createChildContainer(name);
		if (result == null) return null;
		return new OlatRootFolderImpl(folderRelPath + "/" + name, this);
	}

	/**
	 * Wrap all LocalImpls to OlatRootImpls
	 *
	 * @see org.olat.core.util.vfs.VFSContainer#createChildLeaf(java.lang.String)
	 */
	@Override
	public OlatRootFileImpl createChildLeaf(String name) {
		VFSItem result = super.createChildLeaf(name);
		if (result == null) return null;
		return new OlatRootFileImpl(folderRelPath + "/" + name, this);
	}

	/**
	 * Wrap all LocalImpls to OlatRootImpls
	 *
	 * @see org.olat.core.util.vfs.VFSContainer#getItems()
	 */
	@Override
	public List<VFSItem> getItems() {
		List<VFSItem> items = super.getItems();
		items = wrapItems(items);
		return items;
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		List<VFSItem> items = super.getItems(filter);
		items = wrapItems(items);
		return items;
	}

	/**
	 * @param children
	 * @return
	 */
	private List<VFSItem> wrapItems(List<VFSItem> items) {
		List<VFSItem> wrappedItems = new ArrayList<>(items.size());
		// now wrap all LocalImpls to OlatRootImpls...
		for (VFSItem item : items) {
			if (item instanceof LocalFolderImpl) {
				wrappedItems.add(new OlatRootFolderImpl(folderRelPath + "/" + item.getName(), this));
			} else if (item instanceof LocalFileImpl) {
				wrappedItems.add(new OlatRootFileImpl(folderRelPath + "/" + item.getName(), this));
			}
		}
		return wrappedItems;
	}

	@Override
	public String getRelPath() {
		return folderRelPath;
	}

	@Override
	public MetaInfo getMetaInfo() {
		return CoreSpringFactory.getImpl(MetaInfoFactory.class).createMetaInfoFor(getBasefile());
	}
	
}
