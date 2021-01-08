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
package org.olat.modules.curriculum.ui.member;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.ImportMember_1a_LoginListStep;
import org.olat.course.member.wizard.ImportMember_1b_ChooseMemberStep;
import org.olat.course.member.wizard.MembersByNameContext;
import org.olat.course.member.wizard.MembersContext;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumMembersManagementController extends BasicController implements Activateable2 {

	private final Link ownersLink;
	private final Link coachsLink;
	private final Link masterCoachsLink;
	private final Link allMembersLink;
	private final Link participantsLink;
	private final Link searchLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final TooledStackedPanel toolbarPanel;
	
	private Link addMemberLink;
	private Link importMemberLink;
	
	private StepsMainRunController importMembersWizard;
	
	private boolean managed;
	private boolean overrideManaged;
	private final Curriculum curriculum;
	private CurriculumElement curriculumElement;
	private final CurriculumSecurityCallback secCallback;
	
	private CurriculumMemberListController allMembersCtrl;
	private CurriculumMemberListController ownersCtrl;
	private CurriculumMemberListController coachsCtrl;
	private CurriculumMemberListController masterCoachsCtrl;
	private CurriculumMemberListController participantsCtrl;
	private CurriculumMemberListController searchCtrl;
	
	private CurriculumMemberListController selectedCtrl;

	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumMembersManagementController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Curriculum curriculum, CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
		this.toolbarPanel = toolbarPanel;
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		this.curriculumElement = curriculumElement;
		managed = CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.members);
		
		mainVC = createVelocityContainer("management");
		mainVC.contextPut("title", translate("members.overview.title",
				new String[] { StringHelper.escapeHtml(curriculumElement.getDisplayName()) }));
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
		allMembersLink = LinkFactory.createLink("members.all", mainVC, this);
		segmentView.addSegment(allMembersLink, true);
		ownersLink = LinkFactory.createLink("members.owners", mainVC, this);
		segmentView.addSegment(ownersLink, false);
		masterCoachsLink = LinkFactory.createLink("members.master.coachs", mainVC, this);
		segmentView.addSegment(masterCoachsLink, false);
		coachsLink = LinkFactory.createLink("members.coachs", mainVC, this);
		segmentView.addSegment(coachsLink, false);
		participantsLink = LinkFactory.createLink("members.participants", mainVC, this);
		segmentView.addSegment(participantsLink, false);

		searchLink = LinkFactory.createLink("search", mainVC, this);
		segmentView.addSegment(searchLink, false);
		
		if(!managed && secCallback.canManagerCurriculumElementUsers(curriculumElement)) {
			addMemberLink = LinkFactory.createLink("add.member", "add.member", getTranslator(), mainVC, this, Link.BUTTON);
			addMemberLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
			addMemberLink.setVisible(!managed);

			importMemberLink = LinkFactory.createLink("import.member", "import.member", getTranslator(), mainVC, this, Link.BUTTON);
			importMemberLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");
			importMemberLink.setVisible(!managed);
		}
		
		mainVC.put("segments", segmentView);
		
		putInitialPanel(mainVC);
		selectedCtrl = doAllMembers(ureq);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry currentEntry = entries.get(0);
		String segment = currentEntry.getOLATResourceable().getResourceableTypeName();
		List<ContextEntry> subEntries = entries.subList(1, entries.size());
		selectedCtrl = null;
		if("AllMembers".equalsIgnoreCase(segment)) {
			selectedCtrl = doAllMembers(ureq);
			segmentView.select(allMembersLink);
		} else if("Owners".equalsIgnoreCase(segment)) {
			selectedCtrl = doOwners(ureq);
			segmentView.select(ownersLink);
		} else if("MasterCoachs".equalsIgnoreCase(segment)) {
			selectedCtrl = doMasterCoachs(ureq);
			segmentView.select(masterCoachsLink);
		} else if("Coachs".equalsIgnoreCase(segment)) {
			selectedCtrl = doCoachs(ureq);
			segmentView.select(coachsLink);
		} else if("Participants".equalsIgnoreCase(segment)) {
			selectedCtrl = doParticipants(ureq);
			segmentView.select(participantsLink);
		} else if("Search".equalsIgnoreCase(segment)) {
			doSearch(ureq);
			segmentView.select(searchLink);
		}
		
		if(selectedCtrl != null) {
			selectedCtrl.activate(ureq, subEntries, currentEntry.getTransientState());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == importMembersWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importMembersWizard);
				importMembersWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					if(selectedCtrl != null) {
						selectedCtrl.reloadModel();
					}
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(addMemberLink == source) {
			doChooseMembers(ureq, curriculumElement);
		} else if(importMemberLink == source) {
			doImportMembers(ureq, curriculumElement);
		} else if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if(clickedLink == allMembersLink) {
					selectedCtrl = doAllMembers(ureq);
				} else if (clickedLink == ownersLink) {
					selectedCtrl = doOwners(ureq);
				} else if (clickedLink == masterCoachsLink) {
					selectedCtrl = doMasterCoachs(ureq);
				} else if (clickedLink == coachsLink) {
					selectedCtrl = doCoachs(ureq);
				} else if (clickedLink == participantsLink) {
					selectedCtrl = doParticipants(ureq);
				} else if (clickedLink == searchLink) {
					doSearch(ureq);
				}
			}
		}
	}
	
	private CurriculumMemberListController doAllMembers(UserRequest ureq) {
		if(allMembersCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("All", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			allMembersCtrl = new CurriculumMemberListController(ureq, bwControl, toolbarPanel,
					curriculum, curriculumElement, null, secCallback, false);
			listenTo(allMembersCtrl);
		} else {
			allMembersCtrl.reloadModel();
			addToHistory(ureq, allMembersCtrl);
		}

		mainVC.put("membersCmp", allMembersCtrl.getInitialComponent());
		addToHistory(ureq, allMembersCtrl);
		return allMembersCtrl;
	}
	
	private CurriculumMemberListController doOwners(UserRequest ureq) {
		if(ownersCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Owners", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			List<CurriculumRoles> roles = List.of(CurriculumRoles.curriculumelementowner, CurriculumRoles.owner);
			ownersCtrl = new CurriculumMemberListController(ureq, bwControl, toolbarPanel,
					curriculum, curriculumElement, roles, secCallback, false);
			listenTo(ownersCtrl);
		} else {
			ownersCtrl.reloadModel();
			addToHistory(ureq, ownersCtrl);
		}

		mainVC.put("membersCmp", ownersCtrl.getInitialComponent());
		addToHistory(ureq, ownersCtrl);
		return ownersCtrl;
	}
	
	private CurriculumMemberListController doMasterCoachs(UserRequest ureq) {
		if(masterCoachsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("MasterCoachs", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			masterCoachsCtrl = new CurriculumMemberListController(ureq, bwControl, toolbarPanel,
					curriculum, curriculumElement, List.of(CurriculumRoles.mastercoach), secCallback, false);
			listenTo(masterCoachsCtrl);
		} else {
			masterCoachsCtrl.reloadModel();
			addToHistory(ureq, masterCoachsCtrl);
		}

		mainVC.put("membersCmp", masterCoachsCtrl.getInitialComponent());
		addToHistory(ureq, masterCoachsCtrl);
		return masterCoachsCtrl;
	}
	
	private CurriculumMemberListController doCoachs(UserRequest ureq) {
		if(coachsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Coachs", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			coachsCtrl = new CurriculumMemberListController(ureq, bwControl, toolbarPanel,
					curriculum, curriculumElement, List.of(CurriculumRoles.coach), secCallback, false);
			listenTo(coachsCtrl);
		} else {
			coachsCtrl.reloadModel();
			addToHistory(ureq, coachsCtrl);
		}

		mainVC.put("membersCmp", coachsCtrl.getInitialComponent());
		addToHistory(ureq, coachsCtrl);
		return coachsCtrl;
	}
	
	private CurriculumMemberListController doParticipants(UserRequest ureq) {
		if(participantsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Participants", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			participantsCtrl = new CurriculumMemberListController(ureq, bwControl, toolbarPanel,
					curriculum, curriculumElement, List.of(CurriculumRoles.participant), secCallback, false);
			listenTo(participantsCtrl);
		} else {
			participantsCtrl.reloadModel();
			addToHistory(ureq, participantsCtrl);
		}

		mainVC.put("membersCmp", participantsCtrl.getInitialComponent());
		addToHistory(ureq, participantsCtrl);
		return participantsCtrl;
	}
	
	private CurriculumMemberListController doSearch(UserRequest ureq) {
		if(searchCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			searchCtrl = new CurriculumMemberListController(ureq, bwControl, toolbarPanel,
					curriculum, curriculumElement, null, secCallback, true);
			listenTo(searchCtrl);
		} else {
			addToHistory(ureq, searchCtrl);
		}

		mainVC.put("membersCmp", searchCtrl.getInitialComponent());
		addToHistory(ureq, searchCtrl);
		return searchCtrl;
	}
	
	
	private void doChooseMembers(UserRequest ureq, CurriculumElement focusedElement) {
		removeAsListenerAndDispose(importMembersWizard);

		MembersContext membersContext= MembersContext.valueOf(curriculum, focusedElement, overrideManaged, true);
		Step start = new ImportMember_1b_ChooseMemberStep(ureq, membersContext);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addMembers(uureq, runContext);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_group_import_1_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	private void doImportMembers(UserRequest ureq, CurriculumElement focusedElement) {
		removeAsListenerAndDispose(importMembersWizard);
		
		MembersContext membersContext= MembersContext.valueOf(curriculum, focusedElement, overrideManaged, true);
		Step start = new ImportMember_1a_LoginListStep(ureq, membersContext);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addMembers(uureq, runContext);
			MembersByNameContext membersByNameContext = (MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY);
			if(!membersByNameContext.getNotFoundNames().isEmpty()) {
				String notFoundNames = membersByNameContext.getNotFoundNames().stream()
						.collect(Collectors.joining(", "));
				showWarning("user.notfound", notFoundNames);
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("import.member"), "o_sel_group_import_logins_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	private void addMembers(UserRequest ureq, StepsRunContext runContext) {
		Roles roles = ureq.getUserSession().getRoles();

		Set<Identity> members = ((MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY)).getIdentities();
		MailTemplate template = (MailTemplate)runContext.get("mailTemplate");
		MailPackage mailing = new MailPackage(template, getWindowControl().getBusinessControl().getAsString(), template != null);
		MemberPermissionChangeEvent changes = (MemberPermissionChangeEvent)runContext.get("permissions");
		List<CurriculumElementMembershipChange> curriculumChanges = changes.generateCurriculumElementMembershipChange(members);
		curriculumService.updateCurriculumElementMemberships(getIdentity(), roles, curriculumChanges, mailing);
		MailHelper.printErrorsAndWarnings(mailing.getResult(), getWindowControl(), false, getLocale());
		
		if(selectedCtrl != null) {
			selectedCtrl.reloadModel();
		}
	}
	
}
