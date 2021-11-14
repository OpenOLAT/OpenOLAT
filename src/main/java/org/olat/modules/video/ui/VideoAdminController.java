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
package org.olat.modules.video.ui;

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

/**
 * administration segmentview controller
 * @author fkiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 *
 */
public class VideoAdminController extends BasicController  {

	
	private final SegmentViewComponent segmentView;
	private Link adminSetLink, adminListLink, adminTranscodingLink, adminErrorLink;
	private VelocityContainer mainVC;
	
	private VideoAdminSetController adminSetController;
	private VideoAdminListController adminListController;
	private VideoAdminTranscodingController adminTranscodingController;
	private VideoAdminErrorController adminErrorController;

	public VideoAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("video_admin");

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);

		adminSetLink = LinkFactory.createLink("tab.admin.set", mainVC, this);
		segmentView.addSegment(adminSetLink, true);
		adminListLink = LinkFactory.createLink("tab.admin.list", mainVC, this);
		segmentView.addSegment(adminListLink, false);
		adminErrorLink = LinkFactory.createLink("tab.admin.error", mainVC, this);
		segmentView.addSegment(adminErrorLink, false);
		adminTranscodingLink = LinkFactory.createLink("tab.admin.transcoding", mainVC, this);
		segmentView.addSegment(adminTranscodingLink, false);
		
		doOpenAdminList(ureq);
		
		segmentView.select(adminListLink);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == adminSetLink) {
					doOpenAdminConfig(ureq);
				} else if (clickedLink == adminListLink){
					doOpenAdminList(ureq);
				} else if (clickedLink == adminTranscodingLink){
					doOpenTranscodingAdmin(ureq);
				} else if (clickedLink == adminErrorLink) {
					doOpenErrorAdmin(ureq);
				}
			}
		}
	}
	
	private void doOpenErrorAdmin(UserRequest ureq) {
		if(adminErrorController == null) {
			adminErrorController = new VideoAdminErrorController(ureq, getWindowControl());
			listenTo(adminErrorController);
		}
		mainVC.put("segmentCmp", adminErrorController.getInitialComponent());
	}
	
	private void doOpenAdminConfig(UserRequest ureq) {
		if(adminSetController == null) {
			adminSetController = new VideoAdminSetController(ureq, getWindowControl());
			listenTo(adminSetController);
		}
		mainVC.put("segmentCmp", adminSetController.getInitialComponent());
	}

	private void doOpenAdminList(UserRequest ureq) {
		if(adminListController == null) {
			adminListController = new VideoAdminListController(ureq, getWindowControl());
			listenTo(adminListController);
		}
		mainVC.put("segmentCmp", adminListController.getInitialComponent());
	}
	
	private void doOpenTranscodingAdmin(UserRequest ureq){
		if(adminTranscodingController == null) {
			adminTranscodingController = new VideoAdminTranscodingController(ureq, getWindowControl());
			listenTo(adminTranscodingController);
		}
		mainVC.put("segmentCmp", adminTranscodingController.getInitialComponent());
		adminTranscodingController.reloadTable();
	}

}