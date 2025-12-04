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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.components.widget.TextWidget;
import org.olat.core.gui.components.widget.WidgetFactory;
import org.olat.core.gui.components.widget.WidgetGroup;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.creditpoint.CreditPointFormat;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.model.CreditPointTransactionWithInfos;
import org.olat.modules.creditpoint.ui.CreditPointTransactionRow.CreditPointSource;
import org.olat.modules.creditpoint.ui.CreditPointUserTransactionsTableModel.TransactionCols;
import org.olat.modules.creditpoint.ui.component.CreditCellRenderer;
import org.olat.modules.creditpoint.ui.component.CreditPointSourceCellRenderer;
import org.olat.modules.creditpoint.ui.component.DebitCellRenderer;
import org.olat.modules.creditpoint.ui.component.TransactionTypeCellRenderer;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointUserTransactionsController extends FormBasicController {

	private static final String CMD_NOTE = "note";
	private static final String ALL_TAB_ID = "All";
	private static final String RELEVANT_TAB_ID = "Relevant";
	private static final String EXPIRING_TAB_ID = "Expiring";
	
	protected static final String FILTER_TRANSACTION_TYPE = "trx-type";
	protected static final String FILTER_ORDER_NUMBER = "order-num";

	private FlexiFiltersTab relevantTab;
	
	private WidgetGroup widgetGroup;
	private TextWidget balanceWidget;
	private TextWidget nextExpiredWidget;
	private FormItem widgetGroupItem;
	private FormLink addTransactionButton;
	private FormLink removeTransactionButton;
	private FlexiTableElement tableEl;
	private CreditPointUserTransactionsTableModel tableModel;
	
	private int counter = 0;
	private CreditPointWallet wallet;
	private final CreditPointSystem system;
	private final CreditPointSecurityCallback secCallback;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private NoteCalloutController noteCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private AddTransactionController addTransactionCtrl;
	private RemoveTransactionController removeTransactionCtrl;
	private CreditPointTransactionsDetailsController transactionDetailsCtrl;
	private ConfirmCancelTransactionController confirmCancelTransactionCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CreditPointService creditPointService;
	
	public CreditPointUserTransactionsController(UserRequest ureq, WindowControl wControl,
			CreditPointWallet wallet, CreditPointSecurityCallback secCallback) {
		super(ureq, wControl, "user_creditpoints");
		this.wallet = wallet;
		this.system = wallet.getCreditPointSystem();
		this.secCallback = secCallback;
		initForm(ureq);
		loadModel();
	}
	
	public CreditPointWallet getWallet() {
		return wallet;
	}
	
	public CreditPointSystem getCreditPointSystem() {
		return system;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initBalanceForm(formLayout);
		initTableForm(formLayout);
		
		updateWalletUI(ureq);
		tableEl.setSelectedFilterTab(ureq, relevantTab);
	}
	
	protected void initBalanceForm(FormItemContainer formLayout) {
		if(secCallback.canAddTransaction()) {
			addTransactionButton = uifactory.addFormLink("add.transaction", formLayout, Link.BUTTON);
			addTransactionButton.setIconLeftCSS("o_icon o_icon_add");
		}
		if(secCallback.canRemoveTransaction()) {
			removeTransactionButton = uifactory.addFormLink("remove.transaction", formLayout, Link.BUTTON);
			removeTransactionButton.setIconLeftCSS("o_icon o_icon_invalidate");
		}
		
		if(formLayout instanceof FormLayoutContainer formCont) {
			formCont.contextPut("systemUnit", StringHelper.escapeHtml(system.getName()));
		}

		widgetGroup = WidgetFactory.createWidgetGroup("minidashboard", null);
		
		balanceWidget = WidgetFactory.createTextWidget("balance", null, translate("credit.point.balance"), "o_icon_coins");
		widgetGroup.add(balanceWidget);
		nextExpiredWidget = WidgetFactory.createTextWidget("balance", null, translate("credit.point.next.expired"), "o_icon_timelimit_half");
		widgetGroup.add(nextExpiredWidget);

		widgetGroupItem = new ComponentWrapperElement(widgetGroup);
		formLayout.add("widgets", widgetGroupItem);
	}
	
	protected void initTableForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TransactionCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionCols.type,
				new TransactionTypeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionCols.nr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionCols.creationDate,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionCols.credit,
				new CreditCellRenderer(system)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionCols.debit,
				new DebitCellRenderer(system)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionCols.expirationDate,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TransactionCols.source,
				new CreditPointSourceCellRenderer()));
		
		DefaultFlexiColumnModel commentCol = new DefaultFlexiColumnModel(TransactionCols.note);
		commentCol.setIconHeader("o_icon o_icon_notes");
		columnsModel.addFlexiColumnModel(commentCol);

        ActionsColumnModel actionsCol = new ActionsColumnModel(TransactionCols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		tableModel = new CreditPointUserTransactionsTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		
		initFilters();
		initFiltersPresets();
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		filters.add(new FlexiTableTextFilter(translate("filter.order.number"), FILTER_ORDER_NUMBER, true));
		
		SelectionValues typeValues = new SelectionValues();
		typeValues.add(SelectionValues.entry(CreditPointTransactionType.deposit.name(), translate("transaction.type.".concat(CreditPointTransactionType.deposit.name()))));
		typeValues.add(SelectionValues.entry(CreditPointTransactionType.withdrawal.name(), translate("transaction.type.".concat(CreditPointTransactionType.withdrawal.name()))));
		typeValues.add(SelectionValues.entry(CreditPointTransactionType.expiration.name(), translate("transaction.type.".concat(CreditPointTransactionType.expiration.name()))));
		typeValues.add(SelectionValues.entry(CreditPointTransactionType.removal.name(), translate("transaction.type.".concat(CreditPointTransactionType.removal.name()))));
		typeValues.add(SelectionValues.entry(CreditPointTransactionType.reversal.name(), translate("transaction.type.".concat(CreditPointTransactionType.reversal.name()))));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.transaction.type"),
				FILTER_TRANSACTION_TYPE, typeValues, true));

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB_ID, translate("filter.relevant"),
				TabSelectionBehavior.nothing, List.of());
		relevantTab.setFiltersExpanded(true);
		tabs.add(relevantTab);
		
		FlexiFiltersTab expiringTab = FlexiFiltersTabFactory.tabWithImplicitFilters(EXPIRING_TAB_ID, translate("filter.expiring"),
				TabSelectionBehavior.nothing, List.of());
		expiringTab.setFiltersExpanded(true);
		tabs.add(expiringTab);

		tableEl.setFilterTabs(true, tabs);
	}
	
	private void loadModel() {
		List<CreditPointTransactionWithInfos> transactions = creditPointService.getCreditPointTransactions(wallet);
		List<CreditPointTransactionRow> rows = new ArrayList<>(transactions.size());
		for(CreditPointTransactionWithInfos transaction:transactions) {
			rows.add(forgeRow(transaction));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private CreditPointTransactionRow forgeRow(CreditPointTransactionWithInfos transactionWithInfos) {
		CreditPointSource source = null;
		CreditPointTransaction transaction = transactionWithInfos.transaction();
		
		Identity creator = transactionWithInfos.creator();
		if(transactionWithInfos.origin() != null) {
			String displayName = transactionWithInfos.origin().getDisplayname();
			source = new CreditPointSource(displayName, "o_icon o_CourseModule_icon");
		} else if(creator != null) {
			String fullName = userManager.getUserDisplayName(creator);
			source = new CreditPointSource(fullName, "o_icon o_icon_user");
		}

		CreditPointTransactionRow row = new CreditPointTransactionRow(transaction, source);
		
		if(StringHelper.containsNonWhitespace(transaction.getNote())) {
			FormLink noteLink = uifactory.addFormLink("note_" + (++counter), "note", "", null, flc, Link.LINK | Link.NONTRANSLATED);
			noteLink.setDomReplacementWrapperRequired(false);
			noteLink.setIconLeftCSS("o_icon o_icon_notes");
			noteLink.setTitle(translate("note"));
			noteLink.setUserObject(row);
			row.setNoteLink(noteLink);
		}
		
		return row;
	}

	private void updateWalletUI(UserRequest ureq) {
		String formattedBalance = CreditPointFormat.format(wallet.getBalance(), system);
		balanceWidget.setValue(StringHelper.escapeHtml(formattedBalance));
		
		CreditPointTransaction nextExpiringTransaction = creditPointService.nextExpiringCreditPointTransactions(wallet, ureq.getRequestTimestamp());
		if(nextExpiringTransaction == null) {
			String nextExpiringValue = CreditPointFormat.format(BigDecimal.ZERO , system);
			nextExpiredWidget.setValue(StringHelper.escapeHtml(nextExpiringValue));
			nextExpiredWidget.setAdditionalText("");
		} else {
			String nextExpiringValue = CreditPointFormat.format(nextExpiringTransaction.getAmount() , system);
			nextExpiredWidget.setValue(StringHelper.escapeHtml(nextExpiringValue));
			Date expirationDate = nextExpiringTransaction.getExpirationDate();
			long days = DateUtils.countDays(ureq.getRequestTimestamp(), expirationDate);
			String[] args = new String[] {
				Long.toString(days),
				Formatter.getInstance(getLocale()).formatDate(expirationDate)
			};
			
			String msg;
			if(days == 1) {
				msg = translate("warning.expire.one.day", args);
			} else if(days > 1) {
				msg = translate("warning.expire.days", args);
			} else {
				msg = translate("warning.expire.today", args);
			}
			nextExpiredWidget.setAdditionalText(msg);
		}	
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addTransactionCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				wallet = addTransactionCtrl.getWallet();
				loadModel();
				updateWalletUI(ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(removeTransactionCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				wallet = removeTransactionCtrl.getWallet();
				loadModel();
				updateWalletUI(ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmCancelTransactionCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				wallet = confirmCancelTransactionCtrl.getWallet();
				loadModel();
				updateWalletUI(ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(transactionDetailsCtrl == source) {
			if(event == Event.CLOSE_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		} else if(toolsCtrl == source) {
			if(event == Event.CLOSE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmCancelTransactionCtrl);
		removeAsListenerAndDispose(transactionDetailsCtrl);
		removeAsListenerAndDispose(removeTransactionCtrl);
		removeAsListenerAndDispose(addTransactionCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmCancelTransactionCtrl = null;
		transactionDetailsCtrl = null;
		removeTransactionCtrl = null;
		addTransactionCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addTransactionButton == source) {
			doAddTransaction(ureq);
		} else if(removeTransactionButton == source) {
			doRemoveTransaction(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if(ActionsCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					String targetId = ActionsCellRenderer.getId(se.getIndex());
					CreditPointTransactionRow selectedRow = tableModel.getObject(se.getIndex());
					doOpenTools(ureq, selectedRow, targetId);
				}
			} else if(event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(true, true, true);
			}
		} else if(source instanceof FormLink link) {
			if(CMD_NOTE.equals(link.getCmd()) && link.getUserObject() instanceof CreditPointTransactionRow row) {
				doOpenNote(ureq, link, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doRemoveTransaction(UserRequest ureq) {
		removeTransactionCtrl = new RemoveTransactionController(ureq, getWindowControl(), system, wallet);
		listenTo(removeTransactionCtrl);
		
		String title = translate("remove.transaction");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), removeTransactionCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddTransaction(UserRequest ureq) {
		addTransactionCtrl = new AddTransactionController(ureq, getWindowControl(), system, wallet);
		listenTo(addTransactionCtrl);
		
		String title = translate("add.transaction");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addTransactionCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCancelTransaction(UserRequest ureq, CreditPointTransactionRow row) {
		CreditPointTransaction transaction = row.getTransaction();
		String[] args = new String[] {
				transaction.getOrderNumber().toString(),
			CreditPointFormat.format(transaction.getAmount(), system)
		};
		confirmCancelTransactionCtrl = new ConfirmCancelTransactionController(ureq, getWindowControl(), 
				transaction, wallet, system);
		listenTo(confirmCancelTransactionCtrl);
		
		String title = translate("confirm.cancel.transaction.title", args);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmCancelTransactionCtrl.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenDetails(UserRequest ureq, CreditPointTransactionRow row) {
		transactionDetailsCtrl = new CreditPointTransactionsDetailsController(ureq, getWindowControl(), row.getTransaction(), system);
		listenTo(transactionDetailsCtrl);
		
		String title = translate("transaction.details.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), transactionDetailsCtrl.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenTools(UserRequest ureq, CreditPointTransactionRow row, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doOpenNote(UserRequest ureq, FormLink link, CreditPointTransactionRow row) {
		StringBuilder sb = Formatter.stripTabsAndReturns(row.getNote());
		String note = sb == null ? "" : sb.toString();
		noteCtrl = new NoteCalloutController(ureq, getWindowControl(), note);
		listenTo(noteCtrl);
		
		String title = translate("note");
		CalloutSettings settings = new CalloutSettings(title);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				noteCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "", settings);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private CreditPointTransactionRow row;

		private Link cancelTransactionLink;
		private Link transactionDetailsLink;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CreditPointTransactionRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("user_creditpoints_tool");
			transactionDetailsLink = LinkFactory.createLink("details.transaction", "details.transaction", getTranslator(), mainVC, this, Link.LINK);
			transactionDetailsLink.setIconLeftCSS("o_icon o_icon_details");
			
			CreditPointTransactionType type = row.getTransaction().getTransactionType();
			if(secCallback.canCancelTransaction() && (type == CreditPointTransactionType.deposit
					|| type == CreditPointTransactionType.removal || type == CreditPointTransactionType.withdrawal )) {
				cancelTransactionLink = LinkFactory.createLink("cancel.transaction", "cancel.transaction", getTranslator(), mainVC, this, Link.LINK);
				cancelTransactionLink.setIconLeftCSS("o_icon o_icon_revoke");
			}
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(cancelTransactionLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doCancelTransaction(ureq, row);
			} else if(transactionDetailsLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doOpenDetails(ureq, row);
			}
		}
	}
	
	private static class NoteCalloutController extends FormBasicController {

		private final String note;
		
		public NoteCalloutController(UserRequest ureq, WindowControl wControl, String note) {
			super(ureq, wControl, "note");
			this.note = note;
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			if(formLayout instanceof FormLayoutContainer layoutCont) {
				layoutCont.contextPut("note", StringHelper.xssScan(note));
			}
		}

		@Override
		protected void formCancelled(UserRequest ureq) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

}
