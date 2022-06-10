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
import org.olat.course.nodes.practice.ui.PracticeResourceTaxonomyTableModel.PracticeTaxonomyCols;

/**
 * 
 * Initial date: 10 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeResourceTaxonomyTableSortDelegate extends SortableFlexiTableModelDelegate<PracticeResourceTaxonomyRow> {
	
	public PracticeResourceTaxonomyTableSortDelegate(SortKey orderBy, PracticeResourceTaxonomyTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<PracticeResourceTaxonomyRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex == PracticeTaxonomyCols.taxonomyLevel.ordinal()) {
			Collections.sort(rows, new TaxonomyLevelComparator());
		} else {
			super.sort(rows);
		}
	}
	
	private class TaxonomyLevelComparator implements Comparator<PracticeResourceTaxonomyRow> {

		@Override
		public int compare(PracticeResourceTaxonomyRow o1, PracticeResourceTaxonomyRow o2) {
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
		
		private String toPath(PracticeResourceTaxonomyRow o) {
			StringBuilder sb = new StringBuilder();
			if(o.getTaxonomyPath() != null) {
				for(String path:o.getTaxonomyPath()) {
					sb.append(path).append("/");
				}
			}
			if(o.getTaxonomyLevel() != null) {
				sb.append(o.getTaxonomyLevel()).append("/");
			}
			return sb.toString();
		}
	}

}
