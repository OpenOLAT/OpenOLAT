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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
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
import org.olat.course.member.PermissionHelper.BGPermission;
import org.olat.course.member.PermissionHelper.RepoPermission;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupView;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryMembership;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EditMemberController extends FormBasicController {
	
	private EditMemberTableDataModel tableDataModel;
	private MultipleSelectionElement repoRightsEl;
	private final MemberInfoController infoController;
	
	private static final String[] repoRightsKeys = {"owner", "tutor", "participant"};
	
	private final Identity member;
	private List<RepositoryEntryMembership> memberships;
	private List<BusinessGroupMembership> groupMemberships;
	
	private final RepositoryEntry repoEntry;
	private final RepositoryManager repositoryManager;
	private final BusinessGroupService businessGroupService;
	
	private static final String[] keys = new String[] { "ison" };
	private static final String[] values = new String[] {""};
	
	public EditMemberController(UserRequest ureq, WindowControl wControl, Identity member,
			RepositoryEntry repoEntry) {
		super(ureq, wControl, "edit_member");
		this.member = member;
		this.repoEntry = repoEntry;
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		memberships = repositoryManager.getRepositoryEntryMembership(repoEntry, member);

		infoController = new MemberInfoController(ureq, wControl, member);
		initForm(ureq);
		loadModel(member);
	}
	
	private void loadModel(Identity member) {
		List<BusinessGroupView> groups = businessGroupService.findBusinessGroupViews(null, repoEntry.getOlatResource(), 0, -1);
		List<Long> businessGroupKeys = PersistenceHelper.toKeys(groups);
		
		groupMemberships = businessGroupService.getBusinessGroupMembership(businessGroupKeys, member);
		List<MemberOption> options = new ArrayList<MemberOption>();
		for(BusinessGroupView group:groups) {
			MemberOption option = new MemberOption(group);
			BGPermission bgPermission = PermissionHelper.getPermission(group.getKey(), member, groupMemberships);
			option.setTutor(createSelection(bgPermission.isTutor()));
			option.setParticipant(createSelection(bgPermission.isParticipant()));
			option.setWaiting(createSelection(bgPermission.isWaitingList()));
			options.add(option);
		}
		
		tableDataModel.setObjects(options);
	}
	
	private MultipleSelectionElement createSelection(boolean selected) {
		String name = "cb" + UUID.randomUUID().toString().replace("-", "");
		MultipleSelectionElement selection = new MultipleSelectionElementImpl(name, MultipleSelectionElementImpl.createVerticalLayout("checkbox",1));
		selection.setKeysAndValues(keys, values, null);
		flc.add(name, selection);
		selection.select(keys[0], selected);
		return selection;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.put("infos", infoController.getInitialComponent());

			String title = translate("edit.member.title", new String[]{ repoEntry.getDisplayname() });
			layoutCont.contextPut("editTitle", title);
		}
		//repository entry rights
		
		String[] repoValues = new String[] {
				translate("role.owner"), translate("role.tutor"), translate("role.participant")
		};
		repoRightsEl = uifactory.addCheckboxesVertical("repoRights", formLayout, repoRightsKeys, repoValues, null, 1);
		RepoPermission repoPermission = PermissionHelper.getPermission(repoEntry, member, memberships);
		repoRightsEl.select("owner", repoPermission.isOwner());
		repoRightsEl.select("tutor", repoPermission.isTutor());
		repoRightsEl.select("participant", repoPermission.isParticipant());

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
		uifactory.addTableElement("groupList", tableDataModel, formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setRootForm(mainForm);
		uifactory.addFormSubmitButton("ok", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
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
	
	protected void collectRepoChanges(MemberPermissionChangeEvent e) {
		RepoPermission repoPermission = PermissionHelper.getPermission(repoEntry, member, memberships);

		Set<String>	selectRepoRights = repoRightsEl.getSelectedKeys();
		boolean repoOwner = selectRepoRights.contains("owner");
		e.setRepoOwner(repoOwner == repoPermission.isOwner() ? null : new Boolean(repoOwner));
		boolean repoTutor = selectRepoRights.contains("tutor");
		e.setRepoTutor(repoTutor == repoPermission.isTutor() ? null : new Boolean(repoTutor));
		boolean repoParticipant = selectRepoRights.contains("participant");
		e.setRepoParticipant(repoParticipant == repoPermission.isParticipant() ? null : new Boolean(repoParticipant));
	}
	
	protected void collectGroupChanges(MemberPermissionChangeEvent e) {
		List<BusinessGroupMembershipChange> changes = new ArrayList<BusinessGroupMembershipChange>();
		
		for(MemberOption option:tableDataModel.getObjects()) {
			BGPermission bgPermission = PermissionHelper.getPermission(option.getGroupKey(), member, groupMemberships);
			BusinessGroupMembershipChange change = new BusinessGroupMembershipChange(member, option.getGroupKey());
			boolean bgTutor = option.getTutor().isAtLeastSelected(1);
			change.setTutor(bgPermission.isTutor() == bgTutor ? null : new Boolean(bgTutor));
			boolean bgParticipant = option.getParticipant().isAtLeastSelected(1);
			change.setParticipant(bgPermission.isParticipant() == bgParticipant ? null : new Boolean(bgParticipant));
			boolean bgWaitingList = option.getWaiting().isAtLeastSelected(1);
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
		public int getColumnCount() {
			return columnModel.getColumnCount();
		}

		@Override
		public Object getValueAt(int row, int col) {
			MemberOption option = getObject(row);
			switch(Cols.values()[col]) {
				case groupName: return option.getGroupName();
				case tutorCount: return new Long(option.getTutorCount());
				case participantCount: return new Long(option.getParticipantCount());
				case freePlaces: {
					Integer maxParticipants = option.getMaxParticipants();
					if(maxParticipants != null && maxParticipants.intValue() > 0) {
						long free = maxParticipants - option.getParticipantCount();
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