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
package org.olat.modules.forms.ui.model;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.ui.ValidationMessage;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.rules.RulesEngine;

/**
 * 
 * Initial date: 16.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormComponentElement implements EvaluationFormExecutionElement {

	private final PageRunElement runElement;
	
	public EvaluationFormComponentElement(PageRunElement runElement) {
		this.runElement = runElement;
	}

	@Override
	public Component getComponent() {
		return runElement.getComponent();
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return runElement.validate(ureq, messages);
	}

	@Override
	public boolean hasFormItem() {
		return false;
	}

	@Override
	public FormItem getFormItem() {
		return null;
	}

	@Override
	public void initRulesEngine(RulesEngine rulesEngine) {
		//
	}

	@Override
	public void setVisible(boolean visible) {
		getComponent().setVisible(visible);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		getComponent().setEnabled(readOnly);
	}

	@Override
	public boolean hasResponse() {
		return true;
	}

	@Override
	public void initResponse(EvaluationFormSession session, EvaluationFormResponses responses) {
		//
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		//
	}

	@Override
	public Progress getProgress() {
		return Progress.none();
	}

	@Override
	public void dispose() {
		//
	}

}
