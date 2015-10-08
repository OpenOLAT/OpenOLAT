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
	private final Boolean fullyAssessed;
	
	private ScoreEvaluation() {
		this(null, null, null);
	}
	
	/**
	 * @param score
	 * @param passed
	 */
	public ScoreEvaluation(Float score, Boolean passed) {
		this(score, passed, null);
	}
	
	
	/**
	 * @param score
	 * @param passed
	 * @param fullyAssessed
	 */
	public ScoreEvaluation(final Float score, final Boolean passed, final Boolean fullyAssessed) {
		this(score, passed, fullyAssessed, null);
	}

	/**
	 * Constructor for passing the assessmentID.
	 * @param score
	 * @param passed
	 * @param assessmentID
	 */
	public ScoreEvaluation(Float score, Boolean passed, Boolean fullyAssessed, Long assessmentID) {
		this.score = score;
		this.passed = passed;
		this.assessmentID = assessmentID;
		this.fullyAssessed = fullyAssessed;
	}

	/**
	 * @return Returns the passed.
	 */
	public Boolean getPassed() {
		return passed;
	}

	/**
	 * @return Returns the score.
	 */
	public Float getScore() {
		return score;
	}
	
	/** (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "score:" + score + ", passed:" + passed + ", fullyAssessed " + fullyAssessed + ", S" + hashCode();
	}

  /**
   * 
   * @return Returns the assessmentID.
   */
	public Long getAssessmentID() {
		return assessmentID;
	}
	
	public Boolean getFullyAssessed() {
		return fullyAssessed;
	}
}
