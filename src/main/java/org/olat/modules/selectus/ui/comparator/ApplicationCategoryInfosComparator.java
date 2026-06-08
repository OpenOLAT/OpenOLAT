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
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;

import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;

/**
 * 
 * Initial date: 3 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCategoryInfosComparator implements Comparator<ApplicationCategoryInfos> {
	
	@Override
	public int compare(ApplicationCategoryInfos o1, ApplicationCategoryInfos o2) {
		if(o1 == null || o2 == null) {
			return compareNulls(o1, o2);
		}
		if(o1.getCategory() == null || o2.getCategory() == null) {
			return compareNulls(o1, o2);
		}
		
		String n1 = o1.getCategory().getName();
		String n2 = o2.getCategory().getName();
		
		int c = 0;
		if(n1 == null || n2 == null) {
			c = compareNulls(o1, o2);
		} else {
			c = n1.compareToIgnoreCase(n2);
		}
		
		if(c == 0) {
			// reversed
			c = Boolean.compare(o2.isAdministrative(), o1.isAdministrative());
		}
		
		if(c == 0) {
			Long k1 = o1.getCategory().getKey();
			Long k2 = o2.getCategory().getKey();
			c = k1.compareTo(k2);
		}
		return c;
	}
	
	private int compareNulls(Object o1, Object o2) {
		if(o1 == null && o2 == null) {
			return 0;
		}
		return o1 == null ? -1 : 1;
	}
}
