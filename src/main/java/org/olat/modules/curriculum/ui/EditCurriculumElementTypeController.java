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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
	
	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement cssClassEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private RichTextElement descriptionEl;
	private MultipleSelectionElement allowedSubTypesEl;
	private MultipleSelectionElement lecturesEnabledEl;
	private MultipleSelectionElement calendarsEnabledEl;
	private MultipleSelectionElement learningProgressEnabledEl;
	
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

		String[] onValues = new String[] { translate("type.calendars.enabled.on") };
		calendarsEnabledEl = uifactory.addCheckboxesHorizontal("type.calendars.enabled", formLayout, onKeys, onValues);
		calendarsEnabledEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.calendars));
		CurriculumCalendars calendarsEnabled =  curriculumElementType == null ? null : curriculumElementType.getCalendars();
		calendarsEnabledEl.select(onKeys[0], calendarsEnabled == CurriculumCalendars.enabled);
		
		String[] onLecturesValues = new String[] { translate("type.lectures.enabled.on") };
		lecturesEnabledEl = uifactory.addCheckboxesHorizontal("type.lectures.enabled", formLayout, onKeys, onLecturesValues);
		lecturesEnabledEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.lectures));
		CurriculumLectures lecturesEnabled =  curriculumElementType == null ? null : curriculumElementType.getLectures();
		lecturesEnabledEl.select(onKeys[0], lecturesEnabled == CurriculumLectures.enabled);
		
		String[] onLearningProgressValues = new String[] { translate("type.learning.progress.enabled.on") };
		learningProgressEnabledEl = uifactory.addCheckboxesHorizontal("type.learning.progress.enabled", formLayout, onKeys, onLearningProgressValues);
		learningProgressEnabledEl.setEnabled(!CurriculumElementTypeManagedFlag.isManaged(curriculumElementType, CurriculumElementTypeManagedFlag.learningProgress));
		CurriculumLearningProgress learningProgressEnabled =  curriculumElementType == null ? null : curriculumElementType.getLearningProgress();
		learningProgressEnabledEl.select(onKeys[0], learningProgressEnabled == CurriculumLearningProgress.enabled);
		
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		types.remove(curriculumElementType);
		
		String[] subTypeKeys = new String[types.size()];
		String[] subTypeValues = new String[types.size()];
		for(int i=types.size(); i-->0; ) {
			subTypeKeys[i] = types.get(i).getKey().toString();
			subTypeValues[i] = types.get(i).getDisplayName();
		}
		allowedSubTypesEl = uifactory.addCheckboxesVertical("type.allowed.sub.types", formLayout, subTypeKeys, subTypeValues, 2);
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
		if(calendarsEnabledEl.isAtLeastSelected(1)) {
			curriculumElementType.setCalendars(CurriculumCalendars.enabled);
		} else {
			curriculumElementType.setCalendars(CurriculumCalendars.disabled);
		}
		if(lecturesEnabledEl.isAtLeastSelected(1)) {
			curriculumElementType.setLectures(CurriculumLectures.enabled);
		} else {
			curriculumElementType.setLectures(CurriculumLectures.disabled);
		}
		if(learningProgressEnabledEl.isAtLeastSelected(1)) {
			curriculumElementType.setLearningProgress(CurriculumLearningProgress.enabled);
		} else {
			curriculumElementType.setLearningProgress(CurriculumLearningProgress.disabled);
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
