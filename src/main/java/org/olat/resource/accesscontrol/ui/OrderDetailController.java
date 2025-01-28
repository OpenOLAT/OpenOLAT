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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import org.olat.core.util.Formatter;
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
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoProfileController;
import org.olat.user.UserInfoService;
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
	private CloseableModalController cmc;
	private TransactionDetailsController detailsCtlr;
	
	private int counter = 0;
	private Order order;
	private final Identity delivery;
	private final boolean readOnly;
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
	private UserInfoService userInfoService;
	
	public OrderDetailController(UserRequest ureq, WindowControl wControl, Long orderKey,
			UserAvatarMapper avatarMapper, String avatarMapperBaseURL, boolean readOnly) {
		super(ureq, wControl, "order");
		this.readOnly = readOnly;
		
		order = acService.loadOrderByKey(orderKey);
		transactions = acService.findAccessTransactions(order);
		orderMethods = acService.findAccessMethods(order);
		
		delivery = order.getDelivery();
		profileConfig = userInfoService.createProfileConfig();
		profileConfig.setChatEnabled(true);
		profileConfig.setAvatarMapper(avatarMapper);
		profileConfig.setAvatarMapperBaseURL(avatarMapperBaseURL);
		
		initForm(ureq);
		loadModel();
		updateUI();
	}
	
	public OrderDetailController(UserRequest ureq, WindowControl wControl, Long orderKey,
			UserAvatarMapper avatarMapper, String avatarMapperBaseURL, boolean readOnly, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "order", rootForm);
		this.readOnly = readOnly;
		
		order = acService.loadOrderByKey(orderKey);
		transactions = acService.findAccessTransactions(order);
		orderMethods = acService.findAccessMethods(order);
		
		delivery = order.getDelivery();
		profileConfig = userInfoService.createProfileConfig();
		profileConfig.setChatEnabled(true);
		profileConfig.setAvatarMapper(avatarMapper);
		profileConfig.setAvatarMapperBaseURL(avatarMapperBaseURL);
		
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
		
		//TODO booking
		uifactory.addStaticTextElement("offer-name", "offer.name", "", formLayout);

		Date creationDate = order.getCreationDate();
		String creationDateStr = Formatter.getInstance(getLocale()).formatDateAndTime(creationDate);
		uifactory.addStaticTextElement("creation-date", "order.creationDate", creationDateStr, formLayout);
		
		String orderTotal = PriceFormat.fullFormat(order.getTotal());
		String orderTotalStr;
		if(acModule.isVatEnabled()) {
			BigDecimal vat = acModule.getVat();
			String vatStr = vat == null ? "" : vat.setScale(3, RoundingMode.HALF_EVEN).toPlainString();
			orderTotalStr = translate("access.info.price.vat", orderTotal, vatStr);
		} else {
			orderTotalStr = translate("access.info.price.noVat", orderTotal);
		}
		uifactory.addStaticTextElement("order-total", "order.total", orderTotalStr, formLayout);
		
		Price cancellationFee = order.getCancellationFees();
		if(cancellationFee != null && cancellationFee.getAmount() != null && BigDecimal.ZERO.compareTo(cancellationFee.getAmount()) < 0) {
			String fee = PriceFormat.fullFormat(cancellationFee);
			uifactory.addStaticTextElement("cancellation-fee", "order.cancellation.fee", fee, formLayout);
		}
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			try(StringOutput status = new StringOutput()) {
				OrderStatusRenderer statusRenderer = new OrderStatusRenderer(getTranslator());
				statusRenderer.renderStatus(status, "o_labeled", order.getOrderStatus());
				layoutCont.contextPut("orderStatus", status.toString());
			} catch(Exception e) {
				logError("", e);
			}
		}
	}

	private void initDeliveryForm(FormItemContainer formLayout, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			UserInfoProfile teacherProfile = userInfoService.createProfile(delivery);
			UserInfoProfileController profile = new UserInfoProfileController(ureq, getWindowControl(), profileConfig, teacherProfile);
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
		showInfo("info.order.set.as.paied");
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
