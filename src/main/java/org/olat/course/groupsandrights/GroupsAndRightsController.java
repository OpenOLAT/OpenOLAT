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
import java.util.List;
import java.util.UUID;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
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
import org.olat.resource.OLATResource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupsAndRightsController extends FormBasicController {
	
	private GroupsAndRightsDataModel tableDataModel;
	private FormLink addAllLink, removeAllLink;
	private FormSubmit saveLink;
	
	private final OLATResource resource;
	private final BGRightManager rightManager;
	private final BusinessGroupService businessGroupService;
	
	private static final String[] keys = {"ison"};
	private static final String[] values = {""};
	
	
	public GroupsAndRightsController(UserRequest ureq, WindowControl wControl, OLATResource resource) {
		super(ureq, wControl, "right_list");
		
		rightManager = CoreSpringFactory.getImpl(BGRightManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		this.resource = resource;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//group rights
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.groups"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.role"));
		for(String right : CourseRights.getAvailableRights()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(right));
		}
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.remove"));

		List<RightOption> groupRights = loadModel();
		tableDataModel = new GroupsAndRightsDataModel(groupRights, tableColumnModel);
		uifactory.addTableElement("rightList", tableDataModel, formLayout);
		
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsLayout.setRootForm(mainForm);
		formLayout.add("buttons", buttonsLayout);
		
		saveLink = uifactory.addFormSubmitButton("save", buttonsLayout);
		removeAllLink = uifactory.addFormLink("remove.all", buttonsLayout, Link.BUTTON);
		addAllLink = uifactory.addFormLink("add.all", buttonsLayout, Link.BUTTON);
	}
	
	private List<RightOption> loadModel() {
		List<RightOption> options = new ArrayList<RightOption>();
		
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, resource, 0, -1);
		for(BusinessGroup group:groups) {
			RightOption groupRights = new RightOption(group, "tutor");
			fillCheckbox(groupRights);
			FormLink removeLink = uifactory.addFormLink("remove_" + UUID.randomUUID().toString(), "table.header.remove", "table.header.remove", flc, Link.LINK);
			removeLink.setUserObject(groupRights);
			groupRights.setRemoveLink(removeLink);
			
			options.add(groupRights);
		}
		return options;
	}
	
	private void fillCheckbox(RightOption groupRights) {
		List<MultipleSelectionElement> selections = new ArrayList<MultipleSelectionElement>();
		for(String right : CourseRights.getAvailableRights()) {
			MultipleSelectionElement selection = createSelection(false);
			selection.setUserObject(groupRights);
			selections.add(selection);
		}
		groupRights.setRightsEl(selections);
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
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
		
		System.out.println();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addAllLink) {
			
		} else if (source == removeAllLink) {
			doRemoveAllRights();
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	private void doRemoveAllRights() {
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, resource, 0, -1);
		for(BusinessGroup group:groups) {
			
			List<String> rights = rightManager.findBGRights(group);
			
			
			
		}
		
		
		
		
	}

	private static class GroupsAndRightsDataModel extends DefaultTableDataModel<RightOption> implements FlexiTableDataModel {
		private FlexiTableColumnModel columnModel;
		
		public GroupsAndRightsDataModel(List<RightOption> options, FlexiTableColumnModel columnModel) {
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
			RightOption groupRights = getObject(row);
			if(col == 0) {
				return groupRights.getGroupName();
			} else if (col == 1) {
				return groupRights.getRole();
			} else if (col == (getColumnCount() - 1)) {
				return groupRights.getRemoveLink();
			}
			
			//rights
			int rightPos = col - 2;
			MultipleSelectionElement rightEl = groupRights.getRightsEl().get(rightPos);
			return rightEl;
		}
	}
	
	private static class RightOption {
		private final String groupName;
		private final Long groupKey;
		private final String role;
		
		private List<MultipleSelectionElement> rightsEl;
		private FormLink removeLink;
		
		public RightOption(BusinessGroup group, String role) {
			groupName = group.getName();
			groupKey = group.getKey();
			this.role = role;
		}
		
		public String getGroupName() {
			return groupName;
		}

		public Long getGroupKey() {
			return groupKey;
		}
		
		public String getRole() {
			return role;
		}
		
		public List<MultipleSelectionElement> getRightsEl() {
			return rightsEl;
		}
		
		public void setRightsEl(List<MultipleSelectionElement> rightsEl) {
			this.rightsEl = rightsEl;
		}
		
		public FormLink getRemoveLink() {
			return removeLink;
		}
		
		public void setRemoveLink(FormLink removeLink) {
			this.removeLink = removeLink;
		}
	}
}
