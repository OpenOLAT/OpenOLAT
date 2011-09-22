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
* <p>
*/ 

package org.olat.course.tree;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.course.nodes.CourseNode;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class InsertTreeModel extends GenericTreeModel {

	private int nodeCnt = -1;
	
	public InsertTreeModel(CourseEditorTreeModel cetm) {
		// build InsertModel (copy of this Structure with all possible
		// insert-positions)
		CourseEditorTreeNode cnRoot = (CourseEditorTreeNode) cetm.getRootNode();
		nodeCnt = 1;//one rootnode at least
		TreeNode ctn = buildNode(cnRoot);
		setRootNode(ctn);
	}

	private TreeNode buildNode(CourseEditorTreeNode cetn) {
		CourseEditorTreeNode ctn = new CourseEditorTreeNode(cetn);
		ctn.setAccessible(false);

		int childcnt = cetn.getChildCount();
		nodeCnt += childcnt;//add childcount to total node count
		if (childcnt > 0) {
			for (int i = 0; i < childcnt; i++) {
				CourseEditorTreeNode cchild = (CourseEditorTreeNode) cetn.getChildAt(i);
				if (!cchild.isDeleted()) {
					GenericTreeNode gtn = new GenericTreeNode();
					gtn.setAccessible(true);
					gtn.setTitle("");
					gtn.setAltText("");
					gtn.setUserObject(new TreePosition(ctn, i));
					ctn.addChild(gtn);
					TreeNode ctchild = buildNode(cchild);
					ctn.addChild(ctchild);
				}
			}
			// last insert position
			GenericTreeNode gtn = new GenericTreeNode();
			gtn.setAccessible(true);
			gtn.setTitle("");
			gtn.setAltText("");
			gtn.setUserObject(new TreePosition(ctn, childcnt));
			ctn.addChild(gtn);
		} else { // no children yet, propose one new position
			GenericTreeNode gtn = new GenericTreeNode();
			gtn.setAccessible(true);
			gtn.setTitle("");
			gtn.setAltText("");
			gtn.setUserObject(new TreePosition(ctn, 0));
			ctn.addChild(gtn);
		}
		return ctn;
	}

	public TreePosition getTreePosition(String nodeId) {
		TreeNode n = getNodeById(nodeId);
		GenericTreeNode gtn = (GenericTreeNode) n;
		TreePosition tp = (TreePosition) gtn.getUserObject();
		return tp;
	}

	public CourseNode getCourseNode(TreeNode tn) {
		CourseEditorTreeNode ctn = (CourseEditorTreeNode) tn;
		CourseNode cn = ctn.getCourseNode();
		return cn;
	}
	
	/**
	 * total number children of this course editor tree model, without 
	 * insert position.
	 * @return
	 */
	public int totalNodeCount(){
		return nodeCnt;
	}
}
