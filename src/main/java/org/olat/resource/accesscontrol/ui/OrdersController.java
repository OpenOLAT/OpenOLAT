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
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.StackedPanel;
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
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.model.AccessTransaction;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderStatus;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.ui.OrdersDataModel.Col;

/**
 * 
 * Description:<br>
 * List the orders
 * 
 * <P>
 * Initial Date:  20 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrdersController extends BasicController implements Activateable2 {
	
	private static final String CMD_SELECT = "sel";

	private final StackedPanel mainPanel;
	private final VelocityContainer mainVC;
	private final TableController tableCtr;
	private OrderDetailController detailController;
	
	private final AccessControlModule acModule;
	private final ACService acService;
	
	public OrdersController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
		acService = CoreSpringFactory.getImpl(ACService.class);

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "Orders");		
		tableConfig.setTableEmptyMessage(translate("table.order.empty"));
		
		List<ShortName> statusList = new ArrayList<ShortName>(); 
		OrderStatusContextShortName payedStatus = new OrderStatusContextShortName(translate("order.status.payed"), OrderStatus.PAYED);
		OrderStatusContextShortName errorStatus = new OrderStatusContextShortName(translate("order.status.error"), OrderStatus.ERROR);
		statusList.add(payedStatus);
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
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("order.part.payment", Col.methods.ordinal(), null, getLocale(), 
				ColumnDescriptor.ALIGNMENT_LEFT, new AccessMethodRenderer(acModule)));
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("order.total", Col.total.ordinal(), null, getLocale()));
		
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_SELECT, "table.order.details", getTranslator().translate("order.details")));
		listenTo(tableCtr);
		
		loadModel();
		
		mainVC = createVelocityContainer("orders");
		mainVC.put("orderList", tableCtr.getInitialComponent());
		mainVC.contextPut("title", translate("orders.my"));
		mainVC.contextPut("description", translate("orders.my.desc"));

		mainPanel = putInitialPanel(mainVC);
	}
	
	private void loadModel() {
		OrderStatusContextShortName filter = (OrderStatusContextShortName)tableCtr.getActiveFilter();
		List<Order> orders = acService.findOrders(getIdentity(), filter.getStatus());
		List<AccessTransaction> transactions = acService.findAccessTransactions(orders);
		List<PSPTransaction> pspTransactions = acService.findPSPTransactions(orders);
		List<OrderTableItem> items = OrdersDataModel.create(orders, transactions, pspTransactions);
		tableCtr.setTableDataModel(new OrdersDataModel(items, getLocale()));
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
			}
		} else if (source == detailController) {
			if(event == Event.BACK_EVENT) {
				mainPanel.setContent(mainVC);
				removeAsListenerAndDispose(detailController);
				detailController = null;
			}
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if(Order.class.getSimpleName().equals(type)) {
			for(int i=tableCtr.getTableDataModel().getRowCount(); i-->0; ) {
				OrderTableItem order = (OrderTableItem)tableCtr.getTableDataModel().getObject(i);
				if(order.getOrder().getKey().equals(entry.getOLATResourceable().getResourceableId())) {
					selectOrder(ureq, order);
					break;
				}
			}
		}
	}

	protected void selectOrder(UserRequest ureq, OrderTableItem order) {
		removeAsListenerAndDispose(detailController);

		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Order.class, order.getOrder().getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		detailController = new OrderDetailController(ureq, bwControl, order.getOrder(), order.getTransactions());
		listenTo(detailController);
		mainPanel.setContent(detailController.getInitialComponent());
	}
}