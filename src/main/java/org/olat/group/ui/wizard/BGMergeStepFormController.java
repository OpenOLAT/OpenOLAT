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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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
public class BGMergeStepFormController extends StepFormBasicController {
	
	private SingleSelection targetGroupEl;
	
	private final List<BusinessGroup> groups;

	public BGMergeStepFormController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, List<BusinessGroup> groups) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		
		this.groups = groups;
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("merge.description");
		
		String[] theKeys = new String[groups.size()];
		String[] theValues = new String[groups.size()];
		int i=0;
		for(BusinessGroup group:groups) {
			theValues[i] = StringHelper.escapeHtml(group.getName());
			theKeys[i++] = group.getKey().toString();
		}
		targetGroupEl = uifactory.addRadiosVertical("target.group", "merge.target.group", formLayout, theKeys, theValues);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		targetGroupEl.clearError();
		if(!targetGroupEl.isOneSelected()) {
			allOk = false;
		}

		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String selectedKey = targetGroupEl.getSelectedKey();
		for(BusinessGroup group:groups) {
			if(selectedKey.equals(group.getKey().toString())) {
				addToRunContext("targetGroup", group);
				groups.remove(group);
				break;
			}
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
