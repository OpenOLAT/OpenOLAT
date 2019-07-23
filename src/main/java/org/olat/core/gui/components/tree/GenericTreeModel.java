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

package org.olat.core.gui.components.tree;

import java.util.LinkedList;

/**
 * Initial Date: Feb 13, 2004
 * 
 * @author Mike Stock
 */
public class GenericTreeModel implements TreeModel {
	/**
	 * Used during deserialization to verify that the sender and receiver of a
	 * serialized object have loaded classes for that object that are compatible
	 * with respect to serialization. <br/>
	 * Look at this variable as a class version number.
	 * 
	 * @see http://java.sun.com/j2se/1.5.0/docs/api/java/io/Serializable.html
	 */
	static final long serialVersionUID = 1L;

	private TreeNode rootNode;

	/**
	 * @see org.olat.core.gui.components.tree.TreeModel#getRootNode()
	 */
	@Override
	public TreeNode getRootNode() {
		return rootNode;
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
		if (node.getIdent().equals(nodeId))
			return node;
		int childcnt = node.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			TreeNode child = (TreeNode) node.getChildAt(i);
			TreeNode result = findNode(nodeId, child);
			if (result != null)
				return result;
		}
		return null;
	}

	/**
	 * Searches (by breadth-first search) the node in this model whose user
	 * object is equal to the given object.
	 * 
	 * @param object
	 * @return The node whose user object is equal to the given object or null
	 */
	public TreeNode findNodeByUserObject(Object object) {
		LinkedList<TreeNode> queue = new LinkedList<>();
		// initialize the queue by the root node
		queue.add(getRootNode());
		do {
			// dequeue and examine
			TreeNode node = queue.poll();
			Object currentNodesObject = node.getUserObject();
			if (object.equals(currentNodesObject)) {
				return node;
			} else {
				// enqueue successors
				for (int i = 0; i < node.getChildCount(); i++) {
					queue.add((TreeNode) node.getChildAt(i));
				}
			}
		} while (!queue.isEmpty());
		// the node couldn't be found
		return null;
	}

	/**
	 * Sets the rootNode.
	 * 
	 * @param rootNode
	 *            The rootNode to set
	 */
	public void setRootNode(TreeNode rootNode) {
		this.rootNode = rootNode;
	}
}
