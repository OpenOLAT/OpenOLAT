/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.cns.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;

/**
 * 
 * Initial date: 24 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSParticipantDetailsRow implements IndentedCourseNode {
	
	private final CourseNode courseNode;
	private AssessmentEvaluation evaluation;
	private LearningPathStatus learningPathStatus;
	private FormLink obligationFormItem;

	public CNSParticipantDetailsRow(CourseNode courseNode) {
		this.courseNode = courseNode;
	}
	
	public CourseNode getCourseNode() {
		return courseNode;
	}

	@Override
	public String getType() {
		return courseNode.getType();
	}

	@Override
	public String getShortTitle() {
		return courseNode.getShortTitle();
	}

	@Override
	public String getLongTitle() {
		return courseNode.getLongTitle();
	}

	@Override
	public int getRecursionLevel() {
		return 0;
	}
	
	public AssessmentEvaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(AssessmentEvaluation evaluation) {
		this.evaluation = evaluation;
	}

	public LearningPathStatus getLearningPathStatus() {
		return learningPathStatus;
	}

	public void setLearningPathStatus(LearningPathStatus learningPathStatus) {
		this.learningPathStatus = learningPathStatus;
	}

	public FormLink getObligationFormItem() {
		return obligationFormItem;
	}

	public void setObligationFormItem(FormLink obligationFormItem) {
		this.obligationFormItem = obligationFormItem;
	}

}
