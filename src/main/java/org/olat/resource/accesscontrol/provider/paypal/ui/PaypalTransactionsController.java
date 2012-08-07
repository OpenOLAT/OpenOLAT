/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.resource.accesscontrol.provider.paypal.ui;

import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.id.context.StateMapped;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.accesscontrol.provider.paypal.manager.PaypalManager;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;

/**
 * 
 * Description:<br>
 * List paypal transactions
 * 
 * <P>
 * Initial Date:  30 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalTransactionsController extends FormBasicController implements Activateable2 {

	private static final String CMD_SELECT = "sel";
	
	private FormLink backLink;
	private TextElement transactionIdEl;
	private TableController transactionList;
	private PaypalTransactionDetailsController detailsController;
	
	private final PaypalManager paypalManager;
	private final Formatter formatter;
	
	public PaypalTransactionsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "paypal_transactions");
		
		paypalManager = CoreSpringFactory.getImpl(PaypalManager.class);
		formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		//search fields
		FormLayoutContainer searchFieldsLayout = FormLayoutContainer.createDefaultFormLayout("search-fields", getTranslator());
		searchFieldsLayout.setRootForm(mainForm);
		formLayout.add("search-fields", searchFieldsLayout);
		
		transactionIdEl = uifactory.addTextElement("search", "paypal.transaction.id", 32, "", searchFieldsLayout);

		//results
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "PaypalTransaction");		
		tableConfig.setTableEmptyMessage(translate("paypal.transactions.empty"));
		
		transactionList = new TableController(tableConfig, ureq, getWindowControl(), Collections.<ShortName>emptyList(), null, "" , null, false, getTranslator());
		transactionList.addColumnDescriptor(new CustomRenderColumnDescriptor("paypal.transaction.status", Columns.status.ordinal(), null,
				getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new PaypalTransactionStatusRenderer()));
		transactionList.addColumnDescriptor(new DefaultColumnDescriptor("paypal.transaction.order", Columns.orderNr.ordinal(), null, getLocale()));
		transactionList.addColumnDescriptor(new DefaultColumnDescriptor("paypal.transaction.response.date", Columns.payResponseDate.ordinal(), null, getLocale()));

		transactionList.addColumnDescriptor(new DefaultColumnDescriptor("paypal.transaction.amount", Columns.payAmount.ordinal(), null, getLocale()));
		transactionList.addColumnDescriptor(false, new DefaultColumnDescriptor("paypal.transaction.id", Columns.ipnTransactionId.ordinal(), null, getLocale()));
		transactionList.addColumnDescriptor(new DefaultColumnDescriptor("paypal.transaction.status", Columns.ipnTransactionStatus.ordinal(), null, getLocale()));
		transactionList.addColumnDescriptor(false, new DefaultColumnDescriptor("paypal.transaction.sender.id", Columns.ipnSenderTransactionId.ordinal(), null, getLocale()));
		transactionList.addColumnDescriptor(false, new DefaultColumnDescriptor("paypal.transaction.sender.status", Columns.ipnSenderTransactionStatus.ordinal(), null, getLocale()));
		transactionList.addColumnDescriptor(false, new DefaultColumnDescriptor("paypal.transaction.pending.reason", Columns.ipnPendingReason.ordinal(), null, getLocale()));
		transactionList.addColumnDescriptor(false, new DefaultColumnDescriptor("paypal.transaction.sender", Columns.ipnSender.ordinal(), null, getLocale()));
		//pay request
		transactionList.addColumnDescriptor(false, new DefaultColumnDescriptor("paypal.transaction.ack", Columns.payAck.ordinal(), null, getLocale()));
		transactionList.addColumnDescriptor(false, new DefaultColumnDescriptor("paypal.transaction.exec.status", Columns.payPaymentExecStatus.ordinal(), null, getLocale()));
		transactionList.addColumnDescriptor(new DefaultColumnDescriptor("paypal.transaction.olat.status", Columns.olatStatus.ordinal(), null, getLocale()));
		
		transactionList.addColumnDescriptor(new StaticColumnDescriptor(CMD_SELECT, "select", getTranslator().translate("select")));

		transactionList.setTableDataModel(new TransactionDataModel());
		
		transactionList.setSortColumn(Columns.payResponseDate.ordinal(), false);
		listenTo(transactionList);
		
		backLink = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);
		
		String page = velocity_root + "/paypal_transactions_list.html";
		FormLayoutContainer resultsLayout = FormLayoutContainer.createCustomFormLayout("results", getTranslator(), page);
		resultsLayout.setRootForm(mainForm);
		formLayout.add("results", resultsLayout);
		
		resultsLayout.put("results", transactionList.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String transactionId = transactionIdEl.getValue();
		doSearch(ureq, transactionId);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == backLink) {
			if(detailsController != null) {
				flc.remove(detailsController.getInitialComponent());
				removeAsListenerAndDispose(detailsController);
			}
			addSearchToHistory(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == transactionList) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				PaypalTransaction transaction = (PaypalTransaction)transactionList.getTableDataModel().getObject(rowid);
				if(CMD_SELECT.equals(actionid)) {
					if(detailsController != null) {
						removeAsListenerAndDispose(detailsController);
					}
					
					selectTransaction(ureq, transaction);
				}
			}
		}
	}
	
	protected void selectTransaction(UserRequest ureq, PaypalTransaction transaction) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(PaypalTransaction.class, transaction.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		detailsController = new PaypalTransactionDetailsController(ureq, bwControl, transaction, mainForm);
		listenTo(detailsController);
		flc.put("details", detailsController.getInitialComponent());
	}

	private void doSearch(UserRequest ureq, String transactionId) {
		List<PaypalTransaction> transactions = paypalManager.findTransactions(transactionId);
		transactionList.setTableDataModel(new TransactionDataModel(transactions));
		addSearchToHistory(ureq);
	}
	
	protected void addSearchToHistory(UserRequest ureq) {
		String transactionId = transactionIdEl.getValue();
		StateMapped state = new StateMapped();
		if(StringHelper.containsNonWhitespace(transactionId)) {
			state.getDelegate().put("transactionId", transactionId);
		}
		
		ContextEntry currentEntry = getWindowControl().getBusinessControl().getCurrentContextEntry();
		if(currentEntry != null) {
			currentEntry.setTransientState(state);
		}
		addToHistory(ureq, getWindowControl());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state instanceof StateMapped) {
			StateMapped map = (StateMapped)state;
			String transactionId = map.getDelegate().get("transactionId");
			if(StringHelper.containsNonWhitespace(transactionId)) {
				transactionIdEl.setValue(transactionId);
				doSearch(ureq, transactionId);
			}
		}
		
		if(entries == null || entries.isEmpty()) return;
		
		Long trxId = entries.get(0).getOLATResourceable().getResourceableId();
		TransactionDataModel model = (TransactionDataModel)transactionList.getTableDataModel();
		PaypalTransaction trx = model.getTransaction(trxId);
		if(trx != null) {
			selectTransaction(ureq, trx);
		}
	}

	private class TransactionDataModel implements TableDataModel<PaypalTransaction> {

		private List<PaypalTransaction> transactions;
		
		public TransactionDataModel() {
			this(Collections.<PaypalTransaction>emptyList());
		}
		
		public TransactionDataModel(List<PaypalTransaction> transactions) {
			this.transactions = transactions;
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return transactions == null ? 0 : transactions.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			PaypalTransaction transaction = getObject(row);
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
				case payResponseDate: {
					if(transaction.getPayResponseDate() == null) return "";
					return formatter.formatDateAndTime(transaction.getPayResponseDate());
				}
				
				case olatStatus: return transaction.getStatus() == null ? "???" : transaction.getStatus().name();
				
				default: return null;
			}
		}

		@Override
		public PaypalTransaction getObject(int row) {
			return transactions.get(row);
		}
		
		public  PaypalTransaction getTransaction(Long key) {
			if(transactions == null) return null;
			
			for(PaypalTransaction trx:transactions) {
				if(trx.getKey().equals(key)) {
					return trx;
				}
			}
			return null;
		}

		@Override
		public void setObjects(List<PaypalTransaction> objects) {
			transactions = objects;
		}

		@Override
		public Object createCopyWithEmptyList() {
			return new TransactionDataModel();
		}
	}
	
	private enum Columns {
		status,
		orderNr,
		payResponseDate,
		
		ipnTransactionId,
		ipnTransactionStatus,
		ipnSenderTransactionId,
		ipnSenderTransactionStatus,
		ipnSender,
		ipnPendingReason,
		
		payAmount,
		payAck,
		payPaymentExecStatus,
		olatStatus,
	}
}