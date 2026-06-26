/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.mode;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SafeExamBrowserVersionsController extends FormBasicController {
	
	private FormToggle enforceMinimalVersionEl;
	private TextElement minimalVersionIosEl;
	private TextElement minimalVersionMacEl;
	private TextElement minimalVersionWinEl;
	
	@Autowired
	private AssessmentModule assessmentModule;
	
	public SafeExamBrowserVersionsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		enforceMinimalVersionEl = uifactory.addToggleButton("admin.enforce.seb.minimal.version", "admin.enforce.seb.minimal.version",
				translate("on"), translate("off"), formLayout);
		enforceMinimalVersionEl.toggle(assessmentModule.isSafeExamBrowserEnforceMinimalVersion());
		
		String versionWin = assessmentModule.getSafeExamBrowserMinimalVersionWin();
		minimalVersionWinEl = uifactory.addTextElement("admin.seb.minimal.version.win", 64, versionWin, formLayout);
		String versionMac = assessmentModule.getSafeExamBrowserMinimalVersionMac();
		minimalVersionMacEl = uifactory.addTextElement("admin.seb.minimal.version.mac", 64, versionMac, formLayout);
		String versionIos = assessmentModule.getSafeExamBrowserMinimalVersionIos();
		minimalVersionIosEl = uifactory.addTextElement("admin.seb.minimal.version.ios", 64, versionIos, formLayout);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		boolean enabled = enforceMinimalVersionEl.isOn();
		minimalVersionIosEl.setVisible(enabled);
		minimalVersionMacEl.setVisible(enabled);
		minimalVersionWinEl.setVisible(enabled);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enforceMinimalVersionEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enforceMinimalVersionEl.isOn();
		assessmentModule.setSafeExamBrowserEnforceMinimalVersion(enabled);
		if(enabled) {
			assessmentModule.setSafeExamBrowserMinimalVersionIos(minimalVersionIosEl.getValue());
			assessmentModule.setSafeExamBrowserMinimalVersionMac(minimalVersionMacEl.getValue());
			assessmentModule.setSafeExamBrowserMinimalVersionWin(minimalVersionWinEl.getValue());
		} else {
			assessmentModule.setSafeExamBrowserMinimalVersionIos("");
			assessmentModule.setSafeExamBrowserMinimalVersionMac("");
			assessmentModule.setSafeExamBrowserMinimalVersionWin("");
		}
	}
}
