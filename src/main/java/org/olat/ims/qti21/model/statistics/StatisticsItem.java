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

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class StatisticsItem {
	
	private int numOfResults;
	private double averageScore;
	private long averageDuration;
	private double difficulty;
	private long numOfCorrectAnswers;
	private long numOfIncorrectAnswers = 0;
	
	public int getNumOfResults() {
		return numOfResults;
	}
	
	public void setNumOfResults(int numOfResults) {
		this.numOfResults = numOfResults;
	}
	
	public double getAverageScore() {
		return averageScore;
	}
	
	public void setAverageScore(double averageScore) {
		this.averageScore = averageScore;
	}
	
	/**
	 * @return The average duration in milliseconds.
	 */
	public long getAverageDuration() {
		return averageDuration;
	}
	
	/**
	 * @param averageDuration The average duration in milliseconds
	 */
	public void setAverageDuration(long averageDuration) {
		this.averageDuration = averageDuration;
	}

	public double getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(double difficulty) {
		this.difficulty = difficulty;
	}

	public long getNumOfCorrectAnswers() {
		return numOfCorrectAnswers;
	}

	public void setNumOfCorrectAnswers(long numOfCorrectAnswers) {
		this.numOfCorrectAnswers = numOfCorrectAnswers;
	}

	public long getNumOfIncorrectAnswers() {
		return numOfIncorrectAnswers;
	}

	public void setNumOfIncorrectAnswers(long numOfIncorrectAnswers) {
		this.numOfIncorrectAnswers = numOfIncorrectAnswers;
	}
	
	
}
