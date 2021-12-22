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
package org.olat.modules.curriculum.manager;

import java.util.Comparator;
import java.util.List;

import org.olat.modules.curriculum.CurriculumElement;

/**
 * 
 * Initial date: 22 déc. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class ReorderCurriculumElementComparator implements Comparator<CurriculumElement> {

	private final List<Long> orderedList;
	
	public ReorderCurriculumElementComparator(List<Long> orderedList) {
		this.orderedList = orderedList;
	}

	@Override
	public int compare(CurriculumElement c1, CurriculumElement c2) {
		int index1 = orderedList.indexOf(c1.getKey());
		int index2 = orderedList.indexOf(c2.getKey());
		
		int c = 0;
		if(index1 < 0 && index2 < 0) {
			c = comparePos(c1, c2);
		} else if(index1 < 0) {
			c = 1;
		} else if(index2 < 0) {
			c = -1;
		} else {
			c = Integer.compare(index1, index2);
			if(c == 0) {
				comparePos(c1, c2);
			}
		}
		return c;
	}
	
	private int comparePos(CurriculumElement c1, CurriculumElement c2) {
		Long pos1 = c1.getPos();
		Long pos2 = c2.getPos();
		
		int c = 0;
		if(pos1 != null && pos2 != null) {
			c = Long.compare(pos1.longValue(), pos2.longValue());
		}
		return c;
	}
}
