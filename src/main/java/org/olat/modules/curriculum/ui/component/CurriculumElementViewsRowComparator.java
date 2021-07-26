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
package org.olat.modules.curriculum.ui.component;

import java.text.Collator;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.modules.curriculum.ui.CurriculumElementWithViewsRow;

/**
 * Compare and reorder a tree like structure by compare the
 * rows and their parents.
 * 
 * Initial date: 26 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementViewsRowComparator extends FlexiTreeNodeComparator {

	private final Collator collator;
	
	public CurriculumElementViewsRowComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}

	@Override
	protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
		if(o1 == null || o2 == null) {
			return compareNullObjects(o1, o2);
		}
		
		CurriculumElementWithViewsRow c1 = (CurriculumElementWithViewsRow)o1;
		CurriculumElementWithViewsRow c2 = (CurriculumElementWithViewsRow)o2;
		Long parentKey1 = c1.getParentKey();
		Long parentKey2 = c2.getParentKey();
		
		int c = 0;
		if(parentKey1 == null && parentKey2 == null) {
			c = compareCurriculumElements(c1, c2);
		} else if(parentKey1 != null && parentKey1.equals(parentKey2)) {
			c = compareSameParent(c1, c2);
		} else if(parentKey1 != null && parentKey2 != null) {
			// This case is usually not possible
			CurriculumElementWithViewsRow p1 = c1.getParent();
			CurriculumElementWithViewsRow p2 = c2.getParent();
			if(p1 == null || p2 == null) {
				// reversed because no parent at the top, higher in the hierarchy
				c = -compareNullObjects(p1, p2); 
			} else {
				c = compareCurriculumElements(p1, p2);
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
	
	private int compareSameParent(CurriculumElementWithViewsRow c1, CurriculumElementWithViewsRow c2) {
		int c = 0;
		if((c1.isCurriculumElementOnly() || c1.isCurriculumElementWithEntry()) && (c2.isCurriculumElementOnly() || c2.isCurriculumElementWithEntry())) {
			// compare by position
			c = compareCurriculumElements(c1, c2);
		} else if(c1.isCurriculumElementOnly() || c1.isCurriculumElementWithEntry()) {
			c = 1;
		} else if(c2.isCurriculumElementOnly() || c2.isCurriculumElementWithEntry()) {
			c = -1;
		} else {
			c = compareRepositoryEntry(c1, c2);
		}
		return c;
	}
	
	private int compareCurriculumElements(CurriculumElementWithViewsRow c1, CurriculumElementWithViewsRow c2) {
		int c = 0;
		
		if (c1.getCurriculumElementPos() != null && c2.getCurriculumElementPos() != null) {
			c = Long.compare(c1.getCurriculumElementPos().longValue(), c2.getCurriculumElementPos().longValue());
		}
		
		if(c == 0) {
			c = compareClosed(c1, c2);
		}

		if(c == 0) {
			if(c1.getCurriculumElementBeginDate() == null || c2.getCurriculumElementBeginDate() == null) {
				c = compareNullObjects(c1.getCurriculumElementBeginDate(), c2.getCurriculumElementBeginDate());
			} else {
				c = c1.getCurriculumElementBeginDate().compareTo(c2.getCurriculumElementBeginDate());
			}
		}
		
		if(c == 0) {
			if(c1.getCurriculumElementDisplayName() == null || c2.getCurriculumElementDisplayName() == null) {
				c = compareNullObjects(c1.getCurriculumElementDisplayName(), c2.getCurriculumElementDisplayName());
			} else {
				c = collator.compare(c1.getCurriculumElementDisplayName(), c2.getCurriculumElementDisplayName());
			}
		}
		
		if(c == 0) {
			if(c1.getCurriculumElementIdentifier() == null || c2.getCurriculumElementIdentifier() == null) {
				c = compareNullObjects(c1.getCurriculumElementIdentifier(), c2.getCurriculumElementIdentifier());
			} else {
				c = collator.compare(c1.getCurriculumElementIdentifier(), c2.getCurriculumElementIdentifier());
			}
		}
		
		if(c == 0) {
			c = Long.compare(c1.getCurriculumElementKey().longValue(), c2.getCurriculumElementKey().longValue());
		}
		return c;
	}
	
	private int compareRepositoryEntry(CurriculumElementWithViewsRow c1, CurriculumElementWithViewsRow c2) {
		int c = compareClosed(c1, c2);
		
		if(c == 0) {
			if(c1.getRepositoryEntryDisplayName() == null || c2.getRepositoryEntryDisplayName() == null) {
				c = compareNullObjects(c1.getRepositoryEntryDisplayName(), c2.getRepositoryEntryDisplayName());
			} else {
				c = collator.compare(c1.getRepositoryEntryDisplayName(), c2.getRepositoryEntryDisplayName());
			}
		}
		
		if(c == 0) {
			if(c1.getRepositoryEntryExternalRef() == null || c2.getRepositoryEntryExternalRef() == null) {
				c = compareNullObjects(c1.getRepositoryEntryExternalRef(), c2.getRepositoryEntryExternalRef());
			} else {
				c = collator.compare(c1.getRepositoryEntryExternalRef(), c2.getRepositoryEntryExternalRef());
			}
		}
		
		if(c == 0) {
			c = Long.compare(c1.getRepositoryEntryKey().longValue(), c2.getRepositoryEntryKey().longValue());
		}
		return c;
	}
	
	private int compareClosed(CurriculumElementWithViewsRow c1, CurriculumElementWithViewsRow c2) {
		int c = 0;
		if(c1.isClosedOrInactive() && !c2.isClosedOrInactive()) {
			c = 1;
		} else if(!c1.isClosedOrInactive() && c2.isClosedOrInactive()) {
			c = -1;
		}
		return c;
	}
	
	private int compareDisplayName(CurriculumElementWithViewsRow c1, CurriculumElementWithViewsRow c2) {
		String d1 = getDisplayName(c1);
		String d2 = getDisplayName(c2);
		if(d1 == null || d2 == null) {
			return compareNullObjects(d1, d2);
		}
		return d1.compareTo(d2);
	}
	
	private String getDisplayName(CurriculumElementWithViewsRow row) {
		if(row.isCurriculumElementOnly()) {
			return row.getCurriculumElementDisplayName();
		}
		if(row.isRepositoryEntryOnly()) {
			return row.getRepositoryEntryDisplayName();
		}
		String d = row.getRepositoryEntryDisplayName();
		if(d == null) {
			d = row.getCurriculumElementDisplayName();
		}
		return d;
	}
}