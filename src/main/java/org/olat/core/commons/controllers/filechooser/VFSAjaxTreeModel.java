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
package org.olat.core.commons.controllers.filechooser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeModel;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeNode;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * Description:<br>
 * The VFS ajax tree model generates a dynamic data model for an ajax tree based
 * on a given root container. The datamodel can be configured using a vfs item
 * filter
 * <P>
 * Initial Date: 12.06.2008 <br>
 * 
 * @author gnaegi
 */
class VFSAjaxTreeModel extends AjaxTreeModel {
	// since the ext tree uses "/" as node delimiters, we have to encode the path
	// differently and revert it back to the filsystem delimiter later on
	private static final String DELIMITER_NODE = ":-:";
	private static final String DELIMITER_FILESYSTEM = "/";
	// local variables
	private final VFSContainer rootContainer;
	private final VFSItemFilter vfsFilter;

	/**
	 * Constructor for a ajax tree virtual filesystem data model
	 * 
	 * @param rootContainer The catalog root entry
	 * @param vfsFilter The filter to be applied on the root container
	 */
	VFSAjaxTreeModel(VFSContainer rootContainer, VFSItemFilter vfsFilter) {
		super(rootContainer.getName());
		this.rootContainer = rootContainer;
		this.vfsFilter = vfsFilter;
	}

	/**
	 * @see org.olat.core.gui.control.generic.ajax.tree.AjaxTreeModel#getChildrenFor(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<AjaxTreeNode> getChildrenFor(String nodePath) {
		List<AjaxTreeNode> childNodes = new ArrayList<AjaxTreeNode>();
		// get item for given path
		VFSItem item;
		if (rootContainer.getName().equals(nodePath)) {
			item = rootContainer;
			nodePath = ""; // start empty
		} else {
			// replace node delimiter with file system delimiter
			item = resolveFileForTreeNodeId(nodePath);
		}
		// load children of item and add them to the list
		if (item instanceof VFSContainer) {
			VFSContainer directory = (VFSContainer) item;
			List<VFSItem> childItems = directory.getItems(vfsFilter);
			for (VFSItem childItem : childItems) {
				AjaxTreeNode childNode = buildNode(childItem, nodePath);
				childNodes.add(childNode);
			}
		}
		return childNodes;
	}

	/**
	 * Resolve and return a VFSItem from the given tree node id that has been
	 * created by this tree model
	 * 
	 * @param treeNodeId
	 * @return
	 */
	public VFSItem resolveFileForTreeNodeId(String treeNodeId) {
		String relFilePath = treeNodeId.replace(DELIMITER_NODE, DELIMITER_FILESYSTEM);
		VFSItem item = VFSManager.resolveFile(rootContainer, relFilePath);
		return item;
	}

	/**
	 * Internal helper to build a tree node from a vfs item
	 * 
	 * @param vfsItem
	 * @param nodePathPrefix prefix representing the path hierarchy
	 * @return
	 */
	private AjaxTreeNode buildNode(VFSItem vfsItem, String nodePathPrefix) {
		AjaxTreeNode node;
		try {
			// as node ID we use the file path relative to the root container, as
			// delimiter we use our special delimiter to not get in conflict with the
			// ext tree delimiter which uses "/".
			node = new AjaxTreeNode(nodePathPrefix + DELIMITER_NODE + vfsItem.getName(), vfsItem.getName());
			
			// Setting some node attributes - see the Treenode or the extjs
			// documentation on what else you could use
			boolean isContainer = (vfsItem instanceof VFSContainer);
			if (isContainer) {
				VFSContainer directory = (VFSContainer) vfsItem;
				// mark as of type non-leaf for sorting
				node.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, false);
				// set leaf = true when this container contains no children - no
				// expand icon!
				if (directory.getItems(vfsFilter).size() == 0) {
					node.put(AjaxTreeNode.CONF_LEAF, true);
					node.put(AjaxTreeNode.CONF_DISABLED, true);
				} else {
					node.put(AjaxTreeNode.CONF_LEAF, false);
				}
				// use folder css class
				node.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, "b_filetype_folder");
			} else {
				// mark as of type leaf for sorting
				node.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, true);
				// mark as leaf to not show expand/collapse icon
				node.put(AjaxTreeNode.CONF_LEAF, true);
				// create cssclass for the given type
				String name = vfsItem.getName();
				String cssClass = "b_filetype_file"; // default
				int typePos = name.lastIndexOf(".");
				if (typePos > 0) {
					cssClass = "b_filetype_" + name.substring(typePos + 1);
				}
				node.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, cssClass);
			}
			// disable drag&drop support
			node.put(AjaxTreeNode.CONF_ALLOWDRAG, false);
			node.put(AjaxTreeNode.CONF_ALLOWDROP, false);
		} catch (JSONException e) {
			throw new OLATRuntimeException("Error while creating AjaxTreeNode for VFSItem::" + vfsItem, e);
		}

		return node;
	}

}
