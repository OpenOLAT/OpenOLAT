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
import org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemTableModel.IdentityItemCols;
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityAssessmentItemRow;

/**
 * 
 * Initial date: 5 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityAssessmentItemTableSort extends SortableFlexiTableModelDelegate<CorrectionIdentityAssessmentItemRow> {

	public CorrectionIdentityAssessmentItemTableSort(SortKey orderBy, CorrectionIdentityAssessmentItemTableModel model, Locale locale) {
		super(orderBy, model, locale);
	}
	
	@Override
	protected void sort(List<CorrectionIdentityAssessmentItemRow> rows) {
		int columnIndex = getColumnIndex();
		IdentityItemCols column = IdentityItemCols.values()[columnIndex];
		switch(column) {
			case itemType:Collections.sort(rows, new QuestionTypeComparator()); break;
			case corrected: Collections.sort(rows, new CorrectedComparator()); break;
			case notCorrected: Collections.sort(rows, new NotCorrectedComparator()); break;
			default: super.sort(rows);
		}
	}
	
	private int compareItemTitle(CorrectionIdentityAssessmentItemRow o1, CorrectionIdentityAssessmentItemRow o2) {
		int c = compareString(o1.getItemTitle(), o2.getItemTitle());
		if(c == 0) {
			c = compareString(o1.getSectionTitle(), o2.getSectionTitle());
		}
		return c;
	}

	private class QuestionTypeComparator implements Comparator<CorrectionIdentityAssessmentItemRow> {

		@Override
		public int compare(CorrectionIdentityAssessmentItemRow o1, CorrectionIdentityAssessmentItemRow o2) {
			QTI21QuestionType t1 = o1.getItemType();
			QTI21QuestionType t2 = o2.getItemType();
			
			int c = 0;
			if(t1 == null || t2 == null) {
				c = compareNullObjects(t1, t2);
			} else {
				c = t1.compareTo(t2);
			}
			if(c == 0) {
				c = compareItemTitle(o1, o2);
			}
			return c;
		}
	}
	
	private class CorrectedComparator implements Comparator<CorrectionIdentityAssessmentItemRow> {
		@Override
		public int compare(CorrectionIdentityAssessmentItemRow o1, CorrectionIdentityAssessmentItemRow o2) {
			int c = Boolean.compare(o1.isCorrected(), o2.isCorrected());
			if(c == 0) {
				c = compareItemTitle(o1, o2);
			}
			return c;
		}
	}
	
	private class NotCorrectedComparator implements Comparator<CorrectionIdentityAssessmentItemRow> {
		@Override
		public int compare(CorrectionIdentityAssessmentItemRow o1, CorrectionIdentityAssessmentItemRow o2) {
			boolean m1 = o1.isManualCorrection();
			boolean m2 = o2.isManualCorrection();
			
			int c = 0;
			if(m1 && m2) {
				boolean ms1 = o1.getManualScore() != null;
				boolean ms2 = o2.getManualScore() != null;
				if(ms1 == ms2) {
					c = compareItemTitle(o1, o2);
				} else {
					c = Boolean.compare(ms1, ms2);
				}	
			} else if(!m1 && !m2) {
				c = compareItemTitle(o1, o2);
			} else {
				c = Boolean.compare(m1, m2);
			}
			return c;
		}
	}
}
