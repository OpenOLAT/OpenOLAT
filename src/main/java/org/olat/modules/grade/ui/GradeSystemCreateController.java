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
package org.olat.modules.grade.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.Rounding;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeSystemCreateController extends FormBasicController {
	
	private TextElement identifierEl;

	private GradeSystem gradeSystem;
	
	@Autowired
	private GradeService gradeService;

	public GradeSystemCreateController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		identifierEl = uifactory.addTextElement("grade.system.identifier", 128, null, formLayout);
		identifierEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public GradeSystem getGradeSystem() {
		return gradeSystem;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (identifierEl != null) {
			identifierEl.clearError();
			String identifier = identifierEl.getValue();
			if (!StringHelper.containsNonWhitespace(identifier)) {
				identifierEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if (!GradeUIFactory.validateIdentifierChars(identifier)) {
				identifierEl.setErrorKey("error.identifier.invalid.chars");
				allOk &= false;
			} else if (!gradeService.isGradeServiceIdentifierAvailable(identifier)) {
				identifierEl.setErrorKey("error.identifier.not.available");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		gradeSystem = gradeService.createGradeSystem(identifierEl.getValue(), GradeSystemType.numeric);
		// Init the mandatory values
		gradeSystem.setResolution(NumericResolution.whole);
		gradeSystem.setRounding(Rounding.nearest);
		gradeSystem.setBestGrade(Integer.valueOf(10));
		gradeSystem.setLowestGrade(Integer.valueOf(1));
		gradeSystem = gradeService.updateGradeSystem(gradeSystem);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
