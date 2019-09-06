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
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
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

/**
 * Initial Date:  12.08.2007 <br>
 * @author patrickb
 */
class ENEditGroupAreaFormController extends FormBasicController implements GenericEventListener {

	private ModuleConfiguration moduleConfig;
	private CourseEditorEnv cev;
	private MultipleSelectionElement enableCancelEnroll;
	private MultipleSelectionElement allowMultipleEnroll;
	private IntegerElement multipleEnrollCount;
	
	private StaticTextElement easyGroupList;
	private FormLink chooseGroupsLink;
	
	private StaticTextElement easyAreaList;
	private FormLink chooseAreasLink;
	
	private boolean hasAreas;
	private boolean hasGroups;
	
	private FormSubmit subm;
	private FormLink fixGroupError;
	private FormLink fixAreaError;
	
	private NewAreaController areaCreateCntrllr;
	private NewBGController groupCreateCntrllr;
	private AreaSelectionController areaChooseC;
	private GroupSelectionController groupChooseC;
	
	private EventBus singleUserEventCenter;
	private OLATResourceable groupConfigChangeEventOres;
	
	private CloseableModalController cmc;
	
	private final boolean managedGroups;
	
	private final BGAreaManager areaManager;
	private final BusinessGroupService businessGroupService;

	public ENEditGroupAreaFormController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig, CourseEditorEnv cev) {
		super(ureq, wControl);
		Translator pT = Util.createPackageTranslator(Condition.class, ureq.getLocale(), getTranslator());
		this.setTranslator(pT);
		
		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
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

	@Override
	protected void doDispose() {
		singleUserEventCenter.deregisterFor(this, groupConfigChangeEventOres);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// 1. group names
		KeysAndNames groupKeysAndNames = (KeysAndNames)easyGroupList.getUserObject();
		String groupNames = StringHelper.formatAsSortUniqCSVString(groupKeysAndNames.getNames());
		moduleConfig.set(ENCourseNode.CONFIG_GROUPNAME, groupNames);
		moduleConfig.set(ENCourseNode.CONFIG_GROUP_IDS, groupKeysAndNames.getKeys());
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
		// Inform all listeners about the changed condition
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// groups
		List<Long> groupKeys = moduleConfig.getList(ENCourseNode.CONFIG_GROUP_IDS, Long.class);
		if(groupKeys == null || groupKeys.isEmpty()) {
			String groupNames = (String) moduleConfig.get(ENCourseNode.CONFIG_GROUPNAME);
			if(StringHelper.containsNonWhitespace(groupNames)) {
				groupKeys = businessGroupService.toGroupKeys(groupNames, cev.getCourseGroupManager().getCourseEntry());
			} else {
				groupKeys = new ArrayList<>();
			}
		}
		KeysAndNames groupInitVal = getGroupKeysAndNames(groupKeys);
		chooseGroupsLink = uifactory.addFormLink("chooseGroup", formLayout, "btn btn-default o_xsmall o_form_groupchooser");
		chooseGroupsLink.setLabel("form.groupnames", null);
		chooseGroupsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		
		easyGroupList = uifactory.addStaticTextElement("group", null, groupInitVal.getDecoratedNames(), formLayout);
		easyGroupList.setUserObject(groupInitVal);
		easyGroupList.setElementCssClass("text-muted");
		
		hasGroups = businessGroupService.countBusinessGroups(null, cev.getCourseGroupManager().getCourseEntry()) > 0;
		if(hasGroups){
			chooseGroupsLink.setI18nKey("choose");
		}else{
			chooseGroupsLink.setI18nKey("create");
		}
		
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
			chooseAreasLink.setI18nKey("create");
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
		easyGroupList.clearError();

		if (!isEmpty(easyGroupList)) {
			// check whether groups exist
			activeGroupSelection = getKeys(easyGroupList);

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
			
			if (missingGroups.size() > 0) {
				retVal = false;
				String labelKey = missingGroups.size() == 1 ? "error.notfound.name" : "error.notfound.names";
				String csvMissGrps = toString(missingGroups);
				String[] params = new String[] { "-", csvMissGrps };

				// create error with link to fix it
				String vc_errorPage = velocity_root + "/erroritem.html";
				FormLayoutContainer errorGroupItemLayout = FormLayoutContainer.createCustomFormLayout(
						"errorgroupitem", getTranslator(), vc_errorPage);

				easyGroupList.setErrorComponent(errorGroupItemLayout, this.flc);
				// FIXING LINK ONLY IF A DEFAULTCONTEXT EXISTS
				fixGroupError = new FormLinkImpl("error.fix", "create");
				// link
				fixGroupError.setCustomEnabledLinkCSS("btn btn-default");
				errorGroupItemLayout.add(fixGroupError);

				fixGroupError.setErrorKey(labelKey, params);
				fixGroupError.showError(true);
				fixGroupError.showLabel(false);
				// hinty to pass the information if one group is
				// missing or if 2 or more groups are missing
				// (see fixGroupErrer.getUserObject to understand)
				// e.g. if userobject String[].lenght == 1 -> one group only
				// String[].lenght > 1 -> show bulkmode creation group
				if (missingGroups.size() > 1) {
					fixGroupError.setUserObject(new String[] { csvMissGrps, "dummy" });
				} else {
					fixGroupError.setUserObject(new String[] { csvMissGrps });
				}

				easyGroupList.showError(true);
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
			
			if (missingAreas.size() > 0) {
				retVal = false;
				String labelKey = missingAreas.size() == 1 ? "error.notfound.name" : "error.notfound.names";
				String csvMissAreas = toString(missingAreas);
				String[] params = new String[] { "-", csvMissAreas };

				// create error with link to fix it
				String vc_errorPage = velocity_root + "/erroritem.html";
				FormLayoutContainer errorAreaItemLayout = FormLayoutContainer.createCustomFormLayout(
						"errorareaitem", getTranslator(), vc_errorPage);

				easyAreaList.setErrorComponent(errorAreaItemLayout, this.flc);
				// FXINGIN LINK ONLY IF DEFAULT CONTEXT EXISTS
				fixAreaError = new FormLinkImpl("error.fix", "create");// erstellen
				// link
				fixAreaError.setCustomEnabledLinkCSS("btn btn-default");
				errorAreaItemLayout.add(fixAreaError);

				fixAreaError.setErrorKey(labelKey, params);
				fixAreaError.showError(true);
				fixAreaError.showLabel(false);
				
				// hint to pass the information if one area is
				// missing or if 2 or more areas are missing
				// (see fixGroupErrer.getUserObject to understand)
				// e.g. if userobject String[].lenght == 1 -> one group only
				// String[].lenght > 1 -> show bulkmode creation group
				if (missingAreas.size() > 1) {
					fixAreaError.setUserObject(new String[] { csvMissAreas, "dummy" });
				} else {
					fixAreaError.setUserObject(new String[] { csvMissAreas });
				}

				easyAreaList.showError(true);
			}
		}
		
		boolean easyGroupOK = activeGroupSelection != null && activeGroupSelection.size() > 0;
		boolean easyAreaOK = activeAreaSelection != null && activeAreaSelection.size() > 0;
		if (!easyGroupOK && !easyAreaOK) {
			// error concerns both fields -> set it as switch error
			easyGroupList.setErrorKey("form.noGroupsOrAreas", null);
			retVal = false;
		}
		
		//raise error if someone removed all groups and areas from form
		if (!retVal && !easyGroupOK && !easyAreaOK) {
			easyGroupList.setErrorKey("form.noGroupsOrAreas", null);
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
		}else if (source == chooseGroupsLink) {
			removeAsListenerAndDispose(groupChooseC);
			groupChooseC = new GroupSelectionController(ureq, getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyGroupList));
			listenTo(groupChooseC);

			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), "close", groupChooseC.getInitialComponent());
			listenTo(cmc);
			
			cmc.activate();
			subm.setEnabled(false);

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
		} else if (source == fixGroupError) {
			/*
			 * user wants to fix problem with fixing group error link e.g. create one
			 * or more group at once.
			 */
			String[] csvGroupName = (String[]) fixGroupError.getUserObject();
			
			easyGroupList.setEnabled(false);
			removeAsListenerAndDispose(groupCreateCntrllr);
			OLATResource courseResource = this.cev.getCourseGroupManager().getCourseResource();
			RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
			groupCreateCntrllr = new NewBGController(ureq, getWindowControl(), courseRe, true, csvGroupName[0]);
			listenTo(groupCreateCntrllr);

			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), "close",
					groupCreateCntrllr.getInitialComponent()
			);
			listenTo(cmc);
			
			cmc.activate();
			subm.setEnabled(false);
			
		} else if (source == fixAreaError) {
			/*
			 * user wants to fix problem with fixing area error link e.g. create one
			 * or more areas at once.
			 */
			String[] csvAreaName = (String[]) fixAreaError.getUserObject();
			
			easyAreaList.setEnabled(false);
			removeAsListenerAndDispose(areaCreateCntrllr);
			OLATResource courseResource = this.cev.getCourseGroupManager().getCourseResource();
			areaCreateCntrllr = new NewAreaController(ureq, getWindowControl(), courseResource, true, csvAreaName[0]);
			listenTo(areaCreateCntrllr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), "close",
					areaCreateCntrllr.getInitialComponent()
			);
			listenTo(cmc);
			
			cmc.activate();
			subm.setEnabled(false);
			
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		subm.setEnabled(true);
		if (source == groupChooseC) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				KeysAndNames c =  getGroupKeysAndNames(groupChooseC.getSelectedKeys());
				easyGroupList.setValue(c.getDecoratedNames());
				easyGroupList.setUserObject(c);
				easyGroupList.getRootForm().submit(ureq);
				chooseGroupsLink.setI18nKey("choose");
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
			easyGroupList.setEnabled(true);
			cmc.deactivate();

			if (event == Event.DONE_EVENT) {
				List<Long> c = new ArrayList<>();
				c.addAll(getKeys(easyGroupList));
				if (fixGroupError != null && fixGroupError.getUserObject() != null) {
					String[] keyArr = (String[])fixGroupError.getUserObject();
					if(keyArr != null && keyArr.length > 0) {
						List<Long> fixedKeys = toKeys(keyArr[0]);
						c.removeAll(fixedKeys);
					}
				}
				c.addAll(groupCreateCntrllr.getCreatedGroupKeys());
				
				KeysAndNames keysAndNames = getGroupKeysAndNames(c);
				easyGroupList.setValue(keysAndNames.getDecoratedNames());
				easyGroupList.setUserObject(keysAndNames);
				
				if (groupCreateCntrllr.getCreatedGroupNames().size() > 0 && !hasGroups) {
					chooseGroupsLink.setLinkTitle("select");
					singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
				}
				
				easyGroupList.getRootForm().submit(ureq);
			}
		} else if (source == areaCreateCntrllr) {
			easyAreaList.setEnabled(true);
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				List<Long> c = new ArrayList<>();
				c.addAll(getKeys(easyAreaList));
				if (fixAreaError != null && fixAreaError.getUserObject() != null) {
					String[] keyArr = (String[])fixAreaError.getUserObject();
					if(keyArr != null && keyArr.length > 0) {
						List<Long> fixedKeys = toKeys(keyArr[0]);
						c.removeAll(fixedKeys);
					}
				}
				c.addAll(areaCreateCntrllr.getCreatedAreaKeys());
				
				KeysAndNames keysAndNames = getAreaKeysAndNames(c);
				easyAreaList.setValue(keysAndNames.getDecoratedNames());
				easyAreaList.setUserObject(keysAndNames);
				
				if (areaCreateCntrllr.getCreatedAreaNames().size() > 0 && !hasAreas) {
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
		if(keys == null || keys.isEmpty()) {
			return true;
		}
		return false;
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
	
	private List<Long> toKeys(String keysString) {
		List<Long> keyList = new ArrayList<>();
		String[] keys = keysString.split(",");
		for(String key:keys) {
			try {
				keyList.add(Long.parseLong(key));
			} catch (NumberFormatException e) {
				logWarn("Cannot parse this key: " + key, e);
			}
		}
		return keyList;
	}
	
	private KeysAndNames getGroupKeysAndNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder();
		KeysAndNames keysAndNames = new KeysAndNames();
		keysAndNames.getKeys().addAll(keys);
		
		List<BusinessGroupShort> groups = businessGroupService.loadShortBusinessGroups(keys);
		for(BusinessGroupShort group:groups) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_group'>&nbsp;</i> ");
			sb.append(StringHelper.escapeHtml(group.getName()));
			keysAndNames.getNames().add(group.getName());
			
		}
		keysAndNames.setDecoratedNames(sb.toString());
		return keysAndNames;
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