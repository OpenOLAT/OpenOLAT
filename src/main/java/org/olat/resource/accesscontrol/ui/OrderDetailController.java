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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessTransaction;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalAccessMethod;
import org.olat.resource.accesscontrol.provider.paypalcheckout.model.PaypalCheckoutAccessMethod;
import org.olat.resource.accesscontrol.ui.OrderItemsDataModel.OrderItemCol;
import org.olat.resource.accesscontrol.ui.OrderTableItem.Status;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoProfileController;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 27 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OrderDetailController extends FormBasicController {
	
	private static final String CMD_SELECT = "sel";
	
	private FormLink setPaidButton;
	private FormLink selectResourceLink;
	private BillingAddressItem billingAddressItem;
	
	private CloseableModalController cmc;
	private TransactionDetailsController detailsCtlr;
	
	private int counter = 0;
	private Order order;
	private final Identity delivery;
	private final boolean readOnly;
	private final String offerLabel;
	private final String costCenterName;
	private final String costCenterAccount;
	private final List<AccessMethod> orderMethods;
	private final UserInfoProfileConfig profileConfig;
	private Collection<AccessTransaction> transactions;
	
	private FlexiTableElement tableEl;
	private OrderItemsDataModel dataModel;
	
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private UserPortraitService userPortraitService;

	public OrderDetailController(UserRequest ureq, WindowControl wControl, OrderTableItem orderItem,
			boolean readOnly, boolean showCostCenter) {
		super(ureq, wControl, "order");
		this.readOnly = readOnly;
		// Reload to have the last status and transactions
		order = acService.loadOrderByKey(orderItem.getOrderKey());

		transactions = acService.findAccessTransactions(order);
		orderMethods = orderItem.getMethods();
		offerLabel = orderItem.getLabel();
		costCenterName = showCostCenter? orderItem.getCostCenterName(): null;
		costCenterAccount = showCostCenter? orderItem.getCostCenterAccount(): null;
		
		delivery = order.getDelivery();
		profileConfig = userPortraitService.createProfileConfig();
		
		initForm(ureq);
		loadModel();
		updateUI();
	}
	
	public OrderDetailController(UserRequest ureq, WindowControl wControl, OrderTableItem orderItem,
			boolean readOnly, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "order", rootForm);
		this.readOnly = readOnly;
		// Reload to have the last status and transactions
		order = acService.loadOrderByKey(orderItem.getOrderKey());
		transactions = acService.findAccessTransactions(order);
		orderMethods = orderItem.getMethods();
		offerLabel = orderItem.getLabel();
		costCenterName = orderItem.getCostCenterName();
		costCenterAccount = orderItem.getCostCenterAccount();
		
		delivery = order.getDelivery();
		profileConfig = userPortraitService.createProfileConfig();
		
		initForm(ureq);
		loadModel();
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtonsForm(formLayout);
		initMetadataForm(formLayout);
		initDeliveryForm(formLayout, ureq);
		initItemsForm(formLayout);
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			initStatus(layoutCont);
		}
	}

	private void initButtonsForm(FormItemContainer formLayout) {
		if(!readOnly && (order.getOrderStatus() == OrderStatus.NEW || order.getOrderStatus() == OrderStatus.PREPAYMENT)) {
			setPaidButton = uifactory.addFormLink("set.paid", formLayout, Link.BUTTON);
			setPaidButton.setIconLeftCSS("o_icon o_icon-fw o_icon_pay");
		}
	}
	
	private void initMetadataForm(FormItemContainer formLayout) {
		String orderNr = order.getOrderNr();
		StaticTextElement nrEl = uifactory.addStaticTextElement("order-nr", "order.nr", orderNr, formLayout);
		nrEl.setElementCssClass("o_order_number");
		
		// Offer type
		try(StringOutput methods = new StringOutput()) {
			Set<String> types = new HashSet<>();
			AccessMethodRenderer methodRenderer = new AccessMethodRenderer(acModule);
			for(AccessMethod method:orderMethods) {
				methodRenderer.render(methods, method, types, getLocale());
			}
			uifactory.addStaticTextElement("order-methods", "order.part.payment", methods.toString(), formLayout);
		} catch(Exception e) {
			logError("", e);
		}
		
		if(StringHelper.containsNonWhitespace(offerLabel)) {
			uifactory.addStaticTextElement("offer-name", "offer.name", StringHelper.escapeHtml(offerLabel), formLayout);
		}

		Date creationDate = order.getCreationDate();
		String creationDateStr = Formatter.getInstance(getLocale()).formatDateAndTime(creationDate);
		uifactory.addStaticTextElement("creation-date", "table.order.creationDate", creationDateStr, formLayout);
		
		if (orderMethods.stream().anyMatch(AccessMethod::isPaymentMethod)) {
			String priceI18nKey = "access.info.price";
			String orderTotalLines = PriceFormat.fullFormat(order.getTotalOrderLines());
			String orderTotal = PriceFormat.fullFormat(order.getTotal());
			if (!Objects.equals(orderTotalLines, orderTotal)) {
				orderTotal = orderTotalLines + "/"+ PriceFormat.format(order.getTotal());
				priceI18nKey = "access.info.price.original.applicable";
			}
			String orderTotalStr = PriceFormat.fullFormatVat(getTranslator(), acModule, orderTotal, null);
			uifactory.addStaticTextElement("order-total", priceI18nKey, orderTotalStr, formLayout);
		}
		
		if (order.getOrderStatus() == OrderStatus.NEW
				|| order.getOrderStatus() == OrderStatus.PREPAYMENT
				|| order.getOrderStatus() == OrderStatus.PAYED) {
			OrderLine orderLine = order.getParts().get(0).getOrderLines().get(0);
			OLATResource resource = orderLine.getOffer().getResource();
			if (OresHelper.calculateTypeName(CurriculumElement.class).equals(resource.getResourceableTypeName())) {
				Price cancellationFee = orderLine.getCancellationFee();
				if (cancellationFee != null) {
					String cancellingFee = PriceFormat.fullFormat(orderLine.getCancellationFee());
					Date resourceBeginDate = acService.getBeginDate(resource);
					if (orderLine.getCancellingFeeDeadlineDays() != null && resourceBeginDate != null) {
						Date deadline = DateUtils.addDays(resourceBeginDate, -orderLine.getCancellingFeeDeadlineDays());
						cancellingFee += " (" + translate("cancelling.fee.free.until", Formatter.getInstance(getLocale()).formatDate(deadline)) + ")";
					} else {
						cancellingFee += " (" + translate("cancelling.fee.free.never") + ")";
					}
					uifactory.addStaticTextElement("cancellation-fee", "order.cancellation.fee", cancellingFee, formLayout);
				}
			}
		} else if(order.getCancellationFeesLines() != null
				&& order.getCancellationFeesLines().getAmount() != null
				&& BigDecimal.ZERO.compareTo(order.getCancellationFeesLines().getAmount()) < 0) {
			String cancellationFeei18nKey = "order.cancellation.fee.charged";
			String feeLines = PriceFormat.fullFormat(order.getCancellationFeesLines());
			String fee = PriceFormat.fullFormat(order.getCancellationFees());
			if (!Objects.equals(feeLines, fee)) {
				fee = feeLines + "/"+ PriceFormat.format(order.getCancellationFees());
				cancellationFeei18nKey = "order.cancellation.fee.original.charged";
			}
			uifactory.addStaticTextElement("cancellation-fee", cancellationFeei18nKey, fee, formLayout);
		}
		
		if(StringHelper.containsNonWhitespace(costCenterName)) {
			uifactory.addStaticTextElement("cost-center-name", "cost.center", StringHelper.escapeHtml(costCenterName), formLayout);
		}
		if(StringHelper.containsNonWhitespace(costCenterAccount)) {
			uifactory.addStaticTextElement("cost-center-account", "cost.center.account", StringHelper.escapeHtml(costCenterAccount), formLayout);
		}
		if(StringHelper.containsNonWhitespace(order.getPurchaseOrderNumber())) {
			uifactory.addStaticTextElement("order-po-number", "order.purchase.number", StringHelper.escapeHtml(order.getPurchaseOrderNumber()), formLayout);
		}
		if(StringHelper.containsNonWhitespace(order.getComment())) {
			uifactory.addStaticTextElement("order-comment", "order.comment", StringHelper.escapeHtml(order.getComment()), formLayout);
		}
		
		if (order.getBillingAddress() != null) {
			billingAddressItem = new BillingAddressItem("billing-address", getLocale());
			billingAddressItem.setBillingAddress(order.getBillingAddress());
			billingAddressItem.setLabel("billing.address", null);
			formLayout.add("billing-address", billingAddressItem);
		}
	}
	
	private void initStatus(FormLayoutContainer layoutCont) {
		try(StringOutput status = new StringOutput()) {
			OrderStatusRenderer statusRenderer = new OrderStatusRenderer(getTranslator());
			List<OrderTableItem> items = acService.findOrderItems(null, delivery, order.getKey(), null, null,
					OrderStatus.values(), null, null, false, false, 0, 1, null);
			Status consolidatedStatus; 
			if(items.size() == 1) {
				consolidatedStatus = items.get(0).getStatus();
			} else {
				consolidatedStatus = Status.getStatus(order.getOrderStatus().name(), order.getCancellationFees(), "", "", orderMethods);
			}
			statusRenderer.renderStatus(status, consolidatedStatus);
			layoutCont.contextPut("orderStatus", status.toString());
		} catch(Exception e) {
			logError("", e);
		}
	}

	private void initDeliveryForm(FormItemContainer formLayout, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), delivery);
			UserInfoProfileController profile = new UserInfoProfileController(ureq, getWindowControl(), profileConfig, portraitUser);
			listenTo(profile);
			layoutCont.put("delivery-profile", profile.getInitialComponent());
		}
	}

	private void initItemsForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderItemCol.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderItemCol.method,
				new AccessMethodRenderer(acModule)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrderItemCol.select));
		
		dataModel = new OrderItemsDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "orderItemList", dataModel, 25, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings(translate("orders.empty"), null, "o_ac_order_status_prepayment_icon");
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
	}
	
	private void loadModel() {
		List<OrderItemRow> items = new ArrayList<>();
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
				OrderItemRow row = forgeRow(part, line, transaction, first);
				items.add(row);
				first = false;
			}
		}
		
		dataModel.setObjects(items);
		tableEl.reset(true, true, true);
	}
	
	private OrderItemRow forgeRow(OrderPart part, OrderLine line, AccessTransaction transaction, boolean first) {
		OLATResource resource = line.getOffer().getResource();
		String displayName;
		if(resource == null) {
			displayName = line.getOffer().getResourceDisplayName();
		} else {
			displayName = acService.resolveDisplayName(resource);
		}
		OrderItemRow row = new OrderItemRow(part, line, transaction, displayName, first);
		
		if(first) {
			FormLink transactionDetailsLink = uifactory.addFormLink("details." + (++counter), CMD_SELECT, "select", tableEl);
			row.setTransactionDetailsLink(transactionDetailsLink);
			transactionDetailsLink.setUserObject(row);
		}
		return row;
	}
	
	private void updateUI() {
		boolean showTable = dataModel.getRowCount() > 1;
		
		for(AccessMethod method:orderMethods) {
			if(method instanceof PaypalAccessMethod || method instanceof PaypalCheckoutAccessMethod) {
				showTable |= true;
			}
		}
		
		tableEl.setVisible(showTable);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == detailsCtlr) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(detailsCtlr);
		removeAsListenerAndDispose(cmc);
		detailsCtlr = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == selectResourceLink) {
			doSelectResource(ureq);
		} else if(source == setPaidButton) {
			doSetPaid(ureq);
		} else if(source instanceof FormLink link && CMD_SELECT.equals(link.getCmd())
				&& link.getUserObject() instanceof OrderItemRow row) {
			doOpenTransactionDetails(ureq, row);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSetPaid(UserRequest ureq) {
		order = acService.loadOrderByKey(order.getKey());
		if(order != null) {
			acService.changeOrderStatus(order, OrderStatus.PAYED);
		}
		showInfo("info.order.set.as.paid");
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doSelectResource(UserRequest ureq) {
		OrderItemRow wrapper = dataModel.getObject(0);
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
	
	private void doOpenTransactionDetails(UserRequest ureq, OrderItemRow wrapper) {
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
}
