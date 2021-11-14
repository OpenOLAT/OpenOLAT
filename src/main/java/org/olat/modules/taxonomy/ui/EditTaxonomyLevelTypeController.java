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
package org.olat.modules.taxonomy.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelTypeRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditTaxonomyLevelTypeController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement identifierEl, displayNameEl, cssClassEl;
	private RichTextElement descriptionEl;
	private MultipleSelectionElement visibleEl;
	private MultipleSelectionElement allowedSubTypesEl;
	private MultipleSelectionElement allowedAsCompetenceEl;
	
	private TaxonomyLevelType levelType;
	private Taxonomy taxonomy;
	private List<TaxonomyLevelType> types;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public EditTaxonomyLevelTypeController(UserRequest ureq, WindowControl wControl,
			TaxonomyLevelType levelType, Taxonomy taxonomy) {
		super(ureq, wControl);
		this.levelType = levelType;
		this.taxonomy = taxonomy;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String identifier = levelType == null ? "" : levelType.getIdentifier();
		identifierEl = uifactory.addTextElement("level.identifier", "level.identifier", 255, identifier, formLayout);
		identifierEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.identifier));
		identifierEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(identifier)) {
			identifierEl.setFocus(true);
		}
		
		String displayName = levelType == null ? "" : levelType.getDisplayName();
		displayNameEl = uifactory.addTextElement("level.displayname", "level.displayname", 255, displayName, formLayout);
		displayNameEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.displayName));
		displayNameEl.setMandatory(true);

		String cssClass = levelType == null ? "" : levelType.getCssClass();
		cssClassEl = uifactory.addTextElement("level.type.cssClass", "level.type.cssClass", 255, cssClass, formLayout);
		cssClassEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.cssClass));
		
		visibleEl = uifactory.addCheckboxesHorizontal("level.visible", "level.visible", formLayout, onKeys, new String[] { "" });
		visibleEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.visibility));
		if(levelType != null && levelType.isVisible()) {
			visibleEl.select(onKeys[0], true);
		}
		
		allowedAsCompetenceEl = uifactory.addCheckboxesHorizontal("level.allow.competence", formLayout, onKeys, new String[] {translate("level.allow.competence.explanation")});
		if(levelType != null && levelType.isAllowedAsCompetence()) {
			allowedAsCompetenceEl.select(onKeys[0], true);
		}
		
		String description = levelType == null ? "" : levelType.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("level.description", "level.description", description, 10, 60,
				formLayout,  getWindowControl());
		descriptionEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.description));
		
		types = taxonomyService.getTaxonomyLevelTypes(taxonomy);
		types.remove(levelType);
		
		String[] subTypeKeys = new String[types.size()];
		String[] subTypeValues = new String[types.size()];
		for(int i=types.size(); i-->0; ) {
			subTypeKeys[i] = types.get(i).getKey().toString();
			subTypeValues[i] = types.get(i).getDisplayName();
		}
		allowedSubTypesEl = uifactory.addCheckboxesVertical("level.type.allowed.sub.types", formLayout, subTypeKeys, subTypeValues, 2);
		allowedSubTypesEl.setEnabled(!TaxonomyLevelTypeManagedFlag.isManaged(levelType, TaxonomyLevelTypeManagedFlag.subTypes));
		if(levelType != null) {
			Set<TaxonomyLevelTypeToType> typeToTypes = levelType.getAllowedTaxonomyLevelSubTypes();
			for(TaxonomyLevelTypeToType typeToType:typeToTypes) {
				String subTypeKey = typeToType.getAllowedSubTaxonomyLevelType().getKey().toString();
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
			displayNameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		identifierEl.clearError();
		if(!StringHelper.containsNonWhitespace(identifierEl.getValue())) {
			identifierEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(levelType == null) {
			levelType = taxonomyService.createTaxonomyLevelType(identifierEl.getValue(), displayNameEl.getValue(), descriptionEl.getValue(), null, allowedAsCompetenceEl.isAtLeastSelected(1), taxonomy);
		} else {
			levelType = taxonomyService.getTaxonomyLevelType(levelType);
			levelType.setIdentifier(identifierEl.getValue());
			levelType.setDisplayName(displayNameEl.getValue());
			levelType.setDescription(descriptionEl.getValue());
			levelType.setAllowedAsCompetence(allowedAsCompetenceEl.isAtLeastSelected(1));
		}
		
		levelType.setCssClass(cssClassEl.getValue());
		levelType.setVisible(visibleEl.isAtLeastSelected(1));
		
		Collection<String> selectedAllowedSubTypeKeys = allowedSubTypesEl.getSelectedKeys();
		List<TaxonomyLevelType> allowedSubTypes = new ArrayList<>();
		for(String selectedAllowedSubTypeKey:selectedAllowedSubTypeKeys) {
			allowedSubTypes.add(taxonomyService.getTaxonomyLevelType(new TaxonomyLevelTypeRefImpl(Long.valueOf(selectedAllowedSubTypeKey))));
		}
		levelType = taxonomyService.updateTaxonomyLevelType(levelType, allowedSubTypes);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
