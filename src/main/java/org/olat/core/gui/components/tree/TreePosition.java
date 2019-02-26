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

package org.olat.core.gui.components.tree;

import org.olat.core.gui.components.tree.InsertionPoint.Position;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class TreePosition {

	private TreeNode parent;
	private TreeNode node;
	private Position position;
	private int childpos; // the position of the existing treenode to which to new
												// node should be prepended (0 = at the beginning, also
												// works if there are no children yet)

	public TreePosition(TreeNode parent, int childpos) {
		this.parent = parent;
		this.childpos = childpos;
	}
	
	public TreePosition(TreeNode parent, TreeNode node, Position position, int childpos) {
		this.node = node;
		this.parent = parent;
		this.childpos = childpos;
		this.position = position;
	}

	/**
	 * @return int
	 */
	public int getChildpos() {
		return childpos;
	}

	/**
	 * @return TreeNode
	 */
	public TreeNode getParentTreeNode() {
		return parent;
	}
	
	public TreeNode getNode() {
		return node;
	}
	
	public Position getPosition() {
		return position;
	}

}
