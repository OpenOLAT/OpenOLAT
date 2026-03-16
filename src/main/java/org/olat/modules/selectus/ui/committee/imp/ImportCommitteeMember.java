/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.imp;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Initial date: 29 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportCommitteeMember {
	
	private final Identity identity;
	private final PositionRole role;
	
	public ImportCommitteeMember(Identity identity, PositionRole role) {
		this.identity = identity;
		this.role = role;
	}

	public Identity getIdentity() {
		return identity;
	}

	public PositionRole getRole() {
		return role;
	}
}
