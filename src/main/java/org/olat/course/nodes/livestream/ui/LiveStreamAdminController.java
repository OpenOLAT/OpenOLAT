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
package org.olat.course.nodes.livestream.ui;

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
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 4 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamAdminController extends BasicController {
	
	private static final String SETTINGS_RES_TYPE = "Settings";
	private static final String URL_TEMPLATES_RES_TYPE = "UrlTemplates";
	private static final String PAELLA_RES_TYPE = "Paella";
	
	private VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	private Link settingsLink;
	private Link urlTemplatesLink;
	private Link paellaLink;
	
	private LiveStreamAdminSettingsController settingsCtrl;
	private UrlTemplateListController urlTemplatesCtrl;
	private PaellaAdminController paellaCtrl;

	public LiveStreamAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		settingsLink = LinkFactory.createLink("admin.settings", mainVC, this);
		segmentView.addSegment(settingsLink, true);
		urlTemplatesLink = LinkFactory.createLink("admin.url.templates", mainVC, this);
		segmentView.addSegment(urlTemplatesLink, false);
		paellaLink = LinkFactory.createLink("admin.paella", mainVC, this);
		segmentView.addSegment(paellaLink, false);

		doOpenSettings(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == settingsLink) {
					doOpenSettings(ureq);
				} else if (clickedLink == urlTemplatesLink){
					doOpenUrlTemplates(ureq);
				} else if (clickedLink == paellaLink){
					doOpenPaella(ureq);
				}
			}
		}
	}
	
	private void doOpenSettings(UserRequest ureq) {
		if (settingsCtrl != null) {
			removeAsListenerAndDispose(settingsCtrl);
		}
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(SETTINGS_RES_TYPE), null);
		settingsCtrl = new LiveStreamAdminSettingsController(ureq, swControl);
		listenTo(settingsCtrl);
		mainVC.put("segmentCmp", settingsCtrl.getInitialComponent());
	}
	
	private void doOpenUrlTemplates(UserRequest ureq) {
		if (urlTemplatesCtrl != null) {
			removeAsListenerAndDispose(urlTemplatesCtrl);
		}
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(URL_TEMPLATES_RES_TYPE), null);
		urlTemplatesCtrl = new UrlTemplateListController(ureq, swControl);
		listenTo(urlTemplatesCtrl);
		mainVC.put("segmentCmp", urlTemplatesCtrl.getInitialComponent());
	}
	
	private void doOpenPaella(UserRequest ureq) {
		if (paellaCtrl != null) {
			removeAsListenerAndDispose(paellaCtrl);
		}
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(PAELLA_RES_TYPE), null);
		paellaCtrl = new PaellaAdminController(ureq, swControl);
		listenTo(paellaCtrl);
		mainVC.put("segmentCmp", paellaCtrl.getInitialComponent());
	}
}
