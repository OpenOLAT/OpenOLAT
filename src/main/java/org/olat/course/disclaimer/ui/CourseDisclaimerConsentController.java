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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.config.CourseConfig;
import org.olat.course.disclaimer.CourseDisclaimerConsent;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.course.disclaimer.event.CourseDisclaimerEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/* 
 * Date: 11 Mar 2020<br>
 * @author Alexander Boeckle
 */
public class CourseDisclaimerConsentController extends FormBasicController {	
	private static final String[] onKeys = {"on"};

	private MultipleSelectionElement disc1check1;
	private MultipleSelectionElement disc1check2;
	private MultipleSelectionElement disc2check1;
	private MultipleSelectionElement disc2check2;

	private FormSubmit submitBtn;
	private FormCancel cancelBtn;
	
	
	private RepositoryEntry repositoryEntry;
	private CourseConfig courseConfig;

	private DialogBoxController dbc;

	@Autowired
	private CourseDisclaimerManager disclaimerManager;

	public CourseDisclaimerConsentController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "course_disclaimer");

		this.repositoryEntry = repositoryEntry;
		courseConfig = CourseFactory.loadCourse(repositoryEntry.getOlatResource().getResourceableId()).getCourseEnvironment().getCourseConfig();

		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		disclaimerManager.acceptDisclaimer(repositoryEntry, getIdentity(), ureq.getUserSession().getRoles(), courseConfig.isDisclaimerEnabled(1), courseConfig.isDisclaimerEnabled(2));
		// notify course to continue
		fireEvent(ureq, CourseDisclaimerEvent.ACCEPTED);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		super.formCancelled(ureq);
		String message = "<div class='o_warning'><i class='o_icon o_icon_warn o_icon-fw'> </i> " + translate("course.disclaimer.cancel.message") + "</div>";
		dbc = activateYesNoDialog(ureq, translate("course.disclaimer.cancel.confirmation"), message, dbc);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == dbc) {
			if (DialogBoxUIFactory.isYesEvent(event) ){
				// mark DB that user denied by setting the checkboxes to false
				disclaimerManager.acceptDisclaimer(repositoryEntry, getIdentity(), ureq.getUserSession().getRoles(), false, false);
				// notify course to close the window
				fireEvent(ureq, CourseDisclaimerEvent.REJECTED);			
			}
			// else do nothing, user can still accept
			removeAsListenerAndDispose(dbc);
			dbc = null;
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if (courseConfig.isDisclaimerEnabled(1)) {
			disc1check1.clearError();
			if (!disc1check1.getSelectedKeys().contains(onKeys[0])) {
				allOk &= false;
				disc1check1.setErrorKey("course.disclaimer.error", null);
			}
			if (disc1check2 != null) {
				disc1check2.clearError();
				if (!disc1check2.getSelectedKeys().contains(onKeys[0])) {
					allOk &= false;
					disc1check2.setErrorKey("course.disclaimer.error", null);
				}
			} 
		}

		if (courseConfig.isDisclaimerEnabled(2)) {
			disc2check1.clearError();
			if (!disc2check1.getSelectedKeys().contains(onKeys[0])) {
				allOk &= false;
				disc2check1.setErrorKey("course.disclaimer.error", null);
			}
			if (disc2check2 != null) { 
				disc2check2.clearError();
				if(!disc2check2.getSelectedKeys().contains(onKeys[0])) {
					allOk &= false;
					disc2check2.setErrorKey("course.disclaimer.error", null);
				}
			}
		}

		return allOk;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (courseConfig.isDisclaimerEnabled(1)) {
			FormLayoutContainer disclaimer1 = FormLayoutContainer.createVerticalFormLayout("disc1", getTranslator());
			disclaimer1.setRootForm(mainForm);
			formLayout.add(disclaimer1);

			disc1check1 = uifactory.addCheckboxesVertical("course.disclaimer.1.check.1", "", disclaimer1, onKeys, new String[] {courseConfig.getDisclaimerLabel(1, 1)}, 1);
			disc1check1.setEscapeHtml(true);
			disc1check1.setMandatory(true);

			if (StringHelper.containsNonWhitespace(courseConfig.getDisclaimerLabel(1, 2))) {
				disc1check2 = uifactory.addCheckboxesVertical("course.disclaimer.1.check.2", "", disclaimer1, onKeys, new String[] {courseConfig.getDisclaimerLabel(1, 2)}, 1);
				disc1check2.setEscapeHtml(true);
				disc1check2.setMandatory(true);
			}

			flc.contextPut("disc1title", courseConfig.getDisclaimerTitel(1));
			flc.contextPut("disc1terms", courseConfig.getDisclaimerTerms(1));
		}

		if (courseConfig.isDisclaimerEnabled(2)) {
			FormLayoutContainer disclaimer2 = FormLayoutContainer.createVerticalFormLayout("disc2", getTranslator());
			disclaimer2.setRootForm(mainForm);
			formLayout.add(disclaimer2);

			disc2check1 = uifactory.addCheckboxesVertical("course.disclaimer.2.check.1", "", disclaimer2, onKeys, new String[] {courseConfig.getDisclaimerLabel(2, 1)}, 1);
			disc2check1.setEscapeHtml(true);
			disc2check1.setMandatory(true);

			if (StringHelper.containsNonWhitespace(courseConfig.getDisclaimerLabel(2, 2))) {
				disc2check2 = uifactory.addCheckboxesVertical("course.disclaimer.2.check.2", "", disclaimer2, onKeys, new String[] {courseConfig.getDisclaimerLabel(2, 2)}, 1);
				disc2check2.setEscapeHtml(true);
				disc2check2.setMandatory(true);
			}

			flc.contextPut("disc2title", courseConfig.getDisclaimerTitel(2));
			flc.contextPut("disc2terms", courseConfig.getDisclaimerTerms(2));
		}

		// Create submit and cancel buttons
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		buttonLayout.setElementCssClass("o_sel_disclaimer_buttons");

		submitBtn = uifactory.addFormSubmitButton("course.disclaimer.continue", buttonLayout);
		cancelBtn = uifactory.addFormCancelButton("course.disclaimer.cancel", buttonLayout, ureq, getWindowControl());
	}
	
	
	/**
	 * Call this method to setup the form in a read-only view for the given consent
	 * object
	 * 
	 * @param consent
	 */
	protected void initForConsentReview(CourseDisclaimerConsent consent) {
		if (courseConfig.isDisclaimerEnabled(1)) {
			disc1check1.selectAll();
			if (disc1check2 != null) {
				disc1check2.selectAll();
				
			}
		}
		if (courseConfig.isDisclaimerEnabled(2)) {
			disc2check1.selectAll();
			if (disc2check2 != null) {
				disc2check2.selectAll();
			}
		}			
		submitBtn.setVisible(false);
		cancelBtn.setVisible(false);
		this.flc.setEnabled(false);	
	}
}
