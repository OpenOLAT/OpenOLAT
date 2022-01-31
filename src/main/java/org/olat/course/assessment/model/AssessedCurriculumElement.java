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

import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 19 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessedCurriculumElement implements CurriculumElementRef {
	
	private final Long key;
	private final String name;
	
	private final int numOfParticipants;
	private final int numOfPassed;
	private final int numOfFailed;
	private final int numOfUndefined;
	private final double averageScore;
	private final boolean hasScore;
	
	public AssessedCurriculumElement(Long key, String name, double averageScore, boolean hasScore,
			int numOfPassed, int numOfFailed, int numOfUndefined, int numOfParticipants) {
		this.key = key;
		this.name = name;
		this.hasScore = hasScore;
		this.averageScore = averageScore;
		this.numOfPassed = numOfPassed;
		this.numOfFailed = numOfFailed;
		this.numOfUndefined = numOfUndefined;
		this.numOfParticipants = numOfParticipants;
		
	}

	@Override
	public Long getKey() {
		return key;
	}

	public String getName() {
		return name;
	}
	
	public double getAverageScore() {
		return averageScore;
	}

	public int getNumOfParticipants() {
		return numOfParticipants;
	}

	public int getNumOfPassed() {
		return numOfPassed;
	}

	public int getNumOfFailed() {
		return numOfFailed;
	}
	
	public int getNumOfUndefined() {
		return numOfUndefined;
	}

	public boolean isHasScore() {
		return hasScore;
	}
}
