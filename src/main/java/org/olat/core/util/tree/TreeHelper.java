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

package org.olat.core.util.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * Helper methods for tree handling
 * 
 * <P>
 * Initial Date:  04.08.2005 <br>
 * @author gnaegi, Felix Jost
 */
public class TreeHelper {
	
	private static final Logger log = Tracing.createLoggerFor(TreeHelper.class);
	
	/**
	 * Depth-first traversal.
	 * @param nodeId
	 * @param node the root node to start the search with
	 * @return the first treenode with the given user object or null if not found
	 */
	public static TreeNode findNodeByUserObject(Object userObject, TreeNode node) {
		if (node.getUserObject() != null && node.getUserObject().equals(userObject)) {
			return node;
		}
		int childcnt = node.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			TreeNode child = (TreeNode) node.getChildAt(i);
			TreeNode result = findNodeByUserObject(userObject, child);
			if (result != null) return result;
		}
		return null;
	}
	
	public static TreeNode findNodeByResourceableUserObject(OLATResourceable userObject, TreeNode node) {
		if (node.getUserObject() instanceof OLATResourceable
				&& OresHelper.equals((OLATResourceable)node.getUserObject(), userObject)) {
			return node;
		}
		int childcnt = node.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			TreeNode child = (TreeNode) node.getChildAt(i);
			TreeNode result = findNodeByUserObject(userObject, child);
			if (result != null) return result;
		}
		return null;
	}
	
	public static int indexOfByUserObject(Object childUserObject, TreeNode parentNode) {
		TreeNode childNode = findNodeByUserObject(childUserObject, parentNode);
		return indexOf(childNode, parentNode);
	}

	public static int indexOf(TreeNode childNode, INode parentNode) {
		if(parentNode == null) return -1;
		for(int i=parentNode.getChildCount(); i-->0; ) {
			INode n = parentNode.getChildAt(i);
			if(n.getIdent().equals(childNode.getIdent())) {
				return i;
			}
		}
		return -1;
	}
	
	public static int totalNodeCount(TreeNode node) {
		int nodeCnt = 1;//me
		for(int i=0; i<node.getChildCount(); i++) {
			nodeCnt += totalNodeCount((TreeNode)node.getChildAt(i));
		}
		return nodeCnt;
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
	
	public static List<TreeNode> getTreePath(TreeNode node) {
		List<TreeNode> conPath = new ArrayList<>();
		for (TreeNode cur = node; cur != null; cur = (TreeNode) cur.getParent()) {
			conPath.add(cur);
		}
		Collections.reverse(conPath);
		return conPath;
	}
	
	public static boolean isInParentLine(INode node, INode potentialParent) {
		try {
			for(INode iteratorNode=node; node.getParent() != null && iteratorNode != null; iteratorNode=iteratorNode.getParent()) {
				if(iteratorNode.getIdent().equals(potentialParent.getIdent())) {
					return true;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}

	public static void collectNodeIdentifiersRecursive(INode node, List<String> nodeIdentifiers) {
		if(node != null) {
			nodeIdentifiers.add(node.getIdent());
			int numOfChildren = node.getChildCount();
			for (int i = 0; i < numOfChildren; i++) {
				collectNodeIdentifiersRecursive(node.getChildAt(i), nodeIdentifiers);
			}
		}
	}
	
	/**
	 * from tree structure to a flat list
	 * @param node
	 * @param outNodeList
	 */
	public static void makeTreeFlat(TreeNode node, List<TreeNode> outNodeList){
		//add node
		if(node != null) {
			outNodeList.add(node);
			int childcnt = node.getChildCount();
			for (int i = 0; i < childcnt; i++) {
				//add all subnodes.
				TreeNode child = (TreeNode) node.getChildAt(i);
				makeTreeFlat(child, outNodeList);
			}
		}
	}
	
	public static INode getLastNode(INode node) {
		if (node.getChildCount() == 0) {
			return node;
		}
		return getLastNode(node.getChildAt(node.getChildCount() - 1));
	}
}
