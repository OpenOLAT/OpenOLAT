/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.rating;

import java.util.Comparator;
import java.util.Date;

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
public class UserMapperRatingComparator implements Comparator<UserRatingMapper> {

	@Override
	public int compare(UserRatingMapper o1, UserRatingMapper o2) {
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return 1;
		if(o2 == null) return -1;

		UserRating ur1 = o1.getRating();
		UserRating ur2 = o2.getRating();
		if(ur1 == null && ur2 == null) {
			return compareByCreationDate(o1, o2);
		}
		if(ur1 == null) return 1;
		if(ur2 == null) return -1;
		
		Integer r1 = ur1.getRating();
		Integer r2 = ur2.getRating();
		if(r1 == null && r2 == null) {
			return compareByCreationDate(o1, o2);
		}
		if(r1 == null) return 1;
		if(r2 == null) return -1;
		
		int c = r1.compareTo(r2);
		if(c == 0) {
			return compareByCreationDate(o1, o2);
		}
		return c;
	}
	
	private int compareByCreationDate(UserRatingMapper o1, UserRatingMapper o2) {
		Date c1 = o1.getCreationDate();
		Date c2 = o2.getCreationDate();
		if(c1 == null) return 1;
		if(c2 == null) return -1;
		return c1.compareTo(c2);
	}
}
