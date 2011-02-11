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

package org.olat.core.util.tree;

import java.util.List;

import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * Helper methods for tree handling
 * 
 * <P>
 * Initial Date:  04.08.2005 <br>
 * @author gnaegi, Felix Jost
 */
public class TreeHelper {
	
	/**
	 * Depth-first traversal.
	 * @param nodeId
	 * @param node the root node to start the search with
	 * @return the first treenode with the given user object or null if not found
	 */
	public static TreeNode findNodeByUserObject(Object userObject, TreeNode node) {
		if (node.getUserObject().equals(userObject)) return node;
		int childcnt = node.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			TreeNode child = (TreeNode) node.getChildAt(i);
			TreeNode result = findNodeByUserObject(userObject, child);
			if (result != null) return result;
		}
		return null;
	}

	public static TreeNode resolveTreeNode(String treePath, TreeModel treeModel) {
		// even for the root node, our parameter may not be the empty string, therefore the prefix to be chopped here
		treePath =  treePath.substring(1);
		TreeNode cur = treeModel.getRootNode();
		if (!treePath.equals("")) { // if we are not the root node				
			String[] res = treePath.split("_");
			for (int i = res.length -1; i >= 0; i--) {
				String spos = res[i];
				Integer chdPos = Integer.parseInt(spos);
				TreeNode chd = (TreeNode) cur.getChildAt(chdPos);
				if (chd == null) throw new AssertException("cannot find: "+treePath);
				cur = chd;
			}
		}
		return cur;
	}
	
	public static String buildTreePath(TreeNode node) {
		// if in load performance mode -> generate the treeposition and include it as param, 
		// since the nodeid itself is random and thus not replayable
		StringBuilder conPath = new StringBuilder();
		// we need at least one char in the var, even if we click the root node
		conPath.append('_');
		TreeNode cur = node;
		TreeNode par = (TreeNode) cur.getParent();
		while (par != null) {
			int cpos = cur.getPosition();
			conPath.append(cpos).append('_');
			cur = par;
			par = (TreeNode) cur.getParent();
		}
		return conPath.toString();
	}
	
	/**
	 * from tree structure to a flat list
	 * @param node
	 * @param outNodeList
	 */
	public static void makeTreeFlat(TreeNode node, List<TreeNode> outNodeList){
		//add node
		outNodeList.add(node);
		int childcnt = node.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			//add all subnodes.
			TreeNode child = (TreeNode) node.getChildAt(i);
			makeTreeFlat(child, outNodeList);
		}
	}
}
