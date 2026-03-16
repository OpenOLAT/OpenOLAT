/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 21 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeMember {
	
	private String role;
	private String email;
	private Identity identity;
	private CommitteeMemberStatus status;
	
	private String identifier;
	
	public CommitteeMember(String role, String email, Identity identity) {
		this.role = role;
		this.email = email;
		this.identity = identity;
		if(identity.getKey() == null) {
			identifier = CodeHelper.getUniqueID();
		} else {
			identifier = identity.getKey().toString();
		}
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public CommitteeMemberStatus getStatus() {
		return status;
	}

	public void setStatus(CommitteeMemberStatus status) {
		this.status = status;
	}
}
