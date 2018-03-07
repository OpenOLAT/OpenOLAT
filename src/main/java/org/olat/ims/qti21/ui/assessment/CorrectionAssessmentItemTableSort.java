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
package org.olat.ims.qti21.ui.assessment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.ui.assessment.CorrectionAssessmentItemTableModel.ItemCols;
import org.olat.ims.qti21.ui.assessment.model.CorrectionAssessmentItemRow;

/**
 * 
 * Initial date: 5 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionAssessmentItemTableSort extends SortableFlexiTableModelDelegate<CorrectionAssessmentItemRow> {
	
	public CorrectionAssessmentItemTableSort(SortKey orderBy, CorrectionAssessmentItemTableModel model, Locale locale) {
		super(orderBy, model, locale);
	}
	
	@Override
	protected void sort(List<CorrectionAssessmentItemRow> rows) {
		int columnIndex = getColumnIndex();
		ItemCols column = ItemCols.values()[columnIndex];
		switch(column) {
			case itemType:Collections.sort(rows, new QuestionTypeComparator()); break;
			case corrected: Collections.sort(rows, new CorrectedComparator()); break;
			case notCorrected: Collections.sort(rows, new NotCorrectedComparator()); break;
			default: super.sort(rows);
		}
	}

	private class QuestionTypeComparator implements Comparator<CorrectionAssessmentItemRow> {

		@Override
		public int compare(CorrectionAssessmentItemRow o1, CorrectionAssessmentItemRow o2) {
			QTI21QuestionType t1 = o1.getItemType();
			QTI21QuestionType t2 = o2.getItemType();
			
			int c = 0;
			if(t1 == null || t2 == null) {
				c = compareNullObjects(t1, t2);
			} else {
				c = t1.compareTo(t2);
			}
			
			if(c == 0) {
				c = compareString(o1.getItemTitle(), o2.getItemTitle());
			}
			if(c == 0) {
				c = compareString(o1.getSectionTitle(), o2.getSectionTitle());
			}
			return c;
		}
	}
	
	private class CorrectedComparator implements Comparator<CorrectionAssessmentItemRow> {
		@Override
		public int compare(CorrectionAssessmentItemRow o1, CorrectionAssessmentItemRow o2) {
			int c = Integer.compare(o1.getNumCorrected(), o2.getNumCorrected());
			if(c == 0) {
				c = compareString(o1.getItemTitle(), o2.getItemTitle());
			}
			if(c == 0) {
				c = compareString(o1.getSectionTitle(), o2.getSectionTitle());
			}
			return c;
		}
	}
	
	private class NotCorrectedComparator implements Comparator<CorrectionAssessmentItemRow> {
		@Override
		public int compare(CorrectionAssessmentItemRow o1, CorrectionAssessmentItemRow o2) {
			int c = Integer.compare(o1.getNumNotCorrected(), o2.getNumNotCorrected());
			if(c == 0) {
				c = compareString(o1.getItemTitle(), o2.getItemTitle());
			}
			if(c == 0) {
				c = compareString(o1.getSectionTitle(), o2.getSectionTitle());
			}
			return c;
		}
	}
}
