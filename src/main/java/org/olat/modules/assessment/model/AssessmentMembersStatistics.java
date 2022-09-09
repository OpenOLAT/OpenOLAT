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
package org.olat.modules.assessment.model;

/**
 * 
 * Initial date: 23.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentMembersStatistics {
	
	private final int numOfMembers;
	private final int numOfMembersLoggedIn;
	private final int numOfNonMembers;
	private final int numOfNonMembersLoggedIn;
	private final int numOfFakeParticipants;
	private final int numOfFakeParticipantsLoggedIn;
	
	public AssessmentMembersStatistics(int numOfMembers, int numOfMembersLoggedIn, int numOfNonMembers,
			int numOfNonMembersLoggedIn, int numOfFakeParticipants, int numOfFakeParticipantsLoggedIn) {
		this.numOfMembers = numOfMembers;
		this.numOfMembersLoggedIn = numOfMembersLoggedIn;
		this.numOfNonMembers = numOfNonMembers;
		this.numOfNonMembersLoggedIn = numOfNonMembersLoggedIn;
		this.numOfFakeParticipants = numOfFakeParticipants;
		this.numOfFakeParticipantsLoggedIn = numOfFakeParticipantsLoggedIn;
	}

	public int getNumOfMembers() {
		return numOfMembers;
	}

	public int getNumOfMembersLoggedIn() {
		return numOfMembersLoggedIn;
	}

	public int getNumOfNonMembers() {
		return numOfNonMembers;
	}
	
	public int getNumOfNonMembersLoggedIn() {
		return numOfNonMembersLoggedIn;
	}

	public int getNumOfFakeParticipants() {
		return numOfFakeParticipants;
	}

	public int getNumOfFakeParticipantsLoggedIn() {
		return numOfFakeParticipantsLoggedIn;
	}
	
}
