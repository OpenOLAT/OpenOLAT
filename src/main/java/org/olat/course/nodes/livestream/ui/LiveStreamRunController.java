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

import org.olat.commons.calendar.ui.WeeklyCalendarController;
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
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.nodes.livestream.LiveStreamSecurityCallback;
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamRunController extends BasicController {
	
	private static final String PLAY_RES_TYPE = "streams";
	private static final String STATISTIC_RES_TYPE = "statistic";
	private static final String EDIT_RES_TYPE = "edit";
	
	private VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	private Link streamsLink;
	private Link statisticLink;
	private Link editLink;
	
	private LiveStreamsController streamsCtrl;
	private LiveStreamStatisticController statisticCtrl;
	private WeeklyCalendarController editCtrl;
	
	private final ModuleConfiguration moduleConfiguration;
	private final String courseNodeIdent;
	private final UserCourseEnvironment userCourseEnv;
	private final CourseCalendars calendars;
	
	@Autowired
	private LiveStreamService liveStreamService;

	public LiveStreamRunController(UserRequest ureq, WindowControl wControl, CourseNode coureNode,
			UserCourseEnvironment userCourseEnv, LiveStreamSecurityCallback secCallback, CourseCalendars calendars) {
		super(ureq, wControl);
		this.moduleConfiguration = coureNode.getModuleConfiguration();
		this.courseNodeIdent = coureNode.getIdent();
		this.userCourseEnv = userCourseEnv;
		this.calendars = calendars;
		
		mainVC = createVelocityContainer("run");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		if (secCallback.canViewStreams()) {
			streamsLink = LinkFactory.createLink("run.streams", mainVC, this);
			segmentView.addSegment(streamsLink, true);
		}
		if (secCallback.canViewStatistic()) {
			statisticLink = LinkFactory.createLink("run.statistic", mainVC, this);
			segmentView.addSegment(statisticLink, true);
		}
		if (secCallback.canEditStreams()) {
			editLink = LinkFactory.createLink("run.edit.events", mainVC, this);
			segmentView.addSegment(editLink, false);
		}
		// segmentView.setDontShowSingleSegment(true); // adds some ugly space
		boolean segmentViewVisible = segmentView.getSegments().size() > 1;
		segmentView.setVisible(segmentViewVisible);

		doOpenStreams(ureq);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == streamsLink) {
					doOpenStreams(ureq);
				} else if (clickedLink == statisticLink){
					doOpenStatistic(ureq);
				} else if (clickedLink == editLink){
					doOpenEdit(ureq);
				}
			}
		}
	}
	
	private void doOpenStreams(UserRequest ureq) {
		if (streamsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(PLAY_RES_TYPE), null);
			streamsCtrl = new LiveStreamsController(ureq, swControl, moduleConfiguration, calendars);
			listenTo(streamsCtrl);
		} else {
			streamsCtrl.refreshData(ureq.getUserSession());
			addToHistory(ureq, streamsCtrl);
		}
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		liveStreamService.createLaunch(courseEntry, courseNodeIdent, getIdentity());
		segmentView.select(streamsLink);
		mainVC.put("segmentCmp", streamsCtrl.getInitialComponent());
	}
	
	private void doOpenStatistic(UserRequest ureq) {
		if (statisticCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(STATISTIC_RES_TYPE), null);
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			statisticCtrl = new LiveStreamStatisticController(ureq, swControl, courseEntry , moduleConfiguration,
					calendars);
			listenTo(statisticCtrl);
		} else {
			statisticCtrl.refreshData();
			addToHistory(ureq, statisticCtrl);
		}
		segmentView.select(statisticLink);
		mainVC.put("segmentCmp", statisticCtrl.getInitialComponent());
	}
	
	private void doOpenEdit(UserRequest ureq) {
		if (editCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(EDIT_RES_TYPE), null);
			OLATResource courseOres = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseResource();
			editCtrl = new WeeklyCalendarController(ureq, swControl, calendars.getCalendars(),
					WeeklyCalendarController.CALLER_LIVE_STREAM, courseOres, false);
			editCtrl.setDifferentiateManagedEvent(true);
			editCtrl.setDifferentiateLiveStreams(true);
			listenTo(editCtrl);
		} else {
			addToHistory(ureq, editCtrl);
		}
		segmentView.select(editLink);
		mainVC.put("segmentCmp", editCtrl.getInitialComponent());
	}

}
