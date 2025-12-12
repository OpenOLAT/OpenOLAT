package org.olat.modules.certificationprogram.ui.wizard;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 12 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record UserToCertify(Identity identity,  UserMembershipStatus currentStatus) {
	//
}
