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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCurriculumElementController extends FormBasicController {

	private DateChooser endEl;
	private DateChooser beginEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private RichTextElement descriptionEl;
	
	
	private SingleSelection curriculumElementTypeEl;
	
	private Curriculum curriculum;
	private CurriculumElement element;
	private CurriculumElement parentElement;
	
	@Autowired
	private CurriculumService curriculumService;
	
	/**
	 * Create a new curriculum element.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 */
	public EditCurriculumElementController(UserRequest ureq, WindowControl wControl,
			CurriculumElement parentElement, Curriculum curriculum) {
		super(ureq, wControl);
		this.curriculum = curriculum;
		this.parentElement = parentElement;
		initForm(ureq);
	}
	
	public EditCurriculumElementController(UserRequest ureq, WindowControl wControl,
			CurriculumElement element, CurriculumElement parentElement, Curriculum curriculum) {
		super(ureq, wControl);
		this.curriculum = curriculum;
		this.element = element;
		this.parentElement = parentElement;
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
		if(element != null) {
			String key = element.getKey().toString();
			uifactory.addStaticTextElement("curriculum.element.key", key, formLayout);
			String externalId = element.getExternalId();
			uifactory.addStaticTextElement("curriculum.element.external.id", externalId, formLayout);
		}
		
		String identifier = element == null ? "" : element.getIdentifier();
		identifierEl = uifactory.addTextElement("identifier", "curriculum.element.identifier", 255, identifier, formLayout);
		identifierEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.identifier));
		identifierEl.setMandatory(true);

		String displayName = element == null ? "" : element.getDisplayName();
		displayNameEl = uifactory.addTextElement("displayName", "curriculum.element.displayName", 255, displayName, formLayout);
		displayNameEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.displayName));
		displayNameEl.setMandatory(true);
		
		List<CurriculumElementType> types = getTypes();
		String[] typeKeys = new String[types.size() + 1];
		String[] typeValues = new String[types.size() + 1];
		typeKeys[0] = "";
		typeValues[0] = "-";
		for(int i=types.size(); i-->0; ) {
			typeKeys[i+1] = types.get(i).getKey().toString();
			typeValues[i+1] = types.get(i).getDisplayName();
		}
		curriculumElementTypeEl = uifactory.addDropdownSingleselect("type", "curriculum.element.type", formLayout, typeKeys, typeValues, null);
		curriculumElementTypeEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.type));
		boolean typeFound = false;
		if(element != null && element.getType() != null) {
			String selectedTypeKey = element.getType().getKey().toString();
			for(String typeKey:typeKeys) {
				if(typeKey.equals(selectedTypeKey)) {
					curriculumElementTypeEl.select(selectedTypeKey, true);
					typeFound = true;
					break;
				}
			}
		}
		if(!typeFound) {
			curriculumElementTypeEl.select(typeKeys[0], true);
		}
		
		Date begin = element == null ? null : element.getBeginDate();
		beginEl = uifactory.addDateChooser("start", "curriculum.element.begin", begin, formLayout);
		beginEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.dates));

		Date end = element == null ? null : element.getEndDate();
		endEl = uifactory.addDateChooser("start", "curriculum.element.end", end, formLayout);
		endEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.dates));
		
		String description = element == null ? "" : element.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataCompact("curriculum.description", "curriculum.description", description, 10, 60, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.setEnabled(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.description));

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private List<CurriculumElementType> getTypes() {
		List<CurriculumElementType> types;
		if(element != null) {
			types = getTypes(element);
		} else if(parentElement != null) {
			types = getTypes(parentElement);
		} else {
			types = new ArrayList<>();
		}
		
		if(types.isEmpty()) {
			types.addAll(curriculumService.getCurriculumElementTypes());
		} else if(element != null && element.getType() != null && !types.contains(element.getType())) {
			types.add(element.getType());
		}
		return types;
	}
	
	private List<CurriculumElementType> getTypes(CurriculumElement curriculumElement)  {
		List<CurriculumElementType> types = new ArrayList<>();
		List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(curriculumElement);
		for(int i=parentLine.size() - 1; i-->0; ) {
			CurriculumElement parent = parentLine.get(i);
			CurriculumElementType parentType = parent.getType();
			if(parentType != null) {
				Set<CurriculumElementTypeToType> typeToTypes = parentType.getAllowedSubTypes();
				for(CurriculumElementTypeToType typeToType:typeToTypes) {
					if(typeToType != null) {
						types.add(typeToType.getAllowedSubType());
					}
				}
				break;
			}
		}
		return types;
	}

	@Override
	protected void doDispose() {
		//
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
		CurriculumElementType elementType = null;
		String selectedTypeKey = curriculumElementTypeEl.getSelectedKey();
		if(StringHelper.containsNonWhitespace(selectedTypeKey)) {
			elementType = curriculumService
					.getCurriculumElementType(new CurriculumElementTypeRefImpl(new Long(selectedTypeKey)));
		}

		if(element == null) {
			//create a new one
			element = curriculumService.createCurriculumElement(identifierEl.getValue(), displayNameEl.getValue(),
					beginEl.getDate(), endEl.getDate(), parentElement, elementType, curriculum);
		} else {
			element = curriculumService.getCurriculumElement(element);
			element.setIdentifier(identifierEl.getValue());
			element.setDisplayName(displayNameEl.getValue());
			element.setDescription(descriptionEl.getValue());
			element.setBeginDate(beginEl.getDate());
			element.setEndDate(endEl.getDate());
			element.setType(elementType);
			element = curriculumService.updateCurriculumElement(element);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}