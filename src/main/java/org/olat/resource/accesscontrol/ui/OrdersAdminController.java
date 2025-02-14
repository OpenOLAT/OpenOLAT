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
package org.olat.resource.accesscontrol.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.reports.AccountingReportResource;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.ACResourceInfo;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.provider.invoice.InvoiceAccessHandler;
import org.olat.resource.accesscontrol.ui.OrdersDataModel.OrderCol;
import org.olat.resource.accesscontrol.ui.OrdersDataSource.ForgeDelegate;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Controller to manage the orders, limited or not the an OLAT resource.
 * 
 * <P>
 * Initial Date:  30 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrdersAdminController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate, ForgeDelegate {

	private static final String CMD_TOOLS = "odtools";
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	protected static final int USER_PROPS_OFFSET = 500;
	protected static final String USER_PROPS_ID = OrdersAdminController.class.getCanonicalName();
	
	private FlexiFiltersTab allTab;
	
	private FormLink exportButton;
	private FormLink bulkPayButton;
	private FormLink bulkCancelButton;
	private FlexiTableElement tableEl;
	private OrdersDataSource dataSource;
	private OrdersDataModel dataModel;
	private VelocityContainer detailsVC;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private OrderDetailController detailController;
	private CloseableCalloutWindowController calloutCtrl;

	private boolean readOnly;
	private final OLATResource resource;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final UserAvatarMapper avatarMapper = new UserAvatarMapper(true);
	private final String avatarMapperBaseURL;
	private final List<AccessMethod> methods;

	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private BaseSecurityModule securityModule;
	
	/**
	 * Constructor for the admin. extension
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public OrdersAdminController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null, true);
	}
	
	public OrdersAdminController(UserRequest ureq, WindowControl wControl, OLATResource resource, boolean readOnly) {
		super(ureq, wControl, "order_list");
		this.resource = resource;
		this.readOnly = readOnly;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		avatarMapperBaseURL = registerCacheableMapper(ureq, "users-avatars", avatarMapper);
		methods = acService.getAvailableMethods();
		
		detailsVC = createVelocityContainer("order_details");
		
		initForm(ureq);
		
		tableEl.setSelectedFilterTab(ureq, allTab);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("title", translate("orders.admin.my"));
		}
		
		exportButton = uifactory.addFormLink("export.booking.offers", "export.booking.offers", null, formLayout, Link.BUTTON);
		exportButton.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		exportButton.setVisible(resource != null
				&& "CurriculumElement".equals(resource.getResourceableTypeName()));
		
		initTableForm(formLayout, ureq);
	}

	private void initTableForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.orderNr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.status,
				new OrderStatusRenderer(getTranslator())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.methods,
				new AccessMethodRenderer(acModule)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.offerLabel));

		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			columnsModel.addFlexiColumnModel(col);
		}
		
		if(resource == null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.summary));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.orderAmount));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.offersTotalAmount));
		if (acService.isMethodAvailable(InvoiceAccessHandler.METHOD_TYPE)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.orderCancellationFee));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.offersCancellationFees));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrderCol.costCenterName));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrderCol.costCenterAccount));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrderCol.purchaseOrderNumber));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrderCol.comment));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrderCol.billingAddressIdentifier));
		}
		
		if(!readOnly) {
			ActionsColumnModel toolsColumn = new ActionsColumnModel(OrderCol.tools);
			columnsModel.addFlexiColumnModel(toolsColumn);
		}

		dataSource = new OrdersDataSource(acService, resource, null, methods, userPropertyHandlers, this);
		if(resource == null) {
			Calendar cal = CalendarUtils.getStartOfDayCalendar(getLocale());
			cal.add(Calendar.MONTH, -1);
			dataSource.setFrom(cal.getTime());
		}
		
		dataModel = new OrdersDataModel(dataSource, getLocale(), userManager, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "orderList", dataModel, 25, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_ac_order_details_container");
		tableEl.setExportEnabled(true);
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(OrderCol.creationDate.sortKey(), false));
		tableEl.setSortSettings(options);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setSearchEnabled(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		
		String id = resource == null ? "orders-admin-list-v3" : "orders-resource-list-v3";
		tableEl.setAndLoadPersistedPreferences(ureq, id);
		
		if(!readOnly) {
			bulkPayButton = uifactory.addFormLink("bulk.pay", "set.paid", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(bulkPayButton);
			bulkCancelButton = uifactory.addFormLink("bulk.cancel", "set.cancel", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(bulkCancelButton);
		}
		
		initFilters();
		initFilterPresets();
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues rolesValues = new SelectionValues();
		rolesValues.add(SelectionValues.entry(OrderStatus.NEW.name(), translate("order.status.new")));
		rolesValues.add(SelectionValues.entry(OrderStatus.PREPAYMENT.name(), translate("order.status.prepayment")));
		rolesValues.add(SelectionValues.entry(OrdersDataSource.PSEUDO_STATUS_DONE, translate("order.status.ok")));
		rolesValues.add(SelectionValues.entry(OrderStatus.PAYED.name(), translate("order.status.payed")));
		rolesValues.add(SelectionValues.entry(OrderStatus.CANCELED.name(), translate("order.status.canceled")));
		rolesValues.add(SelectionValues.entry(OrderStatus.ERROR.name(), translate("order.status.error")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				OrdersDataSource.FILTER_STATUS, rolesValues, true);
		filters.add(statusFilter);
		
		// Offer types / access methods
		SelectionValues methodsValues = new SelectionValues();
		for(AccessMethod method:methods) {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(method.getType());
			if(handler != null) {
				methodsValues.add(SelectionValues.entry(method.getKey().toString(), handler.getMethodName(getLocale())));
			}
		}
		FlexiTableMultiSelectionFilter methodFilter = new FlexiTableMultiSelectionFilter(translate("filter.method"),
				OrdersDataSource.FILTER_METHOD, methodsValues, true);
		filters.add(methodFilter);
		
		// Offers
		if(resource != null) {
			SelectionValues offersValues = new SelectionValues();
			List<Offer> offers = acService.findOfferByResource(resource, true, null, null);
			for(Offer offer:offers) {
				List<OfferAccess> offerAccess = acService.getOfferAccess(offer, true);
				for(OfferAccess access:offerAccess) {
					SelectionValue val = initOfferFilterValue(offer, access, access.getMethod());
					if(val != null) {
						offersValues.add(val);
					}
				}
			}
			FlexiTableMultiSelectionFilter offerFilter = new FlexiTableMultiSelectionFilter(translate("filter.offer"),
					OrdersDataSource.FILTER_OFFER, offersValues, true);
			filters.add(offerFilter);
		}

		tableEl.setFilters(true, filters, false, false);
	}
	
	private SelectionValue initOfferFilterValue(Offer offer, OfferAccess access, AccessMethod method) {
		if(method != null) {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(method.getType());
			if(handler != null) {
				StringBuilder val = new StringBuilder();
				if(StringHelper.containsNonWhitespace(offer.getLabel())) {
					val.append(offer.getLabel()).append(" \u00B7 ");
				}
				String methodName = handler.getMethodName(getLocale());
				if(StringHelper.containsNonWhitespace(methodName)) {
					val.append(methodName);
				} else {
					val.append(method.getType());
				}
				return SelectionValues.entry(access.getKey().toString(), val.toString());
			}
		}
		return null;
	}
	
	private void initFilterPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		FlexiFiltersTab openTab = FlexiFiltersTabFactory.tabWithImplicitFilters("open", translate("filter.order.open"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(OrdersDataSource.FILTER_STATUS,
						List.of(OrderStatus.NEW.name(), OrderStatus.PREPAYMENT.name()))));
		openTab.setFiltersExpanded(true);
		tabs.add(openTab);
		
		FlexiFiltersTab doneTab = FlexiFiltersTabFactory.tabWithImplicitFilters(OrdersDataSource.PSEUDO_STATUS_DONE, translate("filter.order.done"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(OrdersDataSource.FILTER_STATUS,
						List.of(OrdersDataSource.PSEUDO_STATUS_DONE))));
		doneTab.setFiltersExpanded(true);
		tabs.add(doneTab);
		
		FlexiFiltersTab payedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(OrderStatus.PAYED.name(), translate("filter.order.paid"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(OrdersDataSource.FILTER_STATUS,
						List.of(OrderStatus.PAYED.name()))));
		payedTab.setFiltersExpanded(true);
		tabs.add(payedTab);
		
		FlexiFiltersTab cancelledTab = FlexiFiltersTabFactory.tabWithImplicitFilters("cancelled", translate("filter.order.cancelled"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(OrdersDataSource.FILTER_STATUS,
						List.of(OrderStatus.CANCELED.name()))));
		cancelledTab.setFiltersExpanded(true);
		tabs.add(cancelledTab);
		
		FlexiFiltersTab errorTab = FlexiFiltersTabFactory.tabWithImplicitFilters("error", translate("filter.order.error"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(OrdersDataSource.FILTER_STATUS,
						List.of(OrderStatus.ERROR.name()))));
		errorTab.setFiltersExpanded(true);
		tabs.add(errorTab);
		
		tableEl.setFilterTabs(true, tabs);
	}

	@Override
	public void forge(OrderTableRow row) {
		OrderStatus status = row.getOrderStatus();
		if(status == OrderStatus.NEW || status == OrderStatus.PREPAYMENT || status == OrderStatus.PAYED) {
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator(), CMD_TOOLS);
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof OrderTableRow orderRow
				&& orderRow.getDetailsController() != null) {
			components.add(orderRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		FlexiFiltersTab tab = tableEl.getFilterTabById(type.toLowerCase());
		if(tab != null) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			activateFilters(subEntries);
			loadModel();
		}
	}
	
	private void activateFilters(List<ContextEntry> entries) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("OfferAccess".equalsIgnoreCase(type) || "Link".equalsIgnoreCase(type)) {
			Long offerAccessKey = entries.get(0).getOLATResourceable().getResourceableId();
			FlexiTableFilter offerFilter = FlexiTableFilter.getFilter(tableEl.getFilters(), OrdersDataSource.FILTER_OFFER);
			if (offerFilter instanceof FlexiTableMultiSelectionFilter extendedFilter) {
				tableEl.setFilterValue(extendedFilter, offerAccessKey.toString());
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(bulkPayButton == source) {
			doBulkSetPaid();
		} else if(bulkCancelButton == source) {
			doBulkCancel();
		} else if(exportButton == source) {
			doExport(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					OrderTableRow row = dataModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseOrderDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenOrderDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				OrderTableRow row = dataModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenOrderDetails(ureq, row);
				} else {
					doCloseOrderDetails(row);
				}
			} else if(event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}
		 } else if(source instanceof FormLink link && CMD_TOOLS.equals(link.getCmd())
				&& link.getUserObject() instanceof OrderTableRow row) {
			doOpenTools(ureq, row, link);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof OrderDetailController) {
			if(event == Event.CHANGED_EVENT) {
				loadModel();
			}
		} else if(toolsCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();	
		} else if(calloutCtrl == source || cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(detailController);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		detailController = null;
		calloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	private void loadModel() {
		dataSource.reset();
		tableEl.reset(true, true, true);
	}
	
	private void doOpenTools(UserRequest ureq, OrderTableRow member, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), member);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	private final void doOpenOrderDetails(UserRequest ureq, OrderTableRow row) {
		if(row == null) return;
		
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		OrderTableItem order = row.getItem();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Order.class, order.getOrderKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		detailController = new OrderDetailController(ureq, bwControl, order,
				avatarMapper, avatarMapperBaseURL, readOnly, mainForm);
		listenTo(detailController);
	
		row.setDetailsController(detailController);
		flc.add(detailController.getInitialFormItem());
	}
	
	private final void doCloseOrderDetails(OrderTableRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	private void doBulkSetPaid() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<OrderTableRow> selectedRows = dataModel.getObjects(selectedIndex);
		List<OrderTableRow> rows = selectedRows.stream()
				.filter(r -> OrderStatus.NEW == r.getOrderStatus() || OrderStatus.PREPAYMENT == r.getOrderStatus())
				.toList();
		
		if(rows.isEmpty()) {
			showWarning("warning.no.order.to.pay");
		} else {
			doSetPaid(rows);
		}
	}
	
	private void doSetPaid(List<OrderTableRow> rows) {
		for(OrderTableRow row:rows) {
			Order order = acService.loadOrderByKey(row.getOrderKey());
			if(order != null) {
				acService.changeOrderStatus(order, OrderStatus.PAYED);
			}
		}

		showInfo("info.order.set.as.paid");
		tableEl.deselectAll();
		tableEl.reloadData();
	}
	
	private void doBulkCancel() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<OrderTableRow> selectedRows = dataModel.getObjects(selectedIndex);
		List<OrderTableRow> rows = selectedRows.stream()
				.filter(r -> OrderStatus.NEW == r.getOrderStatus() || OrderStatus.PREPAYMENT == r.getOrderStatus() || OrderStatus.PAYED == r.getOrderStatus())
				.toList();
		
		if(rows.isEmpty()) {
			showWarning("warning.no.order.to.cancel");
		} else {
			doCancelOrder(rows);
		}
	}
	
	private void doCancelOrder(List<OrderTableRow> rows) {
		for(OrderTableRow row:rows) {
			Order order = acService.loadOrderByKey(row.getOrderKey());
			MailPackage mailing = new MailPackage(false);
			acService.cancelOrder(order, getIdentity(), null, mailing);
		}
		
		showInfo("info.order.set.as.cancelled");
		tableEl.deselectAll();
		tableEl.reloadData();
	}
	
	private void doExport(UserRequest ureq) {
		MediaResource reportResource;
		if(resource == null) {
			reportResource = new AccountingReportResource(getIdentity(), getLocale());
		} else {
			ACResourceInfo infos = acService.getResourceInfos(List.of(resource)).get(0);
			String filename = "Orders_" + StringHelper.transformDisplayNameToFileSystemName(infos.getName())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date());
			reportResource = new AccountingReportResource(filename, resource, getLocale());
		}
		ureq.getDispatchResult().setResultingMediaResource(reportResource);
	}

	private class ToolsController extends BasicController {

		private Link payLink;
		private Link cancelLink;
		private final VelocityContainer mainVC;
		
		private final OrderTableRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, OrderTableRow row) {
			super(ureq, wControl);
			this.row = row;
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>();
			if(row.getOrderStatus() == OrderStatus.NEW || row.getOrderStatus() == OrderStatus.PREPAYMENT) {
				payLink = addLink("set.paid", "set.paid", "o_icon o_icon-fw o_icon_pay", links);
			}
			
			if(row.getOrderStatus() == OrderStatus.NEW || row.getOrderStatus() == OrderStatus.PREPAYMENT
					|| row.getOrderStatus() == OrderStatus.PAYED) {
				cancelLink = addLink("set.cancel", "set.cancel", "o_icon o_icon-fw o_icon_decline", links);
			}
			
			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String cmd, String iconCSS, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			links.add(name);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(payLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doSetPaid(List.of(row));
			} else if(cancelLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doCancelOrder(List.of(row));
			}
		}
	}
}