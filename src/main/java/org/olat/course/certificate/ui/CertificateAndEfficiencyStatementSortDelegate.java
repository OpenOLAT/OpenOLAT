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
package org.olat.course.certificate.ui;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.NullOrder;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListModel.Cols;
import org.olat.modules.grade.ui.GradeUIFactory;

/**
 *
 * Initial date: 26 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CertificateAndEfficiencyStatementSortDelegate extends SortableFlexiTableModelDelegate<CertificateAndEfficiencyStatementRow> {

	public CertificateAndEfficiencyStatementSortDelegate(SortKey orderBy,
			SortableFlexiTableDataModel<CertificateAndEfficiencyStatementRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<CertificateAndEfficiencyStatementRow> rows) {
		Cols column = CertificateAndEfficiencyStatementListModel.COLS[getColumnIndex()];
		if (column == Cols.grade) {
			Collections.sort(rows, new GradeComparator());
		} else {
			super.sort(rows);
		}
	}

	private class GradeComparator implements Comparator<CertificateAndEfficiencyStatementRow> {

		private final Translator gradeTranslator;

		public GradeComparator() {
			gradeTranslator = Util.createPackageTranslator(GradeUIFactory.class, getLocale());
		}

		@Override
		public int compare(CertificateAndEfficiencyStatementRow r1, CertificateAndEfficiencyStatementRow r2) {
			String s1 = r1 != null && r1.getGrade() != null
					? GradeUIFactory.translatePerformanceClass(gradeTranslator, r1.getPerformanceClassIdent(), r1.getGrade(), r1.getGradeSystemIdent())
					: null;
			String s2 = r2 != null && r2.getGrade() != null
					? GradeUIFactory.translatePerformanceClass(gradeTranslator, r2.getPerformanceClassIdent(), r2.getGrade(), r2.getGradeSystemIdent())
					: null;

			if (s1 == null || s2 == null) {
				return compareNullObjects(s1, s2, NullOrder.NULLS_ALWAYS_LAST);
			}

			try {
				BigDecimal d1 = new BigDecimal(s1);
				BigDecimal d2 = new BigDecimal(s2);
				return d1.compareTo(d2);
			} catch (NumberFormatException e) {
				return compareString(s1, s2);
			}
		}
	}
}
