/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;
import java.util.List;

import org.olat.modules.selectus.ui.model.AppToCategory;

/**
 * 
 * Initial date: 2 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AppToCategoryListComparator implements Comparator<List<AppToCategory>> {
	
	private final boolean asc;
	private final AppToCategoryComparator catComparator = new AppToCategoryComparator();
	
	public AppToCategoryListComparator(boolean asc) {
		this.asc = asc;
	}

	@Override
	public int compare(List<AppToCategory> o1, List<AppToCategory> o2) {
		boolean empty1 = o1 == null || o1.isEmpty();
		boolean empty2 = o2 == null || o2.isEmpty();
		if(empty1 && empty2) {
			return 0;
		}
		if(empty1) {
			return asc ? 1 : -1;
		}
		if(empty2) {
			return asc ? -1 : 1;
		}
		
		int s1 = o1.size();
		int s2 = o2.size();
		int steps = Math.min(s1, s2);
		
		int c = 0;
		for(int i=0; i<steps && c == 0; i++) {
			c = compare(o1, o2, i);
		}
		return c;
	}
	
	public int compare(List<AppToCategory> o1, List<AppToCategory> o2, int step) {
		int s1 = o1.size() - 1;
		int s2 = o2.size() - 1;
		
		int c = 0;
		if(s1 < step && s2 < step) {
			c = 0;
		} else if(s1 < step) {
			c = 1;
		} else if (s2 < step) {
			c = -1;
		} else {
			AppToCategory c1 = o1.get(step);
			AppToCategory c2 = o2.get(step);
			c = catComparator.compare(c1, c2);
		}
		return c;
	}
}
