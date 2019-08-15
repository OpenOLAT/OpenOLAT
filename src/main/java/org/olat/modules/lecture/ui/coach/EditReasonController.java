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
package org.olat.modules.lecture.ui.coach;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditReasonController extends FormBasicController {
	
	private static final String[] typeKeys = new String[] {
		AbsenceNoticeType.absence.name(), AbsenceNoticeType.notified.name(), AbsenceNoticeType.dispensation.name()
	};
	private static final String[] authorizedKeys = new String[] { "autorized" };
	
	private TextElement reasonEl;
	private SingleSelection typeEl;
	private SingleSelection absenceCategoriesEl;
	private MultipleSelectionElement authorizedEl;
	
	private final boolean wizard;
	private final Identity noticedIdentity;
	private final EditAbsenceNoticeWrapper noticeWrapper;
	private List<AbsenceCategory> absenceCategories;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	
	public EditReasonController(UserRequest ureq, WindowControl wControl, Form rootForm,
			EditAbsenceNoticeWrapper noticeWrapper, boolean wizard) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		setTranslator(Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.wizard = wizard;
		this.noticeWrapper = noticeWrapper;
		this.noticedIdentity = noticeWrapper.getIdentity();
		absenceCategories = lectureService.getAllAbsencesCategories();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(wizard) {
			formLayout.setElementCssClass("o_sel_absence_edit_reason");
			setFormTitle("notice.reason.title");
		}
		
		String fullName = userManager.getUserDisplayName(noticedIdentity);
		uifactory.addStaticTextElement("noticed.identity", fullName, formLayout);
		
		String[] typeValues = new String[] {
			translate("noticed.type.absence"), translate("noticed.type.notified"), translate("noticed.type.dispensation")
		};
		typeEl = uifactory.addRadiosHorizontal("noticed.type", "noticed.type", formLayout, typeKeys, typeValues);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		if(noticeWrapper.getAbsenceNoticeType() != null) {
			typeEl.select(noticeWrapper.getAbsenceNoticeType().name(), true);
		}
		String[] authorizedValues = new String[] { translate("noticed.autorized.yes") };
		authorizedEl = uifactory.addCheckboxesHorizontal("noticed.autorized", null, formLayout, authorizedKeys, authorizedValues);
		if(noticeWrapper.getAuthorized() != null && noticeWrapper.getAuthorized().booleanValue()) {
			authorizedEl.select(authorizedKeys[0], true);
		}
		
		KeyValues absenceKeyValues = new KeyValues();
		for(AbsenceCategory absenceCategory: absenceCategories) {
			absenceKeyValues.add(KeyValues.entry(absenceCategory.getKey().toString(), absenceCategory.getTitle()));
		}

		absenceCategoriesEl = uifactory.addDropdownSingleselect("absence.category", "noticed.category", formLayout, absenceKeyValues.keys(), absenceKeyValues.values());
		absenceCategoriesEl.setDomReplacementWrapperRequired(false);
		absenceCategoriesEl.setVisible(!absenceCategories.isEmpty());
		absenceCategoriesEl.setMandatory(true);
		AbsenceCategory currentCategory = noticeWrapper.getAbsenceCategory();
		if(currentCategory != null) {
			for(AbsenceCategory absenceCategory: absenceCategories) {
				if(absenceCategory.equals(currentCategory)) {
					absenceCategoriesEl.select(absenceCategory.getKey().toString(), true);
				}
			}
		}

		String currentReason = noticeWrapper.getAbsenceReason();
		reasonEl = uifactory.addTextAreaElement("reason", "noticed.reason", 2048, 4, 36, false, false, currentReason, formLayout);
	}

	@Override
	protected void doDispose() {
		//
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
		
		typeEl.clearError();
		if(!typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(typeEl == source) {
			boolean forceAuthorized = typeEl.isOneSelected()
					&& AbsenceNoticeType.dispensation.name().equals(typeEl.getSelectedKey());
			if(forceAuthorized && !authorizedEl.isAtLeastSelected(1)) {
				authorizedEl.select(authorizedKeys[0], true);
			}
			authorizedEl.setEnabled(!forceAuthorized);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		noticeWrapper.setAbsenceReason(reasonEl.getValue());
		noticeWrapper.setAbsenceCategory(getAbsenceCategory());
		noticeWrapper.setAuthorized(authorizedEl.isAtLeastSelected(1));
		noticeWrapper.setAbsenceNoticeType(AbsenceNoticeType.valueOf(typeEl.getSelectedKey()));
	}
}
