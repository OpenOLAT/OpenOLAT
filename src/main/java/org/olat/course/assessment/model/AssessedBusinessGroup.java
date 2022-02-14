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

import org.olat.group.BusinessGroupRef;

/**
 * 
 * Initial date: 09.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedBusinessGroup implements BusinessGroupRef {
	
	private final Long key;
	private final String name;
	
	private final int numOfParticipants;
	private final int numOfPassed;
	private final int numOfFailed;
	private final int numOfUndefined;
	private final int numDone;
	private final int numNotDone;
	private final double averageScore;
	private final boolean hasScore;
	
	public AssessedBusinessGroup(Long key, String name, double averageScore, boolean hasScore, int numOfPassed,
			int numOfFailed, int numOfUndefined, int numDone, int numNotDone, int numOfParticipants) {
		this.key = key;
		this.name = name;
		this.hasScore = hasScore;
		this.averageScore = averageScore;
		this.numOfPassed = numOfPassed;
		this.numOfFailed = numOfFailed;
		this.numOfUndefined = numOfUndefined;
		this.numDone = numDone;
		this.numNotDone = numNotDone;
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

	public int getNumDone() {
		return numDone;
	}

	public int getNumNotDone() {
		return numNotDone;
	}

	public boolean isHasScore() {
		return hasScore;
	}
}
