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
package org.olat.ims.qti21.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.qti21.ui.editor.events.AssessmentTestEvent;

import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestPartEditorController extends ItemSessionControlController {
	
	private SingleSelection navigationModeEl;
	
	private final TestPart testPart;
	
	private static final String[] navigationKeys = new String[]{
			NavigationMode.LINEAR.name(), NavigationMode.NONLINEAR.name()
	};
		
	public AssessmentTestPartEditorController(UserRequest ureq, WindowControl wControl,
			TestPart testPart, boolean restrictedEdit, boolean editable) {
		super(ureq, wControl, testPart, restrictedEdit, editable);
		this.testPart = testPart;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("assessment.testpart.config");
		setFormContextHelp("Configure tests#_config_expert");
		if(!editable) {
			setFormWarning("warning.alien.assessment.test");
		}
		
		String[] navigationValues = new String[] {
				translate("form.testPart.navigationMode.linear"), translate("form.testPart.navigationMode.nonlinear")
		};
		String mode = testPart.getNavigationMode() == null ? NavigationMode.LINEAR.name() : testPart.getNavigationMode().name();
		navigationModeEl = uifactory.addRadiosHorizontal("navigationMode", "form.testPart.navigationMode", formLayout, navigationKeys, navigationValues);
		navigationModeEl.select(mode, true);
		navigationModeEl.setEnabled(!restrictedEdit);
		navigationModeEl.setHelpText(translate("form.testPart.navigationMode.hint"));
		
		super.initForm(formLayout, listener, ureq);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
	}
	
	@Override
	public void setFormTitle(String i18nKey) {
		super.setFormTitle(i18nKey);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		navigationModeEl.clearError();
		if(!navigationModeEl.isOneSelected()) {
			navigationModeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk &= super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		super.formOK(ureq);
		
		// navigation mode
		if(navigationModeEl.isOneSelected() && navigationModeEl.isSelected(0)) {
			testPart.setNavigationMode(NavigationMode.LINEAR);
		} else {
			testPart.setNavigationMode(NavigationMode.NONLINEAR);
		}

		fireEvent(ureq, AssessmentTestEvent.ASSESSMENT_TEST_CHANGED_EVENT);
	}
}
