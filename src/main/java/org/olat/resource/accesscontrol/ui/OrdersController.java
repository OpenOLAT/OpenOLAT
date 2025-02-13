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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.provider.invoice.InvoiceAccessHandler;
import org.olat.resource.accesscontrol.ui.OrdersDataModel.OrderCol;
import org.olat.resource.accesscontrol.ui.OrdersDataSource.ForgeDelegate;
import org.olat.user.UserAvatarMapper;
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
public class OrdersController extends FormBasicController implements Activateable2, ForgeDelegate, BreadcrumbPanelAware {

	private static final String CMD_TOOLS = "ordertools";

	private FlexiTableElement tableEl;
	private OrdersDataSource dataSource;
	private OrdersDataModel dataModel;
	private BreadcrumbPanel stackPanel;

	private final Identity identity;
	private final OLATResource resource;
	private final OrdersSettings settings;
	private final UserAvatarMapper avatarMapper = new UserAvatarMapper(true);
	private final String avatarMapperBaseURL;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	//TODO uh container
	private OrderDetailController detailController;
	private CloseableCalloutWindowController calloutCtrl;

	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AccessControlModule acModule;

	public OrdersController(UserRequest ureq, WindowControl wControl, Identity identity, OrdersSettings settings) {
		super(ureq, wControl, "orders_identity");
		this.identity = identity;
		this.resource = null;
		this.settings = settings;
		avatarMapperBaseURL = registerCacheableMapper(ureq, "users-avatars", avatarMapper);
		initForm(ureq);
	}
	
	public OrdersController(UserRequest ureq, WindowControl wControl, Identity identity, OLATResource resource,
			OrdersSettings settings, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "orders_identity", rootForm);
		this.identity = identity;
		this.resource = resource;
		this.settings = settings;
		avatarMapperBaseURL = registerCacheableMapper(ureq, "users-avatars", avatarMapper);
		initForm(ureq);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(settings.withActivities()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.activity,
					new OrderModificationCellRenderer(getTranslator())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.orderNr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.status,
				new OrderStatusRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.methods,
				new AccessMethodRenderer(acModule)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.offerLabel));
		if(resource == null && settings.withResourceDisplayName()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.summary));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.total));
		
		if (acService.isMethodAvailable(InvoiceAccessHandler.METHOD_TYPE)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderCol.cancellationFee));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrderCol.costCenterName));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrderCol.costCenterAccount));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrderCol.purchaseOrderNumber));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrderCol.comment));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrderCol.billingAddressIdentifier));
		}
		
		if(settings.withTools()) {
			ActionsColumnModel toolsColumn = new ActionsColumnModel(OrderCol.tools);
			columnsModel.addFlexiColumnModel(toolsColumn);
		}

		dataSource = new OrdersDataSource(acService, resource, identity, List.of(), null, this);
		dataModel = new OrdersDataModel(dataSource, getLocale(), userManager, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "orderList", dataModel, 25, true, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyTableMessageKey("table.order.empty");
	}
	
	@Override
	public void forge(OrderTableRow row) {
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator(), CMD_TOOLS);
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	public void setModifications(List<OrderModification> orderModifications) {
		dataSource.setModifications(orderModifications);
		dataModel.updateModifications();
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
		if(source instanceof FormLink link && CMD_TOOLS.equals(link.getCmd())
				&& link.getUserObject() instanceof OrderTableRow row) {
			doOpenTools(ureq, row, link);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();	
		} else if (detailController == source) {
			cleanUp();
			cmc.deactivate();
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

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
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

	private void doOpenDetails(UserRequest ureq, OrderTableRow row) {
		removeAsListenerAndDispose(detailController);

		OrderTableItem order = row.getItem();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Order.class, order.getOrderKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		detailController = new OrderDetailController(ureq, bwControl, order,
				avatarMapper, avatarMapperBaseURL, true);
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
	
	private void doSetPaied(OrderTableRow row) {
		Order order = acService.loadOrderByKey(row.getOrderKey());
		if(order != null) {
			acService.changeOrderStatus(order, OrderStatus.PAYED);
		}

		tableEl.deselectAll();
		tableEl.reloadData();
		
		showInfo("info.order.set.as.paid");
	}
	
	private class ToolsController extends BasicController {

		private Link payLink;
		private Link detailsLink;
		private final VelocityContainer mainVC;
		
		private final OrderTableRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, OrderTableRow row) {
			super(ureq, wControl);
			this.row = row;
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>();
			detailsLink = addLink("details", "details", "o_icon o_icon-fw o_icon_circle_info", links);
			
			if(row.getOrderStatus() == OrderStatus.NEW || row.getOrderStatus() == OrderStatus.PREPAYMENT) {
				payLink = addLink("set.paid", "set.paid", "o_icon o_icon-fw o_icon_pay", links);
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
			if(detailsLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doOpenDetails(ureq, row);
			} else if(payLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doSetPaied(row);
			}
		}
	}
}