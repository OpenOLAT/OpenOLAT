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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
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

	private final VelocityContainer mainVC;
	private final Link ordersLink;
	private Link billingAddressesLink;
	private final SegmentViewComponent segmentView;

	private OrdersController ordersCtrl;
	private BillingAddressListController billingAddressesCtrl;
	
	@Autowired
	private AccessControlModule acModule;

	public HomeOrdersController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("orders");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		ordersLink = LinkFactory.createLink("segment.orders", mainVC, this);
		segmentView.addSegment(ordersLink, true);
		if (acModule.isInvoiceEnabled()) {
			billingAddressesLink = LinkFactory.createLink("segment.billing.addresses", mainVC, this);
			segmentView.addSegment(billingAddressesLink, false);
		}
		doOpenOrders(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == ordersLink) {
					doOpenOrders(ureq);
				} else if (clickedLink == billingAddressesLink){
					doOpenBillingAddresses(ureq);
				}
			}
		}
	}

	private void doOpenOrders(UserRequest ureq) {
		if(ordersCtrl == null) {
			ordersCtrl = new OrdersController(ureq, getWindowControl(), getIdentity());
			listenTo(ordersCtrl);
		}
		mainVC.put("segmentCmp", ordersCtrl.getInitialComponent());
	}

	private void doOpenBillingAddresses(UserRequest ureq) {
		if(billingAddressesCtrl == null) {
			billingAddressesCtrl = new BillingAddressListController(ureq, getWindowControl(), null, getIdentity());
			listenTo(billingAddressesCtrl);
		}
		mainVC.put("segmentCmp", billingAddressesCtrl.getInitialComponent());
	}

}
