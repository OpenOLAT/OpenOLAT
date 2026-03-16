/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.review;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;

import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 21 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Reviewer implements IdentityRef, Comparable<Reviewer> {
	
	private final Long identityKey;
	private final String fullName;
	
	public Reviewer(Identity identity) {
		identityKey = identity.getKey();
		fullName = RecruitingHelper.formatFullName(identity);
	}

	@Override
	public Long getKey() {
		return identityKey;
	}
	
	public String getFullName() {
		return fullName;
	}

	@Override
	public int compareTo(Reviewer o) {
		int c;
		if(o == null) {
			c = -1;
		} else if(fullName == null && o.fullName == null) {
			c = 0;
		} else if(fullName == null) {
			c = 1;
		} else if(o.fullName == null) {
			c = -1;
		} else {
			c = fullName.compareTo(o.fullName);
		}
		
		if(c == 0 && o != null) {
			c = identityKey.compareTo(o.identityKey);
		}
		return c;
	}
	
	

}
