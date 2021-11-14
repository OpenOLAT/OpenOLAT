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
package org.olat.course.disclaimer.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/* 
 * Date: 1 Apr 2020<br>
 * @author Alexander Boeckle
 */
public class CourseDisclaimerUpdateConfirmController extends FormBasicController {
	
	private FormLink deleteButton;
	private MultipleSelectionElement acknowledgeEl;
	
	private final Long numOfConsents;
	private final boolean deleteConfirm;
	private final boolean updatedConfig;
	
	@Autowired
	private CourseDisclaimerManager courseDisclaimerManager;
	
	public CourseDisclaimerUpdateConfirmController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry, boolean deleteConfirm) {
		super(ureq, wControl, "update_confirm");

		numOfConsents = courseDisclaimerManager.countConsents(repositoryEntry);
		this.deleteConfirm = deleteConfirm;
		this.updatedConfig = false;
		
		initForm(ureq);
	}
	
	public CourseDisclaimerUpdateConfirmController(UserRequest ureq, WindowControl wControl, boolean deleteConfirm, Long numOfConsents) {
		super(ureq, wControl, "update_confirm");
		
		this.numOfConsents = numOfConsents;
		this.deleteConfirm = deleteConfirm;
		this.updatedConfig = false;
		
		initForm(ureq);
	}
	
	public CourseDisclaimerUpdateConfirmController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry, boolean deleteConfirm, boolean updatedConfig) {
		super(ureq, wControl, "update_confirm");
		
		this.numOfConsents = courseDisclaimerManager.countConsents(repositoryEntry);
		this.deleteConfirm = deleteConfirm;
		this.updatedConfig = updatedConfig;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layout = (FormLayoutContainer)formLayout;
			
			String confirmMessage = "";
			
			if (updatedConfig) {
				if (deleteConfirm) {
					confirmMessage = translate("disclaimer.update.confirm.remove");
				} else {
					confirmMessage = translate("disclaimer.update.confirm.revoke");
				}
			}
			
			confirmMessage += translate("dialog.confirm" + (deleteConfirm ? ".remove" : ".revoke") + (numOfConsents != 1 ? ".plural" : ""), String.valueOf(numOfConsents));
			layout.contextPut("confirmMessage", confirmMessage);

			FormLayoutContainer layoutCont = FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
			formLayout.add("confirm", layoutCont);
			layoutCont.setRootForm(mainForm);
			
			String[] acknowledge = new String[] { translate("dialog.confrim." + (deleteConfirm ? "remove" : "revoke") + ".acknolege") };
			acknowledgeEl = uifactory.addCheckboxesHorizontal("dialog.confirm", "dialog.confirm", layoutCont, new String[]{ "" },  acknowledge);

			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			layoutCont.add(buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			deleteButton = uifactory.addFormLink("dialog." + (deleteConfirm ? "remove" : "revoke"), buttonsCont, Link.BUTTON);
			
			layout.setDirty(true);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		acknowledgeEl.clearError();
		if(!acknowledgeEl.isAtLeastSelected(1)) {
			acknowledgeEl.setErrorKey("dialog.confirm.error", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			if(validateFormLogic(ureq)) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}
}
