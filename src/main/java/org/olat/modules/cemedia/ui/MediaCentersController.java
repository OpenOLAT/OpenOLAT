/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.cemedia.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 15 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MediaCentersController extends BasicController implements Activateable2 {
	
	private final Link myMediaCenterLink;
	private final Link adminMediaCenterLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final TooledStackedPanel stackPanel;
	
	private MediaCenterController myMediaCenterCtrl;
	private MediaCenterController adminMediaCenterCtrl;
	
	public MediaCentersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		
		mainVC = createVelocityContainer("segments");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		myMediaCenterLink = LinkFactory.createLink("segment.my.mediacenter", mainVC, this);
		segmentView.addSegment(myMediaCenterLink, true);
		adminMediaCenterLink = LinkFactory.createLink("segment.admin.mediacenter", mainVC, this);
		segmentView.addSegment(adminMediaCenterLink, false);
		
		doOpenMyMediaCenter(ureq);
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == myMediaCenterLink) {
					doOpenMyMediaCenter(ureq);
				} else if (clickedLink == adminMediaCenterLink){
					doOpenAdminMediaCenter(ureq);
				}
			}
		}
	}
	
	private void doOpenMyMediaCenter(UserRequest ureq) {
		if(myMediaCenterCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("My"), null);
			myMediaCenterCtrl = new MediaCenterController(ureq, bwControl, stackPanel, MediaCenterConfig.valueOfMy());
			listenTo(myMediaCenterCtrl);
		}
		addToHistory(ureq, myMediaCenterCtrl);
		mainVC.put("segmentCmp", myMediaCenterCtrl.getInitialComponent());
	}
	
	private void doOpenAdminMediaCenter(UserRequest ureq) {
		if(adminMediaCenterCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Admin"), null);
			adminMediaCenterCtrl = new MediaCenterController(ureq, bwControl, stackPanel, MediaCenterConfig.managementConfig());
			adminMediaCenterCtrl.setFormTranslatedTitle(translate("management.title"));
			listenTo(adminMediaCenterCtrl);
		}
		addToHistory(ureq, adminMediaCenterCtrl);
		mainVC.put("segmentCmp", adminMediaCenterCtrl.getInitialComponent());
	}
}