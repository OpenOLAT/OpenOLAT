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
package org.olat.admin.sysinfo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 15.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSessionAdminController extends BasicController implements BreadcrumbPanelAware {

	private final Link sessionsLink, configLink, infosLink;
	private final SegmentViewComponent segmentView;
	
	private final VelocityContainer mainVC;
	
	private UserSessionController sessionListCtrl;
	private UserSessionConfigAdminController configCtrl;
	private UserSessionInformationsController infoCtrl;
	private BreadcrumbPanel stackedController;
	
	public UserSessionAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("segments");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		sessionsLink = LinkFactory.createLink("session.list", mainVC, this);
		segmentView.addSegment(sessionsLink, true);
		
		configLink = LinkFactory.createLink("session.configuration", mainVC, this);
		segmentView.addSegment(configLink, false);

		infosLink = LinkFactory.createLink("sess.details", mainVC, this);
		segmentView.addSegment(infosLink, false);
		
		mainVC.put("segments", segmentView);
		doOpenSessionList(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackedController = stackPanel;
		if(sessionListCtrl != null) {
			sessionListCtrl.setBreadcrumbPanel(stackedController);
		}
	}

	@Override
	protected void doDispose() {
		this.stackedController = null;
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == sessionsLink) {
					doOpenSessionList(ureq);
				} else if (clickedLink == configLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == infosLink) {
					doOpenInformations(ureq);
				}
			}
		}
	}

	private void doOpenSessionList(UserRequest ureq) {
		if(sessionListCtrl == null) {
			sessionListCtrl = new UserSessionController(ureq, getWindowControl());
			sessionListCtrl.setBreadcrumbPanel(stackedController);
			listenTo(sessionListCtrl);
		}
		mainVC.put("segmentCmp", sessionListCtrl.getInitialComponent());
	}

	private void doOpenConfiguration(UserRequest ureq) {
		if(configCtrl == null) {
			configCtrl = new UserSessionConfigAdminController(ureq, getWindowControl());
			listenTo(configCtrl);
		}
		mainVC.put("segmentCmp", configCtrl.getInitialComponent());
	}
	
	private void doOpenInformations(UserRequest ureq) {
		if(infoCtrl == null) {
			infoCtrl = new UserSessionInformationsController(ureq, getWindowControl());
			listenTo(infoCtrl);
		}
		mainVC.put("segmentCmp", infoCtrl.getInitialComponent());
	}
}