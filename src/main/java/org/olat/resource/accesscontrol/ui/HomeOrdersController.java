/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class HomeOrdersController extends BasicController {
	private final static String SCOPE_KEY_ORDERS = "orders";
	private final static String SCOPE_KEY_BILLING_ADDRESSES = "billing.addresses";
	
	private final VelocityContainer mainVC;
	private final ScopeSelection scopeSelection;
	private OrdersController ordersCtrl;
	private BillingAddressListController billingAddressesCtrl;
	
	@Autowired
	private AccessControlModule acModule;

	public HomeOrdersController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("orders");

		List<Scope> scopes = new ArrayList<>();
		Scope ordersScope = ScopeFactory.createScope(SCOPE_KEY_ORDERS, translate("scope.orders"), null, "o_icon o_ac_offer_bookable_icon");
		scopes.add(ordersScope);
		if (acModule.isInvoiceEnabled()) {
			Scope billingAddressesScope = ScopeFactory.createScope(SCOPE_KEY_BILLING_ADDRESSES, translate("scope.billing.addresses"), null, "o_icon o_icon_billing_address");
			scopes.add(billingAddressesScope);
		}

		scopeSelection = ScopeFactory.createScopeSelection("scope.selection", mainVC, this, scopes);
		scopeSelection.setHintsEnabled(false);
		scopeSelection.setAllowNoSelection(false);
		scopeSelection.setSelectedKey(SCOPE_KEY_ORDERS);
		scopeSelection.setVisible(scopes.size() > 1);
	
		doOpenOrders(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == scopeSelection) {
			if (event instanceof ScopeEvent scopeEvent) {
				if (SCOPE_KEY_ORDERS.equals(scopeEvent.getSelectedKey())) {
					doOpenOrders(ureq);
				} else if (SCOPE_KEY_BILLING_ADDRESSES.equals(scopeEvent.getSelectedKey())) {
					doOpenBillingAddresses(ureq);
				}
			}
		}
	}

	private void doOpenOrders(UserRequest ureq) {
		if(ordersCtrl == null) {
			OrdersSettings settings = OrdersSettings.defaultSettings();
			ordersCtrl = new OrdersController(ureq, getWindowControl(), getIdentity(), settings);
			listenTo(ordersCtrl);
		}
		mainVC.put("scopeCmp", ordersCtrl.getInitialComponent());
	}

	private void doOpenBillingAddresses(UserRequest ureq) {
		if(billingAddressesCtrl == null) {
			billingAddressesCtrl = new BillingAddressListController(ureq, getWindowControl(), null, getIdentity());
			listenTo(billingAddressesCtrl);
		}
		mainVC.put("scopeCmp", billingAddressesCtrl.getInitialComponent());
	}

}
