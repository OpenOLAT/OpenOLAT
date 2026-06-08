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
package org.olat.modules.selectus.model.comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 
 * Initial date: 15 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReviewCommentComparator implements Comparator<ApplicationReviewComment> {

	@Override
	public int compare(ApplicationReviewComment c1, ApplicationReviewComment c2) {
		if(c1 == null && c2 == null) {
			return 0;
		} else if(c1 == null) {
			return 1;
		} else if(c2 == null) {
			return -1;
		}

		ApplicationReviewComment p1 = c1.getParentComment();
		ApplicationReviewComment p2 = c2.getParentComment();
		if((p1 == null && p2 == null) || (p1 != null && p1.equals(p2))) {
			return compareCommentWithSameParent(c1, c2);
		}
		
		List<ApplicationReviewComment> parentLine1 = getParentLine(c1);
		List<ApplicationReviewComment> parentLine2 = getParentLine(c2);

		ApplicationReviewComment pp1;
		ApplicationReviewComment pp2;
		if(parentLine1.size() <= parentLine2.size()) {
			pp1 = parentLine1.get(parentLine1.size() - 1);
			pp2 = parentLine2.get(parentLine1.size() - 1);
		} else {
			pp1 = parentLine1.get(parentLine2.size() - 1);
			pp2 = parentLine2.get(parentLine2.size() - 1);
		}

		int c = 0;
		if(pp1.equals(pp2)) {
			c = Integer.compare(parentLine1.size(), parentLine2.size());
		} else {
			int depth = Math.min(parentLine1.size(), parentLine2.size()) - 1;
			for(int i=depth; i-->0; ) {
				pp1 = parentLine1.get(i);
				pp2 = parentLine2.get(i);
				if(pp1.equals(pp2)) {
					pp1 = parentLine1.get(i + 1);
					pp2 = parentLine2.get(i + 1);
					break;
				}	
			}
			if(pp1.equals(pp2)) {
				c = Integer.compare(parentLine1.size(), parentLine2.size());
			} else {
				c = compareCommentWithSameParent(pp1, pp2);
			}
		}
		return c;
	}
	
	private List<ApplicationReviewComment> getParentLine(ApplicationReviewComment node) {
		List<ApplicationReviewComment> nodes = new ArrayList<>();
		for(ApplicationReviewComment parent=node; parent != null; parent = parent.getParentComment()) {
			nodes.add(parent);
		}
		Collections.reverse(nodes);
		return nodes;
	}
	
	private int compareCommentWithSameParent(ApplicationReviewComment c1, ApplicationReviewComment c2) {
		Date date1 = c1.getCreationDate();
		Date date2 = c2.getCreationDate();
		int c = date1.compareTo(date2);
		if(c == 0) {
			Long key1 = c1.getCommentKey();
			Long key2 = c2.getCommentKey();
			c = key1.compareTo(key2);
		}
		return c;
	}
}
