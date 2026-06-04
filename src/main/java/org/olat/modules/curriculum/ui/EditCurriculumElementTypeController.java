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
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
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
	public static final String FOR_USE_AS_IMPL = "implementation";
	private static final String FOR_USE_AS_IMPL_OR_ELEM = "implementationOrElement";
	public static final String FOR_USE_AS_ELEM = "element";
	private static final String TYPE_OF_ELEM_STRUCTURAL = "structuralElement";
	private static final String TYPE_OF_ELEM_SINGLE_ELEMENT = "singleElement";
	private static final String CONTENT_NO_CONTENT = "noContent";
	private static final String CONTENT_SINGLE_COURSE = "singleCourse";
	private static final String CONTENT_COURSE_BUNDLE = "courseBundle";
	
	private TextElement cssClassEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private RichTextElement descriptionEl;
	private MultipleSelectionElement featuresEnabledEl;

	private SingleSelection forUseAsEl;
	private SingleSelection typeOfElementEl;
	private SingleSelection contentStructuralEl;
	private SingleSelection contentSingleEl;
	private SpacerElement dividerEl;
	private MultipleSelectionElement parentTypesEl;
	private MultipleSelectionElement childTypesEl;
	
	private CurriculumElementType curriculumElementType;
	private final String preselectedForUseAs;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public EditCurriculumElementTypeController(UserRequest ureq, WindowControl wControl, CurriculumElementType curriculumElementType) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.curriculumElementType = curriculumElementType;
		this.preselectedForUseAs = null;
		initForm(ureq);
		updateUI();
	}

	public EditCurriculumElementTypeController(UserRequest ureq, WindowControl wControl, String preselectedForUseAs) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.curriculumElementType = null;
		this.preselectedForUseAs = preselectedForUseAs;
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
		
		if(preselectedForUseAs != null) {
			String forUseAsLabel;
			if(FOR_USE_AS_IMPL.equals(preselectedForUseAs)) {
				forUseAsLabel = translate("table.type.for.use.as.implementation");
			} else if(FOR_USE_AS_ELEM.equals(preselectedForUseAs)) {
				forUseAsLabel = translate("table.type.for.use.as.element");
			} else {
				forUseAsLabel = translate("table.type.for.use.as.implementation.or.element");
			}
			uifactory.addStaticTextElement("type.for.use.as", "type.for.use.as", forUseAsLabel, configurationContainer);
		} else {
			SelectionValues forUseAsPK = new SelectionValues();
			forUseAsPK.add(SelectionValues.entry(FOR_USE_AS_IMPL, translate("table.type.for.use.as.implementation")));
			forUseAsPK.add(SelectionValues.entry(FOR_USE_AS_IMPL_OR_ELEM, translate("table.type.for.use.as.implementation.or.element")));
			forUseAsPK.add(SelectionValues.entry(FOR_USE_AS_ELEM, translate("table.type.for.use.as.element")));
			forUseAsEl = uifactory.addRadiosVertical("type.for.use.as", "type.for.use.as", configurationContainer,
					forUseAsPK.keys(), forUseAsPK.values());
			forUseAsEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.implOnly)
					&& !CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.allowAsRoot));
			forUseAsEl.addActionListener(FormEvent.ONCHANGE);
			if(curriculumElementType != null && curriculumElementType.isImplOnly()) {
				forUseAsEl.select(FOR_USE_AS_IMPL, true);
			} else if(curriculumElementType != null && !curriculumElementType.isAllowedAsRootElement()) {
				forUseAsEl.select(FOR_USE_AS_ELEM, true);
			} else {
				forUseAsEl.select(FOR_USE_AS_IMPL_OR_ELEM, true);
			}
		}

		SelectionValues typeOfElementKV = new SelectionValues();
		typeOfElementKV.add(SelectionValues.entry(TYPE_OF_ELEM_STRUCTURAL,
				translate("table.type.type.of.element.structural.element"),
				translate("table.type.type.of.element.structural.element.desc"),
				"o_icon o_icon_structure", null, true));
		typeOfElementKV.add(SelectionValues.entry(TYPE_OF_ELEM_SINGLE_ELEMENT,
				translate("table.type.type.of.element.single.element"),
				translate("table.type.type.of.element.single.element.desc"),
				"o_icon o_icon_single_element", null, true));
		typeOfElementEl = uifactory.addCardSingleSelectHorizontal("type.type.of.element", "table.type.header.type.typeOfElement",
				configurationContainer, typeOfElementKV);
		typeOfElementEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.composite));
		typeOfElementEl.addActionListener(FormEvent.ONCHANGE);
		if(curriculumElementType != null && !curriculumElementType.isSingleElement()) {
			typeOfElementEl.select(TYPE_OF_ELEM_STRUCTURAL, true);
		} else {
			typeOfElementEl.select(TYPE_OF_ELEM_SINGLE_ELEMENT, true);
		}

		boolean contentManaged = CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.maxEntryRelations);
		String initialContent;
		if(curriculumElementType != null && curriculumElementType.getMaxRepositoryEntryRelations() == -1) {
			initialContent = CONTENT_COURSE_BUNDLE;
		} else if(curriculumElementType != null && curriculumElementType.getMaxRepositoryEntryRelations() == 0) {
			initialContent = CONTENT_NO_CONTENT;
		} else {
			initialContent = CONTENT_SINGLE_COURSE;
		}

		SelectionValues contentStructuralKV = new SelectionValues();
		contentStructuralKV.add(SelectionValues.entry(CONTENT_NO_CONTENT,
				translate("table.type.content.no.content"),
				translate("table.type.content.no.content.desc"),
				"o_icon o_icon_ban", null, true));
		contentStructuralKV.add(SelectionValues.entry(CONTENT_SINGLE_COURSE,
				translate("table.type.content.single.course"),
				translate("table.type.content.single.course.desc"),
				"o_icon o_icon_courserun", null, true));
		contentStructuralKV.add(SelectionValues.entry(CONTENT_COURSE_BUNDLE,
				translate("table.type.content.course.bundle"),
				translate("table.type.content.course.bundle.desc"),
				"o_icon o_icon_course_bundle", null, true));
		contentStructuralEl = uifactory.addCardSingleSelectHorizontal("type.content.structural", "table.type.header.type.content",
				configurationContainer, contentStructuralKV);
		contentStructuralEl.setEnabled(!contentManaged);
		contentStructuralEl.select(initialContent, true);

		SelectionValues contentSingleKV = new SelectionValues();
		contentSingleKV.add(SelectionValues.entry(CONTENT_SINGLE_COURSE,
				translate("table.type.content.single.course"),
				translate("table.type.content.single.course.desc"),
				"o_icon o_icon_courserun", null, true));
		contentSingleKV.add(SelectionValues.entry(CONTENT_COURSE_BUNDLE,
				translate("table.type.content.course.bundle"),
				translate("table.type.content.course.bundle.desc"),
				"o_icon o_icon_course_bundle", null, true));
		contentSingleEl = uifactory.addCardSingleSelectHorizontal("type.content.single", "table.type.header.type.content",
				configurationContainer, contentSingleKV);
		contentSingleEl.setEnabled(!contentManaged);
		contentSingleEl.select(CONTENT_NO_CONTENT.equals(initialContent) ? CONTENT_SINGLE_COURSE : initialContent, true);

		dividerEl = uifactory.addSpacerElement("divider", configurationContainer, false);

		List<CurriculumElementType> elementTypes = curriculumService.getCurriculumElementTypes();
		elementTypes.sort(new CurriculumElementTypeComparator(getLocale()));
		elementTypes.remove(curriculumElementType);

		SelectionValues childTypesKV = new SelectionValues();
		for(CurriculumElementType type:elementTypes) {
			if(!type.isImplOnly()) {
				String label = StringHelper.escapeHtml(type.getDisplayName())
						+ "<span class=\"text-muted o_small\"> · " + StringHelper.escapeHtml(type.getIdentifier())
						+ "</span>";
				childTypesKV.add(SelectionValues.entry(type.getKey().toString(), label));
			}
		}

		childTypesEl = uifactory.addCheckboxesVertical("type.allowed.sub.types", configurationContainer,
				childTypesKV.keys(), childTypesKV.values(), 2);
		childTypesEl.setEscapeHtml(false);
		childTypesEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.subTypes));

		if(curriculumElementType != null) {
			Set<CurriculumElementTypeToType> typeToTypes = curriculumElementType.getAllowedSubTypes();
			for(CurriculumElementTypeToType typeToType:typeToTypes) {
				String subTypeKey = typeToType.getAllowedSubType().getKey().toString();
				childTypesEl.select(subTypeKey, true);
			}
		}

		SelectionValues parentTypesKV = new SelectionValues();
		for(CurriculumElementType type:elementTypes) {
			if(!type.isSingleElement()) {
				String label = StringHelper.escapeHtml(type.getDisplayName())
						+ "<span class=\"text-muted o_small\"> · " + StringHelper.escapeHtml(type.getIdentifier())
						+ "</span>";
				parentTypesKV.add(SelectionValues.entry(type.getKey().toString(), label));
			}
		}

		parentTypesEl = uifactory.addCheckboxesVertical("type.parent.types", configurationContainer,
				parentTypesKV.keys(), parentTypesKV.values(), 2);
		parentTypesEl.setEscapeHtml(false);

		List<CurriculumElementTypeToType> allRelations = curriculumElementType != null
				? curriculumService.getAllCurriculumElementTypeRelations()
				: List.of();
		Set<Long> currentParentTypeKeys = allRelations.stream()
				.filter(r -> r.getAllowedSubType().getKey().equals(curriculumElementType.getKey()))
				.map(r -> r.getType().getKey())
				.collect(Collectors.toSet());
		for(Long parentKey : currentParentTypeKeys) {
			parentTypesEl.select(parentKey.toString(), true);
		}

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		configurationContainer.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private String getSelectedForUseAs() {
		if(preselectedForUseAs != null) {
			return preselectedForUseAs;
		}
		if(forUseAsEl != null && forUseAsEl.isOneSelected()) { 
			return forUseAsEl.getSelectedKey();
		}
		return FOR_USE_AS_IMPL_OR_ELEM;
	}

	private void updateUI() {
		boolean structural = typeOfElementEl.isOneSelected()
				&& TYPE_OF_ELEM_STRUCTURAL.equals(typeOfElementEl.getSelectedKey());

		if(structural) {
			if(contentSingleEl.isOneSelected()) {
				contentStructuralEl.select(contentSingleEl.getSelectedKey(), true);
			}
		} else {
			String structuralContent = contentStructuralEl.isOneSelected() ? contentStructuralEl.getSelectedKey() : CONTENT_SINGLE_COURSE;
			contentSingleEl.select(CONTENT_NO_CONTENT.equals(structuralContent) ? CONTENT_SINGLE_COURSE : structuralContent, true);
		}
		contentStructuralEl.setVisible(structural);
		contentSingleEl.setVisible(!structural);

		boolean showParentElements = !FOR_USE_AS_IMPL.equals(getSelectedForUseAs());
		parentTypesEl.setVisible(showParentElements);
		childTypesEl.setVisible(structural);
		dividerEl.setVisible(showParentElements || structural);
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
		if(forUseAsEl == source || typeOfElementEl == source) {
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

		String selected = getSelectedForUseAs();
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

		if(typeOfElementEl.isOneSelected()) {
			curriculumElementType.setSingleElement(TYPE_OF_ELEM_SINGLE_ELEMENT.equals(typeOfElementEl.getSelectedKey()));
		}
		SingleSelection contentEl = contentStructuralEl.isVisible() ? contentStructuralEl : contentSingleEl;
		if(contentEl.isOneSelected()) {
			String content = contentEl.getSelectedKey();
			if(CONTENT_COURSE_BUNDLE.equals(content)) {
				curriculumElementType.setMaxRepositoryEntryRelations(-1);
			} else if(CONTENT_NO_CONTENT.equals(content)) {
				curriculumElementType.setMaxRepositoryEntryRelations(0);
			} else {
				curriculumElementType.setMaxRepositoryEntryRelations(1);
			}
		}

		Collection<String> selectedParentKeys = parentTypesEl.isVisible()
				? parentTypesEl.getSelectedKeys()
				: Set.of();
		List<CurriculumElementType> allTypes = curriculumService.getCurriculumElementTypes();
		allTypes.remove(curriculumElementType);
		for(CurriculumElementType parentCandidate : allTypes) {
			if(selectedParentKeys.contains(parentCandidate.getKey().toString())) {
				curriculumService.allowCurriculumElementSubType(parentCandidate, curriculumElementType);
			} else {
				curriculumService.disallowCurriculumElementSubType(parentCandidate, curriculumElementType);
			}
		}

		Collection<String> selectedAllowedSubTypeKeys = childTypesEl.isVisible()
				? childTypesEl.getSelectedKeys()
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
