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
package org.olat.modules.forms.handler;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.forms.model.xml.SessionInformations.Obligation;
import org.olat.modules.forms.ui.SessionInformationsController;
import org.olat.modules.forms.ui.SessionInformationsEditorController;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.EvaluationFormResponseControllerElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: 14.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SessionInformationsHandler implements EvaluationFormElementHandler, SimpleAddPageElementHandler, CloneElementHandler {
	
	private final boolean restrictedEdit;
	
	public SessionInformationsHandler(boolean restrictedEdit) {
		this.restrictedEdit = restrictedEdit;
	}

	@Override
	public String getType() {
		return SessionInformations.TYPE;
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_eva_session_info";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.organisational;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element,
			PageElementRenderingHints options) {
		if(element instanceof SessionInformations) {
			SessionInformations sessionInformations = (SessionInformations) element;
			EvaluationFormResponseController ctrl = new SessionInformationsController(ureq, wControl, sessionInformations);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof SessionInformations) {
			SessionInformations sessionInformations = (SessionInformations) element;
			return new SessionInformationsEditorController(ureq, wControl, sessionInformations, restrictedEdit);
		}
		return null;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element, ExecutionIdentity executionIdentity) {
		if (element instanceof SessionInformations) {
			SessionInformations sessionInformations = (SessionInformations) element;
			EvaluationFormResponseController ctrl = new SessionInformationsController(ureq, wControl, sessionInformations, rootForm, executionIdentity);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		SessionInformations informations = new SessionInformations();
		informations.setId(UUID.randomUUID().toString());
		informations.setObligation(Obligation.optional);
		return informations;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof SessionInformations) {
			SessionInformations sessionInformations = (SessionInformations)element;
			SessionInformations clone = new SessionInformations();
			clone.setId(UUID.randomUUID().toString());
			if (sessionInformations.getInformationTypes() != null) {
				clone.setInformationTypes(new ArrayList<>(sessionInformations.getInformationTypes()));
			}
			clone.setObligation(sessionInformations.getObligation());
			return clone;
		}
		return null;
	}

}
