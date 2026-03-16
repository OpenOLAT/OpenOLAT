/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rating;

import static org.olat.modules.selectus.RecruitingService.ABSTENTION;

import java.util.Comparator;
import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.UserRating;

import org.olat.modules.selectus.ui.UserRatingMapper;

/**
 * 
 * Description:<br>
 * Compare the rating contained in UserRatingMapper
 * <P>
 * Initial Date:  3 sept. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserMapperCommitteeRatingComparator implements Comparator<UserRatingMapper> {

	private static final int EMPTY_FACTOR = 100 * 100 * 100;
	private static final int A_FACTOR = 100 * 100;
	private static final int B_FACTOR = 100;
	private static final int C_FACTOR = -1;
	private static final int ABSTENTION_FACTOR = 0;
	
	
	@Override
	public int compare(UserRatingMapper o1, UserRatingMapper o2) {
		if(o1 == null || o2 == null) return -1;

		List<UserRating> ur1 = o1.getRatings();
		List<UserRating> ur2 = o2.getRatings();
		
		int a1 = getResult(ur1);
		int a2 = getResult(ur2);
		return a1 - a2;
	}
	
	private int getResult(List<UserRating> ratings) {
		if(ratings == null || ratings.isEmpty()) return EMPTY_FACTOR;
		
		int result = 0;
		boolean empty = true;
		for(UserRating rating:ratings) {
			Integer r = rating.getRating();
			if(r != null) {
				switch(r.intValue()) {
					case 1: result += C_FACTOR; empty &= false; break;
					case 2: result += B_FACTOR; empty &= false; break;
					case 3: result += A_FACTOR; empty &= false; break;
					case ABSTENTION: result += ABSTENTION_FACTOR; empty &= false; break;
				}
			}
		}
		
		if(empty) {
			return EMPTY_FACTOR;
		}
		return result;
	}
}
