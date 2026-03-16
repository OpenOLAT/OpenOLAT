/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.List;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 7 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ExternalUserResults {
	
	private final List<Identity> externalUsers;
	private final List<String> existingUsers;
	
	public ExternalUserResults(List<Identity> externalUsers, List<String> existingUsers) {
		this.externalUsers = externalUsers;
		this.existingUsers = existingUsers;
	}

	public List<Identity> getExternalUsers() {
		return externalUsers;
	}

	public List<String> getExistingUsers() {
		return existingUsers;
	}
}
