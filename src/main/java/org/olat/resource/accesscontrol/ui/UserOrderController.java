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
import org.olat.core.id.Identity;
import org.olat.resource.accesscontrol.provider.auto.ui.AdvanceOrderController;

/**
 *
 * Initial date: 08.09.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserOrderController extends BasicController {

	private final VelocityContainer mainVC;
	private final Link ordersLink, advanceOrdersLink;
	private final SegmentViewComponent segmentView;

	private OrdersController ordersCtrl;
	private AdvanceOrderController advanceOrdersCtrl;

	private Identity identity;

	public UserOrderController(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl);
		this.identity = identity;

		mainVC = createVelocityContainer("segments");

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		ordersLink = LinkFactory.createLink("segment.orders", mainVC, this);
		segmentView.addSegment(ordersLink, true);
		advanceOrdersLink = LinkFactory.createLink("segment.advance.orders", mainVC, this);
		segmentView.addSegment(advanceOrdersLink, false);

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
				} else if (clickedLink == advanceOrdersLink){
					doOpenAdvanceOrders(ureq);
				}
			}
		}
	}

	private void doOpenOrders(UserRequest ureq) {
		if(ordersCtrl == null) {
			ordersCtrl = new OrdersController(ureq, getWindowControl(), identity);
			listenTo(ordersCtrl);
		}
		mainVC.put("segmentCmp", ordersCtrl.getInitialComponent());
	}

	private void doOpenAdvanceOrders(UserRequest ureq) {
		if(advanceOrdersCtrl == null) {
			advanceOrdersCtrl = new AdvanceOrderController(ureq, getWindowControl(), identity);
			listenTo(advanceOrdersCtrl);
		}
		mainVC.put("segmentCmp", advanceOrdersCtrl.getInitialComponent());
	}


}
