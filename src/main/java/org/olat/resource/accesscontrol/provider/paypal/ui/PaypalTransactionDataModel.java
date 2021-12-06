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
package org.olat.resource.accesscontrol.provider.paypal.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;

/**
 * 
 * Initial date: 5 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalTransactionDataModel extends DefaultFlexiTableDataModel<PaypalTransaction>
	implements SortableFlexiTableDataModel<PaypalTransaction>, FilterableFlexiTableModel {

	private List<PaypalTransaction> backups;
	
	public PaypalTransactionDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		PaypalTransactionSortableDelegate sorter = new PaypalTransactionSortableDelegate(orderBy, this, null);
		List<PaypalTransaction> views = sorter.sort();
		super.setObjects(views);
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if(StringHelper.containsNonWhitespace(key)) {
			List<PaypalTransaction> filteredRows = new ArrayList<>(backups.size());
			if(PaypalMergedState.isValueOf(key)) {
				PaypalMergedState filterState = PaypalMergedState.valueOf(key);
				for(PaypalTransaction row:backups) {
					if(PaypalMergedState.value(row) == filterState) {
						filteredRows.add(row);
					}
				}
			} else {
				filteredRows.addAll(backups);
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		PaypalTransaction transaction = getObject(row);
		return getValueAt(transaction, col);
	}

	@Override
	public Object getValueAt(PaypalTransaction transaction, int col) {
		switch(Columns.values()[col]) {
			case status: return transaction;
			case orderNr: return transaction.getRefNo();
			case ipnTransactionId: return transaction.getTransactionId();
			case ipnTransactionStatus: return transaction.getTransactionStatus();
			case ipnSenderTransactionId: return transaction.getSenderTransactionId();
			case ipnSenderTransactionStatus: return transaction.getSenderTransactionStatus();
			case ipnSender: return transaction.getSenderEmail();
			case ipnPendingReason: return transaction.getPendingReason();
			case payAmount: return transaction.getSecurePrice();
			case payAck: return transaction.getAck();
			case payPaymentExecStatus: return transaction.getPaymentExecStatus();
			case payResponseDate: return transaction.getPayResponseDate();
			case olatStatus: return transaction.getStatus() == null ? "???" : transaction.getStatus().name();
			default: return null;
		}
	}

	@Override
	public void setObjects(List<PaypalTransaction> objects) {
		super.setObjects(objects);
		this.backups = objects;
	}

	public PaypalTransaction getTransaction(Long key) {
		List<PaypalTransaction> transactions = getObjects();
		if(transactions == null) return null;
		
		for(PaypalTransaction trx:transactions) {
			if(trx.getKey().equals(key)) {
				return trx;
			}
		}
		return null;
	}
	
	public enum Columns implements FlexiSortableColumnDef {
		status("paypal.transaction.status"),
		orderNr("paypal.transaction.order"),
		payResponseDate("paypal.transaction.response.date"),
		
		ipnTransactionId("paypal.transaction.id"),
		ipnTransactionStatus("paypal.transaction.status"),
		ipnSenderTransactionId("paypal.transaction.sender.id"),
		ipnSenderTransactionStatus("paypal.transaction.sender.status"),
		ipnSender("paypal.transaction.sender"),
		ipnPendingReason("paypal.transaction.pending.reason"),
		
		payAmount("paypal.transaction.amount"),
		payAck("paypal.transaction.ack"),
		payPaymentExecStatus("paypal.transaction.exec.status"),
		olatStatus("paypal.transaction.olat.status");
		
		private final String i18nKey;
		
		private Columns(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}