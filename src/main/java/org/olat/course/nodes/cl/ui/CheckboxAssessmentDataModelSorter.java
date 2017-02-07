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
package org.olat.course.nodes.cl.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.cl.ui.CheckboxAssessmentDataModel.Cols;

/**
 * 
 * Initial date: 5 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxAssessmentDataModelSorter extends SortableFlexiTableModelDelegate<CheckboxAssessmentRow> {

	public CheckboxAssessmentDataModelSorter(SortKey orderBy, CheckboxAssessmentDataModel model, Locale locale) {
		super(orderBy, model, locale);
	}
	
	@Override
	protected void sort(List<CheckboxAssessmentRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex == Cols.check.ordinal()) {
			Collections.sort(rows, new CheckBoxComparator());
			
		} else if(columnIndex == Cols.points.ordinal()) {
			Collections.sort(rows, new PointsComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private class PointsComparator implements Comparator<CheckboxAssessmentRow> {
		@Override
		public int compare(CheckboxAssessmentRow o1, CheckboxAssessmentRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			TextElement e1 = o1.getPointEl();
			TextElement e2 = o2.getPointEl();
			if(e1 == null || e2 == null) {
				return compareNullObjects(e1, e2);
			}
			
			double t1 = parseDouble(e1);
			double t2 = parseDouble(e2);
			return compareDoubles(t1, t2);
		}
		
		private double parseDouble(TextElement el) {
			if(el != null && StringHelper.containsNonWhitespace(el.getValue())) {
				try {
					return Double.parseDouble(el.getValue());
				} catch (NumberFormatException e) {
					//ignore parsing error, the validation take this
				}
			}
			return 0.0d;
		}
	}
	
	private class CheckBoxComparator implements Comparator<CheckboxAssessmentRow> {
		@Override
		public int compare(CheckboxAssessmentRow o1, CheckboxAssessmentRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			MultipleSelectionElement e1 = o1.getCheckedEl();
			MultipleSelectionElement e2 = o2.getCheckedEl();
			if(e1 == null || e2 == null) {
				return compareNullObjects(e1, e2);
			}
			
			boolean b1 = e1.isAtLeastSelected(1);
			boolean b2 = e2.isAtLeastSelected(1);
			return compareBooleans(b1, b2);
		}
	}
}
