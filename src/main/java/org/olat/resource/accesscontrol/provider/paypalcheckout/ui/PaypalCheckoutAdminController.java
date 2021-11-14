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
package org.olat.resource.accesscontrol.provider.paypalcheckout.ui;

import java.util.List;

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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 25 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutAdminController extends BasicController implements Activateable2 {

	private Link masterAccountLink;
	private Link transactionsLink;
	private SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private PaypalCheckoutAccountConfigurationController accountController;
	private PaypalCheckoutTransactionsController transactionsController;
	
	public PaypalCheckoutAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("paypal_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		masterAccountLink = LinkFactory.createLink("paypal.segment.account", mainVC, this);
		segmentView.addSegment(masterAccountLink, true);
	
		transactionsLink = LinkFactory.createLink("paypal.segment.transactions", mainVC, this);
		segmentView.addSegment(transactionsLink, false);
		doOpenAccountSettings(ureq);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("configuration", 0l);
		addToHistory(ureq, ores, null);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == masterAccountLink) {
					doOpenAccountSettings(ureq);
				} else if (clickedLink == transactionsLink){
					doOpenTransactions(ureq);
				}
			}
		}
	}
	
	private void doOpenAccountSettings(UserRequest ureq) {
		if(accountController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("transactions", 0l);
			WindowControl bwControl = addToHistory(ureq, ores, null);
			accountController = new PaypalCheckoutAccountConfigurationController(ureq, bwControl);
			listenTo(accountController);
		} else {
			addToHistory(ureq, accountController);
		}
		mainVC.put("segmentCmp", accountController.getInitialComponent());
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("configuration", 0l);
		addToHistory(ureq, ores, null);
	}
	
	private void doOpenTransactions(UserRequest ureq) {
		if(transactionsController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("transactions", 0l);
			WindowControl bwControl = addToHistory(ureq, ores, null);
			transactionsController = new PaypalCheckoutTransactionsController(ureq, bwControl);
			listenTo(transactionsController);
		} else {
			addToHistory(ureq, transactionsController);
		}
		mainVC.put("segmentCmp", transactionsController.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("configuration".equals(type)) {
			doOpenAccountSettings(ureq);
			segmentView.select(masterAccountLink);
		} else if ("transactions".equals(type)) {
			doOpenTransactions(ureq);
			segmentView.select(transactionsLink);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			transactionsController.activate(ureq, subEntries, entries.get(0).getTransientState());
		}
	}
}
