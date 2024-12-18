/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelection;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Nov 26, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementMetadataController extends FormBasicController {
	
	private TextElement displayNameEl;
	private TextElement identifierEl;
	private SingleSelection curriculumElementTypeEl;
	private SingleSelection educationalTypeEl;
	private TaxonomyLevelSelection taxonomyLevelEl;

	private Curriculum curriculum;
	private CurriculumElement element;
	private CurriculumElement parentElement;
	private final CurriculumElementType preSelectedType;
	private final CurriculumSecurityCallback secCallback;
	private final boolean withCancel;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CatalogV2Module catalogModule;
	
	public EditCurriculumElementMetadataController(UserRequest ureq, WindowControl wControl,
			CurriculumElement parentElement, CurriculumElementType preSelectedType, Curriculum curriculum,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.curriculum = curriculum;
		this.parentElement = parentElement;
		this.secCallback = secCallback;
		this.preSelectedType = preSelectedType;
		this.withCancel = true;
		initForm(ureq);
	}
	
	public EditCurriculumElementMetadataController(UserRequest ureq, WindowControl wControl, CurriculumElement element,
			CurriculumElement parentElement, Curriculum curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.curriculum = curriculum;
		this.element = element;
		this.parentElement = parentElement;
		this.preSelectedType = null;
		this.secCallback = secCallback;
		this.withCancel = false;
		initForm(ureq);
	}
	
	public CurriculumElement getCurriculumElement() {
		return element;
	}

	public CurriculumElement getParentElement() {
		return parentElement;
	}

	public void setParentElement(CurriculumElement parentElement) {
		this.parentElement = parentElement;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("curriculum.element.metadata");
		
		if (element != null) {
			if (ureq.getUserSession().getRoles().isAdministrator()) {
				String key = element.getKey().toString();
				uifactory.addStaticTextElement("curriculum.element.key", key, formLayout);
			}
			if (CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.externalId)) {
				String externalId = element.getExternalId();
				uifactory.addStaticTextElement("curriculum.element.external.id", externalId, formLayout);
			}
		}
		
		boolean canEdit = element == null || secCallback.canEditCurriculumElement(element);
		
		String displayName = element == null ? "" : element.getDisplayName();
		displayNameEl = uifactory.addTextElement("displayName", "curriculum.element.displayName", 255, displayName, formLayout);
		displayNameEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.displayName) && canEdit);
		displayNameEl.setMandatory(true);
		if(displayNameEl.isEnabled() && !StringHelper.containsNonWhitespace(displayName)) {
			displayNameEl.setFocus(true);
		}
		
		String identifier = element == null ? "" : element.getIdentifier();
		identifierEl = uifactory.addTextElement("identifier", "curriculum.element.identifier", 64, identifier, formLayout);
		identifierEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.identifier) && canEdit);
		identifierEl.setMandatory(true);
		
		// Element type
		List<CurriculumElementType> types = getTypes();
		SelectionValues typePK = new SelectionValues();
		for(CurriculumElementType type:types) {
			typePK.add(SelectionValues.entry(type.getKey().toString(), StringHelper.escapeHtml(type.getDisplayName())));
		}
		curriculumElementTypeEl = uifactory.addDropdownSingleselect("type", "curriculum.element.type", formLayout, typePK.keys(), typePK.values());
		curriculumElementTypeEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.type) && canEdit);
		curriculumElementTypeEl.addActionListener(FormEvent.ONCHANGE);
		curriculumElementTypeEl.setMandatory(true);
		boolean typeFound = false;
		CurriculumElementType elementType = element == null ? preSelectedType : element.getType();
		if(elementType != null) {
			String selectedTypeKey = elementType.getKey().toString();
			if(typePK.containsKey(selectedTypeKey)) {
				curriculumElementTypeEl.select(selectedTypeKey, true);
				typeFound = true;
			}
		}
		if(!typeFound) {
			for(CurriculumElementType type:types) {
				if(CurriculumService.DEFAULT_CURRICULUM_ELEMENT_TYPE.equals(type.getExternalId())) {
					curriculumElementTypeEl.select(type.getKey().toString(), true);
				}
			}
		}
		
		// Implementation format
		if (parentElement == null) {
			SelectionValues educationalTypeKV = new SelectionValues();
			repositoryManager.getAllEducationalTypes()
					.forEach(type -> educationalTypeKV.add(SelectionValues.entry(type.getKey().toString(), translate(RepositoyUIFactory.getI18nKey(type)))));
			educationalTypeKV.sort(SelectionValues.VALUE_ASC);
			educationalTypeEl = uifactory.addDropdownSingleselect("cif.educational.type", formLayout, educationalTypeKV.keys(), educationalTypeKV.values());
			educationalTypeEl.enableNoneSelection();
			if (element != null) {
				RepositoryEntryEducationalType educationalType = element.getEducationalType();
				if (educationalType != null && Arrays.asList(educationalTypeEl.getKeys()).contains(educationalType.getKey().toString())) {
					educationalTypeEl.select(educationalType.getKey().toString(), true);
				}
			}
			educationalTypeEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.educationalType));
		}
		
		// Subjects
		List<TaxonomyRef> taxonomyRefs = curriculumModule.getTaxonomyRefs();
		if (taxonomyModule.isEnabled() && !taxonomyRefs.isEmpty()) {
			Set<TaxonomyLevel> allTaxonomieLevels = new HashSet<>(taxonomyService.getTaxonomyLevels(taxonomyRefs));
			List<TaxonomyLevel> taxonomyLevels = curriculumService.getTaxonomy(element);
			
			String labelI18nKey = catalogModule.isEnabled()? "cif.taxonomy.levels.catalog": "cif.taxonomy.levels";
			taxonomyLevelEl = uifactory.addTaxonomyLevelSelection("taxonomyLevel", labelI18nKey, formLayout,
					getWindowControl(), allTaxonomieLevels);
			taxonomyLevelEl.setDisplayNameHeader(translate(labelI18nKey));
			taxonomyLevelEl.setSelection(taxonomyLevels);
			if (catalogModule.isEnabled()) {
				taxonomyLevelEl.setHelpTextKey("cif.taxonomy.levels.help.catalog", null);
			}
		}
		
		if (canEdit || withCancel) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			if (canEdit) {
				formLayout.add(buttonsCont);
				uifactory.addFormSubmitButton("save", buttonsCont);
			}
			if (withCancel) {
				uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			}
		}
	}
	
	private List<CurriculumElementType> getTypes() {
		List<CurriculumElementType> types = curriculumService.getAllowedCurriculumElementType(parentElement, element);
		if(types.isEmpty()) {
			CurriculumElementType defaultType = curriculumService.getDefaultCurriculumElementType();
			if(defaultType != null) {
				types.add(defaultType);
			}
		}
		if(element != null && element.getType() != null && !types.contains(element.getType())) {
			types.add(element.getType());
		}
		return types;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= CurriculumHelper.validateTextElement(displayNameEl, true, 110);
		allOk &= CurriculumHelper.validateTextElement(identifierEl, true, 64);
		
		curriculumElementTypeEl.clearError();
		if(!curriculumElementTypeEl.isOneSelected()) {
			displayNameEl.setErrorKey("form.legende.mandatory");
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
		CurriculumElementType elementType = getSelectedType();
		RepositoryEntryEducationalType educationalType = getEducationalType();
		
		boolean create = element == null;
		if(create) {
			//create a new one
			element = curriculumService.createCurriculumElement(identifierEl.getValue(), displayNameEl.getValue(),
					null, null, null, parentElement, elementType, null, null, null, curriculum);
			if (educationalType != null) {
				element.setEducationalType(educationalType);
				element = curriculumService.updateCurriculumElement(element);
			}
		} else {
			element = curriculumService.getCurriculumElement(element);
			element.setIdentifier(identifierEl.getValue());
			element.setDisplayName(displayNameEl.getValue());
			element.setType(elementType);
			element.setEducationalType(educationalType);
			element = curriculumService.updateCurriculumElement(element);
		}
		
		if (taxonomyLevelEl != null) {
			Collection<Long> selectedLevelKeys = taxonomyLevelEl.getSelection()
					.stream()
					.map(TaxonomyLevelRef::getKey)
					.collect(Collectors.toList());
			List<Long> currentKeys = curriculumService.getTaxonomy(element)
					.stream()
					.map(TaxonomyLevel::getKey)
					.collect(Collectors.toList());
			
			// add newly selected keys
			Collection<Long> addKeys = new HashSet<>(selectedLevelKeys);
			addKeys.removeAll(currentKeys);

			// remove newly unselected keys
			Collection<Long> removeKeys = new HashSet<>(currentKeys);
			removeKeys.removeAll(selectedLevelKeys);
			
			List<TaxonomyLevel> addedLevels = taxonomyService.getTaxonomyLevelsByKeys(addKeys);
			List<TaxonomyLevel> removedLevels = taxonomyService.getTaxonomyLevelsByKeys(removeKeys);
			
			curriculumService.updateTaxonomyLevels(element, addedLevels, removedLevels);
		}
		
		if(create && element.getParent() != null) {
			dbInstance.commit();
			CurriculumElement rootElement = curriculumService.getImplementationOf(element);
			curriculumService.numberRootCurriculumElement(rootElement);
		}
		
		dbInstance.commitAndCloseSession(); // need to relaod properly the tree
		element = curriculumService.getCurriculumElement(element);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private CurriculumElementType getSelectedType() {
		String selectedTypeKey = curriculumElementTypeEl.getSelectedKey();
		if(StringHelper.containsNonWhitespace(selectedTypeKey)) {
			CurriculumElementTypeRef ref = new CurriculumElementTypeRefImpl(Long.valueOf(selectedTypeKey));
			return curriculumService.getCurriculumElementType(ref);
		}
		return null;
	}
	
	private RepositoryEntryEducationalType getEducationalType() {
		if (educationalTypeEl != null && educationalTypeEl.isOneSelected()) {
			return repositoryManager.getEducationalType(Long.valueOf(educationalTypeEl.getSelectedKey()));
		}
		return null;
	}

}
