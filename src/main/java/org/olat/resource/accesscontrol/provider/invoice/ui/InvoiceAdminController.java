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
package org.olat.resource.accesscontrol.provider.invoice.ui;

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
import org.olat.resource.accesscontrol.ui.CostCenterListController;

/**
 * 
 * Initial date: 5 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InvoiceAdminController extends BasicController {

	private final VelocityContainer mainVC;
	private final Link configLink;
	private Link costCentersLink;
	private final SegmentViewComponent segmentView;

	private InvoiceAdminConfigsController configCtrl;
	private CostCenterListController costCentersCtrl;
	
	public InvoiceAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin");
		putInitialPanel(mainVC);
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		configLink = LinkFactory.createLink("configuration", mainVC, this);
		segmentView.addSegment(configLink, true);
		costCentersLink = LinkFactory.createLink("cost.centers", mainVC, this);
		segmentView.addSegment(costCentersLink, false);
		
		doOpenConfig(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configLink) {
					doOpenConfig(ureq);
				} else if (clickedLink == costCentersLink){
					doOpenCostCenters(ureq);
				}
			}
		}
	}

	private void doOpenConfig(UserRequest ureq) {
		if (configCtrl == null) {
			configCtrl = new InvoiceAdminConfigsController(ureq, getWindowControl());
			listenTo(configCtrl);
		}
		mainVC.put("segmentCmp", configCtrl.getInitialComponent());
	}

	private void doOpenCostCenters(UserRequest ureq) {
		if (costCentersCtrl == null) {
			costCentersCtrl = new CostCenterListController(ureq, getWindowControl());
			listenTo(costCentersCtrl);
		}
		mainVC.put("segmentCmp", costCentersCtrl.getInitialComponent());
	}
}
