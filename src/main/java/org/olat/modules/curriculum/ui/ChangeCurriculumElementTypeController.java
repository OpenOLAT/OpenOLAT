/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ChangeCurriculumElementTypeController extends FormBasicController {
	
	private SingleSelection curriculumElementTypeEl;
	
	private Set<CurriculumElementType> types;
	private final List<CurriculumElement> curriculumElements;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	
	public ChangeCurriculumElementTypeController(UserRequest ureq, WindowControl wControl, List<CurriculumElement> curriculumElements) {
		super(ureq, wControl);
		this.curriculumElements = curriculumElements;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		for(CurriculumElement curriculumElement:curriculumElements) {
			CurriculumElement parentElement = curriculumElement.getParent();
			List<CurriculumElementType> allowedTypes = curriculumService.getAllowedCurriculumElementType(parentElement, curriculumElement);
			if(types == null) {
				types = new HashSet<>(allowedTypes);
			} else {
				types.retainAll(allowedTypes);
			}
		}

		SelectionValues typePK = new SelectionValues();
		for(CurriculumElementType type:types) {
			typePK.add(SelectionValues.entry(type.getKey().toString(), type.getDisplayName()));
		}
		typePK.sort(SelectionValues.VALUE_ASC);
		curriculumElementTypeEl = uifactory.addDropdownSingleselect("curriculum.element.type", formLayout, typePK.keys(), typePK.values());
		if(typePK.isEmpty()) {
			curriculumElementTypeEl.setErrorKey("curriculums.elements.bulk.change.type.not.compatible");
		}
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("type.bulk.change", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		curriculumElementTypeEl.clearError();
		if(!curriculumElementTypeEl.isOneSelected()) {
			curriculumElementTypeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String typeKey = curriculumElementTypeEl.getSelectedKey();
		Optional<CurriculumElementType> selectedType = types.stream()
			.filter(type -> typeKey.equals(type.getKey().toString()))
			.findFirst();
		
		if(selectedType.isPresent()) {
			final CurriculumElementType type = selectedType.get();
			for(CurriculumElement curriculumElement:curriculumElements) {
				curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
				curriculumElement.setType(type);
				curriculumService.updateCurriculumElement(getIdentity(), curriculumElement);
			}
		}

		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
