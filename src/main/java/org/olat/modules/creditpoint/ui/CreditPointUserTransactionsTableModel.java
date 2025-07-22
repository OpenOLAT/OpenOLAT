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
package org.olat.modules.creditpoint.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.modules.creditpoint.CreditPointTransactionType;

/**
 * 
 * Initial date: 7 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointUserTransactionsTableModel extends DefaultFlexiTableDataModel<CreditPointTransactionRow>
implements SortableFlexiTableDataModel<CreditPointTransactionRow> {
	
	private static final TransactionCols[] COLS = TransactionCols.values();
	
	private final Locale locale;
	private List<CreditPointTransactionRow> backupRows;
	
	public CreditPointUserTransactionsTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CreditPointTransactionRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	public void filter(String quickSearch, List<FlexiTableFilter> filters) {
		if(filters != null && (StringHelper.containsNonWhitespace(quickSearch) || (!filters.isEmpty() && filters.get(0) != null))) {
			final List<CreditPointTransactionType> types = getTypes(filters);
			final String orderNumber = getOrderNumber(filters);
			
			List<CreditPointTransactionRow> filteredRows = new ArrayList<>(backupRows.size());
			for(CreditPointTransactionRow row:backupRows) {
				if(acceptType(types, row)
						&& acceptOrderNumber(orderNumber, row)) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private boolean acceptType(List<CreditPointTransactionType> types, CreditPointTransactionRow row) {
		if(types == null || types.isEmpty()) return true;
		return types.contains(row.getTransaction().getTransactionType());
	}
	
	private List<CreditPointTransactionType> getTypes(List<FlexiTableFilter> filters) {
		FlexiTableFilter typeFilter = FlexiTableFilter.getFilter(filters, CreditPointUserTransactionsController.FILTER_TRANSACTION_TYPE);
		if(typeFilter instanceof FlexiTableExtendedFilter extendedfilter) {
			List<String> filterValues = extendedfilter.getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(CreditPointTransactionType::valueOf)
						.toList();
			}
		}
		return List.of();
	}
	
	private boolean acceptOrderNumber(String orderNumber, CreditPointTransactionRow row) {
		if(orderNumber == null) return true;
		return orderNumber.equals(row.getTransaction().getOrderNumber().toString());
	}
	
	private String getOrderNumber(List<FlexiTableFilter> filters) {
		FlexiTableFilter numberFilter = FlexiTableFilter.getFilter(filters, CreditPointUserTransactionsController.FILTER_ORDER_NUMBER);
		if(numberFilter != null) {
			String filterValue = numberFilter.getValue();
			if (StringHelper.containsNonWhitespace(filterValue)) {
				return filterValue;
			}
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CreditPointTransactionRow transactionRow = getObject(row);
		return getValueAt(transactionRow, col);
	}

	@Override
	public Object getValueAt(CreditPointTransactionRow row, int col) {
		return switch(COLS[col]) {
			case id -> row.getKey();
			case type -> row.getTransaction().getTransactionType();
			case nr -> row.getTransaction().getOrderNumber();
			case creationDate -> row.getTransaction().getCreationDate();
			case credit -> row.getCredit();
			case debit -> row.getDebit();
			case expirationDate -> row.getExpirationDate();
			case source -> row.getSource();
			case note -> row.getNoteLink();
			case tools -> Boolean.TRUE;
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CreditPointTransactionRow> objects) {
		backupRows = new ArrayList<>(objects);
		super.setObjects(objects);
	}

	public enum TransactionCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		type("table.header.transaction.type"),
		nr("table.header.transaction.nr"),
		creationDate("table.header.transaction.date"),
		credit("table.header.credit"),
		debit("table.header.debit"),
		expirationDate("table.header.expiration.date"),
		source("table.header.source"),
		note("table.header.note"),
		tools("action.more");
		
		private final String i18nKey;
		
		private TransactionCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
