/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;

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
public class FirstnameComparator implements Comparator<ApplicationLight> {

	@Override
	public int compare(ApplicationLight a1, ApplicationLight a2) {
		if(a1 == null) return 1;
		if(a2 == null) return -1;
		
		Person p1 = a1.getPerson();
		Person p2 = a2.getPerson();
		if(p1 == null) return 1;
		if(p2 == null) return -1;
		
		String f1 = p1.getFirstName();
		String f2 = p2.getFirstName();
		if(f1 == null) return 1;
		if(f2 == null) return -1;
		int result = f1.compareToIgnoreCase(f2);
		if(result == 0) {
			String l1 = p1.getLastName();
			String l2 = p2.getLastName();
			if(l1 == null) return 1;
			if(l2 == null) return -1;
			return l1.compareToIgnoreCase(l2);
		}
		return result;
	}
}
