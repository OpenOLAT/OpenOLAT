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

package org.olat.course.nodes.co;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.condition.GroupOrAreaSelectionController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.NewAreaController;
import org.olat.group.ui.NewBGController;
import org.olat.modules.ModuleConfiguration;
import org.olat.resource.OLATResource;

/**
 * Description:<BR/> Configuration form for the contact form building block
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 */
public class COConfigForm extends FormBasicController {
	
	private SelectionElement wantEmail;
	private TextElement teArElEmailToAdresses;
	
	private TextElement teElSubject;
	private TextElement teArElBody;
	
	private SelectionElement wantGroup;
	private SelectionElement coaches;
	private SelectionElement partips;
	private FormLayoutContainer coachesAndPartips;
	
	private SpacerElement s1, s2, s3, s4;
	
	
	private TextElement easyGroupTE;
	private FormLink chooseGroupsLink;
	private FormLink createGroupsLink;
	
	private TextElement easyAreaTE;
	private FormLink chooseAreasLink;
	private FormLink createAreasLink;
	
	private NewAreaController areaCreateCntrllr;
	private NewBGController groupCreateCntrllr;
	private GroupOrAreaSelectionController areaChooseC;
	private GroupOrAreaSelectionController groupChooseC;
	
	
	private FormLayoutContainer areaChooseSubContainer, groupChooseSubContainer ;
	private FormItemContainer groupsAndAreasSubContainer;
	private FormItemContainer recipentsContainer;
	
	private FormLink fixGroupError;
	private FormLink fixAreaError;
	
	private FormSubmit subm;
	
	private boolean hasAreas;
	private boolean hasGroups;
	
	private CloseableModalController cmc;
	
	private List eList;
	private ModuleConfiguration config;
	private CourseEditorEnv cev;
	
	
	/**
	 * Form constructor
	 * 
	 * @param name The form name
	 * @param config The module configuration
	 * @param withCancel true: cancel button is rendered, false: no cancel button
	 */
	protected COConfigForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, UserCourseEnvironment uce) {
		super(ureq, wControl);
		this.config = config;
		this.cev = uce.getCourseEditorEnv();
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.Form#validate(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if (!wantGroup.isSelected(0) && !wantEmail.isSelected(0)) {
			s3.setVisible(true);
			recipentsContainer.setErrorKey("no.recipents.specified", null);
			return false;
		}
		s3.setVisible(false);
		recipentsContainer.clearError();
		
		
		coachesAndPartips.clearError();
		if (wantGroup.isSelected(0)) {
			if (!coaches.isSelected(0) && !partips.isSelected(0)) {
				coachesAndPartips.setErrorKey("form.choose.coachesandpartips", null);
			}
			if (!validateGroupFields()) return false;
		}
		
		/*
		 * somehow e-mail recipients must be specified, checking each of the
		 * possibility, at least one must be configured resulting in some e-mails.
		 * The case that the specified groups can contain zero members must be
		 * handled by the e-mail controller!
		 */
		
		
		
		String emailToAdresses = teArElEmailToAdresses.getValue();
		String[] emailAdress = emailToAdresses.split("\\s*\\r?\\n\\s*");
		
		teArElEmailToAdresses.clearError();
		if (wantEmail.isSelected(0) &&
				(emailAdress == null || emailAdress.length == 0|| "".equals(emailAdress[0]))) {
			// otherwise the entry field shows that no e-mails are specified
			teArElEmailToAdresses.setErrorKey("email.not.specified", null);
			return false;
		}
		
		/*
		 * check validity of manually provided e-mails
		 */
		
		if ((emailAdress != null) && (emailAdress.length > 0) && (!"".equals(emailAdress[0]))) {
			this.eList = new ArrayList();
			for (int i = 0; i < emailAdress.length; i++) {
				String eAd = emailAdress[i].trim();
				boolean emailok = MailHelper.isValidEmailAddress(eAd);
				if (emailok == false) {
					teArElEmailToAdresses.setErrorKey("email.not.valid", null);
					return false;
				}
				eList.add(eAd);
			}
		}
		return true;
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
			boolean exists = false;
			Set<String> missingAreas = new HashSet<String>();
			for (int i = 0; i < activeAreaSelection.length; i++) {
				String trimmed = activeAreaSelection[i].trim();
				exists = cev.existsArea(trimmed);
				if (!exists && trimmed.length() > 0 && !missingAreas.contains(trimmed) ) {
					missingAreas.add(trimmed);
				}
			}
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
			this.groupsAndAreasSubContainer.setErrorKey("form.noGroupsOrAreas", null);
			retVal = false;
		}
		
		//
		boolean needsGroupsOrAreas = coaches.isSelected(0) || partips.isSelected(0);
		if (needsGroupsOrAreas && !easyGroupOK && !easyAreaOK) {
			groupsAndAreasSubContainer.setErrorKey("form.noGroupsOrAreas", null);
		} else {
			groupsAndAreasSubContainer.clearError();
		}
			
		if (retVal) {
			areaChooseSubContainer.clearError();
			groupChooseSubContainer.clearError();
			groupsAndAreasSubContainer.clearError();
		}
		
		return retVal;
	}
	
	
	/**
	 * @return the message subject
	 */
	protected String getMSubject() {
		return teElSubject.getValue();
	}

	/**
	 * @return the meesage body
	 */
	protected String getMBody() {
		return teArElBody.getValue();
	}

	/**
	 * @return the email list
	 */
	protected List getEmailList() {
		return eList;
	}

	/**
	 * returns the choosen groups, or null if no groups were choosen.
	 * 
	 * @return
	 */
	protected String getEmailGroups() {
		if (StringHelper.containsNonWhitespace(easyGroupTE.getValue())) return easyGroupTE.getValue();
		return null;
	}

	/**
	 * returns the choosen learning areas, or null if no ares were choosen.
	 */
	protected String getEmailAreas() {
		if (StringHelper.containsNonWhitespace(easyAreaTE.getValue())) return easyAreaTE.getValue();
		return null;
	}

	protected boolean sendToCoaches() {
		return coaches.isSelected(0);
	}

	protected boolean sendToPartips() {
		return partips.isSelected(0);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("header", null);
		setFormContextHelp("org.olat.course.nodes.co","ced-co.html","help.hover.co");
		
		wantGroup = uifactory.addCheckboxesVertical("wantGroup", "message.want.group", formLayout, new String[]{"xx"},new String[]{null} , null, 1);
		wantGroup.addActionListener(this, FormEvent.ONCLICK);
		
		coaches = uifactory.addCheckboxesVertical("coaches", "form.message.chckbx.coaches", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		coaches.select("xx", config.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES));
		partips = uifactory.addCheckboxesVertical("partips", "form.message.chckbx.partips", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		partips.select("xx", config.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS));
	
		wantGroup.select("xx", coaches.isSelected(0) || partips.isSelected(0));
		
		coachesAndPartips = FormLayoutContainer.createHorizontalFormLayout(
				"coachesAndPartips", getTranslator()
		);
		formLayout.add(coachesAndPartips);
		s1 = uifactory.addSpacerElement("s1", formLayout, true);
		
		// groups
		groupChooseSubContainer = FormLayoutContainer.createHorizontalFormLayout(
				"groupChooseSubContainer", getTranslator()
		);
		groupChooseSubContainer.setLabel("form.message.group", null);
		
		formLayout.add(groupChooseSubContainer);		
		String groupInitVal = (String) config.get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
		easyGroupTE = uifactory.addTextElement("group", null, 1024, groupInitVal, groupChooseSubContainer);
		easyGroupTE.setDisplaySize(24);
		easyGroupTE.setExampleKey("form.message.example.group", null);
		
		chooseGroupsLink = uifactory.addFormLink("choose", groupChooseSubContainer,"b_form_groupchooser");
		createGroupsLink = uifactory.addFormLink("create", groupChooseSubContainer,"b_form_groupchooser");	

		hasGroups = cev.getCourseGroupManager().getAllLearningGroupsFromAllContexts().size() > 0;
		
		// areas
		areaChooseSubContainer = FormLayoutContainer.createHorizontalFormLayout(
				"areaChooseSubContainer", getTranslator()
		);
		areaChooseSubContainer.setLabel("form.message.area", null);
		formLayout.add(areaChooseSubContainer);		
		
		groupsAndAreasSubContainer = FormLayoutContainer.createHorizontalFormLayout("groupSubContainer", getTranslator());
		formLayout.add(groupsAndAreasSubContainer);
		
		String areaInitVal = (String) config.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
		easyAreaTE = uifactory.addTextElement("area", null, 1024, areaInitVal, areaChooseSubContainer);
		easyAreaTE.setDisplaySize(24);
		easyAreaTE.setExampleKey("form.message.example.area", null);
		
		chooseAreasLink = uifactory.addFormLink("choose", areaChooseSubContainer,"b_form_groupchooser");
		createAreasLink = uifactory.addFormLink("create", areaChooseSubContainer,"b_form_groupchooser");
		
		hasAreas = cev.getCourseGroupManager().getAllAreasFromAllContexts().size() > 0;
		
		s2 = uifactory.addSpacerElement("s2", formLayout, false);
		
		wantEmail = uifactory.addCheckboxesVertical("wantEmail", "message.want.email", formLayout, new String[]{"xx"},new String[]{null} , null, 1);
		wantEmail.addActionListener(this, FormEvent.ONCLICK);
		
		//recipients
		this.eList = (List) config.get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
		String emailToAdresses = "";
		if (eList != null) {
			emailToAdresses = StringHelper.formatIdentitesAsEmailToString(eList, "\n");
			wantEmail.select("xx", eList.size()>0);
		}
		teArElEmailToAdresses = uifactory.addTextAreaElement("email", "message.emailtoadresses", -1, 3, 60, true, emailToAdresses, formLayout);
		teArElEmailToAdresses.setMandatory(true);
		
		s3 =uifactory.addSpacerElement("s3", formLayout, true);
		
		//for displaying error message in case neither group stuff nor email is selected
		recipentsContainer = FormLayoutContainer.createHorizontalFormLayout(
				"recipents", getTranslator()
		);
		formLayout.add(recipentsContainer);
		
		s4 = uifactory.addSpacerElement("s4", formLayout, false);
				
		//subject
		String mS = (String) config.get(COEditController.CONFIG_KEY_MSUBJECT_DEFAULT);
		String mSubject = (mS != null) ? mS : "";
		teElSubject = uifactory.addTextElement("mSubject", "message.subject", 255, mSubject, formLayout);
		
		//messagebody
		String mB = (String) config.get(COEditController.CONFIG_KEY_MBODY_DEFAULT);
		String mBody = (mB != null) ? mB : "";
		teArElBody = uifactory.addTextAreaElement("mBody", "message.body", 10000, 8, 60, true, mBody, formLayout);
		
		subm = uifactory.addFormSubmitButton("save", formLayout);
		
		update();
	}

	private void update () {
		
		boolean wg = wantGroup.isSelected(0);
		coaches.setVisible(wg);
		partips.setVisible(wg);
		coachesAndPartips.setVisible(wg);
		
		groupChooseSubContainer.setVisible(wg);
		areaChooseSubContainer.setVisible(wg);
		
		s1.setVisible(wg);
		s2.setVisible(wg);
		
		if (!wg) {
			coaches.select("xx", false);
			partips.select("xx", false);
			easyAreaTE.setValue("");
			easyGroupTE.setValue("");
		}
		
		hasGroups = cev.getCourseGroupManager().getAllLearningGroupsFromAllContexts().size() > 0;	
		chooseGroupsLink.setVisible(wg &&  hasGroups);
		createGroupsLink.setVisible(wg && !hasGroups);
		
		hasAreas = cev.getCourseGroupManager().getAllAreasFromAllContexts().size() > 0;
		chooseAreasLink.setVisible(wg &&  hasAreas);
		createAreasLink.setVisible(wg && !hasAreas);	
		
		teArElEmailToAdresses.setVisible(wantEmail.isSelected(0));
		teArElEmailToAdresses.clearError();
		if (!wantEmail.isSelected(0)) {
			teArElEmailToAdresses.setValue("");
			eList = null;
		}
		
		recipentsContainer.clearError();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {

		if (source == chooseGroupsLink) {
			
			removeAsListenerAndDispose(groupChooseC);
			groupChooseC = new GroupOrAreaSelectionController(
					0, getWindowControl(), ureq, "group",
					cev.getCourseGroupManager(),
					easyGroupTE.getValue()
			);
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
			OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
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
			areaChooseC = new GroupOrAreaSelectionController (
					1, getWindowControl(), ureq, "area",
					cev.getCourseGroupManager(),
					easyAreaTE.getValue()
			);
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

			OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
			removeAsListenerAndDispose(areaCreateCntrllr);
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
			OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
			easyGroupTE.setEnabled(false);
			removeAsListenerAndDispose(groupCreateCntrllr);
			groupCreateCntrllr = BGControllerFactory.getInstance().createNewBGController(
					ureq, getWindowControl(), true, courseResource, true, csvGroupName[0]
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
			OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
			easyAreaTE.setEnabled(false);
			removeAsListenerAndDispose(areaCreateCntrllr);
			areaCreateCntrllr = BGControllerFactory.getInstance().createNewAreaController(
					ureq, getWindowControl(), courseResource, true, csvAreaName[0]
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
		
		update();
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
					//singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
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
					//singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
				}
				easyAreaTE.getRootForm().submit(ureq);
			} 
		}
	}

	
	
	@Override
	protected void doDispose() {
		//
	}
}
