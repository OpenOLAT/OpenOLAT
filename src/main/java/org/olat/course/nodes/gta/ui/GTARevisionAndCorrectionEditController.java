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
		configCont.setFormContextHelp("Three Steps to Your Task#_task_configuration");
		formLayout.add(configCont);

		maxNumberOfDocsEl = uifactory.addTextElement("max.documents", "max.documents", 5, "", configCont);
		updateDefaultMaximumNumberOfDocuments();
		
		//save
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		configCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public void updateDefaultMaximumNumberOfDocuments() {
		int maxDocs = config.getIntegerSafe(GTACourseNode.GTASK_MAX_REVISED_DOCS, -1);
		String maxVal = "";
		if(maxDocs == -1) {
			// !this only works because there is not another configuration in the controller
			maxDocs = config.getIntegerSafe(GTACourseNode.GTASK_MAX_SUBMITTED_DOCS, -1);
		}
		if(maxDocs > 0) {
			maxVal = Integer.toString(maxDocs);
		}
		maxNumberOfDocsEl.setValue(maxVal);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		maxNumberOfDocsEl.clearError();
		String maxVal = maxNumberOfDocsEl.getValue();
		if(StringHelper.containsNonWhitespace(maxVal)) {
			try {
				int val = Integer.parseInt(maxVal);
				if(val <= 0 || val > 12) {
					maxNumberOfDocsEl.setErrorKey("error.number.format", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				//can happen
				allOk &= false;
				maxNumberOfDocsEl.setErrorKey("error.number.format", null);
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String maxVal = maxNumberOfDocsEl.getValue();
		if(StringHelper.isLong(maxVal)) {
			try {
				int val = Integer.parseInt(maxVal);
				config.setIntValue(GTACourseNode.GTASK_MAX_REVISED_DOCS, val);
			} catch (NumberFormatException e) {
				//can happen
			}
		} else {
			config.remove(GTACourseNode.GTASK_MAX_REVISED_DOCS);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}
}
