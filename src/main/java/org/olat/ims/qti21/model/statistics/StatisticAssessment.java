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

import java.util.List;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatisticAssessment {
	
	private int numOfParticipants;
	private int numOfPassed;
	private int numOfFailed;
	private long averageDuration;
	
	private double average;
	private double range;
	private double minScore;
	private double maxScore;
	private double standardDeviation;
	private double median;
	private List<Double> mode;
	
	private double[] durations;
	private double[] scores;
	
	public int getNumOfParticipants() {
		return numOfParticipants;
	}
	
	public void setNumOfParticipants(int numOfParticipants) {
		this.numOfParticipants = numOfParticipants;
	}
	
	public int getNumOfPassed() {
		return numOfPassed;
	}
	
	public void setNumOfPassed(int numOfPassed) {
		this.numOfPassed = numOfPassed;
	}
	
	public int getNumOfFailed() {
		return numOfFailed;
	}
	
	public void setNumOfFailed(int numOfFailed) {
		this.numOfFailed = numOfFailed;
	}
	
	public long getAverageDuration() {
		return averageDuration;
	}
	
	public void setAverageDuration(long averageDuration) {
		this.averageDuration = averageDuration;
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public double getMedian() {
		return median;
	}

	public void setMedian(double median) {
		this.median = median;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getMinScore() {
		return minScore;
	}

	public void setMinScore(double minScore) {
		this.minScore = minScore;
	}

	public double getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(double maxScore) {
		this.maxScore = maxScore;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public List<Double> getMode() {
		return mode;
	}

	public void setMode(List<Double> mode) {
		this.mode = mode;
	}

	/**
	 * @return Duration in secondes
	 */
	public double[] getDurations() {
		return durations;
	}

	public void setDurations(double[] durations) {
		this.durations = durations;
	}

	public double[] getScores() {
		return scores;
	}

	public void setScores(double[] scores) {
		this.scores = scores;
	}
}