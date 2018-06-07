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
package org.olat.group.ui.main;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

import org.olat.group.BusinessGroupShort;

/**
 * Compare the groups of member views based on the group name.<br/>
 * 
 * Initial date: 04.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupMemberViewComparator implements Comparator<MemberRow> {
	
	private Collator collator;
	
	public GroupMemberViewComparator(Collator collator) {
		this.collator = collator;
	}

	@Override
	public int compare(MemberRow m1, MemberRow m2) {
		List<BusinessGroupShort> g1 = m1.getGroups();
		List<BusinessGroupShort> g2 = m2.getGroups();
		
		if(g1 == null || g1.isEmpty()) {
			if(g2 == null || g2.isEmpty()) return 0;
			return -1;
		}
		if(g2 == null || g2.isEmpty()) return 1;
		
		int maxLevel = Math.max(g1.size(), g2.size());
		
		int compare = 0;
		for(int i=0; i<maxLevel && compare==0; i++) {
			BusinessGroupShort gs1 = i < g1.size() ? g1.get(i) : null;
			BusinessGroupShort gs2 = i < g2.size() ? g2.get(i) : null;
			compare = compareLevel(gs1, gs2);
		}
		return compare;
	}
	
	private int compareLevel(BusinessGroupShort g1, BusinessGroupShort g2) {
		if(g1 == null) {
			if(g2 == null) return 0;
			return -1;
		}
		if(g2 == null) return 1;
		
		String n1 = g1.getName();
		String n2 = g2.getName();
		if(n1 == null) {
			if(n2 == null) return 0;
			return -1;
		}
		if(n2 == null) return 1;
		return collator == null ? n1.compareTo(n2) : collator.compare(n1, n2);	
	}
}
