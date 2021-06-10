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
package org.olat.core.configuration.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;

/**
 * Initial date: 10.06.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class OlatPropertiesSortableDelegate extends SortableFlexiTableModelDelegate<OlatPropertiesTableContentRow> {

	public OlatPropertiesSortableDelegate(SortKey orderBy, SortableFlexiTableDataModel<OlatPropertiesTableContentRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<OlatPropertiesTableContentRow> rows) {
		Collections.sort(rows, new OlatPropertyComparator());
	}
	
	private class OlatPropertyComparator implements Comparator<OlatPropertiesTableContentRow> {

		@Override
		public int compare(OlatPropertiesTableContentRow t1, OlatPropertiesTableContentRow t2) {
			Object val1 = getTableModel().getValueAt(t1, getColumnIndex());
			Object val2 = getTableModel().getValueAt(t2, getColumnIndex());
			
			if (val1 == null || val2 == null) {
				return compareNullsLast(val1, val2);
			}
			if (val1 instanceof String && val2 instanceof String) {
				String s1 = (String) val1;
				String s2 = (String) val2;
				
				s1 = StringHelper.containsNonWhitespace(s1) ? s1 : null;
				s2 = StringHelper.containsNonWhitespace(s2) ? s2 : null;
				
				if (s1 == null || s2 == null) { 
					return compareNullsLast(s1, s2);
				}
				
				return getCollator().compare(s1, s2);
			}
			if(val1 instanceof Date && val2 instanceof Date) {
				return compareDateAndTimestamps((Date)val1, (Date)val2);
			}
			if (val1 instanceof Comparable && val2 instanceof Comparable) {
				@SuppressWarnings("rawtypes")
				Comparable c1 = (Comparable)val1;
				@SuppressWarnings("rawtypes")
				Comparable c2 = (Comparable)val2;
				@SuppressWarnings("unchecked")
				int s = c1.compareTo(c2);
				return s;
			}
			return val1.toString().compareTo(val2.toString());
		}
		
		private int compareNullsLast(Object o1, Object o2) {
			return (isAsc() ? -1 : 1) * compareNullObjects(o1, o2);
		}
	}
}
