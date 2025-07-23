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
package org.olat.repository.ui;

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
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 22 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryAdminController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link configurationLink;
	private final Link accessLink;

	private RepositoryAdminAccessController accessCtrl;
	private RepositoryAdminConfigurationController configurationCtrl;
	
	public RepositoryAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		
		mainVC = createVelocityContainer("repository_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		configurationLink = LinkFactory.createLink("admin.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		doOpenSettings(ureq);
		
		accessLink = LinkFactory.createLink("admin.access", mainVC, this);
		accessLink.setElementCssClass("o_sel_access");
		segmentView.addSegment(accessLink, false);

		putInitialPanel(mainVC);

	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configurationLink) {
					doOpenSettings(ureq);
				} else if (clickedLink == accessLink){
					doOpenAccess(ureq);
				}
			}
		}
	}
	
	private void doOpenSettings(UserRequest ureq) {
		if(configurationCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Configuration"), null);
			configurationCtrl = new RepositoryAdminConfigurationController(ureq, bwControl);
			listenTo(configurationCtrl);
		}
		addToHistory(ureq, configurationCtrl);
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
	}
	
	private void doOpenAccess(UserRequest ureq) {
		if(accessCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Access"), null);
			accessCtrl = new RepositoryAdminAccessController(ureq, bwControl);
			listenTo(accessCtrl);
		}
		addToHistory(ureq, accessCtrl);
		mainVC.put("segmentCmp", accessCtrl.getInitialComponent());
	}
}