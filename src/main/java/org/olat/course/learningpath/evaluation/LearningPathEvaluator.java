/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.learningpath.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.nodes.INode;
import org.olat.course.learningpath.LearningPathRoles;
import org.olat.course.learningpath.ui.LearningPathTreeNode;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathEvaluator {
	
	private AccessEvaluator accessEvaluator;
	private LearningPathRoles roles;
	
	private LearningPathEvaluator(LearningPathEvaluatorBuilder builder) {
		this.accessEvaluator = builder.assessEvaluator;
		this.roles = builder.roles;
	}
	
	public void refresh(GenericTreeModel treeModel) {
		
		TreeNode node = treeModel.getRootNode();
		if (node instanceof LearningPathTreeNode) {
			LearningPathTreeNode currentNode = (LearningPathTreeNode)node;
			refreshNodeAndChildren(currentNode);
		}
	}

	private void refreshNodeAndChildren(LearningPathTreeNode currentNode) {
		int childCount = currentNode.getChildCount();
		List<LearningPathTreeNode> children = new ArrayList<>(childCount);
		for (int childIndex = 0; childIndex < childCount; childIndex++) {
			INode child = currentNode.getChildAt(childIndex);
			if (child instanceof LearningPathTreeNode) {
				LearningPathTreeNode childNode = (LearningPathTreeNode)child;
				refreshNodeAndChildren(childNode);
				children.add(childNode);
			}
		}
		
		refreshAccess(currentNode);
	}
	
	private void refreshAccess(LearningPathTreeNode currentNode) {
		if (accessEvaluator != null) {
			boolean accessible = accessEvaluator.isAccessible(currentNode, roles);
			currentNode.setAccessible(accessible);
		}
	}
	
	public static LearningPathEvaluatorBuilder builder() {
		return new LearningPathEvaluatorBuilder();
	}
	
	public static class LearningPathEvaluatorBuilder {

		private AccessEvaluator assessEvaluator;
		private LearningPathRoles roles;
		
		private LearningPathEvaluatorBuilder() {
			//
		}
		
		public LearningPathEvaluator build() {
			return new LearningPathEvaluator(this);
		}
		
		public LearningPathEvaluatorBuilder refreshAccess(AccessEvaluator accessEvaluator, LearningPathRoles roles) {
			this.assessEvaluator = accessEvaluator;
			this.roles = roles;
			return this;
		}
	}
}
