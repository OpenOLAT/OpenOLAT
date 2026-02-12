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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityOrganisationsRow;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.id.Identity;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantStatisticsEntry extends UserPropertiesRow implements IdentityOrganisationsRow {
	
	private static final Entries NO_ATTEMPTS = new Entries(0l, 0l, 0l);
	private static final Certificates NO_CERTIFICATES = new Certificates(0l, 0l, 0l);
	private static final SuccessStatus NO_SUCCESS = new SuccessStatus(0l, 0l, 0l, 0l);
	
	private Date lastVisit;
	private Double averageCompletion;

	private Entries entries = NO_ATTEMPTS;
	private Certificates certificates = NO_CERTIFICATES;
	private SuccessStatus successStatus = NO_SUCCESS;
	
	private long reservations = 0;
	private long reservationsConfirmedByUser = 0;
	private long reservationsConfirmedByAdmin = 0;

	private String onlineStatus;
	
	private List<OrganisationWithParents> organisations;
	
	private boolean readOnlyDueToAdditionalOrgRoles;

	public ParticipantStatisticsEntry(Long identityKey, String externalId, List<UserPropertyHandler> userPropertyHandlers, String[] userProperties, Locale locale) {
		super(identityKey, externalId, userPropertyHandlers, userProperties, locale);
	}

	public ParticipantStatisticsEntry(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
	}

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}
	
	public Entries getEntries() {
		return entries;
	}

	public void setEntries(Entries entries) {
		this.entries = entries;
	}

	public SuccessStatus getSuccessStatus() {
		return successStatus;
	}

	public void setSuccessStatus(SuccessStatus successStatus) {
		this.successStatus = successStatus;
	}

	public Certificates getCertificates() {
		return certificates;
	}

	public void setCertificates(Certificates certificates) {
		this.certificates = certificates;
	}

	public Double getAverageCompletion() {
		return averageCompletion;
	}
	
	public void setAverageCompletion(Double averageCompletion) {
		this.averageCompletion = averageCompletion;
	}

	public Date getLastVisit() {
		return lastVisit;
	}

	public void setLastVisit(Date lastVisit) {
		this.lastVisit = lastVisit;
	}
	
	public long getReservations() {
		return reservations;
	}

	public void addReservations(long reservations) {
		this.reservations += reservations;
	}

	public long getReservationsConfirmedByUser() {
		return reservationsConfirmedByUser;
	}

	public void addReservationsConfirmedByUser(long reservationsConfirmedByUser) {
		this.reservationsConfirmedByUser += reservationsConfirmedByUser;
	}

	public long getReservationsConfirmedByAdmin() {
		return reservationsConfirmedByAdmin;
	}

	public void addReservationsConfirmedByAdmin(long reservationsConfirmedByAdmin) {
		this.reservationsConfirmedByAdmin += reservationsConfirmedByAdmin;
	}
	
	public List<OrganisationWithParents> getOrganisations() {
		return organisations;
	}

	@Override
	public void setOrganisations(List<OrganisationWithParents> organisations) {
		this.organisations = organisations;
	}

	public boolean isReadOnlyDueToAdditionalOrgRoles() {
		return readOnlyDueToAdditionalOrgRoles;
	}

	public void setReadOnlyDueToAdditionalOrgRoles(boolean readOnlyDueToAdditionalOrgRoles) {
		this.readOnlyDueToAdditionalOrgRoles = readOnlyDueToAdditionalOrgRoles;
	}

	public record Entries(long numOfEntries, long numOfVisited, long numOfNotVisited) {
		//
	}

	public record SuccessStatus(long numPassed, long numFailed, long numUndefined, long total) {
		//
	}
	
	public record Certificates(long numOfCertificates, long numOfCoursesWithCertificates, long numOfCoursesWithInvalidCertificates) {
		//
	}
}
