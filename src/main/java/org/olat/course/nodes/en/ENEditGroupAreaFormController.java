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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
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
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.NewAreaController;
import org.olat.group.ui.NewBGController;
import org.olat.modules.ModuleConfiguration;
import org.olat.resource.OLATResource;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ENEditGroupAreaFormController
 * 
 * <P>
 * Initial Date:  12.08.2007 <br>
 * @author patrickb
 */
class ENEditGroupAreaFormController extends FormBasicController implements GenericEventListener {

	private ModuleConfiguration moduleConfig;
	private CourseEditorEnv cev;
	private MultipleSelectionElement enableCancelEnroll;
	
	private TextElement easyGroupTE;
	private FormLink chooseGroupsLink;
	private FormLink createGroupsLink;
	
	private TextElement easyAreaTE;
	private FormLink chooseAreasLink;
	private FormLink createAreasLink;
	
	private boolean hasAreas;
	private boolean hasGroups;
	
	private FormSubmit subm;
	private FormLink fixGroupError;
	private FormLink fixAreaError;
	
	private NewAreaController areaCreateCntrllr;
	private NewBGController groupCreateCntrllr;
	private AreaSelectionController areaChooseC;
	private GroupSelectionController groupChooseC;
	private FormLayoutContainer areaChooseSubContainer, groupChooseSubContainer ;
	private FormItemContainer groupsAndAreasSubContainer;
	
	private EventBus singleUserEventCenter;
	private OLATResourceable groupConfigChangeEventOres;
	
	private CloseableModalController cmc;

	public ENEditGroupAreaFormController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig, CourseEditorEnv cev) {
		super(ureq, wControl, null);
		Translator pT = Util.createPackageTranslator(Condition.class, ureq.getLocale(), getTranslator());
		this.setTranslator(pT);
		
		singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
		groupConfigChangeEventOres = OresHelper.createOLATResourceableType(MultiUserEvent.class);
		singleUserEventCenter.registerFor(this, ureq.getIdentity(), groupConfigChangeEventOres);
		
		this.moduleConfig = moduleConfig;
		this.cev = cev;
		
		hasAreas = cev.getCourseGroupManager().getAllAreasFromAllContexts().size() > 0;
		hasGroups = cev.getCourseGroupManager().getAllLearningGroupsFromAllContexts().size() > 0;
		
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		singleUserEventCenter.deregisterFor(this, groupConfigChangeEventOres);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		
		easyGroupTE.setValue(
			StringHelper.formatAsSortUniqCSVString(
				Arrays.asList(easyGroupTE.getValue().split(","))
			)
		);
		
		easyAreaTE.setValue(
			StringHelper.formatAsSortUniqCSVString(
				Arrays.asList(easyAreaTE.getValue().split(","))
			)
		);
		
		// 1. group names
		String groupName = "";
		if (StringHelper.containsNonWhitespace(easyGroupTE.getValue())){
			groupName = easyGroupTE.getValue();
		}
		moduleConfig.set(ENCourseNode.CONFIG_GROUPNAME, groupName);
		// 2. area names
		String areaName = "";
		if (StringHelper.containsNonWhitespace(easyAreaTE.getValue())){
			areaName = easyAreaTE.getValue();
		}
		moduleConfig.set(ENCourseNode.CONFIG_AREANAME, areaName);
		// 3. chnacel-enroll-enabled flag
		Boolean cancelEnrollEnabled = enableCancelEnroll.getSelectedKeys().size()==1;
		moduleConfig.set(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED, cancelEnrollEnabled);
		hasAreas = cev.getCourseGroupManager().getAllAreasFromAllContexts().size() > 0;
		hasGroups = cev.getCourseGroupManager().getAllLearningGroupsFromAllContexts().size() > 0;
		// Inform all listeners about the changed condition
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// groups
		groupChooseSubContainer = FormLayoutContainer.createHorizontalFormLayout(
				"groupChooseSubContainer", getTranslator()
		);
		groupChooseSubContainer.setLabel("form.groupnames", null);
		formLayout.add(groupChooseSubContainer);		
		
		String groupInitVal = (String) moduleConfig.get(ENCourseNode.CONFIG_GROUPNAME);
		if (groupInitVal != null) {
			groupInitVal  = StringHelper.formatAsSortUniqCSVString(
				Arrays.asList(((String) moduleConfig.get(ENCourseNode.CONFIG_GROUPNAME)).split(","))
			);
		}
		
		easyGroupTE = uifactory.addTextElement("group", null, 1024, groupInitVal, groupChooseSubContainer);
		easyGroupTE.setDisplaySize(24);
		easyGroupTE.setExampleKey("form.groupnames.example", null);	
		
		chooseGroupsLink = uifactory.addFormLink("choose", groupChooseSubContainer,"b_form_groupchooser");
		createGroupsLink = uifactory.addFormLink("create", groupChooseSubContainer,"b_form_groupchooser");	
		hasGroups = cev.getCourseGroupManager().getAllLearningGroupsFromAllContexts().size() > 0;
		
		// areas
		areaChooseSubContainer = FormLayoutContainer.createHorizontalFormLayout("areaChooseSubContainer", getTranslator());
		areaChooseSubContainer.setLabel("form.areanames", null);
		formLayout.add(areaChooseSubContainer);		
		
		groupsAndAreasSubContainer = (FormItemContainer) FormLayoutContainer.createHorizontalFormLayout("groupSubContainer", getTranslator());
		formLayout.add(groupsAndAreasSubContainer);
		
		String areaInitVal = (String) moduleConfig.get(ENCourseNode.CONFIG_AREANAME);
		if (areaInitVal != null) {
			areaInitVal = StringHelper.formatAsSortUniqCSVString(
				Arrays.asList(((String) moduleConfig.get(ENCourseNode.CONFIG_AREANAME)).split(","))
			);
		}
		
		easyAreaTE = uifactory.addTextElement("area", null, 1024, areaInitVal, areaChooseSubContainer);
		easyAreaTE.setDisplaySize(24);
		easyAreaTE.setExampleKey("form.areanames.example", null);
		
		chooseAreasLink = uifactory.addFormLink("choose", areaChooseSubContainer,"b_form_groupchooser");
		createAreasLink = uifactory.addFormLink("create", areaChooseSubContainer,"b_form_groupchooser");
		
		hasAreas = cev.getCourseGroupManager().getAllAreasFromAllContexts().size() > 0;
		
		// enrolment
		Boolean initialCancelEnrollEnabled  = (Boolean) moduleConfig.get(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED);
		enableCancelEnroll = uifactory.addCheckboxesHorizontal("enableCancelEnroll", "form.enableCancelEnroll", formLayout, new String[] { "ison" }, new String[] { "" }, null);
		enableCancelEnroll.select("ison", initialCancelEnrollEnabled);
		
		subm = uifactory.addFormSubmitButton("submit", formLayout);
		
		validateGroupFields ();
		updateGroupsAndAreasCheck();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq){
		return validateGroupFields();
	}
	
	private boolean validateGroupFields () {
		boolean retVal = true;
		String[] activeGroupSelection = new String[0];
		String[] activeAreaSelection = new String[0];

		if (!easyGroupTE.isEmpty()) {
			// check whether groups exist
			activeGroupSelection = easyGroupTE.getValue().split(",");
			boolean exists = false;
			Set<String> missingGroups = new HashSet<String>();
			
			for (int i = 0; i < activeGroupSelection.length; i++) {
				String trimmed = activeGroupSelection[i].trim();
				exists = cev.existsGroup(trimmed);
				if (!exists && trimmed.length() > 0 && !missingGroups.contains(trimmed)) {
					missingGroups.add(trimmed);
				}
			}
			
			if (missingGroups.size() > 0) {
				retVal = false;
				String labelKey = missingGroups.size() == 1 ? "error.notfound.name" : "error.notfound.names";
				String csvMissGrps = StringHelper.formatAsCSVString(missingGroups);
				String[] params = new String[] { "-", csvMissGrps };

				/*
				 * create error with link to fix it
				 */
				String vc_errorPage = velocity_root + "/erroritem.html";
				FormLayoutContainer errorGroupItemLayout = FormLayoutContainer.createCustomFormLayout(
						"errorgroupitem", getTranslator(), vc_errorPage
				);

					groupChooseSubContainer.setErrorComponent(errorGroupItemLayout, this.flc);
					// FIXING LINK ONLY IF A DEFAULTCONTEXT EXISTS
					fixGroupError = new FormLinkImpl("error.fix", "create");
					// link
					fixGroupError.setCustomEnabledLinkCSS("b_button");
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

				groupChooseSubContainer.showError(true);
			} else {
				// no more errors
				groupChooseSubContainer.clearError();
			}
		}
		
		
		if (!easyAreaTE.isEmpty()) {
			// check whether areas exist
			activeAreaSelection = easyAreaTE.getValue().split(",");

			List<String> activeAreaList = new ArrayList<String>();
			for (int i=activeAreaSelection.length; i-->0; ) {
				activeAreaList.add(activeAreaSelection[i].trim());
			}
			List<String> missingAreas = cev.validateAreas(activeAreaList);
			if (missingAreas.size() > 0) {
				retVal = false;
				String labelKey = missingAreas.size() == 1 ? "error.notfound.name" : "error.notfound.names";
				String csvMissAreas = StringHelper.formatAsCSVString(missingAreas);
				String[] params = new String[] { "-", csvMissAreas };

				/*
				 * create error with link to fix it
				 */
				String vc_errorPage = velocity_root + "/erroritem.html";
				FormLayoutContainer errorAreaItemLayout = FormLayoutContainer.createCustomFormLayout(
						"errorareaitem", getTranslator(), vc_errorPage
				);
				

					areaChooseSubContainer.setErrorComponent(errorAreaItemLayout, this.flc);
					// FXINGIN LINK ONLY IF DEFAULT CONTEXT EXISTS
					fixAreaError = new FormLinkImpl("error.fix", "create");// erstellen
					// link
					fixAreaError.setCustomEnabledLinkCSS("b_button");
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

			
				areaChooseSubContainer.showError(true);
			} else {
				areaChooseSubContainer.clearError();
			}	
		}

		boolean easyGroupOK = (!easyGroupTE.isEmpty() && activeGroupSelection.length != 0);
		boolean easyAreaOK = (!easyAreaTE.isEmpty() && activeAreaSelection.length != 0);
		if (easyGroupOK || easyAreaOK) {
			// clear general error
			this.flc.clearError();
		} else {
			// error concerns both fields -> set it as switch error
			this.flc.setErrorKey("form.noGroupsOrAreas", null);
			retVal = false;
		}
		
		//raise error if someone removed all groups and areas from form
		if (!retVal && !easyGroupOK && !easyAreaOK) {
				groupsAndAreasSubContainer.setErrorKey("form.noGroupsOrAreas", null);
		} else if (retVal) {
			areaChooseSubContainer.clearError();
			groupChooseSubContainer.clearError();
			groupsAndAreasSubContainer.clearError();
		}
		
		return retVal;
	}
	

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {

		if (source == chooseGroupsLink) {
			
			removeAsListenerAndDispose(groupChooseC);
			groupChooseC = new GroupSelectionController(ureq, getWindowControl(), "group",
					cev.getCourseGroupManager(), easyGroupTE.getValue());
			listenTo(groupChooseC);

			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), "close",
					groupChooseC.getInitialComponent()
			);
			listenTo(cmc);
			
			cmc.activate();
			subm.setEnabled(false);

		} else if (source == createGroupsLink) {
			// no groups in group management -> directly show group create dialog
			String[] csvGroupName = easyGroupTE.isEmpty() ? new String[0] : easyGroupTE.getValue().split(",");
			
			OLATResource courseResource = this.cev.getCourseGroupManager().getCourseResource();
			removeAsListenerAndDispose(groupCreateCntrllr);
			groupCreateCntrllr = BGControllerFactory.getInstance().createNewBGController(
					ureq, getWindowControl(), 
					true, courseResource,
					true, easyGroupTE.getValue()
			);
			listenTo(groupCreateCntrllr);

			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), "close",
					groupCreateCntrllr.getInitialComponent()
			);
			listenTo(cmc);
			
			cmc.activate();
			subm.setEnabled(false);
			
		} else if (source == chooseAreasLink) {

			// already areas -> choose areas
			removeAsListenerAndDispose(areaChooseC);
			areaChooseC = new AreaSelectionController (ureq, getWindowControl() , "area",
					cev.getCourseGroupManager(), easyAreaTE.getValue());
			listenTo(areaChooseC);
			
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), "close",
					areaChooseC.getInitialComponent()
			);
			listenTo(cmc);
			
			cmc.activate();
			subm.setEnabled(false);
			
		} else if (source == createAreasLink) {
			// no areas -> directly show creation dialog
			removeAsListenerAndDispose(areaCreateCntrllr);
			OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
			areaCreateCntrllr = BGControllerFactory.getInstance().createNewAreaController(
					ureq, getWindowControl(), courseResource, true, easyAreaTE.getValue()
			);
			listenTo(areaCreateCntrllr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), "close",
					areaCreateCntrllr.getInitialComponent()
			);
			listenTo(cmc);
			
			cmc.activate();
			subm.setEnabled(false);
			
		} else if (source == fixGroupError) {
			/*
			 * user wants to fix problem with fixing group error link e.g. create one
			 * or more group at once.
			 */
			String[] csvGroupName = (String[]) fixGroupError.getUserObject();
			
			easyGroupTE.setEnabled(false);
			removeAsListenerAndDispose(groupCreateCntrllr);
			OLATResource courseResource = this.cev.getCourseGroupManager().getCourseResource();
			groupCreateCntrllr = BGControllerFactory.getInstance().createNewBGController(
					ureq, getWindowControl(), true,
					courseResource, true, csvGroupName[0]
			);
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
			
			easyAreaTE.setEnabled(false);
			removeAsListenerAndDispose(areaCreateCntrllr);
			OLATResource courseResource = this.cev.getCourseGroupManager().getCourseResource();
			areaCreateCntrllr = BGControllerFactory.getInstance().createNewAreaController(
					ureq, getWindowControl(), 
					courseResource, true, csvAreaName[0]
			);
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
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Controller source, Event event) {
		
		subm.setEnabled(true);
		
		if (source == groupChooseC) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyGroupTE.setValue(StringHelper.formatAsSortUniqCSVString(groupChooseC.getSelectedEntries()));
				easyGroupTE.getRootForm().submit(ureq);
	
			} else if (Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
				return;
				
			} else if (event == Event.CHANGED_EVENT && !hasGroups){
				//singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
				//why? fireEvent(ureq, new BGContextEvent(BGContextEvent.RESOURCE_ADDED,getDefaultBGContext()));
			}
			
		} else if (source == areaChooseC) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyAreaTE.setValue(StringHelper.formatAsSortUniqCSVString(areaChooseC.getSelectedEntries()));
				easyAreaTE.getRootForm().submit(ureq);
				
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				return;
				
			} else if (event == Event.CHANGED_EVENT && !hasAreas) {
				//singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
				//why? fireEvent(ureq, new BGContextEvent(BGContextEvent.RESOURCE_ADDED,getDefaultBGContext()));
			}
			
		} else if (source == groupCreateCntrllr) {
			
			easyGroupTE.setEnabled(true);
			cmc.deactivate();

			if (event == Event.DONE_EVENT) {
				
				List <String>c = new ArrayList<String>();
				c.addAll(Arrays.asList(easyGroupTE.getValue().split(",")));
				if (fixGroupError != null && fixGroupError.getUserObject() != null) {
					c.removeAll(Arrays.asList((String[])fixGroupError.getUserObject()));
				}
				c.addAll (groupCreateCntrllr.getCreatedGroupNames());
				
				easyGroupTE.setValue(StringHelper.formatAsSortUniqCSVString(c));
				
				if (groupCreateCntrllr.getCreatedGroupNames().size() > 0 && !hasGroups) {
					chooseGroupsLink.setVisible(true);
					createGroupsLink.setVisible(false);
					singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
				}
				
				easyGroupTE.getRootForm().submit(ureq);
			}
			
		} else if (source == areaCreateCntrllr) {
			
			easyAreaTE.setEnabled(true);
			cmc.deactivate();
			
			if (event == Event.DONE_EVENT) {
				List <String>c = new ArrayList<String>();
				c.addAll(Arrays.asList(easyAreaTE.getValue().split(",")));
				if (fixAreaError != null && fixAreaError.getUserObject() != null) {
					c.removeAll(Arrays.asList((String[])fixAreaError.getUserObject()));
				}
				c.addAll (areaCreateCntrllr.getCreatedAreaNames());
				
				easyAreaTE.setValue(StringHelper.formatAsSortUniqCSVString(c));
				
				if (areaCreateCntrllr.getCreatedAreaNames().size() > 0 && !hasAreas) {
					chooseAreasLink.setVisible(true);
					createAreasLink.setVisible(false);
					singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
				}
				easyAreaTE.getRootForm().submit(ureq);
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
		
		hasGroups = cev.getCourseGroupManager().getAllLearningGroupsFromAllContexts().size() > 0;		
		chooseGroupsLink.setVisible(hasGroups);
		createGroupsLink.setVisible(!hasGroups);
		
		hasAreas = cev.getCourseGroupManager().getAllAreasFromAllContexts().size() > 0;
		chooseAreasLink.setVisible(hasAreas);
		createAreasLink.setVisible(!hasAreas);	
	}
}
