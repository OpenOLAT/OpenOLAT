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

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.RelationRole;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.coach.CoachingRoles;

/**
 * 
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SearchParticipantsStatisticsParams {
	
	private final Identity identity;
	private final List<CoachingRoles> roles;
	private final RelationRole relationRole;
	private final List<Organisation> organisations;
	
	private boolean withOrganisations;
	private boolean withReservations;
	private boolean withCourseCompletion;
	private boolean withCourseStatus;
	private List<OrganisationRoles> excludedRoles;
	private List<OrganisationRoles> readOnlyRoles;
	
	private SearchParticipantsStatisticsParams(Identity identity, List<CoachingRoles> roles,
			List<Organisation> organisations, RelationRole relationRole) {
		this.identity = identity;
		this.roles = roles;
		this.relationRole = relationRole;
		this.organisations = organisations;
	}
	
	public static SearchParticipantsStatisticsParams as(Identity identity, List<CoachingRoles> roles) {
		return new SearchParticipantsStatisticsParams(identity, roles, null, null);
	}
	
	public static SearchParticipantsStatisticsParams as(List<Organisation> organisations) {
		return new SearchParticipantsStatisticsParams(null, List.of(), organisations, null);
	}
	
	public static SearchParticipantsStatisticsParams as(Identity identity, RelationRole relationRole) {
		return new SearchParticipantsStatisticsParams(identity, List.of(), null, relationRole);
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public List<CoachingRoles> getRoles() {
		return roles;
	}

	public RelationRole getRelationRole() {
		return relationRole;
	}

	public List<Organisation> getOrganisations() {
		return organisations == null ? List.of() : organisations;
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
	
	public List<OrganisationRoles> excludedRoles() {
		return excludedRoles;
	}
	
	public SearchParticipantsStatisticsParams excludedRoles(List<OrganisationRoles> roles) {
		this.excludedRoles = roles;
		return this;
	}

	public List<OrganisationRoles> readOnlyRoles() {
		return readOnlyRoles;
	}

	public SearchParticipantsStatisticsParams withReadOnlyRoles(List<OrganisationRoles> readOnlyRoles) {
		this.readOnlyRoles = readOnlyRoles;
		return this;
	}
	
	
	
}
