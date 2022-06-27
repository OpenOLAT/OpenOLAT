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
package org.olat.course.assessment;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Sep 2021<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PasseableVisitor implements Visitor {

	private final RepositoryEntry courseEntry;
	private final CourseNode excludeNode;
	private final List<CourseNode> courseNodes = new ArrayList<>();

	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public PasseableVisitor(RepositoryEntry courseEntry, CourseNode excludeNode) {
		this.courseEntry = courseEntry;
		this.excludeNode = excludeNode;
		CoreSpringFactory.autowireObject(this);
	}

	public List<CourseNode> getCourseNodes() {
		return courseNodes;
	}

	@Override
	public void visit(INode node) {
		CourseEditorTreeNode editorNode = null;
		CourseNode courseNode = null;
		if (node instanceof CourseEditorTreeNode) {
			editorNode = (CourseEditorTreeNode) node;
			courseNode = editorNode.getCourseNode();
		} else if (node instanceof CourseNode) {
			courseNode = (CourseNode) node;
		}

		if (courseNode == null) {
			return;
		}

		if (editorNode != null && editorNode.isDeleted()) {
			return;
		}

		if (excludeNode != null && excludeNode.getIdent().equals(courseNode.getIdent())) {
			return;
		}

		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
		if (Mode.none != assessmentConfig.getPassedMode()) {
			courseNodes.add(courseNode);
		}
	}

}
