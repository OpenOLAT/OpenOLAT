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

import org.olat.course.learningpath.SequenceConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.Overridable;
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
	
	private final SequenceConfig sequenceConfig;
	private final AssessmentEvaluation assessmentEvaluation;

	public LearningPathTreeNode(CourseNode courseNode, int treeLevel, SequenceConfig sequenceConfig,
			AssessmentEvaluation assessmentEvaluation) {
		super(courseNode, treeLevel);
		this.sequenceConfig = sequenceConfig;
		this.assessmentEvaluation = assessmentEvaluation;
	}

	public SequenceConfig getSequenceConfig() {
		return sequenceConfig;
	}

	public Date getStartDate() {
		return hasNotExcludedEvaluation()? assessmentEvaluation.getStartDate(): null;
	}

	public Overridable<Date> getEndDate() {
		return hasNotExcludedEvaluation()? assessmentEvaluation.getEndDate(): null;
	}

	public Integer getDuration() {
		return hasNotExcludedEvaluation()? assessmentEvaluation.getDuration(): null;
	}

	public ObligationOverridable getObligation() {
		return assessmentEvaluation != null? assessmentEvaluation.getObligation(): null;
	}
	
	public Date getFirstVisit() {
		return hasNotExcludedEvaluation()? assessmentEvaluation.getFirstVisit(): null;
	}
	
	public Date getLastVisit() {
		return hasNotExcludedEvaluation()? assessmentEvaluation.getLastVisit(): null;
	}

	public AssessmentEntryStatus getAssessmentStatus() {
		return hasNotExcludedEvaluation()? assessmentEvaluation.getAssessmentStatus(): null;
	}

	public Date getAssessmentDone() {
		return hasNotExcludedEvaluation()? assessmentEvaluation.getAssessmentDone(): null;
	}

	public Boolean getFullyAssessed() {
		return hasNotExcludedEvaluation()? assessmentEvaluation.getFullyAssessed(): null;
	}
	
	public Date getFullyAssessedDate() {
		return hasNotExcludedEvaluation()? assessmentEvaluation.getFullyAssessedDate(): null;
	}

	public Double getCompletion() {
		return hasNotExcludedEvaluation()? assessmentEvaluation.getCompletion(): null;
	}

	public AssessmentEvaluation getAssessmentEvaluation() {
		return hasNotExcludedEvaluation()? assessmentEvaluation: null;
	}

	private boolean hasNotExcludedEvaluation() {
		return assessmentEvaluation == null
				|| assessmentEvaluation.getObligation() == null
				|| AssessmentObligation.excluded != assessmentEvaluation.getObligation().getCurrent();
	}
	
}
