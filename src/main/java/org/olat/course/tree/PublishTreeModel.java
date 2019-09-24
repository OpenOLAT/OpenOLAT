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

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.INodeFilter;
import org.olat.course.Structure;

/**
 * Initial Date: Jun 3, 2004
 * 
 * @author Mike Stock
 */
public class PublishTreeModel extends GenericTreeModel implements INodeFilter {

	private static final long serialVersionUID = 8262940754776391142L;
	private Structure currentRunStructure;

	/**
	 * Build a new publish tree model. Node proxies in the model get the ident's
	 * of their correspondents in the CourseEditorTreeModel.
	 * 
	 * @param cetm
	 */
	public PublishTreeModel(CourseEditorTreeModel cetm, Structure runStructure) {
		// build InsertModel (copy of this Structure with all possible
		// insert-positions)
		GenericTreeNode gtn = new GenericTreeNode();
		gtn.setAccessible(false);
		gtn.setTitle("");
		setRootNode(gtn);
		currentRunStructure = runStructure;

		CourseEditorTreeNode cnRoot = (CourseEditorTreeNode) cetm.getRootNode();
		gtn.addChild(buildNode(cnRoot, false, false, false));
	}

	private GenericTreeNode buildNode(CourseEditorTreeNode cetn, boolean parentIsNew, boolean parentIsDeleted, boolean parentIsMoved) {
		GenericTreeNode gtn = new GenericTreeNode();
		gtn.setIdent(cetn.getIdent());
		gtn.setTitle(cetn.getTitle());
		gtn.setAltText(cetn.getAltText());
		gtn.setIconCssClass("o_icon " + cetn.getIconCssClass());

		if (parentIsNew || parentIsDeleted || parentIsMoved) gtn.setAccessible(false);
		else {
			gtn.setAccessible(cetn.hasPublishableChanges());
		}
		gtn.setCssClass(cetn.getCssClass());

		int childcnt = cetn.getChildCount();
		if (childcnt > 0) {
			for (int i = 0; i < childcnt; i++) {
				parentIsNew = parentIsNew || cetn.isNewnode();
				parentIsDeleted = parentIsDeleted || cetn.isDeleted();
				parentIsMoved = parentIsMoved || isMoved(cetn);
				GenericTreeNode childNode = buildNode((CourseEditorTreeNode) cetn.getChildAt(i), parentIsNew, parentIsDeleted, parentIsMoved);
				// if this is the first new node, enable it
				if (!parentIsNew && cetn.isNewnode()) childNode.setAccessible(true);
				// if this is the first deleted node, enable it
				if (!parentIsDeleted && cetn.isDeleted()) childNode.setAccessible(true);
				// if this is the first moved node, enable it
				if (!parentIsMoved && isMoved(cetn)) childNode.setAccessible(true);
				gtn.insert(childNode, i);
			}
		}
		return gtn;
	}

	public boolean isMoved(CourseEditorTreeNode cetn) {
		if (cetn.isNewnode() || cetn.isDeleted()) {
			return false;
		} else if (currentRunStructure.getNode(cetn.getCourseNode().getIdent()) == null) {
			// No course node in runstructure
		  return true;
		} else {
			INode node = currentRunStructure.getNode(cetn.getCourseNode().getIdent());
			String runPath = getPositionPathFor(node);
			String editorPath = getPositionPathFor(cetn);
			return (!(runPath.equals(editorPath))) && cetn.isDirty();
		}
	}

	private String getPositionPathFor(INode node) {
		String path = "";
		INode parent = node.getParent();
		if (parent == null) {
			path = "__root__";
		} 
		while (parent!= null) {
			path += parent.getIdent() + ":" + node.getPosition() + "]";
			parent = parent.getParent();
		}
		return path;
	}

	/**
	 * Check if this publish tree node has any publishable changes.
	 */
	public boolean hasPublishableChanges() {
		return recursiveHasPublishableChanges(getRootNode());
	}

	private boolean recursiveHasPublishableChanges(TreeNode currentNode) {
		if (currentNode.isAccessible()) return true;
		for (int i = 0; i < currentNode.getChildCount(); i++) {
			if (recursiveHasPublishableChanges((TreeNode) currentNode.getChildAt(i))) return true;
		}
		return false;
	}

	public boolean isSelectable(INode node) {
		TreeNode tn = (TreeNode)node;
		return tn.isAccessible();
	}

	@Override
	public boolean isVisible(INode node) {
		TreeNode tn = (TreeNode)node;
		if(tn.isAccessible()) {
			return true;
		}
		return isVisibleRec(tn);
	}
	
	private boolean isVisibleRec(TreeNode tn) {
		for(int i=tn.getChildCount(); i-->0; ) {
			TreeNode child = (TreeNode)tn.getChildAt(i);
			if(child.isAccessible()) {
				return true;
			}
			if(child.getChildCount() > 0) {
				if(isVisibleRec(child)) {
					return true;
				}
			}
		}
		return false;
	}
}