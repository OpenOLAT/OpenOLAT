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
import org.olat.core.gui.components.tree.InsertionPoint.Position;
import org.olat.core.gui.components.tree.InsertionTreeModel;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;

/**
 * Initial Date: Jan 05, 2005 <br>
 * 
 * @author mike
 */
public class InsertItemTreeModel extends GenericTreeModel implements InsertionTreeModel {

	private static final long serialVersionUID = 8416409302317405234L;
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

	private final Object source;
	private final int appendToInstancesOf;

	/**
	 * @param treeModel
	 * @param appendToInstancesOf
	 */
	public InsertItemTreeModel(TreeModel treeModel,  Object source, int appendToInstancesOf) {
		this.source = source;
		this.appendToInstancesOf = appendToInstancesOf;
		GenericQtiNode cnRoot = (GenericQtiNode) treeModel.getRootNode();
		TreeNode ctn = buildNode(cnRoot);
		setRootNode(ctn);
	}

	@Override
	public boolean isSource(TreeNode node) {
		return source == node.getUserObject();
	}

	@Override
	public Position[] getInsertionPosition(TreeNode node) {
		if(INSTANCE_ASSESSMENT == appendToInstancesOf) {
			if(node.getUserObject() instanceof AssessmentNode) {
				return new Position[] { Position.under };
			} else if(node.getUserObject() instanceof SectionNode) {
				return new Position[] { Position.up, Position.down };
			}
		} else if(INSTANCE_SECTION == appendToInstancesOf) {
			if(node.getUserObject() instanceof SectionNode) {
				return new Position[] { Position.under };
			} else if(node.getUserObject() instanceof ItemNode) {
				return new Position[] { Position.up, Position.down };
			}
		} 
		return new Position[0];
	}

	private TreeNode buildNode(TreeNode parent) {
		GenericTreeNode ctn = new GenericTreeNode(parent.getTitle(), parent);
		ctn.setIconCssClass(parent.getIconCssClass());
		int childcnt = parent.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			// add child itself
			TreeNode ctchild = buildNode((TreeNode)parent.getChildAt(i));
			ctn.addChild(ctchild);
		}
		return ctn;
	}
}
