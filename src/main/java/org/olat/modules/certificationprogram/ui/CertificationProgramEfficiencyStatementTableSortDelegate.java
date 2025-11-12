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
				return compareNullObjectsLast(o1, o2);
			}
			
			int c = compareBigDecimal(o1.getScore(), o2.getScore());
			if(c == 0) {
				c = compareBigDecimal(o1.getMaxScore(), o2.getMaxScore());
			}
			return c;
		}
	}
}
