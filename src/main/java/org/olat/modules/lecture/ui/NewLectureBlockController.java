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
package org.olat.modules.lecture.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.AutoCompleter;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LocationHistory;
import org.olat.modules.lecture.ui.component.LocationDateComparator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewLectureBlockController extends FormBasicController {
	
	private static final String COMPULSORY = "compulsory";
	
	private TextElement titleEl;
	private DateChooser endDateEl;
	private DateChooser startDateEl;
	private TextElement descriptionEl;
	private TextElement preparationEl;
	private AutoCompleter locationEl;
	private MultipleSelectionElement teacherEl;
	private MultipleSelectionElement compulsoryEl;
	private SingleSelection curriculumEntriesEl;
	
	private final RepositoryEntry entry;
	private final CurriculumElement curriculumElement;
	private final List<RepositoryEntry> curriculumEntries;
	private final List<LocationHistory> locations;
	private final List<Identity> teachers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	
	public NewLectureBlockController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, List<RepositoryEntry> curriculumEntries) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.entry = null;
		this.curriculumElement = curriculumElement;
		this.curriculumEntries = curriculumEntries;
		locations = getLocations(ureq);
		teachers = getTeachers();
		initForm(ureq);
	}
	
	private List<LocationHistory> getLocations(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		List<LocationHistory> showConfig  = guiPrefs.getList(LectureBlock.class, getLocationsPrefsId(), LocationHistory.class);
		return showConfig == null ? new ArrayList<>() : showConfig;
	}
	
	private String getLocationsPrefsId() {
		return "Lectures::Location::" + getIdentity().getKey();
	}
	
	private List<Identity> getTeachers() {
		List<Identity> coaches;
		if(curriculumElement != null && entry == null) {
			coaches = curriculumService.getMembersIdentity(curriculumElement, CurriculumRoles.coach);
		} else {
			coaches = repositoryService.getMembers(entry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.coach.name());
		}
		return coaches;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleEl = uifactory.addTextElement("title", "lecture.title", 128, null, formLayout);
		titleEl.setElementCssClass("o_sel_repo_lecture_title");
		titleEl.setMandatory(true);
		titleEl.setFocus(true);
		
		String datesPage = velocity_root + "/dates_2_columns.html";
		FormLayoutContainer datesCont = uifactory.addCustomFormLayout("dates", null, datesPage, formLayout);
		startDateEl = uifactory.addDateChooser("lecture.date.start", null, datesCont);
		startDateEl.setElementCssClass("o_sel_repo_lecture_date");
		startDateEl.setDateChooserTimeEnabled(true);
		startDateEl.setMandatory(true);
		startDateEl.setSameDay(true);

		endDateEl = uifactory.addDateChooser("lecture.date.end", null, datesCont);
		endDateEl.setElementCssClass("o_sel_repo_lecture_date");
		endDateEl.setDateChooserTimeEnabled(true);
		endDateEl.setMandatory(true);
		endDateEl.setSameDay(true);

		SelectionValues courseEntriesPK = new SelectionValues();
		courseEntriesPK.add(SelectionValues.entry("-", translate("no.course")));
		if(curriculumEntries != null) {
			for(RepositoryEntry curriculumEntry:curriculumEntries) {
				courseEntriesPK.add(SelectionValues.entry(curriculumEntry.getKey().toString(), curriculumEntry.getDisplayname()));
			}
		}
		curriculumEntriesEl = uifactory.addDropdownSingleselect("lecture.curriculum.entries", "lecture.curriculum.entries", formLayout,
				courseEntriesPK.keys(), courseEntriesPK.values());
		curriculumEntriesEl.select("-", true);
		curriculumEntriesEl.setVisible(curriculumEntries != null && curriculumEntries.size() > 1);
		
		SelectionValues teacherPK = new SelectionValues();
		teacherPK.add(SelectionValues.entry("-", translate("no.teachers")));
		for(Identity teacher:teachers) {
			teacherPK.add(SelectionValues.entry(teacher.getKey().toString(), userManager.getUserDisplayName(teacher)));
		}
		teacherEl = uifactory.addCheckboxesVertical("teacher", "lecture.teacher", formLayout, teacherPK.keys(), teacherPK.values(), 2);
		teacherEl.setElementCssClass("o_sel_repo_lecture_teachers");
		teacherEl.setMandatory(true);
		
		descriptionEl = uifactory.addTextAreaElement("lecture.descr", 4, 72, null, formLayout);
		descriptionEl.setElementCssClass("o_sel_repo_lecture_description");
		
		preparationEl = uifactory.addTextAreaElement("lecture.preparation", 4, 72, null, formLayout);
		preparationEl.setElementCssClass("o_sel_repo_lecture_preparation");
		
		locationEl = uifactory.addTextElementWithAutoCompleter("location", "lecture.location", 128, null, formLayout);
		locationEl.setElementCssClass("o_sel_repo_lecture_location");
		locationEl.setListProvider(new LocationListProvider(), ureq.getUserSession());
		locationEl.setMinLength(1);
		
		SelectionValues compulsoryPK = new SelectionValues();
		compulsoryPK.add(SelectionValues.entry(COMPULSORY, translate("compulsory.check")));
		compulsoryEl = uifactory.addCheckboxesVertical("compulsory", "lecture.compulsory", formLayout,
				compulsoryPK.keys(), compulsoryPK.values(), 1);
		compulsoryEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer buttonLayout = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", "save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		LectureBlock lectureBlock;
		if(entry != null) {
			lectureBlock = lectureService.createLectureBlock(entry);
		} else if(curriculumElement != null) {
			RepositoryEntry courseEntry = null;
			if(curriculumEntries != null) {
				if(curriculumEntries.size() == 1) {
					courseEntry = curriculumEntries.get(0);
				} else if(curriculumEntriesEl.isVisible() && curriculumEntriesEl.isOneSelected()
						&& !"-".equals(curriculumEntriesEl.getSelectedKey())) {
					Long key = Long.valueOf(curriculumEntriesEl.getSelectedKey());
					for(RepositoryEntry curriculumEntry:curriculumEntries) {
						if(key.equals(curriculumEntry.getKey())) {
							courseEntry = curriculumEntry;
						}
					}
				}
			}
			lectureBlock = lectureService.createLectureBlock(curriculumElement, courseEntry);
		} else {
			return;
		}

		lectureBlock.setTitle(titleEl.getValue());
		lectureBlock.setCompulsory(compulsoryEl.isAtLeastSelected(1));
		lectureBlock.setDescription(descriptionEl.getValue());
		lectureBlock.setPreparation(preparationEl.getValue());
		if(locationEl.isEnabled()) {// autocompleter don't collect value if disabled
			lectureBlock.setLocation(locationEl.getValue());
		}
		lectureBlock.setStartDate(startDateEl.getDate());
		lectureBlock.setEndDate(endDateEl.getSecondDate());
		
		int plannedLectures = 4;// Integer.parseInt(plannedLecturesEl.getSelectedKey());
		lectureBlock.setPlannedLecturesNumber(plannedLectures);

		List<Group> selectedGroups = new ArrayList<>();
		if(entry != null) {
			//TODO curriculum
		} else if(curriculumElement != null) {
			selectedGroups.add(curriculumElement.getGroup());
		}

		lectureBlock = lectureService.save(lectureBlock, selectedGroups);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public class LocationListProvider implements ListProvider {
		
		@Override
		public int getMaxEntries() {
			return locations.size();
		}

		@Override
		public void getResult(String searchValue, ListReceiver receiver) {
			if(locations != null && !locations.isEmpty()) {
				if(locations.size() > 2) {
					Collections.sort(locations, new LocationDateComparator());
				}
				
				for(LocationHistory location:locations) {
					String val = StringHelper.xssScan(location.getLocation());
					if(StringHelper.containsNonWhitespace(val)) {
						receiver.addEntry(val, val);
					}
				}
			}
		}
	}
}
