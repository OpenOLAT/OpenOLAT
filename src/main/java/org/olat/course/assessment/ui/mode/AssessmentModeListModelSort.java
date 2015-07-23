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
package org.olat.course.assessment.ui.mode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.ui.mode.AssessmentModeListModel.Cols;

/**
 * 
 * Initial date: 30.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeListModelSort extends SortableFlexiTableModelDelegate<AssessmentMode> {
	
	public AssessmentModeListModelSort(SortKey orderBy, SortableFlexiTableDataModel<AssessmentMode> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<AssessmentMode> rows) {
		int columnIndex = getColumnIndex();
		Cols column = Cols.values()[columnIndex];
		switch(column) {
			case status: Collections.sort(rows, new StatusComparator()); break;
			default: {
				super.sort(rows);
			}
		}
	}
	
	private static class StatusComparator implements Comparator<AssessmentMode> {
		@Override
		public int compare(AssessmentMode o1, AssessmentMode o2) {
			Status s1 = o1.getStatus();
			Status s2 = o2.getStatus();
			
			if(s1 == null) {
				if(s2 == null) return 0;
				return -1;
			}
			if(s2 == null) return 1;
			
			return s1.ordinal() - s2.ordinal();
		}
	}
}
