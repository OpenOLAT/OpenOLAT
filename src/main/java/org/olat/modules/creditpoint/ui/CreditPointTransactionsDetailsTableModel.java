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

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 17 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointTransactionsDetailsTableModel extends DefaultFlexiTableDataModel<CreditPointTransactionDetailsRow> {
	
	private static final TransactionDetailCols[] COLS = TransactionDetailCols.values();

	public CreditPointTransactionsDetailsTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CreditPointTransactionDetailsRow details = getObject(row);
		return switch(COLS[col]) {
			case id -> details.getKey();
			case nr -> details.getTransaction().getOrderNumber();
			case creationDate -> details.getCreationDate();
			case credit -> details.getCredit();
			case debit -> details.getDebit();
			case expirationDate -> details.getExpirationDate();
			case source -> details.getSource();
			default -> "ERROR";
		};
	}
	
	public enum TransactionDetailCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		nr("table.header.transaction.nr"),
		creationDate("table.header.transaction.date"),
		credit("table.header.credit"),
		debit("table.header.debit"),
		expirationDate("table.header.expiration.date"),
		source("table.header.source");
		
		private final String i18nKey;
		
		private TransactionDetailCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
