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
import org.olat.modules.ceditor.ValidatingController;
import org.olat.modules.ceditor.ui.ValidationMessage;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.rules.RuleAware;
import org.olat.modules.forms.rules.RulesEngine;

/**
 * 
 * Initial date: 13.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormResponseControllerElement implements EvaluationFormExecutionElement {
	
	private final EvaluationFormResponseController controller;
	private boolean visible = true;
	
	public EvaluationFormResponseControllerElement(EvaluationFormResponseController controller) {
		this.controller = controller;
	}

	@Override
	public Component getComponent() {
		return controller.getInitialComponent();
	}

	@Override
	public boolean hasFormItem() {
		return visible;
	}

	@Override
	public FormItem getFormItem() {
		return controller.getInitialFormItem();
	}

	@Override
	public void initRulesEngine(RulesEngine rulesEngine) {
		if (controller instanceof RuleAware) {
			((RuleAware)controller).initRulesEngine(rulesEngine);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
		controller.getInitialFormItem().setVisible(visible);
		controller.setValidationEnabled(visible);
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		if(visible && controller instanceof ValidatingController) {
			return ((ValidatingController)controller).validate(ureq, messages);
		}
		return true;
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		controller.setReadOnly(readOnly);
	}

	@Override
	public boolean hasResponse() {
		return visible? controller.hasResponse(): true;
	}
	
	@Override
	public void initResponse(EvaluationFormSession session, EvaluationFormResponses responses) {
		controller.initResponse(null, session, responses);
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		if (visible) {
			controller.saveResponse(ureq, session);
		} else {
			controller.deleteResponse(session);
		}
	}

	@Override
	public Progress getProgress() {
		return visible? controller.getProgress(): Progress.none();
	}

	@Override
	public void dispose() {
		controller.dispose();
	}

}
