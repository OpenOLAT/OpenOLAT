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
package org.olat.group.ui.wizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGMergeConfirmationStepFormController extends StepFormBasicController {

	private final List<BusinessGroup> groups;
	
	public BGMergeConfirmationStepFormController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, List<BusinessGroup> groups) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		
		this.groups = groups;
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("merge.confirmation.description");
		
		BusinessGroup selectedGroup = (BusinessGroup)getFromRunContext("targetGroup");
		StringBuilder sb = new StringBuilder();
		for(BusinessGroup group:groups) {
			if(!selectedGroup.equals(group)) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(StringHelper.escapeHtml(group.getName()));	
			}
		}
		String text = translate("merge.confirmation", new String[]{sb.toString()});
		uifactory.addStaticTextElement("merge.confirmation", null, text, formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
