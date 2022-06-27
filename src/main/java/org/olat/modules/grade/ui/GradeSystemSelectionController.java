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
package org.olat.modules.grade.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.grade.GradeSystem;

/**
 * 
 * Initial date: 22 Apr 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeSystemSelectionController extends FormBasicController {
	
	private SingleSelection gradeSystemEl;

	private final SelectionValues gradeSystemSV;
	private final GradeSystem gradeSystem;

	public GradeSystemSelectionController(UserRequest ureq, WindowControl wControl, SelectionValues gradeSystemSV, GradeSystem gradeSystem) {
		super(ureq, wControl);
		this.gradeSystemSV = gradeSystemSV;
		this.gradeSystem = gradeSystem;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("grade.system.selection.info");
		
		gradeSystemEl = uifactory.addDropdownSingleselect("grade.system", formLayout, gradeSystemSV.keys(), gradeSystemSV.values());
		gradeSystemEl.addActionListener(FormEvent.ONCHANGE);
		if (gradeSystem != null && gradeSystemEl.containsKey(gradeSystem.getKey().toString())) {
			gradeSystemEl.select(gradeSystem.getKey().toString(), true);
		} else if (gradeSystemEl.getKeys().length > 0) {
			gradeSystemEl.select(gradeSystemEl.getKey(0), true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setElementCssClass("o_button_group o_button_group_right o_button_group_bottom");
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("select", buttonsCont);
	}
	
	public String getSelectedKey() {
		return gradeSystemEl.isOneSelected()? gradeSystemEl.getSelectedKey(): gradeSystemEl.getKey(0);
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
