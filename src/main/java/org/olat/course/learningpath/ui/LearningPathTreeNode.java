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

import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 26 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathTreeNode extends CourseTreeNode {

	private static final long serialVersionUID = -9033714563825036957L;
	
	private final AssessmentEvaluation assessmentEvaluation;

	public LearningPathTreeNode(CourseNode courseNode, int treeLevel, AssessmentEvaluation assessmentEvaluation) {
		super(courseNode, treeLevel);
		this.assessmentEvaluation = assessmentEvaluation;
	}
	
	public Date getStartDate() {
		return assessmentEvaluation != null? assessmentEvaluation.getStartDate(): null;
	}

	public Date getEndDate() {
		return assessmentEvaluation != null? assessmentEvaluation.getEndDate(): null;
	}

	public Integer getDuration() {
		return assessmentEvaluation != null? assessmentEvaluation.getDuration(): null;
	}

	public AssessmentObligation getObligation() {
		return assessmentEvaluation != null? assessmentEvaluation.getObligation(): null;
	}
	
	public Date getFirstVisit() {
		return assessmentEvaluation != null? assessmentEvaluation.getFirstVisit(): null;
	}
	
	public Date getLastVisit() {
		return assessmentEvaluation != null? assessmentEvaluation.getLastVisit(): null;
	}

	public AssessmentEntryStatus getAssessmentStatus() {
		return assessmentEvaluation != null? assessmentEvaluation.getAssessmentStatus(): null;
	}

	public Date getAssessmentDone() {
		return assessmentEvaluation != null? assessmentEvaluation.getAssessmentDone(): null;
	}

	public Boolean getFullyAssessed() {
		return assessmentEvaluation != null? assessmentEvaluation.getFullyAssessed(): null;
	}
	
	public Date getFullyAssessedDate() {
		return assessmentEvaluation != null? assessmentEvaluation.getFullyAssessedDate(): null;
	}

	public Double getCompletion() {
		return assessmentEvaluation != null? assessmentEvaluation.getCompletion(): null;
	}

	public AssessmentEvaluation getAssessmentEvaluation() {
		return assessmentEvaluation;
	}
	
}
