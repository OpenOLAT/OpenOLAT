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
package org.olat.group.model.comparator;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.UserConstants;
import org.olat.group.model.MemberView;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 2 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberViewNamesComparator implements Comparator<MemberView> {
	
	private final Collator collator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public MemberViewNamesComparator(List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		this.userPropertyHandlers = userPropertyHandlers;
		collator = Collator.getInstance(locale);
	}

	@Override
	public int compare(MemberView o1, MemberView o2) {
		if(o1 == null || o2 == null) {
			return compareNulls(o1, o2);
		}
		
		String lastName1 = o1.getIdentityProp(UserConstants.LASTNAME, userPropertyHandlers);
		String lastName2 = o2.getIdentityProp(UserConstants.LASTNAME, userPropertyHandlers);
		int c = compareString(lastName1, lastName2);
		if(c == 0) {
			String firstName1 = o1.getIdentityProp(UserConstants.FIRSTNAME, userPropertyHandlers);
			String firstName2 = o2.getIdentityProp(UserConstants.FIRSTNAME, userPropertyHandlers);
			c = compareString(firstName1, firstName2);
		}
		
		if(c == 0) {
			c = o1.getIdentityKey().compareTo(o2.getIdentityKey());
		}
		return c;
	}
	
	private int compareString(String s1, String s2) {
		if(s1 == null || s2 == null) {
			return compareNulls(s1, s2);
		}
		return collator.compare(s1, s2);
	}
	
	private int compareNulls(Object o1, Object o2) {
		if(o1 == null && o2 == null) {
			return 0;
		}
		if(o1 == null) {
			return -1;
		}
		if(o2 == null) {
			return 1;
		}
		return 0;
	}
	
}
