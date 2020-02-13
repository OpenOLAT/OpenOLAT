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
package org.olat.user.propertyhandlers;

import java.util.Comparator;

public class UserInterestsComparator implements Comparator<String> {

	@Override
	public int compare(String id0, String id1) {
		String[] levels0 = id0.split("\\.");
		String[] levels1 = id1.split("\\.");
		Integer level00 = Integer.parseInt(levels0[0]);
		Integer level01 = Integer.parseInt(levels0[1]);
		Integer level10 = Integer.parseInt(levels1[0]);
		Integer level11 = Integer.parseInt(levels1[1]);
		
		if (level00.equals(level10)) {
			return level01.compareTo(level11);
		} else {
			return level00.compareTo(level10);
		}
	}
}
