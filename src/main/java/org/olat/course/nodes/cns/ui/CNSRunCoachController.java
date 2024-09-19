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
package org.olat.course.nodes.cns.ui;

import java.util.List;

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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.CNSCourseNode;
import org.olat.course.nodes.cns.manager.CNSPreviewEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 10 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSRunCoachController extends BasicController implements Activateable2 {
	
	private static final String ORES_TYPE_PARTICIPANTS = "Participants";
	private static final String ORES_TYPE_PREVIEW = "Preview";
	
	private CNSParticipantListController participantsCtrl;
	private CNSSelectionController previewCtrl;
	
	private final VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	private Link participantsLink;
	private Link previewLink;

	private final CNSCourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	
	public CNSRunCoachController(UserRequest ureq, WindowControl wControl, CNSCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		
		mainVC = createVelocityContainer("segments");
		putInitialPanel(mainVC);
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
//		participantsLink = LinkFactory.createLink("participants.title", mainVC, this);
//		segmentView.addSegment(participantsLink, false);
		
		previewLink = LinkFactory.createLink("preview", mainVC, this);
		segmentView.addSegment(previewLink, false);
		
//		doOpenParticipants(ureq);
		doOpenPreview(ureq);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (ORES_TYPE_PARTICIPANTS.equalsIgnoreCase(type)) {
			doOpenParticipants(ureq);
		} else if (ORES_TYPE_PREVIEW.equalsIgnoreCase(type)) {
			doOpenPreview(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == participantsLink) {
					doOpenParticipants(ureq);
				} else if (clickedLink == previewLink) {
					doOpenPreview(ureq);
				}
			}
		} 
	}
	
	private void doOpenParticipants(UserRequest ureq) {
		if (participantsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PARTICIPANTS), null);
			participantsCtrl = new CNSParticipantListController(ureq, bwControl);
			listenTo(participantsCtrl);
		} else {
			participantsCtrl.reload(ureq);
		}
		mainVC.put("segmentCmp", participantsCtrl.getInitialComponent());
		segmentView.select(participantsLink);
	}
	
	private void doOpenPreview(UserRequest ureq) {
		if (previewCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PREVIEW), null);
			previewCtrl = new CNSSelectionController(ureq, bwControl, courseNode, userCourseEnv, new CNSPreviewEnvironment());
			listenTo(previewCtrl);
		}
		mainVC.put("segmentCmp", previewCtrl.getInitialComponent());
		segmentView.select(previewLink);
	}

}
