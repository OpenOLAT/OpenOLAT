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
package org.olat.course.assessment.model;

import java.math.BigDecimal;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentStatistics {

	private int countTotal;
	private int countPassed;
	private int countFailed;
	private int countUndefined;
	private int countDone;
	private int countNotDone;
	private int countScore;
	private Double averageScore;
	private BigDecimal maxScore;
	
	public int getCountTotal() {
		return countTotal;
	}

	public void setCountTotal(int countTotal) {
		this.countTotal = countTotal;
	}

	public int getCountPassed() {
		return countPassed;
	}
	
	public void setCountPassed(int countPassed) {
		this.countPassed = countPassed;
	}
	
	public int getCountFailed() {
		return countFailed;
	}
	
	public void setCountFailed(int countFailed) {
		this.countFailed = countFailed;
	}
	
	public int getCountUndefined() {
		return countUndefined;
	}

	public void setCountUndefined(int countUndefined) {
		this.countUndefined = countUndefined;
	}

	public int getCountDone() {
		return countDone;
	}

	public void setCountDone(int countDone) {
		this.countDone = countDone;
	}

	public int getCountNotDone() {
		return countNotDone;
	}

	public void setCountNotDone(int countNotDone) {
		this.countNotDone = countNotDone;
	}

	public int getCountScore() {
		return countScore;
	}

	public void setCountScore(int countScore) {
		this.countScore = countScore;
	}

	public Double getAverageScore() {
		return averageScore;
	}
	
	public void setAverageScore(Double averageScore) {
		this.averageScore = averageScore;
	}

	public BigDecimal getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(BigDecimal maxScore) {
		this.maxScore = maxScore;
	}
}
