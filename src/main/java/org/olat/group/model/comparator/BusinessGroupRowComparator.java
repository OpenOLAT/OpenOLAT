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
import java.util.Locale;

import org.olat.group.model.BusinessGroupRow;

/**
 * 
 * Initial date: 25.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupRowComparator implements Comparator<BusinessGroupRow> {
	
	private final Collator collator;
	
	public BusinessGroupRowComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}

	@Override
	public int compare(BusinessGroupRow o1, BusinessGroupRow o2) {
		if(o1 == null) {
			if(o2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if(o2 == null) {
			return 1;
		}
		
		String n1 = o1.getName();
		String n2 = o2.getName();
		if(n1 == null) {
			if(n2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if(n2 == null) {
			return 1;
		}
		
		return collator.compare(n1, n2);
	}
}
