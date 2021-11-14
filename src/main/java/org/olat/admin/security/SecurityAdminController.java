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
package org.olat.admin.security;

import org.olat.core.commons.services.csp.CSPModule;
import org.olat.core.commons.services.csp.ui.CSPLogListController;
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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SecurityAdminController extends BasicController implements BreadcrumbPanelAware {
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link cspLink;
	private final Link configurationLink;
	private BreadcrumbPanel stackPanel;
	
	private CSPLogListController logListCtrl;
	private SecurityAdminConfigurationController configurationCtrl;

	@Autowired
	private CSPModule cspModule;
	
	public SecurityAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("security_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		configurationLink = LinkFactory.createLink("security.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		doOpenConfiguration(ureq);
		
		cspLink = LinkFactory.createLink("security.csp.log", mainVC, this);
		if(cspModule.isContentSecurityPolicyEnabled()) {
			segmentView.addSegment(cspLink, false);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
		if(logListCtrl != null) {
			logListCtrl.setBreadcrumbPanel(stackPanel);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configurationLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == cspLink){
					doOpenCSPLog(ureq);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == configurationCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				segmentView.removeSegment(cspLink);
				if(cspModule.isContentSecurityPolicyEnabled()) {
					segmentView.addSegment(cspLink, false);
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		if(configurationCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Configuration"), null);
			configurationCtrl = new SecurityAdminConfigurationController(ureq, bwControl);
			listenTo(configurationCtrl);
		}
		addToHistory(ureq, configurationCtrl);
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
	}
	
	private void doOpenCSPLog(UserRequest ureq) {
		if(logListCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("CSPLog"), null);
			logListCtrl = new CSPLogListController(ureq, bwControl);
			logListCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(configurationCtrl);
		}
		addToHistory(ureq, logListCtrl);
		mainVC.put("segmentCmp", logListCtrl.getInitialComponent());
	}
}