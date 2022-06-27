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
package org.olat.course.nodes.practice.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.nodes.practice.ui.PracticeParticipantTaxonomyStatisticsTableModel.TaxonomyStatisticsCols;

/**
 * 
 * Initial date: 10 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeParticipantTaxonomyStatisticsTableSortDelegate extends SortableFlexiTableModelDelegate<PracticeParticipantTaxonomyStatisticsRow> {

	public PracticeParticipantTaxonomyStatisticsTableSortDelegate(SortKey orderBy, PracticeParticipantTaxonomyStatisticsTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<PracticeParticipantTaxonomyStatisticsRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex == TaxonomyStatisticsCols.taxonomyLevel.ordinal()) {
			Collections.sort(rows, new TaxonomyLevelComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private class TaxonomyLevelComparator implements Comparator<PracticeParticipantTaxonomyStatisticsRow> {

		@Override
		public int compare(PracticeParticipantTaxonomyStatisticsRow o1, PracticeParticipantTaxonomyStatisticsRow o2) {
			if(o1.withoutTaxonomy() && o2.withoutTaxonomy()) {
				return 0;
			} else if(o1.withoutTaxonomy()) {
				return 1;
			} else if(o2.withoutTaxonomy()) {
				return -1;
			}
			
			String p1 = toPath(o1);
			String p2 = toPath(o2);
			return compareString(p1, p2);
		}
		
		private String toPath(PracticeParticipantTaxonomyStatisticsRow o) {
			StringBuilder sb = new StringBuilder();
			if(o.getTaxonomyPath() != null) {
				for(String path:o.getTaxonomyPath()) {
					sb.append(path).append("/");
				}
			}
			if(o.getTaxonomyLevelName() != null) {
				sb.append(o.getTaxonomyLevelName()).append("/");
			}
			return sb.toString();
		}
	}

}
