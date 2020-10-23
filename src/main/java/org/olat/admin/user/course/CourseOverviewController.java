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
package org.olat.admin.user.course;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.admin.user.course.CourseOverviewMembershipDataModel.MSCols;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.CourseModule;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.manager.MemberViewQueries;
import org.olat.group.model.MemberView;
import org.olat.group.ui.main.CourseMembership;
import org.olat.group.ui.main.CourseRoleCellRenderer;
import org.olat.group.ui.main.EditSingleMembershipController;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositoryEntryFilter;
import org.olat.repository.controllers.RepositorySearchController.Can;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.olat.repository.ui.author.TypeRenderer;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseOverviewController extends FormBasicController  {

	private static final String TABLE_ACTION_LAUNCH = "reTblLaunch";
	private static final String TABLE_ACTION_EDIT = "edit";
	private static final String TABLE_ACTION_UNSUBSCRIBE = "unsubscribe";
	
	private FormLink addAsOwner;
	private FormLink addAsTutor;
	private FormLink addAsParticipant;
	private FormLink leaveButton;
	private FlexiTableElement tableEl;
	private CourseOverviewMembershipDataModel tableDataModel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmSendMailBox;
	private CourseLeaveDialogBoxController removeFromCourseDlg;
	private ReferencableEntriesSearchController repoSearchCtr;
	private EditSingleMembershipController editSingleMemberCtrl;
	
	private final boolean canModify;
	private final Identity editedIdentity;
	private final boolean isLastVisitVisible;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MemberViewQueries memberQueries;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private UserCourseInformationsManager userInfosMgr;
	
	public CourseOverviewController(UserRequest ureq, WindowControl wControl, Identity identity, boolean canModify) {
		super(ureq, wControl, "courseoverview", Util.createPackageTranslator(CourseMembership.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		editedIdentity = identity;
		this.canModify = canModify;
		isLastVisitVisible = securityModule.isUserLastVisitVisible(ureq.getUserSession().getRoles());
		
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MSCols.key, TABLE_ACTION_LAUNCH));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MSCols.entry, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MSCols.title, TABLE_ACTION_LAUNCH));
		if(repositoryModule.isManagedRepositoryEntries()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MSCols.externalId, TABLE_ACTION_LAUNCH));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MSCols.externalRef, TABLE_ACTION_LAUNCH));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MSCols.role, new CourseRoleCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MSCols.firstTime));
		if(isLastVisitVisible) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MSCols.lastTime));
		}
		if(canModify) {
			DefaultFlexiColumnModel editCol = new DefaultFlexiColumnModel("table.header.edit", translate("table.header.edit"), TABLE_ACTION_EDIT);
			editCol.setAlwaysVisible(true);
			editCol.setExportable(false);
			columnsModel.addFlexiColumnModel(editCol);
			DefaultFlexiColumnModel leaveCol = new DefaultFlexiColumnModel(MSCols.allowLeave.i18nHeaderKey(),
					MSCols.allowLeave.ordinal(), TABLE_ACTION_UNSUBSCRIBE,
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(MSCols.allowLeave.i18nHeaderKey()), TABLE_ACTION_UNSUBSCRIBE), null));
			leaveCol.setAlwaysVisible(true);
			leaveCol.setExportable(false);
			columnsModel.addFlexiColumnModel(leaveCol);
		}

		tableDataModel = new CourseOverviewMembershipDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table.courses", tableDataModel, 24, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_coursetable");
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(MSCols.entry.sortKey(), true));
		tableEl.setSortSettings(options);
		tableEl.setAndLoadPersistedPreferences(ureq, "course-overview-v2");
		
		if(canModify) {
			tableEl.setMultiSelect(true);
			leaveButton = uifactory.addFormLink("table.header.leave", formLayout, Link.BUTTON);
			
			addAsOwner = uifactory.addFormLink("add.course.owner", formLayout, Link.BUTTON);
			addAsTutor = uifactory.addFormLink("add.course.tutor", formLayout, Link.BUTTON);
			addAsParticipant = uifactory.addFormLink("add.course.participant", formLayout, Link.BUTTON);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	private void updateModel() {
		List<MemberView> memberships = memberQueries.getIdentityMemberships(editedIdentity);

		Map<OLATResource,CourseMemberView> resourceToViewMap = new HashMap<>();
		for(MemberView membership: memberships) {
			resourceToViewMap.put(membership.getOLATResource(), new CourseMemberView(membership));
		}
		
		List<UserCourseInformations> userCourseInfos = userInfosMgr.getUserCourseInformations(editedIdentity);
		for(UserCourseInformations userCourseInfo:userCourseInfos) {
			CourseMemberView view = resourceToViewMap.get(userCourseInfo.getResource());
			if(view != null) {
				view.setFirstTime(userCourseInfo.getInitialLaunch());
				view.setLastTime(userCourseInfo.getRecentLaunch());
			}
		}

		List<CourseMemberView> views = new ArrayList<>(resourceToViewMap.values());
		tableDataModel.setObjects(views);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addAsOwner) {
			doSearchRepoEntries(ureq, SearchType.owner, translate("add.course.owner"));
		} else if(source == addAsTutor) {
			doSearchRepoEntries(ureq, SearchType.tutor, translate("add.course.tutor"));
		} else if(source == addAsParticipant) {
			doSearchRepoEntries(ureq, SearchType.participant, translate("add.course.participant"));
		} else if(source == leaveButton) {
			List<CourseMemberView> items = getSelectedViews();
			tableEl.getMultiSelectedIndex();
			doLeave(ureq, items);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if (TABLE_ACTION_LAUNCH.equals(se.getCommand())) {
					CourseMemberView item = tableDataModel.getObject(se.getIndex());
					launch(ureq, item.getRepoKey());
				} else if (TABLE_ACTION_UNSUBSCRIBE.equals(se.getCommand())) {
					CourseMemberView item = tableDataModel.getObject(se.getIndex());
					doLeave(ureq, Collections.singletonList(item));
				} else if (TABLE_ACTION_EDIT.equals(se.getCommand())) {
					CourseMemberView item = tableDataModel.getObject(se.getIndex());
					doOpenEdit(ureq, item);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == repoSearchCtr) {
			SearchType type = (SearchType)repoSearchCtr.getUserObject();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				// repository search controller done
				RepositoryEntry re = repoSearchCtr.getSelectedEntry();
				removeAsListenerAndDispose(repoSearchCtr);
				cmc.deactivate();
				doConfirmSendEmail(ureq, Collections.singletonList(re), type);
			} else if(event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) {
				// repository search controller done
				List<RepositoryEntry> res = repoSearchCtr.getSelectedEntries();
				removeAsListenerAndDispose(repoSearchCtr);
				cmc.deactivate();
				if (res != null && !res.isEmpty()) {
					doConfirmSendEmail(ureq, res, type);
				}
			}
		} else if(source == editSingleMemberCtrl) {
			cmc.deactivate();
			if(event instanceof MemberPermissionChangeEvent) {
				MemberPermissionChangeEvent e = (MemberPermissionChangeEvent)event;
				RepositoryEntry re = editSingleMemberCtrl.getRepositoryEntry();
				doConfirmChangePermission(ureq, e, re);
			}
		} else if(source == confirmSendMailBox) {
			boolean sendMail = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			Object confirmation = confirmSendMailBox.getUserObject();
			if(confirmation instanceof ConfirmAdd) {
				ConfirmAdd addInfos = (ConfirmAdd)confirmation;
				doAddToRepositoryEntry(ureq, addInfos.getEntries(), addInfos.getType(), sendMail);
			} else if(confirmation instanceof ConfirmEdit) {
				ConfirmEdit editInfos = (ConfirmEdit)confirmation;
				doChangePermission(ureq, editInfos.getChangeEvent(), editInfos.getEntry(), sendMail);
			}
			updateModel();
		} else if(source == removeFromCourseDlg) {
			if(event == Event.DONE_EVENT) {
				boolean sendMail = removeFromCourseDlg.isSendMail();
				List<BusinessGroup> groupsToDelete = removeFromCourseDlg.getGroupsToDelete();
				List<BusinessGroup> groupsToLeave = removeFromCourseDlg.getGroupsToLeave();
				List<RepositoryEntry> repoEntriesToLeave = removeFromCourseDlg.getRepoEntriesToLeave();
				removeUserFromCourse(ureq, repoEntriesToLeave, groupsToLeave, groupsToDelete, sendMail);
			}
			
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(removeFromCourseDlg);
		removeAsListenerAndDispose(repoSearchCtr);
		removeAsListenerAndDispose(cmc);
		removeFromCourseDlg = null;
		repoSearchCtr = null;
		cmc = null;
	}
	
	public List<CourseMemberView> getSelectedViews() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<CourseMemberView> selectedViews = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			CourseMemberView view = tableDataModel.getObject(selectedIndex.intValue());
			if(view != null) {
				selectedViews.add(view);
			}
		}
 		return selectedViews;
	}
	
	private void launch(UserRequest ureq, Long repoKey) {
		NewControllerFactory.getInstance().launch("[RepositoryEntry:" + repoKey + "]", ureq, getWindowControl());
	}
	
	private void doOpenEdit(UserRequest ureq, CourseMemberView member) {
		RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(member.getRepoKey());
		editSingleMemberCtrl = new EditSingleMembershipController(ureq, getWindowControl(), editedIdentity, repoEntry, null, true, false);
		listenTo(editSingleMemberCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editSingleMemberCtrl.getInitialComponent(),
				true, translate("edit.member"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmChangePermission(UserRequest ureq, MemberPermissionChangeEvent e, RepositoryEntry re) {
		if(e.size() == 0) {
			//nothing to do
			return;
		}

		boolean mailMandatory = repositoryModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
		if(mailMandatory) {
			doChangePermission(ureq, e, re, true);
		} else {
			confirmSendMailBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.send.mail"), confirmSendMailBox);
			confirmSendMailBox.setUserObject(new ConfirmEdit(e, re));
		}
	}
	
	private void doChangePermission(UserRequest ureq, MemberPermissionChangeEvent e, RepositoryEntry re, boolean sendMail) {
		MailPackage mailing = new MailPackage(sendMail);
		if(re != null) {
			Roles roles = ureq.getUserSession().getRoles();
			List<RepositoryEntryPermissionChangeEvent> changes = Collections.singletonList((RepositoryEntryPermissionChangeEvent)e);
			repositoryManager.updateRepositoryEntryMemberships(getIdentity(), roles, re, changes, mailing);
			
			curriculumService.updateCurriculumElementMemberships(getIdentity(), roles, e.getCurriculumChanges(), mailing);
		}

		businessGroupService.updateMemberships(getIdentity(), e.getGroupChanges(), mailing);
		//make sure all is committed before loading the model again (I see issues without)
		dbInstance.commitAndCloseSession();
		updateModel();
	}
	
	private void doSearchRepoEntries(UserRequest ureq, SearchType type, String title) {
		removeAsListenerAndDispose(repoSearchCtr);
		removeAsListenerAndDispose(cmc);
		
		RepositoryEntryFilter filter = new ManagedEntryfilter();
		repoSearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				new String[]{ CourseModule.getCourseTypeName() }, filter, null,
				translate("choose"), false, false, true, false, true, Can.all);
		repoSearchCtr.setUserObject(type);
		listenTo(repoSearchCtr);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), repoSearchCtr.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmSendEmail(UserRequest ureq, Collection<RepositoryEntry> res, SearchType type) {
		boolean mailMandatory = repositoryModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
		if(mailMandatory) {
			doAddToRepositoryEntry(ureq, res, type, false);
			updateModel();
		} else {
			confirmSendMailBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.send.mail"), confirmSendMailBox);
			confirmSendMailBox.setUserObject(new ConfirmAdd(type, res));
		}
	}
	
	private void doAddToRepositoryEntry(UserRequest ureq, Collection<RepositoryEntry> res, SearchType type, boolean sendEmail) {
		//commit changes to the repository entry
		MailerResult result = new MailerResult();
		MailPackage reMailing = new MailPackage(result, getWindowControl().getBusinessControl().getAsString(), sendEmail);
		
		RepositoryEntryPermissionChangeEvent changeEvent = new RepositoryEntryPermissionChangeEvent(editedIdentity);
		switch(type) {
			case owner: changeEvent.setRepoOwner(Boolean.TRUE); break;
			case tutor: changeEvent.setRepoTutor(Boolean.TRUE); break;
			case participant: changeEvent.setRepoParticipant(Boolean.TRUE); break;
		}
		List<RepositoryEntryPermissionChangeEvent> repoChanges = Collections.singletonList(changeEvent);
		for(RepositoryEntry repoEntry:res) {
			repositoryManager.updateRepositoryEntryMemberships(getIdentity(), ureq.getUserSession().getRoles(), repoEntry, repoChanges, reMailing);
		}
		
		//make sure all is committed before loading the model again (I see issues without)
		dbInstance.commitAndCloseSession();
		
		// print errors
		Roles roles = ureq.getUserSession().getRoles();
		boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
		MailHelper.printErrorsAndWarnings(result, getWindowControl(), detailedErrorOutput, getLocale());
	}

	/**
	 * The method check the managed flags
	 * @param ureq
	 * @param views
	 */
	private void doLeave(UserRequest ureq, Collection<CourseMemberView> selectedViews) {
		List<CourseMemberView> views = selectedViews.stream()
				.filter(view -> !view.isFullyManaged())
				.collect(Collectors.toList());

		if(views.isEmpty()) {
			boolean groupWarning = false;
			for(CourseMemberView selectedView:selectedViews) {
				if(selectedView.getMembership().isBusinessGroupMember()) {
					groupWarning = true;
				}
			}
			
			if(groupWarning) {
				showWarning("warning.cannot.leave.group");
			} else {
				showWarning("warning.cannot.leave.entry");
			}
			return;
		}
		
		List<Long> groupKeys = new ArrayList<>();
		List<RepositoryEntry> repoEntryToLeave = new ArrayList<>();
		for(CourseMemberView view:views) {
			if(view.getGroups() != null) {
				for(BusinessGroupShort group:view.getGroups()) {
					if(!BusinessGroupManagedFlag.isManaged(group.getManagedFlags(), BusinessGroupManagedFlag.membersmanagement)) {
						groupKeys.add(group.getKey());
					}
				}
			}

			RepositoryEntry re = repositoryManager.lookupRepositoryEntry(view.getRepoKey());	
			if(!RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.membersmanagement)) {
				repoEntryToLeave.add(re);
				if(view.getMembership().isRepositoryEntryOwner()) {
					int numOfOwners = repositoryService.countMembers(re, GroupRoles.owner.name());
					if(numOfOwners == 1) {
						showError("error.atleastone", view.getDisplayName());
						return;//break the process
					}
				}
			}
		}
		
		List<BusinessGroup> groupsToLeave = businessGroupService.loadBusinessGroups(groupKeys);

		List<BusinessGroup> groupsToDelete = new ArrayList<>(1);
		for(BusinessGroup group:groupsToLeave) {
			int numOfOwners = businessGroupService.countMembers(group, GroupRoles.coach.name());
			int numOfParticipants = businessGroupService.countMembers(group, GroupRoles.participant.name());
			if ((numOfOwners == 1 && numOfParticipants == 0) || (numOfOwners == 0 && numOfParticipants == 1)) {
				groupsToDelete.add(group);
			}
		}
		
		removeFromCourseDlg = new CourseLeaveDialogBoxController(ureq, getWindowControl(), editedIdentity,
				repoEntryToLeave, groupsToLeave, groupsToDelete);
		listenTo(removeFromCourseDlg);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), removeFromCourseDlg.getInitialComponent(),
				true, translate("unsubscribe.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void removeUserFromCourse(UserRequest ureq, List<RepositoryEntry> repoEntriesToLeave, List<BusinessGroup> groupsToLeave, List<BusinessGroup> groupsToDelete, boolean doSendMail) {
		List<Identity> membersToRemove = Collections.singletonList(editedIdentity);
		
		for(BusinessGroup group:groupsToLeave) {
			if (groupsToDelete.contains(group)) {
				// really delete the group as it has no more owners/participants
				if(doSendMail) {
					String businessPath = getWindowControl().getBusinessControl().getAsString();
					businessGroupService.deleteBusinessGroupWithMail(group, businessPath, getIdentity(), getLocale());
				} else {
					businessGroupService.deleteBusinessGroup(group);
				}
			} else {
				// 1) remove as owner
				if (businessGroupService.hasRoles(editedIdentity, group, GroupRoles.coach.name())) {
					businessGroupService.removeOwners(ureq.getIdentity(), membersToRemove, group);
				}
				MailPackage mailing = new MailPackage(doSendMail);
				// 2) remove as participant
				businessGroupService.removeParticipants(getIdentity(), membersToRemove, group, mailing);
				Roles roles = ureq.getUserSession().getRoles();
				boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
				MailHelper.printErrorsAndWarnings(mailing.getResult(), getWindowControl(), detailedErrorOutput, getLocale());
			}
		}
		
		for(RepositoryEntry repoEntry:repoEntriesToLeave) {
			if(isRepoMember(repoEntry)) {
				MailPackage mailing = new MailPackage(doSendMail);
				repositoryManager.removeMembers(getIdentity(), membersToRemove, repoEntry, mailing);
			}
		}

		updateModel();

		StringBuilder groupNames = new StringBuilder();
		for(BusinessGroup group:groupsToLeave) {
			if(groupNames.length() > 0) groupNames.append(", ");
			groupNames.append(group.getName());
		}
		showInfo("unsubscribe.successful");	
	}
	
	private boolean isRepoMember(RepositoryEntry repoEntry) {
		for(CourseMemberView view:tableDataModel.getObjects()) {
			if(repoEntry.getKey().equals(view.getRepoKey())) {
				CourseMembership membership = view.getMembership();
				return membership.isRepositoryEntryOwner() || membership.isRepositoryEntryCoach() || membership.isRepositoryEntryParticipant();
			}
		}
		return false;
	}
	
	private enum SearchType {
		owner, tutor, participant
	}
	
	private static class ConfirmAdd {
		private final SearchType type;
		private final Collection<RepositoryEntry> entries;
		
		public ConfirmAdd(SearchType type, Collection<RepositoryEntry> entries) {
			this.type = type;
			this.entries = entries;
		}

		public SearchType getType() {
			return type;
		}

		public Collection<RepositoryEntry> getEntries() {
			return entries;
		}
	}
	
	private class ConfirmEdit {
		private final RepositoryEntry entry;
		private final MemberPermissionChangeEvent e;
		
		public ConfirmEdit(MemberPermissionChangeEvent e, RepositoryEntry entry) {
			this.e = e;
			this.entry = entry;
		}

		public RepositoryEntry getEntry() {
			return entry;
		}

		public MemberPermissionChangeEvent getChangeEvent() {
			return e;
		}
	}
	
	private static class ManagedEntryfilter implements RepositoryEntryFilter {
		@Override
		public boolean accept(RepositoryEntry re) {
			return !RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.membersmanagement);
		}
	}
}