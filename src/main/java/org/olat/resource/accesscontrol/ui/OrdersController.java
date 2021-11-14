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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ui.OrdersDataModel.OrderCol;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Description:<br>
 * List the orders
 *
 * <P>
 * Initial Date:  20 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrdersController extends FormBasicController implements Activateable2, BreadcrumbPanelAware {

	private static final String CMD_SELECT = "sel";


	private FlexiTableElement tableEl;
	private OrdersDataSource dataSource;
	private OrdersDataModel dataModel;
	private BreadcrumbPanel stackPanel;
	private CloseableModalController cmc;

	private Identity identity;

	private OrderDetailController detailController;

	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AccessControlModule acModule;

	/**
	 * Constructor that shows table with title and description for identity of user
	 * session.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public OrdersController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "orders");
		this.identity = getIdentity();
		initForm(ureq);
	}

	/**
	 * Constructor that shows table without title and description for the given
	 * identity, e.g. for an admin interface. 
	 * 
	 * @param ureq
	 * @param wControl
	 * @param identity
	 */
	public OrdersController(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.identity = identity;
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.methods, new AccessMethodRenderer(acModule)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.total));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.order.details", translate("select"), CMD_SELECT));

		dataSource = new OrdersDataSource(acService, null, identity, null);
		dataModel = new OrdersDataModel(dataSource, getLocale(), userManager, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "orderList", dataModel, 25, true, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyTableMessageKey("table.order.empty");

		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("order.status.payed"), OrderStatus.PAYED.name()));
		filters.add(new FlexiTableFilter(translate("order.status.error"), OrderStatus.ERROR.name()));
		tableEl.setFilters("", filters, false);

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", translate("orders.my"));
			layoutCont.contextPut("description", translate("orders.my.desc"));
		}
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(cmc);
		cmc = null;
        super.doDispose();
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


	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == detailController) {
			if(event == Event.BACK_EVENT) {
				removeAsListenerAndDispose(detailController);
				detailController = null;
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		/*if(entries == null || entries.isEmpty()) return;

		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if(Order.class.getSimpleName().equals(type)) {
			for(int i=tableCtr.getTableDataModel().getRowCount(); i-->0; ) {
				OrderTableItem order = (OrderTableItem)tableCtr.getTableDataModel().getObject(i);
				if(order.getOrderKey().equals(entry.getOLATResourceable().getResourceableId())) {
					selectOrder(ureq, order);
					break;
				}
			}
		}*/
	}

	private void doSelectOrder(UserRequest ureq, OrderTableItem order) {
		removeAsListenerAndDispose(detailController);

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Order.class, order.getOrderKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		detailController = new OrderDetailController(ureq, bwControl, order.getOrderKey());
		detailController.hideBackLink();
		listenTo(detailController);
		if (stackPanel != null) {
			stackPanel.pushController(order.getOrderNr(), detailController);
		} else {
			cmc = new CloseableModalController(getWindowControl(), translate("close"), detailController.getInitialComponent(),
					true, translate("order.booking"));
			cmc.activate();
			listenTo(cmc);
		}
	}
}