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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.ui.tool.IdentityAssessmentOverviewTableModel.NodeCols;

/**
 * 
 * Initial date: 22 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityAssessmentOverviewSorter extends SortableFlexiTableModelDelegate<AssessmentNodeData> {
	
	public IdentityAssessmentOverviewSorter(SortKey orderBy, SortableFlexiTableDataModel<AssessmentNodeData> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<AssessmentNodeData> rows) {
		int columnIndex = getColumnIndex();
		NodeCols column = NodeCols.values()[columnIndex];
		switch(column) {
			case minMax: Collections.sort(rows, new MinMaxComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private class MinMaxComparator implements Comparator<AssessmentNodeData> {
		@Override
		public int compare(AssessmentNodeData o1, AssessmentNodeData o2) {
			Float minScore1 = o1.getMinScore();
			Float minScore2 = o2.getMaxScore();
			if (minScore1 == null || minScore2 == null) {
				return compareNullObjects(minScore1, minScore2);
			}
			return Float.compare(minScore1.floatValue(), minScore1.floatValue());
		}
	}

}
