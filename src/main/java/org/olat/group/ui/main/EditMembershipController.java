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
package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.member.PermissionHelper;
import org.olat.course.member.PermissionHelper.BGPermission;
import org.olat.course.member.PermissionHelper.RepoPermission;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupView;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.resource.OLATResource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EditMembershipController extends FormBasicController {
	
	private EditMemberTableDataModel tableDataModel;
	private MultipleSelectionElement repoRightsEl;
	private boolean withButtons;
	
	private static final String[] repoRightsKeys = {"owner", "tutor", "participant"};
	
	private final Identity member;
	private final List<Identity> members;
	private List<RepositoryEntryMembership> memberships;
	private List<BusinessGroupMembership> groupMemberships;
	
	private final BusinessGroup businessGroup;
	private final RepositoryEntry repoEntry;
	private final RepositoryManager repositoryManager;
	private final BusinessGroupService businessGroupService;
	
	private static final String[] keys = new String[] { "ison" };
	private static final String[] values = new String[] {""};
	
	public EditMembershipController(UserRequest ureq, WindowControl wControl, Identity member,
			RepositoryEntry repoEntry, BusinessGroup businessGroup) {
		super(ureq, wControl, "edit_member");
		this.member = member;
		this.members = null;
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		this.withButtons = true;
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		memberships = repositoryManager.getRepositoryEntryMembership(repoEntry, member);
		initForm(ureq);
		loadModel(member);
		
		Date membershipCreation = null;
		if(memberships != null) {
			for(RepositoryEntryMembership membership:memberships) {
				Date creationDate = membership.getCreationDate();
				if(creationDate != null && (membershipCreation == null || membershipCreation.compareTo(creationDate) > 0)) {
					membershipCreation = creationDate;
				}
			}
		}
		
		if(groupMemberships != null) {
			for(BusinessGroupMembership membership:groupMemberships) {
				Date creationDate = membership.getCreationDate();
				if(creationDate != null && (membershipCreation == null || membershipCreation.compareTo(creationDate) > 0)) {
					membershipCreation = creationDate;
				}
			}
		}
	}
	
	public EditMembershipController(UserRequest ureq, WindowControl wControl, List<Identity> members,
			RepositoryEntry repoEntry, BusinessGroup businessGroup) {
		super(ureq, wControl, "edit_member");
		
		this.member = null;
		this.members = (members == null ? null : new ArrayList<Identity>(members));
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		this.withButtons = true;
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		memberships = Collections.emptyList();

		initForm(ureq);
		loadModel(member);
	}
	
	public EditMembershipController(UserRequest ureq, WindowControl wControl, List<Identity> members,
			RepositoryEntry repoEntry, BusinessGroup businessGroup, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "edit_member", rootForm);
		
		this.member = null;
		this.members = (members == null ? null : new ArrayList<Identity>(members));
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		this.withButtons = false;
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		memberships = Collections.emptyList();

		initForm(ureq);
		loadModel(member);
	}
	
	private void loadModel(Identity member) {
		OLATResource resource = null;
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		if(repoEntry == null) {
			params.setGroupKeys(Collections.singletonList(businessGroup.getKey()));
		} else {
			resource = repoEntry.getOlatResource();
		}
		List<BusinessGroupView> groups = businessGroupService.findBusinessGroupViews(params, resource, 0, -1);

		List<Long> businessGroupKeys = PersistenceHelper.toKeys(groups);
		groupMemberships = member == null ?
				Collections.<BusinessGroupMembership>emptyList() : businessGroupService.getBusinessGroupMembership(businessGroupKeys, member);
		List<MemberOption> options = new ArrayList<MemberOption>();
		for(BusinessGroupView group:groups) {
			MemberOption option = new MemberOption(group);
			BGPermission bgPermission = PermissionHelper.getPermission(group.getKey(), member, groupMemberships);
			option.setTutor(createSelection(bgPermission.isTutor(), true));
			option.setParticipant(createSelection(bgPermission.isParticipant(), true));
			boolean waitingListEnable = group.getWaitingListEnabled() != null && group.getWaitingListEnabled().booleanValue();
			option.setWaiting(createSelection(bgPermission.isWaitingList(), waitingListEnable));
			options.add(option);
		}
		
		tableDataModel.setObjects(options);
	}
	
	private MultipleSelectionElement createSelection(boolean selected, boolean enabled) {
		String name = "cb" + UUID.randomUUID().toString().replace("-", "");
		MultipleSelectionElement selection = new MultipleSelectionElementImpl(name, MultipleSelectionElementImpl.createVerticalLayout("checkbox",1));
		selection.setKeysAndValues(keys, values, null);
		flc.add(name, selection);
		selection.select(keys[0], selected);
		selection.setEnabled(enabled);
		return selection;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String name = repoEntry == null ? businessGroup.getName() : repoEntry.getDisplayname();
			String title = translate("edit.member.title", new String[]{ name });
			layoutCont.contextPut("editTitle", title);
		}
		//repository entry rights
		if(repoEntry != null) {
			String[] repoValues = new String[] {
					translate("role.repo.owner"), translate("role.repo.tutor"), translate("role.repo.participant")
			};
			repoRightsEl = uifactory.addCheckboxesVertical("repoRights", formLayout, repoRightsKeys, repoValues, null, 1);
			if(member != null) {
				RepoPermission repoPermission = PermissionHelper.getPermission(repoEntry, member, memberships);
				repoRightsEl.select("owner", repoPermission.isOwner());
				repoRightsEl.select("tutor", repoPermission.isTutor());
				repoRightsEl.select("participant", repoPermission.isParticipant());
			}
		}

		//group rights
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.groups"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.tutorsCount"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.participantsCount"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.freePlace"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.tutors"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.participants"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.waitingList"));
		
		tableDataModel = new EditMemberTableDataModel(Collections.<MemberOption>emptyList(), tableColumnModel);
		uifactory.addTableElement(ureq, "groupList", tableDataModel, formLayout);
		
		if(withButtons) {
			FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			formLayout.add(buttonLayout);
			buttonLayout.setRootForm(mainForm);
			uifactory.addFormSubmitButton("ok", buttonLayout);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public Identity getMember() {
		return member;
	}

	public List<Identity> getMembers() {
		return members;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		MemberPermissionChangeEvent e = new MemberPermissionChangeEvent(member);
		collectRepoChanges(e);
		collectGroupChanges(e);
		fireEvent(ureq, e);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	public void collectRepoChanges(MemberPermissionChangeEvent e) {
		if(repoEntry == null) return;
		
		RepoPermission repoPermission = PermissionHelper.getPermission(repoEntry, member, memberships);

		Set<String>	selectRepoRights = repoRightsEl.getSelectedKeys();
		boolean repoOwner = selectRepoRights.contains("owner");
		e.setRepoOwner(repoOwner == repoPermission.isOwner() ? null : new Boolean(repoOwner));
		boolean repoTutor = selectRepoRights.contains("tutor");
		e.setRepoTutor(repoTutor == repoPermission.isTutor() ? null : new Boolean(repoTutor));
		boolean repoParticipant = selectRepoRights.contains("participant");
		e.setRepoParticipant(repoParticipant == repoPermission.isParticipant() ? null : new Boolean(repoParticipant));
	}
	
	public void collectGroupChanges(MemberPermissionChangeEvent e) {
		List<BusinessGroupMembershipChange> changes = new ArrayList<BusinessGroupMembershipChange>();
		
		for(MemberOption option:tableDataModel.getObjects()) {
			BGPermission bgPermission = PermissionHelper.getPermission(option.getGroupKey(), member, groupMemberships);
			BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(member, option.getGroup());
			boolean bgTutor = option.getTutor().isAtLeastSelected(1);
			change.setTutor(bgPermission.isTutor() == bgTutor ? null : new Boolean(bgTutor));
			boolean bgParticipant = option.getParticipant().isAtLeastSelected(1);
			change.setParticipant(bgPermission.isParticipant() == bgParticipant ? null : new Boolean(bgParticipant));
			boolean bgWaitingList = option.getWaiting().isEnabled() && option.getWaiting().isAtLeastSelected(1);
			change.setWaitingList(bgPermission.isWaitingList() == bgWaitingList ? null : new Boolean(bgWaitingList));

			if(change.getTutor() != null || change.getParticipant() != null || change.getWaitingList() != null) {
				changes.add(change);
			}
		}
		e.setGroupChanges(changes);
	}

	private static class MemberOption {
		private final BusinessGroupView group;
		private MultipleSelectionElement tutor;
		private MultipleSelectionElement participant;
		private MultipleSelectionElement waiting;
		
		public MemberOption(BusinessGroupView group) {
			this.group = group;
		}
		
		public BusinessGroupView getGroup() {
			return group;
		}
		
		public Long getGroupKey() {
			return group.getKey();
		}
		
		public String getGroupName() {
			return group.getName();
		}

		public long getTutorCount() {
			return group.getNumOfOwners();
		}
		
		public long getParticipantCount() {
			return group.getNumOfParticipants();
		}
		
		public long getNumOfPendings() {
			return group.getNumOfPendings();
		}
		
		public Integer getMaxParticipants() {
			return group.getMaxParticipants();
		}
		
		public MultipleSelectionElement getTutor() {
			return tutor;
		}
		
		public void setTutor(MultipleSelectionElement tutor) {
			this.tutor = tutor;
		}
		
		public MultipleSelectionElement getParticipant() {
			return participant;
		}
		
		public void setParticipant(MultipleSelectionElement participant) {
			this.participant = participant;
		}
		
		public MultipleSelectionElement getWaiting() {
			return waiting;
		}
		
		public void setWaiting(MultipleSelectionElement waiting) {
			this.waiting = waiting;
		}
	}
	
	private static class EditMemberTableDataModel extends DefaultTableDataModel<MemberOption> implements FlexiTableDataModel {
		private FlexiTableColumnModel columnModel;
		
		public EditMemberTableDataModel(List<MemberOption> options, FlexiTableColumnModel columnModel) {
			super(options);
			this.columnModel = columnModel;
		}

		@Override
		public FlexiTableColumnModel getTableColumnModel() {
			return columnModel;
		}

		@Override
		public void setTableColumnModel(FlexiTableColumnModel tableColumnModel) {
			columnModel = tableColumnModel;
		}

		@Override
		public void load(int firstResult, int maxResults, SortKey... sortedCol) {
			//already loaded
		}

		@Override
		public int getColumnCount() {
			return columnModel.getColumnCount();
		}

		@Override
		public Object getValueAt(int row, int col) {
			MemberOption option = getObject(row);
			switch(Cols.values()[col]) {
				case groupName: return option.getGroupName();
				case tutorCount: return new Long(option.getTutorCount());
				case participantCount: return new Long(option.getParticipantCount() + option.getNumOfPendings());
				case freePlaces: {
					Integer maxParticipants = option.getMaxParticipants();
					if(maxParticipants != null && maxParticipants.intValue() > 0) {
						long free = maxParticipants - (option.getParticipantCount() + option.getNumOfPendings());
						return Long.toString(free);
					}
					return "&infin;";
				}
				case tutor: return option.getTutor();
				case participant: return option.getParticipant();
				case waitingList: return option.getWaiting();
				default: return option;
			}
		}

		@Override
		public EditMemberTableDataModel createCopyWithEmptyList() {
			return new EditMemberTableDataModel(new ArrayList<MemberOption>(), columnModel);
		}
	}
	
	public static enum Cols {
		groupName("table.header.groups"),
		tutorCount("table.header.tutorsCount"),
		participantCount("table.header.participantsCount"),
		freePlaces("table.header.freePlace"),
		tutor("table.header.tutors"),
		participant("table.header.participants"),
		waitingList("table.header.waitingList");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}
	}
}