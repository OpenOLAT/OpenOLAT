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

package org.olat.commons.info.portlet;

import java.util.Comparator;
import java.util.Date;

import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.gui.control.generic.portal.SortingCriteria;

/**
 * 
 * Description:<br>
 * Comparator for InfoPortletEntry
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoPortletEntryComparator implements Comparator<InfoSubscriptionItem> {
	
	private final SortingCriteria criteria;
	
	public InfoPortletEntryComparator(SortingCriteria criteria) {
		this.criteria = criteria;
	}

	@Override
	public int compare(InfoSubscriptionItem isi1, InfoSubscriptionItem isi2) {
		if(isi1 == null) return -1;
		else if(isi2 == null) return 1;

		SubscriptionListItem m1 = isi1.getItem();
		SubscriptionListItem m2 = isi2.getItem();
		if(m1 == null) return -1;
		else if(m2 == null) return 1;
		
		// only sorting per date
		Date d1 = m1.getDate();
		Date d2 = m2.getDate();
		if(d1 == null) return -1;
		else if(d2 == null) return 1;
		
		int result = d1.compareTo(d2);
		return criteria.isAscending() ? result : -result;
	}
}