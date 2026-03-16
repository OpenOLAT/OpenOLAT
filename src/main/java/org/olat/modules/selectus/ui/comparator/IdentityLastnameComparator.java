/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;

import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  1 mars 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class IdentityLastnameComparator implements Comparator<Identity> {

	@Override
	public int compare(Identity i1, Identity i2) {
		if(i1 == null) return 1;
		if(i2 == null) return -1;
		
		User u1 = i1.getUser();
		User u2 = i2.getUser();
		
		if(u1 == null) return 1;
		if(u2 == null) return -1;
		
		String l1 = u1.getProperty(UserConstants.LASTNAME, null);
		String l2 = u2.getProperty(UserConstants.LASTNAME, null);
		if(l1 == null) return 1;
		if(l2 == null) return -1;
		int result = l1.compareToIgnoreCase(l2);
		if(result == 0) {
			String f1 = u1.getProperty(UserConstants.FIRSTNAME, null);
			String f2 = u2.getProperty(UserConstants.FIRSTNAME, null);
			return f1.compareToIgnoreCase(f2);
		}
		return result;
	}
}
