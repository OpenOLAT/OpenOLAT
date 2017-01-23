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
package org.olat.course.assessment.ui.tool;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.model.AssessedBusinessGroup;
import org.olat.course.assessment.ui.tool.AssessedBusinessGroupTableModel.ABGCols;

/**
 * 
 * Initial date: 19 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedBusinessGroupSorter extends SortableFlexiTableModelDelegate<AssessedBusinessGroup> {
	
	public AssessedBusinessGroupSorter(SortKey orderBy, AssessedBusinessGroupTableModel model, Locale locale) {
		super(orderBy, model, locale);
	}
	
	@Override
	protected void sort(List<AssessedBusinessGroup> rows) {
		int columnIndex = getColumnIndex();
		ABGCols column = ABGCols.values()[columnIndex];
		switch(column) {
			case countPassed: Collections.sort(rows, new CountPassedComparator()); break;
			default: {
				super.sort(rows);
			}
		}
	}
	
	private class CountPassedComparator implements Comparator<AssessedBusinessGroup> {
		@Override
		public int compare(AssessedBusinessGroup o1, AssessedBusinessGroup o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			int p1 = o1.getNumOfParticipants();
			int p2 = o2.getNumOfParticipants();
			return Integer.compare(p1, p2);
		}
	}
}
