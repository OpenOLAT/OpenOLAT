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
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.MembersCourseNode;
import org.olat.course.nodes.members.ui.group.MembersSelectorFormFragment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<BR/>
 * Configuration form for the members coursenode
 *
 * Initial Date: Sep 11, 2015
 *
 * @autohr dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class MembersConfigForm extends MembersSelectorFormFragment {

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	private static final String[] emailFctKeys = new String[]{ MembersCourseNode.EMAIL_FUNCTION_ALL, MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN };

	private MultipleSelectionElement showOwners;
	private SingleSelection emailFunctionEl;
	private SingleSelection downloadFunctionEl;

	private FormSubmit submitButton;

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
	protected MembersConfigForm(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment euce, ModuleConfiguration config) {
		super(ureq, wControl, euce.getCourseEditorEnv(), config, true, false);
	}

	@Override
	protected String getConfigKeyCoachesGroup() {
		return MembersCourseNode.CONFIG_KEY_COACHES_GROUP;
	}

	@Override
	protected String getConfigKeyCoachesArea() {
		return MembersCourseNode.CONFIG_KEY_COACHES_AREA;
	}

	@Override
	protected String getConfigKeyCoachesGroupIds() {
		return MembersCourseNode.CONFIG_KEY_COACHES_GROUP_ID;
	}

	@Override
	protected String getConfigKeyCoachesAreaIds() {
		return MembersCourseNode.CONFIG_KEY_COACHES_AREA_IDS;
	}
	
	@Override
	protected String getConfigKeyCoachesCurriculumElement() {
		return MembersCourseNode.CONFIG_KEY_COACHES_CUR_ELEMENT;
	}
	
	@Override
	protected String getConfigKeyCoachesCurriculumElementIds() {
		return MembersCourseNode.CONFIG_KEY_COACHES_CUR_ELEMENT_ID;
	}

	@Override
	protected String getConfigKeyCoachesCourse() {
		return MembersCourseNode.CONFIG_KEY_COACHES_COURSE;
	}
	
	@Override
	protected String getConfigKeyCoachesAssigned() {
		return null;
	}

	@Override
	protected String getConfigKeyCoachesAll() {
		return MembersCourseNode.CONFIG_KEY_COACHES_ALL;
	}

	@Override
	protected String getConfigKeyParticipantsGroup() {
		return MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP;
	}
	
	@Override
	protected String getConfigKeyParticipantsArea() {
		return MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA;
	}

	@Override
	protected String getConfigKeyParticipantsGroupIds() {
		return MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP_ID;
	}

	@Override
	protected String getConfigKeyParticipantsAreaIds() {
		return MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA_ID;
	}

	@Override
	protected String getConfigKeyParticipantsCurriculumElement() {
		return MembersCourseNode.CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT;
	}

	@Override
	protected String getConfigKeyParticipantsCurriculumElementIds() {
		return MembersCourseNode.CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT_ID;
	}

	@Override
	protected String getConfigKeyParticipantsCourse() {
		return MembersCourseNode.CONFIG_KEY_PARTICIPANTS_COURSE;
	}

	@Override
	protected String getConfigKeyParticipantsAll() {
		return MembersCourseNode.CONFIG_KEY_PARTICIPANTS_ALL;
	}

	@Override
	public void storeConfiguration(ModuleConfiguration configToStore) {
		configToStore.setBooleanEntry(MembersCourseNode.CONFIG_KEY_SHOWOWNER, showOwners.isSelected(0));
		super.storeConfiguration(configToStore);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// set Formtitle and infobar
		setFormTitle("pane.tab.membersconfig");
		setFormInfo("members.info");
		setFormContextHelp("manual_user/course_elements/Communication_and_Collaboration/#participant_list");
		formLayout.setElementCssClass("o_sel_cmembers_settings");
		// Read Configuration
		boolean showOwnerConfig = config.getBooleanSafe(MembersCourseNode.CONFIG_KEY_SHOWOWNER);

		// Generate widgets
		showOwners = uifactory.addCheckboxesHorizontal("members.owners", formLayout, onKeys, onValues);
		showOwners.addActionListener(FormEvent.ONCLICK);

		// include existing fragment
		super.initForm(formLayout, listener, ureq);

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
		String[] downloadFctValues = new String[]{
				translate("download.function.all"), translate("download.function.coachAndAdmin")
		};
		downloadFunctionEl = uifactory.addRadiosVertical("download", "download.function", formLayout, emailFctKeys, downloadFctValues);
		downloadFunctionEl.addActionListener(FormEvent.ONCLICK);
		String downloadFct =  config.getStringValue(MembersCourseNode.CONFIG_KEY_DOWNLOAD_FUNCTION, MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN);
		if(MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN.equals(downloadFct)) {
			downloadFunctionEl.select(MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN, true);
		} else if(MembersCourseNode.EMAIL_FUNCTION_ALL.equals(downloadFct)) {
			downloadFunctionEl.select(MembersCourseNode.EMAIL_FUNCTION_ALL, true);
		}
		
		submitButton = uifactory.addFormSubmitButton("save", formLayout);
		
		update();
	}

	//method to check if the of of the checkboxes needs to be disabled in order to ensure a valid configuration
	//in the rare case of an invalid config all checkboxes are enabled
	@Override
	protected void update() {
		super.update();
		flc.setDirty(true);		
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		
		if(showOwners == source) {
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
		} else if (downloadFunctionEl == source) {
			if (downloadFunctionEl.isOneSelected()) {
				if (downloadFunctionEl.isSelected(0)) {					
					config.setStringValue(MembersCourseNode.CONFIG_KEY_DOWNLOAD_FUNCTION, MembersCourseNode.EMAIL_FUNCTION_ALL);
				} else {
					config.setStringValue(MembersCourseNode.CONFIG_KEY_DOWNLOAD_FUNCTION, MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN);
				}
			}
		}
		
		update();
	}

	@Override
	protected void setFormCanSubmit(boolean enable) {
		submitButton.setEnabled(enable);
	}
}
