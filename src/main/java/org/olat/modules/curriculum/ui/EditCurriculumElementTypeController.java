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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
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
	private static final String CAN_BE_USED_AS_ELEMENT = "canBeUsedAsElement";
	private static final String SINGLE = "single";
	private static final String BUNDLE = "bundle";
	private static final String STRUCTURED = "structured";
	
	private TextElement cssClassEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private FormToggle withContentEl;
	private RichTextElement descriptionEl;
	private MultipleSelectionElement allowedSubElementsEl;
	private MultipleSelectionElement featuresEnabledEl;
	private SpacerElement mixedTypeSpacer;
	private MultipleSelectionElement mixedTypeEl;
	private SingleSelection maxRepositoryEntryRelationsEl;
	private SingleSelection cardEl;
	
	private CurriculumElementType curriculumElementType;
	private CurriculumElementType.Type type;
	private Collection<String> selectedKeys;
	
	@Autowired
	private CurriculumService curriculumService;

	public EditCurriculumElementTypeController(UserRequest ureq, WindowControl wControl, CurriculumElementType.Type type, 
											   CurriculumElementType curriculumElementType) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.type = type;
		this.curriculumElementType = curriculumElementType;
		guessType();
		initForm(ureq);
		updateUI();
	}

	private void guessType() {
		if (type != null) {
			return;
		}
		if (curriculumElementType == null) {
			type = CurriculumElementType.Type.element;
			return;
		}
		type = curriculumElementType.isAllowedAsRootElement() ? CurriculumElementType.Type.mixed : CurriculumElementType.Type.element;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initCommon(formLayout);
		initTypeSpecific(formLayout);
		initButtons(formLayout, ureq);
	}

	private void initCommon(FormItemContainer formLayout) {
		FormLayoutContainer commonCont = FormLayoutContainer.createDefaultFormLayout("common", getTranslator());
		formLayout.add(commonCont);

		String displayName = curriculumElementType == null ? "" : curriculumElementType.getDisplayName();
		displayNameEl = uifactory.addTextElement("type.displayname", "type.displayname", 255, displayName, commonCont);
		displayNameEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.displayName));
		displayNameEl.setMandatory(true);
		if(displayNameEl.isEnabled() && !StringHelper.containsNonWhitespace(displayName)) {
			displayNameEl.setFocus(true);
		}

		// UI label: "Ext. ref."
		String identifier = curriculumElementType == null ? "" : curriculumElementType.getIdentifier();
		identifierEl = uifactory.addTextElement("type.identifier", "type.identifier", 255, identifier, commonCont);
		identifierEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.identifier));
		identifierEl.setMandatory(true);

		String cssClass = curriculumElementType == null ? "" : curriculumElementType.getCssClass();
		cssClassEl = uifactory.addTextElement("type.cssClass", "type.cssClass", 255, cssClass, commonCont);
		cssClassEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.cssClass));

		String description = curriculumElementType == null ? "" : curriculumElementType.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("type.description", "type.description", description, 10, -1,
				commonCont,  getWindowControl());
		descriptionEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.description));
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);

		SelectionValues featuresPK = new SelectionValues();
		featuresPK.add(SelectionValues.entry(LECTURES, translate("type.lectures.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.lectures)));
		featuresPK.add(SelectionValues.entry(CALENDAR, translate("type.calendars.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.calendars)));
		featuresPK.add(SelectionValues.entry(LEARNING_PROGRESS, translate("type.learning.progress.enabled"), null, null, null,
				!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.learningProgress)));

		featuresEnabledEl = uifactory.addCheckboxesVertical("type.features.enabled", commonCont, featuresPK.keys(), featuresPK.values(), 1);
		featuresEnabledEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.calendars));
		CurriculumLectures lecturesEnabled =  curriculumElementType == null ? null : curriculumElementType.getLectures();
		featuresEnabledEl.select(LECTURES, lecturesEnabled == CurriculumLectures.enabled);
		CurriculumCalendars calendarsEnabled =  curriculumElementType == null ? null : curriculumElementType.getCalendars();
		featuresEnabledEl.select(CALENDAR, calendarsEnabled == CurriculumCalendars.enabled);
		CurriculumLearningProgress learningProgressEnabled =  curriculumElementType == null ? null : curriculumElementType.getLearningProgress();
		featuresEnabledEl.select(LEARNING_PROGRESS, learningProgressEnabled == CurriculumLearningProgress.enabled);
	}
	
	private void initTypeSpecific(FormItemContainer formLayout) {
		boolean isElementType = CurriculumElementType.Type.element.equals(type);
		FormLayoutContainer oldCont = FormLayoutContainer.createDefaultFormLayout("old", getTranslator());
		formLayout.add(oldCont);
		oldCont.setFormTitle(translate(isElementType ? 
				"type.element.settings" : "type.implementation.settings"));

		SelectionValues cardKV = new SelectionValues();
		cardKV.add(SelectionValues.entry(SINGLE,
				translate("type.single.course"), translate(isElementType ? "type.single.course.desc.element" : 
						"type.single.course.desc.implementation"),
				"o_icon o_icon_courserun", null, true));
		cardKV.add(SelectionValues.entry(BUNDLE,
				translate("type.course.bundle"), translate(isElementType ? "type.course.bundle.desc.element" : 
						"type.course.bundle.desc.implementation"),
				"o_icon o_icon_course_bundle", null, true));
		cardKV.add(SelectionValues.entry(STRUCTURED,
				translate("type.structured.implementation"), translate("type.structured.implementation.desc"),
				"o_icon o_icon_structure", null, true));
		cardEl = uifactory.addCardSingleSelectHorizontal("card", 
				"type.of.implementation", oldCont, cardKV);
		cardEl.addActionListener(FormEvent.ONCHANGE);
		cardEl.select(getInitialCardSelection(), true);
		cardEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.composite));

		int maxRelations = curriculumElementType == null ? 1 : curriculumElementType.getMaxRepositoryEntryRelations();

		withContentEl = uifactory.addToggleButton("type.with.content", "type.with.content", translate("on"), translate("off"), oldCont);
		withContentEl.toggle(maxRelations != 0);
		withContentEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues maxRelationsPK = new SelectionValues();
		maxRelationsPK.add(SelectionValues.entry(SINGLE, translate("type.single.course")));
		maxRelationsPK.add(SelectionValues.entry(BUNDLE, translate("type.course.bundle")));
		maxRepositoryEntryRelationsEl = uifactory.addRadiosHorizontal("type.max.repository.entry.relations", 
				"curriculum.element.options", oldCont, maxRelationsPK.keys(), maxRelationsPK.values());
		maxRepositoryEntryRelationsEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.maxEntryRelations));
		if(maxRelations == 0) {
			maxRepositoryEntryRelationsEl.setVisible(false);
		} else if(maxRelations == -1) {
			maxRepositoryEntryRelationsEl.select(BUNDLE, true);
		} else {
			maxRepositoryEntryRelationsEl.select(SINGLE, true);
		}

		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		if (types.size() > 1) {
			types.sort(new CurriculumElementTypeComparator(getLocale()));
		}

		types.remove(curriculumElementType);
		SelectionValues subTypePK = new SelectionValues();
		for(CurriculumElementType type:types) {
			subTypePK.add(SelectionValues.entry(type.getKey().toString(), type.getDisplayName()));
		}
		allowedSubElementsEl = uifactory.addCheckboxesVertical("type.allowed.sub.elements", oldCont, subTypePK.keys(), subTypePK.values(), 2);
		allowedSubElementsEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.subTypes));
		if(curriculumElementType != null) {
			Set<CurriculumElementTypeToType> typeToTypes = curriculumElementType.getAllowedSubTypes();
			for(CurriculumElementTypeToType typeToType:typeToTypes) {
				String subTypeKey = typeToType.getAllowedSubType().getKey().toString();
				allowedSubElementsEl.select(subTypeKey, true);
			}
		}

		mixedTypeSpacer = uifactory.addSpacerElement("mixed.type.spacer", oldCont, false);
		
		SelectionValues mixedTypeKV = new SelectionValues();
		mixedTypeKV.add(SelectionValues.entry(CAN_BE_USED_AS_ELEMENT, translate("type.can.be.used.as.element")));
		mixedTypeEl = uifactory.addCheckboxesHorizontal("type.mixed", oldCont, mixedTypeKV.keys(), mixedTypeKV.values());
		mixedTypeEl.setHelpText(translate("type.mixed.help"));
		mixedTypeEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.allowAsRoot));
		boolean isAllowedAsRoot = curriculumElementType != null && !isElementType && curriculumElementType.isAllowedAsRootElement();
		mixedTypeEl.select(CAN_BE_USED_AS_ELEMENT, isAllowedAsRoot);
	}

	private String getInitialCardSelection() {
		int maxRelations = curriculumElementType == null ? 1 : curriculumElementType.getMaxRepositoryEntryRelations();
		boolean isSingleElement = curriculumElementType == null || curriculumElementType.isSingleElement();
		if (!isSingleElement) {
			return STRUCTURED;
		}
		if (maxRelations == 1) {
			return SINGLE;			
		}
		if (maxRelations == -1) {
			return BUNDLE;
		}
		return STRUCTURED;
	}

	private void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		FormLayoutContainer footerCont = FormLayoutContainer.createDefaultFormLayout("footer", getTranslator());
		formLayout.add("footer", footerCont);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		footerCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void updateUI() {
		boolean isStructured = STRUCTURED.equals(cardEl.getSelectedKey());
		boolean isSingleCourse = SINGLE.equals(cardEl.getSelectedKey());
		boolean isCourseBundle = BUNDLE.equals(cardEl.getSelectedKey());

		if (isSingleCourse) {
			withContentEl.toggleOn();
			maxRepositoryEntryRelationsEl.select(SINGLE, true);
		} else if (isCourseBundle) {
			withContentEl.toggleOn();
			maxRepositoryEntryRelationsEl.select(BUNDLE, true);
		}
		
		if (isStructured && !allowedSubElementsEl.isVisible()) {
			if (selectedKeys != null) {
				for (String key : selectedKeys) {
					allowedSubElementsEl.select(key, true);
				}
			}
		}
		if (!isStructured && allowedSubElementsEl.isVisible()) {
			selectedKeys = allowedSubElementsEl.getSelectedKeys();
		}
		allowedSubElementsEl.setVisible(isStructured);
		
		withContentEl.setVisible(isStructured);
		boolean withContent = withContentEl.isOn();
		maxRepositoryEntryRelationsEl.setVisible(isStructured && withContent);
		if (isStructured && withContent && !maxRepositoryEntryRelationsEl.isOneSelected()) {
			maxRepositoryEntryRelationsEl.select(SINGLE, true);
		}

		boolean isElementType = CurriculumElementType.Type.element.equals(type);
		mixedTypeSpacer.setVisible(!isElementType);
		mixedTypeEl.setVisible(!isElementType);
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
		if (cardEl == source || withContentEl == source) {
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
		curriculumElementType.setType(type);
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
		
		curriculumElementType.setSingleElement(!STRUCTURED.equals(cardEl.getSelectedKey()));
		curriculumElementType.setAllowedAsRootElement(mixedTypeEl.isAtLeastSelected(1));

		curriculumElementType.setMaxRepositoryEntryRelations(getMaxRepositoryEntryRelations());

		Collection<String> selectedAllowedSubTypeKeys = allowedSubElementsEl.isVisible()
				? allowedSubElementsEl.getSelectedKeys()
				: List.of();
		List<CurriculumElementType> allowedSubTypes = new ArrayList<>();
		for(String selectedAllowedSubTypeKey:selectedAllowedSubTypeKeys) {
			allowedSubTypes.add(curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(Long.valueOf(selectedAllowedSubTypeKey))));
		}
		curriculumElementType = curriculumService.updateCurriculumElementType(curriculumElementType, allowedSubTypes);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private int getMaxRepositoryEntryRelations() {
		int maxRepositoryEntryRelations;
		if (withContentEl.isVisible() && withContentEl.isOn()) {
			if (maxRepositoryEntryRelationsEl.isOneSelected() && BUNDLE.equals(maxRepositoryEntryRelationsEl.getSelectedKey())) {
				maxRepositoryEntryRelations = -1;
			} else {
				maxRepositoryEntryRelations = 1;
			}
		} else {
			if (SINGLE.equals(cardEl.getSelectedKey())) {
				maxRepositoryEntryRelations = 1;
			} else if (BUNDLE.equals(maxRepositoryEntryRelationsEl.getSelectedKey())) {
				maxRepositoryEntryRelations = -1;
			} else {
				maxRepositoryEntryRelations = 0;				
			}
		}
		return maxRepositoryEntryRelations;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
