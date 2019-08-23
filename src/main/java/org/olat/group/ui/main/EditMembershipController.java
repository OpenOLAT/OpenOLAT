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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement.Layout;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
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
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumElementRow;
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
	
	private FlexiTableElement groupTableEl;
	private FlexiTableElement curriculumTableEl;
	private MultipleSelectionElement repoRightsEl;
	private FormLink selectAllCurriculumOwnersButton;
	private FormLink selectAllCurriculumCoachesButton;
	private FormLink selectAllCurriculumParticipantsButton;
	private EditGroupMembershipTableDataModel groupTableDataModel;
	private EditCurriculumMembershipTableDataModel curriculumTableDataModel;
	
	private static final String[] repoRightsKeys = {"owner", "tutor", "participant"};
	
	private final Identity member;
	private final List<Identity> members;
	private List<RepositoryEntryMembership> memberships;
	private List<BusinessGroupMembership> groupMemberships;
	private List<CurriculumElementMembership> curriculumElementMemberships;
	
	private boolean withButtons;
	private final boolean overrideManaged;
	private final BusinessGroup businessGroup;
	private final RepositoryEntry repoEntry;
	private final Curriculum curriculum;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;
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
		this.curriculum = null;
		this.withButtons = true;
		this.overrideManaged = overrideManaged;
		
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
		super(ureq, wControl, "edit_member");
		
		this.member = null;
		this.members = (members == null ? null : new ArrayList<>(members));
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		this.curriculum = null;
		this.withButtons = true;
		this.overrideManaged = overrideManaged;
		
		memberships = Collections.emptyList();

		initForm(ureq);
		loadModel(ureq, member);
	}
	
	public EditMembershipController(UserRequest ureq, WindowControl wControl, List<Identity> members,
			RepositoryEntry repoEntry, BusinessGroup businessGroup, Curriculum curriculum, boolean overrideManaged, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "edit_member", rootForm);
		
		this.member = null;
		this.members = (members == null ? null : new ArrayList<>(members));
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		this.curriculum = curriculum;
		this.withButtons = false;
		this.overrideManaged = overrideManaged;
		
		memberships = Collections.emptyList();

		initForm(ureq);
		loadModel(ureq, member);
	}
	
	private void loadModel(UserRequest ureq, Identity memberToLoad) {
		Roles roles = ureq.getUserSession().getRoles();
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		if(repoEntry == null && businessGroup != null) {
			params.setBusinessGroupKey(businessGroup.getKey());
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
		
		List<MemberGroupOption> options = new ArrayList<>();
		for(StatisticsBusinessGroupRow group:groups) {
			boolean managed = BusinessGroupManagedFlag.isManaged(group.getManagedFlags(), BusinessGroupManagedFlag.membersmanagement) && !overrideManaged;
            boolean excludeGroupCoachesFromMembersManagementEnabled = BusinessGroupManagedFlag.isManaged(group.getManagedFlags(), BusinessGroupManagedFlag.excludeGroupCoachesFromMembersmanagement);
			BGPermission bgPermission = PermissionHelper.getPermission(group.getKey(), memberToLoad, groupMemberships);
			option.setTutor(createSelection(bgPermission.isTutor(), !managed || excludeGroupCoachesFromMembersManagementEnabled, GroupRoles.coach.name()));
			option.setParticipant(createSelection(bgPermission.isParticipant() || defaultMembership, !managed, GroupRoles.participant.name()));
			boolean waitingListEnable = !managed && group.isWaitingListEnabled();
			option.setWaiting(createSelection(bgPermission.isWaitingList(), waitingListEnable, GroupRoles.waiting.name()));
			options.add(option);
		}
		
		groupTableDataModel.setObjects(options);
		groupTableEl.setVisible(!options.isEmpty());
		
		List<CurriculumElement> curriculumElements;
		if(curriculum != null) {
			curriculumElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.notDeleted());
			curriculumElements = orderCurriculumElements(curriculumElements);
		} else if (repoEntry != null) {
			curriculumElements = curriculumService.getCurriculumElements(repoEntry);
		} else {
			curriculumElements = Collections.emptyList();
		}
	
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
			option.setOwner(createSelection(rePermission.isOwner(), !managed, GroupRoles.owner.name()));
			option.setCoach(createSelection(rePermission.isTutor(), !managed, GroupRoles.coach.name()));
			option.setParticipant(createSelection(rePermission.isParticipant() || defaultMembership, !managed, GroupRoles.participant.name()));
			curriculumOptions.add(option);
		}
		
		curriculumTableDataModel.setObjects(curriculumOptions);
		curriculumTableEl.setVisible(!curriculumOptions.isEmpty());
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
		groupTableEl = uifactory.addTableElement(getWindowControl(), "groupList", groupTableDataModel, getTranslator(), formLayout);
		groupTableEl.setCustomizeColumns(false);
		groupTableEl.setNumOfRowsEnabled(false);
		
		// curriculum rights
		FlexiTableColumnModel curriculumTableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(curriculum == null) {
			curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.curriculum));
			curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.curriculumElement));
		} else {
			curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.curriculumElement, new CurriculumElementIndentRenderer()));
		}
		curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.owner));
		curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.coach));
		curriculumTableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.participant));
		
		curriculumTableDataModel = new EditCurriculumMembershipTableDataModel(Collections.<MemberCurriculumOption>emptyList(), curriculumTableColumnModel);
		curriculumTableEl = uifactory.addTableElement(getWindowControl(), "curriculumList", curriculumTableDataModel, getTranslator(), formLayout);
		curriculumTableEl.setCustomizeColumns(false);
		curriculumTableEl.setNumOfRowsEnabled(false);
		
		selectAllCurriculumOwnersButton = uifactory.addFormLink("select.all.curriculum.owners", formLayout, Link.BUTTON_SMALL);
		selectAllCurriculumOwnersButton.setIconLeftCSS("o_icon o_icon_check_on");
		selectAllCurriculumOwnersButton.setUserObject(Boolean.TRUE);
		selectAllCurriculumCoachesButton = uifactory.addFormLink("select.all.curriculum.coaches", formLayout, Link.BUTTON_SMALL);
		selectAllCurriculumCoachesButton.setIconLeftCSS("o_icon o_icon_check_on");
		selectAllCurriculumCoachesButton.setUserObject(Boolean.TRUE);
		selectAllCurriculumParticipantsButton = uifactory.addFormLink("select.all.curriculum.participants", formLayout, Link.BUTTON_SMALL);
		selectAllCurriculumParticipantsButton.setIconLeftCSS("o_icon o_icon_check_on");
		selectAllCurriculumParticipantsButton.setUserObject(Boolean.TRUE);

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
		if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement selectEl = (MultipleSelectionElement)source;
			if(selectEl.isSelected(0)) {
				for(MemberGroupOption option:groupTableDataModel.getObjects()) {
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
		} else if(selectAllCurriculumOwnersButton == source) {
			toogleCurriculumMembership(selectAllCurriculumOwnersButton, GroupRoles.owner,
					"select.all.curriculum.owners", "deselect.all.curriculum.owners");
		} else if(selectAllCurriculumCoachesButton == source) {
			toogleCurriculumMembership(selectAllCurriculumCoachesButton, GroupRoles.coach,
					"select.all.curriculum.coaches", "deselect.all.curriculum.coaches");
		} else if(selectAllCurriculumParticipantsButton == source) {
			toogleCurriculumMembership(selectAllCurriculumParticipantsButton, GroupRoles.participant,
					"select.all.curriculum.participants", "deselect.all.curriculum.participants");
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void toogleCurriculumMembership(FormLink link, GroupRoles role, String selectI18nKey, String deselectI18nKey) {
		Boolean enabled = (Boolean)link.getUserObject();
		
		List<MemberCurriculumOption> memberOptions = curriculumTableDataModel.getObjects();
		for(MemberCurriculumOption memberOption:memberOptions) {
			MultipleSelectionElement element = memberOption.getSelection(role);
			if(element.isEnabled()) {
				element.select(keys[0], enabled.booleanValue());
			}
		}
		
		boolean newValue = !enabled.booleanValue();
		link.setUserObject(Boolean.valueOf(newValue));
		if(newValue) {
			link.setIconLeftCSS("o_icon o_icon_check_on");
			link.setI18nKey(selectI18nKey);
		} else {
			link.setIconLeftCSS("o_icon o_icon_check_off");
			link.setI18nKey(deselectI18nKey);
		}
	}

	public void collectRepoChanges(MemberPermissionChangeEvent e) {
		if(repoEntry == null) return;
		
		RepoPermission repoPermission = PermissionHelper.getPermission(repoEntry, member, memberships);

		Collection<String> selectRepoRights = repoRightsEl.getSelectedKeys();
		boolean repoOwner = selectRepoRights.contains("owner");
		e.setRepoOwner(repoOwner == repoPermission.isOwner() ? null : Boolean.valueOf(repoOwner));
		boolean repoTutor = selectRepoRights.contains("tutor");
		e.setRepoTutor(repoTutor == repoPermission.isTutor() ? null : Boolean.valueOf(repoTutor));
		boolean repoParticipant = selectRepoRights.contains("participant");
		e.setRepoParticipant(repoParticipant == repoPermission.isParticipant() ? null : Boolean.valueOf(repoParticipant));
	}
	
	public void collectGroupChanges(MemberPermissionChangeEvent e) {
		List<BusinessGroupMembershipChange> changes = new ArrayList<>();
		
		for(MemberGroupOption option:groupTableDataModel.getObjects()) {
			BGPermission bgPermission = PermissionHelper.getPermission(option.getGroupKey(), member, groupMemberships);
			BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(member, option.getGroup());
			boolean bgTutor = option.getTutor().isAtLeastSelected(1);
			change.setTutor(bgPermission.isTutor() == bgTutor ? null : Boolean.valueOf(bgTutor));
			boolean bgParticipant = option.getParticipant().isAtLeastSelected(1);
			change.setParticipant(bgPermission.isParticipant() == bgParticipant ? null : Boolean.valueOf(bgParticipant));
			boolean bgWaitingList = option.getWaiting().isEnabled() && option.getWaiting().isAtLeastSelected(1);
			change.setWaitingList(bgPermission.isWaitingList() == bgWaitingList ? null : Boolean.valueOf(bgWaitingList));

			if(change.getTutor() != null || change.getParticipant() != null || change.getWaitingList() != null) {
				changes.add(change);
			}
		}
		e.setGroupChanges(changes);
	}
	
	public void collectCurriculumElementChanges(MemberPermissionChangeEvent e) {
		List<CurriculumElementMembershipChange> changes = new ArrayList<>();
		
		for(MemberCurriculumOption option:curriculumTableDataModel.getObjects()) {
			RepoPermission rePermission = PermissionHelper.getPermission(option.getElement(), member, curriculumElementMemberships);
			CurriculumElementMembershipChange change = new CurriculumElementMembershipChange(member, option.getElement());
			boolean cOwner = option.getOwner().isAtLeastSelected(1);
			change.setRepositoryEntryOwner(rePermission.isOwner() == cOwner ? null : Boolean.valueOf(cOwner));
			boolean cCoach = option.getCoach().isAtLeastSelected(1);
			change.setCoach(rePermission.isTutor() == cCoach ? null : Boolean.valueOf(cCoach));
			boolean cParticipant = option.getParticipant().isAtLeastSelected(1);
			change.setParticipant(rePermission.isParticipant() == cParticipant ? null : Boolean.valueOf(cParticipant));
			if(change.getCurriculumElementOwner() != null || change.getRepositoryEntryOwner() != null || change.getCoach() != null || change.getParticipant() != null) {
				changes.add(change);
			}
		}
		
		e.setCurriculumChanges(changes);
	}
	
	private static class MemberCurriculumOption {

		private final CurriculumElement curriculumElement;
		private MultipleSelectionElement owner;
		private MultipleSelectionElement coach;
		private MultipleSelectionElement participant;
		
		public MemberCurriculumOption(CurriculumElement curriculumElement) {
			this.curriculumElement = curriculumElement;
		}
		
		public CurriculumElement getElement() {
			return curriculumElement;
		}

		public MultipleSelectionElement getOwner() {
			return owner;
		}

		public void setOwner(MultipleSelectionElement owner) {
			this.owner = owner;
		}

		public MultipleSelectionElement getCoach() {
			return coach;
		}

		public void setCoach(MultipleSelectionElement coach) {
			this.coach = coach;
		}

		public MultipleSelectionElement getParticipant() {
			return participant;
		}

		public void setParticipant(MultipleSelectionElement participant) {
			this.participant = participant;
		}
		
		public MultipleSelectionElement getSelection(GroupRoles role) {
			switch(role) {
				case owner: return owner;
				case coach: return coach;
				case participant: return participant;
				default: return null;
			}
		}
	}

	private static class MemberGroupOption {
		private final StatisticsBusinessGroupRow group;
		private MultipleSelectionElement tutor;
		private MultipleSelectionElement participant;
		private MultipleSelectionElement waiting;
		
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
				case owner: return option.getOwner();
				case coach: return option.getCoach();
				case participant: return option.getParticipant();
			}
			return null;
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
	
	
	public static enum CurriculumCols implements FlexiColumnDef {
		curriculum("table.header.curriculum"),
		curriculumElement("table.header.curriculum.element"),
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
	}
	
	public static enum GroupCols implements FlexiColumnDef {
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
