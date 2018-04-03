package org.olat.basesecurity.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 29 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityPropertiesRow extends UserPropertiesRow {
	
	private final Date lastLogin;
	private final Date creationDate;
	
	public IdentityPropertiesRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		lastLogin = identity.getLastLogin();
		creationDate = identity.getCreationDate();
	}
	
	public IdentityPropertiesRow(Long identityKey, String identityName, Date creationDate, Date lastLogin, String[] identityProps) {
		super(identityKey, identityName, identityProps);
		this.creationDate = creationDate;
		this.lastLogin = lastLogin;	
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getLastLogin() {
		return lastLogin;
	}
}
