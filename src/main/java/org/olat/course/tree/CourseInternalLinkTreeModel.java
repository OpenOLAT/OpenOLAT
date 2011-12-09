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

package org.olat.course.tree;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeNode;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.nodes.INode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * Initial Date: 21.12.2006
 * 
 * @author Christian Guretzki
 * @author Florian Gnägi, http://www.frentix.com
 */
public class CourseInternalLinkTreeModel extends CustomLinkTreeModel {

	/**
	 * Create a tree model based on the course editor model
	 * 
	 * @param courseEditorTreeModel
	 */
	public CourseInternalLinkTreeModel(CourseEditorTreeModel courseEditorTreeModel) {
		super(courseEditorTreeModel.getRootNode().getIdent());
		this.setRootNode(courseEditorTreeModel.getRootNode());
	}

	/**
	 * Create a tree model based on the course root node from the course structure
	 * 
	 * @param courseRootNode
	 */
	public CourseInternalLinkTreeModel(CourseNode courseRootNode) {
		super(courseRootNode.getIdent());
		TreeNode treeRootNode = convertToTreeNode(courseRootNode);
		this.setRootNode(treeRootNode);
	}

	/**
	 * Internal helper to convert the given course node into a tree node. The
	 * course node identifyer will be transfered onto the tree nodes identifyer
	 * 
	 * @param courseNode
	 * @return the course node as converted tree node
	 */
	private TreeNode convertToTreeNode(CourseNode courseNode) {
		// create convert this course node to a tree node
		GenericTreeNode treeNode = new GenericTreeNode();
		treeNode.setIdent(courseNode.getIdent());
		treeNode.setTitle(courseNode.getShortTitle());
		treeNode.setIconCssClass(CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass());
		// go through all children and add them as converted tree nodes
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			treeNode.addChild(convertToTreeNode(child));
		}
		return treeNode;
	}

	/**
	 * @see org.olat.core.commons.editor.htmleditor.InternalLinkTreeModel#getInternalLinkUrlFor(java.lang.String)
	 */
	public String getInternalLinkUrlFor(String nodeId) {
		return "javascript:parent.gotonode(" + nodeId + ")";
	}
	
	@Override
	public List<AjaxTreeNode> getChildrenFor(String nodeId) {
		if (nodeId.contains("/")) throw new AssertException("Ext AJAX tree does not support node id's that contain a '/'");
		List<AjaxTreeNode> childAjaxTreeNodes = new ArrayList<AjaxTreeNode>();
		TreeNode treeNode = findNode(nodeId, getRootNode());
		// Now build the ajax tree nodes for each child
		int childCount = treeNode.getChildCount();
		for (int i = 0; i < childCount; i++) {
			INode childNode = treeNode.getChildAt(i);
			AjaxTreeNode childAjaxNode = buildAjaxTreeNode((TreeNode) childNode);
			childAjaxTreeNodes.add(childAjaxNode);
		}
		return childAjaxTreeNodes;		
	}

	/**
	 * Internal helper to build a tree node from a vfs item
	 * 
	 * @param vfsItem
	 * @return
	 */
	private AjaxTreeNode buildAjaxTreeNode(TreeNode treeNode) {
		AjaxTreeNode node;
		try {
			// as node ID we use the file path relative to the root container, as
			// delimiter we use our special delimiter to not get in conflict with the
			// ext tree delimiter which uses "/".
			node = new AjaxTreeNode(treeNode.getIdent(), treeNode.getTitle());
			// use folder css class
			node.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, treeNode.getIconCssClass());
			// disable drag&drop support
			node.put(AjaxTreeNode.CONF_ALLOWDRAG, false);
			node.put(AjaxTreeNode.CONF_ALLOWDROP, false);
			// set open-icon only when there are children available
			node.put(AjaxTreeNode.CONF_LEAF, (treeNode.getChildCount() < 1));
		} catch (JSONException e) {
			throw new OLATRuntimeException("Error while creating AjaxTreeNode for treeNode::" + treeNode.getIdent(), e);
		}

		return node;
	}

	

	
	
	
	/* *
	 * 
	 * copy past from GenericTreeModel 
	 * 
	 * */
	
	private TreeNode rootNode;
	

	/**
	 * @see org.olat.core.gui.components.tree.TreeModel#getRootNode()
	 */
	public TreeNode getRootNode() { return rootNode; }

	/**
	 * @see org.olat.core.gui.components.tree.TreeModel#getNodeById(java.lang.String)
	 */
	public TreeNode getNodeById(String nodeId) {
		return findNode(nodeId, rootNode);
	}

	/**
	 * Depth-first traversal.
	 * 
	 * @param nodeId
	 * @param node
	 * @return the treenode with the node id or null if not found
	 */
	private TreeNode findNode(String nodeId, TreeNode node) {
		if (node.getIdent().equals(nodeId)) return node;
		int childcnt = node.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			TreeNode child = (TreeNode) node.getChildAt(i);
			TreeNode result = findNode(nodeId, child);
			if (result != null) return result;
		}
		return null;
	}

	/**
	 * Sets the rootNode.
	 * @param rootNode The rootNode to set
	 */
	public void setRootNode(TreeNode rootNode) {
		this.rootNode = rootNode;
	}

}
