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
package org.olat.course.groupsandrights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.Group;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.groupsandrights.BGRightsRow.BGRight;
import org.olat.course.groupsandrights.GroupsAndRightsDataModel.GroupRightCols;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRights;
import org.olat.group.right.BGRightsRole;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupsAndRightsController extends FormBasicController {
	
	protected static final int PERMISSIONS_OFFSET = 500;

	private FormLink removeAllLink;
	private FlexiTableElement tableEl;
	private GroupsAndRightsDataModel tableDataModel;
	
	@Autowired
	private BGRightManager rightManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryService repositoryService;
	
	private static final String[] keys = {"ison"};
	private static final String[] values = {""};
	
	private int counter = 0;
	private final boolean readOnly;
	private final RepositoryEntry courseEntry;
	
	public GroupsAndRightsController(UserRequest ureq, WindowControl wControl, RepositoryEntry resource, boolean readOnly) {
		super(ureq, wControl, "right_list");
		this.readOnly = readOnly;
		this.courseEntry = resource;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//group rights
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GroupRightCols.key));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupRightCols.title, new BGRightsResourceNameRenderer()));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, GroupRightCols.externalId));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupRightCols.role));
		
		int colIndex = PERMISSIONS_OFFSET;
		for(String right : CourseRights.getAvailableRights()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(right, colIndex++));
		}

		tableDataModel = new GroupsAndRightsDataModel(tableColumnModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "rightList", tableDataModel, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "groups-rights-v1");
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsLayout.setRootForm(mainForm);
		formLayout.add("buttons", buttonsLayout);
		
		if(!readOnly) {
			uifactory.addFormSubmitButton("save", buttonsLayout);
			removeAllLink = uifactory.addFormLink("remove.all", buttonsLayout, Link.BUTTON);
		}
	}
	
	private void loadModel() {
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, courseEntry, 0, -1);
		List<Group> baseGroups = getAllBaseGroups(groups);
		List<BGRights> currentRights = rightManager.findBGRights(baseGroups, courseEntry.getOlatResource());
		
		Map<Group,BGRights> tutorToRightsMap = new HashMap<>();
		Map<Group,BGRights> participantToRightsMap = new HashMap<>();
		for(BGRights right:currentRights) {
			if(right.getRole() == BGRightsRole.tutor) {
				tutorToRightsMap.put(right.getBaseGroup(), right);
			} else if(right.getRole() == BGRightsRole.participant) {
				participantToRightsMap.put(right.getBaseGroup(), right);
			}	
		}

		List<BGRightsRow> options = new ArrayList<>();
		String courseName = courseEntry.getDisplayname();
		Group defGroup = repositoryService.getDefaultGroup(courseEntry);
		options.add(getRightsOption(defGroup, courseEntry.getKey(), courseName, courseEntry.getExternalId(), courseEntry.getManagedFlagsString(),
				tutorToRightsMap.get(defGroup), BGRightsRole.tutor, BGRightsResourceType.repositoryEntry));
		options.add(getRightsOption(defGroup, courseEntry.getKey(), courseName, courseEntry.getExternalId(), courseEntry.getManagedFlagsString(),
				participantToRightsMap.get(defGroup), BGRightsRole.participant, BGRightsResourceType.repositoryEntry));

		for(BusinessGroup group:groups) {
			String name = group.getName();
			Group bGroup = group.getBaseGroup();
			options.add(getRightsOption(bGroup, group.getKey(), name, group.getExternalId(), group.getManagedFlagsString(),
					tutorToRightsMap.get(bGroup), BGRightsRole.tutor, BGRightsResourceType.businessGroup));
			options.add(getRightsOption(bGroup, group.getKey(), name, group.getExternalId(), group.getManagedFlagsString(),
					participantToRightsMap.get(bGroup), BGRightsRole.participant, BGRightsResourceType.businessGroup));
		}
		tableDataModel.setObjects(options);
	}
	
	private BGRightsRow getRightsOption(Group group, Long key, String name, String externalId, String managedFlags,
			BGRights r, BGRightsRole role, BGRightsResourceType type) {
		BGRightsRow options = new BGRightsRow(group, key, name, externalId, managedFlags, role, type);
		fillCheckbox(options, r == null ? null : r.getRights());
		FormLink rmLink = uifactory.addFormLink("remove_" + (++counter), "table.header.remove", "table.header.remove", flc, Link.LINK);
		rmLink.setUserObject(options);
		return options;
	}
	
	private void fillCheckbox(BGRightsRow groupRights, List<String> permissions) {
		List<BGRight> selections = new ArrayList<>();
		for(String permission : CourseRights.getAvailableRights()) {
			BGRight permissionEl = new BGRight(permission);
			boolean selected = permissions == null ? false : permissions.contains(permission);
			MultipleSelectionElement selection = createSelection(selected);
			permissionEl.setSelection(selection);
			selection.setUserObject(permissionEl);
			selections.add(permissionEl);
		}
		groupRights.setRightsEl(selections);
	}
	
	private MultipleSelectionElement createSelection(boolean selected) {
		String name = "cb_" + (++counter);
		MultipleSelectionElement selection = uifactory.addCheckboxesHorizontal(name, null, flc, keys, values);
		selection.setEnabled(!readOnly);
		flc.add(name, selection);
		if(selected) {
			selection.select(keys[0], true);
		}
		return selection;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		doSaveChanges();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == removeAllLink) {
			doRemoveAllRights();
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	private void doSaveChanges() {
		//collect group
		List<BGRightsRow> options = tableDataModel.getObjects();
		List<Group> groups = getAllBaseGroups();

		//collect current rights
		List<BGRights> currentRights = rightManager.findBGRights(groups, courseEntry.getOlatResource());
		Map<Group,BGRights> tutorToRightsMap = new HashMap<>();
		Map<Group,BGRights> participantToRightsMap = new HashMap<>();
		for(BGRights right:currentRights) {
			if(right.getRole() == BGRightsRole.tutor) {
				tutorToRightsMap.put(right.getBaseGroup(), right);
			} else if(right.getRole() == BGRightsRole.participant) {
				participantToRightsMap.put(right.getBaseGroup(), right);
			}	
		}
		
		for(BGRightsRow option:options) {
			List<String> newPermissions = option.getSelectedPermissions();
			
			BGRights rights = null;
			if(option.getRole() == BGRightsRole.tutor) {
				rights = tutorToRightsMap.get(option.getBaseGroup());
			} else if(option.getRole() == BGRightsRole.participant) {
				rights = participantToRightsMap.get(option.getBaseGroup());
			}
			
			if(rights == null && newPermissions.isEmpty()) {
				continue;//nothing to do
			}
			List<String> currentPermissions = (rights == null ? Collections.<String>emptyList() : rights.getRights());
			if(newPermissions.containsAll(currentPermissions) && currentPermissions.containsAll(newPermissions)) {
				continue;//nothing to do
			}
			
			List<String> newPermissionsTmp = new ArrayList<>(newPermissions);
			newPermissionsTmp.removeAll(currentPermissions);
			for(String newPermission:newPermissionsTmp) {
				rightManager.addBGRight(newPermission, option.getBaseGroup(), courseEntry.getOlatResource(), option.getRole());
			}
			
			currentPermissions.removeAll(newPermissions);
			for(String currentPermission:currentPermissions) {
				rightManager.removeBGRight(currentPermission, option.getBaseGroup(), courseEntry.getOlatResource(), option.getRole());
			}
		}
	}
	
	private List<Group> getAllBaseGroups() {
		List<BusinessGroup> businessGroups = businessGroupService.findBusinessGroups(null, courseEntry, 0, -1);
		return getAllBaseGroups(businessGroups);
	}
	
	private List<Group> getAllBaseGroups(List<BusinessGroup> businessGroups) {
		List<Group> baseGroups = new ArrayList<>(businessGroups.size() + 1);
		Group group = repositoryService.getDefaultGroup(courseEntry);
		baseGroups.add(group);
		for(BusinessGroup businessGroup:businessGroups) {
			baseGroups.add(businessGroup.getBaseGroup());
		}
		return baseGroups;
	}
	
	private void doRemoveAllRights() {
		List<Group> groups = getAllBaseGroups();
		rightManager.removeBGRights(groups, courseEntry.getOlatResource());
		loadModel();
		tableEl.reset(false, false, true);
	}
}
