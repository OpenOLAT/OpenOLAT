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
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.provider.auto.ui.AdvanceOrderController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 08.09.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserOrderController extends BasicController {
	private final static String SCOPE_KEY_ORDERS = "Orders";
	private final static String SCOPE_KEY_PENDING_MEMBERSHIPS = "PendingMemberships";
	private final static String SCOPE_KEY_ADVANCE_ORDERS = "AdvanceOrders";

	private final VelocityContainer mainVC;
	private final List<Scope> scopes;
	private final ScopeSelection scopeSelection;
	private OrdersController ordersCtrl;
	private PendingMembershipsController pendingMembershipsCtrl;
	private AdvanceOrderController advanceOrdersCtrl;

	private final Identity identity;
	private final boolean canActivatePendingAccounts;

	@Autowired
	private AccessControlModule accessControlModule;
	@Autowired
	private CurriculumModule curriculumModule;

	public UserOrderController(UserRequest ureq, WindowControl wControl, Identity identity, boolean canActivatePendingAccounts) {
		super(ureq, wControl);
		this.identity = identity;
		this.canActivatePendingAccounts = canActivatePendingAccounts;

		mainVC = createVelocityContainer("scopes");

		scopes = new ArrayList<>();
		Scope ordersScope = ScopeFactory.createScope(SCOPE_KEY_ORDERS, translate("scope.orders"), null, "o_icon o_ac_offer_bookable_icon");
		scopes.add(ordersScope);
		if (isShowPendingMemberships()) {
			Scope pendingMembershipsScope = ScopeFactory.createScope(SCOPE_KEY_PENDING_MEMBERSHIPS, translate("scope.pending.memberships"), null, "o_icon o_icon_payment_open");
			scopes.add(pendingMembershipsScope);
		}
		if (accessControlModule.isAutoEnabled()) {
			Scope advanceOrdersScope = ScopeFactory.createScope(SCOPE_KEY_ADVANCE_ORDERS, translate("scope.advance.orders"), null, "o_icon o_ac_order_pre");
			scopes.add(advanceOrdersScope);
		}

		scopeSelection = ScopeFactory.createScopeSelection("scope.selection", mainVC, this, scopes);
		scopeSelection.setHintsEnabled(false);
		scopeSelection.setAllowNoSelection(false);
		scopeSelection.setSelectedKey(SCOPE_KEY_ORDERS);
		scopeSelection.setVisible(scopes.size() > 1);
		
		doOpenOrders(ureq);
		putInitialPanel(mainVC);
	}

	private boolean isShowPendingMemberships() {
		if (!curriculumModule.isEnabled()) {
			return false;
		}
		if (!accessControlModule.isInvoiceEnabled()) {
			return false;
		}
		if (!canActivatePendingAccounts) {
			return false;
		}
		
		return true;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == scopeSelection) {
			if (event instanceof ScopeEvent scopeEvent) {
				if (SCOPE_KEY_ORDERS.equals(scopeEvent.getSelectedKey())) {
					doOpenOrders(ureq);
				} else if (SCOPE_KEY_PENDING_MEMBERSHIPS.equals(scopeEvent.getSelectedKey())) {
					doPendingMemberships(ureq);
				} else if (SCOPE_KEY_ADVANCE_ORDERS.equals(scopeEvent.getSelectedKey())) {
					doOpenAdvanceOrders(ureq);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == pendingMembershipsCtrl) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	private void doOpenOrders(UserRequest ureq) {
		if(ordersCtrl == null) {
			OrdersSettings settings = OrdersSettings.defaultSettings();
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(SCOPE_KEY_ORDERS), null);
			ordersCtrl = new OrdersController(ureq, bwControl, identity, settings);
			listenTo(ordersCtrl);
		}
		mainVC.put("scopeCmp", ordersCtrl.getInitialComponent());
	}

	private void doPendingMemberships(UserRequest ureq) {
		if (pendingMembershipsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(SCOPE_KEY_PENDING_MEMBERSHIPS), null);
			pendingMembershipsCtrl = new PendingMembershipsController(ureq, bwControl, identity);
			listenTo(pendingMembershipsCtrl);
		}
		mainVC.put("scopeCmp", pendingMembershipsCtrl.getInitialComponent());
	}

	private void doOpenAdvanceOrders(UserRequest ureq) {
		if(advanceOrdersCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(SCOPE_KEY_ADVANCE_ORDERS), null);
			advanceOrdersCtrl = new AdvanceOrderController(ureq, bwControl, identity);
			listenTo(advanceOrdersCtrl);
		}
		mainVC.put("scopeCmp", advanceOrdersCtrl.getInitialComponent());
	}

	public void goToPendingMemberships(UserRequest ureq) {
		if (!scopes.stream().map(Scope::getKey).anyMatch(SCOPE_KEY_PENDING_MEMBERSHIPS::equals)) {
			return;
		}

		scopeSelection.setSelectedKey(SCOPE_KEY_PENDING_MEMBERSHIPS);
		doPendingMemberships(ureq);
	}
}
