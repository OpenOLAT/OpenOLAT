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
package org.olat.core.commons.controllers.navigation;

import java.util.Comparator;
import java.util.Date;

/**
 * Compares Dated objects in reversed chronological order.
 * 
 * <P>
 * Initial Date: Aug 13, 2009 <br>
 * 
 * @author gwassmann
 */
public class DatedComparator implements Comparator<Dated> {

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Dated a, Dated b) {
		if(b == null) return -1;
		if(a == null) return 1;
		Date db = b.getDate();
		Date da = a.getDate();
		if(db == null) return -1;
		if(da == null) return 1;
		
		return db.compareTo(da);
	}
}
