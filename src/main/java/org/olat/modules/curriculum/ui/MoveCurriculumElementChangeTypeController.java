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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;

/**
 * 
 * Initial date: 19 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MoveCurriculumElementChangeTypeController extends FormBasicController {
	
	private SingleSelection typesEl;
	
	private List<CurriculumElementType> types;
	private List<CurriculumElement> invalidElements;
	
	public MoveCurriculumElementChangeTypeController(UserRequest ureq, WindowControl wControl,
			List<CurriculumElement> invalidElements, List<CurriculumElementType> types) {
		super(ureq, wControl);
		this.types = types;
		this.invalidElements = invalidElements;
		initForm(ureq);
	}
	
	public List<CurriculumElement> getInvalidElements() {
		return invalidElements;
	}
	
	public CurriculumElementType getSelectedCurriculumElementType() {
		if(typesEl.isOneSelected()) {
			Long selectedKey = Long.valueOf(typesEl.getSelectedKey());
			for(CurriculumElementType type:types) {
				if(type.getKey().equals(selectedKey)) {
					return type;
				}
			}
		}
		return null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues typesPK = new SelectionValues();
		for(CurriculumElementType type:types) {
			typesPK.add(SelectionValues.entry(type.getKey().toString(), StringHelper.escapeHtml(type.getDisplayName())));
		}
		typesEl = uifactory.addDropdownSingleselect("move.element.type.list", "move.element.type.list", formLayout, typesPK.keys(), typesPK.values());	
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("move.element", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
