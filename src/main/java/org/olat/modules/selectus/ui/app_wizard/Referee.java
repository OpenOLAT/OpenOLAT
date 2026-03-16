/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Referee {

	private final String title;
	private final String firstName;
	private final String lastName;
	private final String institution;
	private final String email;
	private final String fullName;
	
	public Referee(String title, String firstName, String lastName, String fullName, String institution, String email) {
		this.title = title;
		this.firstName = firstName;
		this.lastName = lastName;
		this.institution = institution;
		this.email = email;
		this.fullName = fullName;
	}
	
	public String getFullName() {
		return fullName;
	}

	public String getTitle() {
		return title;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getInstitution() {
		return institution;
	}

	public String getEmail() {
		return email;
	}
	
	public boolean isComplete() {
		return StringHelper.containsNonWhitespace(firstName)
				&& StringHelper.containsNonWhitespace(lastName)
				&& StringHelper.containsNonWhitespace(institution)
				&& StringHelper.containsNonWhitespace(email);
	}
}
