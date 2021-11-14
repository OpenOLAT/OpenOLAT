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
package org.olat.modules.lecture.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.LectureService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReasonController extends FormBasicController {
	
	private TextElement reasonEl;
	private SingleSelection absenceCategoriesEl;
	
	private final boolean editable;
	private RollCallRow row;
	private final List<AbsenceCategory> absenceCategories;
	
	@Autowired
	private LectureService lectureService;

	public ReasonController(UserRequest ureq, WindowControl wControl, RollCallRow row, boolean editable) {
		super(ureq, wControl, "reason");
		this.row = row;
		this.editable = editable;
		absenceCategories = lectureService.getAbsencesCategories(null);
		initForm(ureq);
	}
	
	public RollCallRow getRollCallRow() {
		return row;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		AbsenceCategory currentCategory = row.getRollCall() == null ? null : row.getRollCall().getAbsenceCategory();
		SelectionValues absenceKeyValues = new SelectionValues();
		for(AbsenceCategory absenceCategory: absenceCategories) {
			if(absenceCategory.isEnabled() || absenceCategory.equals(currentCategory)) {
				absenceKeyValues.add(SelectionValues.entry(absenceCategory.getKey().toString(), absenceCategory.getTitle()));
			}
		}
		absenceCategoriesEl = uifactory.addDropdownSingleselect("absence.category", null, formLayout, absenceKeyValues.keys(), absenceKeyValues.values());
		absenceCategoriesEl.setDomReplacementWrapperRequired(false);
		absenceCategoriesEl.setVisible(!absenceKeyValues.isEmpty());
		absenceCategoriesEl.setMandatory(true);
		if(currentCategory != null) {
			for(AbsenceCategory absenceCategory: absenceCategories) {
				if(absenceCategory.equals(currentCategory)) {
					absenceCategoriesEl.select(absenceCategory.getKey().toString(), true);
				}
			}
		}

		String currentReason = row.getRollCall() == null ? "" : row.getRollCall().getAbsenceReason();
		reasonEl = uifactory.addTextAreaElement("reason", "reason", 2048, 4, 36, false, false, currentReason, formLayout);
		reasonEl.setEnabled(editable);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		if(editable) {
			uifactory.addFormSubmitButton("save", formLayout);
		}
	}
	
	public String getReason() {
		return reasonEl.getValue();
	}
	
	public AbsenceCategory getAbsenceCategory() {
		if(!absenceCategoriesEl.isVisible() || !absenceCategoriesEl.isOneSelected()) return null;
		
		String selectedKey = absenceCategoriesEl.getSelectedKey();
		if(StringHelper.isLong(selectedKey)) {
			Long categoryKey = Long.valueOf(selectedKey);
			for(AbsenceCategory absenceCategory:absenceCategories) {
				if(absenceCategory.getKey().equals(categoryKey)) {
					return absenceCategory;
				}
			}
		}
		return null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		absenceCategoriesEl.clearError();
		if(absenceCategoriesEl.isVisible() && !absenceCategoriesEl.isOneSelected()) {
			absenceCategoriesEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
