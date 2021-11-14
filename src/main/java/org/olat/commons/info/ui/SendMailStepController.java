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

package org.olat.commons.info.ui;

import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SendMailStepController extends StepFormBasicController {
	
	private String[] sendCourseRolesOptionKeys;
	private String[] sendCourseRolesOptionValues;
	private String[] sendGroupsOptionKeys;
	private String[] sendGroupsOptionValues;
	private String[] sendCurriculaOptionKeys;
	private String[] sendCurriculaOptionValues;
	
	private MultipleSelectionElement sendCourseRolesSelection;
	private MultipleSelectionElement sendGroupsSelection;
	private MultipleSelectionElement sendCurriculaSelection;
	
	public SendMailStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			List<SendMailOption> courseRoleOptions, List<SendMailOption> groupOptions, List<SendMailOption> curriculaOptions, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		
		sendCourseRolesOptionKeys = new String[courseRoleOptions.size()];
		sendCourseRolesOptionValues = new String[courseRoleOptions.size()];
		int count = 0;
		for(SendMailOption option:courseRoleOptions) {
			sendCourseRolesOptionKeys[count] = option.getOptionKey();
			sendCourseRolesOptionValues[count++] = option.getOptionName();
		}
		
		if (groupOptions != null && !groupOptions.isEmpty()) {
			sendGroupsOptionKeys = new String[groupOptions.size()];
			sendGroupsOptionValues = new String[groupOptions.size()];
			
			int groupCount = 0;
			for (SendMailOption groupOption : groupOptions) {
				sendGroupsOptionKeys[groupCount] = groupOption.getOptionKey();
				sendGroupsOptionValues[groupCount++] = groupOption.getOptionName();
			}
		}
		
		if (curriculaOptions != null && curriculaOptions.size() > 1) {
			sendCurriculaOptionKeys = new String[curriculaOptions.size()];
			sendCurriculaOptionValues = new String[curriculaOptions.size()];
			
			int curriculaCount = 0;
			for (SendMailOption curriculaOption : curriculaOptions) {
				sendCurriculaOptionKeys[curriculaCount] = curriculaOption.getOptionKey();
				sendCurriculaOptionValues[curriculaCount++] = curriculaOption.getOptionName();
			}
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_info_contact");
		setFormTitle("wizard.step1.title");
		setFormDescription("wizard.step1.form_description");
		sendCourseRolesSelection = uifactory.addCheckboxesVertical("wizard.step1.send_option", formLayout, sendCourseRolesOptionKeys, sendCourseRolesOptionValues, 1);
		
		if (sendGroupsOptionKeys != null && sendGroupsOptionKeys.length > 0) {
			sendGroupsSelection = uifactory.addCheckboxesVertical("wizard.step1.send_group_option", formLayout, sendGroupsOptionKeys, sendGroupsOptionValues, 1);
		}
		
		if (sendCurriculaOptionKeys != null && sendCurriculaOptionKeys.length > 0) {
			sendCurriculaSelection = uifactory.addCheckboxesVertical("wizard.step1.send_curricula_option", formLayout, sendCurriculaOptionKeys, sendCurriculaOptionValues, 1);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> selectedOptions = sendCourseRolesSelection.getSelectedKeys();
		addToRunContext(WizardConstants.SEND_MAIL, selectedOptions);
		
		if (sendGroupsSelection != null) {
			addToRunContext(WizardConstants.SEND_GROUPS, sendGroupsSelection.getSelectedKeys());
		}
		
		if (sendCurriculaSelection != null) {
			addToRunContext(WizardConstants.SEND_CURRICULA, sendCurriculaSelection.getSelectedKeys());
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}