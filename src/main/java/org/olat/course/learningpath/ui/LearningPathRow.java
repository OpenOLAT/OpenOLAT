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
package org.olat.course.learningpath.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.course.learningpath.LearningPathObligation;
import org.olat.course.learningpath.LearningPathStatus;

/**
 * 
 * Initial date: 16 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathRow implements FlexiTreeTableNode, IndentedCourseNode {
	
	private final LearningPathTreeNode learningPathNode;
	private LearningPathRow parent;
	private boolean hasChildren;
	
	public LearningPathRow(LearningPathTreeNode learningPathNode) {
		this.learningPathNode = learningPathNode;
	}

	@Override
	public int getRecursionLevel() {
		return learningPathNode.getRecursionLevel();
	}

	@Override
	public String getType() {
		return learningPathNode.getCourseNode().getType();
	}

	@Override
	public String getShortTitle() {
		return learningPathNode.getCourseNode().getShortName();
	}

	@Override
	public String getLongTitle() {
		return learningPathNode.getCourseNode().getLongTitle();
	}

	public LearningPathStatus getStatus() {
		return learningPathNode.getStatus();
	}

	public Date getDateDone() {
		return learningPathNode.getDateDone();
	}

	public LearningPathObligation getObligation() {
		return learningPathNode.getObligation();
	}

	public Integer getDuration() {
		return learningPathNode.getDuration();
	}

	public Integer getProgress() {
		return learningPathNode.getProgress();
	}

	@Override
	public String getCrump() {
		return null;
	}

	public void setParent(LearningPathRow parent) {
		this.parent = parent;
		if (parent != null) {
			parent.hasChildren = true;
		}
	}

	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}

	public boolean hasChildren() {
		return hasChildren;
	}

}
