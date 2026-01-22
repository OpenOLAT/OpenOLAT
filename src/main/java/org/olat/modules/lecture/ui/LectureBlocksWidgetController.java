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

import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer.LectureBlockVirtualStatus;

/**
 * 
 * Initial date: Jan 8, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class LectureBlocksWidgetController extends TableWidgetController implements FlexiTableComponentDelegate {

	private final Formatter formatter;
	protected DayNavElement dayNavEl;
	protected LectureBlocksWidgetTableModel dataModel;
	protected FlexiTableElement tableEl;
	private FormLayoutContainer emptyCont;
	protected FormLink backButton;
	protected FormLink forwardButton;
	protected FormLink showAllLink;

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
	protected String createIndicators(FormLayoutContainer widgetCont) {
		dayNavEl = DayNavFactory.createElement("day.nav", widgetCont);
		return dayNavEl.getComponent().getComponentName();
	}

	@Override
	protected String createTable(FormLayoutContainer widgetCont) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		dataModel = new LectureBlocksWidgetTableModel(tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 1000, true, getTranslator(), widgetCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		tableEl.setRendererType(FlexiTableRendererType.custom);
		setVelocityRoot(Util.getPackageVelocityRoot(LectureBlocksWidgetController.class));
		VelocityContainer rowVC = createVelocityContainer("lecture_block_widget_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		tableEl.setCssDelegate(TableCssDelegate.DELEGATE);
		
		return tableEl.getComponent().getComponentName();
	}
	
	@Override
	public boolean isRowClickEnabled() {
		return true;
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
		emptyState.setElementCssClass("o_no_border o_no_margin");
		
		backButton = uifactory.addFormLink("back", emptyCont, Link.BUTTON);
		backButton.setIconLeftCSS( ("o_icon o_icon-lg o_icon_course_previous"));
		
		forwardButton = uifactory.addFormLink("forward", emptyCont, Link.BUTTON);
		forwardButton.setIconRightCSS( ("o_icon o_icon-lg o_icon_course_next"));
		
		return emptyCont.getName();
	}

	@Override
	protected String createShowAll(FormLayoutContainer widgetCont) {
		showAllLink = uifactory.addFormLink("dashboard.widget.show.all", widgetCont);
		showAllLink.setIconRightCSS("o_icon o_icon_start");
		return showAllLink.getComponent().getComponentName();
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
		Date fromDate = DateUtils.getStartOfDay(dayNavEl.getStartDate());
		Date toDate= DateUtils.getEndOfDay(dayNavEl.getEndDate());
		List<LectureBlock> lectureBlocks = loadLectureBlocks(fromDate, toDate);
		LectureBlockRef nextScheduledBlock = loadNextScheduledBlock();
		updateTable(lectureBlocks, nextScheduledBlock);
	}
	
	protected abstract List<LectureBlock> loadLectureBlocks(Date fromDate, Date toDate);

	protected abstract LectureBlockRef loadNextScheduledBlock();

	private void updateTable(List<LectureBlock> lectureBlocks, LectureBlockRef nextScheduledBlock) {
		lectureBlocks.sort(Comparator.comparing(LectureBlock::getStartDate));
		
		Date currentDay = null;
		Date selectedDate = DateUtils.getStartOfDay(dayNavEl.getSelectedDate());
		String scrollToId = null;
		List<LectureBlocksWidgetRow> rows = new ArrayList<>(lectureBlocks.size());
		for (LectureBlock lectureBlock : lectureBlocks) {
			LectureBlocksWidgetRow row = new LectureBlocksWidgetRow();
			row.setKey(lectureBlock.getKey());
			row.setExternalRef(lectureBlock.getExternalRef());
			row.setTitle(lectureBlock.getTitle());
			row.setLocation(lectureBlock.getLocation());
			row.setOnlineMeeting(lectureBlock.getBBBMeeting() != null || lectureBlock.getTeamsMeeting() != null);
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
			
			String url = getLectureBlockUrl(lectureBlock);
			row.setUrl(url);
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		if (scrollToId != null) {
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.scrollInContainer(scrollToId, ".o_table_body", "instant"));
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
		StringBuilder sb = new StringBuilder();
		sb.append("<span>");
		// Hour
		sb.append("<span class='o_lecture_time'>")
		      .append(formatter.formatTimeShort(lectureBlock.getStartDate()))
		      .append("</span>");
		// Duration
		if(lectureBlock.getEndDate() != null) {
			String duration = Formatter.formatDurationCompact(lectureBlock.getEndDate().getTime() - lectureBlock.getStartDate().getTime());
			sb.append(", <span class='o_lecture_duration'>").append(duration).append("</span>");
		}
		sb.append("</span>");
		return sb.toString();
	}
	
	private void updateEmptyButtonUI() {
		Date prevLectureBlock = getPrevLectureBlock(dayNavEl.getStartDate());
		backButton.setUserObject(prevLectureBlock);
		backButton.setEnabled(backButton.getUserObject() != null);
		Date nextLectureBlock = getNextLectureBlock(dayNavEl.getStartDate());
		forwardButton.setUserObject(nextLectureBlock);
		forwardButton.setEnabled(forwardButton.getUserObject() != null);
	}

	protected abstract Date getPrevLectureBlock(Date date);

	protected abstract Date getNextLectureBlock(Date date);

	/**
	 * @param lectureBlock  
	 */
	protected String getLectureBlockUrl(LectureBlock lectureBlock) {
		return null;
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
			return "o_table_row row o_dashboard_table_widget_noborder";
		}
		
	}

}
