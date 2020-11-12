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
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ui.OrdersDataModel.OrderCol;
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
public class OrdersAdminController extends FormBasicController implements Activateable2, BreadcrumbPanelAware {
	
	protected static final int USER_PROPS_OFFSET = 500;
	protected static final String USER_PROPS_ID = OrdersAdminController.class.getCanonicalName();
	
	private static final String CMD_SELECT = "sel";

	private FlexiTableElement tableEl;
	private OrdersDataSource dataSource;
	private OrdersDataModel dataModel;
	
	private BreadcrumbPanel stackPanel;
	private OrdersSearchForm searchForm;
	private OrderDetailController detailController;

	private final OLATResource resource;

	private final List<UserPropertyHandler> userPropertyHandlers;

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
		this(ureq, wControl, null, null);
	}
	
	public OrdersAdminController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, OLATResource resource) {
		super(ureq, wControl, "order_list");
		this.resource = resource;
		this.stackPanel = stackPanel;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		initForm(ureq);
	}
	
	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.status, new OrderStatusRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.orderNr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.summary));

		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			columnsModel.addFlexiColumnModel(col);
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.methods, new AccessMethodRenderer(acModule)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.total));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.order.details", translate("select"), CMD_SELECT));
		
		dataSource = new OrdersDataSource(acService, resource, null, userPropertyHandlers);
		if(resource == null) {
			searchForm = new OrdersSearchForm(ureq, getWindowControl(), mainForm);
			listenTo(searchForm);
			formLayout.add("searchForm", searchForm.getInitialFormItem());
			
			Calendar cal = CalendarUtils.getStartOfDayCalendar(getLocale());
			cal.add(Calendar.MONTH, -1);
			searchForm.setFrom(cal.getTime());
			dataSource.setFrom(cal.getTime());
		}
		
		dataModel = new OrdersDataModel(dataSource, getLocale(), userManager, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "orderList", dataModel, 25, true, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);

		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("order.status.new"), OrderStatus.NEW.name()));
		filters.add(new FlexiTableFilter(translate("order.status.prepayment"), OrderStatus.PREPAYMENT.name()));
		filters.add(new FlexiTableFilter(translate("order.status.payed"), OrderStatus.PAYED.name()));
		filters.add(new FlexiTableFilter(translate("order.status.canceled"), OrderStatus.CANCELED.name()));
		filters.add(new FlexiTableFilter(translate("order.status.error"), OrderStatus.ERROR.name()));
		tableEl.setFilters("", filters, false);
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(OrderCol.creationDate.sortKey(), false));
		tableEl.setSortSettings(options);
		
		String id = resource == null ? "orders-admin-list-v2" : "orders-resource-list-v2";
		tableEl.setAndLoadPersistedPreferences(ureq, id);
	
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", translate("orders.admin.my"));
			layoutCont.contextPut("description", translate("orders.admin.my.desc"));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				OrderTableItem row = dataModel.getObject(se.getIndex());
				if(CMD_SELECT.equals(se.getCommand())) {
					doSelectOrder(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchForm) {
			if(event == Event.DONE_EVENT) {
				doSearch();
				addSearchToHistory(ureq);
			}
		} else if (source == detailController) {
			if(event == Event.BACK_EVENT) {
				if(stackPanel == null) {
					initialPanel.popContent();
				} else {
					stackPanel.popController(detailController);
				}
				removeAsListenerAndDispose(detailController);
				detailController = null;
				addSearchToHistory(ureq);
			}
		}
	}
	
	private void doSearch() {
		dataSource.setFrom(searchForm.getFrom());
		dataSource.setTo(searchForm.getTo());
		dataSource.setRefNo(searchForm.getRefNo());
		dataSource.reset();
		tableEl.reset(true, true, true);
	}
	
	protected void addSearchToHistory(UserRequest ureq) {
		StateEntry state = searchForm == null ? null : searchForm.getStateEntry(); 
		ContextEntry currentEntry = getWindowControl().getBusinessControl().getCurrentContextEntry();
		if(currentEntry != null) {
			currentEntry.setTransientState(state);
		}
		addToHistory(ureq, getWindowControl());
	}
	
	protected void doSelectOrder(UserRequest ureq, OrderTableItem order) {
		removeAsListenerAndDispose(detailController);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Order.class, order.getOrderKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		detailController = new OrderDetailController(ureq, bwControl, order.getOrderKey());
		listenTo(detailController);
		
		if(stackPanel == null) {
			initialPanel.pushContent(detailController.getInitialComponent());
		} else {
			detailController.hideBackLink();
			stackPanel.pushController(order.getOrderNr(), detailController);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
}