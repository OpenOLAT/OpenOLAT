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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package org.olat.course.condition.additionalconditions;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.run.RunMainController;
import org.olat.course.run.userview.UserCourseEnvironment;

import de.bps.course.nodes.CourseNodePasswordManager;
import de.bps.course.nodes.CourseNodePasswordManagerImpl;

/**
 * Initial Date:  17.09.2010 <br>
 * @author blaw
 * @author srosse, stephane.rosse@frentix.com
 */
public class PasswordVerificationController extends FormBasicController {
	
	private TextElement pwElement;
	
	private final PasswordCondition condition;
	private final UserCourseEnvironment userCourseEnv;

	protected PasswordVerificationController(UserRequest ureq, WindowControl wControl, PasswordCondition condition, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.condition = condition;
		this.userCourseEnv = userCourseEnv;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("password.title");
		setFormWarning("password.inputorder");
		formLayout.setElementCssClass("o_sel_course_password_form");

		pwElement = uifactory.addPasswordElement("password.field", "password.field", 255, "", formLayout);
		pwElement.setElementCssClass("o_sel_course_password");
		pwElement.setMandatory(true);
		pwElement.setDisplaySize(30);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", "password.submit", buttonLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new Event(RunMainController.REBUILD));
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean valid = false;
		pwElement.clearError();
		
		if(!StringHelper.containsNonWhitespace(pwElement.getValue())) {
			pwElement.setErrorKey("form.legende.mandatory", new String[] {});
		} else {
			valid = condition.evaluate(pwElement.getValue());
			if (valid) {
				CourseNodePasswordManager cnpm = CourseNodePasswordManagerImpl.getInstance();
				//used the identity of the user course environment for the preview of courses
				cnpm.updatePwd(userCourseEnv.getIdentityEnvironment(), condition.getNodeIdentifier(), condition.getCourseId(), pwElement.getValue());
			} else {
				pwElement.setErrorKey("password.incorrect", new String[0]);
			}
		}
		
		return valid;
	}
}