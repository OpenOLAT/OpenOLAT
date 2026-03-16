/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;
import java.util.Locale;

import org.olat.modules.selectus.model.OrganisationUnit;

/**
 * 
 * Initial date: 16 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationUnitComparator implements Comparator<OrganisationUnit> {
	
	private final Locale locale;
	
	public OrganisationUnitComparator(Locale locale) {
		this.locale = locale;
	}

	@Override
	public int compare(OrganisationUnit o1, OrganisationUnit o2) {
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return -1;
		if(o2 == null) return 1;
		
		String n1 = o1.getMLName(locale);
		String n2 = o2.getMLName(locale);
		
		if(n1 == null && n2 == null) return 0;
		if(n1 == null) return -1;
		if(n2 == null) return 1;
		return n1.compareTo(n2);
	}
}
