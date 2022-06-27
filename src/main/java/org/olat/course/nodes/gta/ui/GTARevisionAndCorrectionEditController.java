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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 4 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTARevisionAndCorrectionEditController extends FormBasicController {

	private TextElement minNumberOfDocsEl;
	private TextElement maxNumberOfDocsEl;
	
	private final ModuleConfiguration config;
	
	public GTARevisionAndCorrectionEditController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		this.config = config;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//configuration
		FormLayoutContainer configCont = FormLayoutContainer.createDefaultFormLayout("config", getTranslator());
		configCont.setRootForm(mainForm);
		configCont.setFormTitle(translate("editor.revisions.title"));
		configCont.setFormContextHelp("manual_user/task/Three_Steps_to_Your_Task/#configuration");
		formLayout.add(configCont);

		minNumberOfDocsEl = uifactory.addTextElement("min.documents", "min.documents", 5, "", configCont);
		maxNumberOfDocsEl = uifactory.addTextElement("max.documents", "max.documents", 5, "", configCont);
		updateDefaultNumbersOfDocuments();
		
		//save
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		configCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public void updateDefaultNumbersOfDocuments() {
		updateDefaultNumbersOfDocuments(minNumberOfDocsEl, GTACourseNode.GTASK_MIN_REVISED_DOCS, GTACourseNode.GTASK_MIN_SUBMITTED_DOCS);
		updateDefaultNumbersOfDocuments(maxNumberOfDocsEl, GTACourseNode.GTASK_MAX_REVISED_DOCS, GTACourseNode.GTASK_MAX_SUBMITTED_DOCS);
	}
	
	private void updateDefaultNumbersOfDocuments(TextElement numberEl, String configKey, String fallbackConfigKey) {
		int maxDocs = config.getIntegerSafe(configKey, -1);
		String maxVal = "";
		if(maxDocs == -1) {
			// !this only works because there is not another configuration in the controller
			maxDocs = config.getIntegerSafe(fallbackConfigKey, -1);
		}
		if(maxDocs > 0) {
			maxVal = Integer.toString(maxDocs);
		}
		numberEl.setValue(maxVal);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateNumberOfDocuments(minNumberOfDocsEl);
		allOk &= validateNumberOfDocuments(maxNumberOfDocsEl);
		
		if(allOk && StringHelper.isLong(minNumberOfDocsEl.getValue()) && StringHelper.isLong(maxNumberOfDocsEl.getValue())
				&& Long.parseLong(minNumberOfDocsEl.getValue()) > Long.parseLong(maxNumberOfDocsEl.getValue())) {
			maxNumberOfDocsEl.setErrorKey("error.max.smaller.than.min.documents", null);
			allOk &= false;	
		}
		
		return allOk;
	}
	
	private boolean validateNumberOfDocuments(TextElement numberEl) {
		boolean allOk = true;
		
		numberEl.clearError();
		String maxVal = numberEl.getValue();
		if(StringHelper.containsNonWhitespace(maxVal)) {
			try {
				int val = Integer.parseInt(maxVal);
				if(val <= 0 || val > 12) {
					numberEl.setErrorKey("error.number.format", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				//can happen
				allOk &= false;
				numberEl.setErrorKey("error.number.format", null);
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		setNumberOfDocuments(minNumberOfDocsEl, GTACourseNode.GTASK_MIN_REVISED_DOCS);
		setNumberOfDocuments(maxNumberOfDocsEl, GTACourseNode.GTASK_MAX_REVISED_DOCS);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void setNumberOfDocuments(TextElement numberEl, String configKey) {
		String number = numberEl.getValue();
		if(StringHelper.isLong(number)) {
			try {
				int val = Integer.parseInt(number);
				config.setIntValue(configKey, val);
			} catch (NumberFormatException e) {
				//can happen
			}
		} else {
			config.remove(configKey);
		}
	}
}
