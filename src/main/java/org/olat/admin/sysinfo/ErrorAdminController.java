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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 16.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ErrorAdminController extends BasicController {

	private final Link errorsLink;
	private final Link logLevelLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private ErrorSearchController searchCtrl;
	private ErrorLogLevelController logLevelCtrl;
	
	public ErrorAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("segments");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		errorsLink = LinkFactory.createLink("errors", mainVC, this);
		segmentView.addSegment(errorsLink, true);
		logLevelLink = LinkFactory.createLink("loglevels", mainVC, this);
		segmentView.addSegment(logLevelLink, false);
		
		mainVC.put("segments", segmentView);
		doErrorList(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if(clickedLink == errorsLink) {
					doErrorList(ureq);
				} else if(clickedLink == logLevelLink) {
					doLogLevel(ureq);
				}
			}
		}
	}

	private void doLogLevel(UserRequest ureq) {
		if(logLevelCtrl == null) {
			logLevelCtrl = new ErrorLogLevelController(ureq, getWindowControl());
			listenTo(logLevelCtrl);
		}
		mainVC.put("segmentCmp", logLevelCtrl.getInitialComponent());
	}
	
	private void doErrorList(UserRequest ureq) {
		if(searchCtrl == null) {
			searchCtrl = new ErrorSearchController(ureq, getWindowControl());
			listenTo(searchCtrl);
		}
		mainVC.put("segmentCmp", searchCtrl.getInitialComponent());
	}
}
