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
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeRef;
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
public class EditTaxonomyLevelController extends FormBasicController {
	
	private TextElement identifierEl, displayNameEl, sortOrderEl;
	private RichTextElement descriptionEl;
	private SingleSelection taxonomyLevelTypeEl;
	
	private TaxonomyLevel level;
	private TaxonomyLevel parentLevel;
	private Taxonomy taxonomy;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public EditTaxonomyLevelController(UserRequest ureq, WindowControl wControl, TaxonomyLevel level) {
		super(ureq, wControl);
		this.level = level;
		this.parentLevel = level.getParent();
		this.taxonomy = level.getTaxonomy();
		initForm(ureq);
	}
	
	public EditTaxonomyLevelController(UserRequest ureq, WindowControl wControl, TaxonomyLevel parentLevel, Taxonomy rootTaxonomy) {
		super(ureq, wControl);
		this.level = null;
		this.parentLevel = parentLevel;
		this.taxonomy = rootTaxonomy;
		initForm(ureq);
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return level;
	}
	
	private List<TaxonomyLevelType> getTypes() {
		List<TaxonomyLevelType> types = new ArrayList<>();
		if(level != null) {
			List<TaxonomyLevel> parentLine = taxonomyService.getTaxonomyLevelParentLine(level, taxonomy);
			for(int i=parentLine.size() - 1; i-->0; ) {
				TaxonomyLevel parent = parentLine.get(i);
				TaxonomyLevelType parentType = parent.getType();
				if(parentType != null) {
					Set<TaxonomyLevelTypeToType> typeToTypes = parentType.getAllowedTaxonomyLevelSubTypes();
					for(TaxonomyLevelTypeToType typeToType:typeToTypes) {
						if(typeToType != null) {
							types.add(typeToType.getAllowedSubTaxonomyLevelType());
						}
					}
					break;
				}
			}
		}
		if(types.isEmpty()) {
			types.addAll(taxonomyService.getTaxonomyLevelTypes(taxonomy));
		} else if(level != null && level.getType() != null) {
			if(!types.contains(level.getType())) {
				types.add(level.getType());
			}
		}
		return types;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String identifier = level == null ? "" : level.getIdentifier();
		identifierEl = uifactory.addTextElement("level.identifier", "level.identifier", 255, identifier, formLayout);
		identifierEl.setEnabled(!TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.identifier));
		identifierEl.setMandatory(true);

		String displayName = level == null ? "" : level.getDisplayName();
		displayNameEl = uifactory.addTextElement("level.displayname", "level.displayname", 255, displayName, formLayout);
		displayNameEl.setMandatory(true);
		displayNameEl.setEnabled(!TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.displayName));
		if(!StringHelper.containsNonWhitespace(displayName)) {
			displayNameEl.setFocus(true);
		}
		
		String sortOrder = level == null || level.getSortOrder() == null ? "" : level.getSortOrder().toString();
		sortOrderEl = uifactory.addTextElement("level.sort.order", "level.sort.order", 255, sortOrder, formLayout);
		sortOrderEl.setEnabled(!TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.displayName));

		List<TaxonomyLevelType> types = getTypes();
		String[] typeKeys = new String[types.size() + 1];
		String[] typeValues = new String[types.size() + 1];
		typeKeys[0] = "";
		typeValues[0] = "-";
		for(int i=types.size(); i-->0; ) {
			typeKeys[i+1] = types.get(i).getKey().toString();
			typeValues[i+1] = types.get(i).getDisplayName();
		}
		taxonomyLevelTypeEl = uifactory.addDropdownSingleselect("level.type", "level.type", formLayout, typeKeys, typeValues, null);
		taxonomyLevelTypeEl.setEnabled(!TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.type));
		boolean typeFound = false;
		if(level != null && level.getType() != null) {
			String selectedTypeKey = level.getType().getKey().toString();
			for(String typeKey:typeKeys) {
				if(typeKey.equals(selectedTypeKey)) {
					taxonomyLevelTypeEl.select(selectedTypeKey, true);
					typeFound = true;
					break;
				}
			}
		}
		if(!typeFound) {
			taxonomyLevelTypeEl.select(typeKeys[0], true);
		}
		
		String description = level == null ? "" : level.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataCompact("level.description", "level.description", description, 10, 60, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.setEnabled(!TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.description));
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
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
		
		sortOrderEl.clearError();
		if(StringHelper.containsNonWhitespace(sortOrderEl.getValue())) {
			try {
				Integer.parseInt(sortOrderEl.getValue());
			} catch (NumberFormatException e) {
				sortOrderEl.setErrorKey("error.sort.order.integer", null);
				allOk &= false;
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(level == null) {
			level = taxonomyService.createTaxonomyLevel(identifierEl.getValue(), displayNameEl.getValue(), descriptionEl.getValue(),
					null, null, parentLevel, taxonomy);
		} else {
			level = taxonomyService.getTaxonomyLevel(level);
			level.setIdentifier(identifierEl.getValue());
			level.setDisplayName(displayNameEl.getValue());
			level.setDescription(descriptionEl.getValue());
		}
		
		String selectedTypeKey = taxonomyLevelTypeEl.getSelectedKey();
		if(StringHelper.containsNonWhitespace(selectedTypeKey)) {
			TaxonomyLevelTypeRef typeRef = new TaxonomyLevelTypeRefImpl(new Long(selectedTypeKey));
			TaxonomyLevelType type = taxonomyService.getTaxonomyLevelType(typeRef);
			level.setType(type);
		} else {
			level.setType(null);
		}
		if(StringHelper.isLong(sortOrderEl.getValue())) {
			level.setSortOrder(new Integer(sortOrderEl.getValue()));
		} else {
			level.setSortOrder(null);
		}
		
		level = taxonomyService.updateTaxonomyLevel(level);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
