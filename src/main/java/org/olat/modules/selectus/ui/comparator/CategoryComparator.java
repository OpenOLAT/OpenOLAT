/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;

import org.olat.modules.selectus.model.Category;

/**
 * 
 * Initial date: 17 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CategoryComparator implements Comparator<Category> {

	@Override
	public int compare(Category o1, Category o2) {
		if(o1 == null || o2 == null) {
			return compareNulls(o1, o2);
		}
		String n1 = o1.getName();
		String n2 = o2.getName();
		
		int c = 0;
		if(n1 == null || n2 == null) {
			c = compareNulls(o1, o2);
		} else {
			c = n1.compareToIgnoreCase(n2);
		}
		
		if(c == 0) {
			Long k1 = o1.getKey();
			Long k2 = o2.getKey();
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
