package org.olat.modules.curriculum.ui;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.modules.curriculum.model.CurriculumElementMember;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementUserRow extends UserPropertiesRow {
	
	private final String role;
	private final GroupMembershipInheritance inheritanceMode;
	
	public CurriculumElementUserRow(CurriculumElementMember member, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(member.getIdentity(), userPropertyHandlers, locale);
		role = member.getRole();
		inheritanceMode = member.getInheritanceMode();
	}
	
	public String getRole() {
		return role;
	}
	
	public GroupMembershipInheritance getInheritanceMode() {
		return inheritanceMode;
	}


}
