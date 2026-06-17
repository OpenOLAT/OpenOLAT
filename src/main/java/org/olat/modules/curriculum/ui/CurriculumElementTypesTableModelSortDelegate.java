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
package org.olat.modules.curriculum.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.ToIntFunction;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.ui.CurriculumElementTypesTableModel.TypesCols;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTypesTableModelSortDelegate extends SortableFlexiTableModelDelegate<CurriculumElementTypeRow> {
	
	public CurriculumElementTypesTableModelSortDelegate(SortKey orderBy, CurriculumElementTypesTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<CurriculumElementTypeRow> rows) {
		String sortKey = getOrderBy() != null ? getOrderBy().getKey() : null;
		if (TypesCols.parents.name().equals(sortKey)) {
			rows.sort(countComparator(CurriculumElementTypeRow::getNumParents));
		} else if (TypesCols.children.name().equals(sortKey)) {
			rows.sort(countComparator(CurriculumElementTypeRow::getNumChildren));
		} else if (TypesCols.uses.name().equals(sortKey)) {
			rows.sort(countComparator(CurriculumElementTypeRow::getNumUses));
		} else {
			super.sort(rows);
		}
	}

	private Comparator<CurriculumElementTypeRow> countComparator(ToIntFunction<CurriculumElementTypeRow> extractor) {
		return (a, b) -> {
			int ca = extractor.applyAsInt(a);
			int cb = extractor.applyAsInt(b);
			
			// Special handling of ascending sort so that 0 values end at the bottom
			if (isAsc()) {
				// both 0: equal
				if (ca == 0 && cb == 0) return 0;
				
				// first 0, second greater: flip (return 1 instead of -1), so that 0 ends at the bottom
				if (ca == 0) return 1;
				
				// first greater, second 0: flip (return -1 instead of 1), so that 0 ends at the bottom
				if (cb == 0) return -1;
			}
			return Integer.compare(ca, cb);
		};
	}
}
