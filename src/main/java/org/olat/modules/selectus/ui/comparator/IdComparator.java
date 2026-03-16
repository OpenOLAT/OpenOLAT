/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;

import org.olat.modules.selectus.model.ApplicationLight;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  1 mars 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class IdComparator implements Comparator<ApplicationLight> {
	
	private final LastnameComparator lastnameComparator = new LastnameComparator();

	@Override
	public int compare(ApplicationLight a1, ApplicationLight a2) {
		if(a1 == null) return 1;
		if(a2 == null) return -1;
		
		Integer i1 = a1.getId();
		Integer i2 = a2.getId();
		if(i1 == null) return 1;
		if(i2 == null) return -1;

		int result = i1.compareTo(i2);
		if(result == 0) {
			return lastnameComparator.compare(a1, a2);
		}
		return -result;
	}
}
