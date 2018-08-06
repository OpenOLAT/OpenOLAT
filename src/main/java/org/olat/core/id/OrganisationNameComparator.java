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
package org.olat.core.id;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * 
 * Initial date: 6 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationNameComparator implements Comparator<Organisation> {

	private final Collator collator;
	
	public OrganisationNameComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}
	
	@Override
	public int compare(Organisation o1, Organisation o2) {
		if(o1 == null || o2 == null) {
			return compareNull(o1, o2);
		}
		
		String n1 = o1.getDisplayName();
		String n2 = o2.getDisplayName();
		if(n1 == null || n2 == null) {
			return compareNull(n1, n2);
		}
		int c = collator.compare(n1, n2);
		if(c == 0) {
			String i1 = o1.getIdentifier();
			String i2 = o2.getIdentifier();
			c = compareNull(i1, i2);
		}
		
		if(c == 0) {
			Long k1 = o1.getKey();
			Long k2 = o2.getKey();
			if(k1 != null && k2 != null) {
				c = Long.compare(k1.longValue(), k2.longValue());
			} else {
				c = compareNull(k1, k2);
			}
		}
		return c;
	}
	
	private final int compareNull(Object o1, Object o2) {
		if(o1 == null && o2 == null) {
			return 0;
		} else if(o1 == null) {
			return -1;
		} else if(o2 == null) {
			return 1;
		}
		return 0;
	}
	
	

}
