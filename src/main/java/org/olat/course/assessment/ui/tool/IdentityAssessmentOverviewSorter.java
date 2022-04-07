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
import org.olat.modules.assessment.model.AssessmentEntryStatus;

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
			case score: Collections.sort(rows, new ScoreComparator()); break;
			case passed: Collections.sort(rows, new PassedComparator()); break;
			case minMax: Collections.sort(rows, new MinMaxComparator()); break;
			case status: Collections.sort(rows, new StatusComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private class ScoreComparator implements Comparator<AssessmentNodeData> {
		@Override
		public int compare(AssessmentNodeData o1, AssessmentNodeData o2) {
			Float s1 = o1.getScore();
			Float s2 = o2.getScore();
			if (s1 == null || s2 == null) {
				return compareNullObjects(s1, s2);
			}
			return Float.compare(s1.floatValue(), s1.floatValue());
		}
	}
	
	private class PassedComparator implements Comparator<AssessmentNodeData> {
		@Override
		public int compare(AssessmentNodeData o1, AssessmentNodeData o2) {
			Boolean passed1 = o1.getPassed();
			Boolean passed2 = o2.getPassed();
			if (passed1 == null || passed2 == null) {
				return compareNullObjects(passed1, passed2);
			}
			return passed1.compareTo(passed2);
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
	
	private class StatusComparator implements Comparator<AssessmentNodeData> {
		@Override
		public int compare(AssessmentNodeData o1, AssessmentNodeData o2) {
			AssessmentEntryStatus status1 = o1.getAssessmentStatus();
			AssessmentEntryStatus status2 = o2.getAssessmentStatus();
			if (status1 == null || status2 == null) {
				return compareNullObjects(status1, status2);
			}
			return status1.compareTo(status2);
		}
	}

}
