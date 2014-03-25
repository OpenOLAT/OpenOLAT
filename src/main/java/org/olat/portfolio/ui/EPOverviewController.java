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
package org.olat.portfolio.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 24.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EPOverviewController extends BasicController {
	
	private EPMapRunController myMapsCtrl;
	private EPMapRunController myTasksCtrl;
	private EPMapRunController publicMapsCtrl;
	private EPArtefactPoolRunController artefactsCtrl;
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link myArtefactLink, myMapLink, myTaskLink, publicMapLink;
	
	public EPOverviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("segments");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		myArtefactLink = LinkFactory.createLink("myartefacts.menu.title", mainVC, this);
		segmentView.addSegment(myArtefactLink, true);
		myMapLink = LinkFactory.createLink("mymaps.menu.title", mainVC, this);
		segmentView.addSegment(myMapLink, false);
		myTaskLink = LinkFactory.createLink("mystructuredmaps.menu.title", mainVC, this);
		segmentView.addSegment(myTaskLink, false);
		publicMapLink = LinkFactory.createLink("othermaps.menu.title", mainVC, this);
		segmentView.addSegment(publicMapLink, false);
		
		doOpenMyArtefacts(ureq);

		MainPanel panel = new MainPanel("portfolio");
		panel.setContent(mainVC);
		putInitialPanel(panel);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == myArtefactLink) {
					doOpenMyArtefacts(ureq);
				} else if (clickedLink == myMapLink) {
					doOpenMyMaps(ureq);
				} else if (clickedLink == myTaskLink) {
					doOpenMyTasks(ureq);
				} else if (clickedLink == publicMapLink) {
					doOpenPublicMaps(ureq);
				}
			}
		}
	}
	
	private void doOpenMyArtefacts(UserRequest ureq) {
		if(artefactsCtrl == null) {
			artefactsCtrl =  new EPArtefactPoolRunController(ureq, getWindowControl());
			listenTo(artefactsCtrl);
		}
		mainVC.put("segmentCmp", artefactsCtrl.getInitialComponent());
	}
	
	private void doOpenMyMaps(UserRequest ureq) {
		if(myMapsCtrl == null) {
			myMapsCtrl = new EPMapRunController(ureq, getWindowControl(), true, EPMapRunViewOption.MY_DEFAULTS_MAPS, null);
			listenTo(myMapsCtrl);
		}
		mainVC.put("segmentCmp", myMapsCtrl.getInitialComponent());
	}
	
	private void doOpenMyTasks(UserRequest ureq) {
		if(myTasksCtrl == null) {
			myTasksCtrl = new EPMapRunController(ureq, getWindowControl(), false, EPMapRunViewOption.MY_EXERCISES_MAPS, null);
			listenTo(myTasksCtrl);
		}
		mainVC.put("segmentCmp", myTasksCtrl.getInitialComponent());
	}
	
	private void doOpenPublicMaps(UserRequest ureq) {
		if(publicMapsCtrl == null) {
			publicMapsCtrl = new EPMapRunController(ureq, getWindowControl(), false, EPMapRunViewOption.OTHERS_MAPS, null);
			listenTo(publicMapsCtrl);
		}
		mainVC.put("segmentCmp", publicMapsCtrl.getInitialComponent());
	}
}
