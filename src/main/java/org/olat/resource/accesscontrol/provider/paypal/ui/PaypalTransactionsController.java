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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.id.context.StateMapped;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.accesscontrol.provider.paypal.manager.PaypalManager;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;
import org.olat.resource.accesscontrol.provider.paypal.ui.PaypalTransactionDataModel.Columns;
import org.springframework.beans.factory.annotation.Autowired;

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
	private FlexiTableElement tableEl;
	private PaypalTransactionDataModel dataModel;
	
	private PaypalTransactionDetailsController detailsController;
	
	@Autowired
	private PaypalManager paypalManager;
	
	public PaypalTransactionsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "paypal_transactions");
		initForm(ureq);
		doSearch(ureq, null);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		backLink = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.status, new PaypalTransactionStatusRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.orderNr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.payResponseDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.payAmount));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.ipnTransactionId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.ipnTransactionStatus));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.ipnSenderTransactionId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.ipnSenderTransactionStatus));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.ipnPendingReason));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.ipnSender));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.payAck));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.payPaymentExecStatus));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.olatStatus));
		DefaultFlexiColumnModel selectColumns = new DefaultFlexiColumnModel("select", translate("select"), CMD_SELECT);
		selectColumns.setExportable(false);
		selectColumns.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(selectColumns);
		
		dataModel = new PaypalTransactionDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "results", dataModel, 25, false, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("paypal.transactions.empty");
		tableEl.setCustomizeColumns(true);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(Columns.payResponseDate.name(), false));
		options.setFromColumnModel(true);
		tableEl.setSortSettings(options);
		
		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("show.all"), "showAll"));
		filters.add(FlexiTableFilter.SPACER);
		for(PaypalMergedState state:PaypalMergedState.values()) {
			filters.add(new FlexiTableFilter(translate("filter.".concat(state.name())), state.name()));
		}
		tableEl.setFilters("", filters, false);
		tableEl.setAndLoadPersistedPreferences(ureq, "FPaypalTransaction");
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == backLink) {
			if(detailsController != null) {
				flc.remove(detailsController.getInitialComponent());
				removeAsListenerAndDispose(detailsController);
			}
			addSearchToHistory(ureq);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				PaypalTransaction row = dataModel.getObject(se.getIndex());
				if(CMD_SELECT.equals(se.getCommand())) {
					doSelectTransaction(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				doSearch(ureq, se.getSearch());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	protected void doSelectTransaction(UserRequest ureq, PaypalTransaction transaction) {
		removeAsListenerAndDispose(detailsController);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(PaypalTransaction.class, transaction.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		detailsController = new PaypalTransactionDetailsController(ureq, bwControl, transaction, mainForm);
		listenTo(detailsController);
		flc.put("details", detailsController.getInitialComponent());
	}

	private void doSearch(UserRequest ureq, String transactionId) {
		List<PaypalTransaction> transactions = paypalManager.findTransactions(transactionId);
		dataModel.setObjects(transactions);
		tableEl.reset(true, true, true);
		addSearchToHistory(ureq);
	}
	
	protected void addSearchToHistory(UserRequest ureq) {
		String transactionId = tableEl.getQuickSearchString();
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
				tableEl.quickSearch(ureq, transactionId);
			}
		}
		
		if(entries == null || entries.isEmpty()) return;
		
		Long trxId = entries.get(0).getOLATResourceable().getResourceableId();
		PaypalTransaction trx = dataModel.getTransaction(trxId);
		if(trx != null) {
			doSelectTransaction(ureq, trx);
		}
	}
}