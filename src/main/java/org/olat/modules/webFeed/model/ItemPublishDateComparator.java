/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.webFeed.model;

import java.util.Comparator;
import java.util.Date;

import org.olat.modules.webFeed.Item;

/**
 * Comparator to separate the items in drafts and published items. Inside the
 * two groups the items are sorted according to the reverse chronological order
 * of the publish date.
 * <P>
 * Initial Date: Aug 4, 2009 <br>
 * 
 * @author gwassmann
 */
public class ItemPublishDateComparator implements Comparator<Item> {

	@Override
	public int compare(Item a, Item b) {
		if (a.isDraft() == b.isDraft()) {
			Date d1 = a.getPublishDate();
			Date d2 = b.getPublishDate();
			if(d1 == null) return 1;
			if(d2 == null) return -1;
			return d2.compareTo(d1);
		}
		if (b.isDraft()) {
			return 1;
		}
		return -1;
	}
}
