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

import java.util.HashSet;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.course.assessment.AssessmentMode;

/**
 * 
 * Initial date: 11 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeStatistics {
	
	private AssessmentMode.Status status;

	private final Set<Long> loggedInIdentitiesKeys = new HashSet<>();
	private final Set<Long> startedAssessedIdentitiesKeys = new HashSet<>();
	private final Set<Long> waitingAssessedIdentitiesKeys = new HashSet<>();
	private final Set<Long> plannedAssessedIdentitiesKeys = new HashSet<>();
	
	public AssessmentModeStatistics() {
		//
	}

	public AssessmentMode.Status getStatus() {
		return status;
	}

	public void setStatus(AssessmentMode.Status status) {
		this.status = status;
	}
	
	/**
	 * @return The list of identities keys which are not in one stage
	 * 		or the other in OpenOlat.
	 */
	public synchronized Set<Long> getNotRegisteredAssessedIdentitiesKeys() {
		Set<Long> all = new HashSet<>(plannedAssessedIdentitiesKeys);
		all.removeAll(loggedInIdentitiesKeys);
		all.removeAll(waitingAssessedIdentitiesKeys);
		all.removeAll(startedAssessedIdentitiesKeys);
		return all;
	}
	
	public synchronized int getNumInOpenOlat() {
		return waitingAssessedIdentitiesKeys.size()
				+ startedAssessedIdentitiesKeys.size()
				+ loggedInIdentitiesKeys.size();
	}

	public synchronized int getNumPlanned() {
		return plannedAssessedIdentitiesKeys.size();
	}

	public synchronized int getNumWaiting() {
		return waitingAssessedIdentitiesKeys.size();
	}

	public synchronized int getNumStarted() {
		return startedAssessedIdentitiesKeys.size();
	}
	
	public boolean isAssessedIdentity(Long identityKey) {
		return waitingAssessedIdentitiesKeys.contains(identityKey)
				|| startedAssessedIdentitiesKeys.contains(identityKey)
				|| loggedInIdentitiesKeys.contains(identityKey);
	}
	
	public synchronized void addLoggedInIdentitiesKeys(IdentityRef identity) {
		if(!waitingAssessedIdentitiesKeys.contains(identity.getKey()) && !startedAssessedIdentitiesKeys.contains(identity.getKey())) {
			loggedInIdentitiesKeys.add(identity.getKey());	
		}
	}
	
	public synchronized void addStartedAssessedIdentity(IdentityRef identity) {
		loggedInIdentitiesKeys.remove(identity.getKey());
		waitingAssessedIdentitiesKeys.remove(identity.getKey());
		startedAssessedIdentitiesKeys.add(identity.getKey());
	}
	
	public synchronized void addWaitingAssessedIdentity(IdentityRef identity) {
		loggedInIdentitiesKeys.remove(identity.getKey());
		waitingAssessedIdentitiesKeys.add(identity.getKey());
		startedAssessedIdentitiesKeys.remove(identity.getKey());
	}
	
	public synchronized void updatePlannedAssessedIdentitiesKeys(Set<Long> assessedIdentitiesKeys) {
		plannedAssessedIdentitiesKeys.clear();
		plannedAssessedIdentitiesKeys.addAll(assessedIdentitiesKeys);
	}
}
