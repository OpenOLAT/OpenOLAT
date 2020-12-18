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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.AddRemoveElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.AddRemoveElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.AddRemoveElementImpl.AddRemoveMode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.course.member.PermissionHelper;
import org.olat.course.member.PermissionHelper.BGPermission;
import org.olat.course.member.PermissionHelper.RepoPermission;
import org.olat.course.member.wizard.MembersContext;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.BusinessGroupRow;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.model.comparator.BusinessGroupRowComparator;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumElementRow;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EditMembershipController extends FormBasicController {
	
	private FlexiTableElement groupTableEl;
	private FlexiTableElement curriculumTableEl;
	private FlexiTableElement groupBatchTableEl;
	private FlexiTableElement curriculumBatchTableEl;
	private AddRemoveElement repoOwnerRoleEl;
	private AddRemoveElement repoCoachRoleEl;
	private AddRemoveElement repoParticipantRoleEl;
	private AddRemoveElement selectAllGroupCoachEl;
	private AddRemoveElement selectAllGroupParticipantEl;
	private AddRemoveElement selectAllGroupWaitingListEl;
	private AddRemoveElement selectAllCurriculumOwnerEl;
	private AddRemoveElement selectAllCurriculumCoachEl;
	private AddRemoveElement selectAllCurriculumParticipantEl;
	private FormLayoutContainer groupContainer;
	private FormLayoutContainer curriculumContainer;
	private EditGroupMembershipTableDataModel groupTableDataModel;
	private EditCurriculumMembershipTableDataModel curriculumTableDataModel;
	
	private final Identity member;
	private final List<Identity> members;
	private List<RepositoryEntryMembership> memberships;
	private List<BusinessGroupMembership> groupMemberships;
	private List<CurriculumElementMembership> curriculumElementMemberships;
	
	private boolean withButtons;
	private final boolean overrideManaged;
	private final boolean extendedCurriculumRoles;
	private final boolean curriculumVisible;
	private final BusinessGroup businessGroup;
	private final RepositoryEntry repoEntry;
	private final Curriculum curriculum;
	private final CurriculumElement rootCurriculumElement;
	private final AddRemoveMode addRemoveMode;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public EditMembershipController(UserRequest ureq, WindowControl wControl, Identity member,
			RepositoryEntry repoEntry, BusinessGroup businessGroup, boolean overrideManaged) {
		super(ureq, wControl, "edit_multi_member");
		this.member = member;
		this.members = null;
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		curriculum = null;
		rootCurriculumElement = null;
		this.withButtons = true;
		this.overrideManaged = overrideManaged;
		this.curriculumVisible = false;
		extendedCurriculumRoles = false;
		this.addRemoveMode = AddRemoveMode.TWO_STATE;
		
		memberships = repositoryManager.getRepositoryEntryMembership(repoEntry, member);
		initForm(ureq);
		loadModel(ureq, member);
		
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
		super(ureq, wControl, "edit_multi_member");
		
		this.member = null;
		this.members = (members == null ? null : new ArrayList<>(members));
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		curriculum = null;
		rootCurriculumElement = null;
		this.withButtons = true;
		this.overrideManaged = overrideManaged;
		extendedCurriculumRoles = false;
		this.addRemoveMode = AddRemoveMode.THREE_STATE;
		this.curriculumVisible = false;
		
		memberships = Collections.emptyList();

		initForm(ureq);
		loadModel(ureq, member);
	}
	
	public EditMembershipController(UserRequest ureq, WindowControl wControl, List<Identity> members,
			Curriculum curriculum, CurriculumElement curriculumElement, boolean overrideManaged) {
		super(ureq, wControl, "edit_multi_member");
		
		member = null;
		this.members = (members == null ? null : new ArrayList<>(members));
		repoEntry = null;
		businessGroup = null;
		this.curriculum = curriculum;
		this.rootCurriculumElement = curriculumElement;
		this.withButtons = true;
		this.overrideManaged = overrideManaged;
		extendedCurriculumRoles = true;
		this.addRemoveMode = AddRemoveMode.THREE_STATE;
		this.curriculumVisible = true;
		
		memberships = Collections.emptyList();

		initForm(ureq);
		loadModel(ureq, member);
	}
	
	public EditMembershipController(UserRequest ureq, WindowControl wControl, Identity member,
			Curriculum curriculum, CurriculumElement curriculumElement, boolean overrideManaged) {
		super(ureq, wControl, "edit_multi_member");
		
		this.member = member;
		this.members = null;
		repoEntry = null;
		businessGroup = null;
		this.curriculum = curriculum;
		this.rootCurriculumElement = curriculumElement;
		this.withButtons = true;
		this.overrideManaged = overrideManaged;
		extendedCurriculumRoles = true;
		this.addRemoveMode = AddRemoveMode.TWO_STATE;
		this.curriculumVisible = true;
		
		memberships = Collections.emptyList();
		curriculumElementMemberships =  curriculumService
				.getCurriculumElementMemberships(Collections.singletonList(curriculumElement), member);

		initForm(ureq);
		loadModel(ureq, member);
	}
	
	public EditMembershipController(UserRequest ureq, WindowControl wControl, List<Identity> members,
			MembersContext membersContext, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "edit_multi_member", rootForm);
		
		member = null;
		this.members = (members == null ? null : new ArrayList<>(members));
		repoEntry = membersContext.getRepoEntry();
		businessGroup = membersContext.getGroup();
		curriculum = membersContext.getCurriculum();
		rootCurriculumElement = membersContext.getRootCurriculumElement();
		this.withButtons = false;
		overrideManaged = membersContext.isOverrideManaged();
		extendedCurriculumRoles = membersContext.isExtendedCurriculumRoles();
		this.addRemoveMode = AddRemoveMode.THREE_STATE;
		this.curriculumVisible = false;
		
		memberships = Collections.emptyList();

		initForm(ureq);
		loadModel(ureq, member);
	}
	
	private void loadModel(UserRequest ureq, Identity memberToLoad) {
		Roles roles = ureq.getUserSession().getRoles();
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		if(repoEntry == null && businessGroup != null) {
			List<Long> keys = new ArrayList<>();
			keys.add(businessGroup.getKey());
			params.setBusinessGroupKeys(keys);
		} else {
			params.setRepositoryEntry(repoEntry);
		}

		List<StatisticsBusinessGroupRow> groups;
		if(repoEntry != null || businessGroup != null) {
			groups = businessGroupService.findBusinessGroupsStatistics(params);
			if(groups.size() > 1) {
				Collections.sort(groups, new BusinessGroupRowComparator(getLocale()));
			}
		} else {
			groups = new ArrayList<>(1);
		}

		boolean defaultMembership = false;
		if(memberToLoad == null) {
			if(repoEntry != null && groups.isEmpty()) {
				boolean managed = RepositoryEntryManagedFlag.isManaged(repoEntry, RepositoryEntryManagedFlag.membersmanagement) && !overrideManaged;
				if(!managed) {
					repoParticipantRoleEl.setSelection(true);
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
		
		List<MemberGroupOption> options = new ArrayList<>();
		boolean anyGroupWithWaitingList = false;
		for(StatisticsBusinessGroupRow group:groups) {
			boolean managed = BusinessGroupManagedFlag.isManaged(group.getManagedFlags(), BusinessGroupManagedFlag.membersmanagement) && !overrideManaged;
			MemberGroupOption option = new MemberGroupOption(group);
			BGPermission bgPermission = PermissionHelper.getPermission(group.getKey(), memberToLoad, groupMemberships);
			option.setTutor(createAddRemove(member == null ? null : bgPermission.isTutor(), !managed, GroupRoles.coach.name()));
			option.setParticipant(createAddRemove(member == null ? null : bgPermission.isParticipant() || defaultMembership, !managed, GroupRoles.participant.name()));
			boolean waitingListEnable = !managed && group.isWaitingListEnabled();
			anyGroupWithWaitingList |= waitingListEnable;
			option.setWaiting(createAddRemove(member == null ? null : bgPermission.isWaitingList(), waitingListEnable, GroupRoles.waiting.name()));
			options.add(option);
		}
		
		selectAllGroupWaitingListEl.setEnabled(anyGroupWithWaitingList);
		groupTableDataModel.setObjects(options);
		groupBatchTableEl.setVisible(options.size() > 1);
		groupContainer.setVisible(!options.isEmpty());
		
		List<CurriculumElement> curriculumElements = loadCurriculumElements();
		List<CurriculumElement> editableElements = curriculumService.filterElementsWithoutManagerRole(curriculumElements, roles);
		curriculumElementMemberships = memberToLoad == null ? Collections.emptyList() :
				 curriculumService.getCurriculumElementMemberships(curriculumElements, memberToLoad);

		List<MemberCurriculumOption> curriculumOptions = new ArrayList<>();
		for(CurriculumElement element:curriculumElements) {
			boolean managed = CurriculumElementManagedFlag.isManaged(element.getManagedFlags(), CurriculumElementManagedFlag.members) && !overrideManaged;
			if(!editableElements.contains(element)) {
				managed = true;
			}
			MemberCurriculumOption option = new MemberCurriculumOption(element);
			RepoPermission rePermission = PermissionHelper.getPermission(element, memberToLoad, curriculumElementMemberships);
			option.setOwner(createAddRemove(member == null ? null : rePermission.isOwner(), !managed, CurriculumRoles.owner.name()));
			option.setCoach(createAddRemove(member == null ? null : rePermission.isTutor(), !managed, CurriculumRoles.coach.name()));
			option.setParticipant(createAddRemove(member == null ? null : rePermission.isParticipant() || defaultMembership, !managed, CurriculumRoles.participant.name()));
			if(extendedCurriculumRoles) {
				option.setMasterCoach(createAddRemove(member == null ? null : rePermission.isMasterCoach(), !managed, CurriculumRoles.mastercoach.name()));
				option.setElementOwner(createAddRemove(member == null ? null : rePermission.isCurriculumElementOwner(), !managed, CurriculumRoles.curriculumelementowner.name()));
			}
			curriculumOptions.add(option);
		}
		
		curriculumTableDataModel.setObjects(curriculumOptions);
		curriculumBatchTableEl.setVisible(curriculumVisible && curriculumOptions.size() > 1);
		curriculumContainer.setVisible(curriculumVisible && !curriculumOptions.isEmpty());
	}
	
	private List<CurriculumElement> loadCurriculumElements() {
		List<CurriculumElement> curriculumElements;
		if(curriculum != null) {
			curriculumElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.notDeleted());
			curriculumElements = orderCurriculumElements(curriculumElements);
		} else if (repoEntry != null) {
			curriculumElements = curriculumService.getCurriculumElements(repoEntry);
		} else {
			curriculumElements = Collections.emptyList();
		}
		return curriculumElements;
	}


	
	private List<CurriculumElement> orderCurriculumElements(List<CurriculumElement> curriculumElements) {
		try {
			List<CurriculumElementRow> rows = new ArrayList<>(curriculumElements.size());
			Map<Long, CurriculumElementRow> keyToRows = new HashMap<>();
			for(CurriculumElement element:curriculumElements) {
				CurriculumElementRow row = new CurriculumElementRow(element);
				rows.add(row);
				keyToRows.put(element.getKey(), row);
			}
			//parent line
			for(CurriculumElementRow row:rows) {
				if(row.getParentKey() != null) {
					row.setParent(keyToRows.get(row.getParentKey()));
				}
			}
			
			if(rootCurriculumElement != null) {
				rows = filterCurriculumElements(rows);
			}
			Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
			
			List<CurriculumElement> orderedElements = new ArrayList<>(rows.size());
			for(CurriculumElementRow row:rows) {
				orderedElements.add(row.getCurriculumElement());
			}
			return orderedElements;
		} catch (Exception e) {
			logError("", e);
			return curriculumElements;
		}
	}
	
	private List<CurriculumElementRow> filterCurriculumElements(List<CurriculumElementRow> rows) {
		for(CurriculumElementRow row:rows) {
			if(row != null) {
				boolean hasRoot = hasRootCurriculumElementInParentLine(row);
				row.setAcceptedByFilter(hasRoot);
			}
		}
		
		for(CurriculumElementRow row:rows) {
			if(row.isAcceptedByFilter()) {
				for(CurriculumElementRow parent=row.getParent(); parent != null; parent=parent.getParent()) {
					parent.setAcceptedByFilter(true);
				}
			}
		}
		
		return rows.stream()
				.filter(CurriculumElementRow::isAcceptedByFilter)
				.collect(Collectors.toList());
	}
	
	private boolean hasRootCurriculumElementInParentLine(CurriculumElementRow curriculumElement) {
		for(CurriculumElementRow parentElement=curriculumElement; parentElement != null; parentElement = parentElement.getParent()) {
			if(parentElement.getCurriculumElement().equals(rootCurriculumElement)) {
				return true;
			}	
		}
		return false;
	}
	
	private AddRemoveElement createAddRemove(Boolean selected, boolean enabled, String role) {
		String name = "cb" + UUID.randomUUID().toString().replace("-", "");
		AddRemoveElement addRemove = new AddRemoveElementImpl(name, Link.BUTTON_XSMALL);
		addRemove.setShowText(false);
		addRemove.setElementCssClass("o_sel_role_".concat(role));
		flc.add(name, addRemove);
		addRemove.setSelection(selected);
		addRemove.setEnabled(enabled);
		return addRemove;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name;
		if(repoEntry != null) {
			name = repoEntry.getDisplayname();
		} else if(businessGroup != null) {
			name = businessGroup.getName();
		} else if(curriculum != null) {
			name = curriculum.getDisplayName();
		} else {
			name = "";
		}
		name = StringHelper.escapeHtml(name);
		String title = translate("edit.member.title", new String[]{ name });
			
		//repository entry rights
		if (repoEntry != null) {
			FormLayoutContainer repoRights = FormLayoutContainer.createDefaultFormLayout("repoRights", getTranslator());
			repoRights.setRootForm(mainForm);
			repoRights.setFormTitle(title);
			formLayout.add(repoRights);
			
			boolean managed = RepositoryEntryManagedFlag.isManaged(repoEntry, RepositoryEntryManagedFlag.membersmanagement) && !overrideManaged;
			String grantText = translate("edit.members.grant");
			String denyText = translate("edit.members.deny");
			
			repoOwnerRoleEl = uifactory.addAddRemoveElement("repoOwnerRight", "role.repo.owner", Link.BUTTON, true, addRemoveMode, repoRights);
			repoOwnerRoleEl.setAddText(grantText);
			repoOwnerRoleEl.setRemoveText(denyText);
			repoOwnerRoleEl.setElementCssClass("o_sel_role_owner");
			repoOwnerRoleEl.setEnabled(!managed);
			repoCoachRoleEl = uifactory.addAddRemoveElement("repoCoachRight", "role.repo.tutor", Link.BUTTON, true, addRemoveMode, repoRights);
			repoCoachRoleEl.setAddText(grantText);
			repoCoachRoleEl.setRemoveText(denyText);
			repoCoachRoleEl.setElementCssClass("o_sel_role_coach");
			repoCoachRoleEl.setEnabled(!managed);
			repoParticipantRoleEl = uifactory.addAddRemoveElement("repoParticipantRight", "role.repo.participant", Link.BUTTON, true, addRemoveMode, repoRights);
			repoParticipantRoleEl.setAddText(grantText);
			repoParticipantRoleEl.setRemoveText(denyText);
			repoParticipantRoleEl.setElementCssClass("o_sel_role_participant");
			repoParticipantRoleEl.setEnabled(!managed);
			
			if (member != null) {
				RepoPermission repoPermission = PermissionHelper.getPermission(repoEntry, member, memberships);
				repoOwnerRoleEl.setSelection(repoPermission.isOwner());
				repoCoachRoleEl.setSelection(repoPermission.isTutor());
				repoParticipantRoleEl.setSelection(repoPermission.isParticipant());
			}
		}

		//group rights
		groupContainer = FormLayoutContainer.createVerticalFormLayout("groupRights", getTranslator());
		groupContainer.setRootForm(mainForm);
		groupContainer.setFormTitle(translate("edit.member.groups"));
		formLayout.add(groupContainer);
		
		FormLayoutContainer groupRightActions = FormLayoutContainer.createHorizontalFormLayout("groupRightActions", getTranslator());
		groupRightActions.setRootForm(mainForm);
		groupRightActions.setElementCssClass("pull-right");
		groupContainer.add(groupRightActions);
		
		selectAllGroupCoachEl = uifactory.addAddRemoveElement("edit.members.group.select.all.coach", Link.BUTTON_XSMALL, true, null);
		selectAllGroupCoachEl.setElementCssClass("o_spacer_left");
		selectAllGroupParticipantEl = uifactory.addAddRemoveElement("edit.members.group.select.all.participant", Link.BUTTON_XSMALL, true, null);
		selectAllGroupParticipantEl.setElementCssClass("o_spacer_left");
		selectAllGroupWaitingListEl = uifactory.addAddRemoveElement("edit.members.group.select.all.waiting.list", Link.BUTTON_XSMALL, true, null);
		selectAllGroupWaitingListEl.setElementCssClass("o_spacer_left");	
		
		FlexiTableColumnModel groupBatchActionsColumns = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		groupBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.empty));
		groupBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.empty));
		groupBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.empty));
		groupBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.empty));
		groupBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.groupCoach));
		groupBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.groupParticipant));
		groupBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.groupWaiting));
		
		BatchActionsRow groupBatchActionsRow = new BatchActionsRow(selectAllGroupCoachEl, selectAllGroupParticipantEl, selectAllGroupWaitingListEl, null);
		BatchActionsTableDataModel groupBatchActionsTableModel = new BatchActionsTableDataModel(groupBatchActionsColumns, Collections.singletonList(groupBatchActionsRow));
		groupBatchTableEl = uifactory.addTableElement(getWindowControl(), "group_batch_actions", groupBatchActionsTableModel, getTranslator(), groupRightActions);
		groupBatchTableEl.setCustomizeColumns(false);
		groupBatchTableEl.setNumOfRowsEnabled(false);
		groupBatchTableEl.setVisible(members != null && members.size() > 1);
		
		FlexiTableColumnModel groupTableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		groupTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.groupName));
		groupTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.tutorCount));
		groupTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.participantCount));
		groupTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, "table.header.freePlace", 3,
				false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.none)));
		groupTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.tutor));
		groupTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.participant));
		groupTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.waitingList));
		
		groupTableDataModel = new EditGroupMembershipTableDataModel(Collections.<MemberGroupOption>emptyList(), groupTableColumnModel);
		groupTableEl = uifactory.addTableElement(getWindowControl(), "groupList", groupTableDataModel, getTranslator(), groupContainer);
		groupTableEl.setCustomizeColumns(false);
		groupTableEl.setNumOfRowsEnabled(false);
		
		// curriculum rights
		curriculumContainer = FormLayoutContainer.createVerticalFormLayout("curriculumRights", getTranslator());
		curriculumContainer.setRootForm(mainForm);
		curriculumContainer.setFormTitle(translate("edit.member.curriculums"));
		formLayout.add(curriculumContainer);
		
		FormLayoutContainer curriculumRightActions = FormLayoutContainer.createHorizontalFormLayout("curriculumRightActions", getTranslator());
		curriculumRightActions.setRootForm(mainForm);
		curriculumRightActions.setElementCssClass("pull-right");
		curriculumContainer.add(curriculumRightActions);
		
		selectAllCurriculumOwnerEl = uifactory.addAddRemoveElement("edit.members.curriculum.select.all.owner", Link.BUTTON_XSMALL, true, null);
		selectAllCurriculumOwnerEl.setElementCssClass("o_spacer_left");
		selectAllCurriculumCoachEl = uifactory.addAddRemoveElement("edit.members.curriculum.select.all.coach", Link.BUTTON_XSMALL, true, null);
		selectAllCurriculumCoachEl.setElementCssClass("o_spacer_left");
		selectAllCurriculumParticipantEl = uifactory.addAddRemoveElement("edit.members.curriculum.select.all.participant", Link.BUTTON_XSMALL, true, null);
		selectAllCurriculumParticipantEl.setElementCssClass("o_spacer_left");
		
		FlexiTableColumnModel curriculumBatchActionsColumns = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		curriculumBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.empty));
		curriculumBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.empty));
		curriculumBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.empty));
		curriculumBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.curriculumOwner));
		curriculumBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.curriculumCoach));
		curriculumBatchActionsColumns.addFlexiColumnModel(new DefaultFlexiColumnModel(BatchActionCols.curriculumParticipant));
		
		BatchActionsRow curriculumBatchActionsRow = new BatchActionsRow(selectAllCurriculumCoachEl, selectAllCurriculumParticipantEl, null, selectAllCurriculumOwnerEl);
		BatchActionsTableDataModel curriculumBatchActionsTableModel = new BatchActionsTableDataModel(curriculumBatchActionsColumns, Collections.singletonList(curriculumBatchActionsRow));
		curriculumBatchTableEl = uifactory.addTableElement(getWindowControl(), "curriculum_batch_actions", curriculumBatchActionsTableModel, getTranslator(), curriculumRightActions);
		curriculumBatchTableEl.setCustomizeColumns(false);
		curriculumBatchTableEl.setNumOfRowsEnabled(false);
		
		FlexiTableColumnModel curriculumTableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(curriculum == null) {
			curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.curriculum));
			curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.curriculumElement));
		} else {
			curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.curriculumElement, new CurriculumElementIndentRenderer()));
		}

		curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.curriculumElementIdentifier));
		if(extendedCurriculumRoles) {
			curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.elementOwner));
			curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.masterCoach));
		}
		curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.owner));
		curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.coach));
		curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.participant));
		
		curriculumTableDataModel = new EditCurriculumMembershipTableDataModel(Collections.<MemberCurriculumOption>emptyList(), curriculumTableColumnModel);
		curriculumTableEl = uifactory.addTableElement(getWindowControl(), "curriculumList", curriculumTableDataModel, getTranslator(), curriculumContainer);
		curriculumTableEl.setCustomizeColumns(false);
		curriculumTableEl.setNumOfRowsEnabled(false);
		
		if(withButtons) {
			FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			formLayout.add(buttonLayout);
			buttonLayout.setRootForm(mainForm);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
			uifactory.addFormSubmitButton("ok", buttonLayout);
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
		collectCurriculumElementChanges(e);
		fireEvent(ureq, e);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectAllCurriculumOwnerEl == source) {
			List<MemberCurriculumOption> memberOptions = curriculumTableDataModel.getObjects();
			for (MemberCurriculumOption memberOption : memberOptions) {
				memberOption.getOwner().setSelection(selectAllCurriculumOwnerEl.getSelection());
			}
		} else if(selectAllCurriculumCoachEl == source) {
			List<MemberCurriculumOption> memberOptions = curriculumTableDataModel.getObjects();
			for (MemberCurriculumOption memberOption : memberOptions) {
				memberOption.getCoach().setSelection(selectAllCurriculumCoachEl.getSelection());
			}
		} else if(selectAllCurriculumParticipantEl == source) {
			List<MemberCurriculumOption> memberOptions = curriculumTableDataModel.getObjects();
			for (MemberCurriculumOption memberOption : memberOptions) {
				memberOption.getParticipant().setSelection(selectAllCurriculumParticipantEl.getSelection());
			}
		} else if (selectAllGroupWaitingListEl == source) {
			List<MemberGroupOption> memberOptions = groupTableDataModel.getObjects();
			for(MemberGroupOption memberOption:memberOptions) {
				if (memberOption.getWaiting().isEnabled()) {
					memberOption.getWaiting().setSelection(selectAllGroupWaitingListEl.getSelection());
				}
			}
		} else if (selectAllGroupCoachEl == source) {
			List<MemberGroupOption> memberOptions = groupTableDataModel.getObjects();
			for(MemberGroupOption memberOption:memberOptions) {
				memberOption.getTutor().setSelection(selectAllGroupCoachEl.getSelection());
			}
		} else if (selectAllGroupParticipantEl == source) {
			List<MemberGroupOption> memberOptions = groupTableDataModel.getObjects();
			for(MemberGroupOption memberOption:memberOptions) {
				memberOption.getParticipant().setSelection(selectAllGroupParticipantEl.getSelection());
			}
		} else if(source instanceof AddRemoveElement) {
			AddRemoveElement addRemoveEl = (AddRemoveElement)source;
			for(MemberGroupOption option:groupTableDataModel.getObjects()) {
				// If set on waiting list, remove as participant and tutor
				if(option.getWaiting() == addRemoveEl) {
					if (addRemoveEl.isAddSelected()) {
						option.getParticipant().selectRemove();
						option.getTutor().selectRemove();
					} 
				// If set as participant or tutor, remove from waiting list
				} else if((option.getParticipant() == addRemoveEl || option.getTutor() == addRemoveEl) && option.getWaiting().isEnabled()) {
					if (addRemoveEl.isAddSelected()) {
						option.getWaiting().selectRemove();
					} else if (!option.getParticipant().isAddSelected() && !option.getTutor().isAddSelected()) {
						option.getWaiting().reset();
					}
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	public void collectRepoChanges(MemberPermissionChangeEvent e) {
		if(repoEntry == null) return;
		
		List<RepositoryEntryPermissionChangeEvent> repoChanges = new ArrayList<>();

		if (member != null) {
			RepoPermission repoPermission = PermissionHelper.getPermission(repoEntry, member, memberships);
			
			e.setRepoOwner(getChange(repoOwnerRoleEl.getSelection(), repoPermission.isOwner()));
			e.setRepoTutor(getChange(repoCoachRoleEl.getSelection(), repoPermission.isTutor()));
			e.setRepoParticipant(getChange(repoParticipantRoleEl.getSelection(), repoPermission.isParticipant()));
			
			return;			
		} else if (members != null){
			for (Identity member : members) {
				List<RepositoryEntryMembership> memberships = repositoryManager.getRepositoryEntryMembership(repoEntry, member);
				RepoPermission repoPermission = PermissionHelper.getPermission(repoEntry, member, memberships);
				RepositoryEntryPermissionChangeEvent change = new RepositoryEntryPermissionChangeEvent(member);
				
				change.setRepoOwner(getChange(repoOwnerRoleEl.getSelection(), repoPermission.isOwner()));
				change.setRepoTutor(getChange(repoCoachRoleEl.getSelection(), repoPermission.isTutor()));
				change.setRepoParticipant(getChange(repoParticipantRoleEl.getSelection(), repoPermission.isParticipant()));
				
				if (change.getRepoOwner() != null || change.getRepoParticipant() != null || change.getRepoTutor() != null) {
					repoChanges.add(change);
				}
			}
		} else {
			e.setRepoOwner(repoOwnerRoleEl.getSelection());
			e.setRepoTutor(repoCoachRoleEl.getSelection());
			e.setRepoParticipant(repoParticipantRoleEl.getSelection());
		}
	
		e.setRepoChanges(repoChanges);
	}
	
	public void collectGroupChanges(MemberPermissionChangeEvent e) {
		List<BusinessGroupMembershipChange> changes = new ArrayList<>();
		List<MemberGroupOption> changedGroups = groupTableDataModel.getObjects().stream().filter(group -> group.containsChanges()).collect(Collectors.toList());
		List<Long> groupKeys = changedGroups.stream().map(change -> change.getGroupKey()).collect(Collectors.toList());
		
		Identity[] membersArray;
		List<BusinessGroupMembership> allMemberships = new ArrayList<>();
		
		if (members != null) {
			membersArray = new Identity[members.size()];
			members.toArray(membersArray);
			allMemberships = businessGroupService.getBusinessGroupMembership(groupKeys, membersArray);
		}
		
		for(MemberGroupOption option:changedGroups) {
			if (members != null) {
				for (Identity member : members) {	
					List<BusinessGroupMembership> memberships = allMemberships.stream().filter(ms -> ms.getIdentityKey().equals(member.getKey())).collect(Collectors.toList());
					BGPermission bgPermission = PermissionHelper.getPermission(option.getGroupKey(), member, memberships);
					BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(member, option.getGroup());
					
					
					change.setTutor(getChange(option.getTutor().getSelection(), bgPermission.isTutor()));
					change.setParticipant(getChange(option.getParticipant().getSelection(), bgPermission.isParticipant()));
					change.setWaitingList(getChange(option.getWaiting().getSelection(), bgPermission.isWaitingList()));
		
					if(change.getTutor() != null || change.getParticipant() != null || change.getWaitingList() != null) {
						changes.add(change);
					}
				}
			} else {
				BGPermission bgPermission = PermissionHelper.getPermission(option.getGroupKey(), member, groupMemberships);
				BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(member, option.getGroup());
				
				
				change.setTutor(getChange(option.getTutor().getSelection(), bgPermission.isTutor()));
				change.setParticipant(getChange(option.getParticipant().getSelection(), bgPermission.isParticipant()));
				change.setWaitingList(getChange(option.getWaiting().getSelection(), bgPermission.isWaitingList()));
	
				if(change.getTutor() != null || change.getParticipant() != null || change.getWaitingList() != null) {
					changes.add(change);
				}
			}
		}
		e.setGroupChanges(changes);
	}
	
	public void collectCurriculumElementChanges(MemberPermissionChangeEvent e) {
		List<CurriculumElementMembershipChange> changes = new ArrayList<>();
		List<MemberCurriculumOption> changedCurriculaOptions = curriculumTableDataModel.getObjects().stream().filter(curriculum -> curriculum.containsChanges()).collect(Collectors.toList());
		List<CurriculumElement> changedCurricula = changedCurriculaOptions.stream().map(option -> option.getElement()).collect(Collectors.toList());
		
		Identity[] membersArray;
		List<CurriculumElementMembership> allMemberships = new ArrayList<>();
		
		if (members != null) {
			membersArray = new Identity[members.size()];
			members.toArray(membersArray);
			allMemberships = curriculumService.getCurriculumElementMemberships(changedCurricula, membersArray);
		}
		
		for(MemberCurriculumOption option : changedCurriculaOptions) {
			if (members != null) {
				for (Identity member : members) {
					List<CurriculumElementMembership> curriculumElementMemberships = allMemberships.stream().filter(membership -> membership.getIdentityKey().equals(member.getKey())).collect(Collectors.toList());
					RepoPermission rePermission = PermissionHelper.getPermission(option.getElement(), member, curriculumElementMemberships);
					CurriculumElementMembershipChange change = new CurriculumElementMembershipChange(member, option.getElement());
		
					change.setRepositoryEntryOwner(getChange(option.getOwner().getSelection(), rePermission.isOwner()));
					change.setCoach(getChange(option.getCoach().getSelection(), rePermission.isTutor()));
					change.setParticipant(getChange(option.getParticipant().getSelection(), rePermission.isParticipant()));
					
					if(option.getMasterCoach() != null) {
						change.setMasterCoach(getChange(option.getMasterCoach().getSelection(), rePermission.isMasterCoach()));
					}
					if(option.getElementOwner() != null) {
						change.setCurriculumElementOwner(getChange(option.getElementOwner().getSelection(), rePermission.isCurriculumElementOwner()));
					}
					
					if(change.getCurriculumElementOwner() != null || change.getRepositoryEntryOwner() != null
							|| change.getCoach() != null || change.getParticipant() != null
							|| change.getCurriculumElementOwner() != null || change.getMasterCoach() != null) {
						changes.add(change);
					}
				}
			} else {
				RepoPermission rePermission = PermissionHelper.getPermission(option.getElement(), member, curriculumElementMemberships);
				CurriculumElementMembershipChange change = new CurriculumElementMembershipChange(member, option.getElement());
	
				change.setRepositoryEntryOwner(getChange(option.getOwner().getSelection(), rePermission.isOwner()));
				change.setCoach(getChange(option.getCoach().getSelection(), rePermission.isTutor()));
				change.setParticipant(getChange(option.getParticipant().getSelection(), rePermission.isParticipant()));
				
				if(option.getMasterCoach() != null) {
					change.setMasterCoach(getChange(option.getMasterCoach().getSelection(), rePermission.isMasterCoach()));
				}
				if(option.getElementOwner() != null) {
					change.setCurriculumElementOwner(getChange(option.getElementOwner().getSelection(), rePermission.isCurriculumElementOwner()));
				}
				
				if(change.getCurriculumElementOwner() != null || change.getRepositoryEntryOwner() != null
						|| change.getCoach() != null || change.getParticipant() != null
						|| change.getCurriculumElementOwner() != null || change.getMasterCoach() != null) {
					changes.add(change);
				}
			}
		}
		
		e.setCurriculumChanges(changes);
	}
	
	private Boolean getChange(Boolean selection, boolean permissionState) {
		if (selection == null || selection == permissionState) {
			return null;
		} else {
			return selection;
		}
	}
	
	private static boolean containsChange(AddRemoveElement addRemove) {
		if (addRemove == null) {
			return false;
		}
		
		if (addRemove.getSelection() != null) {
			return true;
		}
		
		return false;
	}
	
	private static class MemberCurriculumOption {

		private final CurriculumElement curriculumElement;
		private AddRemoveElement owner;
		private AddRemoveElement coach;
		private AddRemoveElement participant;
		private AddRemoveElement masterCoach;
		private AddRemoveElement elementOwner;
		
		public MemberCurriculumOption(CurriculumElement curriculumElement) {
			this.curriculumElement = curriculumElement;
		}
		
		public CurriculumElement getElement() {
			return curriculumElement;
		}

		public AddRemoveElement getOwner() {
			return owner;
		}

		public void setOwner(AddRemoveElement owner) {
			this.owner = owner;
		}

		public AddRemoveElement getCoach() {
			return coach;
		}

		public void setCoach(AddRemoveElement coach) {
			this.coach = coach;
		}

		public AddRemoveElement getParticipant() {
			return participant;
		}

		public void setParticipant(AddRemoveElement participant) {
			this.participant = participant;
		}
		
		public AddRemoveElement getMasterCoach() {
			return masterCoach;
		}

		public void setMasterCoach(AddRemoveElement masterCoach) {
			this.masterCoach = masterCoach;
		}

		public AddRemoveElement getElementOwner() {
			return elementOwner;
		}

		public void setElementOwner(AddRemoveElement elementOwner) {
			this.elementOwner = elementOwner;
		}
		
		public boolean containsChanges() {
			return containsChange(owner) || containsChange(coach) || containsChange(participant) || containsChange(masterCoach) || containsChange(elementOwner);
		}
	}

	private static class MemberGroupOption {
		private final StatisticsBusinessGroupRow group;
		private AddRemoveElement tutor;
		private AddRemoveElement participant;
		private AddRemoveElement waiting;
		
		public MemberGroupOption(StatisticsBusinessGroupRow group) {
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
		
		public AddRemoveElement getTutor() {
			return tutor;
		}
		
		public void setTutor(AddRemoveElement tutor) {
			this.tutor = tutor;
		}
		
		public AddRemoveElement getParticipant() {
			return participant;
		}
		
		public void setParticipant(AddRemoveElement participant) {
			this.participant = participant;
		}
		
		public AddRemoveElement getWaiting() {
			return waiting;
		}
		
		public void setWaiting(AddRemoveElement waiting) {
			this.waiting = waiting;
		}
		
		public boolean containsChanges() {
			return  containsChange(tutor) || containsChange(participant) || containsChange(waiting);
		}
	}
	
	private static class BatchActionsTableDataModel extends DefaultFlexiTableDataModel<BatchActionsRow> {

		public BatchActionsTableDataModel(FlexiTableColumnModel columnModel, List<BatchActionsRow> objects) {
			super(objects, columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			BatchActionsRow actions = getObject(row);
			
			switch (BatchActionCols.values()[col]) {
				case curriculumCoach: 
				case groupCoach:
					return actions.getCoach();
				case curriculumParticipant:
				case groupParticipant:
					return actions.getParticipant();
				case groupWaiting: 
					return actions.getWating();
				case curriculumOwner:
					return actions.getOwner();
				case empty:
				default:
					return null;
			}
		}

		@Override
		public DefaultFlexiTableDataModel<BatchActionsRow> createCopyWithEmptyList() {
			return null;
		}
		
	}
	
	private static class BatchActionsRow {
		private AddRemoveElement coach;
		private AddRemoveElement participant;
		private AddRemoveElement wating;
		private AddRemoveElement owner;
		
		public BatchActionsRow(AddRemoveElement coach, AddRemoveElement participant, AddRemoveElement waiting, AddRemoveElement owner) {
			this.coach = coach;
			this.participant = participant; 
			this.wating = waiting;
			this.owner = owner;
		}
		
		public AddRemoveElement getCoach() {
			return coach;
		}
		
		public AddRemoveElement getParticipant() {
			return participant;
		}
		
		public AddRemoveElement getWating() {
			return wating;
		}
		
		public AddRemoveElement getOwner() {
			return owner;
		}
	}
	

	private static class EditCurriculumMembershipTableDataModel extends DefaultFlexiTableDataModel<MemberCurriculumOption>  {
		
		public EditCurriculumMembershipTableDataModel(List<MemberCurriculumOption> options, FlexiTableColumnModel columnModel) {
			super(options, columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			MemberCurriculumOption option = getObject(row);
			switch(CurriculumCols.values()[col]) {
				case curriculum: return option.getElement().getCurriculum().getDisplayName();
				case curriculumElement: return option.getElement().getDisplayName();
				case curriculumElementIdentifier: return option.getElement().getIdentifier();
				case owner: return option.getOwner();
				case coach: return option.getCoach();
				case participant: return option.getParticipant();
				case masterCoach: return option.getMasterCoach();
				case elementOwner: return option.getElementOwner();
				default: return "ERROR";
			}
		}

		@Override
		public DefaultFlexiTableDataModel<MemberCurriculumOption> createCopyWithEmptyList() {
			return new EditCurriculumMembershipTableDataModel(new ArrayList<>(), this.getTableColumnModel());
		}
	}
	
	private static class EditGroupMembershipTableDataModel extends DefaultFlexiTableDataModel<MemberGroupOption>  {

		public EditGroupMembershipTableDataModel(List<MemberGroupOption> options, FlexiTableColumnModel columnModel) {
			super(options, columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			MemberGroupOption option = getObject(row);
			switch(GroupCols.values()[col]) {
				case groupName: return option.getGroupName();
				case tutorCount: return Long.valueOf(option.getTutorCount());
				case participantCount: return Long.valueOf(option.getParticipantCount() + option.getNumOfPendings());
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
		public EditGroupMembershipTableDataModel createCopyWithEmptyList() {
			return new EditGroupMembershipTableDataModel(new ArrayList<>(), getTableColumnModel());
		}
	}
	
	private static class CurriculumElementIndentRenderer implements FlexiCellRenderer {

		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
				URLBuilder ubu, Translator translator) {
			MemberCurriculumOption memberRow = (MemberCurriculumOption)source.getFlexiTableElement().getTableDataModel().getObject(row);
			indent(target, memberRow);
			if(cellValue instanceof String) {
				target.append(StringHelper.escapeHtml((String)cellValue));
			}
		}
		
		private void indent(StringOutput target, MemberCurriculumOption memberRow) {
			CurriculumElement element = memberRow.getElement();
			String path = element.getMaterializedPathKeys();
			if(StringHelper.containsNonWhitespace(path)) {
				char[] pathArr = path.toCharArray();
				for(int i=pathArr.length; i-->1; ) {
					if(pathArr[i] == '/') {
						target.append("&nbsp;&nbsp;");
					}
				}
			}
		}
	}
	
	public enum BatchActionCols implements FlexiColumnDef {
		empty("table.header.empty"),
		groupCoach("edit.members.group.select.all.coach"),
		groupParticipant("edit.members.group.select.all.participant"),
		groupWaiting("edit.members.group.select.all.waiting.list"),
		curriculumCoach("edit.members.curriculum.select.all.coach"),
		curriculumParticipant("edit.members.curriculum.select.all.participant"),
		curriculumOwner("edit.members.curriculum.select.all.owner");

		private final String i18n;
		
		private BatchActionCols(String i18n) {
			this.i18n = i18n;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18n;
		}
	}
	
	
	public enum CurriculumCols implements FlexiSortableColumnDef {
		curriculum("table.header.curriculum"),
		curriculumElement("table.header.curriculum.element"),
		curriculumElementIdentifier("table.header.identifier"),
		masterCoach("table.header.mastercoachs"),
		elementOwner("table.header.elementowners"),
		owner("table.header.owners"),
		coach("table.header.tutors"),
		participant("table.header.participants");
		
		private final String i18n;
		
		private CurriculumCols(String i18n) {
			this.i18n = i18n;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18n;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
	
	public enum GroupCols implements FlexiColumnDef {
		groupName("table.header.groups"),
		tutorCount("table.header.tutorsCount"),
		participantCount("table.header.participantsCount"),
		freePlaces("table.header.freePlace"),
		tutor("table.header.tutors"),
		participant("table.header.participants"),
		waitingList("table.header.waitingList");
		
		private final String i18n;
		
		private GroupCols(String i18n) {
			this.i18n = i18n;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18n;
		}
	}
}