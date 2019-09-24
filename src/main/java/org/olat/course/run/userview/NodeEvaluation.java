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

package org.olat.course.run.userview;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.nodes.GenericNode;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * Description:<br>
 * <pre>
 *  the nodeeval determines the treenode!
 *  first new()...
 *  sec: put,put,put
 *  thrd: build
 *  4th: addChildren
 * </pre>
 * 
 * @author Felix Jost
 */
public class NodeEvaluation extends GenericNode {

	private static final long serialVersionUID = -5576947730073187682L;
	
	private final CourseNode courseNode;
	private GenericTreeNode gtn;

	private final Map<String, Boolean> accesses = new HashMap<>(4);

	private boolean visible = false;
	private boolean atLeastOneAccessible = false;

	public NodeEvaluation(CourseNode courseNode) {
		this.courseNode = courseNode;
	}

	public void putAccessStatus(String capabilityName, boolean mayAccess) {
		accesses.put(capabilityName, new Boolean(mayAccess));
	}
	// <OLATCE-91>
	
	public boolean oldStyleConditionsOk(){
			return accesses.containsKey(AbstractAccessableCourseNode.BLOCKED_BY_ORIGINAL_ACCESS_RULES);
	}
	// </OLATCE-91>

	public boolean isCapabilityAccessible(String capabilityName) {
		if(accesses.get(capabilityName)!=null) {
		  return accesses.get(capabilityName).booleanValue();
		}
		return false;
	}

	public void addNodeEvaluationChild(NodeEvaluation chdNodeEval) {
		addChild(chdNodeEval);
		TreeNode chTn = chdNodeEval.getTreeNode();
		gtn.addChild(chTn);
	}

	public NodeEvaluation getNodeEvaluationChildAt(int pos) {
		return (NodeEvaluation) getChildAt(pos);
	}

	/**
	 * @return boolean
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets the visible.
	 * 
	 * @param visible The visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * 1. Calculate if the node should be accessible at all. <br/>
	 * 2. If the coursenode is visible, build a treenode.
	 */
	public void build() {
		// if at least one access	capability is true 
		for (Iterator<Boolean> iter = accesses.values().iterator(); iter.hasNext();) {
			Boolean entry = iter.next();
			atLeastOneAccessible = atLeastOneAccessible || entry.booleanValue();
		}

		// if the coursenode is visible, build a treenode
		if (isVisible()) {
			gtn = new GenericTreeNode(courseNode.getIdent());
			gtn.setTitle(courseNode.getShortTitle());
			gtn.setAltText(courseNode.getLongTitle());
			String type = courseNode.getType();
			CourseNodeConfiguration cnConfig = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(type);
			if(cnConfig != null) {
				String nodeCssClass = null;
				if (courseNode.getParent() == null) {
					// Spacial case for root node
					nodeCssClass = "o_CourseModule_icon";
				} else {
					nodeCssClass = cnConfig.getIconCSSClass();					
				}
				gtn.setIconCssClass(nodeCssClass);
			}
			gtn.setUserObject(this); // the current NodeEval is set into the treenode
																// as the userobject
			// all treenodes added here are set to be visible/accessible, since the
			// invisible are not pushed by convention
			gtn.setAccessible(true);
		}
		// else treenode is null
	}

	/**
	 * upon first call, the result is cached. Therefore first put all AccessStati,
	 * and then calculate the overall accessibility
	 * 
	 * @return
	 */
	public boolean isAtLeastOneAccessible() {
		return atLeastOneAccessible;
	}

	/**
	 * @return CourseNode
	 */
	public CourseNode getCourseNode() {
		return courseNode;
	}

	/**
	 * @return GenericTreeNode
	 */
	public TreeNode getTreeNode() {
		return gtn;
	}

	/**
	 * Only for special cases!!! like overriding coursenodes-children
	 * accessibility!! Sets the atLeastOneAccessible.
	 * 
	 * @param atLeastOneAccessible The atLeastOneAccessible to set
	 */
	public void setAtLeastOneAccessible(boolean atLeastOneAccessible) {
		this.atLeastOneAccessible = atLeastOneAccessible;
	}

}
