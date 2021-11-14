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
package org.olat.course.nodes.gta.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.model.Membership;
import org.olat.course.reminder.ui.CourseNodeReminderRunController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTARunController extends BasicController implements Activateable2 {
	
	private GTAParticipantController runCtrl;
	private GTACoachSelectionController coachCtrl;
	private GTACoachSelectionController markedCtrl;
	private GTACoachManagementController manageCtrl;
	private CourseNodeReminderRunController remindersCtrl;

	private Link runLink;
	private Link coachLink;
	private Link markedLink;
	private Link manageLink;
	private Link remindersLink;
	private VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	
	private final GTACourseNode gtaNode;
	private final UserCourseEnvironment userCourseEnv;
	
	@Autowired
	private GTAManager gtaManager;
	
	public GTARunController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.userCourseEnv = userCourseEnv;
		
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Membership membership = gtaManager.getMembership(getIdentity(), entry, gtaNode);
		if((membership.isCoach() && userCourseEnv.isCoach()) || userCourseEnv.isAdmin()) {
			mainVC = createVelocityContainer("run_segments");

			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			markedLink = LinkFactory.createLink("run.coach.marked", mainVC, this);
			segmentView.addSegment(markedLink, false);
			coachLink = LinkFactory.createLink("run.coach.all", mainVC, this);
			segmentView.addSegment(coachLink, true);
			if(isManagementTabAvalaible(config)) {
				manageLink = LinkFactory.createLink("run.manage.coach", mainVC, this);
				segmentView.addSegment(manageLink, false);
			}
			
			if (userCourseEnv.isAdmin() && !userCourseEnv.isCourseReadOnly()) {
				WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Reminders"), null);
				remindersCtrl = new CourseNodeReminderRunController(ureq, swControl, entry, gtaNode.getReminderProvider(false));
				listenTo(remindersCtrl);
				if (remindersCtrl.hasDataOrActions()) {
					remindersLink = LinkFactory.createLink("run.reminders", mainVC, this);
					segmentView.addSegment(remindersLink, false);
				}
			}
			
			doOpenSelectionList(ureq);
			mainVC.put("segments", segmentView);
			putInitialPanel(mainVC);
		} else if(membership.isParticipant() && userCourseEnv.isParticipant()) {
			createRun(ureq);
			putInitialPanel(runCtrl.getInitialComponent());
		} else {
			String title = translate("error.not.member.title");
			String message = translate("error.not.member.message");
			Controller msgCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
			listenTo(msgCtrl);
			putInitialPanel(msgCtrl.getInitialComponent());
		}
	}
	
	private boolean isManagementTabAvalaible(ModuleConfiguration config) {
		return (userCourseEnv.isAdmin()
				|| (userCourseEnv.isCoach() && config.getBooleanSafe(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, false)))
				&& (config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT) || config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION));
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("coach".equalsIgnoreCase(type)) {
			if(coachLink != null || coachCtrl != null) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doOpenCoach(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
				if(segmentView != null) {
					segmentView.select(coachLink);
				}
			}
		} else if("marked".equalsIgnoreCase(type)) {
			if(markedLink != null) {
				doOpenMarked(ureq);
				if(segmentView != null) {
					segmentView.select(markedLink);
				}
			}
		} else if("management".equalsIgnoreCase(type)) {
			if(manageLink != null) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doManage(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
				if(segmentView != null) {
					segmentView.select(manageLink);
				}
			}
		} else if("Reminders".equalsIgnoreCase(type)) {
			if(remindersLink != null) {
				doOpenReminders(ureq);
				if(segmentView != null) {
					segmentView.select(remindersLink);
				}
			}
		} else if("identity".equalsIgnoreCase(type)) {
			if(getIdentity().getKey().equals(entries.get(0).getOLATResourceable().getResourceableId())) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doOpenRun(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
				if(segmentView != null) {
					segmentView.select(runLink);
				}
			}
		} else {
			if("CourseNode".equalsIgnoreCase(entries.get(0).getOLATResourceable().getResourceableTypeName())) {
				state = entries.get(0).getTransientState();
				entries = entries.subList(1, entries.size());
			}
			
			if(runCtrl != null && segmentView == null) {
				runCtrl.activate(ureq, entries, state);
			}
			
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(runCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == runLink) {
					doOpenRun(ureq);
				} else if (clickedLink == coachLink) {
					doOpenCoach(ureq);
				} else if (clickedLink == markedLink) {
					doOpenMarked(ureq);
				} else if(clickedLink == manageLink) {
					doManage(ureq);
				} else if (clickedLink == remindersLink) {
					doOpenReminders(ureq);
				}
			}
		}
	}
	
	private void doOpenSelectionList(UserRequest ureq) {
		RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		boolean hasMarks = gtaManager.hasMarks(entry, gtaNode, getIdentity());
		if (hasMarks) {
			doOpenMarked(ureq);
			if(segmentView != null) {
				segmentView.select(markedLink);
			}
		} else {
			doOpenCoach(ureq);
			if(segmentView != null) {
				segmentView.select(coachLink);
			}
		}
	}
	
	private Activateable2 doOpenRun(UserRequest ureq) {
		if(runCtrl == null) {
			createRun(ureq);
		}
		addToHistory(ureq, runCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", runCtrl.getInitialComponent());
		}
		return runCtrl;
	}

	private Activateable2 doOpenMarked(UserRequest ureq) {
		if(markedCtrl == null) {
			createMarked(ureq);
		} else {
			markedCtrl.reload(ureq);
		}
		addToHistory(ureq, markedCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", markedCtrl.getInitialComponent());
		}
		return markedCtrl;
	}	

	private Activateable2 doOpenCoach(UserRequest ureq) {
		if(coachCtrl == null) {
			createCoach(ureq);
		} else {
			coachCtrl.reload(ureq);
		}
		addToHistory(ureq, coachCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", coachCtrl.getInitialComponent());
		}
		return coachCtrl;
	}
	
	private GTACoachManagementController doManage(UserRequest ureq) {
		if(manageCtrl == null) {
			createManage(ureq);
		}
		addToHistory(ureq, manageCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", manageCtrl.getInitialComponent());
		}
		return manageCtrl;
	}
	
	private GTAParticipantController createRun(UserRequest ureq) {
		removeAsListenerAndDispose(runCtrl);
		
		runCtrl = new GTAParticipantController(ureq, getWindowControl(), gtaNode, userCourseEnv);
		listenTo(runCtrl);
		return runCtrl;
	}
	
	private GTACoachSelectionController createMarked(UserRequest ureq) {
		removeAsListenerAndDispose(markedCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("marked"), null);
		markedCtrl = new GTACoachSelectionController(ureq, swControl, userCourseEnv, gtaNode, true);
		listenTo(markedCtrl);
		return coachCtrl;
	}
	
	private GTACoachSelectionController createCoach(UserRequest ureq) {
		removeAsListenerAndDispose(coachCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("coach"), null);
		coachCtrl = new GTACoachSelectionController(ureq, swControl, userCourseEnv, gtaNode, false);
		listenTo(coachCtrl);
		return coachCtrl;
	}
	
	private GTACoachManagementController createManage(UserRequest ureq) {
		removeAsListenerAndDispose(manageCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("management"), null);
		manageCtrl = new GTACoachManagementController(ureq, swControl, userCourseEnv, gtaNode);
		listenTo(manageCtrl);
		return manageCtrl;
	}
	
	private void doOpenReminders(UserRequest ureq) {
		if (remindersLink != null) {
			remindersCtrl.reload(ureq);
			mainVC.put("segmentCmp", remindersCtrl.getInitialComponent());
		}
	}
}