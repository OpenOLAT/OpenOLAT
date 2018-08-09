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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;

/**
 * 
 * Initial date: 9 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTreeRowComparator extends FlexiTreeNodeComparator {
	
	@Override
	protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
		if(o1 == null || o2 == null) {
			return compareNullObjects(o1, o2);
		}
		
		CurriculumElementRow c1 = (CurriculumElementRow)o1;
		CurriculumElementRow c2 = (CurriculumElementRow)o2;
		Long parentKey1 = c1.getParentKey();
		Long parentKey2 = c2.getParentKey();
		
		int c = 0;
		if(parentKey1 == null && parentKey2 == null) {
			c = compareDisplayName(c1, c2);
		} else if(parentKey1 != null && parentKey1.equals(parentKey2)) {
			Long pos1 = c1.getPos();
			Long pos2 = c2.getPos();
			if(pos1 == null || pos2 == null) {
				c = compareNullObjects(pos1, pos2);
			} else {
				c = Long.compare(pos1.longValue(), pos2.longValue());
			}
		} else if(parentKey1 != null && parentKey2 != null) {
			// This case is usually not possible
			CurriculumElementRow p1 = c1.getParent();
			CurriculumElementRow p2 = c2.getParent();
			c = compareDisplayName(p1, p2);
			if(c == 0) {
				c = Long.compare(p1.getKey().longValue(), p2.getKey().longValue());
			}
		} else {
			// This case is usually not possible
			c = compareDisplayName(c1, c2);
		}
		
		if(c == 0) {
			c = Long.compare(c1.getKey().longValue(), c2.getKey().longValue());
		}
		return c;
	}
	
	private int compareDisplayName(CurriculumElementRow c1, CurriculumElementRow c2) {
		String d1 = c1.getDisplayName();
		String d2 = c2.getDisplayName();
		if(d1 == null || d2 == null) {
			return compareNullObjects(d1, d2);
		}
		return d1.compareTo(d2);
	}
}
