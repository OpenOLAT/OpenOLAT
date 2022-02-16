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
package org.olat.core.commons.services.export.ui;

import java.util.Comparator;
import java.util.Date;

/**
 * 
 * Initial date: 10 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportRowComparator implements Comparator<ExportRow> {

	@Override
	public int compare(ExportRow o1, ExportRow o2) {
		if(o1 == null && o2 == null) {
			return compareNullObjects(o1, o2);
		}
		
		Date d1 = o1.getCreationDate();
		Date d2 = o2.getCreationDate();
		if(d1 == null || d2 == null) {
			return compareNullObjects(d1, d2);
		}
		return -d1.compareTo(d2);
	}
	
	private int compareNullObjects(Object o1, Object o2) {
		if(o1 == null && o2 == null) {
			return 0;
		}
		if(o1 == null && o2 != null) {
			return -1;
		}
		if(o1 != null && o2 == null) {
			return 1;
		}
		return 0;
	}
	
	

}
