package org.olat.modules.bigbluebutton;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 25 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum JoinPolicyEnum {
	
	/** Disable manual meeting control */
	disabled,
	/** Guest must be approved by moderator */
	guestsApproval,
	/** All users must be approved by moderator */
	allUsersApproval;
	
	public static final JoinPolicyEnum secureValueOf(String val) {
		JoinPolicyEnum policy = JoinPolicyEnum.disabled;
		if(StringHelper.containsNonWhitespace(val)) {
			for(JoinPolicyEnum j:values()) {
				if(j.name().equals(val)) {
					policy = j;
				}
			}
		}
		return policy;
	}
}
