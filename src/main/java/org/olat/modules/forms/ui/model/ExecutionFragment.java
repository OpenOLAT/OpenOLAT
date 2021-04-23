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
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.ui.ValidationMessage;
import org.olat.modules.ceditor.ui.model.PageFragment;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.rules.RulesEngine;

/**
 * 
 * Initial date: 17 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExecutionFragment extends PageFragment {
	
	private final EvaluationFormExecutionElement executionElement;
	
	public ExecutionFragment(String type, String componentName, EvaluationFormExecutionElement executionElement, PageElement element) {
		super(type, componentName, executionElement, element);
		this.executionElement = executionElement;
	}
	
	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return executionElement.validate(ureq, messages);
	}
	
	public void initRulesEngine(RulesEngine rulesEngine) {
		executionElement.initRulesEngine(rulesEngine);
	}
	
	public void setVisible(boolean visible) {
		executionElement.setVisible(visible);
	}
	
	public void setReadOnly(boolean readOnly) {
		executionElement.setReadOnly(readOnly);
	}
	
	public boolean hasResponse() {
		return executionElement.hasResponse();
	}
	
	public void initResponse(UserRequest ureq, EvaluationFormSession session, EvaluationFormResponses responses){
		executionElement.initResponse(ureq, session, responses);
	}
	
	public void save(UserRequest ureq, EvaluationFormSession session) {
		executionElement.saveResponse(ureq, session);
	}

	public Progress getProgress() {
		return executionElement.getProgress();
	}
	
	public void dispose() {
		executionElement.dispose();
	}
}
