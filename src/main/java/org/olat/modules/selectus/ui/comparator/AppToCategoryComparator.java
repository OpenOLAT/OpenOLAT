/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;

import org.olat.modules.selectus.ui.model.AppToCategory;

/**
 * 
 * Initial date: 2 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AppToCategoryComparator implements Comparator<AppToCategory> {
	
	@Override
	public int compare(AppToCategory o1, AppToCategory o2) {
		if(o1 == null || o2 == null) {
			return compareNulls(o1, o2);
		}
		String n1 = o1.getCategoryName();
		String n2 = o2.getCategoryName();
		
		int c = 0;
		if(n1 == null || n2 == null) {
			c = compareNulls(o1, o2);
		} else {
			c = n1.compareToIgnoreCase(n2);
		}
		
		if(c == 0) {
			Long k1 = o1.getCategoryKey();
			Long k2 = o2.getCategoryKey();
			c = k1.compareTo(k2);
		}
		return c;
	}
	
	private int compareNulls(Object o1, Object o2) {
		if(o1 == null && o2 == null) {
			return 0;
		}
		return o1 == null ? -1 : 1;
	}
}
