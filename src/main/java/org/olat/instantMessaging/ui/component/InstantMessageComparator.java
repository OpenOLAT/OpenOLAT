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
package org.olat.instantMessaging.ui.component;

import java.util.Comparator;
import java.util.Date;

import org.olat.instantMessaging.InstantMessage;

/**
 * 
 * Initial date: 29 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InstantMessageComparator implements Comparator<InstantMessage> {

	@Override
	public int compare(InstantMessage o1, InstantMessage o2) {
		Date d1 = o1.getCreationDate();
		Date d2 = o2.getCreationDate();
		
		int c = 0;
		if(d1 == null || d2 == null) {
			c = compareNullObjects(d1, d2);
		} else {
			c = d2.compareTo(d1);
		}
		
		if(c == 0) {
			Long k1 = o1.getKey();
			Long k2 = o2.getKey();
			if(k1 == null || k2 == null) {
				c = compareNullObjects(k1, k2);
			} else {
				c = k1.compareTo(k2);
			}
		}
		return c;
	}
	
	protected final int compareNullObjects(final Object a, final Object b) {
		boolean ba = (a == null);
		boolean bb = (b == null);
		return ba? (bb? 0: -1):(bb? 1: 0);
	}
	
	

}
