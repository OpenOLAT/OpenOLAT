/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.course.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.en.ENEditGroupTableContentRow;
import org.olat.course.nodes.en.ENEditGroupTableModel;
import org.olat.course.nodes.en.ENEditGroupTableModel.ENEditGroupTableColumns;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.ui.NewBGController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial Date: 15.06.2007 <br>
 * @author patrickb
 */
public class GroupSelectionController extends FormBasicController {

	private FormLink createNew;
	private CourseGroupManager courseGrpMngr;
	private NewBGController groupCreateCntrllr;
	private CloseableModalController cmc;
	
	private boolean createEnable;
	private boolean showSave;
	
	private FlexiTableElement groupTableElement;
	private ENEditGroupTableModel groupTableModel;
	private List<ENEditGroupTableContentRow> groupTableRows;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	public GroupSelectionController(UserRequest ureq, WindowControl wControl, boolean allowCreate,
			CourseGroupManager courseGrpMngr, List<Long> selectionKeys) {
		super(ureq, wControl, "group_or_area_selection");
		this.courseGrpMngr = courseGrpMngr;
		this.showSave = true;
		
		init(ureq, allowCreate, courseGrpMngr, selectionKeys);
	}
	
	public GroupSelectionController(UserRequest ureq, WindowControl wControl, Form rootForm, boolean allowCreate,
			CourseGroupManager courseGrpMngr, List<Long> selectionKeys) {
		super(ureq, wControl, LAYOUT_CUSTOM, "group_or_area_selection", rootForm);
		this.courseGrpMngr = courseGrpMngr;
		this.showSave = false;
		
		init(ureq, allowCreate, courseGrpMngr, selectionKeys);
	}
	
	private void init(UserRequest ureq, boolean allowCreate, CourseGroupManager courseGrpMngr,
			List<Long> selectionKeys) {
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(courseGrpMngr.getCourseResource(), false);
		createEnable = allowCreate && !RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.groups);
		
		initForm(ureq);
		loadModel(selectionKeys);
	}
	
	public void loadModel(List<Long> selectionKeys) {
		List<BusinessGroup> groups = courseGrpMngr.getAllBusinessGroups();
		
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		
		List<Long> keys = new ArrayList<>();
		for (BusinessGroup group : groups) {
			keys.add(group.getKey());
		}
		
		params.setBusinessGroupKeys(keys);
		Map<Long, StatisticsBusinessGroupRow> stats = businessGroupService.findBusinessGroupsStatistics(params).stream().collect(Collectors.toMap(StatisticsBusinessGroupRow::getKey, g -> g, (u, v) -> u));
		
		groupTableRows = new ArrayList<>();
		Set<Integer> selectedRows = new HashSet<>();
		
		for (BusinessGroup businessGroup : groups) {
			groupTableRows.add(new ENEditGroupTableContentRow(businessGroup, stats.get(businessGroup.getKey())));
			for (Long selectionKey : selectionKeys) {
				if (selectionKey.equals(businessGroup.getKey())) {
					selectedRows.add(groupTableRows.size() - 1);
					break;
				}
			}
		}
		
		groupTableModel.setObjects(groupTableRows);
		groupTableElement.reset(true, true, true);
		groupTableElement.setMultiSelectedIndex(selectedRows);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, org.olat.core.gui.components.form.flexible.FormItem source,
			org.olat.core.gui.components.form.flexible.impl.FormEvent event) {
		if (source == createNew) {
			// user wants to create a new group -> show group create form
			removeAsListenerAndDispose(groupCreateCntrllr);
			
			RepositoryEntry re = repositoryManager.lookupRepositoryEntry(courseGrpMngr.getCourseResource(), false);
			groupCreateCntrllr = new NewBGController(ureq, getWindowControl(), re, true, null);
			listenTo(groupCreateCntrllr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(),"close",groupCreateCntrllr.getInitialComponent()
			);
			listenTo(cmc);
			cmc.activate();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == groupCreateCntrllr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				// select new value
				Collection<BusinessGroup> newGroups = groupCreateCntrllr.getCreatedGroups();
				List<Integer> selectedRows = new ArrayList<>(groupTableElement.getMultiSelectedIndex());
				List<Long> keys = new ArrayList<>();
				
				for (BusinessGroup businessGroup : newGroups) {
					keys.add(businessGroup.getKey());
				}
				
				BusinessGroupQueryParams params = new BusinessGroupQueryParams();
				params.setBusinessGroupKeys(keys);
				
				Map<Long, StatisticsBusinessGroupRow> stats = businessGroupService.findBusinessGroupsStatistics(params).stream().collect(Collectors.toMap(StatisticsBusinessGroupRow::getKey, g -> g, (u, v) -> u));
				
				for(BusinessGroup newGroup : newGroups) {
					groupTableRows.add(new ENEditGroupTableContentRow(newGroup, stats.get(newGroup.getKey())));
					selectedRows.add(groupTableRows.size() - 1);
				}
				groupTableModel.setObjects(groupTableRows);
				groupTableElement.reset(true, true, true);
				groupTableElement.setMultiSelectedIndex(new HashSet<>(selectedRows));
				//inform condition config easy about new groups -> which informs further
				fireEvent(ureq, Event.CHANGED_EVENT);
			} 
		} 
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(createEnable) {
			// easy creation only possible if a default group context available
			createNew = uifactory.addFormLink("create", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel keyColumn = new DefaultFlexiColumnModel(ENEditGroupTableColumns.key);
		keyColumn.setDefaultVisible(true);
		keyColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(keyColumn);

		DefaultFlexiColumnModel groupColumn = new DefaultFlexiColumnModel(ENEditGroupTableColumns.groupName);
		groupColumn.setDefaultVisible(true);
		groupColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(groupColumn);
		
		DefaultFlexiColumnModel descriptionColumn = new DefaultFlexiColumnModel(ENEditGroupTableColumns.description);
		descriptionColumn.setDefaultVisible(true);
		descriptionColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(descriptionColumn);
		
		DefaultFlexiColumnModel coachesColumn = new DefaultFlexiColumnModel(ENEditGroupTableColumns.coaches);
		coachesColumn.setDefaultVisible(true);
		coachesColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(coachesColumn);
		
		DefaultFlexiColumnModel participantsColumn = new DefaultFlexiColumnModel(ENEditGroupTableColumns.participants);
		participantsColumn.setDefaultVisible(true);
		participantsColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(participantsColumn);
		
		DefaultFlexiColumnModel maxParticipantsColumn = new DefaultFlexiColumnModel(ENEditGroupTableColumns.maxParticipants);
		maxParticipantsColumn.setDefaultVisible(true);
		maxParticipantsColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(maxParticipantsColumn);
		
		DefaultFlexiColumnModel minParticipantsColumn = new DefaultFlexiColumnModel(ENEditGroupTableColumns.minParticipants);
		minParticipantsColumn.setDefaultVisible(true);
		minParticipantsColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(minParticipantsColumn);
		
		groupTableModel = new ENEditGroupTableModel(columnsModel, getTranslator());
		groupTableElement = uifactory.addTableElement(getWindowControl(), "entries", groupTableModel, getTranslator(), formLayout);
		groupTableElement.setEmptyTableSettings("groupselection.noentries", null, "o_icon_group");
		groupTableElement.setMultiSelect(true);		
		groupTableElement.setSelectAllEnable(true);
		groupTableElement.setCustomizeColumns(false);
		
		if (showSave) {
			uifactory.addFormSubmitButton("subm", "apply", formLayout);
			uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formResetted(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public List<String> getSelectedNames() {
		List<String> selectedEntries = new ArrayList<>();
		
		for (Integer integer : groupTableElement.getMultiSelectedIndex()) {
			selectedEntries.add(groupTableModel.getObject(integer).getGroupName());
		}
	 
		return selectedEntries;
	}
	
	public List<Long> getSelectedKeys() {
		List<Long> selectedEntries = new ArrayList<>();
		
		for (Integer integer : groupTableElement.getMultiSelectedIndex()) {
			selectedEntries.add(groupTableModel.getObject(integer).getKey());
		}
	 
		return selectedEntries;
	}

}
