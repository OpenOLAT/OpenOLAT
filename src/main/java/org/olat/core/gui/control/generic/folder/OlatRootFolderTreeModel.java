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
*/
package org.olat.core.gui.control.generic.folder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * This TreeModel is intended for OlatRootFolderImpl and OlatRootFileImpl.
 * Instances of these classes have usually a MetaInfo object attached, whose
 * title string is used to set the tree node's title (filename if title is
 * empty).<br/>
 * No CSS classes are added.
 * 
 * <P>
 * Initial Date: Jul 9, 2009 <br>
 * 
 * @author gwassmann
 */
public class OlatRootFolderTreeModel extends GenericTreeModel {
	/**
	 * used during deserialization to verify that the sender and receiver of a
	 * serialized object have loaded classes for that object that are compatible
	 * with respect to serialization
	 * 
	 * @see http://java.sun.com/j2se/1.5.0/docs/api/java/io/Serializable.html
	 */
	static final long serialVersionUID = 1L;

	private VFSItemFilter filter;
	private Comparator<VFSItem> comparator;

	public OlatRootFolderTreeModel(LocalFolderImpl root) {
		setRootNode(createNode(root));
		getRootNode().getChildCount();
	}

	public OlatRootFolderTreeModel(LocalFolderImpl root, VFSItemFilter filter) {
		this.filter = filter;
		setRootNode(createNode(root));
		getRootNode().getChildCount();
	}

	public OlatRootFolderTreeModel(LocalFolderImpl root,
			VFSItemFilter filter, Comparator<VFSItem> comparator) {
		this.filter = filter;
		this.comparator = comparator;
		setRootNode(createNode(root));
		getRootNode().getChildCount();
	}

	/**
	 * Add children to the node
	 * 
	 * @param node
	 * @param root
	 */
	protected void makeChildren(OlatRootFolderTreeNode node, LocalFolderImpl root) {
		List<VFSItem> children = root.getItems(filter);
		if (comparator != null) {
			Collections.sort(children, comparator);
		}
		for (VFSItem child : children) {
			// create a node for each child and add it
			OlatRootFolderTreeNode childNode = createNode(child);
			node.addChild(childNode);
			if (child instanceof LocalFolderImpl) {
				// add the child's children recursively
				makeChildren(childNode, (LocalFolderImpl) child);
			}
		}
	}

	/**
	 * Create a node out of a relative path vfs item. The user object is set to
	 * the relative path.
	 * 
	 * @param item
	 */
	private OlatRootFolderTreeNode createNode(VFSItem item) {
		OlatRootFolderTreeNode node = new OlatRootFolderTreeNode(item, this);
		VFSMetadata meta = item.getMetaInfo();
		if (meta != null) {
			String title = meta.getTitle();
			if (StringHelper.containsNonWhitespace(title)) {
				node.setTitle(title);
			} else {
				node.setTitle(meta.getFilename());
			}
		}
		node.setUserObject(item.getRelPath());
		return node;
	}

	@Override
	public OlatRootFolderTreeNode getRootNode() {
		return (OlatRootFolderTreeNode) super.getRootNode();
	}
}
