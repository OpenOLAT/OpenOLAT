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
package org.olat.ims.qti21.resultexport;

import java.util.Comparator;

/**
 * 
 * Initial date: 28 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedMemberComparator implements Comparator<AssessedMember> {

	@Override
	public int compare(AssessedMember o1, AssessedMember o2) {
		int c = 0;
		
		String l1 = o1.getLastname();
		String l2 = o2.getLastname();
		if(l1 == null || l2 == null) {
			c = compareNullObjects(l1, l2);
		} else {
			c = l1.compareTo(l2);
		}
		
		if(c == 0) {
			String n1 = o1.getNickname();
			String n2 = o2.getNickname();
			if(n1 == null || n2 == null) {
				c = compareNullObjects(n1, n2);
			} else {
				c = n1.compareTo(n2);
			}
		}
		return c;
	}
	
	private final int compareNullObjects(final Object a, final Object b) {
		boolean ba = (a == null);
		boolean bb = (b == null);
		return ba? (bb? 0: -1):(bb? 1: 0);
	}
}
