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

package org.olat.course.nodes.members;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.IFormFragmentController;
import org.olat.core.gui.components.form.flexible.impl.IFormFragmentHost;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.editor.formfragments.MembersSelectorFormFragment;
import org.olat.course.nodes.MembersCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.IModuleConfiguration;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<BR/>
 * Configuration form for the members coursenode
 *
 * Initial Date: Sep 11, 2015
 *
 * @autohr dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class MembersConfigForm extends FormBasicController {

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	private static final String[] emailFctKeys = new String[]{ MembersCourseNode.EMAIL_FUNCTION_ALL, MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN };

	private final ModuleConfiguration config;
	private MultipleSelectionElement showOwners;
	private SingleSelection emailFunctionEl;

	private final MembersSelectorFormFragment membersFragment;
	
	private FormSubmit subm;

	/**
	 * Form constructor
	 *
	 * @param name
	 *            The form name
	 * @param config
	 *            The module configuration
	 * @param withCancel
	 *            true: cancel button is rendered, false: no cancel button
	 */
	protected MembersConfigForm(UserRequest ureq, WindowControl wControl, UserCourseEnvironment euce,
			ModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		membersFragment = new MembersSelectorFormFragment(euce.getCourseEditorEnv());
		registerFormFragment(membersFragment);	// register with parent for proper lifecycle handling
		initForm(ureq);
		validateFormLogic(ureq);
	}

	@Override
	public void storeFormData(UserRequest ureq) {
		config.setBooleanEntry(MembersCourseNode.CONFIG_KEY_SHOWOWNER, showOwners.isSelected(0));
		membersFragment.storeConfiguration(ureq, IModuleConfiguration.fragment("members", config));
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isOK = true;

		return isOK
				& membersFragment.validateFormLogic(ureq) 
				& super.validateFormLogic(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// set Formtitle and infobar
		setFormTitle("pane.tab.membersconfig");
		setFormInfo("members.info");
		setFormContextHelp("Communication and Collaboration#_teilnehmerliste");
		// Read Configuration
		boolean showOwnerConfig = config.getBooleanSafe(MembersCourseNode.CONFIG_KEY_SHOWOWNER);

		// Generate widgets
		showOwners = uifactory.addCheckboxesHorizontal("members.owners", formLayout, onKeys, onValues);

		showOwners.addActionListener(FormEvent.ONCLICK);

		// include existing fragment
		IModuleConfiguration membersFrag = IModuleConfiguration.fragment("members", config);
		membersFragment.initFormFragment(ureq, this, this, membersFrag);

		// select initial state according to config
		showOwners.select("on", showOwnerConfig);
		
		String[] emailFctValues = new String[]{
				translate("email.function.all"), translate("email.function.coachAndAdmin")
		};
		emailFunctionEl = uifactory.addRadiosVertical("emails", "email.function", formLayout, emailFctKeys, emailFctValues);
		emailFunctionEl.addActionListener(FormEvent.ONCLICK);
		String emailFct =  config.getStringValue(MembersCourseNode.CONFIG_KEY_EMAIL_FUNCTION, MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN);
		if(MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN.equals(emailFct)) {
			emailFunctionEl.select(MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN, true);
		} else if(MembersCourseNode.EMAIL_FUNCTION_ALL.equals(emailFct)) {
			emailFunctionEl.select(MembersCourseNode.EMAIL_FUNCTION_ALL, true);
		}
		
		subm = uifactory.addFormSubmitButton("save", formLayout);
		
		update();
	}

	@Override
	protected void doDispose() {
		membersFragment.dispose();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	//method to check if the of of the checkboxes needs to be disabled in order to ensure a valid configuration
	//in the rare case of an invalid config all checkboxes are enabled
	private void update() {
		membersFragment.refreshContents();

		flc.setDirty(true);		
	}

	@Override
	public IFormFragmentHost getFragmentHostInterface() {
		return new IFormFragmentHost() {
			final Translator parent = MembersConfigForm.this.getTranslator();
			final Translator delegate = Util.createPackageTranslator(MembersSelectorFormFragment.class, parent.getLocale(), parent);
			final IFormFragmentController adapter = IFormFragmentController.fragmentControllerAdapter(MembersConfigForm.this, canSubmit -> {
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		membersFragment.processFormEvent(ureq, source, event);
		
		if(showOwners == source) { // || showCoaches == source || showParticipants == source) {
			config.setBooleanEntry(MembersCourseNode.CONFIG_KEY_SHOWOWNER, showOwners.isSelected(0));
			update();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(emailFunctionEl == source) {
			if(emailFunctionEl.isOneSelected()) {
				if(emailFunctionEl.isSelected(0)) {
					config.setStringValue(MembersCourseNode.CONFIG_KEY_EMAIL_FUNCTION, MembersCourseNode.EMAIL_FUNCTION_ALL);
				} else {
					config.setStringValue(MembersCourseNode.CONFIG_KEY_EMAIL_FUNCTION, MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN);
				}
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
		
		update();
	}

}
