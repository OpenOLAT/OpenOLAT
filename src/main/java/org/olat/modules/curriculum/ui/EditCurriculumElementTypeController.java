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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
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
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.olat.modules.curriculum.ui.component.CurriculumElementTypeComparator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCurriculumElementTypeController extends FormBasicController {
	
	private static final String LECTURES = "lectures";
	private static final String CALENDAR = "calendar";
	private static final String LEARNING_PROGRESS = "learningprogress";
	private static final String COMPOSITE = "composite";
	private static final String ROOT = "root";
	private static final String UNLIMITED = "-1";
	
	private TextElement cssClassEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private FormToggle withContentEl;
	private FormToggle compositeTypeEl;
	private RichTextElement descriptionEl;
	private MultipleSelectionElement allowedSubTypesEl;
	private MultipleSelectionElement featuresEnabledEl;
	private MultipleSelectionElement allowedAsRootEl;
	private SingleSelection maxRepositoryEntryRelationsEl;
	
	private CurriculumElementType curriculumElementType;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public EditCurriculumElementTypeController(UserRequest ureq, WindowControl wControl, CurriculumElementType curriculumElementType) {
		super(ureq, wControl);
		this.curriculumElementType = curriculumElementType;
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String displayName = curriculumElementType == null ? "" : curriculumElementType.getDisplayName();
		displayNameEl = uifactory.addTextElement("type.displayname", "type.displayname", 255, displayName, formLayout);
		displayNameEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.displayName));
		displayNameEl.setMandatory(true);
		if(displayNameEl.isEnabled() && !StringHelper.containsNonWhitespace(displayName)) {
			displayNameEl.setFocus(true);
		}
		
		String identifier = curriculumElementType == null ? "" : curriculumElementType.getIdentifier();
		identifierEl = uifactory.addTextElement("type.identifier", "type.identifier", 255, identifier, formLayout);
		identifierEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.identifier));
		identifierEl.setMandatory(true);

		String cssClass = curriculumElementType == null ? "" : curriculumElementType.getCssClass();
		cssClassEl = uifactory.addTextElement("type.cssClass", "type.cssClass", 255, cssClass, formLayout);
		cssClassEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.cssClass));
		
		String description = curriculumElementType == null ? "" : curriculumElementType.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("type.description", "type.description", description, 10, 60,
				formLayout,  getWindowControl());
		descriptionEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.description));

		SelectionValues featuresPK = new SelectionValues();
		featuresPK.add(SelectionValues.entry(LECTURES, translate("type.lectures.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.lectures)));
		featuresPK.add(SelectionValues.entry(CALENDAR, translate("type.calendars.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.calendars)));
		featuresPK.add(SelectionValues.entry(LEARNING_PROGRESS, translate("type.learning.progress.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.learningProgress)));

		featuresEnabledEl = uifactory.addCheckboxesVertical("type.features.enabled", formLayout, featuresPK.keys(), featuresPK.values(), 1);
		featuresEnabledEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.calendars));
		CurriculumLectures lecturesEnabled =  curriculumElementType == null ? null : curriculumElementType.getLectures();
		featuresEnabledEl.select(LECTURES, lecturesEnabled == CurriculumLectures.enabled);
		CurriculumCalendars calendarsEnabled =  curriculumElementType == null ? null : curriculumElementType.getCalendars();
		featuresEnabledEl.select(CALENDAR, calendarsEnabled == CurriculumCalendars.enabled);
		CurriculumLearningProgress learningProgressEnabled =  curriculumElementType == null ? null : curriculumElementType.getLearningProgress();
		featuresEnabledEl.select(LEARNING_PROGRESS, learningProgressEnabled == CurriculumLearningProgress.enabled);
		
		int maxRelations = curriculumElementType == null ? 0 : curriculumElementType.getMaxRepositoryEntryRelations();
		
		// Max course references
		withContentEl = uifactory.addToggleButton("type.with.content", "type.with.content", translate("on"), translate("off"), formLayout);
		withContentEl.toggle(maxRelations != 0);
		withContentEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues maxRelationsPK = new SelectionValues();
		maxRelationsPK.add(SelectionValues.entry("1", translate("type.max.repository.entry.relations.1")));
		maxRelationsPK.add(SelectionValues.entry(UNLIMITED, translate("type.max.repository.entry.relations.unlimited")));
		maxRepositoryEntryRelationsEl = uifactory.addRadiosHorizontal("type.max.repository.entry.relations", null, formLayout,
				maxRelationsPK.keys(), maxRelationsPK.values());
		maxRepositoryEntryRelationsEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.maxEntryRelations));
		if(maxRelations == 0) {
			maxRepositoryEntryRelationsEl.setVisible(false);
		} else if(maxRelations == -1) {
			maxRepositoryEntryRelationsEl.select("-1", true);
		} else {
			maxRepositoryEntryRelationsEl.select("1", true);
		}
		
		// Composite type : can contain multiple sub-elements
		SelectionValues compositePK = new SelectionValues();
		compositePK.add(SelectionValues.entry(COMPOSITE, translate("type.composite.multiple")));
		compositeTypeEl = uifactory.addToggleButton("type.composite", "type.composite", translate("on"), translate("off"), formLayout);
		compositeTypeEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.composite));
		boolean singleElement = curriculumElementType != null && curriculumElementType.isSingleElement();
		compositeTypeEl.toggle(!singleElement);
		compositeTypeEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues rootPK = new SelectionValues();
		rootPK.add(SelectionValues.entry(ROOT, translate("type.allow.as.root.value")));
		allowedAsRootEl = uifactory.addCheckboxesHorizontal("type.allow.as.root", formLayout, rootPK.keys(), rootPK.values());
		allowedAsRootEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.allowAsRoot));
		boolean allowAsRoot = curriculumElementType == null ? true : curriculumElementType.isAllowedAsRootElement();
		allowedAsRootEl.select(ROOT, allowAsRoot);
		
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		if(types.size() > 1) {
			Collections.sort(types, new CurriculumElementTypeComparator(getLocale()));
		}
		
		types.remove(curriculumElementType);
		SelectionValues subTypePK = new SelectionValues();
		for(CurriculumElementType type:types) {
			subTypePK.add(SelectionValues.entry(type.getKey().toString(), StringHelper.escapeHtml(type.getDisplayName())));
		}
		allowedSubTypesEl = uifactory.addCheckboxesVertical("type.allowed.sub.types", formLayout, subTypePK.keys(), subTypePK.values(), 2);
		allowedSubTypesEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.subTypes));
		if(curriculumElementType != null) {
			Set<CurriculumElementTypeToType> typeToTypes = curriculumElementType.getAllowedSubTypes();
			for(CurriculumElementTypeToType typeToType:typeToTypes) {
				String subTypeKey = typeToType.getAllowedSubType().getKey().toString();
				allowedSubTypesEl.select(subTypeKey, true);
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void updateUI() {
		boolean multipleElements = compositeTypeEl.isOn();
		allowedSubTypesEl.setVisible(multipleElements);
		
		maxRepositoryEntryRelationsEl.setVisible(withContentEl.isOn());
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(compositeTypeEl == source || withContentEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(curriculumElementType == null) {
			curriculumElementType = curriculumService.createCurriculumElementType(identifierEl.getValue(), displayNameEl.getValue(),
					descriptionEl.getValue(), null);
		} else {
			curriculumElementType = curriculumService.getCurriculumElementType(curriculumElementType);
			curriculumElementType.setIdentifier(identifierEl.getValue());
			curriculumElementType.setDisplayName(displayNameEl.getValue());
			curriculumElementType.setDescription(descriptionEl.getValue());
		}
		curriculumElementType.setCssClass(cssClassEl.getValue());
		Collection<String> selectedFeatures = featuresEnabledEl.getSelectedKeys();
		if(selectedFeatures.contains(CALENDAR)) {
			curriculumElementType.setCalendars(CurriculumCalendars.enabled);
		} else {
			curriculumElementType.setCalendars(CurriculumCalendars.disabled);
		}
		if(selectedFeatures.contains(LECTURES)) {
			curriculumElementType.setLectures(CurriculumLectures.enabled);
		} else {
			curriculumElementType.setLectures(CurriculumLectures.disabled);
		}
		if(selectedFeatures.contains(LEARNING_PROGRESS)) {
			curriculumElementType.setLearningProgress(CurriculumLearningProgress.enabled);
		} else {
			curriculumElementType.setLearningProgress(CurriculumLearningProgress.disabled);
		}
		
		curriculumElementType.setSingleElement(!compositeTypeEl.isOn());
		curriculumElementType.setAllowedAsRootElement(allowedAsRootEl.isAtLeastSelected(1));
		
		int maxRepositoryEntryRelations;
		if(withContentEl.isOn()) {
			if(maxRepositoryEntryRelationsEl.isOneSelected() && UNLIMITED.equals(maxRepositoryEntryRelationsEl.getSelectedKey())) {
				maxRepositoryEntryRelations = -1;
			} else {
				maxRepositoryEntryRelations = 1;
			}
		} else {
			maxRepositoryEntryRelations = 0;
		}
		curriculumElementType.setMaxRepositoryEntryRelations(maxRepositoryEntryRelations);

		Collection<String> selectedAllowedSubTypeKeys = allowedSubTypesEl.isVisible()
				? allowedSubTypesEl.getSelectedKeys()
				: List.of();
		List<CurriculumElementType> allowedSubTypes = new ArrayList<>();
		for(String selectedAllowedSubTypeKey:selectedAllowedSubTypeKeys) {
			allowedSubTypes.add(curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(Long.valueOf(selectedAllowedSubTypeKey))));
		}
		curriculumElementType = curriculumService.updateCurriculumElementType(curriculumElementType, allowedSubTypes);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
