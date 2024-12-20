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
package org.olat.modules.lecture.ui.component;

import java.util.Comparator;
import java.util.Date;

import org.olat.modules.lecture.model.LocationHistory;

/**
 * 
 * Initial date: 13 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LocationDateComparator implements Comparator<LocationHistory> {

	@Override
	public int compare(LocationHistory o1, LocationHistory o2) {
		if(o1 == null) {
			if(o2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if(o2 == null) {
			return 1;
		}
		
		Date d1 = o1.getLastUsed();
		Date d2 = o2.getLastUsed();
		if(d1 == null) {
			if(d2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if(d2 == null) {
			return 1;
		}
		
		return -d1.compareTo(d2);
	}
}