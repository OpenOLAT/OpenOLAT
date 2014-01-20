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
package org.olat.ims.qti.statistics.model;

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
	private double standardDeviation;
	private double median;
	private List<Double> mode;
	
	private long[] durations;
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

	public long[] getDurations() {
		return durations;
	}

	public void setDurations(long[] durations) {
		this.durations = durations;
	}

	public double[] getScores() {
		return scores;
	}

	public void setScores(double[] scores) {
		this.scores = scores;
	}
}