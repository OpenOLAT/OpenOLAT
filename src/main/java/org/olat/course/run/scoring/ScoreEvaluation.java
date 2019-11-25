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

package org.olat.course.run.scoring;

import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;

/**
 *  Description:<br>
 * @author Felix Jost
 */
public class ScoreEvaluation {
	//works because it's immutable
	public static final ScoreEvaluation EMPTY_EVALUATION = new ScoreEvaluation();
	
	private final Float score;
	private final Boolean passed; //could be Boolean.TRUE, Boolean.FALSE or null if "passed" info is not defined
	private final Long assessmentID;
	private final Boolean userVisible;
	private final AssessmentEntryStatus assessmentStatus;
	
	private Double currentRunCompletion;
	private AssessmentRunStatus runStatus;
	
	private ScoreEvaluation() {
		this(null, null);
	}
	
	/**
	 * This make a clone of the given score evaluation.
	 * 
	 * @param scoreEval
	 */
	public ScoreEvaluation(ScoreEvaluation scoreEval) {
		this(scoreEval.getScore(), scoreEval.getPassed(), scoreEval.getAssessmentStatus(), scoreEval.getUserVisible(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
	}

	public ScoreEvaluation(Float score, Boolean passed) {
		this(score, passed, null, null, null, null, null);
	}

	public ScoreEvaluation(Float score, Boolean passed, Long assessmentID) {
		this(score, passed, null, null, null, null, assessmentID);
	}
	
	public ScoreEvaluation(Float score, Boolean passed, AssessmentEntryStatus assessmentStatus,
			Boolean userVisible, Double currentRunCompletion, AssessmentRunStatus runStatus, Long assessmentID) {
		this.score = score;
		this.passed = passed;
		this.assessmentID = assessmentID;
		this.userVisible = userVisible;
		this.assessmentStatus = assessmentStatus;
		this.currentRunCompletion = currentRunCompletion;
		this.runStatus = runStatus;
	}

	public Boolean getPassed() {
		return passed;
	}

	public Float getScore() {
		return score;
	}
	
	public AssessmentEntryStatus getAssessmentStatus() {
		return assessmentStatus;
	}
	
	public Boolean getUserVisible() {
		return userVisible;
	}

	public Long getAssessmentID() {
		return assessmentID;
	}
	
	public Double getCurrentRunCompletion() {
		return currentRunCompletion;
	}
	
	public AssessmentRunStatus getCurrentRunStatus() {
		return runStatus;
	}
	
	@Override
	public String toString() {
		return "score:" + score + ", passed:" + passed + ", S" + hashCode();
	}
}
