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
import java.util.Date;
import java.util.List;

import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ui.OrdersDataModel.Col;
import org.olat.user.UserManager;
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
public class OrdersAdminController extends BasicController implements Activateable2  {
	
	private static final String CMD_SELECT = "sel";

	private final StackedPanel mainPanel;
	private final VelocityContainer mainVC;
	private final TableController tableCtr;
	private OrdersSearchForm searchForm;
	private final TooledStackedPanel stackPanel;
	private OrderDetailController detailController;

	private final OLATResource resource;
	
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	
	/**
	 * Constructor for the admin. extension
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public OrdersAdminController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null, null);
	}
	
	public OrdersAdminController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, OLATResource resource) {
		super(ureq, wControl);
		this.resource = resource;
		this.stackPanel = stackPanel;
		
		if(resource == null) {
			searchForm = new OrdersSearchForm(ureq, wControl);
			listenTo(searchForm);
		}

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(true);
		//tableConfig.setPreferencesOffered(true, "Orders2");		
		//tableConfig.setTableEmptyMessage(translate("table.order.empty"));
		
		List<ShortName> statusList = new ArrayList<ShortName>();
		OrderStatusContextShortName allStatus = new OrderStatusContextShortName("-", OrderStatus.values());
		OrderStatusContextShortName newStatus = new OrderStatusContextShortName(translate("order.status.new"), OrderStatus.NEW);
		OrderStatusContextShortName preStatus = new OrderStatusContextShortName(translate("order.status.prepayment"), OrderStatus.PREPAYMENT);
		OrderStatusContextShortName payedStatus = new OrderStatusContextShortName(translate("order.status.payed"), OrderStatus.PAYED);
		OrderStatusContextShortName cancelStatus = new OrderStatusContextShortName(translate("order.status.canceled"), OrderStatus.CANCELED);
		OrderStatusContextShortName errorStatus = new OrderStatusContextShortName(translate("order.status.error"), OrderStatus.ERROR);
		statusList.add(allStatus);
		statusList.add(newStatus);
		statusList.add(preStatus);
		statusList.add(payedStatus);
		statusList.add(cancelStatus);
		statusList.add(errorStatus);

		tableCtr = new TableController(tableConfig, ureq, wControl, statusList, payedStatus, translate("order.status"), null, false, getTranslator());
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("order.status", Col.status.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new OrderStatusRenderer()) {
					@Override
					public int compareTo(int rowa, int rowb) {
						OrderTableItem a = (OrderTableItem)table.getTableDataModel().getValueAt(rowa, dataColumn);
						OrderTableItem b = (OrderTableItem)table.getTableDataModel().getValueAt(rowb, dataColumn);
						return a.compareStatusTo(b);
					}
		});
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("order.nr", Col.orderNr.ordinal(), CMD_SELECT, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("order.creationDate", Col.creationDate.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("order.summary", Col.summary.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("order.delivery", Col.delivery.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("order.part.payment", Col.methods.ordinal(), null, getLocale(), 
				ColumnDescriptor.ALIGNMENT_LEFT, new AccessMethodRenderer(acModule)));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("order.total", Col.total.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_SELECT, "table.order.details", getTranslator().translate("select")));
		
		listenTo(tableCtr);
		
		loadModel();
		
		mainVC = createVelocityContainer("orders");
		if(searchForm != null) {
			mainVC.put("searchForm", searchForm.getInitialComponent());
		}
		mainVC.put("orderList", tableCtr.getInitialComponent());
		mainVC.contextPut("title", translate("orders.admin.my"));
		mainVC.contextPut("description", translate("orders.admin.my.desc"));

		mainPanel = putInitialPanel(mainVC);
	}
	
	private void loadModel() {
		OrderStatusContextShortName filter = (OrderStatusContextShortName)tableCtr.getActiveFilter();
		Date from = null;
		Date to = null;
		Long orderNr = null;
		if(searchForm != null) {
			from = searchForm.getFrom();
			to = searchForm.getTo();
			orderNr = searchForm.getRefNo();
		}
		List<OrderTableItem> items = acService.findOrderItems(resource, null, orderNr, from, to, filter.getStatus(), 0, -1);
		tableCtr.setTableDataModel(new OrdersDataModel(items, getLocale(), userManager));
	}
	
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				OrderTableItem order = (OrderTableItem)tableCtr.getTableDataModel().getObject(rowid);
				if(CMD_SELECT.equals(actionid)) {
					selectOrder(ureq, order);
				}
			} else if (TableController.EVENT_FILTER_SELECTED == event) {
				loadModel();
				addToHistory(ureq, getWindowControl());
			}
		} else if (source == searchForm) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				addSearchToHistory(ureq);
			}
		} else if (source == detailController) {
			if(event == Event.BACK_EVENT) {
				mainPanel.setContent(mainVC);
				removeAsListenerAndDispose(detailController);
				detailController = null;
				addSearchToHistory(ureq);
			}
		}
	}
	
	protected void addSearchToHistory(UserRequest ureq) {
		StateEntry state = searchForm == null ? null : searchForm.getStateEntry(); 
		ContextEntry currentEntry = getWindowControl().getBusinessControl().getCurrentContextEntry();
		if(currentEntry != null) {
			currentEntry.setTransientState(state);
		}
		addToHistory(ureq, getWindowControl());
	}
	
	protected void selectOrder(UserRequest ureq, OrderTableItem order) {
		removeAsListenerAndDispose(detailController);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Order.class, order.getOrderKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		detailController = new OrderDetailController(ureq, bwControl, order.getOrderKey());
		listenTo(detailController);
		
		if(stackPanel == null) {
			mainPanel.setContent(detailController.getInitialComponent());
		} else {
			detailController.hideBackLink();
			stackPanel.pushController(order.getOrderNr(), detailController);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state != null) {
			if(searchForm.setStateEntry(state)) {
				loadModel();
			}
		}

		if(entries == null || entries.isEmpty()) return;
		
		Long orderKey = entries.get(0).getOLATResourceable().getResourceableId();
		OrdersDataModel model = (OrdersDataModel)tableCtr.getTableDataModel();
		OrderTableItem order = model.getItem(orderKey);
		if(order != null) {
			selectOrder(ureq, order);
		}
	}
}