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
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.nodes.members.ui.group.MembersSelectorFormFragment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<BR/> Configuration form for the contact form building block
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 * @author Dirk Furrer
 */
public class COConfigForm extends MembersSelectorFormFragment {
	
	private SelectionElement wantEmail;
	private SelectionElement wantOwners;
	private TextElement teArElEmailToAdresses;
	private TextElement teElSubject;
	private TextElement teArElBody;
	
	private FormItemContainer recipentsContainer;	

	private FormSubmit submitButton;

	private List<String> eList;

	/**
	 * Form constructor
	 * 
	 * @param name The form name
	 * @param config The module configuration
	 */
	protected COConfigForm(UserRequest ureq, WindowControl wControl,
			ModuleConfiguration config, UserCourseEnvironment uce) {
		super(ureq, wControl, uce.getCourseEditorEnv(), config, false, true);
	}

	@Override
	protected String getConfigKeyCoachesGroup() {
		return COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP;
	}

	@Override
	protected String getConfigKeyCoachesGroupIds() {
		return COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP_ID;
	}

	@Override
	protected String getConfigKeyCoachesArea() {
		return COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA;
	}

	@Override
	protected String getConfigKeyCoachesAreaIds() {
		return COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA_IDS;
	}

	@Override
	protected String getConfigKeyCoachesCurriculumElement() {
		return null;// not used now
	}

	@Override
	protected String getConfigKeyCoachesCurriculumElementIds() {
		return null;// not used now
	}

	@Override
	protected String getConfigKeyCoachesCourse() {
		return COEditController.CONFIG_KEY_EMAILTOCOACHES_COURSE;
	}

	@Override
	protected String getConfigKeyCoachesAssigned() {
		return COEditController.CONFIG_KEY_EMAILTOCOACHES_ASSIGNED;
	}

	@Override
	protected String getConfigKeyCoachesAll() {
		return COEditController.CONFIG_KEY_EMAILTOCOACHES_ALL;
	}
	
	@Override
	protected String getConfigKeyParticipantsGroup() {
		return COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP;
	}

	@Override
	protected String getConfigKeyParticipantsArea() {
		return COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA;
	}

	@Override
	protected String getConfigKeyParticipantsGroupIds() {
		return COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_GROUP_ID;
	}

	@Override
	protected String getConfigKeyParticipantsAreaIds() {
		return COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_AREA_ID;
	}

	@Override
	protected String getConfigKeyParticipantsCurriculumElement() {
		return null;// not used now
	}

	@Override
	protected String getConfigKeyParticipantsCurriculumElementIds() {
		return null;// not used now
	}

	@Override
	protected String getConfigKeyParticipantsCourse() {
		return COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_COURSE;
	}

	@Override
	protected String getConfigKeyParticipantsAll() {
		return COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS_ALL;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isOK = super.validateFormLogic(ureq);
		
		if (!sendToCoaches() && !sendToPartips() && !wantEmail.isSelected(0) && !sendToOwners()) {
			recipentsContainer.setErrorKey("no.recipents.specified", null);
			isOK = false;
		}
		
		/*
		 * somehow e-mail recipients must be specified, checking each of the
		 * possibility, at least one must be configured resulting in some e-mails.
		 * The case that the specified groups can contain zero members must be
		 * handled by the e-mail controller!
		 */
		String emailToAdresses = teArElEmailToAdresses.getValue();
		// Windows: \r\n Unix/OSX: \n
		String[] emailAdress = emailToAdresses.split("\\r?\\n");
		
		teArElEmailToAdresses.clearError();
		if (wantEmail.isSelected(0) &&
				(emailAdress == null || emailAdress.length == 0|| "".equals(emailAdress[0]))) {
			// otherwise the entry field shows that no e-mails are specified
			teArElEmailToAdresses.setErrorKey("email.not.specified", null);
			isOK = false;
		}
		
		//check validity of manually provided e-mails
		if ((emailAdress != null) && (emailAdress.length > 0) && (!"".equals(emailAdress[0]))) {
			this.eList = new ArrayList<>();
			for (int i = 0; i < emailAdress.length; i++) {
				String eAd = emailAdress[i].trim();
				boolean emailok = MailHelper.isValidEmailAddress(eAd);
				if (!emailok) {
					teArElEmailToAdresses.setErrorKey("email.not.valid", null);
					isOK = false;
				}
				eList.add(eAd);
			}
		}
		
		return isOK;
	}
	
	/**
	 * @return the message subject
	 */
	protected String getMSubject() {
		return teElSubject.getValue();
	}

	/**
	 * @return the message body
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

	protected boolean sendToOwners() {
		return wantOwners.isSelected(0);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_co_config_form");

		Boolean ownerSelection = config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOOWNERS);

		setFormTitle("header", null);
		setFormContextHelp("manual_user/course_elements/Administration_and_Organisation/#mail");

		//for displaying error message in case neither group stuff nor email is selected
		recipentsContainer = FormLayoutContainer.createHorizontalFormLayout("recipents", getTranslator());
		formLayout.add(recipentsContainer);
		
		wantEmail = uifactory.addCheckboxesHorizontal("wantEmail", "message.want.email", formLayout, new String[]{"xx"}, new String[]{null});
		wantEmail.addActionListener(FormEvent.ONCLICK);
		
		// External recipients
		eList = config.getList(COEditController.CONFIG_KEY_EMAILTOADRESSES, String.class);
		String emailToAdresses = "";
		if (eList != null) {
			emailToAdresses = StringHelper.formatIdentitesAsEmailToString(eList, "\n");
			wantEmail.select("xx", !eList.isEmpty());
		}
		teArElEmailToAdresses = uifactory.addTextAreaElement("email", "message.emailtoadresses", -1, 3, 60, true, false, emailToAdresses, formLayout);
		teArElEmailToAdresses.setMandatory(true);
		
		// Course authors / owners
		wantOwners = uifactory.addCheckboxesHorizontal("wantOwners","message.want.owners" , formLayout, new String[]{"xx"},new String[]{null});
		wantOwners.setElementCssClass("o_sel_co_want_owners");
		if( ownerSelection!= null){
			wantOwners.select("xx", ownerSelection.booleanValue());
		}
		
		wantOwners.addActionListener(FormEvent.ONCLICK);
		
		// include existing fragment
		super.initForm(formLayout, listener, ureq);

		//subject
		String mS = (String) config.get(COEditController.CONFIG_KEY_MSUBJECT_DEFAULT);
		String mSubject = (mS != null) ? mS : "";
		teElSubject = uifactory.addTextElement("mSubject", "message.subject", 255, mSubject, formLayout);
		MailHelper.setSubjectAsHelp(teElSubject, getLocale());
		
		//message body
		String mB = (String) config.get(COEditController.CONFIG_KEY_MBODY_DEFAULT);
		String mBody = (mB != null) ? mB : "";
		teArElBody = uifactory.addRichTextElementForStringDataMinimalistic("mBody", "message.body", mBody, 8, 60, formLayout, getWindowControl());
		MailHelper.setVariableNamesAsHelp(teArElBody, CourseMailTemplate.variableNames(), getLocale());
		
		submitButton = uifactory.addFormSubmitButton("save", formLayout);
		update();
	}

	@Override
	protected void update() {
		super.update();
		
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
		super.formInnerEvent(ureq, source, event);
		update();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		submitButton.setEnabled(true);
		// the parent takes care of dealing with fragments
		super.event(ureq, source, event);
	}

	@Override
	protected void setFormCanSubmit(boolean enable) {
		submitButton.setEnabled(enable);
	}

	@Override
	protected void storeConfiguration(ModuleConfiguration configToStore) {
		super.storeConfiguration(configToStore);
		configToStore.setBooleanEntry(COEditController.CONFIG_KEY_EMAILTOOWNERS, sendToOwners());
		configToStore.set(COEditController.CONFIG_KEY_EMAILTOADRESSES, getEmailList());
		configToStore.set(COEditController.CONFIG_KEY_MSUBJECT_DEFAULT, getMSubject());
		configToStore.set(COEditController.CONFIG_KEY_MBODY_DEFAULT, getMBody());
	}
}