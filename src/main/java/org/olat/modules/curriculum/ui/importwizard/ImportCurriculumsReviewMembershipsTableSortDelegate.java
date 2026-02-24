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
package org.olat.modules.curriculum.ui.importwizard;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 24 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsReviewMembershipsTableSortDelegate extends SortableFlexiTableModelDelegate<ImportedMembershipRow> {
	
	public ImportCurriculumsReviewMembershipsTableSortDelegate(SortKey orderBy, ImportCurriculumsReviewMembershipsTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<ImportedMembershipRow> rows) {
		SortKey orderBy = getOrderBy();
		if(orderBy != null && ImportCurriculumsReviewMembershipsController.SORT_RELEVANCE_KEY.equals(orderBy.getKey())) {
			 Collections.sort(rows, new RelevanceComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private class RelevanceComparator implements Comparator<ImportedMembershipRow> {
		@Override
		public int compare(ImportedMembershipRow o1, ImportedMembershipRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			ImportCurriculumsStatus s1 = normalizeStatus(o1);
			ImportCurriculumsStatus s2 = normalizeStatus(o2);
			
			int c;
			if(s1 == null || s2 == null) {
				c = compareNullObjects(s1, s2);
			} else {
				c = s1.compareTo(s2);
			}
			
			if(c == 0) {
				c = compareString(o1.getUsername(), o2.getUsername());
			}
			return c;
		}
		
		private ImportCurriculumsStatus normalizeStatus(ImportedMembershipRow o) {
			ImportCurriculumsStatus s = o.getStatus();
			if(s == null) {
				if(o.hasValidationErrors()) {
					s = ImportCurriculumsStatus.ERROR;
				} else {
					s = ImportCurriculumsStatus.NO_CHANGES;
				}
			}
			return s;
		}
	}
}
