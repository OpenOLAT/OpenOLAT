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
package org.olat.resource.accesscontrol.provider.paypalcheckout.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.provider.paypal.ui.PaypalMergedState;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;

/**
 * 
 * Initial date: 5 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutTransactionDataModel extends DefaultFlexiTableDataModel<PaypalCheckoutTransaction>
	implements SortableFlexiTableDataModel<PaypalCheckoutTransaction>, FilterableFlexiTableModel {

	private final Locale locale;
	private List<PaypalCheckoutTransaction> backups;
	
	public PaypalCheckoutTransactionDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<PaypalCheckoutTransaction> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if(StringHelper.containsNonWhitespace(key)) {
			List<PaypalCheckoutTransaction> filteredRows = new ArrayList<>(backups.size());
			if(PaypalMergedState.isValueOf(key)) {
				for(PaypalCheckoutTransaction row:backups) {
					if(accept(key, row)) {
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
	
	private boolean accept(String filter, PaypalCheckoutTransaction transaction) {
		boolean accept = true;
		if(StringHelper.containsNonWhitespace(filter) && !"showAll".equals(filter)) {
			String status = transaction.getStatus() == null ? "" : transaction.getStatus().name();
			String paypalStatus = transaction.getPaypalOrderStatus();
			accept = filter.equalsIgnoreCase(status) || filter.equalsIgnoreCase(paypalStatus);
		}
		return accept;
	}

	@Override
	public Object getValueAt(int row, int col) {
		PaypalCheckoutTransaction transaction = getObject(row);
		return getValueAt(transaction, col);
	}

	@Override
	public Object getValueAt(PaypalCheckoutTransaction transaction, int col) {
		switch(CheckoutCols.values()[col]) {
			case status: return transaction.getPaypalOrderStatus();
			case date: return transaction.getCreationDate();
			case paypalOrderNr: return transaction.getPaypalOrderId();
			case orderNr: return transaction.getOrderNr();
			case amount: return transaction.getSecurePrice();
			case olatStatus: return transaction.getStatus() == null ? "???" : transaction.getStatus().name();
			default: return null;
		}
	}

	@Override
	public void setObjects(List<PaypalCheckoutTransaction> objects) {
		super.setObjects(objects);
		this.backups = objects;
	}

	public PaypalCheckoutTransaction getTransaction(Long key) {
		List<PaypalCheckoutTransaction> transactions = getObjects();
		if(transactions == null) return null;
		
		for(PaypalCheckoutTransaction trx:transactions) {
			if(trx.getKey().equals(key)) {
				return trx;
			}
		}
		return null;
	}
	
	public enum CheckoutCols implements FlexiSortableColumnDef {
		status("table.header.order.status"),
		date("table.header.order.date"),
		orderNr("table.header.order.nr"),
		paypalOrderNr("table.header.order.id"),
		amount("paypal.transaction.amount"),
		olatStatus("table.header.order.oo.status");
		
		private final String i18nKey;
		
		private CheckoutCols(String i18nKey) {
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