/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rating;

import java.util.Comparator;

import org.olat.core.commons.services.commentAndRating.model.UserRating;

/**
 * 
 * Description:<br>
 * Order the UserRating by rating
 * <P>
 * Initial Date:  20 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RatingComparator implements Comparator<UserRating> {

	@Override
	public int compare(UserRating o1, UserRating o2) {
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return 1;
		if(o2 == null) return -1;
		
		Integer r1 = o1.getRating();
		Integer r2 = o2.getRating();
		if(r1 == null && r2 == null) return 0;
		if(r1 == null) return 1;
		if(r2 == null) return -1;
		
		return -r1.compareTo(r2);
	}
}
