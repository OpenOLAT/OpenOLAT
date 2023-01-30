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
package org.olat.user.ui.organisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeManagedFlag;
import org.olat.basesecurity.OrganisationTypeToType;
import org.olat.basesecurity.model.OrganisationTypeRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditOrganisationTypeController extends FormBasicController {

	private TextElement identifierEl;
	private TextElement displayNameEl;
	private TextElement cssClassEl;
	private RichTextElement descriptionEl;
	private MultipleSelectionElement allowedSubTypesEl;
	
	private OrganisationType organisationType;
	
	@Autowired
	private OrganisationService organisationService;
	
	public EditOrganisationTypeController(UserRequest ureq, WindowControl wControl,
			OrganisationType type) {
		super(ureq, wControl);
		this.organisationType = type;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String identifier = organisationType == null ? "" : organisationType.getIdentifier();
		identifierEl = uifactory.addTextElement("type.identifier", "type.identifier", 255, identifier, formLayout);
		identifierEl.setEnabled(!OrganisationTypeManagedFlag.isManaged(organisationType, OrganisationTypeManagedFlag.identifier));
		identifierEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(identifier)) {
			identifierEl.setFocus(true);
		}
		
		String displayName = organisationType == null ? "" : organisationType.getDisplayName();
		displayNameEl = uifactory.addTextElement("type.displayname", "type.displayname", 255, displayName, formLayout);
		displayNameEl.setEnabled(!OrganisationTypeManagedFlag.isManaged(organisationType, OrganisationTypeManagedFlag.displayName));
		displayNameEl.setMandatory(true);

		String cssClass = organisationType == null ? "" : organisationType.getCssClass();
		cssClassEl = uifactory.addTextElement("type.cssClass", "type.cssClass", 255, cssClass, formLayout);
		cssClassEl.setEnabled(!OrganisationTypeManagedFlag.isManaged(organisationType, OrganisationTypeManagedFlag.cssClass));
		
		String description = organisationType == null ? "" : organisationType.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("type.description", "type.description", description, 10, 60,
				formLayout,  getWindowControl());
		descriptionEl.setEnabled(!OrganisationTypeManagedFlag.isManaged(organisationType, OrganisationTypeManagedFlag.description));
		
		List<OrganisationType> types = organisationService.getOrganisationTypes();
		types.remove(organisationType);
		
		String[] subTypeKeys = new String[types.size()];
		String[] subTypeValues = new String[types.size()];
		for(int i=types.size(); i-->0; ) {
			subTypeKeys[i] = types.get(i).getKey().toString();
			subTypeValues[i] = types.get(i).getDisplayName();
		}
		allowedSubTypesEl = uifactory.addCheckboxesVertical("type.allowed.sub.types", formLayout, subTypeKeys, subTypeValues, 2);
		allowedSubTypesEl.setEnabled(!OrganisationTypeManagedFlag.isManaged(organisationType, OrganisationTypeManagedFlag.subTypes));
		if(organisationType != null) {
			Set<OrganisationTypeToType> typeToTypes = organisationType.getAllowedSubTypes();
			for(OrganisationTypeToType typeToType:typeToTypes) {
				String subTypeKey = typeToType.getAllowedSubOrganisationType().getKey().toString();
				allowedSubTypesEl.select(subTypeKey, true);
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		displayNameEl.clearError();
		if(!StringHelper.containsNonWhitespace(displayNameEl.getValue())) {
			displayNameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		identifierEl.clearError();
		if(!StringHelper.containsNonWhitespace(identifierEl.getValue())) {
			identifierEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(organisationType == null) {
			organisationType = organisationService.createOrganisationType(displayNameEl.getValue(), identifierEl.getValue(), descriptionEl.getValue());
		} else {
			organisationType = organisationService.getOrganisationType(organisationType);
			organisationType.setIdentifier(identifierEl.getValue());
			organisationType.setDisplayName(displayNameEl.getValue());
			organisationType.setDescription(descriptionEl.getValue());
		}
		
		organisationType.setCssClass(cssClassEl.getValue());
		
		Collection<String> selectedAllowedSubTypeKeys = allowedSubTypesEl.getSelectedKeys();
		List<OrganisationType> allowedSubTypes = new ArrayList<>();
		for(String selectedAllowedSubTypeKey:selectedAllowedSubTypeKeys) {
			allowedSubTypes.add(organisationService.getOrganisationType(new OrganisationTypeRefImpl(Long.valueOf(selectedAllowedSubTypeKey))));
		}
		organisationType = organisationService.updateOrganisationType(organisationType, allowedSubTypes);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
