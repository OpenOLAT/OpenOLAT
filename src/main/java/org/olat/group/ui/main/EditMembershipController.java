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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement.Layout;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.member.PermissionHelper;
import org.olat.course.member.PermissionHelper.BGPermission;
import org.olat.course.member.PermissionHelper.RepoPermission;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.BusinessGroupRow;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.model.comparator.BusinessGroupRowComparator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryMembership;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	private final boolean overrideManaged;
	private final BusinessGroup businessGroup;
	private final RepositoryEntry repoEntry;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	private static final String[] keys = new String[] { "ison" };
	private static final String[] values = new String[] {""};
	
	public EditMembershipController(UserRequest ureq, WindowControl wControl, Identity member,
			RepositoryEntry repoEntry, BusinessGroup businessGroup, boolean overrideManaged) {
		super(ureq, wControl, "edit_member");
		this.member = member;
		this.members = null;
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		this.withButtons = true;
		this.overrideManaged = overrideManaged;
		
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
			RepositoryEntry repoEntry, BusinessGroup businessGroup, boolean overrideManaged) {
		super(ureq, wControl, "edit_member");
		
		this.member = null;
		this.members = (members == null ? null : new ArrayList<Identity>(members));
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		this.withButtons = true;
		this.overrideManaged = overrideManaged;
		
		memberships = Collections.emptyList();

		initForm(ureq);
		loadModel(member);
	}
	
	public EditMembershipController(UserRequest ureq, WindowControl wControl, List<Identity> members,
			RepositoryEntry repoEntry, BusinessGroup businessGroup, boolean overrideManaged, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "edit_member", rootForm);
		
		this.member = null;
		this.members = (members == null ? null : new ArrayList<Identity>(members));
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		this.withButtons = false;
		this.overrideManaged = overrideManaged;
		
		memberships = Collections.emptyList();

		initForm(ureq);
		loadModel(member);
	}
	
	private void loadModel(Identity memberToLoad) {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		if(repoEntry == null) {
			params.setBusinessGroupKey(businessGroup.getKey());
		} else {
			params.setRepositoryEntry(repoEntry);
		}

		List<StatisticsBusinessGroupRow> groups = businessGroupService.findBusinessGroupsStatistics(params);
		if(groups.size() > 1) {
			Collections.sort(groups, new BusinessGroupRowComparator(getLocale()));
		}

		boolean defaultMembership = false;
		if(memberToLoad == null) {
			if(repoEntry != null && groups.isEmpty()) {
				boolean managed = RepositoryEntryManagedFlag.isManaged(repoEntry, RepositoryEntryManagedFlag.membersmanagement) && !overrideManaged;
				if(!managed) {
					repoRightsEl.select("participant", true);
				}
			} else if(repoEntry == null && groups.size() == 1) {
				boolean managed = BusinessGroupManagedFlag.isManaged(groups.get(0).getManagedFlags(), BusinessGroupManagedFlag.membersmanagement) && !overrideManaged;
				if(!managed) {
					defaultMembership = true;
				}
			}
		}

		List<Long> businessGroupKeys = new ArrayList<>(groups.size());
		groups.forEach(group -> businessGroupKeys.add(group.getKey()));
		
		groupMemberships = memberToLoad == null ?
				Collections.<BusinessGroupMembership>emptyList() : businessGroupService.getBusinessGroupMembership(businessGroupKeys, memberToLoad);
		
		List<MemberOption> options = new ArrayList<MemberOption>();
		for(StatisticsBusinessGroupRow group:groups) {
			boolean managed = BusinessGroupManagedFlag.isManaged(group.getManagedFlags(), BusinessGroupManagedFlag.membersmanagement) && !overrideManaged;
			MemberOption option = new MemberOption(group);
			BGPermission bgPermission = PermissionHelper.getPermission(group.getKey(), memberToLoad, groupMemberships);
			option.setTutor(createSelection(bgPermission.isTutor(), !managed, GroupRoles.coach.name()));
			option.setParticipant(createSelection(bgPermission.isParticipant() || defaultMembership, !managed, GroupRoles.participant.name()));
			boolean waitingListEnable = !managed && group.isWaitingListEnabled();
			option.setWaiting(createSelection(bgPermission.isWaitingList(), waitingListEnable, GroupRoles.waiting.name()));
			options.add(option);
		}
		
		tableDataModel.setObjects(options);
	}
	
	private MultipleSelectionElement createSelection(boolean selected, boolean enabled, String role) {
		String name = "cb" + UUID.randomUUID().toString().replace("-", "");
		MultipleSelectionElement selection = new MultipleSelectionElementImpl(name, Layout.horizontal);
		selection.setElementCssClass("o_sel_role");
		selection.addActionListener(FormEvent.ONCHANGE);
		selection.setKeysAndValues(keys, values, new String[]{ "o_sel_role_".concat(role) }, null);
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
			name = StringHelper.escapeHtml(name);
			String title = translate("edit.member.title", new String[]{ name });
			layoutCont.contextPut("editTitle", title);
		}
		//repository entry rights
		if(repoEntry != null) {
			String[] repoValues = new String[] {
					translate("role.repo.owner"), translate("role.repo.tutor"), translate("role.repo.participant")
			};
			boolean managed = RepositoryEntryManagedFlag.isManaged(repoEntry, RepositoryEntryManagedFlag.membersmanagement) && !overrideManaged;
			repoRightsEl = uifactory.addCheckboxesVertical("repoRights", null, formLayout, repoRightsKeys, repoValues, 1);
			repoRightsEl.setEnabled(!managed);
			if(member != null) {
				RepoPermission repoPermission = PermissionHelper.getPermission(repoEntry, member, memberships);
				repoRightsEl.select("owner", repoPermission.isOwner());
				repoRightsEl.select("tutor", repoPermission.isTutor());
				repoRightsEl.select("participant", repoPermission.isParticipant());
			}
		}

		//group rights
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.groups", 0));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.tutorsCount", 1));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.participantsCount", 2));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, "table.header.freePlace", 3,
				false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.none)));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.tutors", 4));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.participants", 5));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.waitingList", 6));
		
		tableDataModel = new EditMemberTableDataModel(Collections.<MemberOption>emptyList(), tableColumnModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "groupList", tableDataModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		
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

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement selectEl = (MultipleSelectionElement)source;
			if(selectEl.isSelected(0)) {
				for(MemberOption option:tableDataModel.getObjects()) {
					if(option.getWaiting() == selectEl) {
						if(option.getParticipant().isSelected(0)) {
							option.getParticipant().select(keys[0], false);
						}
						if(option.getTutor().isSelected(0)) {
							option.getTutor().select(keys[0], false);
						}
					} else if(option.getParticipant() == selectEl || option.getTutor() == selectEl) {
						if(option.getWaiting() != null && option.getWaiting().isSelected(0)) {
							option.getWaiting().select(keys[0], false);
						}
					}
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	public void collectRepoChanges(MemberPermissionChangeEvent e) {
		if(repoEntry == null) return;
		
		RepoPermission repoPermission = PermissionHelper.getPermission(repoEntry, member, memberships);

		Collection<String>	selectRepoRights = repoRightsEl.getSelectedKeys();
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
		private final StatisticsBusinessGroupRow group;
		private MultipleSelectionElement tutor;
		private MultipleSelectionElement participant;
		private MultipleSelectionElement waiting;
		
		public MemberOption(StatisticsBusinessGroupRow group) {
			this.group = group;
		}
		
		public BusinessGroupRow getGroup() {
			return group;
		}
		
		public Long getGroupKey() {
			return group.getKey();
		}
		
		public String getGroupName() {
			return group.getName();
		}

		public long getTutorCount() {
			return group.getNumOfCoaches();
		}
		
		public long getParticipantCount() {
			return group.getNumOfParticipants();
		}
		
		public long getNumOfPendings() {
			return group.getNumPending();
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
	
	private static class EditMemberTableDataModel extends DefaultFlexiTableDataModel<MemberOption>  {

		public EditMemberTableDataModel(List<MemberOption> options, FlexiTableColumnModel columnModel) {
			super(options, columnModel);
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
			return new EditMemberTableDataModel(new ArrayList<MemberOption>(), getTableColumnModel());
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