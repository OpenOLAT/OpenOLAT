/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.assignment;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentKey {
	
	private final Long identityKey;
	private final Long applicationKey;
	
	public AssignmentKey(Long identityKey, Long applicationKey) {
		this.identityKey = identityKey;
		this.applicationKey = applicationKey;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public Long getApplicationKey() {
		return applicationKey;
	}

	@Override
	public int hashCode() {
		return identityKey.hashCode() + applicationKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AssignmentKey) {
			AssignmentKey assignmentKey = (AssignmentKey)obj;
			return identityKey.equals(assignmentKey.identityKey)
					&& applicationKey.equals(assignmentKey.applicationKey);
		}
		return false;
	}
}
