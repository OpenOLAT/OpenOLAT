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
package org.olat.modules.scorm.assessment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.scorm.assessment.ScoSummaryTableModel.SummaryCols;

/**
 * 
 * Initial date: 20 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ScoSummaryTableSortDelegate extends SortableFlexiTableModelDelegate<ScoSummaryRow> {
	
	private static final SummaryCols[] COLS = SummaryCols.values();
	
	public ScoSummaryTableSortDelegate(SortKey orderBy, ScoSummaryTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<ScoSummaryRow> rows) {
		int columnIndex = getColumnIndex();
		switch(COLS[columnIndex]) {
			case lastRun: Collections.sort(rows, new LastRunComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	public static class LastRunComparator implements Comparator<ScoSummaryRow> {
		@Override
		public int compare(ScoSummaryRow o1, ScoSummaryRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
		
			int c = o1.getDate().compareTo(o2.getDate());
			if(c == 0) {
				c = o1.getScoId().compareTo(o2.getScoId());
			}
			return c;
		}
	}
}
