/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;

import org.olat.core.id.Organisation;

/**
 * 
 */
public class OrganisationComparator implements Comparator<Organisation> {
	
	public OrganisationComparator() {
		//
	}

	@Override
	public int compare(Organisation o1, Organisation o2) {
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return -1;
		if(o2 == null) return 1;
		
		String n1 = o1.getDisplayName();
		String n2 = o2.getDisplayName();
		
		if(n1 == null && n2 == null) return 0;
		if(n1 == null) return -1;
		if(n2 == null) return 1;
		return n1.compareTo(n2);
	}
}
