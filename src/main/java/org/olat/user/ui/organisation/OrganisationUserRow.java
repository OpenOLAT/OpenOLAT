package org.olat.user.ui.organisation;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.model.OrganisationMember;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 14 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationUserRow extends UserPropertiesRow {
	
	private final String role;
	
	public OrganisationUserRow(OrganisationMember member, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(member.getIdentity(), userPropertyHandlers, locale);
		role = member.getRole();
	}
	
	public String getRole() {
		return role;
	}

}
