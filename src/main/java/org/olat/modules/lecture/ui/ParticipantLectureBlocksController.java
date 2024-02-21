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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAppealStatus;
import org.olat.modules.lecture.LectureBlockAuditLog.Action;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.ui.ParticipantLectureBlocksDataModel.ParticipantCols;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LecturesCompulsoryRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Appeal button: after 5 day (coach can change the block) start, end after 15 day (appeal period)
 * 
 * Initial date: 28 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ParticipantLectureBlocksController extends FormBasicController {

	public static final String APPEAL_KEY = "appeal";
	public static final String ALL_TAB_ID = "showAll";
	public static final String WITH_COMMENT_TAB = "withComment";
	public static final String FILTER_WITH_COMMENT = "filter.with.comment";
	
	private FormLink openCourseButton;
	private FlexiTableElement tableEl;
	private ParticipantLectureBlocksDataModel tableModel;
	
	private final RepositoryEntry entry;
	private final boolean appealEnabled;
	
	private CloseableModalController cmc;
	private ContactFormController appealCtrl;
	
	private int count = 0;
	private final int appealOffset;
	private final int appealPeriod;
	private final boolean withPrint;
	private final boolean withAppeal;
	private final Identity assessedIdentity;
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	
	public ParticipantLectureBlocksController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, Identity assessedIdentity) {
		this(ureq, wControl, entry, assessedIdentity, true, true);
	}
	
	private ParticipantLectureBlocksController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, Identity assessedIdentity, boolean withPrint, boolean withAppeal) {
		super(ureq, wControl, "participant_blocks");
		this.entry = entry;
		this.withPrint = withPrint;
		this.withAppeal = withAppeal;
		this.assessedIdentity = assessedIdentity;
		appealEnabled = lectureModule.isAbsenceAppealEnabled();
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		
		appealOffset = lectureModule.getRollCallAutoClosePeriod();
		appealPeriod = lectureModule.getAbsenceAppealPeriod();
		initForm(ureq);
		loadModel();
		initFiltersPresets(ureq);
		initFilters();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			if(withPrint) {
				layoutCont.contextPut("winid", "w" + layoutCont.getFormItemComponent().getDispatchID());
				layoutCont.getFormItemComponent().addListener(this);
				layoutCont.getFormItemComponent().contextPut("withPrint", Boolean.TRUE);
				layoutCont.contextPut("title", StringHelper.escapeHtml(entry.getDisplayname()));

				// entry lifecycle date information
				initEntryLifecycleInformation(layoutCont);
				
				openCourseButton = uifactory.addFormLink("open.course", formLayout, Link.BUTTON);
				openCourseButton.setIconLeftCSS("o_icon o_CourseModule_icon");
			} else {
				layoutCont.contextPut("title", translate("lectures.repository.print.title", StringHelper.escapeHtml(entry.getDisplayname()),
						StringHelper.escapeHtml(userManager.getUserDisplayName(assessedIdentity))));
			}
			layoutCont.contextPut("authorizedAbsenceEnabled", authorizedAbsenceEnabled);
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.startTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.endTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.entry));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.lectureBlock));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.coach));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.plannedLectures, new LecturesCompulsoryRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.attendedLectures));
		if(authorizedAbsenceEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.unauthorizedAbsentLectures));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.authorizedAbsentLectures));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.absentLectures));
		}
	
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.status,
				new LectureBlockRollCallStatusCellRenderer(authorizedAbsenceEnabled, absenceDefaultAuthorized, getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.coachComment));

		if(appealEnabled && withAppeal && assessedIdentity.equals(getIdentity())) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(APPEAL_KEY, ParticipantCols.appeal.ordinal(), APPEAL_KEY,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(APPEAL_KEY), APPEAL_KEY), null)));
		}

		tableModel = new ParticipantLectureBlocksDataModel(columnsModel,
				authorizedAbsenceEnabled, absenceDefaultAuthorized, getLocale());
		int paging = withPrint ? 20 : -1;
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, paging, false, getTranslator(), formLayout);
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(ParticipantCols.date.name(), true));
		tableEl.setSortSettings(options);
		tableEl.setCustomizeColumns(withPrint);
		tableEl.setAndLoadPersistedPreferences(ureq, "participant-roll-call-appeal");
		tableEl.setEmptyTableMessageKey("empty.repository.entry.lectures");
		tableEl.setSearchEnabled(true);
	}
	
	private void loadModel() {
		Date now = new Date();
		Formatter formatter = Formatter.getInstance(getLocale());

		String separator = translate("user.fullname.separator");
		List<LectureBlockAndRollCall> rollCalls = lectureService.getParticipantLectureBlocks(entry, assessedIdentity, separator);
		List<LectureBlockAndRollCallRow> rows = new ArrayList<>(rollCalls.size());
		for(LectureBlockAndRollCall rollCall:rollCalls) {
			LectureBlockAndRollCallRow row = new LectureBlockAndRollCallRow(rollCall);

			// check if any filter is applied and if then skip this row, instead of adding it to the table
			if (tableEl.getSelectedFilterTab() != null
					&& tableEl.getSelectedFilterTab().getId().equals(WITH_COMMENT_TAB)
					&& row.getRow().getCoachComment() == null
					|| (!tableEl.getQuickSearchString().isBlank() && isExcludedBySearchString(tableEl.getQuickSearchString(), row.getRow()))
					|| (getFilterByComment().contains(translate(FILTER_WITH_COMMENT))
					&& row.getRow().getCoachComment() == null)) {
				continue;
			}

			if(appealEnabled && !LectureBlockStatus.cancelled.equals(row.getRow().getStatus())
					&& rollCall.isCompulsory()) {
				
				int lectures = row.getRow().getEffectiveLecturesNumber();
				if(lectures <= 0) {
					lectures = row.getRow().getPlannedLecturesNumber();
				}
				
				boolean absenceNotice = rollCall.hasAbsenceNotice();
				int attended = row.getRow().getLecturesAttendedNumber();
				if(!absenceNotice && attended < lectures) {
					Date date = row.getRow().getStartDate();
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal = CalendarUtils.getEndOfDay(cal);
					cal.add(Calendar.DATE, appealOffset);
					Date beginAppeal = CalendarUtils.removeTime(cal.getTime());
					cal.add(Calendar.DATE, appealPeriod);
					Date endAppeal = CalendarUtils.getEndOfDay(cal).getTime();
					
					Date sendAppealDate = null;
					LectureBlockAppealStatus appealStatus = null;
					if(row.getRow().getRollCallRef() != null ) {
						sendAppealDate = rollCall.getAppealDate();
						appealStatus = rollCall.getAppealStatus();
					}

					FormLink appealLink = null;
					if(sendAppealDate != null) {
						appealLink = getAppealedLink(beginAppeal, endAppeal, sendAppealDate, appealStatus, formatter);
					} else if(now.compareTo(beginAppeal) >= 0 && now.compareTo(endAppeal) <= 0) {
						appealLink = uifactory.addFormLink("appeal_" + count++, APPEAL_KEY, translate(APPEAL_KEY), null, flc, Link.LINK | Link.NONTRANSLATED);
						appealLink.setTitle(translate("appeal.tooltip", formatter.formatDate(beginAppeal), formatter.formatDate(endAppeal)));
						appealLink.setUserObject(row);
						//appeal
					} else if(now.compareTo(endAppeal) > 0) {
						// appeal closed
						appealLink = uifactory.addFormLink("appeal_" + count++, "aclosed", "appeal.closed", null, flc, Link.LINK);
						appealLink.setEnabled(false);
						appealLink.setDomReplacementWrapperRequired(false);
					} else if(now.compareTo(date) >= 0) {
						// appeal at
						String appealFrom = translate("appeal.from", formatter.formatDate(beginAppeal));
						appealLink = uifactory.addFormLink("appeal_" + count++, "appealat", appealFrom, null, flc, Link.LINK | Link.NONTRANSLATED);
						appealLink.setEnabled(false);
						appealLink.setDomReplacementWrapperRequired(false);
					}
					row.setAppealButton(appealLink);
				}
			}
			rows.add(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void initEntryLifecycleInformation(FormLayoutContainer layoutCont) {
		// is lifecycle available at all?
		if (entry.getLifecycle() != null) {
			Formatter formatter = Formatter.getInstance(getLocale());
			// start- & endDate is available
			if (entry.getLifecycle().getValidFrom() != null
					&& entry.getLifecycle().getValidTo() != null) {
				String startDate = formatter.formatDate(entry.getLifecycle().getValidFrom());
				String endDate = formatter.formatDate(entry.getLifecycle().getValidTo());
				String startDayOfWeek = "";
				String endDayOfWeek = "";
				startDayOfWeek = formatter.dayOfWeek(entry.getLifecycle().getValidFrom());
				endDayOfWeek = formatter.dayOfWeek(entry.getLifecycle().getValidTo());

				String[] args = new String[] {
						startDate,					// 0
						endDate,					// 1
						startDayOfWeek,				// 2
						endDayOfWeek				// 3
				};

				layoutCont.contextPut("dateAndTime", translate("lecture.block.dateAndTime.lifecycle.full", args));
			} else {
				String date = "";
				String dayOfWeek = "";
				// preText is the information if it is a start- or endDate, e.g. Begin or End
				String preText = "";
				if (entry.getLifecycle().getValidFrom() != null) {
					// only startDate is available
					date = formatter.formatDate(entry.getLifecycle().getValidFrom());
					dayOfWeek = formatter.dayOfWeek(entry.getLifecycle().getValidFrom());
					preText = translate("lecture.start");
				} else if (entry.getLifecycle().getValidTo() != null) {
					// only endDate is available
					date = formatter.formatDate(entry.getLifecycle().getValidTo());
					dayOfWeek = formatter.dayOfWeek(entry.getLifecycle().getValidTo());
					preText = translate("lecture.end");
				}
				String[] args = new String[] {
						date,					// 0
						dayOfWeek,				// 1
				};
				layoutCont.contextPut("dateAndTime", preText + ": " + translate("lecture.block.dateAndTime.lifecycle.single", args));
			}
		}
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		if (!tableModel.getObjects().isEmpty()) {
			SelectionValues columnValues = new SelectionValues();

			columnValues.add(SelectionValues.entry(translate(FILTER_WITH_COMMENT), translate(FILTER_WITH_COMMENT)));

			FlexiTableMultiSelectionFilter commentFilter = new FlexiTableMultiSelectionFilter(translate("table.header.comment"),
					"comment", columnValues, true);
			filters.add(commentFilter);
		}

		tableEl.setFilters(true, filters, false, false);
		tableEl.expandFilters(true);
	}

	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		// filter: show all
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.showAll"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);

		// filter: show only rows with coachComments
		FlexiFiltersTab withCommentTab = FlexiFiltersTabFactory.tabWithFilters(WITH_COMMENT_TAB, translate(FILTER_WITH_COMMENT),
				TabSelectionBehavior.clear, List.of());
		withCommentTab.setFiltersExpanded(true);
		tabs.add(withCommentTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	private boolean isExcludedBySearchString(String searchString, LectureBlockAndRollCall row) {
		boolean excluded = true;

		// compare searchString with name of course (display name)
		if (row.getEntryDisplayname().toLowerCase().contains(searchString.toLowerCase())) {
			excluded = false;
		}
		// compare searchString with lecture block title
		else if (row.getLectureBlockTitle().toLowerCase().contains(searchString.toLowerCase()))
			excluded = false;
		// compare searchString with coach name (if it exists)
		else if (row.getCoach() != null && row.getCoach().toLowerCase().contains(searchString.toLowerCase()))
			excluded = false;
		// compare searchString with comment string from coach (if it exists)
		else if (row.getCoachComment() != null && row.getCoachComment().toLowerCase().contains(searchString.toLowerCase()))
			excluded = false;


		return excluded;
	}

	/**
	 * currently there is only one filter (comment)
	 * But this method can also be used later if more filters will be added, just needs to be modified with FlexiTableFilter
	 * @return list of selected filterValues
	 */
	private List<String> getFilterByComment() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter commentFilter = FlexiTableFilter.getFilter(filters, "comment");
		if (commentFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter) commentFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				return filterValues;
			}
		}
		return Collections.emptyList();
	}
	
	private FormLink getAppealedLink(Date beginAppeal, Date endAppeal, Date sendAppealDate,
			LectureBlockAppealStatus status, Formatter formatter) {
		String i18nKey;
		if(status == LectureBlockAppealStatus.oldWorkflow) {
			i18nKey = "appeal.sent";
		} else if(status == LectureBlockAppealStatus.pending) {
			i18nKey = "appeal.pending";
		} else if(status == LectureBlockAppealStatus.rejected) {
			i18nKey = "appeal.rejected";
		} else if(status == LectureBlockAppealStatus.approved) {
			i18nKey = "appeal.approved";
		} else {
			i18nKey = "appeal.sent";
		}
		String appealFrom = translate(i18nKey, formatter.formatDate(sendAppealDate));
		FormLink appealLink = uifactory.addFormLink("appeal_" + count++, "appealsend", appealFrom, null, flc, Link.LINK | Link.NONTRANSLATED);
		appealLink.setTitle(translate("appeal.sent.tooltip", formatter.formatDate(sendAppealDate), formatter.formatDate(beginAppeal), formatter.formatDate(endAppeal)));
		appealLink.setEnabled(false);
		appealLink.setDomReplacementWrapperRequired(false);
		return appealLink;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(flc.getFormItemComponent() == source && "print".equals(event.getCommand())) {
			doPrint(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(appealCtrl == source) {
			if(event == Event.DONE_EVENT) {
				LectureBlockAndRollCall row = (LectureBlockAndRollCall)appealCtrl.getUserObject();
				String body = appealCtrl.getBody();
				doAppealAudit(row, body);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(appealCtrl);
		removeAsListenerAndDispose(cmc);
		appealCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				LectureBlockAndRollCallRow row = tableModel.getObject(se.getIndex());
				if(APPEAL_KEY.equals(cmd)) {
					doAppeal(ureq, row.getRow());
				}
			}
			if (event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}
		} else if(openCourseButton == source) {
			doOpenCourse(ureq);
		} else if(source instanceof FormLink link) {
			if(APPEAL_KEY.equals(link.getCmd())) {
				LectureBlockAndRollCallRow row = (LectureBlockAndRollCallRow)link.getUserObject();
				doAppeal(ureq, row.getRow());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAppeal(UserRequest ureq, LectureBlockAndRollCall row) {
		if(guardModalController(appealCtrl)) return;
		
		LectureBlock block = lectureService.getLectureBlock(row.getLectureBlockRef());
		List<Identity> teachers = lectureService.getTeachers(block);
		List<Identity> owners = repositoryService.getMembers(entry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.owner.name());
		
		ContactList contactList = new ContactList(translate("appeal.contact.list"));
		contactList.addAllIdentites(teachers);
		contactList.addAllIdentites(owners);
		
		StringBuilder teacherNames = new StringBuilder();
		for(Identity teacher:teachers) {
			if(!teacherNames.isEmpty()) teacherNames.append(", ");
			teacherNames.append(teacher.getUser().getFirstName()).append(" ").append(teacher.getUser().getLastName());
		}
		String date = Formatter.getInstance(getLocale()).formatDate(block.getStartDate());
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "][LectureBlock:" + block.getKey() + "]";
		String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);	
		String[] args = new String[] {
			row.getLectureBlockTitle(),
			teacherNames.toString(),
			date,
			url
		};

		StringBuilder body = new StringBuilder(1024);
		body.append(translate("appeal.body.title", args))
		    .append(translate("appeal.body", args));
		
		ContactMessage cmsg = new ContactMessage(getIdentity());
		cmsg.addEmailTo(contactList);
		cmsg.setSubject(translate("appeal.subject", args));
		cmsg.setBodyText(body.toString());
		appealCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		appealCtrl.setUserObject(row);
		appealCtrl.setContactFormTitle(translate("new.appeal.title"));
		listenTo(appealCtrl);
		
		String title = translate("appeal.title", row.getLectureBlockTitle());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), appealCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAppealAudit(LectureBlockAndRollCall row, String message) {
		logAudit("Appeal send for lecture block: " + row.getLectureBlockTitle() + " (" + row.getLectureBlockRef().getKey() + ")");
		
		LectureBlock lectureBlock = lectureService.getLectureBlock(row.getLectureBlockRef());
		LectureBlockRollCall rollCall = lectureService.getRollCall(row.getRollCallRef());
		if(rollCall == null) {
			rollCall = lectureService.getOrCreateRollCall(assessedIdentity, lectureBlock, null, null, null);
		}
		
		String before = lectureService.toAuditXml(rollCall);
		rollCall.setAppealDate(new Date());
		rollCall.setAppealStatus(LectureBlockAppealStatus.pending);
		rollCall.setAppealReason(message);
		rollCall = lectureService.updateRollCall(rollCall);
		String after = lectureService.toAuditXml(rollCall);
		lectureService.auditLog(Action.sendAppeal, before, after, message, lectureBlock, rollCall, entry, assessedIdentity, null);
		dbInstance.commit();
		loadModel();
		tableEl.reset(false, false, true);
	}
	
	private void doPrint(UserRequest ureq) {
		ControllerCreator printControllerCreator = (lureq, lwControl) -> {
			lwControl.getWindowBackOffice().getChiefController().addBodyCssClass("o_lectures_print");
			Controller printCtrl = new ParticipantLectureBlocksController(lureq, lwControl, entry, assessedIdentity, false, false);
			listenTo(printCtrl);
			return printCtrl;
		};				
		ControllerCreator layoutCtrl = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(printControllerCreator);
		openInNewBrowserWindow(ureq, layoutCtrl, true);
	}
	
	private void doOpenCourse(UserRequest ureq) {
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "][Infos]";
		NewControllerFactory.getInstance().	launch(businessPath, ureq, getWindowControl());
	}
}
