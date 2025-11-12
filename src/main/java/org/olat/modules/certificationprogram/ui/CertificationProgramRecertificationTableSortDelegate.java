package org.olat.modules.certificationprogram.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.certificationprogram.ui.CertificationProgramRecertificationTableModel.RecertificationCols;
import org.olat.modules.certificationprogram.ui.component.NextRecertificationInDays;

/**
 * 
 * Initial date: 12 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramRecertificationTableSortDelegate extends SortableFlexiTableModelDelegate<CertificationProgramRecertificationRow> {

	private static final RecertificationCols[] COLS = RecertificationCols.values();
	
	public CertificationProgramRecertificationTableSortDelegate(SortKey orderBy,
			CertificationProgramRecertificationTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CertificationProgramRecertificationRow> rows) {
		int columnIndex = getColumnIndex();
		switch(COLS[columnIndex]) {
			case nextRecertificationDays: Collections.sort(rows, new NextRecertificationInDaysComparator()); break;
			case certificate: Collections.sort(rows, new CertificateComparator()); break;
			case status: Collections.sort(rows, new StatusComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private class CertificateComparator implements Comparator<CertificationProgramRecertificationRow> {

		@Override
		public int compare(CertificationProgramRecertificationRow o1, CertificationProgramRecertificationRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjectsLast(o1, o2);
			}
			
			Date c1 = o1.getCertificationDate();
			Date c2 = o2.getCertificationDate();
			int c = 0;
			if(c1 == null || c2 == null) {
				c = compareDateAndTimestamps(c1, c2);
			}
			if(c == 0) {
				c = compareLongs(o1.getCertificateKey(), o2.getCertificateKey());
			}
			return c;
		}
	}
	
	private class StatusComparator implements Comparator<CertificationProgramRecertificationRow> {

		@Override
		public int compare(CertificationProgramRecertificationRow o1, CertificationProgramRecertificationRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjectsLast(o1, o2);
			}
			
			CertificationStatus c1 = o1.getCertificationStatus();
			CertificationStatus c2 = o2.getCertificationStatus();
			if(c1 == null || c2 == null) {
				return compareNullObjectsLast(c1, c2);
			}
			return c1.compareTo(c2);
		}
	}
	
	private class NextRecertificationInDaysComparator implements Comparator<CertificationProgramRecertificationRow> {

		@Override
		public int compare(CertificationProgramRecertificationRow o1, CertificationProgramRecertificationRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjectsLast(o1, o2);
			}
			
			NextRecertificationInDays n1 = o1.getNextRecertification();
			NextRecertificationInDays n2 = o2.getNextRecertification();
			if(n1 == null || n2 == null) {
				return compareNullObjectsLast(n1, n2);
			}
			
			int c = compareLongs(n1.days(), n2.days());
			if(c == 0) {
				c = compareLongs(n1.overdueDays(), n2.overdueDays());
			}
			return c;
		}
	}
}
