package org.olat.modules.lecture.ui.component;

import java.util.Comparator;
import java.util.Date;

import org.olat.modules.lecture.model.LocationHistory;

/**
 * 
 * Initial date: 13 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LocationDateComparator implements Comparator<LocationHistory> {

	@Override
	public int compare(LocationHistory o1, LocationHistory o2) {
		if(o1 == null) {
			if(o2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if(o2 == null) {
			return 1;
		}
		
		Date d1 = o1.getLastUsed();
		Date d2 = o2.getLastUsed();
		if(d1 == null) {
			if(d2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if(d2 == null) {
			return 1;
		}
		
		return -d1.compareTo(d2);
	}
}