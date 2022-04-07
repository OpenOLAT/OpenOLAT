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

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeToLectureBlock;
import org.olat.modules.lecture.AbsenceNoticeToRepositoryEntry;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;
import org.olat.modules.lecture.model.LectureBlockWithNotice;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.coach.EditDatesLecturesEntriesTableModel.DateCols;
import org.olat.modules.lecture.ui.component.LectureBlockWithTeachersComparator;
import org.olat.modules.lecture.ui.component.StartEndDayCellRenderer;
import org.olat.modules.lecture.ui.component.StartEndTimeCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditDatesLecturesEntriesController extends FormBasicController {

	private static final String[] durationKeys = new String[] { "today", "days", "exact" };
	private static final String[] targetKeys = new String[] {
		AbsenceNoticeTarget.allentries.name(), AbsenceNoticeTarget.entries.name(), AbsenceNoticeTarget.lectureblocks.name()
	};
	
	private DateChooser datesEl;
	private SingleSelection targetsEl;
	private SingleSelection durationEl;
	private FormLink prolongateButton;
	private MultipleSelectionElement entriesEl;
	private FlexiTableElement lectureBlocksEl;
	private EditDatesLecturesEntriesTableModel lectureBlocksTableModel;

	private final boolean wizard;
	private final Formatter formatter;
	private final Identity noticedIdentity;
	private final LecturesSecurityCallback secCallback;
	private final EditAbsenceNoticeWrapper noticeWrapper;
	private List<RepositoryEntry> loadedRepositoryEntries;
	private List<LectureBlockWithTeachers> loadedLectureBlocks;
	/**
	 * First default selection which stays until user deselect and go to the next step
	 */
	private List<EditDatesLecturesEntryRow> wrapSelection;

	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	
	public EditDatesLecturesEntriesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			EditAbsenceNoticeWrapper noticeWrapper, LecturesSecurityCallback secCallback, boolean wizard) {
		super(ureq, wControl, LAYOUT_BAREBONE, null, rootForm);
		setTranslator(Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.wizard = wizard;
		this.secCallback = secCallback;
		this.noticeWrapper = noticeWrapper;
		this.noticedIdentity = noticeWrapper.getIdentity();
		formatter = Formatter.getInstance(getLocale());
		initForm(ureq);
		updateDuration();
		updateTargets();
		updateAnalyseCollision();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormLayoutContainer defaultCont = FormLayoutContainer.createDefaultFormLayout("def-cont", getTranslator());
		formLayout.add(defaultCont);
		defaultCont.setRootForm(mainForm);
		
		if(wizard) {
			defaultCont.setElementCssClass("o_sel_absence_edit_dates_lectures");
			setFormTitle("notice.dates.lectures.title");
			
			String fullName = userManager.getUserDisplayName(noticedIdentity);
			uifactory.addStaticTextElement("noticed.identity", fullName, defaultCont);
		}
		
		// dates: today, day, exact (with minute hour)
		Date endDate;
		Date startDate;
		String currentDateLabel;
		String selectedDurationKey;
		if(noticeWrapper.getCurrentDate() != null) {
			startDate = noticeWrapper.getCurrentDate();
			endDate = noticeWrapper.getCurrentDate();
			selectedDurationKey = "today";
			if(CalendarUtils.isToday(startDate)) {
				currentDateLabel = translate("noticed.duration.today");
			} else {
				currentDateLabel = formatter.formatDate(startDate);
			}
		} else if(noticeWrapper.getStartDate() == null || noticeWrapper.getEndDate() == null) {
			Date now = new Date();
			startDate = CalendarUtils.startOfDay(now);
			endDate = CalendarUtils.endOfDay(now);
			if(noticeWrapper.getAbsenceNoticeType() == AbsenceNoticeType.absence) {
				selectedDurationKey = "today";
			} else {
				selectedDurationKey = "days";
			}
			currentDateLabel = translate("noticed.duration.today");
		} else {
			startDate = noticeWrapper.getStartDate();
			endDate = noticeWrapper.getEndDate();
			
			boolean sameDay = CalendarUtils.isSameDay(startDate, endDate);
			boolean startDay = AbsenceNoticeHelper.isStartOfWholeDay(startDate);
			boolean endDay = AbsenceNoticeHelper.isEndOfWholeDay(endDate);
			
			if(sameDay && startDay && endDay) {
				if(CalendarUtils.isToday(startDate)) {
					currentDateLabel = translate("noticed.duration.today");
				} else {
					currentDateLabel = formatter.formatDate(startDate);
				}
				selectedDurationKey = "today";
			} else if(startDay && endDay) {
				currentDateLabel = formatter.formatDate(startDate);
				selectedDurationKey = "days";
			} else {
				currentDateLabel = formatter.formatDate(startDate);
				selectedDurationKey = "exact";
			}
		}

		String[] durationValues = new String[] {
			currentDateLabel, translate("noticed.duration.days"), translate("noticed.duration.exact")
		};
		durationEl = uifactory.addRadiosHorizontal("noticed.duration", "noticed.duration", defaultCont, durationKeys, durationValues);
		durationEl.addActionListener(FormEvent.ONCHANGE);
		durationEl.select(selectedDurationKey, true);
		durationEl.setMandatory(true);

		datesEl = uifactory.addDateChooser("noticed.start", null, startDate, defaultCont);
		datesEl.addActionListener(FormEvent.ONCHANGE);
		datesEl.setDomReplacementWrapperRequired(false);
		datesEl.setSecondDate(endDate);
		datesEl.setSeparator("noticed.till");
		datesEl.setMandatory(true);

		if(noticeWrapper.getAbsenceNotice() == null) {
			prolongateButton = uifactory.addFormLink("prolongate.notice", defaultCont, Link.BUTTON);
			prolongateButton.setVisible(false);
		}

		// targets: all, courses, lectureblocks
		String[] targetValues = new String[] {
			translate("noticed.target.all"), translate("noticed.target.courses"), translate("noticed.target.lectureblocks")
		};
		targetsEl = uifactory.addRadiosHorizontal("noticed.targets", "noticed.targets", defaultCont, targetKeys, targetValues);
		targetsEl.addActionListener(FormEvent.ONCHANGE);
		if(noticeWrapper.getAbsenceNoticeTarget() != null) {
			targetsEl.select(noticeWrapper.getAbsenceNoticeTarget().name(), true);
		} else {
			targetsEl.select(targetKeys[2], true);
		}

		SelectionValues entriesKeyValues = new SelectionValues();
		entriesEl = uifactory.addCheckboxesVertical("noticed.entries", defaultCont, entriesKeyValues.keys(), entriesKeyValues.values(), 1);
		entriesEl.setEscapeHtml(false);
		entriesEl.setMandatory(true);
		
		FormLayoutContainer lectureTableCont = FormLayoutContainer.createVerticalFormLayout("lectures-table-cont", getTranslator());
		formLayout.add(lectureTableCont);
		lectureTableCont.setRootForm(mainForm);

		initLecturesTable(ureq, lectureTableCont);
	}
	
	private void initLecturesTable(UserRequest ureq, FormItemContainer formLayout) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DateCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DateCols.courseTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DateCols.courseExternalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DateCols.courseExternalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DateCols.date,
				new StartEndDayCellRenderer(getTranslator(), getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DateCols.time,
				new StartEndTimeCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DateCols.numOfLectures));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DateCols.lectureTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DateCols.teachers));

		lectureBlocksTableModel = new EditDatesLecturesEntriesTableModel(columnsModel, userManager, getLocale());
		lectureBlocksEl = uifactory.addTableElement(getWindowControl(), "noticed.lecture", lectureBlocksTableModel, 24, false, getTranslator(), formLayout);
		lectureBlocksEl.setLabel("noticed.lectures", durationKeys);
		lectureBlocksEl.setMandatory(true);
		lectureBlocksEl.setMultiSelect(true);
		lectureBlocksEl.setSelectAllEnable(true);
		lectureBlocksEl.setSearchEnabled(true);
		lectureBlocksEl.setExtendedSearch(null);
		lectureBlocksEl.setSearchEnabled(false);
		lectureBlocksEl.setCssDelegate(lectureBlocksTableModel);
		lectureBlocksEl.setElementCssClass("o_sel_absence_lectures_table");
		lectureBlocksEl.setAndLoadPersistedPreferences(ureq, "absence-lecture-table");
		initLecturesTableFilters();
	}
	
	private void initLecturesTableFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// courses
		if(loadedLectureBlocks != null) {
			Set<RepositoryEntry> entries = loadedLectureBlocks.stream()
					.map(LectureBlockWithTeachers::getLectureBlock)
					.map(LectureBlock::getEntry)
					.collect(Collectors.toSet());
			List<RepositoryEntry> entriesList = new ArrayList<>(entries);
			SelectionValues entryValues = new SelectionValues();
			for(RepositoryEntry entry:entriesList) {
				entryValues.add(SelectionValues.entry(entry.getKey().toString(), StringHelper.escapeHtml(entry.getDisplayname())));
			}
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.entry"), EditDatesLecturesEntriesTableModel.ENTRY_FILTER, entryValues, true));
		}
		
		// days of week
		SelectionValues dayValues = new SelectionValues();
		for(DayOfWeek dayOfWeek:DayOfWeek.values()) {
			dayValues.add(SelectionValues.entry(dayOfWeek.name(), dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, getLocale())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("day.week"), EditDatesLecturesEntriesTableModel.DAY_FILTER, dayValues, true));
		
		lectureBlocksEl.setFilters(true, filters, false, true);
	}
	
	private void updateDuration() {
		if(!durationEl.isOneSelected()) {
			//
		} else if(durationEl.isSelected(0)) {
			//today
			datesEl.setDateChooserTimeEnabled(false);
			datesEl.setSameDay(true);
			datesEl.setSecondDate(false);
		} else if(durationEl.isSelected(1)) {
			//daytime
			datesEl.setDateChooserTimeEnabled(false);
			datesEl.setSameDay(false);
			datesEl.setSecondDate(true);
		} else if(durationEl.isSelected(2)) {
			//exact
			datesEl.setDateChooserTimeEnabled(true);
			datesEl.setSameDay(false);
			datesEl.setSecondDate(true);
		}
	}
	
	private void updateTargets() {
		if(targetsEl.isOneSelected()) {
			AbsenceNoticeTarget selectedTarget = AbsenceNoticeTarget.valueOf(targetsEl.getSelectedKey());
			if(AbsenceNoticeTarget.allentries == selectedTarget) { // all entries
				entriesEl.setVisible(false);
				lectureBlocksEl.setVisible(false);
			} else if(AbsenceNoticeTarget.entries == selectedTarget) { // selected courses
				entriesEl.setVisible(true);
				lectureBlocksEl.setVisible(false);
				loadRepositoryEntries();
			} else if(AbsenceNoticeTarget.lectureblocks == selectedTarget) { // selected lecture blocks
				entriesEl.setVisible(false);
				lectureBlocksEl.setVisible(true);
				loadLectureBlocks();
			}
		}
	}
	
	private void loadRepositoryEntries() {
		loadedRepositoryEntries = new ArrayList<>();

		SelectionValues keyValues = new SelectionValues();
		Map<RepositoryEntry,Long> entries = getRepositoryEntries();
		for(Map.Entry<RepositoryEntry, Long> entry:entries.entrySet()) {
			loadedRepositoryEntries.add(entry.getKey());
			String key = entry.getKey().getKey().toString();
			String title = getRepositoryEntryLabel(entry);
			keyValues.add(SelectionValues.entry(key, title));
		}
		
		entriesEl.setKeysAndValues(keyValues.keys(), keyValues.values());
		
		if(noticeWrapper.getEntries() != null && !noticeWrapper.getEntries().isEmpty()) {
			List<RepositoryEntry> currentEntries = noticeWrapper.getEntries();
			if(currentEntries != null) {
				for(String key:keyValues.keys()) {
					for(RepositoryEntry currentEntry:currentEntries) {
						if(key.equals(currentEntry.getKey().toString())) {
							entriesEl.select(key, true);
						}
					}
				}
			}
		}	
	}
	
	private Map<RepositoryEntry,Long> getRepositoryEntries() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();

		searchParams.setViewAs(getIdentity(), secCallback.viewAs());
		if(noticedIdentity != null) {
			searchParams.setParticipant(noticedIdentity);
		}

		searchParams.setStartDate(datesEl.getDate());
		if(durationEl.isSelected(0)) {
			searchParams.setEndDate(CalendarUtils.endOfDay(searchParams.getStartDate()));
		} else if(!datesEl.isSameDay()) {
			if(durationEl.isSelected(1)) {
				searchParams.setEndDate(CalendarUtils.endOfDay(datesEl.getSecondDate()));
			} else {
				searchParams.setEndDate(datesEl.getSecondDate());
			}
		}
		
		List<LectureBlockWithTeachers> lectureBlocks = lectureService.getLectureBlocksWithTeachers(searchParams);
		Map<RepositoryEntry,Long> deduplicate = new HashMap<>();
		for(LectureBlockWithTeachers lectureBlock:lectureBlocks) {
			RepositoryEntry entry = lectureBlock.getLectureBlock().getEntry();
			Long count = deduplicate.get(entry);
			if(count == null) {
				deduplicate.put(entry, Long.valueOf(1));
			} else {
				deduplicate.put(entry, Long.valueOf(count.longValue() + 1));
			}
		}
		return deduplicate;
	}
	
	private String getRepositoryEntryLabel(Map.Entry<RepositoryEntry, Long> entryOfMap) {
		RepositoryEntry entry = entryOfMap.getKey();
		int numOfLectureBlocks = entryOfMap.getValue().intValue();
		String externalRef = StringHelper.containsNonWhitespace(entry.getExternalRef()) ? StringHelper.escapeHtml(entry.getExternalRef()) : "";
		
		String[] args = new String[] {
			StringHelper.escapeHtml(entry.getDisplayname()), 	// 0
			externalRef, 										// 1
			Integer.toString(numOfLectureBlocks)
		};
		
		String i18nKey = numOfLectureBlocks == 1 ? "wizard.entries.label.block" : "wizard.entries.label.blocks";
		return  translate(i18nKey, args);
	}
	
	private void searchLectureBlocks(FlexiTableSearchEvent event) {
		Map<Long,EditDatesLecturesEntryRow> lectureKeys = new HashMap<>();
		lectureBlocksTableModel.filter(event.getFilters());
		loadLectureBlocks(lectureKeys);
	}
	
	private void loadLectureBlocks() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		if(noticeWrapper.getPredefinedLectureBlocks() != null && !noticeWrapper.getPredefinedLectureBlocks().isEmpty()) {
			searchParams.setLectureBlocks(noticeWrapper.getPredefinedLectureBlocks());
		} else {
			searchParams.setViewAs(getIdentity(), secCallback.viewAs());
			if(noticedIdentity != null) {
				searchParams.setParticipant(noticedIdentity);
			}
			searchParams.setStartDate(datesEl.getDate());
			if(durationEl.isSelected(0)) {
				searchParams.setEndDate(CalendarUtils.endOfDay(searchParams.getStartDate()));
			} else if(!datesEl.isSameDay()) {
				if(durationEl.isSelected(1)) {
					searchParams.setEndDate(CalendarUtils.endOfDay(datesEl.getSecondDate()));
				} else {
					searchParams.setEndDate(datesEl.getSecondDate());
				}
			}
		}
		loadedLectureBlocks = lectureService.getLectureBlocksWithTeachers(searchParams);
		Collections.sort(loadedLectureBlocks, new LectureBlockWithTeachersComparator());
		
		Map<Long,EditDatesLecturesEntryRow> lectureKeys = new HashMap<>();
		List<EditDatesLecturesEntryRow> keyValues = new ArrayList<>();
		for(LectureBlockWithTeachers lectureBlock:loadedLectureBlocks) {
			EditDatesLecturesEntryRow row = new EditDatesLecturesEntryRow(lectureBlock);
			keyValues.add(row);
			lectureKeys.put(lectureBlock.getLectureBlock().getKey(), row);
		}
		lectureBlocksTableModel.setObjects(keyValues);
		loadLectureBlocks(lectureKeys);
		initLecturesTableFilters();
	}
		
	private void loadLectureBlocks(Map<Long, EditDatesLecturesEntryRow> lectureKeys) {
		List<EditDatesLecturesEntryRow> selected = getSelectedLectures();
		if(noticeWrapper.getLectureBlocks() != null && !noticeWrapper.getLectureBlocks().isEmpty()) {
			List<LectureBlock> currentBlocks = noticeWrapper.getLectureBlocks();
			if(currentBlocks != null) {
				for(LectureBlock currentBlock:currentBlocks) {
					if(lectureKeys.containsKey(currentBlock.getKey())) {
						selected.add(lectureKeys.get(currentBlock.getKey()));
					}
				}
			}
		} else if(wizard) {
			Dates dates = getDates();
			if(dates.getStartDate() != null && dates.getEndDate() != null) {
				List<AbsenceNotice> notices = lectureService.detectCollision(noticedIdentity,
						noticeWrapper.getAbsenceNotice(), dates.getStartDate(), dates.getEndDate());
				if(autoProlongate(notices)) {
					noticeWrapper.wrap(notices.get(0));
					wrapSelection = new ArrayList<>();
					List<LectureBlockWithNotice> currentBlocks = lectureService.getLectureBlocksWithAbsenceNotices(notices);
					for(LectureBlockWithNotice currentBlock:currentBlocks) {
						if(lectureKeys.containsKey(currentBlock.getLectureBlock().getKey())) {
							wrapSelection.add(lectureKeys.get(currentBlock.getLectureBlock().getKey()));
						}
					}
					datesEl.setExampleKey("update.notice", null);
				}
			}
		}
		
		lectureBlocksEl.reset(true, true, true);
		if(!selected.isEmpty()) {
			lectureBlocksTableModel.sort(new SortKey("selected", true), Set.copyOf(selected));
		}
		
		Set<Integer> indexes = new HashSet<>();
		if(wrapSelection != null) {
			selected.addAll(wrapSelection);
		}
		for(EditDatesLecturesEntryRow row:selected) {
			Integer index = lectureBlocksTableModel.indexOf(row);
			if(index != null) {
				indexes.add(index);
			}
		}
		lectureBlocksEl.setMultiSelectedIndex(indexes);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		targetsEl.clearError();
		entriesEl.clearError();
		lectureBlocksEl.clearError();
		if(!targetsEl.isOneSelected()
				|| (targetsEl.isSelected(1) && !entriesEl.isAtLeastSelected(1))
				|| (targetsEl.isSelected(3) && getSelectedLectures().isEmpty())) {
			targetsEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		allOk &= validate(lectureBlocksEl);
		allOk &= validate(entriesEl);
		
		datesEl.clearError();
		clearMarkColissions();
		if(prolongateButton != null) {
			prolongateButton.setVisible(false);
		}
		if(datesEl.getDate() == null || datesEl.getSecondDate() == null) {
			datesEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			Dates dates = getDates();
			List<AbsenceNotice> notices = lectureService.detectCollision(noticedIdentity,
					noticeWrapper.getAbsenceNotice(), dates.getStartDate(), dates.getEndDate());
			if(!notices.isEmpty()) {
				analyseCollision(notices);
			}
		}

		return allOk;
	}
	
	private List<EditDatesLecturesEntryRow> getSelectedLectures() {
		Set<Integer> selectedIndexes = lectureBlocksEl.getMultiSelectedIndex();
		List<EditDatesLecturesEntryRow> blocks = new ArrayList<>();
		for(Integer index:selectedIndexes) {
			EditDatesLecturesEntryRow row = lectureBlocksTableModel.getObject(index.intValue());
			if(row != null) {
				blocks.add(row);
			}
		}
		return blocks;
	}
	
	private List<Long> getSelectedLecturesKeys() {
		return getSelectedLectures().stream()
				.map(EditDatesLecturesEntryRow::getLectureBlockKey)
				.collect(Collectors.toList());
	}
	
	private void updateAnalyseCollision() {
		Dates dates = getDates();
		if(dates.getStartDate() != null && dates.getEndDate() != null) {
			List<AbsenceNotice> notices = lectureService.detectCollision(noticedIdentity,
					noticeWrapper.getAbsenceNotice(), dates.getStartDate(), dates.getEndDate());
			if(!notices.isEmpty()) {
				analyseCollision(notices);
			}
		}
	}
	
	private boolean analyseCollision(List<AbsenceNotice> notices) {
		if(autoProlongate(notices)) {
			noticeWrapper.wrap(notices.get(0));
			datesEl.setExampleKey("update.notice", null);
			return true;
		}

		String explanation = getNoticesExplaination(notices);
		datesEl.setErrorKey("error.collision", true, explanation);
		if(prolongateButton != null) {
			prolongateButton.setVisible(true);
			prolongateButton.setUserObject(notices);
		}
		if(entriesEl.isVisible()) {
			markEntriesCollisions(notices);
		} else if(lectureBlocksEl.isVisible()) {
			markLectureBlocksCollisions(notices);
		}
		return false;
	}
	
	private String getNoticesExplaination(List<AbsenceNotice> notices) {
		StringBuilder sb = new StringBuilder(128);
		for(AbsenceNotice notice: notices) {
			if(sb.length() > 0) {
				sb.append("; ");
			}
			
			String type = translate("noticed.type." + notice.getNoticeType().name());
			sb.append(type).append(" (");
			
			Date start = notice.getStartDate();
			Date end = notice.getEndDate();
			if(start != null && end != null) {
				if(DateUtils.isSameDate(start, end)) {
					sb.append(formatter.formatDate(start));
				} else {
					sb.append(formatter.formatDate(start)).append(" - ").append(formatter.formatDate(end));
				}
			} else if(start != null) {
				sb.append(formatter.formatDate(start));
			} else if(end != null) {
				sb.append(formatter.formatDate(end));
			}
			sb.append(")");
		}
		return sb.toString();
	}
	
	private boolean autoProlongate(List<AbsenceNotice> notices) {
		return wizard && noticeWrapper.getAbsenceNotice() == null
				&& notices.size() == 1
				&& notices.get(0).getNoticeType() == noticeWrapper.getAbsenceNoticeType()
				&& notices.get(0).getNoticeTarget() == AbsenceNoticeTarget.lectureblocks
				&& dateMatches(notices.get(0)) && lectureBlocksEl.isVisible();
	}
	
	private boolean dateMatches(AbsenceNotice notice) {
		Dates dates = getDates();
		return dates.getStartDate() != null && notice.getStartDate() != null && DateUtils.isSameDate(dates.getStartDate(), notice.getStartDate())
				&& dates.getEndDate() != null && notice.getEndDate() != null && DateUtils.isSameDate(dates.getEndDate(), notice.getEndDate());	
	}
	
	private void clearMarkColissions() {
		List<EditDatesLecturesEntryRow> lectureBlocks = lectureBlocksTableModel.getObjects();
		for(EditDatesLecturesEntryRow lectureBlock:lectureBlocks) {
			lectureBlock.setCssClass(null);
		}
		lectureBlocksEl.reset(false, false, true);
		Set<String> entriesKeys = entriesEl.getKeys();
		for(String entryKey:entriesKeys) {
			entriesEl.setCssClass(entryKey, null);
		}
	}
	
	private void markEntriesCollisions(List<AbsenceNotice> notices) {
		List<LectureBlockWithNotice> noticeToLectureBlocks = lectureService.getLectureBlocksWithAbsenceNotices(notices);
		Set<String> collisionKeys = noticeToLectureBlocks.stream()
				.filter(noticeToLectureBlock -> noticeToLectureBlock.getEntry() != null)
				.map(noticeToLectureBlock -> noticeToLectureBlock.getEntry().getKey().toString())
				.collect(Collectors.toSet());
		
		Set<String> entriesKeys = entriesEl.getKeys();
		for(String entryKey:entriesKeys) {
			String cssClass = collisionKeys.contains(entryKey) ? "o_checkbox_warning" : null;
			entriesEl.setCssClass(entryKey, cssClass);
		}
		entriesEl.getComponent().setDirty(true);
	}
	
	private void markLectureBlocksCollisions(List<AbsenceNotice> notices) {
		List<LectureBlockWithNotice> noticeToLectureBlocks = lectureService.getLectureBlocksWithAbsenceNotices(notices);
		Set<Long> collisionKeys = noticeToLectureBlocks.stream()
				.filter(noticeToLectureBlock -> noticeToLectureBlock.getLectureBlock() != null)
				.map(noticeToLectureBlock -> noticeToLectureBlock.getLectureBlock().getKey())
				.collect(Collectors.toSet());

		List<EditDatesLecturesEntryRow> lectureBlocks = lectureBlocksTableModel.getObjects();
		for(EditDatesLecturesEntryRow lectureBlock:lectureBlocks) {
			String cssClass = collisionKeys.contains(lectureBlock.getLectureBlockKey()) ? "o_lecture_warning" : null;
			lectureBlock.setCssClass(cssClass);
		}
		lectureBlocksEl.reset(false, false, true);
	}
	
	private boolean validate(MultipleSelectionElement el) {
		boolean allOk = true;
		if(el.isVisible() && !el.isAtLeastSelected(1)) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validate(FlexiTableElement el) {
		boolean allOk = true;
		if(el.isVisible() && el.getMultiSelectedIndex().isEmpty()) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(durationEl == source) {
			updateDuration();
			updateAnalyseCollision();
		} else if(targetsEl == source || datesEl == source) {
			updateTargets();
			updateAnalyseCollision();
		} else if(prolongateButton == source) {
			doProlongate(ureq);
		} else if(lectureBlocksEl == source) {
			if(event instanceof FlexiTableSearchEvent) {
				searchLectureBlocks((FlexiTableSearchEvent)event);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Dates dates = getDates();
		noticeWrapper.setStartDate(dates.getStartDate());	
		noticeWrapper.setEndDate(dates.getEndDate());
		
		AbsenceNoticeTarget selectedTarget = AbsenceNoticeTarget.valueOf(targetsEl.getSelectedKey());
		noticeWrapper.setAbsenceNoticeTarget(selectedTarget);
		if(selectedTarget == AbsenceNoticeTarget.entries) {
			final Collection<String> selectedEntryKeys = entriesEl.getSelectedKeys();
			List<RepositoryEntry> entries = loadedRepositoryEntries.stream()
					.filter(r -> selectedEntryKeys.contains(r.getKey().toString()))
					.collect(Collectors.toList());
			noticeWrapper.setEntries(entries);
		} else if(selectedTarget == AbsenceNoticeTarget.lectureblocks) {
			final List<Long> selectedLectureBlockKeys = getSelectedLecturesKeys();
			List<LectureBlock> blocks = loadedLectureBlocks.stream()
					.filter(b -> selectedLectureBlockKeys.contains(b.getLectureBlock().getKey()))
					.map(LectureBlockWithTeachers::getLectureBlock)
					.collect(Collectors.toList());
			noticeWrapper.setLectureBlocks(blocks);
		}
	}
	
	private void doProlongate(UserRequest ureq) {
		Dates dates = getDates();
		List<AbsenceNotice> notices = lectureService.detectCollision(noticedIdentity,
				noticeWrapper.getAbsenceNotice(), dates.getStartDate(), dates.getEndDate());
		if(!notices.isEmpty()) {
			AbsenceNotice notice = notices.get(0);
			noticeWrapper.wrap(notice);
			if(notice.getStartDate().before(dates.getStartDate())) {
				datesEl.setDate(notice.getStartDate());
			} else {
				noticeWrapper.setStartDate(dates.getStartDate());
			}
			if(notice.getEndDate().after(dates.getEndDate())) {
				datesEl.setSecondDate(notice.getEndDate());
			} else {
				noticeWrapper.setEndDate(dates.getEndDate());
			}
			updateTargets();
			
			Set<Integer> currentSelectedIndexes = lectureBlocksEl.getMultiSelectedIndex();
			List<AbsenceNoticeToLectureBlock> noticesToBlocks = lectureService.getAbsenceNoticeToLectureBlocks(notice);
			List<Long> lectureBlocksKeys = getSelectedLecturesKeys();
			for(AbsenceNoticeToLectureBlock noticeToBlock:noticesToBlocks) {
				Long lectureBlockKey = noticeToBlock.getLectureBlock().getKey();
				if(lectureBlocksKeys.contains(lectureBlockKey)) {
					Integer index = lectureBlocksTableModel.indexOf(lectureBlockKey);
					if(index != null) {
						currentSelectedIndexes.add(index);
					}
				}
			}
			lectureBlocksEl.setMultiSelectedIndex(currentSelectedIndexes);
			
			List<AbsenceNoticeToRepositoryEntry> noticesToEntries = lectureService.getAbsenceNoticeToRepositoryEntries(notice);
			Set<String> entriesKeys = entriesEl.getKeys();
			for(AbsenceNoticeToRepositoryEntry noticeToEntry:noticesToEntries) {
				String repositoryEntryKey = noticeToEntry.getEntry().getKey().toString();
				if(entriesKeys.contains(repositoryEntryKey)) {
					entriesEl.select(repositoryEntryKey, true);
				}
			}
			
			Date startDate = noticeWrapper.getStartDate();
			Date endDate = noticeWrapper.getEndDate();
			boolean sameDay = CalendarUtils.isSameDay(startDate, endDate);
			boolean startDay = AbsenceNoticeHelper.isStartOfWholeDay(startDate);
			boolean endDay = AbsenceNoticeHelper.isEndOfWholeDay(endDate);

			String selectedDurationKey;
			if(sameDay && startDay && endDay) {
				selectedDurationKey = "today";
			} else if(startDay && endDay) {
				selectedDurationKey = "days";
			} else {
				selectedDurationKey = "exact";
			}
			durationEl.select(selectedDurationKey, true);
			updateDuration();
			validateFormLogic(ureq);
		}
	}
	
	public Dates getDates() {
		Date start = null;
		Date end = null;
		if(durationEl.isSelected(0)) {
			start = CalendarUtils.startOfDay(datesEl.getDate());
			end = CalendarUtils.endOfDay(datesEl.getDate());
		} else if(durationEl.isSelected(1)) {
			start = CalendarUtils.startOfDay(datesEl.getDate());
			end = CalendarUtils.endOfDay(datesEl.getSecondDate());
		} else if(durationEl.isSelected(2)) {
			start = datesEl.getDate();
			end = datesEl.getSecondDate();
		}
		return new Dates(start, end);
	}
	
	private static class Dates {
		
		private final Date startDate;
		private final Date endDate;
		
		public Dates(Date startDate, Date endDate) {
			this.startDate = startDate;
			this.endDate = endDate;
		}

		public Date getStartDate() {
			return startDate;
		}

		public Date getEndDate() {
			return endDate;
		}
	}
}
