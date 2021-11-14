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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.BusinessGroupFormController;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGCopySingleGroupStepController extends StepFormBasicController   {
	
	private BusinessGroup originalGroup;
	private BusinessGroupFormController groupController;
	
	public BGCopySingleGroupStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			BusinessGroup originalGroup) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "wrapper");
		
		this.originalGroup = originalGroup;
		groupController = new BusinessGroupFormController(ureq, getWindowControl(), originalGroup, mainForm);
		listenTo(groupController);
		groupController.setGroupName(originalGroup.getName() + " " + translate("bgcopywizard.copyform.name.copy"));	
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("wrapped", groupController.getInitialFormItem());
	}

	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(groupController);
		mainForm.removeSubFormListener(this);
        super.doDispose();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return groupController.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		@SuppressWarnings("unchecked")
		List<BGCopyBusinessGroup> copies = (List<BGCopyBusinessGroup>)getFromRunContext("groupsCopy");
		if(copies == null) {
			copies = new ArrayList<>();
			addToRunContext("groupsCopy", copies);
		}
		
		BGCopyBusinessGroup currentCopy = getWithOriginal(copies);
		if(currentCopy == null) {
			BGCopyBusinessGroup group = new BGCopyBusinessGroup(originalGroup);
			group.setNames(groupController.getGroupNames());
			group.setDescription(groupController.getGroupDescription());
			group.setMinParticipants(groupController.getGroupMin());
			group.setMaxParticipants(groupController.getGroupMax());
			group.setAllowToLeave(groupController.isAllowToLeave());
			copies.add(group);
		} else {
			currentCopy.setNames(groupController.getGroupNames());
			currentCopy.setDescription(groupController.getGroupDescription());
			currentCopy.setMinParticipants(groupController.getGroupMin());
			currentCopy.setMaxParticipants(groupController.getGroupMax());
			currentCopy.setAllowToLeave(groupController.isAllowToLeave());
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private BGCopyBusinessGroup getWithOriginal(List<BGCopyBusinessGroup> copies) {
		for(BGCopyBusinessGroup copy:copies) {
			if(copy.getOriginal().equals(originalGroup)) {
				return copy;
			}
		}
		return null;
	}
}
