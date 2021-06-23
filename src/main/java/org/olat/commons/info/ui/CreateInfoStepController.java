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

import org.olat.commons.info.InfoMessage;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CreateInfoStepController extends StepFormBasicController {
	
	private final StepsRunContext runContext;
	private final InfoEditFormController editForm;
	private final InfoMessage message;
	
	public CreateInfoStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form rootForm, InfoMessage message) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		
		this.runContext = runContext;
		this.message = message;
		editForm = new InfoEditFormController(ureq, wControl, rootForm, true, message);
		listenTo(editForm);
		
		initForm(ureq);
	}
	
	@Override
	public FormItem getStepFormItem() {
		return editForm.getInitialFormItem();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(editForm.getInitialFormItem());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == editForm) {
			if(event == Event.CHANGED_EVENT) {
				if (message != null && message.getKey() == null) {
					runContext.put(WizardConstants.PATH_TO_DELETE, editForm.getAttachmentPathToDelete());
				} 
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return editForm.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		runContext.put(WizardConstants.MSG, editForm.getInfoMessage());
		runContext.put(WizardConstants.PATH_TO_DELETE, editForm.getAttachmentPathToDelete());
		runContext.put(WizardConstants.ATTACHEMENTS, editForm.getAttachements());
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
