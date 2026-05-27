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
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementTypeController extends FormBasicController {
	
	private static final String LECTURES = "lectures";
	private static final String CALENDAR = "calendar";
	private static final String LEARNING_PROGRESS = "learningprogress";
	private static final String COMPOSITE = "composite";
	private static final String UNLIMITED = "-1";
	private static final String FOR_USE_AS_IMPL = "implementation";
	private static final String FOR_USE_AS_IMPL_OR_ELEM = "implementationOrElement";
	private static final String FOR_USE_AS_ELEM = "element";
	private static final String TYPE_OF_ELEM_SINGLE_COURSE = "singleCourse";
	private static final String TYPE_OF_ELEM_COURSE_BUNDLE = "courseBundle";
	private static final String TYPE_OF_ELEM_STRUCTURAL = "structuralElement";
	private static final String SCOPE_STRUCT_ONLY = "structureOnly";
	private static final String SCOPE_STRUCT_SINGLE = "structureSingleCourse";
	private static final String SCOPE_STRUCT_BUNDLE = "structureCourseBundle";
	
	private TextElement cssClassEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private FormToggle withContentEl;
	private FormToggle compositeTypeEl;
	private RichTextElement descriptionEl;
	private SingleSelection forUseAsEl;
	private SingleSelection typeOfElementEl;
	private SingleSelection scopeOfUseEl;
	private MultipleSelectionElement allowedSubTypesEl;
	private MultipleSelectionElement featuresEnabledEl;
	private SingleSelection maxRepositoryEntryRelationsEl;
	
	private CurriculumElementType curriculumElementType;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public EditCurriculumElementTypeController(UserRequest ureq, WindowControl wControl, CurriculumElementType curriculumElementType) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.curriculumElementType = curriculumElementType;
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer commonContainer = FormLayoutContainer.createDefaultFormLayout("common", getTranslator());
		formLayout.add(commonContainer);

		String displayName = curriculumElementType == null ? "" : curriculumElementType.getDisplayName();
		displayNameEl = uifactory.addTextElement("type.displayname", "type.displayname", 255, displayName, commonContainer);
		displayNameEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.displayName));
		displayNameEl.setMandatory(true);
		if(displayNameEl.isEnabled() && !StringHelper.containsNonWhitespace(displayName)) {
			displayNameEl.setFocus(true);
		}
		
		String identifier = curriculumElementType == null ? "" : curriculumElementType.getIdentifier();
		identifierEl = uifactory.addTextElement("type.identifier", "type.identifier", 255, identifier, commonContainer);
		identifierEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.identifier));
		identifierEl.setMandatory(true);

		String cssClass = curriculumElementType == null ? "" : curriculumElementType.getCssClass();
		cssClassEl = uifactory.addTextElement("type.cssClass", "type.cssClass", 255, cssClass, commonContainer);
		cssClassEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.cssClass));
		
		String description = curriculumElementType == null ? "" : curriculumElementType.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringData("type.description", "type.description", 
				description, 10, -1, false, null, null,
				commonContainer, ureq.getUserSession(), getWindowControl());
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		descriptionEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.description));

		SelectionValues featuresPK = new SelectionValues();
		featuresPK.add(SelectionValues.entry(LECTURES, translate("type.lectures.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.lectures)));
		featuresPK.add(SelectionValues.entry(CALENDAR, translate("type.calendars.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.calendars)));
		featuresPK.add(SelectionValues.entry(LEARNING_PROGRESS, translate("type.learning.progress.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.learningProgress)));

		featuresEnabledEl = uifactory.addCheckboxesVertical("type.features.enabled", commonContainer, featuresPK.keys(), featuresPK.values(), 1);
		featuresEnabledEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.calendars));
		CurriculumLectures lecturesEnabled =  curriculumElementType == null ? null : curriculumElementType.getLectures();
		featuresEnabledEl.select(LECTURES, lecturesEnabled == CurriculumLectures.enabled);
		CurriculumCalendars calendarsEnabled =  curriculumElementType == null ? null : curriculumElementType.getCalendars();
		featuresEnabledEl.select(CALENDAR, calendarsEnabled == CurriculumCalendars.enabled);
		CurriculumLearningProgress learningProgressEnabled =  curriculumElementType == null ? null : curriculumElementType.getLearningProgress();
		featuresEnabledEl.select(LEARNING_PROGRESS, learningProgressEnabled == CurriculumLearningProgress.enabled);
		
		FormLayoutContainer configurationContainer = FormLayoutContainer.createDefaultFormLayout("configuration", getTranslator());
		formLayout.add(configurationContainer);
		configurationContainer.setFormTitle(translate("configuration"));
		
		SelectionValues forUseAsPK = new SelectionValues();
		forUseAsPK.add(SelectionValues.entry(FOR_USE_AS_IMPL, translate("table.type.for.use.as.implementation")));
		forUseAsPK.add(SelectionValues.entry(FOR_USE_AS_IMPL_OR_ELEM, translate("table.type.for.use.as.implementation.or.element")));
		forUseAsPK.add(SelectionValues.entry(FOR_USE_AS_ELEM, translate("table.type.for.use.as.element")));
		forUseAsEl = uifactory.addRadiosVertical("type.for.use.as", "type.for.use.as", configurationContainer,
				forUseAsPK.keys(), forUseAsPK.values());
		forUseAsEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.implOnly)
				&& !CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.allowAsRoot));
		if(curriculumElementType != null && curriculumElementType.isImplOnly()) {
			forUseAsEl.select(FOR_USE_AS_IMPL, true);
		} else if(curriculumElementType != null && !curriculumElementType.isAllowedAsRootElement()) {
			forUseAsEl.select(FOR_USE_AS_ELEM, true);
		} else {
			forUseAsEl.select(FOR_USE_AS_IMPL_OR_ELEM, true);
		}

		SelectionValues typeOfElementKV = new SelectionValues();
		typeOfElementKV.add(SelectionValues.entry(TYPE_OF_ELEM_SINGLE_COURSE,
				translate("table.type.type.of.element.single.course"),
				translate("table.type.type.of.element.single.course.desc"),
				"o_icon o_icon_courserun", null, true));
		typeOfElementKV.add(SelectionValues.entry(TYPE_OF_ELEM_COURSE_BUNDLE,
				translate("table.type.type.of.element.course.bundle"),
				translate("table.type.type.of.element.course.bundle.desc"),
				"o_icon o_icon_course_bundle", null, true));
		typeOfElementKV.add(SelectionValues.entry(TYPE_OF_ELEM_STRUCTURAL,
				translate("table.type.type.of.element.structural.element"),
				translate("table.type.type.of.element.structural.element.desc"),
				"o_icon o_icon_structure", null, true));
		typeOfElementEl = uifactory.addCardSingleSelectHorizontal("type.type.of.element", "table.type.header.type.typeOfElement",
				configurationContainer, typeOfElementKV);
		typeOfElementEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.composite)
				&& !CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.maxEntryRelations));
		typeOfElementEl.addActionListener(FormEvent.ONCHANGE);
		if(curriculumElementType != null && !curriculumElementType.isSingleElement()) {
			typeOfElementEl.select(TYPE_OF_ELEM_STRUCTURAL, true);
		} else if(curriculumElementType != null && curriculumElementType.getMaxRepositoryEntryRelations() == -1) {
			typeOfElementEl.select(TYPE_OF_ELEM_COURSE_BUNDLE, true);
		} else {
			typeOfElementEl.select(TYPE_OF_ELEM_SINGLE_COURSE, true);
		}

		String[] scopeKeys = { SCOPE_STRUCT_ONLY, SCOPE_STRUCT_SINGLE, SCOPE_STRUCT_BUNDLE };
		String[] scopeValues = {
				translate("table.type.type.of.application.structure.only"),
				translate("table.type.type.of.application.structure.single.course"),
				translate("table.type.type.of.application.structure.course.bundle")
		};
		scopeOfUseEl = uifactory.addDropdownSingleselect("type.scope.of.use", "type.scope.of.use",
				configurationContainer, scopeKeys, scopeValues, null);
		scopeOfUseEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.maxEntryRelations));
		if(curriculumElementType != null && !curriculumElementType.isSingleElement()) {
			int maxRelationsForScope = curriculumElementType.getMaxRepositoryEntryRelations();
			if(maxRelationsForScope == 1) {
				scopeOfUseEl.select(SCOPE_STRUCT_SINGLE, true);
			} else if(maxRelationsForScope == -1) {
				scopeOfUseEl.select(SCOPE_STRUCT_BUNDLE, true);
			} else {
				scopeOfUseEl.select(SCOPE_STRUCT_ONLY, true);
			}
		} else {
			scopeOfUseEl.select(SCOPE_STRUCT_ONLY, true);
		}

		int maxRelations = curriculumElementType == null ? 1 : curriculumElementType.getMaxRepositoryEntryRelations();
		
		// Max course references
		withContentEl = uifactory.addToggleButton("type.with.content", "type.with.content", translate("on"), translate("off"), configurationContainer);
		withContentEl.toggle(maxRelations != 0);
		withContentEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues maxRelationsPK = new SelectionValues();
		maxRelationsPK.add(SelectionValues.entry("1", translate("type.max.repository.entry.relations.1")));
		maxRelationsPK.add(SelectionValues.entry(UNLIMITED, translate("type.max.repository.entry.relations.unlimited")));
		maxRepositoryEntryRelationsEl = uifactory.addRadiosHorizontal("type.max.repository.entry.relations", null, configurationContainer,
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
		compositeTypeEl = uifactory.addToggleButton("type.composite", "type.composite", translate("on"), translate("off"), configurationContainer);
		compositeTypeEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.composite));
		boolean singleElement = curriculumElementType == null || curriculumElementType.isSingleElement();
		compositeTypeEl.toggle(!singleElement);
		compositeTypeEl.addActionListener(FormEvent.ONCHANGE);
		
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		if(types.size() > 1) {
			Collections.sort(types, new CurriculumElementTypeComparator(getLocale()));
		}
		
		types.remove(curriculumElementType);
		SelectionValues subTypePK = new SelectionValues();
		for(CurriculumElementType type:types) {
			subTypePK.add(SelectionValues.entry(type.getKey().toString(), type.getDisplayName()));
		}
		allowedSubTypesEl = uifactory.addCheckboxesVertical("type.allowed.sub.types", configurationContainer, subTypePK.keys(), subTypePK.values(), 2);
		allowedSubTypesEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.subTypes));
		if(curriculumElementType != null) {
			Set<CurriculumElementTypeToType> typeToTypes = curriculumElementType.getAllowedSubTypes();
			for(CurriculumElementTypeToType typeToType:typeToTypes) {
				String subTypeKey = typeToType.getAllowedSubType().getKey().toString();
				allowedSubTypesEl.select(subTypeKey, true);
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		configurationContainer.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void updateUI() {
		boolean structural = typeOfElementEl.isOneSelected()
				&& TYPE_OF_ELEM_STRUCTURAL.equals(typeOfElementEl.getSelectedKey());
		scopeOfUseEl.setVisible(structural);

		boolean multipleElements = compositeTypeEl.isOn();
		allowedSubTypesEl.setVisible(multipleElements);
		
		boolean withContent = withContentEl.isOn();
		maxRepositoryEntryRelationsEl.setVisible(withContent);
		if(withContent && !maxRepositoryEntryRelationsEl.isOneSelected()) {
			maxRepositoryEntryRelationsEl.select("1", true);
		}
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
		if(compositeTypeEl == source || withContentEl == source || typeOfElementEl == source) {
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
		
		if(forUseAsEl.isOneSelected()) {
			String selected = forUseAsEl.getSelectedKey();
			if(FOR_USE_AS_IMPL.equals(selected)) {
				curriculumElementType.setImplOnly(true);
				curriculumElementType.setAllowedAsRootElement(true);
			} else if(FOR_USE_AS_ELEM.equals(selected)) {
				curriculumElementType.setImplOnly(false);
				curriculumElementType.setAllowedAsRootElement(false);
			} else {
				curriculumElementType.setImplOnly(false);
				curriculumElementType.setAllowedAsRootElement(true);
			}
		}

		if(typeOfElementEl.isOneSelected()) {
			String selected = typeOfElementEl.getSelectedKey();
			if(TYPE_OF_ELEM_COURSE_BUNDLE.equals(selected)) {
				curriculumElementType.setSingleElement(true);
				curriculumElementType.setMaxRepositoryEntryRelations(-1);
			} else if(TYPE_OF_ELEM_STRUCTURAL.equals(selected)) {
				curriculumElementType.setSingleElement(false);
				if(scopeOfUseEl.isOneSelected()) {
					String scope = scopeOfUseEl.getSelectedKey();
					if(SCOPE_STRUCT_SINGLE.equals(scope)) {
						curriculumElementType.setMaxRepositoryEntryRelations(1);
					} else if(SCOPE_STRUCT_BUNDLE.equals(scope)) {
						curriculumElementType.setMaxRepositoryEntryRelations(-1);
					} else {
						curriculumElementType.setMaxRepositoryEntryRelations(0);
					}
				}
			} else {
				curriculumElementType.setSingleElement(true);
				curriculumElementType.setMaxRepositoryEntryRelations(1);
			}
		}

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
