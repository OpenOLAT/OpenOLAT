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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
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
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.component.LectureBlockWithTeachersComparator;
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
	private MultipleSelectionElement lectureBlocksEl;

	private final boolean wizard;
	private final Formatter formatter;
	private final Identity noticedIdentity;
	private final LecturesSecurityCallback secCallback;
	private final EditAbsenceNoticeWrapper noticeWrapper;
	private List<RepositoryEntry> loadedRepositoryEntries;
	private List<LectureBlockWithTeachers> loadedLectureBlocks;

	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	
	public EditDatesLecturesEntriesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			EditAbsenceNoticeWrapper noticeWrapper, LecturesSecurityCallback secCallback, boolean wizard) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		setTranslator(Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.wizard = wizard;
		this.secCallback = secCallback;
		this.noticeWrapper = noticeWrapper;
		this.noticedIdentity = noticeWrapper.getIdentity();
		formatter = Formatter.getInstance(getLocale());
		initForm(ureq);
		updateDuration();
		updateTargets();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(wizard) {
			formLayout.setElementCssClass("o_sel_absence_edit_dates_lectures");
			setFormTitle("notice.dates.lectures.title");
			
			String fullName = userManager.getUserDisplayName(noticedIdentity);
			uifactory.addStaticTextElement("noticed.identity", fullName, formLayout);
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
		durationEl = uifactory.addRadiosHorizontal("noticed.duration", "noticed.duration", formLayout, durationKeys, durationValues);
		durationEl.addActionListener(FormEvent.ONCHANGE);
		durationEl.select(selectedDurationKey, true);
		durationEl.setMandatory(true);

		datesEl = uifactory.addDateChooser("noticed.start", null, startDate, formLayout);
		datesEl.addActionListener(FormEvent.ONCHANGE);
		datesEl.setDomReplacementWrapperRequired(false);
		datesEl.setSecondDate(endDate);
		datesEl.setSeparator("noticed.till");
		datesEl.setMandatory(true);

		if(noticeWrapper.getAbsenceNotice() == null) {
			prolongateButton = uifactory.addFormLink("prolongate.notice", formLayout, Link.BUTTON);
			prolongateButton.setVisible(false);
		}

		// targets: all, courses, lectureblocks
		String[] targetValues = new String[] {
			translate("noticed.target.all"), translate("noticed.target.courses"), translate("noticed.target.lectureblocks")
		};
		targetsEl = uifactory.addRadiosHorizontal("noticed.targets", "noticed.targets", formLayout, targetKeys, targetValues);
		targetsEl.addActionListener(FormEvent.ONCHANGE);
		if(noticeWrapper.getAbsenceNoticeTarget() != null) {
			targetsEl.select(noticeWrapper.getAbsenceNoticeTarget().name(), true);
		} else {
			targetsEl.select(targetKeys[2], true);
		}

		KeyValues entriesKeyValues = new KeyValues();
		entriesEl = uifactory.addCheckboxesVertical("noticed.entries", formLayout, entriesKeyValues.keys(), entriesKeyValues.values(), 1);
		entriesEl.setEscapeHtml(false);
		entriesEl.setMandatory(true);

		KeyValues lecturesKeyValues = new KeyValues();
		lectureBlocksEl = uifactory.addCheckboxesVertical("noticed.lectures", formLayout, lecturesKeyValues.keys(), lecturesKeyValues.values(), 1);
		lectureBlocksEl.setEscapeHtml(false);
		lectureBlocksEl.setMandatory(true);
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

		KeyValues keyValues = new KeyValues();
		Map<RepositoryEntry,Long> entries = getRepositoryEntries();
		for(Map.Entry<RepositoryEntry, Long> entry:entries.entrySet()) {
			loadedRepositoryEntries.add(entry.getKey());
			String key = entry.getKey().getKey().toString();
			String title = getRepositoryEntryLabel(entry);
			keyValues.add(KeyValues.entry(key, title));
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
		
		String[] args = new String[] {
			StringHelper.escapeHtml(entry.getDisplayname()), 	// 0
			StringHelper.escapeHtml(entry.getExternalRef()), 	// 1
			Integer.toString(numOfLectureBlocks)
		};
		
		String i18nKey = numOfLectureBlocks == 1 ? "wizard.entries.label.block" : "wizard.entries.label.blocks";
		return  translate(i18nKey, args);
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
		
		KeyValues keyValues = new KeyValues();
		for(LectureBlockWithTeachers lectureBlock:loadedLectureBlocks) {
			String key = lectureBlock.getLectureBlock().getKey().toString();
			String value = getLectureBlockLabel(lectureBlock);
			keyValues.add(KeyValues.entry(key, value));
		}
		lectureBlocksEl.setKeysAndValues(keyValues.keys(), keyValues.values());
		
		if(noticeWrapper.getLectureBlocks() != null && !noticeWrapper.getLectureBlocks().isEmpty()) {
			List<LectureBlock> currentBlocks = noticeWrapper.getLectureBlocks();
			if(currentBlocks != null) {
				for(String key:keyValues.keys()) {
					for(LectureBlock currentBlock:currentBlocks) {
						if(key.equals(currentBlock.getKey().toString())) {
							lectureBlocksEl.select(key, true);
						}
					}
				}
			}
		}
	}
	
	private String getLectureBlockLabel(LectureBlockWithTeachers lectureBlock) {
		LectureBlock block = lectureBlock.getLectureBlock();
		RepositoryEntry entry = block.getEntry();
		
		String[] args = new String[] {
			StringHelper.escapeHtml(entry.getDisplayname()),	// 0
			StringHelper.escapeHtml(entry.getExternalRef()),	// 1
			formatter.formatDate(block.getStartDate()), 		// 2
			formatter.formatTimeShort(block.getStartDate()), 	// 3
			formatter.formatTimeShort(block.getEndDate()), 		// 4
			Integer.toString(block.getPlannedLecturesNumber()),	// 5
			StringHelper.escapeHtml(block.getTitle()),			// 6
			getTeachers(lectureBlock)							// 7	
		};
		return translate("wizard.lectureblock.label", args);
	}
	
	private String getTeachers(LectureBlockWithTeachers lectureBlock) {
		StringBuilder sb = new StringBuilder();
		for(Identity teacher:lectureBlock.getTeachers()) {
			if(sb.length() > 0) sb.append("; ");
			String fullName = userManager.getUserDisplayName(teacher);
			sb.append(StringHelper.escapeHtml(fullName));
		}
		return sb.toString();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		targetsEl.clearError();
		entriesEl.clearError();
		lectureBlocksEl.clearError();
		if(!targetsEl.isOneSelected()
				|| (targetsEl.isSelected(1) && !entriesEl.isAtLeastSelected(1))
				|| (targetsEl.isSelected(3) && !lectureBlocksEl.isAtLeastSelected(1))) {
			targetsEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		allOk &= validate(lectureBlocksEl);
		allOk &= validate(entriesEl);
		
		datesEl.clearError();
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
				datesEl.setErrorKey("error.collision", null);
				allOk &= false;
				if(prolongateButton != null) {
					prolongateButton.setVisible(true);
					prolongateButton.setUserObject(notices);
				}
			}
		}

		return allOk;
	}
	
	private boolean validate(MultipleSelectionElement el) {
		boolean allOk = true;
		if(el.isVisible() && !el.isAtLeastSelected(1)) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(durationEl == source) {
			updateDuration();
		} else if(targetsEl == source || datesEl == source) {
			updateTargets();
		} else if(prolongateButton == source) {
			doProlongate(ureq);
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
			final Collection<String> selectedLectureBlockKeys = lectureBlocksEl.getSelectedKeys();
			List<LectureBlock> blocks = loadedLectureBlocks.stream()
					.filter(b -> selectedLectureBlockKeys.contains(b.getLectureBlock().getKey().toString()))
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
			
			List<AbsenceNoticeToLectureBlock> noticesToBlocks = lectureService.getAbsenceNoticeToLectureBlocks(notice);
			Set<String> lectureBlocksKeys = lectureBlocksEl.getKeys();
			for(AbsenceNoticeToLectureBlock noticeToBlock:noticesToBlocks) {
				String lectureBlockKey = noticeToBlock.getLectureBlock().getKey().toString();
				if(lectureBlocksKeys.contains(lectureBlockKey)) {
					lectureBlocksEl.select(lectureBlockKey, true);
				}
			}
			
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
