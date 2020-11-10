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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessTransaction;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

public class OrderDetailController extends FormBasicController {
	
	private static final String CMD_SELECT = "sel";
	
	private FormLink backLink;
	private FormLink selectResourceLink;
	private TableController tableCtr;
	private CloseableModalController cmc;
	private TransactionDetailsController detailsCtlr;
	
	private final Order order;
	private Collection<AccessTransaction> transactions;
	
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private UserManager userManager;
	
	public OrderDetailController(UserRequest ureq, WindowControl wControl, Long orderKey) {
		super(ureq, wControl, "order");
		
		order = acService.loadOrderByKey(orderKey);
		transactions = acService.findAccessTransactions(order);
		
		initForm(ureq);
	}
	
	public void hideBackLink() {
		backLink.setVisible(false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		backLink = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);
		
		FormLayoutContainer mainLayout = FormLayoutContainer.createDefaultFormLayout("mainCmp", getTranslator());
		mainLayout.setRootForm(mainForm);
		formLayout.add("mainCmp", mainLayout);
		
		String orderNr = order.getOrderNr();
		uifactory.addStaticTextElement("order-nr", "order.nr", orderNr, mainLayout);	

		Date creationDate = order.getCreationDate();
		String creationDateStr = Formatter.getInstance(getLocale()).formatDateAndTime(creationDate);
		uifactory.addStaticTextElement("creation-date", "order.creationDate", creationDateStr, mainLayout);
		
		String orderTotal = PriceFormat.fullFormat(order.getTotal());
		String orderTotalStr;
		if(acModule.isVatEnabled()) {
			BigDecimal vat = acModule.getVat();
			String vatStr = vat == null ? "" : vat.setScale(3, RoundingMode.HALF_EVEN).toPlainString();
			orderTotalStr = translate("access.info.price.vat", new String[]{orderTotal, vatStr});
		} else {
			orderTotalStr = translate("access.info.price.noVat", new String[]{orderTotal});
		}
		uifactory.addStaticTextElement("order-total", "order.total", orderTotalStr, mainLayout);
		
		OrderItemsDataModel tableModel = getOrderItemsDataModel();
		
		if(tableModel.getRowCount() == 1) {
			OrderItemWrapper wrapper = tableModel.getObject(0);
			if(wrapper.getItem().getOffer().getResource() != null) {
				//resource is null if the resource has been deleted
				String linkName = StringHelper.escapeHtml(wrapper.getDisplayName());
				selectResourceLink = uifactory.addFormLink("resource", linkName, translate("order.item"), mainLayout, Link.NONTRANSLATED);
				selectResourceLink.setUserObject(wrapper);
				selectResourceLink.setCustomEnabledLinkCSS("form-control-static");
			}
		}

		User user = order.getDelivery().getUser();
		String delivery = StringHelper.escapeHtml(userManager.getUserDisplayName(user));
		uifactory.addStaticTextElement("delivery", "order.delivery", delivery, mainLayout);

		if(formLayout instanceof FormLayoutContainer) {
			TableGuiConfiguration tableConfig = new TableGuiConfiguration();
			tableConfig.setDownloadOffered(false);		
			tableConfig.setTableEmptyMessage(translate("orders.empty"));

			tableCtr = new TableController(tableConfig, ureq, getWindowControl(), Collections.<ShortName>emptyList(), null, null , null, false, getTranslator());
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("order.item.name", 0, null, getLocale()));
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("order.part.payment", 1, null, getLocale(), 
					ColumnDescriptor.ALIGNMENT_LEFT, new AccessMethodRenderer(acModule)));
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_SELECT, "table.order.details", getTranslator().translate("order.details")));
			
			tableCtr.setTableDataModel(tableModel);
			listenTo(tableCtr);
			
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			layoutContainer.put("orderItemList", tableCtr.getInitialComponent());
		}
	}
	
	private OrderItemsDataModel getOrderItemsDataModel() {
		List<OrderItemWrapper> items = new ArrayList<>();
		for(OrderPart part: order.getParts()) {
			boolean first = true;
			
			AccessTransaction transaction = null;
			if(transactions != null) {
				for(AccessTransaction trx:transactions) {
					if(trx.getOrderPart().equals(part)) {
						transaction = trx;
						break;
					}
				}
			}

			for(OrderLine line:part.getOrderLines()) {
				OLATResource resource = line.getOffer().getResource();
				String displayName;
				if(resource == null) {
					displayName = line.getOffer().getResourceDisplayName();
				} else {
					displayName = acService.resolveDisplayName(resource);
				}
				OrderItemWrapper wrapper = new OrderItemWrapper(part, line, transaction, displayName, first);
				items.add(wrapper);
				first = false;
			}
		}
		
		return new OrderItemsDataModel(items);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				OrderItemWrapper wrapper = (OrderItemWrapper)tableCtr.getTableDataModel().getObject(rowid);
				if(CMD_SELECT.equals(actionid)) {
					popupTransactionDetails(ureq, wrapper);
				}
			}
		} else if (source == detailsCtlr) {
			cmc.deactivate();
			removeAsListenerAndDispose(detailsCtlr);
			removeAsListenerAndDispose(cmc);
			detailsCtlr = null;
			cmc = null;
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if (source == selectResourceLink) {
			OrderItemWrapper wrapper = (OrderItemWrapper)source.getUserObject();
			final Long resourceId = wrapper.getItem().getOffer().getResourceId();
			final String type = wrapper.getItem().getOffer().getResourceTypeName();

			String url;
			if("BusinessGroup".equals(type)) {
				url = "[" + type + ":" + resourceId + "]";
			} else {
				OLATResourceable ores = wrapper.getOLATResourceable();
				RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
				url = "[RepositoryEntry:" + re.getKey() + "]";
			}
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(url);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void popupTransactionDetails(UserRequest ureq, OrderItemWrapper wrapper) {
		if(wrapper.getTransaction() == null) {
			//no transaction???
		} else {
			detailsCtlr = new TransactionDetailsController(ureq, getWindowControl(), order, wrapper);
			listenTo(detailsCtlr);
			String title = translate("transaction.details.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), detailsCtlr.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	public class OrderItemsDataModel implements TableDataModel<OrderItemWrapper> {
		
		private List<OrderItemWrapper> items;
		
		public OrderItemsDataModel(List<OrderItemWrapper> items) {
			this.items = items;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return items == null ? 0 : items.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			OrderItemWrapper wrapper = getObject(row);
			switch(col) {
				case 0: {
					String name = wrapper.getDisplayName();
					if(StringHelper.containsNonWhitespace(name)) {
						return name;
					}
					return "-";
				}
				case 1: {
					if(wrapper.isFirst() && wrapper.getTransaction() != null) {
						return wrapper.getTransaction();
					}
					return null;
				}
				default: return wrapper;
			}
		}

		@Override
		public OrderItemWrapper getObject(int row) {
			return items.get(row);
		}

		@Override
		public void setObjects(List<OrderItemWrapper> objects) {
			this.items = objects;
		}

		@Override
		public OrderItemsDataModel createCopyWithEmptyList() {
			return new OrderItemsDataModel(Collections.<OrderItemWrapper>emptyList());
		}
	}
	
	public class OrderItemWrapper {
		
		private final boolean first;
		private final OrderPart part;
		private final OrderLine item;
		private final AccessTransaction transaction;
		private final String displayName;
		
		public OrderItemWrapper(OrderPart part, OrderLine item, AccessTransaction transaction, String displayName,  boolean first) {
			this.part = part;
			this.item = item;
			this.first = first;
			this.transaction = transaction;
			this.displayName = displayName;
		}

		public boolean isFirst() {
			return first;
		}

		public String getDisplayName() {
			return displayName;
		}

		public OrderPart getPart() {
			return part;
		}

		public OrderLine getItem() {
			return item;
		}
		
		public AccessTransaction getTransaction() {
			return transaction;
		}
		
		public OLATResourceable getOLATResourceable() {
			return new OLATResourceable() {
				@Override
				public String getResourceableTypeName() {
					return item.getOffer().getResourceTypeName();
				}
				@Override
				public Long getResourceableId() {
					return item.getOffer().getResourceId();
				}
			};
		}
	}
}
