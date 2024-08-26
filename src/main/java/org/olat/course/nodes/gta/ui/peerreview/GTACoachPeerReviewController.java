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
package org.olat.course.nodes.gta.ui.peerreview;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * 
 * Initial date: 7 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachPeerReviewController extends BasicController {
	
	private final Link awardedLink;
	private final Link receivedLink;
	private final VelocityContainer mainVC;
	private final BreadcrumbPanel stackPanel;
	private final SegmentViewComponent segmentView;
	
	private Task assignedTask;
	private Identity assessedIdentity;
	
	private final TaskList taskList;
	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	
	private GTACoachPeerReviewAwardedListController awardedListCtrl;
	private GTACoachPeerReviewReceivedListController receivedListCtrl;
	
	public GTACoachPeerReviewController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			TaskList taskList, Task assignedTask, Identity assessedIdentity, CourseEnvironment courseEnv,
			GTACourseNode gtaNode) {
		super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
		this.assessedIdentity = assessedIdentity;
		this.assignedTask = assignedTask;
		this.stackPanel = stackPanel;
		this.courseEnv = courseEnv;
		this.taskList = taskList;
		this.gtaNode = gtaNode;
		
		mainVC = createVelocityContainer("coach_peer_review");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
		awardedLink = LinkFactory.createLink("coach.segment.awarded", mainVC, this);
		segmentView.addSegment(awardedLink, true);
		receivedLink = LinkFactory.createLink("coach.segment.received", mainVC, this);
		segmentView.addSegment(receivedLink, false);
		doOpenAwardedPeerReview(ureq);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView
				&& (event instanceof SegmentViewEvent sve)) {
			String segmentCName = sve.getComponentName();
			Component clickedLink = mainVC.getComponent(segmentCName);
			if (clickedLink == awardedLink) {
				doOpenAwardedPeerReview(ureq);
			} else if (clickedLink == receivedLink){
				doOpenReceivedPeerReview(ureq);
			}
		}
	}
	
	private GTACoachPeerReviewAwardedListController doOpenAwardedPeerReview(UserRequest ureq) {
		if(awardedListCtrl == null) {
			awardedListCtrl = new GTACoachPeerReviewAwardedListController(ureq, getWindowControl(),
					taskList, assessedIdentity, courseEnv, gtaNode);
			listenTo(awardedListCtrl);
		}
		mainVC.put("segmentCmp", awardedListCtrl.getInitialComponent());
		return awardedListCtrl;
	}
	
	private GTACoachPeerReviewReceivedListController doOpenReceivedPeerReview(UserRequest ureq) {
		if(receivedListCtrl == null) {
			receivedListCtrl = new GTACoachPeerReviewReceivedListController(ureq, getWindowControl(), stackPanel,
					taskList, assessedIdentity, assignedTask, courseEnv, gtaNode);
			listenTo(receivedListCtrl);
		}
		mainVC.put("segmentCmp", receivedListCtrl.getInitialComponent());
		return receivedListCtrl;
	}
}
