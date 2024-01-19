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
package org.olat.course.assessment.ui.inspection;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionConfigurationListModel.InspectionCols;

/**
 * 
 * Initial date: 17 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionConfigurationListModelSortDelegate
extends SortableFlexiTableModelDelegate<AssessmentInspectionConfigurationRow> {
	
	public AssessmentInspectionConfigurationListModelSortDelegate(SortKey orderBy, AssessmentInspectionConfigurationListModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<AssessmentInspectionConfigurationRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex == InspectionCols.resultsDisplay.ordinal()) {
			Collections.sort(rows, new ResultsDisplayComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private class ResultsDisplayComparator implements Comparator<AssessmentInspectionConfigurationRow> {
		@Override
		public int compare(AssessmentInspectionConfigurationRow o1, AssessmentInspectionConfigurationRow o2) {
			List<String> opt1 = o1.getConfiguration().getOverviewOptionsAsList();
			List<String> opt2 = o2.getConfiguration().getOverviewOptionsAsList();
			int optSize1 = opt1 == null ? 0 : opt1.size();
			int optSize2 = opt2 == null ? 0 : opt2.size();
			return Integer.compare(optSize1, optSize2);
		}
	}
}