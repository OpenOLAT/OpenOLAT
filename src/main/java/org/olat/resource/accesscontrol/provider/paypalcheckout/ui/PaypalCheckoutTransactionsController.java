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

import org.olat.basesecurity.BaseSecurityModule;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutManager;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutStatus;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.PaypalCheckoutTransactionWithDelivery;
import org.olat.resource.accesscontrol.provider.paypalcheckout.ui.PaypalCheckoutTransactionDataModel.CheckoutCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutTransactionsController extends FormBasicController implements Activateable2 {

	protected static final int USER_PROPS_OFFSET = 500;
	protected static final String USER_PROPS_ID = PaypalCheckoutTransactionsController.class.getCanonicalName();
	
	private FlexiTableElement tableEl;
	private PaypalCheckoutTransactionDataModel dataModel;

	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private CloseableModalController cmc;
	private PaypalCheckoutTransactionDetailsController transactionDetailsCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private PaypalCheckoutManager paypalManager;
	
	public PaypalCheckoutTransactionsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "transactions");
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
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
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			columnsModel.addFlexiColumnModel(col);
		}
		
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
	
	private void loadModel() {
		String searchString = tableEl.getQuickSearchString();
		List<PaypalCheckoutTransactionWithDelivery> transactionsWithDeliveryList = paypalManager.searchTransactions(searchString);
		List<PaypalCheckoutTransactionRow> rows = new ArrayList<>(transactionsWithDeliveryList.size());
		for(PaypalCheckoutTransactionWithDelivery transactionWithDelivery:transactionsWithDeliveryList) {
			Identity identity = transactionWithDelivery.delivery();
			PaypalCheckoutTransaction transaction = transactionWithDelivery.transaction();
			rows.add(new PaypalCheckoutTransactionRow(transaction, identity, userPropertyHandlers, getLocale()));
		}
		
		dataModel.setObjects(rows);
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
				PaypalCheckoutTransactionRow row = dataModel.getObject(se.getIndex());
				if("select".equals(se.getCommand())) {
					doSelectTransaction(ureq, row.getTransaction());
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelectTransaction(UserRequest ureq, PaypalCheckoutTransaction trx) {
		transactionDetailsCtrl = new PaypalCheckoutTransactionDetailsController(ureq, getWindowControl(), trx);
		listenTo(transactionDetailsCtrl);
		
		String title = translate("paypal.transaction.title", new String[] { trx.getPaypalOrderId() });
		cmc = new CloseableModalController(getWindowControl(), translate("close"), transactionDetailsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
