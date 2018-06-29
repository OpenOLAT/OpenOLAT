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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.modules.curriculum.ui.CurriculumElementWithViewsRow;

/**
 * Compare and reorder a tree like structure by compare the
 * rows and their parents.
 * 
 * Initial date: 26 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementViewsRowComparator implements Comparator<CurriculumElementWithViewsRow > {

	private final Collator collator;
	
	public CurriculumElementViewsRowComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}

	@Override
	public int compare(CurriculumElementWithViewsRow element1, CurriculumElementWithViewsRow element2) {
		int c = 0;
		
		List<CurriculumElementWithViewsRow> parents1 = getParentLine(element1);
		List<CurriculumElementWithViewsRow> parents2 = getParentLine(element2);
		if(parents1.size() == parents2.size()) {
			c = compareParents(element1, element2);
		} else if(parents1.size() > parents2.size()) {
			int sharedLevel = parents2.size() - 1;
			c = compareParents(parents1.get(sharedLevel), parents2.get(sharedLevel));
		} else if(parents1.size() < parents2.size()) {
			int sharedLevel = parents1.size() - 1;
			c = compareParents(parents1.get(sharedLevel), parents2.get(sharedLevel));
		}
		
		if(c == 0) {
			c = element1.getKey().compareTo(element2.getKey());
		}
		return c;
	}
	
	private List<CurriculumElementWithViewsRow> getParentLine(CurriculumElementWithViewsRow row) {
		List<CurriculumElementWithViewsRow> parentLine = new ArrayList<>();
		for(CurriculumElementWithViewsRow view=row; view != null; view=view.getParent()) {
			parentLine.add(view);
		}
		if(parentLine.size() > 1) {
			Collections.reverse(parentLine);
		}
		return parentLine;
	}

	private int compareParents(CurriculumElementWithViewsRow element1, CurriculumElementWithViewsRow element2) {
		CurriculumElementWithViewsRow parent1 = element1.getParent();
		CurriculumElementWithViewsRow parent2 = element2.getParent();
		
		int c = 0;
		// root
		if(parent1 == null && parent2 == null) {
			c = compareRows(element1, element2);
		} else if(parent1.equals(parent2)) {
			c = compareRows(element1, element2);
		} else {
			c = compareParents(parent1, parent2);
		}
		return c;
	}
	
	private int compareRows(CurriculumElementWithViewsRow element1, CurriculumElementWithViewsRow element2) {
		Date start1 = getStartDate(element1);
		Date start2 = getStartDate(element2);
		int c = compareDates(start1, start2);
		if(c == 0) {
			String display1 = getDisplayName(element1);
			String display2 = getDisplayName(element2);
			c = compareStrings(display1, display2); 
		}
		return c;
	}
	
	private int compareStrings(String d1, String d2) {
		int c = 0;
		if(d1 == null || d2 == null) {
			c = compareNulls(d1, d2);
		} else {
			c = collator.compare(d1, d2);
		}
		return c;
	}
	
	private int compareDates(Date d1, Date d2) {
		int c = 0;
		if(d1 == null || d2 == null) {
			c = compareNulls(d1, d2);
		} else {
			c = d1.compareTo(d2);
		}
		return c;
	}
	
	private int compareNulls(Object o1, Object o2) {
		if(o1 == null && o2 == null) {
			return 0;
		} 
		if(o1 == null) {
			return -1;
		}
		if( o2 == null) {
			return 1;
		}
		return 0;
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
	
	private Date getStartDate(CurriculumElementWithViewsRow row) {
		if(row.isCurriculumElementOnly()) {
			return row.getCurriculumElementBeginDate();
		}
		if(row.isRepositoryEntryOnly()) {
			return row.getLifecycleStart();
		}
		Date d = row.getLifecycleStart();
		if(d == null) {
			d = row.getCurriculumElementBeginDate();
		} else if(row.getCurriculumElementBeginDate() != null && d.after(row.getCurriculumElementBeginDate())) {
			d = row.getCurriculumElementBeginDate();
		}
		return d;
	}
}