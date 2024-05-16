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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.ui.CurriculumElementSearchDataModel.SearchCols;

/**
 * 
 * Initial date: 16 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementSearchTableModelSortDelegate extends SortableFlexiTableModelDelegate<CurriculumElementSearchRow> {
	
	public CurriculumElementSearchTableModelSortDelegate(SortKey orderBy, CurriculumElementSearchDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CurriculumElementSearchRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex == SearchCols.resources.ordinal()) {
			Collections.sort(rows, new NumOfResourcesComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private class NumOfResourcesComparator implements Comparator<CurriculumElementSearchRow> {
		@Override
		public int compare(CurriculumElementSearchRow o1, CurriculumElementSearchRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			long n1 = o1.getNumOfRessources();
			long n2 = o2.getNumOfRessources();
			
			int c = Long.compare(n1, n2);
			if(c == 0) {
				c = compareString(o1.getCurriculumDisplayName(), o2.getCurriculumDisplayName());
			}
			return c;
		}
	}
}
