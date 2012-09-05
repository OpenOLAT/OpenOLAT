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
package org.olat.course.member;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * The members overview.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembersOverviewController extends BasicController implements Activateable2 {
	
	private final Link allMembersLink, ownersLink, tutorsLink, participantsLink, waitingListLink, searchLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private MemberListController allMemberListCtrl;
	private MemberListController ownersCtrl;
	private MemberListController tutorsCtrl;
	private MemberListController participantsCtrl;
	private MemberListController waitingCtrl;
	
	private final RepositoryEntry repoEntry;
	
	public MembersOverviewController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry) {
		super(ureq, wControl);
		this.repoEntry = repoEntry;

		mainVC = createVelocityContainer("members_overview");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
		allMembersLink = LinkFactory.createLink("members.all", mainVC, this);
		segmentView.addSegment(allMembersLink, true);
		ownersLink = LinkFactory.createLink("owners", mainVC, this);
		segmentView.addSegment(ownersLink, false);
		tutorsLink = LinkFactory.createLink("tutors", mainVC, this);
		segmentView.addSegment(tutorsLink, false);
		participantsLink = LinkFactory.createLink("participants", mainVC, this);
		segmentView.addSegment(participantsLink, false);
		waitingListLink = LinkFactory.createLink("waitinglist", mainVC, this);
		segmentView.addSegment(waitingListLink, false);
		searchLink = LinkFactory.createLink("search", mainVC, this);
		segmentView.addSegment(searchLink, false);
		
		updateAllMembers(ureq);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(event instanceof SegmentViewEvent) {
			SegmentViewEvent sve = (SegmentViewEvent)event;
			String segmentCName = sve.getComponentName();
			Component clickedLink = mainVC.getComponent(segmentCName);
			if (clickedLink == allMembersLink) {
				updateAllMembers(ureq);
			} else if (clickedLink == ownersLink){
				updateOwners(ureq);
			} else if (clickedLink == tutorsLink){
				updateTutors(ureq);
			} else if (clickedLink == participantsLink) {
				updateParticipants(ureq);
			} else if (clickedLink == waitingListLink) {
				updateWaitingList(ureq);
			} else if (clickedLink == searchLink) {
				updateSearch(ureq);
			}
		}
	}
	
	private MemberListController updateAllMembers(UserRequest ureq) {
		if(allMemberListCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("AllMembers", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			SearchMembersParams searchParams = new SearchMembersParams(true, true, true, true, true, false);
			allMemberListCtrl = new MemberListController(ureq, bwControl, repoEntry, searchParams);
			listenTo(allMemberListCtrl);
		}
		
		allMemberListCtrl.reloadModel();
		mainVC.put("membersCmp", allMemberListCtrl.getInitialComponent());
		addToHistory(ureq, allMemberListCtrl);
		return allMemberListCtrl;
	}
	
	private MemberListController updateOwners(UserRequest ureq) {
		if(ownersCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Owners", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			SearchMembersParams searchParams = new SearchMembersParams(true, false, false, false, false, false);
			ownersCtrl = new MemberListController(ureq, bwControl, repoEntry, searchParams);
			listenTo(ownersCtrl);
		}
		
		ownersCtrl.reloadModel();
		mainVC.put("membersCmp", ownersCtrl.getInitialComponent());
		addToHistory(ureq, ownersCtrl);
		return ownersCtrl;
	}

	private MemberListController updateTutors(UserRequest ureq) {
		if(tutorsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Tutors", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			SearchMembersParams searchParams = new SearchMembersParams(false, true, false, true, false, false);
			tutorsCtrl = new MemberListController(ureq, bwControl, repoEntry, searchParams);
			listenTo(tutorsCtrl);
		}
		
		tutorsCtrl.reloadModel();
		mainVC.put("membersCmp", tutorsCtrl.getInitialComponent());
		addToHistory(ureq, tutorsCtrl);
		return tutorsCtrl;
	}
	
	private MemberListController updateParticipants(UserRequest ureq) {
		if(participantsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Tutors", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			SearchMembersParams searchParams = new SearchMembersParams(false, false, true, false, true, false);
			participantsCtrl = new MemberListController(ureq, bwControl, repoEntry, searchParams);
			listenTo(participantsCtrl);
		}
		
		participantsCtrl.reloadModel();
		mainVC.put("membersCmp", participantsCtrl.getInitialComponent());
		addToHistory(ureq, participantsCtrl);
		return participantsCtrl;
	}
	
	private MemberListController updateWaitingList(UserRequest ureq) {
		if(waitingCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("WaitingList", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			SearchMembersParams searchParams = new SearchMembersParams(false, false, false, false, false, true);
			waitingCtrl = new MemberListController(ureq, bwControl, repoEntry, searchParams);
			listenTo(waitingCtrl);
		}
		
		waitingCtrl.reloadModel();
		mainVC.put("membersCmp", waitingCtrl.getInitialComponent());
		addToHistory(ureq, waitingCtrl);
		return waitingCtrl;
	}
	
	private void updateSearch(UserRequest ureq) {
		mainVC.put("memberList", new Panel("empty"));
	}
}
