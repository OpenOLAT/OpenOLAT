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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.event.SearchAbsenceNoticeEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeSearchController extends FormBasicController {
	
	private static final String[] authorizedKeys = new String[] { "authorized", "not"};
	private static final String[] typeKeys = new String[] {
			AbsenceNoticeType.absence.name(), AbsenceNoticeType.notified.name(), AbsenceNoticeType.dispensation.name()
		};
	
	private TextElement searchEl;
	private DateChooser dateEl;
	private MultipleSelectionElement typeEl;
	private SingleSelection absenceCategoryEl;
	private MultipleSelectionElement authorizedEl;
	
	private Date currentDate;
	private List<AbsenceCategory> categories;
	
	@Autowired
	private LectureService lectureService;
	
	public AbsenceNoticeSearchController(UserRequest ureq, WindowControl wControl, Date currentDate) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		categories = lectureService.getAbsencesCategories(Boolean.TRUE);
		this.currentDate = currentDate;
		initForm(ureq);
	}
	
	public void setCurrentDate(Date date) {
		currentDate = date;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchEl = uifactory.addTextElement("search.form.string", 128, "", formLayout);
		searchEl.setHelpTextKey("search.form.string.hint.more", null);
		
		String[] typeValues = new String[] {
			translate("noticed.type.absence"), translate("noticed.type.notified"), translate("noticed.type.dispensation")
		};
		typeEl = uifactory.addCheckboxesDropdown("type", "search.form.type", formLayout, typeKeys, typeValues);
		typeEl.selectAll();
		
		String[] authorizedValues = new String[] {
			translate("authorized.absence"), translate("not.authorized.absence")
		};
		authorizedEl = uifactory.addCheckboxesHorizontal("authorized", null, formLayout, authorizedKeys, authorizedValues);
		authorizedEl.selectAll();
		
		KeyValues categoriesKeyValues = new KeyValues();
		categoriesKeyValues.add(KeyValues.entry("", translate("all")));
		categories.forEach(cat -> categoriesKeyValues.add(KeyValues.entry(cat.getKey().toString(), cat.getTitle())));
		absenceCategoryEl = uifactory.addDropdownSingleselect("search.form.category", formLayout,
				categoriesKeyValues.keys(), categoriesKeyValues.values());
		absenceCategoryEl.setVisible(!categoriesKeyValues.isEmpty());
		
		dateEl = uifactory.addDateChooser("search.date", currentDate, formLayout);
		dateEl.setSecondDate(true);
		dateEl.setSecondDate(currentDate);
		dateEl.setSeparator("search.form.till");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("search", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Date startDate = dateEl.getDate();
		Date endDate = dateEl.getSecondDate();
		if(endDate != null) {
			endDate = CalendarUtils.endOfDay(endDate);
		}
		Boolean authorized = null;
		if(authorizedEl.isAtLeastSelected(1) && !authorizedEl.isAtLeastSelected(2)) {
			authorized = Boolean.valueOf(authorizedEl.isSelected(0)); 
		}
		
		AbsenceCategory category = null;
		String selectedCategory = absenceCategoryEl.getSelectedKey();
		if(StringHelper.isLong(selectedCategory)) {
			Long selectedKey = Long.valueOf(selectedCategory);
			category = categories.stream()
					.filter(cat -> cat.getKey().equals(selectedKey))
					.findFirst().orElse(null);
		}
		
		Collection<String> selectedTypeKeys = typeEl.getSelectedKeys();
		List<AbsenceNoticeType> types = selectedTypeKeys.stream()
				.map(AbsenceNoticeType::valueOf).collect(Collectors.toList());
		fireEvent(ureq, new SearchAbsenceNoticeEvent(searchEl.getValue(), startDate, endDate, authorized, category, types));
	}
}
