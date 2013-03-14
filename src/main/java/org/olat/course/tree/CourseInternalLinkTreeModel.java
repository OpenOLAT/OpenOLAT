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

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * Initial Date: 21.12.2006
 * 
 * @author Christian Guretzki
 * @author Florian Gnägi, http://www.frentix.com
 */
public class CourseInternalLinkTreeModel extends CustomLinkTreeModel {

	private static final long serialVersionUID = -1069112575005677374L;
	private TreeNode rootNode;
	
	/**
	 * Create a tree model based on the course editor model
	 * 
	 * @param courseEditorTreeModel
	 */
	public CourseInternalLinkTreeModel(CourseEditorTreeModel courseEditorTreeModel) {
		super(courseEditorTreeModel.getRootNode().getIdent());
		setRootNode(courseEditorTreeModel.getRootNode());
	}

	/**
	 * Create a tree model based on the course root node from the course structure
	 * 
	 * @param courseRootNode
	 */
	public CourseInternalLinkTreeModel(CourseNode courseRootNode) {
		super(courseRootNode.getIdent());
		TreeNode treeRootNode = convertToTreeNode(courseRootNode);
		setRootNode(treeRootNode);
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
	@Override
	public String getInternalLinkUrlFor(String nodeId) {
		return "javascript:parent.gotonode(" + nodeId + ")";
	}
	
	/**
	 * @see org.olat.core.gui.components.tree.TreeModel#getRootNode()
	 */
	@Override
	public TreeNode getRootNode() {
		return rootNode;
	}
	
	/**
	 * Sets the rootNode.
	 * @param rootNode The rootNode to set
	 */
	public void setRootNode(TreeNode rootNode) {
		this.rootNode = rootNode;
	}

	/**
	 * @see org.olat.core.gui.components.tree.TreeModel#getNodeById(java.lang.String)
	 */
	@Override
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
}