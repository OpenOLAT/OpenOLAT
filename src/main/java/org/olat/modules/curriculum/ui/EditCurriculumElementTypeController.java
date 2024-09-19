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
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
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
	
	private TextElement cssClassEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private RichTextElement descriptionEl;
	private MultipleSelectionElement allowedSubTypesEl;
	private MultipleSelectionElement featuresEnabledEl;
	private MultipleSelectionElement compositeTypeEl;
	private MultipleSelectionElement allowedAsRootEl;
	private SingleSelection maxRepositoryEntryRelationsEl;
	
	private CurriculumElementType curriculumElementType;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public EditCurriculumElementTypeController(UserRequest ureq, WindowControl wControl, CurriculumElementType curriculumElementType) {
		super(ureq, wControl);
		this.curriculumElementType = curriculumElementType;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String identifier = curriculumElementType == null ? "" : curriculumElementType.getIdentifier();
		identifierEl = uifactory.addTextElement("type.identifier", "type.identifier", 255, identifier, formLayout);
		identifierEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.identifier));
		identifierEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(identifier)) {
			identifierEl.setFocus(true);
		}
		
		String displayName = curriculumElementType == null ? "" : curriculumElementType.getDisplayName();
		displayNameEl = uifactory.addTextElement("type.displayname", "type.displayname", 255, displayName, formLayout);
		displayNameEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.displayName));
		displayNameEl.setMandatory(true);
		
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

		featuresEnabledEl = uifactory.addCheckboxesVertical("type.features.enabled", formLayout, featuresPK.keys(), featuresPK.values(), 2);
		featuresEnabledEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.calendars));
		CurriculumLectures lecturesEnabled =  curriculumElementType == null ? null : curriculumElementType.getLectures();
		featuresEnabledEl.select(LECTURES, lecturesEnabled == CurriculumLectures.enabled);
		CurriculumCalendars calendarsEnabled =  curriculumElementType == null ? null : curriculumElementType.getCalendars();
		featuresEnabledEl.select(CALENDAR, calendarsEnabled == CurriculumCalendars.enabled);
		CurriculumLearningProgress learningProgressEnabled =  curriculumElementType == null ? null : curriculumElementType.getLearningProgress();
		featuresEnabledEl.select(LEARNING_PROGRESS, learningProgressEnabled == CurriculumLearningProgress.enabled);
		
		// Max course references
		SelectionValues maxRelationsPK = new SelectionValues();
		maxRelationsPK.add(SelectionValues.entry("-1", translate("unlimited")));
		maxRelationsPK.add(SelectionValues.entry("0", "0"));
		maxRelationsPK.add(SelectionValues.entry("1", "1"));
		maxRepositoryEntryRelationsEl = uifactory.addDropdownSingleselect("type.max.repository.entry.relations", formLayout,
				maxRelationsPK.keys(), maxRelationsPK.values());
		maxRepositoryEntryRelationsEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.maxEntryRelations));
		int maxRelations = curriculumElementType == null ? -1 : curriculumElementType.getMaxRepositoryEntryRelations();
		String maxRelationsKey = Integer.toString(maxRelations);
		if(maxRelationsPK.containsKey(maxRelationsKey)) {
			maxRepositoryEntryRelationsEl.select(maxRelationsKey, true);
		} else {
			maxRepositoryEntryRelationsEl.select("-1", true);
		}
		
		// Composite type : can contain multiple sub-elements
		SelectionValues compositePK = new SelectionValues();
		compositePK.add(SelectionValues.entry(COMPOSITE, translate("type.composite.multiple")));
		compositeTypeEl = uifactory.addCheckboxesHorizontal("type.composite", formLayout, compositePK.keys(), compositePK.values());
		compositeTypeEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.composite));
		boolean singleElement = curriculumElementType == null ? false : curriculumElementType.isSingleElement();
		compositeTypeEl.select(COMPOSITE, !singleElement);
		
		SelectionValues rootPK = new SelectionValues();
		rootPK.add(SelectionValues.entry(ROOT, translate("type.allow.as.root.value")));
		allowedAsRootEl = uifactory.addCheckboxesHorizontal("type.allow.as.root", formLayout, rootPK.keys(), rootPK.values());
		allowedAsRootEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.allowAsRoot));
		boolean allowAsRoot = curriculumElementType == null ? true : curriculumElementType.isAllowedAsRootElement();
		allowedAsRootEl.select(ROOT, allowAsRoot);
		
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
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
		
		curriculumElementType.setSingleElement(!compositeTypeEl.isAtLeastSelected(1));
		curriculumElementType.setAllowedAsRootElement(allowedAsRootEl.isAtLeastSelected(1));
		if(maxRepositoryEntryRelationsEl.isOneSelected()) {
			curriculumElementType.setMaxRepositoryEntryRelations(Integer.parseInt(maxRepositoryEntryRelationsEl.getSelectedKey()));
		}

		Collection<String> selectedAllowedSubTypeKeys = allowedSubTypesEl.getSelectedKeys();
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
