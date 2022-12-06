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
package org.olat.course.nodes.en;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.Condition;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.en.ENEditGroupTableModel.ENEditGroupTableColumns;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.ui.NewAreaController;
import org.olat.group.ui.NewBGController;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  12.08.2007 <br>
 * @author patrickb
 */
class ENEditGroupAreaFormController extends FormBasicController implements GenericEventListener {

	private ModuleConfiguration moduleConfig;
	private CourseEditorEnv cev;
	private MultipleSelectionElement enableCancelEnroll;
	private MultipleSelectionElement allowMultipleEnroll;
	private MultipleSelectionElement allowGroupSort;
	private IntegerElement multipleEnrollCount;

	private FlexiTableElement easyGroupTableElement;
	private ENEditGroupTableModel easyGroupTableModel;
	private List<ENEditGroupTableContentRow> easyGroupTableRows;
	private DefaultFlexiColumnModel moveUpColumnModel;
	private DefaultFlexiColumnModel moveDownColumnModel;

	private FormLink chooseGroupsLink;
	private FormLink createGroupLink;

	private StaticTextElement easyAreaList;
	private FormLink chooseAreasLink;

	private boolean hasAreas;
	private boolean hasGroups;

	private FormSubmit subm;

	private NewAreaController areaCreateCntrllr;
	private NewBGController groupCreateCntrllr;
	private AreaSelectionController areaChooseC;
	private GroupSelectionController groupChooseC;

	private EventBus singleUserEventCenter;
	private OLATResourceable groupConfigChangeEventOres;

	private CloseableModalController cmc;

	private final boolean managedGroups;

	private final BGAreaManager areaManager;

	private static final String CMD_UP = "up";
	private static final String CMD_DOWN = "down";
	private static final String CMD_REMOVE = "remove";
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private EnrollmentManager enrollmentManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	public ENEditGroupAreaFormController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig, CourseEditorEnv cev) {
		super(ureq, wControl);
		Translator pT = Util.createPackageTranslator(Condition.class, ureq.getLocale(), getTranslator());
		this.setTranslator(pT);

		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);

		singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
		groupConfigChangeEventOres = OresHelper.createOLATResourceableType(MultiUserEvent.class);
		singleUserEventCenter.registerFor(this, ureq.getIdentity(), groupConfigChangeEventOres);

		this.moduleConfig = moduleConfig;
		this.cev = cev;

		hasAreas = areaManager.countBGAreasInContext(cev.getCourseGroupManager().getCourseResource()) > 0;
		hasGroups = businessGroupService.countBusinessGroups(null, cev.getCourseGroupManager().getCourseEntry()) > 0;


		OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
		RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
		managedGroups = RepositoryEntryManagedFlag.isManaged(courseRe, RepositoryEntryManagedFlag.groups);

		initForm(ureq);
	}
	
	public void loadModel() {
		List<Long> groupKeys = moduleConfig.getList(ENCourseNode.CONFIG_GROUP_IDS, Long.class);
		if(groupKeys == null || groupKeys.isEmpty()) {
			String groupNames = (String) moduleConfig.get(ENCourseNode.CONFIG_GROUPNAME);
			if(StringHelper.containsNonWhitespace(groupNames)) {
				groupKeys = businessGroupService.toGroupKeys(groupNames, cev.getCourseGroupManager().getCourseEntry());
			} else {
				groupKeys = new ArrayList<>();
			}
		}	

		updateModel(groupKeys);
	}

	public void updateModel(List<Long> groupKeys) {
		List<BusinessGroup> groups = businessGroupService.loadBusinessGroups(groupKeys);
		Map<Long,BusinessGroup> groupMap = groups.stream().collect(Collectors.toMap(BusinessGroup::getKey, g -> g, (u, v) -> u));
		List<EnrollmentRow> enrollmentRows = enrollmentManager.getEnrollments(getIdentity(), groupKeys, null, 256);
		Map<Long,EnrollmentRow> enrollmentMap = enrollmentRows.stream().collect(Collectors.toMap(EnrollmentRow::getKey, g -> g, (u, v) -> u));

		easyGroupTableRows = new ArrayList<>();
		for (Long groupKey : groupKeys) {
			BusinessGroup group = groupMap.get(groupKey);
			if(group != null) {
				EnrollmentRow enrollment = enrollmentMap.get(groupKey);
				easyGroupTableRows.add(new ENEditGroupTableContentRow(group, enrollment));
			}
		}

		easyGroupTableModel.setObjects(easyGroupTableRows);
		easyGroupTableElement.reset(true,true,true);
	}

	@Override
	protected void doDispose() {
		singleUserEventCenter.deregisterFor(this, groupConfigChangeEventOres);
        super.doDispose();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// 1. group names
		String groupNames = StringHelper.formatAsSortUniqCSVString(easyGroupTableModel.getNames());
		moduleConfig.set(ENCourseNode.CONFIG_GROUPNAME, groupNames);
		moduleConfig.set(ENCourseNode.CONFIG_GROUP_IDS, easyGroupTableModel.getKeys());
		// 2. area names
		KeysAndNames areaKeysAndNames = (KeysAndNames)easyAreaList.getUserObject();
		String areaNames = StringHelper.formatAsSortUniqCSVString(areaKeysAndNames.getNames());
		moduleConfig.set(ENCourseNode.CONFIG_AREANAME, areaNames);
		moduleConfig.set(ENCourseNode.CONFIG_AREA_IDS, areaKeysAndNames.getKeys());
		// 3. cancel-enroll-enabled flag
		Boolean cancelEnrollEnabled = enableCancelEnroll.getSelectedKeys().size()==1;
		moduleConfig.set(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED, cancelEnrollEnabled);
		hasAreas = areaManager.countBGAreasInContext(cev.getCourseGroupManager().getCourseResource()) > 0;
		hasGroups = businessGroupService.countBusinessGroups(null, cev.getCourseGroupManager().getCourseEntry()) > 0;
		//4. multiple groups flag
		int enrollCount;
		if(allowMultipleEnroll.isSelected(0)) {
			enrollCount = multipleEnrollCount.getIntValue();
		} else {
			enrollCount = 1; 
		}
		moduleConfig.set(ENCourseNode.CONFIG_ALLOW_MULTIPLE_ENROLL_COUNT, enrollCount);
		// 5. group sorting
		moduleConfig.set(ENCourseNode.CONFIG_GROUP_SORTED, allowGroupSort.isSelected(0));
		// Inform all listeners about the changed condition
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// groups
		String page = this.velocity_root + "/chooseGroups.html";
		FormLayoutContainer buttonLayout = FormLayoutContainer.createCustomFormLayout("chooseGroups", getTranslator(), page);
		formLayout.add("buttonLayout",buttonLayout);
		
		chooseGroupsLink = uifactory.addFormLink("chooseGroup", buttonLayout, "btn btn-default o_xsmall o_form_groupchooser");
		chooseGroupsLink.setI18nKey("choose");
		chooseGroupsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");

		createGroupLink = uifactory.addFormLink("createGroup", buttonLayout, "btn btn-default o_xsmall o_form_groupcreate");
		createGroupLink.setI18nKey("create");
		createGroupLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		
		buttonLayout.add("chooseGroupBtn", chooseGroupsLink);
		buttonLayout.add("createGroupBtn", createGroupLink);
		buttonLayout.setLabel("form.groupnames", null);
		

		// Group sort
		allowGroupSort = uifactory.addCheckboxesHorizontal("allowGroupSort", "form.allowGroupSort", formLayout, new String[] { "allowGroupSort" }, new String[] { "" });
		allowGroupSort.select("allowGroupSort", moduleConfig.getBooleanSafe(ENCourseNode.CONFIG_GROUP_SORTED, false));
		allowGroupSort.addActionListener(FormEvent.ONCLICK);

		// Group table		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		moveUpColumnModel = new DefaultFlexiColumnModel(ENEditGroupTableColumns.up, CMD_UP,
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", CMD_UP, "o_icon o_icon-lg o_icon_move_up"),
						null));
		moveDownColumnModel = new DefaultFlexiColumnModel(ENEditGroupTableColumns.down, CMD_DOWN,
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", CMD_DOWN, "o_icon o_icon-lg o_icon_move_down"),
						null));

		columnsModel.addFlexiColumnModel(moveUpColumnModel);
		columnsModel.addFlexiColumnModel(moveDownColumnModel);
		DefaultFlexiColumnModel keyColumn = new DefaultFlexiColumnModel(ENEditGroupTableColumns.key);
		keyColumn.setDefaultVisible(false);
		keyColumn.setAlwaysVisible(false);
		columnsModel.addFlexiColumnModel(keyColumn);

		DefaultFlexiColumnModel groupColumn = new DefaultFlexiColumnModel(ENEditGroupTableColumns.groupName);
		groupColumn.setDefaultVisible(true);
		groupColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(groupColumn);
		
		DefaultFlexiColumnModel descriptionColumn = new DefaultFlexiColumnModel(ENEditGroupTableColumns.description);
		descriptionColumn.setDefaultVisible(true);
		descriptionColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(descriptionColumn);
		
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
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ENEditGroupTableColumns.remove, CMD_REMOVE));

		easyGroupTableModel = new ENEditGroupTableModel(columnsModel, getTranslator());
		easyGroupTableElement = uifactory.addTableElement(getWindowControl(), "en_edit_group_table", easyGroupTableModel, getTranslator(), formLayout);
		easyGroupTableElement.setCustomizeColumns(false);
		easyGroupTableElement.setNumOfRowsEnabled(false);

		// Update model
		if (!moduleConfig.getBooleanSafe(ENCourseNode.CONFIG_GROUP_SORTED, false)) {
			easyGroupTableElement.setColumnModelVisible(moveDownColumnModel, false);
			easyGroupTableElement.setColumnModelVisible(moveUpColumnModel, false);
		}	
		this.loadModel();


		hasGroups = businessGroupService.countBusinessGroups(null, cev.getCourseGroupManager().getCourseEntry()) > 0;

		// areas

		@SuppressWarnings("unchecked")
		List<Long> areaKeys = (List<Long>)moduleConfig.get(ENCourseNode.CONFIG_AREA_IDS);
		if(areaKeys == null || areaKeys.isEmpty()) {
			String areaNames = (String) moduleConfig.get(ENCourseNode.CONFIG_AREANAME);
			areaKeys = areaManager.toAreaKeys(areaNames, cev.getCourseGroupManager().getCourseResource());
		}

		KeysAndNames areaInitVal = getAreaKeysAndNames(areaKeys);
		chooseAreasLink = uifactory.addFormLink("chooseArea", formLayout, "btn btn-default o_xsmall o_form_areachooser");
		chooseAreasLink.setLabel("form.areanames", null);
		chooseAreasLink.setIconLeftCSS("o_icon o_icon-fw o_icon_courseareas");

		easyAreaList = uifactory.addStaticTextElement("area", null, areaInitVal.getDecoratedNames(), formLayout);
		easyAreaList.setUserObject(areaInitVal);
		easyAreaList.setElementCssClass("text-muted");

		hasAreas = areaManager.countBGAreasInContext(cev.getCourseGroupManager().getCourseResource()) > 0;
		if(hasAreas){
			chooseAreasLink.setI18nKey("choose");
		} else {
			chooseAreasLink.setVisible(false);
			easyAreaList.setVisible(false);
		}

		//multiple group selection
		int enrollCountConfig = moduleConfig.getIntegerSafe(ENCourseNode.CONFIG_ALLOW_MULTIPLE_ENROLL_COUNT,1);
		Boolean multipleEnroll = (enrollCountConfig > 1);
		allowMultipleEnroll = uifactory.addCheckboxesHorizontal("allowMultipleEnroll", "form.allowMultiEnroll", formLayout, new String[] { "multiEnroll" }, new String[] { "" });
		allowMultipleEnroll.select("multiEnroll", multipleEnroll);
		allowMultipleEnroll.addActionListener(FormEvent.ONCLICK);

		multipleEnrollCount = uifactory.addIntegerElement("form.multipleEnrollCount", enrollCountConfig, formLayout);
		multipleEnrollCount.setElementCssClass("o_sel_enroll_max");
		multipleEnrollCount.setMinValueCheck(1, "error.multipleEnroll");
		multipleEnrollCount.setVisible(allowMultipleEnroll.isSelected(0));

		// enrolment
		Boolean initialCancelEnrollEnabled  = (Boolean) moduleConfig.get(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED);
		enableCancelEnroll = uifactory.addCheckboxesHorizontal("enableCancelEnroll", "form.enableCancelEnroll", formLayout, new String[] { "ison" }, new String[] { "" });
		enableCancelEnroll.select("ison", initialCancelEnrollEnabled);

		subm = uifactory.addFormSubmitButton("submit", formLayout);

		validateGroupFields();
		updateGroupsAndAreasCheck();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq){
		return validateGroupFields();
	}

	private boolean validateGroupFields() {
		boolean retVal = true;
		List<Long> activeGroupSelection = null;
		List<Long> activeAreaSelection = null;

		easyAreaList.clearError();
		easyGroupTableElement.clearError();

		if (!isEmpty(easyGroupTableModel)) {
			// check whether groups exist
			activeGroupSelection = easyGroupTableModel.getKeys();

			Set<Long> missingGroups = new HashSet<>();
			List<BusinessGroupShort> existingGroups =  businessGroupService.loadShortBusinessGroups(activeGroupSelection);
			a_a:
			for(Long activeGroupKey:activeGroupSelection) {
				for(BusinessGroupShort group:existingGroups) {
					if(group.getKey().equals(activeGroupKey)) {
						continue a_a;
					}
				}
				missingGroups.add(activeGroupKey);
			}

			if (missingGroups.isEmpty()) {
				easyGroupTableElement.clearError();
			} else {
				String labelKey = missingGroups.size() == 1 ? "error.notfound.name" : "error.notfound.names";
				String csvMissGrps = toString(missingGroups);
				String[] params = new String[] { "-", csvMissGrps };
				easyGroupTableElement.setWarningKey(labelKey, params);
			}
		}

		if (!isEmpty(easyAreaList)) {
			// check whether areas exist
			activeAreaSelection = getKeys(easyAreaList);

			List<Long> missingAreas = new ArrayList<>();
			List<BGArea> cnt = areaManager.loadAreas(activeAreaSelection);
			a_a:
			for(Long activeAreaKey:activeAreaSelection) {
				for (BGArea element : cnt) {
					if (element.getKey().equals(activeAreaKey)) { 
						continue a_a;
					}
				}
				missingAreas.add(activeAreaKey);
			}

			if (missingAreas.isEmpty()) {
				easyAreaList.clearError();
			} else {
				retVal = false;
				String labelKey = missingAreas.size() == 1 ? "error.notfound.name" : "error.notfound.names";
				String csvMissAreas = toString(missingAreas);
				String[] params = new String[] { "-", csvMissAreas };
				easyAreaList.setWarningKey(labelKey, params);
			}
		}

		boolean easyGroupOK = activeGroupSelection != null && !activeGroupSelection.isEmpty();
		boolean easyAreaOK = activeAreaSelection != null && !activeAreaSelection.isEmpty();
		if (!easyGroupOK && !easyAreaOK) {
			// error concerns both fields -> set it as switch error
			easyGroupTableElement.setErrorKey("form.noGroupsOrAreas");
			retVal = false;
		}

		//raise error if someone removed all groups and areas from form
		if (!retVal && !easyGroupOK && !easyAreaOK) {
			easyGroupTableElement.setErrorKey("form.noGroupsOrAreas");
		}
		return retVal;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == allowMultipleEnroll){
			if(allowMultipleEnroll.isSelected(0)){
				multipleEnrollCount.setVisible(true);
			}else{
				multipleEnrollCount.setVisible(false);
			}
		} else if(source == allowGroupSort) {
			if (allowGroupSort.isSelected(0)) {				
				easyGroupTableElement.setColumnModelVisible(moveDownColumnModel, true);
				easyGroupTableElement.setColumnModelVisible(moveUpColumnModel, true);
			} else {
				easyGroupTableElement.setColumnModelVisible(moveDownColumnModel, false);
				easyGroupTableElement.setColumnModelVisible(moveUpColumnModel, false);
			}
		} else if (source == chooseGroupsLink) {
			removeAsListenerAndDispose(groupChooseC);
			groupChooseC = new GroupSelectionController(ureq, getWindowControl(), false,
					cev.getCourseGroupManager(), easyGroupTableModel.getKeys());
			listenTo(groupChooseC);

			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), "close", groupChooseC.getInitialComponent());
			listenTo(cmc);

			cmc.activate();
			subm.setEnabled(false);

		} else if (source == createGroupLink) {
			// user wants to create a new group -> show group create form
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(groupCreateCntrllr);
			
			RepositoryEntry re = repositoryManager.lookupRepositoryEntry(cev.getCourseGroupManager().getCourseResource(), false);
			groupCreateCntrllr = new NewBGController(ureq, getWindowControl(), re, true, null);
			listenTo(groupCreateCntrllr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(),"close",groupCreateCntrllr.getInitialComponent()
			);
			listenTo(cmc);
			cmc.activate();
		
		} else if (source == chooseAreasLink) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(areaChooseC);

			// already areas -> choose areas
			areaChooseC = new AreaSelectionController (ureq, getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyAreaList));
			listenTo(areaChooseC);

			cmc = new CloseableModalController(getWindowControl(), "close", areaChooseC.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			subm.setEnabled(false);
		} else if(source == easyGroupTableElement) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if (CMD_UP.equals(cmd)) {
					doUp(se.getIndex());	
				} else if (CMD_DOWN.equals(cmd)) {
					doDown(se.getIndex());
				} else if(CMD_REMOVE.equals(cmd)) {
					doDelete(se.getIndex());
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		subm.setEnabled(true);
		if (source == groupChooseC) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();

				updateModel(groupChooseC.getSelectedKeys());
				easyGroupTableElement.getRootForm().submit(ureq);
			} else if (Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
			}
		} else if (source == areaChooseC) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				KeysAndNames c = getAreaKeysAndNames(areaChooseC.getSelectedKeys());
				easyAreaList.setValue(c.getDecoratedNames());
				easyAreaList.setUserObject(c);
				easyAreaList.getRootForm().submit(ureq);
				chooseAreasLink.setI18nKey("choose");
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		} else if (source == groupCreateCntrllr) {
			easyGroupTableElement.setEnabled(true);
			cmc.deactivate();

			if (event == Event.DONE_EVENT) {
				List<Long> c = new ArrayList<>();
				c.addAll(easyGroupTableModel.getKeys());
				c.addAll(groupCreateCntrllr.getCreatedGroupKeys());

				updateModel(c);

				if (!groupCreateCntrllr.getCreatedGroupNames().isEmpty() && !hasGroups) {
					chooseGroupsLink.setLinkTitle("select");
					singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
				}

				easyGroupTableElement.getRootForm().submit(ureq);
			}
		} else if (source == areaCreateCntrllr) {
			easyAreaList.setEnabled(true);
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				List<Long> c = new ArrayList<>();
				c.addAll(getKeys(easyAreaList));
				c.addAll(areaCreateCntrllr.getCreatedAreaKeys());

				KeysAndNames keysAndNames = getAreaKeysAndNames(c);
				easyAreaList.setValue(keysAndNames.getDecoratedNames());
				easyAreaList.setUserObject(keysAndNames);

				if (!areaCreateCntrllr.getCreatedAreaNames().isEmpty() && !hasAreas) {
					chooseAreasLink.setLinkTitle("select");
					singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
				}
				easyAreaList.getRootForm().submit(ureq);
			} 
		}
	}

	public ModuleConfiguration getModuleConfiguration() {
		return moduleConfig;
	}

	@Override
	public void event(Event event) {
		if (event.getCommand().equals("changed")) {
			validateGroupFields();
			updateGroupsAndAreasCheck();
		}
	}

	private void doDown(int index) {
		switchGroups(index, index + 1);
	}

	private void doUp(int index) {
		switchGroups(index - 1, index);
	}

	private void doDelete(int index) {
		easyGroupTableRows.remove(index);

		easyGroupTableModel.setObjects(easyGroupTableRows);
		easyGroupTableElement.reset(true,true,true);
	}

	private void switchGroups(int downGroup, int upGroup) {
		ENEditGroupTableContentRow temp = easyGroupTableRows.get(downGroup);
		easyGroupTableRows.set(downGroup, easyGroupTableRows.get(upGroup));
		easyGroupTableRows.set(upGroup, temp);

		easyGroupTableModel.setObjects(easyGroupTableRows);
		easyGroupTableElement.reset(true,true,true);
	}

	private void updateGroupsAndAreasCheck() {
		hasGroups = businessGroupService.countBusinessGroups(null, cev.getCourseGroupManager().getCourseEntry()) > 0;
		if(!hasGroups && !managedGroups){
			chooseGroupsLink.setLinkTitle("create");
		}else{
			chooseGroupsLink.setLinkTitle("choose");
		}

		hasAreas = areaManager.countBGAreasInContext(cev.getCourseGroupManager().getCourseResource()) > 0;
		if(hasAreas){
			chooseAreasLink.setLinkTitle("choose");
		}else{
			chooseAreasLink.setLinkTitle("create");
		}
	}

	private boolean isEmpty(StaticTextElement element) {
		List<Long> keys = getKeys(element);
		return keys == null || keys.isEmpty();
	}

	private boolean isEmpty(ENEditGroupTableModel element) {
		List<Long> keys = element.getKeys();
		return keys == null || keys.isEmpty();
	}

	private List<Long> getKeys(StaticTextElement element) {
		KeysAndNames keys = (KeysAndNames)element.getUserObject();
		if(keys == null) {
			keys = new KeysAndNames();
			element.setUserObject(keys);
		}
		return keys.getKeys();
	}

	private String toString(Collection<Long> keys) {
		StringBuilder sb = new StringBuilder();
		for(Long key:keys) {
			if(sb.length() > 0) sb.append(',');
			sb.append(key);
		}
		return sb.toString();
	}

	private KeysAndNames getAreaKeysAndNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder();
		List<BGArea> areas = areaManager.loadAreas(keys);
		KeysAndNames keysAndNames = new KeysAndNames();
		keysAndNames.getKeys().addAll(keys);
		for(BGArea area:areas) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_courseareas'>&nbsp;</i> ");
			sb.append(StringHelper.escapeHtml(area.getName()));
			keysAndNames.getNames().add(area.getName());
		}
		keysAndNames.setDecoratedNames(sb.toString());
		return keysAndNames;
	}

	private static class KeysAndNames {

		private final List<Long> keys = new ArrayList<>();
		private final List<String> names = new ArrayList<>();
		private String decoratedNames;

		public List<Long> getKeys() {
			return keys;
		}

		public List<String> getNames() {
			return names;
		}

		public String getDecoratedNames() {
			return decoratedNames;
		}

		public void setDecoratedNames(String decoratedNames) {
			this.decoratedNames = decoratedNames;
		}
	}
}