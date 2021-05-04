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

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutManager;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutStatus;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.ui.PaypalCheckoutTransactionDataModel.CheckoutCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutTransactionsController extends FormBasicController implements Activateable2 {
	
	private FlexiTableElement tableEl;
	private PaypalCheckoutTransactionDataModel dataModel;
	
	private CloseableModalController cmc;
	private PaypalCheckoutTransactionDetailsController transactionDetailsCtrl;
	
	@Autowired
	private PaypalCheckoutManager paypalManager;
	
	public PaypalCheckoutTransactionsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "transactions");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CheckoutCols.status,
				new PaypalCheckoutTransactionStatusRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CheckoutCols.date));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CheckoutCols.status));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CheckoutCols.paypalOrderNr, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CheckoutCols.amount));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CheckoutCols.orderNr, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CheckoutCols.olatStatus));
		
		dataModel = new PaypalCheckoutTransactionDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "results", dataModel, 25, false, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("paypal.transactions.empty");
		tableEl.setCustomizeColumns(true);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(CheckoutCols.date.name(), false));
		options.setFromColumnModel(true);
		tableEl.setSortSettings(options);
		
		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("show.all"), "showAll"));
		filters.add(FlexiTableFilter.SPACER);
		for(PaypalCheckoutStatus state:PaypalCheckoutStatus.values()) {
			String filter = state.name().toLowerCase();
			filters.add(new FlexiTableFilter(translate("filter.".concat(filter)), filter));
		}
		tableEl.setFilters("", filters, false);
		tableEl.setAndLoadPersistedPreferences(ureq, "paypal-checkout-transactions");	
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<PaypalCheckoutTransaction> transactions = paypalManager.searchTransactions(null);
		dataModel.setObjects(transactions);
		tableEl.reset(true, true, true);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(transactionDetailsCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(transactionDetailsCtrl);
		removeAsListenerAndDispose(cmc);
		transactionDetailsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				PaypalCheckoutTransaction row = dataModel.getObject(se.getIndex());
				if("select".equals(se.getCommand())) {
					doSelectTransaction(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				doSearch(se.getSearch());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSearch(String search) {
		List<PaypalCheckoutTransaction> transactions = paypalManager.searchTransactions(search);
		dataModel.setObjects(transactions);
		tableEl.reset(true, true, true);
	}
	
	private void doSelectTransaction(UserRequest ureq, PaypalCheckoutTransaction trx) {
		transactionDetailsCtrl = new PaypalCheckoutTransactionDetailsController(ureq, getWindowControl(), trx);
		listenTo(transactionDetailsCtrl);
		
		String title = translate("paypal.transaction.title", new String[] { trx.getPaypalOrderId() });
		cmc = new CloseableModalController(getWindowControl(), "close", transactionDetailsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
