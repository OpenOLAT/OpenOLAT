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
import java.util.Date;

import org.olat.modules.selectus.model.ApplicationLight;

public class PositionApplicationsPDFComparator implements Comparator<ApplicationLight> {

	private final Date deadline;
	
	public PositionApplicationsPDFComparator(Date deadline) {
		this.deadline = deadline;
	}
	
	@Override
	public int compare(ApplicationLight l1, ApplicationLight l2) {
		if(l1 == null && l2 == null) {
			return 0;
		}
		if(l2 == null) return 1;
		if(l1 == null) return -1;

		//after deadline application at the end
		if(deadline != null) {
			Date d1 = l1.getCreationDate();
			Date d2 = l2.getCreationDate();
			if(d1 == null) return -1;//normally not possible
			if(d2 == null) return 1;//normally not possible
			
			boolean a1 = d1.after(deadline);
			boolean a2 = d2.after(deadline);
			
			if(a1 && !a2) {
				return 1;
			}
			if(!a1 && a2) {
				return -1;
			}
		}
		
		Integer i1 = l1.getId();
		Integer i2 = l2.getId();
		if(i1 == null) return -1;
		if(i2 == null) return 1;
		
		int compare = i1.compareTo(i2);
		if(compare == 0) {
			String f1 = l1.getPerson().getLastName();
			String f2 = l2.getPerson().getLastName();
			if(f1 == null) return -1;
			if(f2 == null) return 1;
			compare = f1.compareToIgnoreCase(f2);
		}
		return compare;
	}
}