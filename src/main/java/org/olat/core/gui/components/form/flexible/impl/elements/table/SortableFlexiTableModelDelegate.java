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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;

/**
 * Replicate the same mechanism as in the TableController but
 * as delegate.
 * 
 * 
 * Initial date: 11.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SortableFlexiTableModelDelegate<T> {
	
	private boolean asc;
	private int columnIndex;
	private final SortKey orderBy;
	private final Collator collator; 
	private final SortableFlexiTableDataModel<T> tableModel;
	private final Locale locale;
	
	public SortableFlexiTableModelDelegate(SortKey orderBy, SortableFlexiTableDataModel<T> tableModel, Locale locale) {
		this.tableModel = tableModel;
		this.orderBy = orderBy;
		this.locale = locale;
		if(orderBy != null && orderBy.getKey() != null) {
			FlexiColumnModel colModel = getColumnModel(orderBy.getKey(), tableModel.getTableColumnModel());
			if(colModel != null) {
				columnIndex = colModel.getColumnIndex();
			}
			asc = orderBy.isAsc();
		} else {
			columnIndex = 0;
			asc = orderBy == null || orderBy.isAsc();
		}
		
		if (locale != null) {
			collator = Collator.getInstance(locale);
		} else {
			collator = Collator.getInstance();
		}
	}
	
	public int getColumnIndex() {
		return columnIndex;
	}
	
	public boolean isAsc() {
		return asc;
	}
	
	public Collator getCollator() {
		return collator;
	}
	
	public SortKey getOrderBy() {
		return orderBy;
	}
	
	public SortableFlexiTableDataModel<T> getTableModel() {
		return tableModel;
	}
	
	public Locale getLocale() {
		return locale;
	}

	public List<T> sort() {
		int rowCount = tableModel.getRowCount();
		List<T> rows = new ArrayList<>(rowCount);
		for(int i=0; i<rowCount; i++) {
			rows.add(tableModel.getObject(i));
		}
		sort(rows);
		reverse(rows);
		return rows;
	}
	
	protected void reverse(List<T> rows) {
		if(!asc) {
			Collections.reverse(rows);
		}
	}
	
	protected void sort(List<T> rows) {
		Collections.sort(rows, new DefaultComparator());
	}
	
	private static final FlexiColumnModel getColumnModel(String orderBy, FlexiTableColumnModel columnModel) {
		FlexiColumnModel colModel = null;
		for(int i=columnModel.getColumnCount(); i-->0; ) {
			FlexiColumnModel cm = columnModel.getColumnModel(i);
			if(cm.isSortable() && orderBy.equals(cm.getSortKey())) {
				colModel = cm;
				break;
			}
		}
		return colModel;
	}
	
	protected final int compareString(final String a, final String b) {
		if (a == null || b == null) {
			return compareNullObjects(a, b);
		}
		return collator == null ? a.compareTo(b) : collator.compare(a, b);
	}

	protected final int compareBooleans(final Boolean a, final Boolean b) {
		if (a == null || b == null) {
			return compareNullObjects(a, b);
		}
		
		boolean ba = a.booleanValue();
		boolean bb = b.booleanValue();
		return compareBooleans(ba, bb);
	}
	
	protected final int compareBooleans(final boolean a, final boolean b) {
		return a? (b? 0: -1):(b? 1: 0);
	}
	
	protected final int compareDateAndTimestamps(Date a, Date b) {
		if (a == null || b == null) {
			return compareNullObjects(a, b);
		}
		
		if (a instanceof Timestamp) { // a timestamp (a) cannot compare a date (b), but vice versa is ok.
			if(b instanceof Timestamp) {
				return ((Timestamp)a).compareTo((Timestamp)b);
			} else {
				Timestamp ta = (Timestamp)a;
				Date aAsDate = new Date(ta.getTime());
				return aAsDate.compareTo(b);
			}
		} else if (b instanceof Timestamp) {
			Timestamp tb = (Timestamp)b;
			Date bAsDate = new Date(tb.getTime());
			return a.compareTo(bAsDate);
		}
		return a.compareTo(b);
	}
	
	protected final int compareLongs(Long a, Long b) {
		if (a == null || b == null) {
			return compareNullObjects(a, b);
		}
		return a.compareTo(b);
	}
	
	protected final int compareInts(int a, int b) {
		return Integer.compare(a, b);
	}
	
	protected final int compareDoubles(double a, double b) {
		return Double.compare(a, b);
	}

	protected final int compareNullObjects(final Object a, final Object b) {
		boolean ba = (a == null);
		boolean bb = (b == null);
		return ba? (bb? 0: -1):(bb? 1: 0);
	}
	
	public class ReverseComparator implements Comparator<T> {
		
		private final Comparator<T> delegate;
		
		public ReverseComparator(Comparator<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public int compare(T o1, T o2) {
			return -1 * delegate.compare(o1, o2);
		}
	}
	
	public class DefaultComparator implements Comparator<T> {

		@Override
		public int compare(T t1, T t2) {
			Object val1 = tableModel.getValueAt(t1, columnIndex);
			Object val2 = tableModel.getValueAt(t2, columnIndex);
			
			if (val1 == null || val2 == null) {
				return compareNullObjects(val1, val2);
			}
			if (val1 instanceof String && val2 instanceof String) {
				return collator.compare(val1, val2);
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
	}
}