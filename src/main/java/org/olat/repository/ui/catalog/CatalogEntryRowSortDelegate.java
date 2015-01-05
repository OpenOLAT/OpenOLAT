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
package org.olat.repository.ui.catalog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.catalog.CatalogEntryRowModel.Cols;

/**
 * 
 * Initial date: 05.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntryRowSortDelegate extends SortableFlexiTableModelDelegate<CatalogEntryRow> {

	public CatalogEntryRowSortDelegate(SortKey orderBy, CatalogEntryRowModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<CatalogEntryRow> rows) {
		int columnIndex = getColumnIndex();
		Cols column = Cols.values()[columnIndex];
		switch(column) {
			case ac: Collections.sort(rows, new ACComparator()); break;
			case type: Collections.sort(rows, new TypeComparator()); break;
			default: {
				super.sort(rows);
			}
		}
	}
	
	private class TypeComparator implements Comparator<CatalogEntryRow> {
		@Override
		public int compare(CatalogEntryRow t1, CatalogEntryRow t2) {
			String r1 = t1.getResourceType();
			String r2 = t2.getResourceType();
			
			int compare = compareString(r1, r2);
			if(compare == 0) {
				compare = compareString(t1.getDisplayname(), t2.getDisplayname());
			}
			return compare;
		}
	}

	private class ACComparator implements Comparator<CatalogEntryRow> {
		@Override
		public int compare(CatalogEntryRow t1, CatalogEntryRow t2) {
			List<PriceMethod> r1 = t1.getAccessTypes();
			List<PriceMethod> r2 = t2.getAccessTypes();
				
			if(r1 != null && r1.size() > 0) {
				if(r2 != null && r2.size() > 0) {
					return r1.size() - r2.size();
				}
				return 1;
			} else if(r2 != null && r2.size() > 0) {
				return -1;
			}
			return compareString(t1.getDisplayname(), t2.getDisplayname());
		}
	}
}
