/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.daynav.DayNavElement;
import org.olat.core.gui.components.daynav.DayNavFactory;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.DashboardUIFactory;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer.LectureBlockVirtualStatus;
import org.olat.modules.lecture.ui.component.LectureBlocksWidgetRenderer;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementModule;
import org.olat.modules.roommanagement.RoomManagementService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 8, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class LectureBlocksWidgetController extends TableWidgetController implements FlexiTableComponentDelegate {

	private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d{1,2})\\D(\\d{2})\\W*(.*)$");
	
	private final Formatter formatter;
	protected DayNavElement dayNavEl;
	protected LectureBlocksWidgetTableModel dataModel;
	protected FlexiTableElement tableEl;
	private FormLayoutContainer emptyCont;
	protected FormLink backButton;
	protected FormLink forwardButton;
	protected FormLink showAllLink;

	@Autowired
	private RoomManagementModule roomManagementModule;
	@Autowired
	private RoomManagementService roomManagementService;

	public LectureBlocksWidgetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LectureBlocksWidgetController.class, ureq.getLocale(), getTranslator()));
		formatter = Formatter.getInstance(getLocale());
	}

	@Override
	protected String getTitle() {
		return "<i class=\"o_icon o_icon_calendar_day\"> </i> " + translate("dashboard.widget.title");
	}

	@Override
	protected String createTopArea(FormLayoutContainer widgetCont) {
		dayNavEl = DayNavFactory.createElement("day.nav", widgetCont);
		return dayNavEl.getComponent().getComponentName();
	}
	
	@Override
	protected String createHeaderTools(FormLayoutContainer widgetCont) {
		return dayNavEl.getWeekComponent().getComponentName();
	}

	@Override
	protected String createTable(FormLayoutContainer widgetCont) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		dataModel = new LectureBlocksWidgetTableModel(tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 5000, true, getTranslator(), widgetCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.external);
		tableEl.setRendererType(FlexiTableRendererType.external);
		tableEl.setExternalRenderer(new LectureBlocksWidgetRenderer(), "");
		setVelocityRoot(Util.getPackageVelocityRoot(LectureBlocksWidgetController.class));
		VelocityContainer rowVC = createVelocityContainer("lecture_block_widget_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		tableEl.setCssDelegate(TableCssDelegate.DELEGATE);
		
		return tableEl.getComponent().getComponentName();
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if (rowObject instanceof LectureBlocksWidgetRow widgetRow) {
			cmps.add(widgetRow.getTitleLink().getComponent());
			if (widgetRow.getRollCallLink() != null) {
				cmps.add(widgetRow.getRollCallLink().getComponent());
			}
		}
		return cmps;
	}
	
	@Override
	protected String createEmptyState(FormLayoutContainer widgetCont) {
		String page = velocity_root + "/lecture_block_widget_empty.html";
		emptyCont = FormLayoutContainer.createCustomFormLayout("offers", getTranslator(), page);
		emptyCont.setRootForm(mainForm);
		widgetCont.add(emptyCont);
		
		EmptyState emptyState = EmptyStateFactory.create("empty", emptyCont.getFormItemComponent(), this);
		emptyState.setMessageTranslated(translate("dashboard.widget.empty.message"));
		emptyState.setIconCss("o_icon_calendar_day");
		
		backButton = uifactory.addFormLink("back", "dashboard.widget.previous.event", null, emptyCont, Link.BUTTON);
		backButton.setIconLeftCSS("o_icon o_icon-lg o_icon_course_previous");
		backButton.setGhost(true);
		
		forwardButton = uifactory.addFormLink("forward", "dashboard.widget.next.event", null,emptyCont, Link.BUTTON);
		forwardButton.setIconRightCSS("o_icon o_icon-lg o_icon_course_next");
		forwardButton.setGhost(true);

		
		return emptyCont.getName();
	}

	@Override
	protected List<String> createFooterButtons(FormLayoutContainer widgetCont) {
		showAllLink = DashboardUIFactory.createShowAllLink(widgetCont);
		return List.of(showAllLink.getComponent().getComponentName());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == dayNavEl) {
			loadModel();
		} else if (source == backButton && backButton.getUserObject() instanceof Date targetDate) {
			doGoto(targetDate);
		} else if (source == forwardButton && forwardButton.getUserObject() instanceof Date targetDate) {
			doGoto(targetDate);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doGoto(Date targetDate) {
		dayNavEl.setStartDate(DateUtils.getPreviousDay(DateUtils.addDays(targetDate, 1), DayOfWeek.MONDAY));
		dayNavEl.setSelectedDate(targetDate);
		loadModel();
	}

	public void reload() {
		loadModel();
	}

	protected void loadModel() {
		Date weekStart = DateUtils.getStartOfDay(dayNavEl.getStartDate());
		Date toDate = DateUtils.getEndOfDay(dayNavEl.getEndDate());
		Date selStart = DateUtils.getStartOfDay(dayNavEl.getSelectedDate());
		List<LectureBlock> allWeekBlocks = loadLectureBlocks(weekStart, toDate);
		LectureBlockRef nextScheduledBlock = loadNextScheduledBlock();

		boolean[] markedDays = new boolean[7];
		List<LectureBlock> visible = new ArrayList<>(allWeekBlocks.size());
		for (LectureBlock b : allWeekBlocks) {
			int idx = (int) DateUtils.countDays(weekStart, DateUtils.getStartOfDay(b.getStartDate()));
			if (idx >= 0 && idx < 7) {
				markedDays[idx] = true;
			}
			if (!b.getStartDate().before(selStart)) {
				visible.add(b);
			}
		}
		dayNavEl.setMarkedDays(markedDays);

		updateTable(visible, nextScheduledBlock);
		
		String i18nKey = allWeekBlocks.size() == 1 ? "dashboard.widget.footer.event" : "dashboard.widget.footer.events";
		setFooterText(translate(i18nKey, String.valueOf(allWeekBlocks.size()), String.valueOf(dayNavEl.getWeekOfYear())));
	}
	
	protected abstract List<LectureBlock> loadLectureBlocks(Date fromDate, Date toDate);

	protected abstract LectureBlockRef loadNextScheduledBlock();

	private Map<Long, String> loadRooms(List<LectureBlock> lectureBlocks) {
		if (!roomManagementModule.isEnabled() || lectureBlocks.isEmpty()) {
			return Map.of();
		}

		List<Long> keys = lectureBlocks.stream().map(LectureBlock::getKey).toList();
		return roomManagementService.getBookings(keys).stream()
				.map(booking -> Map.entry(booking.getLectureBlock().getKey(), getRoomName(booking)))
				.filter(entry -> StringHelper.containsNonWhitespace(entry.getValue()))
				.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.joining(", "))));
	}

	private String getRoomName(RoomBooking booking) {
		Room room = booking.getRoom();
		return StringHelper.containsNonWhitespace(room.getExternalRef())? room.getExternalRef(): room.getDescription();
	}

	private String getLocation(LectureBlock lectureBlock, boolean onlineMeeting) {
		if (StringHelper.containsNonWhitespace(lectureBlock.getLocation())) {
			return lectureBlock.getLocation();
		}
		if (onlineMeeting) {
			return StringHelper.containsNonWhitespace(lectureBlock.getMeetingTitle())
					? lectureBlock.getMeetingTitle()
					: translate("lecture.online.meeting");
		}
		return null;
	}

	private void updateTable(List<LectureBlock> lectureBlocks, LectureBlockRef nextScheduledBlock) {
		lectureBlocks.sort(Comparator.comparing(LectureBlock::getStartDate));
		
		Date currentDay = null;
		Date selectedDate = DateUtils.getStartOfDay(dayNavEl.getSelectedDate());
		String scrollToId = null;
		Map<Long, String> roomsByBlock = loadRooms(lectureBlocks);
		List<LectureBlocksWidgetRow> rows = new ArrayList<>(lectureBlocks.size());
		for (LectureBlock lectureBlock : lectureBlocks) {
			LectureBlocksWidgetRow row = new LectureBlocksWidgetRow();
			row.setKey(lectureBlock.getKey());
			row.setTitle(lectureBlock.getTitle());
			boolean onlineMeeting = lectureBlock.getBBBMeeting() != null || lectureBlock.getTeamsMeeting() != null
					|| StringHelper.containsNonWhitespace(lectureBlock.getMeetingUrl());
			row.setOnlineMeeting(onlineMeeting);
			row.setLocation(getLocation(lectureBlock, onlineMeeting));
			row.setRooms(roomsByBlock.get(lectureBlock.getKey()));
			row.setTime(formatTime(lectureBlock));
			
			if (!DateUtils.isSameDay(currentDay, lectureBlock.getStartDate())) {
				currentDay = lectureBlock.getStartDate();
				row.setDayAbbr(formatter.dayOfWeekShort(currentDay));
				row.setDay(String.valueOf(DateUtils.dayFromDate(currentDay)));
			}
			if (selectedDate != null && scrollToId == null) {
				if (lectureBlock.getStartDate().after(selectedDate)) {
					scrollToId = "o_" + CodeHelper.getRAMUniqueID();
					row.setId(scrollToId);
				}
			}
			
			LectureBlockVirtualStatus vStatus = LectureBlockStatusCellRenderer.calculateStatus(lectureBlock);
			boolean nextScheduled = nextScheduledBlock != null && nextScheduledBlock.getKey().equals(lectureBlock.getKey());
			
			String statusCss = "";
			String statusText = null;
			if (nextScheduled && vStatus != LectureBlockVirtualStatus.RUNNING) {
				statusText = translate("next");
				statusCss = "o_lecture_widget_status_next";
			} else if (vStatus == LectureBlockVirtualStatus.RUNNING) {
				statusText = translate("running");
				statusCss = "o_lecture_widget_status_running";
			}
			row.setStatusText(statusText);
			row.setStatusCss(statusCss);
			
			row.setTitleLink(createTitleLink(lectureBlock, row));
			row.setRollCallLink(createRollCallLink(lectureBlock));
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		if (scrollToId != null) {
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.scrollInContainer(scrollToId, ".o_table_body", "instant"));
		}
		
		// Redraw the widget only when it switches between the table and the empty state
		if (rows.isEmpty() == tableEl.isVisible()) {
			flc.setDirty(true);
		}
		if (rows.isEmpty()) {
			tableEl.setVisible(false);
			emptyCont.setVisible(true);
			updateEmptyButtonUI();
		} else {
			tableEl.setVisible(true);
			emptyCont.setVisible(false);
		}
	}
	
	public String formatTime(LectureBlock lectureBlock) {
		String time = formatter.formatTimeShort(lectureBlock.getStartDate());
		StringBuilder sb = new StringBuilder();
		sb.append("<span class='sr-only'>").append(formatter.formatDateAndTime(lectureBlock.getStartDate())).append("</span>");
		sb.append("<span aria-hidden='true'>");
		Matcher matcher = TIME_PATTERN.matcher(time);
		if (matcher.matches()) {
			sb.append("<span class='o_lecture_time_hour'>").append(matcher.group(1)).append("</span>");
			sb.append("<span class='o_lecture_time_minute'>").append(matcher.group(2)).append("</span>");
			if (StringHelper.containsNonWhitespace(matcher.group(3))) {
				sb.append("<span class='o_lecture_time_ampm'>").append(matcher.group(3)).append("</span>");
			}
		} else {
			sb.append("<span class='o_lecture_time_hour'>").append(time).append("</span>");
		}
		sb.append("</span>");
		return sb.toString();
	}
	
	private void updateEmptyButtonUI() {
		Date prevLectureBlock = getPrevLectureBlock(dayNavEl.getSelectedDate());
		backButton.setUserObject(prevLectureBlock);
		backButton.setEnabled(backButton.getUserObject() != null);
		Date nextLectureBlock = getNextLectureBlock(dayNavEl.getSelectedDate());
		forwardButton.setUserObject(nextLectureBlock);
		forwardButton.setEnabled(forwardButton.getUserObject() != null);
	}

	protected abstract Date getPrevLectureBlock(Date date);

	protected abstract Date getNextLectureBlock(Date date);

	/**
	 * @param link
	 * @param lectureBlock
	 */
	protected void setLectureBlockUrl(FormLink link, LectureBlock lectureBlock) {
		//
	}

	private FormLink createTitleLink(LectureBlock lectureBlock, LectureBlocksWidgetRow row) {
		VelocityContainer linkVC = createVelocityContainer("lecture_block_widget_link");
		linkVC.setDomReplacementWrapperRequired(false);
		linkVC.contextPut("row", row);
		
		FormLink titleLink = uifactory.addFormLink("title_" + lectureBlock.getKey(), "open", "", tableEl, Link.NONTRANSLATED);
		titleLink.setInnerComponent(linkVC);
		titleLink.setElementCssClass("o_link_plain o_row_select");
		setLectureBlockUrl(titleLink, lectureBlock);
		return titleLink;
	}
	
	/**
	 * @param lectureBlock  
	 */
	protected FormLink createRollCallLink(LectureBlock lectureBlock) {
		return null;
	}
	
	protected FormLink addRollCallLink(LectureBlock lectureBlock) {
		FormLink rollCallLink = uifactory.addFormLink("rcall_" + lectureBlock.getKey(), "rollcall", "", tableEl, Link.LINK_CUSTOM_CSS | Link.NONTRANSLATED);
		rollCallLink.setDomReplacementWrapperRequired(false);
		rollCallLink.setElementCssClass("o_row_rollcall");
		rollCallLink.setTitle(translate("dashboard.widget.edit.absence", lectureBlock.getTitle(), formatter.formatDateAndTime(lectureBlock.getStartDate())));
		rollCallLink.setIconLeftCSS("o_icon o_icon-lg o_icon_lecture");
		return rollCallLink;
	}
	
	private final static class TableCssDelegate implements FlexiTableCssDelegate {
		
		private static final FlexiTableCssDelegate DELEGATE = new TableCssDelegate();

		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return null;
		}

		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_table_body container-fluid o_dashboard_table_max_height o_scrollable_vertical";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_table_row o_dashboard_table_widget_noborder";
		}
		
	}

}
