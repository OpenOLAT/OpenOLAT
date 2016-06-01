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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.IFormFragmentController;
import org.olat.core.gui.components.form.flexible.impl.IFormFragmentHost;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.editor.formfragments.MembersSelectorFormFragment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.IModuleConfiguration;
import org.olat.modules.ModuleConfiguration;

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

	// --
//	private SelectionElement wantCoaches;	
//	private SingleSelection coachesChoice;	
//	private FormLink chooseGroupCoachesLink;
//	private GroupSelectionController groupChooseCoaches;
//	private StaticTextElement easyGroupCoachSelectionList;
//	private FormLink chooseAreasCoachesLink;
//	private AreaSelectionController areaChooseCoaches;
//	private StaticTextElement easyAreaCoachSelectionList;

	// --
//	private SelectionElement wantParticipants;
//	private SingleSelection participantsChoice;
//	private FormLink chooseGroupParticipantsLink;
//	private GroupSelectionController groupChooseParticipants;
//	private StaticTextElement easyGroupParticipantsSelectionList;
//	private FormLink chooseAreasParticipantsLink;
//	private AreaSelectionController areaChooseParticipants;
//	private StaticTextElement easyAreaParticipantsSelectionList;
	// --

	private final MembersSelectorFormFragment membersFragment;
	
	private FormItemContainer recipentsContainer;	

	private FormSubmit subm;
	
//	private CloseableModalController cmc;
	
	private List<String> eList;
	private ModuleConfiguration config;
//	private CourseEditorEnv cev;
	
//	@Autowired
//	private BGAreaManager areaManager;
//	@Autowired
//	private BusinessGroupService businessGroupService;


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
//		this.cev = uce.getCourseEditorEnv();
		
		membersFragment = new MembersSelectorFormFragment(uce.getCourseEditorEnv());
		registerFormFragment(membersFragment);	// register with parent for proper lifecycle handling
		initForm(ureq);
		validateFormLogic(ureq);
	}

	@Override
	public void storeFormData(UserRequest ureq) {
		membersFragment.storeConfiguration(ureq, IModuleConfiguration.fragment("emailTo", "", config));
	}

	/**
	 * @see org.olat.core.gui.components.Form#validate(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isOK = true;
		
		if (!membersFragment.sendToCoaches() && !membersFragment.sendToPartips() && !wantEmail.isSelected(0) && !sendToOwners()) {
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
			this.eList = new ArrayList<String>();
			for (int i = 0; i < emailAdress.length; i++) {
				String eAd = emailAdress[i].trim();
				boolean emailok = MailHelper.isValidEmailAddress(eAd);
				if (emailok == false) {
					teArElEmailToAdresses.setErrorKey("email.not.valid", null);
					isOK = false;
				}
				eList.add(eAd);
			}
		}
		
		return isOK 
				& membersFragment.validateFormLogic(ureq) 
				& super.validateFormLogic(ureq);
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
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	public IFormFragmentHost getFragmentHostInterface() {
		return new IFormFragmentHost() {
			final Translator parent = COConfigForm.this.getTranslator();
			final Translator delegate = Util.createPackageTranslator(MembersSelectorFormFragment.class, parent.getLocale(), parent);
			final IFormFragmentController adapter = IFormFragmentController.fragmentControllerAdapter(COConfigForm.this, canSubmit -> {
				subm.setEnabled(canSubmit);
			});

			@Override
			public Translator getFragmentTranslator() {
				return delegate;
			}

			@Override
			public IFormFragmentController getFragmentController() {
				return adapter;
			}
		};
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		Boolean ownerSelection = config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOOWNERS);
//		Boolean coacheSelection = config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_ALL) || config.getBooleanSafe(COEditController.CONFIG_KEY_EMAILTOCOACHES_COURSE) || config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_GROUP) != null || config.get(COEditController.CONFIG_KEY_EMAILTOCOACHES_AREA) != null;

		
		setFormTitle("header", null);
		setFormContextHelp("Administration and Organisation#_mail");

		//for displaying error message in case neither group stuff nor email is selected
				recipentsContainer = FormLayoutContainer.createHorizontalFormLayout(
						"recipents", getTranslator()
				);
				formLayout.add(recipentsContainer);
		
		wantEmail = uifactory.addCheckboxesHorizontal("wantEmail", "message.want.email", formLayout, new String[]{"xx"}, new String[]{null});
		wantEmail.addActionListener(FormEvent.ONCLICK);
		
		// External recipients
		eList = (List<String>) config.get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
		String emailToAdresses = "";
		if (eList != null) {
			emailToAdresses = StringHelper.formatIdentitesAsEmailToString(eList, "\n");
			wantEmail.select("xx", eList.size()>0);
		}
		teArElEmailToAdresses = uifactory.addTextAreaElement("email", "message.emailtoadresses", -1, 3, 60, true, emailToAdresses, formLayout);
		teArElEmailToAdresses.setMandatory(true);
		
		// Course authors / owners
		wantOwners = uifactory.addCheckboxesHorizontal("wantOwners","message.want.owners" , formLayout, new String[]{"xx"},new String[]{null});
		if( ownerSelection!= null){
			wantOwners.select("xx", ownerSelection.booleanValue());
		}
		
		wantOwners.addActionListener(FormEvent.ONCLICK);
		
		// include existing fragment
		IModuleConfiguration emailToFrag = IModuleConfiguration.fragment("emailTo", "", config);
		membersFragment.initFormFragment(ureq, this, this, emailToFrag);

		//subject
		String mS = (String) config.get(COEditController.CONFIG_KEY_MSUBJECT_DEFAULT);
		String mSubject = (mS != null) ? mS : "";
		teElSubject = uifactory.addTextElement("mSubject", "message.subject", 255, mSubject, formLayout);
		
		//message body
		String mB = (String) config.get(COEditController.CONFIG_KEY_MBODY_DEFAULT);
		String mBody = (mB != null) ? mB : "";
		teArElBody = uifactory.addTextAreaElement("mBody", "message.body", 10000, 8, 60, true, mBody, formLayout);
		
		subm = uifactory.addFormSubmitButton("save", formLayout);
		
		
		update();
	}

	private void update () {
		membersFragment.refreshContents();
		
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
		/*boolean processed = */this.membersFragment.processFormEvent(ureq, source, event);
		
		update();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		subm.setEnabled(true);

		// the parent takes care of dealing with fragments
		super.event(ureq, source, event);
		
	}

	@Override
	protected void doDispose() {
		membersFragment.dispose();
	}
	
}