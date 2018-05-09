package org.olat.modules.curriculum.model;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementMember {

	private final Identity identity;
	private final String role;
	private final GroupMembershipInheritance inheritanceMode;
	
	public CurriculumElementMember(Identity identity, String role, GroupMembershipInheritance inheritanceMode) {
		this.identity = identity;
		this.role = role;
		this.inheritanceMode = inheritanceMode;
	}

	public Identity getIdentity() {
		return identity;
	}

	public String getRole() {
		return role;
	}
	
	public GroupMembershipInheritance getInheritanceMode() {
		return inheritanceMode;
	}
	
}
