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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.DailyRollCall;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.LectureBlockBlockStatistics;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.TeacherOverviewDataModel.TeachCols;
import org.olat.modules.lecture.ui.coach.DailyLectureBlockTableModel.BlockCols;
import org.olat.modules.lecture.ui.component.IdentityComparator;
import org.olat.modules.lecture.ui.component.LectureBlockAbsenceAlertCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockAbsenceWarningCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockTimesCellRenderer;
import org.olat.modules.lecture.ui.event.OpenRepositoryEntryEvent;
import org.olat.modules.lecture.ui.event.RollCallEvent;
import org.olat.modules.lecture.ui.export.LectureBlockExport;
import org.olat.modules.lecture.ui.export.LecturesBlockPDFExport;
import org.olat.modules.lecture.ui.export.LecturesBlockSignaturePDFExport;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This view is for teachers or students. It presents a tree table
 * view of lectures.
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DailyLectureBlockOverviewController extends FormBasicController {
	
	private FormLink closeButton;
	private FlexiTableElement tableEl;
	private DailyLectureBlockTableModel tableModel;
	
	private int counter = 0;
	private Date currentDate;
	private final Formatter formatter;
	private final boolean authorizedAbsenceEnabled;
	private final boolean dailyRecordingEnabled;
	private final Identity profiledIdentity;
	private final LecturesSecurityCallback secCallback;
	private final RollCallSecurityCallback rollCallSecCallback;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private CloseLecturesController closeLecturesCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public DailyLectureBlockOverviewController(UserRequest ureq, WindowControl wControl, Date currentDate, Identity profiledIdentity, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "lectureblocks_daily_overview", Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		
		this.currentDate = currentDate;
		this.secCallback = secCallback;
		boolean teacher = secCallback.viewAs() == LectureRoles.teacher;
		boolean masterCoach = secCallback.viewAs() == LectureRoles.mastercoach;
		rollCallSecCallback = new RollCallSecurityCallbackImpl(false, masterCoach, teacher, null, lectureModule);
		this.profiledIdentity = profiledIdentity;
		formatter = Formatter.getInstance(getLocale());
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		dailyRecordingEnabled = lectureModule.getDailyRollCall() == DailyRollCall.daily;
		
		initForm(ureq);
		loadModel();
		updateCanClose();
	}
	
	public Date getCurrentDate() {
		return currentDate;
	}
	
	public void setCurrentDate(Date date) {
		this.currentDate = date;
		if(closeButton != null) {
			String title = translate("close.lecture.blocks.day", new String[] { formatter.formatDate(date) });
			closeButton.setI18nKey(title);
		}
		
		loadModel();
		updateCanClose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer(new LectureBlockTimesCellRenderer(true, getLocale()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.times, treeNodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.entry, "open.course"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.lectureBlock));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.location));
		DefaultFlexiColumnModel participantsCol = new DefaultFlexiColumnModel(BlockCols.numOfParticipants);
		participantsCol.setIconHeader("o_icon o_icon_number_of");
		columnsModel.addFlexiColumnModel(participantsCol);
		DefaultFlexiColumnModel presenceCol = new DefaultFlexiColumnModel(BlockCols.numOfPresences);
		presenceCol.setIconHeader("o_icon o_icon_presence");
		columnsModel.addFlexiColumnModel(presenceCol);
		DefaultFlexiColumnModel absenceCol = new DefaultFlexiColumnModel(BlockCols.numOfAbsences);
		absenceCol.setIconHeader("o_icon o_icon_absence");
		columnsModel.addFlexiColumnModel(absenceCol);
		DefaultFlexiColumnModel warningCol = new DefaultFlexiColumnModel(BlockCols.warnings, new LectureBlockAbsenceWarningCellRenderer(getTranslator()));
		warningCol.setIconHeader("o_icon o_absences_col_warning");
		columnsModel.addFlexiColumnModel(warningCol);
		DefaultFlexiColumnModel alertCol = new DefaultFlexiColumnModel(BlockCols.alerts, new LectureBlockAbsenceAlertCellRenderer());
		alertCol.setIconHeader("o_icon o_absences_col_alert");
		columnsModel.addFlexiColumnModel(alertCol);
		DefaultFlexiColumnModel detailsCol = new DefaultFlexiColumnModel(BlockCols.details.i18nHeaderKey(), BlockCols.details.ordinal(), "details",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.details"), "details"), null));
		// set sort key even though we do not sort - added as css classes to column headers for styling
		detailsCol.setSortKey(TeachCols.details.name());
		columnsModel.addFlexiColumnModel(detailsCol);
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(BlockCols.tools);

		toolsCol.setSortable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new DailyLectureBlockTableModel(columnsModel, getLocale(), dailyRecordingEnabled);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(BlockCols.times.name(), true));
		tableEl.setSortSettings(sortOptions);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setEmptyTableMessageKey("cockpit.lectures.day.list");
		tableEl.setAndLoadPersistedPreferences(ureq, "daily-lecture-blocks-overview-v2");
		
		initCloseButton(formLayout);
	}
	
	private void initCloseButton(FormItemContainer formLayout) {
		if(secCallback.viewAs() == LectureRoles.teacher) {
			String title = translate("close.lecture.blocks.day", new String[] { formatter.formatDate(getCurrentDate()) });
			closeButton = uifactory.addFormLink("close", title, null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			closeButton.setVisible(false);
		}
	}
	
	private void updateCanClose() {
		if(closeButton == null) return;
		
		boolean canClose = lectureModule.getDailyRollCall() == DailyRollCall.daily;
		if(canClose) {
			List<LectureBlock> blocks = tableModel.getLectureBlocks();
			canClose &= rollCallSecCallback.canClose(blocks);
		}
		closeButton.setVisible(canClose);
	}
	
	protected void loadModel() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setStartDate(CalendarUtils.startOfDay(currentDate));
		searchParams.setEndDate(CalendarUtils.endOfDay(currentDate));
		
		boolean iamTeacher = secCallback.viewAs() == LectureRoles.teacher;
		searchParams.setViewAs(getIdentity(), secCallback.viewAs());
		if(profiledIdentity != null) {
			searchParams.setParticipant(profiledIdentity);
		}
		
		List<LectureBlockBlockStatistics> blocks;
		if(searchParams.getParticipant() == null && searchParams.getTeacher() == null) {
			blocks = new ArrayList<>();// it's only for students and teachers
		} else {
			blocks = lectureService.getLectureBlocksStatistics(searchParams);
		}
		List<DailyLectureBlockRow> rows = new ArrayList<>(blocks.size());
		if(!blocks.isEmpty()) {
			DailyLectureBlockRow todayRow = new DailyLectureBlockRow(getRootLabel(),
					CalendarUtils.startOfDay(currentDate), CalendarUtils.endOfDay(currentDate));
			rows.add(todayRow);
			
			DailyLectureBlockRow morningRow = getSeparator("morning", 0, 11, todayRow);
			DailyLectureBlockRow afternoonRow = getSeparator("afternoon", 12, 18, todayRow);
			DailyLectureBlockRow eveningRow = getSeparator("evening", 19, 23, todayRow);

			for(LectureBlockBlockStatistics block:blocks) {
				DailyLectureBlockRow parentRow = getParent(block.getLectureBlock(), morningRow, afternoonRow, eveningRow);
				rows.add(forgeRow(block, iamTeacher, parentRow));
			}
			
			if(morningRow.getNumOfChildren() > 0) {
				rows.add(morningRow);
			}
			if(afternoonRow.getNumOfChildren() > 0) {
				rows.add(afternoonRow);
			}
			if(eveningRow.getNumOfChildren() > 0) {
				rows.add(eveningRow);
			}
		}

		Collections.sort(rows, new DailyLectureBlockRowTreeComparator());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private DailyLectureBlockRow getSeparator(String i18nKey, int fromHour, int toHour, DailyLectureBlockRow parent) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.set(Calendar.HOUR_OF_DAY, fromHour);
		removeMinutes(cal);
		Date startDate = cal.getTime();
		
		cal.set(Calendar.HOUR_OF_DAY, toHour);
		fillMinutes(cal);
		Date endDate = cal.getTime();
		return new DailyLectureBlockRow(translate(i18nKey), parent, startDate, endDate);
	}
	
	private void fillMinutes(Calendar cal) { 
		cal.set(Calendar.MINUTE, 59);  
		cal.set(Calendar.SECOND, 59);  
		cal.set(Calendar.MILLISECOND, 999);  
	}
	
	private void removeMinutes(Calendar cal) { 
		cal.set(Calendar.MINUTE, 0);  
		cal.set(Calendar.SECOND, 0);  
		cal.set(Calendar.MILLISECOND, 0);  
	}
	
	private DailyLectureBlockRow getParent(LectureBlock lectureRow,
			DailyLectureBlockRow morningRow, DailyLectureBlockRow afternoonRow, DailyLectureBlockRow eveningRow) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(lectureRow.getStartDate());
		int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
		if(hourOfDay < 12) {
			morningRow.incrementNumOfChildren();
			return morningRow;
		} else if(hourOfDay < 18) {
			afternoonRow.incrementNumOfChildren();
			return afternoonRow;
		}
		eveningRow.incrementNumOfChildren();
		return eveningRow;
	}
	
	private String getRootLabel() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		
		String dayLabel;
		if(CalendarUtils.isToday(currentDate)) {
			dayLabel = translate("today");
		} else {
			dayLabel = Formatter.getInstance(getLocale()).formatDate(currentDate);
		}
		return dayLabel;
	}
	
	private DailyLectureBlockRow forgeRow(LectureBlockBlockStatistics block, boolean iamTeacher, DailyLectureBlockRow parentRow) {
		DailyLectureBlockRow row = new DailyLectureBlockRow(block, iamTeacher);
		row.setParent(parentRow);
		
		String linkName = "tools-" + counter++;
		FormLink toolsLink = uifactory.addFormLink(linkName, "tools", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setUserObject(row);
		flc.add(linkName, toolsLink);
		row.setTools(toolsLink);
		
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCalloutCtrl == source) {
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if(closeLecturesCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doCloseLectures();
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(closeLecturesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		closeLecturesCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("details".equals(cmd)) {
					List<LectureBlock> lectureBlocks = tableModel.getLectureBlocks(se.getIndex());
					fireEvent(ureq, new RollCallEvent(RollCallEvent.WORK_ON_ROLL_CALL, lectureBlocks));
				} else if("open.course".equals(cmd)) {
					DailyLectureBlockRow row = tableModel.getObject(se.getIndex());
					doOpenCourseLectures(ureq, row);
				}
			}
		} else if(source == closeButton) {
			doConfirmCloseLectures(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd())) {
				DailyLectureBlockRow row = (DailyLectureBlockRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doOpenCourseLectures(UserRequest ureq, DailyLectureBlockRow row) {
		fireEvent(ureq, new OpenRepositoryEntryEvent(row.getEntry()));
	}
	
	private void doOpenTools(UserRequest ureq, DailyLectureBlockRow row, FormLink link) {
		if(toolsCtrl != null) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		LectureBlock block = lectureService.getLectureBlock(row.getLectureBlock());
		if(block == null) {
			tableEl.reloadData();
			showWarning("lecture.blocks.not.existing");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doExportLectureBlock(UserRequest ureq, LectureBlock block) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(block);
		List<Identity> teachers = lectureService.getTeachers(lectureBlock);
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		LectureBlockExport export = new LectureBlockExport(lectureBlock, teachers, isAdministrativeUser, authorizedAbsenceEnabled, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
	
	private void doExportAttendanceListForSignature(UserRequest ureq, LectureBlock block) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(block);
		List<Identity> participants = lectureService.getParticipants(lectureBlock);
		if(participants.size() > 1) {
			Collections.sort(participants, new IdentityComparator(getLocale()));
		}
		try {
			LecturesBlockSignaturePDFExport export = new LecturesBlockSignaturePDFExport(lectureBlock, getTranslator());
			export.setTeacher(userManager.getUserDisplayName(getIdentity()));
			export.create(participants);
			ureq.getDispatchResult().setResultingMediaResource(export);
		} catch (IOException | TransformerException e) {
			logError("", e);
		}
	}
	
	private void doExportAttendanceList(UserRequest ureq, LectureBlock block) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(block);
		List<Identity> participants = lectureService.getParticipants(lectureBlock);
		if(participants.size() > 1) {
			Collections.sort(participants, new IdentityComparator(getLocale()));
		}
		List<LectureBlockRollCall> rollCalls = lectureService.getRollCalls(block);
		List<AbsenceNotice> notices = lectureService.getAbsenceNoticeRelatedTo(lectureBlock);
		try {
			LecturesBlockPDFExport export = new LecturesBlockPDFExport(lectureBlock, authorizedAbsenceEnabled, getTranslator());
			export.setTeacher(userManager.getUserDisplayName(getIdentity()));
			export.create(participants, rollCalls, notices);
			ureq.getDispatchResult().setResultingMediaResource(export);
		} catch (IOException | TransformerException e) {
			logError("", e);
		}
	}
	
	private void doConfirmCloseLectures(UserRequest ureq) {
		int numOfLectures = tableModel.getLectureBlocks().size();
		closeLecturesCtrl = new CloseLecturesController(ureq, getWindowControl(), numOfLectures, getCurrentDate());
		listenTo(closeLecturesCtrl);

		String title = translate("close.lecture.blocks");
		cmc = new CloseableModalController(getWindowControl(), "close", closeLecturesCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCloseLectures() {
		List<LectureBlock> blocks = tableModel.getLectureBlocks();
		lectureService.saveDefaultRollCalls(blocks, getIdentity(), true);
	}

	private class ToolsController extends BasicController {
		
		private final DailyLectureBlockRow row;

		public ToolsController(UserRequest ureq, WindowControl wControl, DailyLectureBlockRow row) {
			super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			addLink("export", "export", "o_icon o_filetype_xlsx", mainVC);
			addLink("attendance.list", "attendance.list", "o_icon o_filetype_pdf", mainVC);
			addLink("attendance.list.to.sign", "attendance.list.to.sign", "o_icon o_filetype_pdf", mainVC);
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, VelocityContainer mainVC) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if("export".equals(cmd)) {
					LectureBlock block = lectureService.getLectureBlock(row);
					doExportLectureBlock(ureq, block);
				} else if("attendance.list.to.sign".equals(cmd)) {
					LectureBlock block = lectureService.getLectureBlock(row);
					doExportAttendanceListForSignature(ureq, block);
				} else if("attendance.list".equals(cmd)) {
					LectureBlock block = lectureService.getLectureBlock(row);
					doExportAttendanceList(ureq, block);
				}
			}
		}
	}
}
