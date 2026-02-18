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
package org.olat.modules.certificationprogram.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.certificationprogram.ui.CertificationProgramEfficiencyStatementTableModel.StatmentCols;

/**
 * 
 * Initial date: 12 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramEfficiencyStatementTableSortDelegate extends SortableFlexiTableModelDelegate<CertificationProgramEfficiencyStatementRow> {
	
	private static final StatmentCols[] COLS = StatmentCols.values();
	
	public CertificationProgramEfficiencyStatementTableSortDelegate(SortKey orderBy,
			CertificationProgramEfficiencyStatementTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CertificationProgramEfficiencyStatementRow> rows) {
		int columnIndex = getColumnIndex();
		switch(COLS[columnIndex]) {
			case score: Collections.sort(rows, new ScoreComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private class ScoreComparator implements Comparator<CertificationProgramEfficiencyStatementRow> {

		@Override
		public int compare(CertificationProgramEfficiencyStatementRow o1, CertificationProgramEfficiencyStatementRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjectsAlwaysLast(o1, o2);
			}
			
			int c = compareBigDecimal(o1.getScore(), o2.getScore());
			if(c == 0) {
				c = compareBigDecimal(o1.getMaxScore(), o2.getMaxScore());
			}
			return c;
		}
	}
}
