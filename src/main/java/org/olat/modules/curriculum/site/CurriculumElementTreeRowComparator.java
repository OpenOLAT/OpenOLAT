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
package org.olat.modules.curriculum.site;

import java.text.Collator;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.modules.curriculum.ui.CurriculumElementRow;

/**
 * This is based of the position of the elements.
 * 
 * Initial date: 9 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTreeRowComparator extends FlexiTreeNodeComparator {

	private final Collator collator;
	
	public CurriculumElementTreeRowComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}
	
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
			Integer pos1 = c1.getPosCurriculum();
			Integer pos2 = c2.getPosCurriculum();
			if(pos1 == null || pos2 == null) {
				c = compareNullObjects(pos1, pos2);
			} else {
				c = Integer.compare(pos1.intValue(), pos2.intValue());
			}
		} else if(parentKey1 != null && parentKey1.equals(parentKey2)) {
			Integer pos1 = c1.getPos();
			Integer pos2 = c2.getPos();
			if(pos1 == null || pos2 == null) {
				c = compareNullObjects(pos1, pos2);
			} else {
				c = Integer.compare(pos1.intValue(), pos2.intValue());
			}
		} else if(parentKey1 != null && parentKey2 != null && c1.getParent() != null && c2.getParent() != null) {
			// This case is usually not possible
			CurriculumElementRow p1 = c1.getParent();
			CurriculumElementRow p2 = c2.getParent();
			c = compareCurriculumElements(p1, p2);
			if(c == 0) {
				c = Long.compare(p1.getKey().longValue(), p2.getKey().longValue());
			}
		} else {
			// This case is usually not possible
			c = compareCurriculumElements(c1, c2);
		}
		
		if(c == 0) {
			c = Long.compare(c1.getKey().longValue(), c2.getKey().longValue());
		}
		return c;
	}
	
	private int compareCurriculumElements(CurriculumElementRow c1, CurriculumElementRow c2) {
		int c = 0;
		if(c1.getBeginDate() == null || c2.getBeginDate() == null) {
			c = compareNullObjects(c1.getBeginDate(), c2.getBeginDate());
		} else {
			c = c1.getBeginDate().compareTo(c2.getBeginDate());
		}
		
		if(c == 0) {
			if(c1.getDisplayName() == null || c2.getDisplayName() == null) {
				c = compareNullObjects(c1.getDisplayName(), c2.getDisplayName());
			} else {
				c = collator.compare(c1.getDisplayName(), c2.getDisplayName());
			}
		}
		
		if(c == 0) {
			if(c1.getIdentifier() == null || c2.getIdentifier() == null) {
				c = compareNullObjects(c1.getIdentifier(), c2.getIdentifier());
			} else {
				c = collator.compare(c1.getIdentifier(), c2.getIdentifier());
			}
		}
		
		if(c == 0) {
			c = Long.compare(c1.getKey().longValue(), c2.getKey().longValue());
		}
		return c;
	}
}
