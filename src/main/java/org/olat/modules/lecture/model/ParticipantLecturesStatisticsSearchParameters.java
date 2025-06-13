package org.olat.modules.lecture.model;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 13 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantLecturesStatisticsSearchParameters {
	
	private Identity identity;
	private OrganisationRoles limitToRole;
	
	public ParticipantLecturesStatisticsSearchParameters(Identity identity) {
		this.identity = identity;
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public OrganisationRoles getLimitToRole() {
		return limitToRole;
	}

	public void setLimitToRole(OrganisationRoles limitToRole) {
		this.limitToRole = limitToRole;
	}
}
