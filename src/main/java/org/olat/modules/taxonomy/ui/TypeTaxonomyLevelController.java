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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelTypeRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TypeTaxonomyLevelController extends FormBasicController {
	
	private SingleSelection typeEl;
	
	private Taxonomy taxonomy;
	private final List<TaxonomyLevel> levels;
	private List<TaxonomyLevelType> availableTypes;

	@Autowired
	private TaxonomyService taxonomyService;
	
	public TypeTaxonomyLevelController(UserRequest ureq, WindowControl wControl, List<TaxonomyLevel> levels, Taxonomy taxonomy) {
		super(ureq, wControl, "type_taxonomy_levels");
		this.levels = levels;
		this.taxonomy = taxonomy;
		availableTypes = getAvailableTypes();
		initForm(ureq);
	}
	
	private List<TaxonomyLevelType> getAvailableTypes() {
		List<TaxonomyLevelType> allowedTypes = new ArrayList<>(taxonomyService.getTaxonomyLevelTypes(taxonomy));

		Set<TaxonomyLevelType> analyzedTypes = new HashSet<>();
		for(TaxonomyLevel level:levels) {
			TaxonomyLevel parentLevel = level.getParent();
			if(parentLevel != null && parentLevel.getType() != null && !analyzedTypes.contains(parentLevel.getType())) {
				
				TaxonomyLevelType parentType = parentLevel.getType();
				List<TaxonomyLevelType> allowedSubTypes = new ArrayList<>();
				Set<TaxonomyLevelTypeToType> typesToTypes = parentType.getAllowedTaxonomyLevelSubTypes();
				for(TaxonomyLevelTypeToType typeToType:typesToTypes) {
					allowedSubTypes.add(typeToType.getAllowedSubTaxonomyLevelType());
				}
				
				if(!allowedSubTypes.isEmpty()) {
					allowedTypes.retainAll(allowedSubTypes);
				}
			}
		}

		return allowedTypes;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(availableTypes.isEmpty()) {
			if(formLayout instanceof FormLayoutContainer) {
				FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
				layoutCont.contextPut("errorMsg", translate("error.found.no.allowed.sub.types"));
			}
			
			uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		} else {
			String[] theKeys = new String[availableTypes.size()];
			String[] theValues = new String[availableTypes.size()];
			for(int i=availableTypes.size(); i-->0; ) {
				TaxonomyLevelType type = availableTypes.get(i);
				theKeys[i] = type.getKey().toString();
				theValues[i] = type.getDisplayName();
			}
			typeEl = uifactory.addDropdownSingleselect("types", "level.types.to.assign", formLayout, theKeys, theValues, null);

			uifactory.addFormSubmitButton("assign.type", formLayout);
			uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		typeEl.clearError();
		if(!typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String selectedTypeKey = typeEl.getSelectedKey();
		TaxonomyLevelType selectedType = taxonomyService
				.getTaxonomyLevelType(new TaxonomyLevelTypeRefImpl(Long.valueOf(selectedTypeKey)));
		for(TaxonomyLevel level:levels) {
			level.setType(selectedType);
			level = taxonomyService.updateTaxonomyLevel(level);
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
