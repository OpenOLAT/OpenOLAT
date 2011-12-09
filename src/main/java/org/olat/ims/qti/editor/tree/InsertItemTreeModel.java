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

package org.olat.ims.qti.editor.tree;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.course.tree.TreePosition;

/**
 * Initial Date: Jan 05, 2005 <br>
 * 
 * @author mike
 */
public class InsertItemTreeModel extends GenericTreeModel {

	/**
	 * Comment for <code>INSTANCE_ASSESSMENT</code>
	 */
	public static final int INSTANCE_ASSESSMENT = 0;
	/**
	 * Comment for <code>INSTANCE_SECTION</code>
	 */
	public static final int INSTANCE_SECTION = 1;
	/**
	 * Comment for <code>INSTANCE_ASSESSMENT</code>
	 */
	public static final int INSTANCE_ITEM = 2;

	private int appendToInstancesOf;

	/**
	 * @param treeModel
	 * @param appendToInstancesOf
	 */
	public InsertItemTreeModel(TreeModel treeModel, int appendToInstancesOf) {
		this.appendToInstancesOf = appendToInstancesOf;
		GenericQtiNode cnRoot = (GenericQtiNode) treeModel.getRootNode();
		TreeNode ctn = buildNode(cnRoot);
		setRootNode(ctn);
	}

	private TreeNode buildNode(GenericQtiNode parent) {
		int parentInstance = INSTANCE_ASSESSMENT;
		if (parent instanceof SectionNode) parentInstance = INSTANCE_SECTION;
		if (parent instanceof ItemNode) parentInstance = INSTANCE_ITEM;

		GenericTreeNode ctn = new GenericTreeNode(parent.getTitle(), parent);
		ctn.setIconCssClass(parent.getIconCssClass());
		ctn.setAccessible(false);

		int childcnt = parent.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			if (parentInstance == appendToInstancesOf) { // add insert pos
				GenericTreeNode gtn = new GenericTreeNode();
				gtn.setAccessible(true);
				gtn.setTitle("");
				gtn.setAltText("");
				gtn.setUserObject(new TreePosition(parent, i));
				ctn.addChild(gtn);
			}
			// add child itself
			GenericQtiNode cchild = (GenericQtiNode) parent.getChildAt(i);
			TreeNode ctchild = buildNode(cchild);
			ctn.addChild(ctchild);
		}
		if (parentInstance == appendToInstancesOf) {
			// add last insert position
			GenericTreeNode gtn = new GenericTreeNode();
			gtn.setAccessible(true);
			gtn.setTitle("");
			gtn.setAltText("");
			gtn.setUserObject(new TreePosition(parent, childcnt));
			ctn.addChild(gtn);
		}
		return ctn;
	}

	/**
	 * @param nodeId
	 * @return TreePosition
	 */
	public TreePosition getTreePosition(String nodeId) {
		TreeNode n = getNodeById(nodeId);
		GenericTreeNode gtn = (GenericTreeNode) n;
		TreePosition tp = (TreePosition) gtn.getUserObject();
		return tp;
	}
}
