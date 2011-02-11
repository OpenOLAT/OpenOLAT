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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.control.generic.folder;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * Initial Date:  Feb 13, 2004
 *
 * @author Mike Stock
 */
public class FolderTreeModel extends GenericTreeModel {

	private boolean foldersOnly = false;
	private boolean selectableFiles = false;
	private boolean selectableFolders = true;
	private Locale locale;
	private VFSItemFilter fileFilter;
	Collator collator;

	/**
	 * 
	 * @param alocale
	 * @param rootContainer
	 * @param foldersOnly
	 * @param selectableFiles
	 * @param selectableFolders
	 * @param allowRootFolderSelect
	 * @param fileFilter
	 */
	public FolderTreeModel(Locale alocale, VFSContainer rootContainer, boolean foldersOnly, boolean selectableFiles, boolean selectableFolders, boolean allowRootFolderSelect, VFSItemFilter fileFilter) {
		this.locale = alocale;
		this.collator = Collator.getInstance(locale);
		this.foldersOnly = foldersOnly;
		this.selectableFiles = selectableFiles;
		this.selectableFolders = selectableFolders;
		this.fileFilter = fileFilter;

		GenericTreeNode newRoot = new GenericTreeNode(rootContainer.getName(), rootContainer.getName());
		newRoot.setIconCssClass("b_filetype_folder");
		setRootNode(newRoot);
		if (allowRootFolderSelect) { // include root folder as selection
			GenericTreeNode effectiveRoot = new GenericTreeNode("/", "/");
			newRoot.addChild(effectiveRoot);
			buildTree(effectiveRoot, rootContainer, "/");
		} else {
			buildTree(getRootNode(), rootContainer, "/");
		}
	}
	
	/**
	 * @param selectedNode
	 * @return The Path object represented by the given selection.
	 */
	public String getSelectedPath(TreeNode selectedNode) {
		if (selectedNode == null || !(selectedNode instanceof GenericTreeNode))
			return null;
		return (String)((GenericTreeNode)selectedNode).getUserObject();
	}

	private boolean buildTree(TreeNode tParent, VFSContainer parentContainer, String parentPath) {
		List children = parentContainer.getItems(fileFilter);
		if (children.size() == 0) return false;

		// sort the children
		Collections.sort(children, new Comparator(){
			final Collator c = collator;
			public int compare(final Object o1, final Object o2) {
				return c.compare(((VFSItem)o1).getName(), ((VFSItem)o2).getName());
			}});

		boolean addedAtLeastOneChild = false;
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			VFSItem child = (VFSItem) iter.next();
			String childName = child.getName();
			if (child instanceof VFSContainer) {
				// container node
				String filePath = parentPath + childName + "/";
				GenericTreeNode tChild = new GenericTreeNode(childName, filePath); // filePath is the information to be remembered later
				tChild.setIconCssClass("b_filetype_folder");
				tChild.setAltText(child.getName());
				tChild.setAccessible(selectableFolders ? (child.canWrite() == VFSConstants.YES) : false);
				tParent.addChild(tChild);
				boolean addedChildren = buildTree(tChild, (VFSContainer)child, filePath);
				if (foldersOnly || addedChildren) {
					addedAtLeastOneChild = true;
				} else {
					// this directory does not contain anything usefull, remove it again!
					tParent.remove(tChild);
				}
			} else {
				// leaf node
				if (foldersOnly) continue;
				String filePath = parentPath + childName;
				GenericTreeNode tChild = new GenericTreeNode(childName, filePath);
				String type = FolderHelper.extractFileType(childName, locale);
				if (!FolderHelper.isKnownFileType(type)) { type = "file"; }
				tChild.setIconCssClass("b_filetype_" + type);
				tChild.setAltText(childName);
				tChild.setAccessible(selectableFiles);
				tParent.addChild(tChild);
				addedAtLeastOneChild = true;
			}
		}
		return addedAtLeastOneChild;
	}
	
	/**
	 * @see org.olat.core.gui.components.tree.GenericTreeModel#getRootNode()
	 */
	public GenericTreeNode getRootNode() {
		return (GenericTreeNode)super.getRootNode();
	}
}
