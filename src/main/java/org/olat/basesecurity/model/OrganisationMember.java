package org.olat.basesecurity.model;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 15 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationMember {
	
	private final Identity identity;
	private final String role;
	
	public OrganisationMember(Identity identity, String role) {
		this.identity = identity;
		this.role = role;
	}

	public Identity getIdentity() {
		return identity;
	}

	public String getRole() {
		return role;
	}
}