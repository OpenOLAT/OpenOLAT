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
package org.olat.course.nodes.topicbroker.ui;

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
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.TopicBrokerCourseNode;
import org.olat.course.nodes.topicbroker.TBCourseNodeSecurityCallbackFactory;
import org.olat.course.nodes.topicbroker.TopicBrokerCourseNodeGroupRestrictionCandidates;
import org.olat.course.nodes.topicbroker.TopicBrokerCourseNodeParticipantCandidates;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBGroupRestrictionCandidates;
import org.olat.modules.topicbroker.TBParticipantCandidates;
import org.olat.modules.topicbroker.TBSecurityCallback;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.TBParticipantListController;
import org.olat.modules.topicbroker.ui.TBTopicListController;
import org.olat.modules.topicbroker.ui.TBTopicSelectionsController;
import org.olat.modules.topicbroker.ui.TBUIFactory;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBRunCoachController extends BasicController implements Activateable2 {
	
	private static final String ORES_TYPE_PARTICIPANTS = "Participants";
	private static final String ORES_TYPE_TOPICS = "Topics";
	
	private TBParticipantListController participantsCtrl;
	private TBTopicListController topicsCtrl;
	
	private final VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	private Link participantsLink;
	private Link topicsLink;

	private final TBBroker broker;
	private final TBSecurityCallback secCallback;
	private final TBParticipantCandidates participantCandidates;
	private final TBGroupRestrictionCandidates groupRestrictionCandidates;
	
	@Autowired
	private TopicBrokerService topicBrokerService;

	public TBRunCoachController(UserRequest ureq, WindowControl wControl, TopicBrokerCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TBUIFactory.class, getLocale(), getTranslator()));
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		broker = topicBrokerService.getOrCreateBroker(getIdentity(),
				courseEntry, courseNode.getIdent());
		
		secCallback = TBCourseNodeSecurityCallbackFactory.createSecurityCallback(courseNode, userCourseEnv);
		participantCandidates = new TopicBrokerCourseNodeParticipantCandidates(
				getIdentity(), userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				userCourseEnv.isAdmin());
		groupRestrictionCandidates = new TopicBrokerCourseNodeGroupRestrictionCandidates(
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		
		mainVC = createVelocityContainer("segments");
		putInitialPanel(mainVC);
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
		participantsLink = LinkFactory.createLink("participants.title", mainVC, this);
		segmentView.addSegment(participantsLink, false);
		
		topicsLink = LinkFactory.createLink("topics.title", mainVC, this);
		segmentView.addSegment(topicsLink, false);
		
		doOpenParticipants(ureq);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (ORES_TYPE_PARTICIPANTS.equalsIgnoreCase(type)) {
			doOpenParticipants(ureq);
		} else if (ORES_TYPE_TOPICS.equalsIgnoreCase(type)) {
			doOpenTopics(ureq);
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
				} else if (clickedLink == topicsLink) {
					doOpenTopics(ureq);
				}
			}
		} 
	}
	
	private void doOpenParticipants(UserRequest ureq) {
		if (participantsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PARTICIPANTS), null);
			participantsCtrl = new TBParticipantListController(ureq, bwControl, broker, secCallback, participantCandidates);
			listenTo(participantsCtrl);
		} else {
			participantsCtrl.reload(ureq);
		}
		mainVC.put("segmentCmp", participantsCtrl.getInitialComponent());
		segmentView.select(participantsLink);
	}
	
	private void doOpenTopics(UserRequest ureq) {
		if (topicsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_TOPICS), null);
			topicsCtrl = new TBTopicSelectionsController(ureq, bwControl, broker, secCallback, participantCandidates, groupRestrictionCandidates);
			listenTo(topicsCtrl);
		} else {
			topicsCtrl.reload(ureq);
		}
		mainVC.put("segmentCmp", topicsCtrl.getInitialComponent());
		segmentView.select(topicsLink);
	}

}
