/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.coach.model;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.RelationRole;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SearchParticipantsStatisticsParams {
	
	private final Identity identity;
	private final GroupRoles role;
	private final RelationRole relationRole;
	private final List<Organisation> organisations;
	
	private boolean withOrganisations;
	private boolean withReservations;
	private boolean withCourseCompletion;
	private boolean withCourseStatus;
	
	private SearchParticipantsStatisticsParams(Identity identity, GroupRoles role,
			List<Organisation> organisations, RelationRole relationRole) {
		this.identity = identity;
		this.role = role;
		this.relationRole = relationRole;
		this.organisations = organisations;
	}
	
	public static SearchParticipantsStatisticsParams as(Identity identity, GroupRoles role) {
		return new SearchParticipantsStatisticsParams(identity, role, null, null);
	}
	
	public static SearchParticipantsStatisticsParams as(List<Organisation> organisations) {
		return new SearchParticipantsStatisticsParams(null, null, organisations, null);
	}
	
	public static SearchParticipantsStatisticsParams as(Identity identity, RelationRole relationRole) {
		return new SearchParticipantsStatisticsParams(identity, null, null, relationRole);
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public GroupRoles getRole() {
		return role;
	}

	public RelationRole getRelationRole() {
		return relationRole;
	}

	public List<Organisation> getOrganisations() {
		return organisations;
	}

	public boolean withOrganisations() {
		return withOrganisations;
	}

	public SearchParticipantsStatisticsParams withOrganisations(boolean withOrganisations) {
		this.withOrganisations = withOrganisations;
		return this;
	}

	public boolean withReservations() {
		return withReservations;
	}

	public SearchParticipantsStatisticsParams withReservations(boolean withReservations) {
		this.withReservations = withReservations;
		return this;
	}

	public boolean withCourseCompletion() {
		return withCourseCompletion;
	}

	public SearchParticipantsStatisticsParams withCourseCompletion(boolean withCourseCompletion) {
		this.withCourseCompletion = withCourseCompletion;
		return this;
	}

	public boolean withCourseStatus() {
		return withCourseStatus;
	}

	public SearchParticipantsStatisticsParams withCourseStatus(boolean withCourseStatus) {
		this.withCourseStatus = withCourseStatus;
		return this;
	}
	
	
	
	
}
