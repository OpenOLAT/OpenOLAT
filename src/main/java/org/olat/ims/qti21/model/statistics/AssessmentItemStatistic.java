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
package org.olat.ims.qti21.model.statistics;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;

/**
 * 
 * Initial date: 9 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemStatistic {
	
	private final AssessmentItem assessmentItem;
	private final double averageScore;
	private final double averageScoreAllParticipants;
	private final long numOfAnswers;
	private final long numOfCorrectAnswers;
	
	public AssessmentItemStatistic(AssessmentItem assessmentItem, double averageScore, double averageScoreAllParticipants,
			long numOfAnswers, long numOfCorrectAnswers) {
		this.assessmentItem = assessmentItem;
		this.averageScore = averageScore;
		this.averageScoreAllParticipants = averageScoreAllParticipants;
		this.numOfAnswers = numOfAnswers;
		this.numOfCorrectAnswers = numOfCorrectAnswers;
	}
	
	public AssessmentItem getAssessmentItem() {
		return assessmentItem;
	}
	
	/**
	 * Average build with the score and the number of answered questions.
	 * It doesn't take not answered questions in the count.
	 * @return
	 */
	public double getAverageScore() {
		return averageScore;
	}
	
	/**
	 * Average build with the score and the number of participants to the test.
	 * The average is built with the users which didn't answer the question.
	 * @return
	 */
	public double getAverageScoreAllParticipants() {
		return averageScoreAllParticipants;
	}

	public long getNumOfAnswers() {
		return numOfAnswers;
	}

	public long getNumOfCorrectAnswers() {
		return numOfCorrectAnswers;
	}
}
