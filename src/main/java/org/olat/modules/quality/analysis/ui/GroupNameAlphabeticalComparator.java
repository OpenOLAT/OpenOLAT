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
package org.olat.modules.quality.analysis.ui;

import java.util.Comparator;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 14.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GroupNameAlphabeticalComparator implements Comparator<HeatMapRow> {

	@Override
	public int compare(HeatMapRow row1, HeatMapRow row2) {
		List<String> groupNames1 = row1.getGroupNames();
		if (groupNames1 == null || groupNames1.isEmpty()) return -1;
		List<String> groupNames2 = row2.getGroupNames();
		if (groupNames2 == null || groupNames2.isEmpty()) return 1;
		
		for (int i = 0; i < groupNames1.size(); i++) {
			String groupName1 = groupNames1.get(i);
			String groupName2 = groupNames2.get(i);
			if (!StringHelper.containsNonWhitespace(groupName1)) {
				return -1;
			}
			if (!StringHelper.containsNonWhitespace(groupName2)) {
				return 1;
			}
			int compareTo = groupName1.compareTo(groupName2);
			if (compareTo != 0) {
				return compareTo;
			}
		}
		return 0;
	}

}
