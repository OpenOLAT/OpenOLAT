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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR/> Configuration form for the contact form building block
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 * @author Dirk Furrer
 */
public class COConfigForm extends FormBasicController {
	
	private SelectionElement wantEmail;
	private TextElement teArElEmailToAdresses;
	
	private SelectionElement wantOwners;
	
	private TextElement teElSubject;
	private TextElement teArElBody;
	
	private FormLayoutContainer coachesAndPartips;

	private SelectionElement wantCoaches;
	private SelectionElement wantParticipants;
	
	private SingleSelection coachesChoice;
	private SingleSelection participantsChoice;

	private StaticTextElement easyGroupParticipantsSelectionList;
	private StaticTextElement easyAreaParticipantsSelectionList;
	private StaticTextElement easyGroupCoachSelectionList;
	private FormLink chooseGroupCoachesLink;
	

	private FormLink chooseGroupParticipantsLink;
	
	private FormLink chooseAreasCoachesLink;
	private FormLink chooseAreasParticipantsLink;
	
	private StaticTextElement easyAreaCoachSelectionList;
	
	private AreaSelectionController areaChooseCoaches;
	private GroupSelectionController groupChooseCoaches;
	private AreaSelectionController areaChooseParticipants;
	private GroupSelectionController groupChooseParticipants;
	
	private FormItemContainer groupsAndAreasSubContainer;
	private FormItemContainer recipentsContainer;
	

	private FormSubmit subm;
	
	
	private CloseableModalController cmc;
	
	private List<String> eList;
	private ModuleConfiguration config;
	private CourseEditorEnv cev;
	
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired	
	private RepositoryManager repositoryManager;
	



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
		if (!wantCoaches.isSelected(0) && !wantParticipants.isSelected(0) && !wantEmail.isSelected(0) && !wantOwners.isSelected(0)) {
			recipentsContainer.setErrorKey("no.recipents.specified", null);
			return false;
		}
		recipentsContainer.clearError();
		

		coachesAndPartips.clearError();
//		if (wantGroup.isSelected(0)) {
//			if (!coaches.isSelected(0) && !partips.isSelected(0)) {
//				coachesAndPartips.setErrorKey("form.choose.coachesandpartips", null);
//			}
//			if (!validateGroupFields()) return false;
//		}
		
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
		
		//check validity of manually provided e-mails
		if ((emailAdress != null) && (emailAdress.length > 0) && (!"".equals(emailAdress[0]))) {
			this.eList = new ArrayList<String>();
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
		if(wantCoaches.isSelected(0)){
			if(!coachesChoice.isOneSelected()){
				coachesChoice.setErrorKey("error.no.choice.specified", null);
				return false;
			}
			if(coachesChoice.isSelected(2) &&(isEmpty(easyAreaCoachSelectionList)|| easyAreaCoachSelectionList == null)){
				if(easyGroupCoachSelectionList.getValue() == null && isEmpty(easyGroupCoachSelectionList) || easyGroupCoachSelectionList.getValue().equals("")){
					easyAreaCoachSelectionList.setErrorKey("error.no.group.specified", null);
					easyGroupCoachSelectionList.setErrorKey("error.no.group.specified", null);
					return false;
				}
				
			}
		}

		if(wantParticipants.isSelected(0)){
			if(!participantsChoice.isOneSelected()){
				participantsChoice.setErrorKey("error.no.choice.specified", null);
				return false;
			}
			if(participantsChoice.isSelected(2) &&(isEmpty(easyAreaParticipantsSelectionList)|| easyAreaParticipantsSelectionList == null)){
				if(easyGroupParticipantsSelectionList.getValue() == null && isEmpty(easyGroupParticipantsSelectionList)|| easyGroupParticipantsSelectionList.getValue().equals("")){
					easyAreaParticipantsSelectionList.setErrorKey("error.no.group.specified", null);
					easyGroupParticipantsSelectionList.setErrorKey("error.no.group.specified", null);
					return false;
				}
			}
		}

		return true;
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
	protected List<String> getEmailList() {
		return eList;
	}

	/**
	 * returns the choosen groups, or null if no groups were choosen.
	 * 
	 * @return
	 */
	protected String getEmailGroupCoaches() {
		if (!isEmpty(easyGroupCoachSelectionList) && wantCoaches.isSelected(0) && coachesChoice.isSelected(2)) {
			return easyGroupCoachSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getEmailGroupCoachesIds() {
		if (!isEmpty(easyGroupCoachSelectionList) && wantCoaches.isSelected(0) && coachesChoice.isSelected(2)) {
			return getKeys(easyGroupCoachSelectionList);
		}
		return null;
	}
	
	protected String getEmailGroupParticipants() {
		if (!isEmpty(easyGroupParticipantsSelectionList) && wantParticipants.isSelected(0)&& participantsChoice.isSelected(2)) {
			return easyGroupParticipantsSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getEmailGroupParticipantsIds() {
		if (!isEmpty(easyGroupParticipantsSelectionList) && wantParticipants.isSelected(0)&& participantsChoice.isSelected(2)) {
			return getKeys(easyGroupParticipantsSelectionList);
		}
		return null;
	}

	/**
	 * returns the choosen learning areas, or null if no ares were choosen.
	 */
	protected String getEmailCoachesAreas() {
		if(!isEmpty(easyAreaCoachSelectionList)&&wantCoaches.isSelected(0)&& coachesChoice.isSelected(2)) {
			return easyAreaCoachSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getEmailCoachesAreaIds() {
		if(!isEmpty(easyAreaCoachSelectionList)&&wantCoaches.isSelected(0)&& coachesChoice.isSelected(2)) {
			return getKeys(easyAreaCoachSelectionList);
		}
		return null;
	}

	protected String getEmailParticipantsAreas() {
		if(!isEmpty(easyAreaParticipantsSelectionList)&& wantParticipants.isSelected(0)&& participantsChoice.isSelected(2)) {
			return easyAreaParticipantsSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getEmailParticipantsAreaIds() {
		if(!isEmpty(easyAreaParticipantsSelectionList)&& wantParticipants.isSelected(0)&& participantsChoice.isSelected(2)) {
			return getKeys(easyAreaParticipantsSelectionList);
		}
		return null;
	}

	protected boolean sendToPartips() {
		return wantParticipants.isSelected(0);
	}
	
	protected boolean sendToOwners() {
		return wantOwners.isSelected(0);
	}
	
	protected boolean sendToCoachesCourse(){
		return coachesChoice.isSelected(1)&& wantCoaches.isSelected(0);
	}
	
	protected boolean sendToCoachesAll(){
		return coachesChoice.isSelected(0)&& wantCoaches.isSelected(0);
	}
	
	protected boolean sendToCoachesGroup(){
		return coachesChoice.isSelected(2) && wantCoaches.isSelected(0);
	}
	
	protected boolean sendToParticipantsCourse(){
		return participantsChoice.isSelected(1)&& wantParticipants.isSelected(0);
	}
	
	protected boolean sendToParticipantsAll(){
		return participantsChoice.isSelected(0)&& wantParticipants.isSelected(0);
	}
	
	protected boolean sendToParticipantsGroup(){
		return participantsChoice.isSelected(2) && wantParticipants.isSelected(0);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
	
		
		Boolean ownerSelection = config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOOWNERS);
		Boolean coacheSelection = config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_ALL) || config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_COURSE) || config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP) != null || config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA) != null;

		
		setFormTitle("header", null);
		setFormContextHelp("org.olat.course.nodes.co","ced-co.html","help.hover.co");
		
		//for displaying error message in case neither group stuff nor email is selected
				recipentsContainer = FormLayoutContainer.createHorizontalFormLayout(
						"recipents", getTranslator()
				);
				formLayout.add(recipentsContainer);
		
		wantEmail = uifactory.addCheckboxesHorizontal("wantEmail", "message.want.email", formLayout, new String[]{"xx"}, new String[]{null});
		wantEmail.addActionListener(FormEvent.ONCLICK);
		
		//recipients
		eList = (List<String>) config.get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
		String emailToAdresses = "";
		if (eList != null) {
			emailToAdresses = StringHelper.formatIdentitesAsEmailToString(eList, "\n");
			wantEmail.select("xx", eList.size()>0);
		}
		teArElEmailToAdresses = uifactory.addTextAreaElement("email", "message.emailtoadresses", -1, 3, 60, true, emailToAdresses, formLayout);
		teArElEmailToAdresses.setMandatory(true);

		wantOwners = uifactory.addCheckboxesHorizontal("wantOwners","message.want.owners" , formLayout, new String[]{"xx"},new String[]{null});
		if( ownerSelection!= null){
			wantOwners.select("xx", ownerSelection.booleanValue());
		}

		wantOwners.addActionListener(FormEvent.ONCLICK);
		

		
		wantCoaches = uifactory.addCheckboxesHorizontal("coaches", "message.want.coaches", formLayout, new String[]{"xx"},new String[]{null});
		if(coacheSelection != null && coacheSelection) wantCoaches.select("xx", true);

		wantCoaches.addActionListener(FormEvent.ONCLICK);
		
		
		coachesChoice = uifactory.addRadiosVertical(
				"coachesChoice", null, formLayout, 
				new String[]{"all", "course", "group"},
				new String[]{translate("form.message.coaches.all"), translate("form.message.coaches.course"), translate("form.message.coaches.group")}
		);
		if(config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_ALL)) coachesChoice.select("all", true);
		if(config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_COURSE)) coachesChoice.select("course", true);
		if(config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP) != null || config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA) != null) coachesChoice.select("group", true);
		coachesChoice.addActionListener(FormEvent.ONCLICK);
		coachesChoice.setVisible(false);
		
		
		String groupCoachesInitVal;
		@SuppressWarnings("unchecked")
		List<Long> groupCoachesKeys = (List<Long>)config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP_ID);
		if(groupCoachesKeys == null) {
			groupCoachesInitVal = (String) config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP);
			groupCoachesKeys = businessGroupService.toGroupKeys(groupCoachesInitVal, cev.getCourseGroupManager().getCourseEntry());
		}
		groupCoachesInitVal = getGroupNames(groupCoachesKeys);

		easyGroupCoachSelectionList = uifactory.addStaticTextElement("groupCoaches", "form.message.group", groupCoachesInitVal, formLayout);
		easyGroupCoachSelectionList.setUserObject(groupCoachesKeys);
		
		easyGroupCoachSelectionList.setVisible(false);
		
		chooseGroupCoachesLink = uifactory.addFormLink("groupCoachesChoose", formLayout, "o_form_groupchooser");
		chooseGroupCoachesLink.setVisible(false);
		
		
		String areaCoachesInitVal;
		@SuppressWarnings("unchecked")
		List<Long> areaCoachesKeys = (List<Long>)config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS);
		if(areaCoachesKeys == null) {
			areaCoachesInitVal = (String)config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA);
			areaCoachesKeys = areaManager.toAreaKeys(areaCoachesInitVal, cev.getCourseGroupManager().getCourseResource());
		}
		areaCoachesInitVal = getAreaNames(areaCoachesKeys);

		easyAreaCoachSelectionList = uifactory.addStaticTextElement("areaCoaches", "form.message.area", areaCoachesInitVal, formLayout);
		easyAreaCoachSelectionList.setUserObject(areaCoachesKeys);
		easyAreaCoachSelectionList.setVisible(false);
		
		chooseAreasCoachesLink = uifactory.addFormLink("areaCoachesChoose", formLayout, "o_form_groupchooser");
				
				
				
		
				
				
		// PARTICIPANTS
		Boolean particiapntSelection = config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_ALL) || config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_COURSE) || config.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP) != null || config.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA) != null;
		
		wantParticipants = uifactory.addCheckboxesHorizontal("participants", "message.want.participants", formLayout, new String[]{"xx"},new String[]{null});
		if(particiapntSelection != null && particiapntSelection) wantParticipants.select("xx", true);
		wantParticipants.addActionListener(FormEvent.ONCLICK);
		
		participantsChoice = uifactory.addRadiosVertical(
				"participantsChoice", null, formLayout, 
				new String[]{"all", "course", "group"},
				new String[]{translate("form.message.participants.all"), translate("form.message.participants.course"), translate("form.message.participants.group")}
		);
		if(config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_ALL)) participantsChoice.select("all", true);
		if(config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_COURSE)) participantsChoice.select("course", true);
		if(config.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP) != null || config.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA) != null) participantsChoice.select("group", true);
		participantsChoice.addActionListener(FormEvent.ONCLICK);
		participantsChoice.setVisible(false); 
		
		String groupParticipantsInitVal;
		@SuppressWarnings("unchecked")
		List<Long> groupParticipantsKeys = (List<Long>)config.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP_ID);
		if(groupParticipantsKeys == null) {
			groupParticipantsInitVal = (String)config.get(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP);
			groupParticipantsKeys = businessGroupService.toGroupKeys(groupParticipantsInitVal, cev.getCourseGroupManager().getCourseEntry());
		}
		groupParticipantsInitVal = getGroupNames(groupParticipantsKeys);

		easyGroupParticipantsSelectionList = uifactory.addStaticTextElement("groupParticipants", "form.message.group", groupParticipantsInitVal, formLayout);
		easyGroupParticipantsSelectionList.setUserObject(groupParticipantsKeys);
		easyGroupParticipantsSelectionList.setVisible(false);
		
		chooseGroupParticipantsLink = uifactory.addFormLink("groupParticipantsChoose", formLayout, "o_form_groupchooser");
		chooseGroupParticipantsLink.setVisible(false);
		
		String areaParticipantsInitVal;
		@SuppressWarnings("unchecked")
		List<Long> areaParticipantsKeys = (List<Long>)config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS);
		if(areaParticipantsKeys == null) {
			areaParticipantsInitVal = (String)config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA);
			areaParticipantsKeys = areaManager.toAreaKeys(areaParticipantsInitVal, cev.getCourseGroupManager().getCourseResource());
		}
		areaParticipantsInitVal = getAreaNames(areaParticipantsKeys);

		easyAreaParticipantsSelectionList = uifactory.addStaticTextElement("areaParticipants", "form.message.area", areaParticipantsInitVal, formLayout);
		easyAreaParticipantsSelectionList.setUserObject(areaParticipantsKeys);
		easyAreaParticipantsSelectionList.setVisible(false);
		
		
		chooseAreasParticipantsLink = uifactory.addFormLink("areaParticipantsChoose", formLayout, "o_form_groupchooser");
		chooseAreasParticipantsLink.setVisible(false);
		

		coachesAndPartips = FormLayoutContainer
				.createHorizontalFormLayout("coachesAndPartips", getTranslator());
		formLayout.add(coachesAndPartips);

		

	
		groupsAndAreasSubContainer = FormLayoutContainer.createHorizontalFormLayout("groupSubContainer", getTranslator());
		formLayout.add(groupsAndAreasSubContainer);
		
		
		

		
		uifactory.addSpacerElement("s4", formLayout, false);
				
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
		
		
		coachesChoice.setVisible(wantCoaches.isSelected(0));
		chooseGroupCoachesLink.setVisible(coachesChoice.isSelected(2) && wantCoaches.isSelected(0));
		chooseAreasCoachesLink.setVisible(coachesChoice.isSelected(2) && wantCoaches.isSelected(0));
		easyGroupCoachSelectionList.setVisible(coachesChoice.isSelected(2) && wantCoaches.isSelected(0));
		easyAreaCoachSelectionList.setVisible(coachesChoice.isSelected(2) && wantCoaches.isSelected(0));
		
		
		participantsChoice.setVisible(wantParticipants.isSelected(0));
		chooseGroupParticipantsLink.setVisible(participantsChoice.isSelected(2) && wantParticipants.isSelected(0));
		chooseAreasParticipantsLink.setVisible(participantsChoice.isSelected(2) && wantParticipants.isSelected(0));
		easyGroupParticipantsSelectionList.setVisible(participantsChoice.isSelected(2) && wantParticipants.isSelected(0));
		easyAreaParticipantsSelectionList.setVisible(participantsChoice.isSelected(2) && wantParticipants.isSelected(0));
		
		teArElEmailToAdresses.setVisible(wantEmail.isSelected(0));
		teArElEmailToAdresses.clearError();
		if (!wantEmail.isSelected(0)) {
			teArElEmailToAdresses.setValue("");
			eList = null;
		}
		
		recipentsContainer.clearError();
		flc.setDirty(true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseGroupCoachesLink) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(groupChooseCoaches);
			
			
			
			groupChooseCoaches = new GroupSelectionController(ureq, getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyGroupCoachSelectionList));
			listenTo(groupChooseCoaches);
			

			cmc = new CloseableModalController(getWindowControl(), "close", groupChooseCoaches.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			subm.setEnabled(false);
		} else if(source == chooseGroupParticipantsLink){
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(groupChooseParticipants);
			
			groupChooseParticipants = new GroupSelectionController(ureq, getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyGroupParticipantsSelectionList));
			listenTo(groupChooseParticipants);
			

			cmc = new CloseableModalController(getWindowControl(), "close", groupChooseParticipants.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			subm.setEnabled(false);
		} else if (source == chooseAreasCoachesLink) {
			// already areas -> choose areas
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(areaChooseCoaches);
			
			areaChooseCoaches = new AreaSelectionController (ureq, getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyAreaCoachSelectionList));
			listenTo(areaChooseCoaches);

			cmc = new CloseableModalController(getWindowControl(), "close", areaChooseCoaches.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			subm.setEnabled(false);
		} else if (source == chooseAreasParticipantsLink){
			// already areas -> choose areas
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(areaChooseParticipants);
			
			areaChooseParticipants = new AreaSelectionController (ureq, getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyAreaParticipantsSelectionList));
			listenTo(areaChooseParticipants);

			cmc = new CloseableModalController(getWindowControl(), "close", areaChooseParticipants.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			subm.setEnabled(false);
		} 

		update();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		subm.setEnabled(true);
		if (source == groupChooseCoaches) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyGroupCoachSelectionList.setValue(StringHelper.formatAsSortUniqCSVString(groupChooseCoaches.getSelectedNames()));
				easyGroupCoachSelectionList.setUserObject(groupChooseCoaches.getSelectedKeys());
				easyGroupCoachSelectionList.getRootForm().submit(ureq);
			} else if (Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
			}
		} else if (source == areaChooseCoaches) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyAreaCoachSelectionList.setValue(StringHelper.formatAsSortUniqCSVString(areaChooseCoaches.getSelectedNames()));
				easyAreaCoachSelectionList.setUserObject(areaChooseCoaches.getSelectedKeys());
				easyAreaCoachSelectionList.getRootForm().submit(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		} else if (source == groupChooseParticipants) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyGroupParticipantsSelectionList.setValue(StringHelper.formatAsSortUniqCSVString(groupChooseParticipants.getSelectedNames()));
				easyGroupParticipantsSelectionList.setUserObject(groupChooseParticipants.getSelectedKeys());
				easyGroupParticipantsSelectionList.getRootForm().submit(ureq);
			} else if (Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
			}
		} else if (source == areaChooseParticipants) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyAreaParticipantsSelectionList.setValue(StringHelper.formatAsSortUniqCSVString(areaChooseParticipants.getSelectedNames()));
				easyAreaParticipantsSelectionList.setUserObject(areaChooseParticipants.getSelectedKeys());
				easyAreaParticipantsSelectionList.getRootForm().submit(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		}

	}

	@Override
	protected void doDispose() {
		//
	}
	
	private boolean isEmpty(StaticTextElement element) {
		List<Long> keys = getKeys(element);
		if(keys == null || keys.isEmpty()) {
			return true;
		}
		return false;
	}
	
	private List<Long> getKeys(StaticTextElement element) {
		@SuppressWarnings("unchecked")
		List<Long> keys = (List<Long>)element.getUserObject();
		if(keys == null) {
			keys = new ArrayList<Long>();
			element.setUserObject(keys);
		}
		return keys;
	}
	
	
	private String getGroupNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder();
		List<BusinessGroupShort> groups = businessGroupService.loadShortBusinessGroups(keys);
		for(BusinessGroupShort group:groups) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(group.getName());
		}
		return sb.toString();
	}
	
	private String getAreaNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder();
		List<BGArea> areas = areaManager.loadAreas(keys);
		for(BGArea area:areas) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(area.getName());
		}
		return sb.toString();
	}

}