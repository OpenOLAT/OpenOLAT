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
package org.olat.modules.lecture.ui;

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
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureRepositoryAdminController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link lecturesLink, settingsLink, participantsLink;
	
	private LectureListRepositoryController lecturesCtrl;
	private LectureRepositorySettingsController settingsCtrl;
	private ParticipantListRepositoryController participantsCtrl;
	
	private RepositoryEntry entry;
	
	public LectureRepositoryAdminController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;
		
		mainVC = createVelocityContainer("admin_repository");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		lecturesLink = LinkFactory.createLink("repo.lectures.block", mainVC, this);
		segmentView.addSegment(lecturesLink, true);
		participantsLink = LinkFactory.createLink("repo.participants", mainVC, this);
		segmentView.addSegment(participantsLink, false);
		settingsLink = LinkFactory.createLink("repo.settings", mainVC, this);
		segmentView.addSegment(settingsLink, false);
		
		doOpenLectures(ureq);
		putInitialPanel(mainVC);
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
				if (clickedLink == lecturesLink) {
					doOpenLectures(ureq);
				} else if (clickedLink == settingsLink){
					doOpenSettings(ureq);
				} else if(clickedLink == participantsLink) {
					doOpenParticipants(ureq);
				}
			}
		}
	}

	private void doOpenLectures(UserRequest ureq) {
		if(lecturesCtrl == null) {
			lecturesCtrl = new LectureListRepositoryController(ureq, getWindowControl(), entry);
			listenTo(lecturesCtrl);
		}
		mainVC.put("segmentCmp", lecturesCtrl.getInitialComponent());
	}
	
	private void doOpenSettings(UserRequest ureq) {
		if(settingsCtrl == null) {
			settingsCtrl = new LectureRepositorySettingsController(ureq, getWindowControl(), entry);
			listenTo(settingsCtrl);
		}
		mainVC.put("segmentCmp", settingsCtrl.getInitialComponent());
	}
	
	private void doOpenParticipants(UserRequest ureq) {
		if(participantsCtrl == null) {
			participantsCtrl = new ParticipantListRepositoryController(ureq, getWindowControl(), entry);
			listenTo(participantsCtrl);
		}
		mainVC.put("segmentCmp", participantsCtrl.getInitialComponent());
	}
}