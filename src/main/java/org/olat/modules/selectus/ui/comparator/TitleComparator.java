/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;

import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Person;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  1 mars 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TitleComparator implements Comparator<ApplicationLight> {
	
	private final LastnameComparator lastnameComparator = new LastnameComparator();

	@Override
	public int compare(ApplicationLight a1, ApplicationLight a2) {
		if(a1 == null) return 1;
		if(a2 == null) return -1;
		
		Person p1 = a1.getPerson();
		Person p2 = a2.getPerson();
		if(p1 == null) return 1;
		if(p2 == null) return -1;
		
		String t1 = p1.getTitle();
		String t2 = p2.getTitle();
		if(!StringHelper.containsNonWhitespace(t1)) return 1;
		if(!StringHelper.containsNonWhitespace(t2)) return -1;
		int result = t1.compareToIgnoreCase(t2);
		if(result == 0) {
			return lastnameComparator.compare(a1, a2);
		}
		return result;
	}
}
