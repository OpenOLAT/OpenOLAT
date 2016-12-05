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
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement.Layout;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRights;
import org.olat.group.right.BGRightsRole;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupsAndRightsController extends FormBasicController {
	
	private GroupsAndRightsDataModel tableDataModel;
	private FormLink removeAllLink;
	
	private final RepositoryEntry resource;
	@Autowired
	private BGRightManager rightManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	private static final String[] keys = {"ison"};
	private static final String[] values = {""};
	
	private final boolean readOnly;
	
	public GroupsAndRightsController(UserRequest ureq, WindowControl wControl, RepositoryEntry resource, boolean readOnly) {
		super(ureq, wControl, "right_list");
		this.readOnly = readOnly;
		this.resource = resource;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//group rights
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = 0;
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.groups", colIndex++));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.role", colIndex++));
		for(String right : CourseRights.getAvailableRights()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(right, colIndex++));
		}

		List<BGRightsOption> groupRights = loadModel();
		tableDataModel = new GroupsAndRightsDataModel(groupRights, tableColumnModel);
		uifactory.addTableElement(getWindowControl(), "rightList", tableDataModel, getTranslator(), formLayout);
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsLayout.setRootForm(mainForm);
		formLayout.add("buttons", buttonsLayout);
		
		if(!readOnly) {
			uifactory.addFormSubmitButton("save", buttonsLayout);
			removeAllLink = uifactory.addFormLink("remove.all", buttonsLayout, Link.BUTTON);
		}
	}
	
	private List<BGRightsOption> loadModel() {
		List<BGRightsOption> options = new ArrayList<BGRightsOption>();
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, resource, 0, -1);
		
		List<BGRights> currentRights = rightManager.findBGRights(groups, resource.getOlatResource());
		Map<Long,BGRights> tutorToRightsMap = new HashMap<Long,BGRights>();
		Map<Long,BGRights> participantToRightsMap = new HashMap<Long,BGRights>();
		for(BGRights right:currentRights) {
			if(right.getRole() == BGRightsRole.tutor) {
				tutorToRightsMap.put(right.getBusinessGroupKey(), right);
			} else if(right.getRole() == BGRightsRole.participant) {
				participantToRightsMap.put(right.getBusinessGroupKey(), right);
			}	
		}

		for(BusinessGroup group:groups) {
			options.add(getRightsOption(group, tutorToRightsMap.get(group.getKey()), BGRightsRole.tutor));
			options.add(getRightsOption(group, participantToRightsMap.get(group.getKey()), BGRightsRole.participant));
		}
		return options;
	}
	
	private BGRightsOption getRightsOption(BusinessGroup group, BGRights r, BGRightsRole role) {
		BGRightsOption options = new BGRightsOption(group, role);
		fillCheckbox(options, r == null ? null : r.getRights());
		FormLink rmLink = uifactory.addFormLink("remove_" + UUID.randomUUID().toString(), "table.header.remove", "table.header.remove", flc, Link.LINK);
		rmLink.setUserObject(options);
		return options;
	}
	
	private void fillCheckbox(BGRightsOption groupRights, List<String> permissions) {
		List<BGRight> selections = new ArrayList<BGRight>();
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
		String name = "cb" + UUID.randomUUID().toString().replace("-", "");
		MultipleSelectionElement selection = new MultipleSelectionElementImpl(name, Layout.horizontal);
		selection.setKeysAndValues(keys, values);
		selection.setEnabled(!readOnly);
		flc.add(name, selection);
		selection.select(keys[0], selected);
		return selection;
	}
	
	@Override
	protected void doDispose() {
		//
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
	
	private List<BusinessGroup> getGroups() {
		List<BusinessGroup> groups = new ArrayList<BusinessGroup>();
		for(BGRightsOption option:tableDataModel.getObjects()) {
			if(option.getRole() == BGRightsRole.tutor) {
				groups.add(option.getGroup());
			}
		}
		return groups;
	}
	
	private void doSaveChanges() {
		//collect group
		List<BGRightsOption> options = tableDataModel.getObjects();
		List<BusinessGroup> groups = getGroups();

		//collect current rights
		List<BGRights> currentRights = rightManager.findBGRights(groups, resource.getOlatResource());
		Map<Long,BGRights> tutorToRightsMap = new HashMap<Long,BGRights>();
		Map<Long,BGRights> participantToRightsMap = new HashMap<Long,BGRights>();
		for(BGRights right:currentRights) {
			if(right.getRole() == BGRightsRole.tutor) {
				tutorToRightsMap.put(right.getBusinessGroupKey(), right);
			} else if(right.getRole() == BGRightsRole.participant) {
				participantToRightsMap.put(right.getBusinessGroupKey(), right);
			}	
		}
		
		for(BGRightsOption option:options) {
			List<String> newPermissions = option.getSelectedPermissions();
			
			BGRights rights = null;
			if(option.getRole() == BGRightsRole.tutor) {
				rights = tutorToRightsMap.get(option.getGroupKey());
			} else if(option.getRole() == BGRightsRole.participant) {
				rights = participantToRightsMap.get(option.getGroupKey());
			}
			
			if(rights == null && newPermissions.isEmpty()) {
				continue;//nothing to do
			}
			List<String> currentPermissions = (rights == null ? Collections.<String>emptyList() : rights.getRights());
			if(newPermissions.containsAll(currentPermissions) && currentPermissions.containsAll(newPermissions)) {
				continue;//nothing to do
			}
			
			List<String> newPermissionsTmp = new ArrayList<String>(newPermissions);
			newPermissionsTmp.removeAll(currentPermissions);
			for(String newPermission:newPermissionsTmp) {
				rightManager.addBGRight(newPermission, option.getGroup(), resource.getOlatResource(), option.getRole());
			}
			
			currentPermissions.removeAll(newPermissions);
			for(String currentPermission:currentPermissions) {
				rightManager.removeBGRight(currentPermission, option.getGroup(), resource.getOlatResource(), option.getRole());
			}
		}
	}
	
	private void doRemoveAllRights() {
		List<BusinessGroup> groups = getGroups();
		rightManager.removeBGRights(groups, resource.getOlatResource());
		loadModel();
	}

	private class GroupsAndRightsDataModel extends DefaultTableDataModel<BGRightsOption> implements FlexiTableDataModel<BGRightsOption> {
		private FlexiTableColumnModel columnModel;
		
		public GroupsAndRightsDataModel(List<BGRightsOption> options, FlexiTableColumnModel columnModel) {
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
			BGRightsOption groupRights = getObject(row);
			if(col == 0) {
				return groupRights.getGroupName();
			} else if (col == 1) {
				BGRightsRole role = groupRights.getRole();
				switch(role) {
					case tutor: return translate("tutor");
					case participant: return translate("participant");
				}
				return "";
			}
			
			//rights
			int rightPos = col - 2;
			return groupRights.getRightsEl().get(rightPos).getSelection();
		}

		@Override
		public Object createCopyWithEmptyList() {
			return new GroupsAndRightsDataModel(new ArrayList<BGRightsOption>(), columnModel);
		}
	}
	
	private static class BGRight {
		private final String permission;
		private MultipleSelectionElement selection;
		
		public BGRight(String permission) {
			this.permission = permission;
		}

		public MultipleSelectionElement getSelection() {
			return selection;
		}

		public void setSelection(MultipleSelectionElement selection) {
			this.selection = selection;
		}

		public String getPermission() {
			return permission;
		}
	}
	
	private static class BGRightsOption {
		private final BusinessGroup group;
		private final BGRightsRole role;
		
		private List<BGRight> rightsEl;
		
		public BGRightsOption(BusinessGroup group, BGRightsRole role) {
			this.group = group;
			this.role = role;
		}
		
		public String getGroupName() {
			return group.getName();
		}

		public Long getGroupKey() {
			return group.getKey();
		}
		
		public BusinessGroup getGroup() {
			return group;
		}
		
		public BGRightsRole getRole() {
			return role;
		}
		
		public List<String> getSelectedPermissions() {
			List<String> permissions = new ArrayList<String>(rightsEl.size());
			for(BGRight rightEl:rightsEl) {
				if(rightEl.getSelection().isAtLeastSelected(1)) {
					permissions.add(rightEl.getPermission());
				}	
			}
			return permissions;
		}
		
		public List<BGRight> getRightsEl() {
			return rightsEl;
		}
		
		public void setRightsEl(List<BGRight> rightsEl) {
			this.rightsEl = rightsEl;
		}
	}
}
