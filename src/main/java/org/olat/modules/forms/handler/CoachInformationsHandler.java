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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.forms.CoachCandidates.Role;
import org.olat.modules.forms.model.xml.CoachInformations;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;
import org.olat.modules.forms.model.xml.SessionInformations.Obligation;
import org.olat.modules.forms.ui.CoachInfoInspectorController;
import org.olat.modules.forms.ui.CoachInformationsController;
import org.olat.modules.forms.ui.SessionInformationsEditorController;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormResponseControllerElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: Dec 19, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CoachInformationsHandler implements EvaluationFormElementHandler, SimpleAddPageElementHandler, CloneElementHandler {
	
	private final boolean restrictedEdit;
	
	public CoachInformationsHandler(boolean restrictedEdit) {
		this.restrictedEdit = restrictedEdit;
	}

	@Override
	public String getType() {
		return CoachInformations.TYPE;
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_coach";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.organisational;
	}

	@Override
	public int getSortOrder() {
		return 11;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element,
			RenderingHints options) {
		if (element instanceof CoachInformations coachInformations) {
			CoachInformationsController ctrl = new CoachInformationsController(ureq, wControl, coachInformations);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof CoachInformations coachInformations) {
			return new SessionInformationsEditorController(ureq, wControl, coachInformations,
					restrictedEdit, CoachInformations.AVAILABLE_TYPES);
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof CoachInformations coachInformations) {
			return new CoachInfoInspectorController(ureq, wControl, coachInformations, restrictedEdit);
		}
		return null;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element, ExecutionIdentity executionIdentity) {
		if (element instanceof CoachInformations coachInformations) {
			CoachInformationsController ctrl = new CoachInformationsController(ureq, wControl, coachInformations, rootForm);
			return new EvaluationFormResponseControllerElement(ctrl);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		CoachInformations informations = new CoachInformations();
		informations.setId(UUID.randomUUID().toString());
		List<InformationType> informationTypes = new ArrayList<>(2);
		informationTypes.add(InformationType.USER_FIRSTNAME);
		informationTypes.add(InformationType.USER_LASTNAME);
		informations.setInformationTypes(informationTypes);
		List<Role> roles = new ArrayList<>(1);
		roles.add(Role.coach);
		informations.setRoles(roles);
		informations.setObligation(Obligation.optional);
		return informations;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof CoachInformations coachInformations) {
			CoachInformations clone = new CoachInformations();
			clone.setId(UUID.randomUUID().toString());
			if (coachInformations.getInformationTypes() != null) {
				clone.setInformationTypes(new ArrayList<>(coachInformations.getInformationTypes()));
			}
			if (coachInformations.getRoles() != null) {
				clone.setRoles(new ArrayList<>(coachInformations.getRoles()));
			}
			clone.setObligation(coachInformations.getObligation());
			clone.setLayoutSettings(BlockLayoutSettings.clone(coachInformations.getLayoutSettings()));
			clone.setAlertBoxSettings(AlertBoxSettings.clone(coachInformations.getAlertBoxSettings()));
			return clone;
		}
		return null;
	}

}
