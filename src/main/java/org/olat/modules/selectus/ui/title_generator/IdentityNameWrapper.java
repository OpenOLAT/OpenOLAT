/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.title_generator;

import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;

import org.olat.modules.selectus.model.PersonName;

/**
 * 
 * Initial date: 25 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityNameWrapper implements PersonName {
	
	private final Identity identity;
	private final Locale locale;

	public IdentityNameWrapper(Identity identity, Locale locale) {
		this.identity = identity;
		this.locale = locale;
	}
	
	@Override
	public String getTitle() {
		String title = identity.getUser().getProperty(UserConstants.TITLE, locale);
		if("-".equals(title)) {
			return null;
		}
		return title;
	}

	@Override
	public String getFirstName() {
		return identity.getUser().getProperty(UserConstants.FIRSTNAME, locale);
	}

	@Override
	public String getLastName() {
		return identity.getUser().getProperty(UserConstants.LASTNAME, locale);
	}
}
