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
package org.olat.modules.lecture.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.LectureService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditAbsenceCategoryController extends FormBasicController {

	private TextElement titleEl;
	private TextElement descriptionEl;
	
	private AbsenceCategory category;
	
	@Autowired
	private LectureService lectureService;
	
	public EditAbsenceCategoryController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}
	
	public EditAbsenceCategoryController(UserRequest ureq, WindowControl wControl, AbsenceCategory category) {
		super(ureq, wControl);
		this.category = category;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = category == null ? "" : category.getTitle();
		titleEl = uifactory.addTextElement("title", "absence.category.title", 128, title, formLayout);
		titleEl.setMandatory(true);
		
		String description = category == null ? "" : category.getDescription();
		descriptionEl = uifactory.addTextAreaElement("absence.category.description", 4, 72, description, formLayout);
		descriptionEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		descriptionEl.clearError();
		if(!StringHelper.containsNonWhitespace(descriptionEl.getValue())) {
			descriptionEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(category == null) {
			category = lectureService.createAbsenceCategory(titleEl.getValue(), descriptionEl.getValue());
		} else {
			category.setTitle(titleEl.getValue());
			category.setDescription(descriptionEl.getValue());
			category = lectureService.updateAbsenceCategory(category);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
