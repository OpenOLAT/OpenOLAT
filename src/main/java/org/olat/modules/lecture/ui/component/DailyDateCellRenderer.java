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
package org.olat.modules.lecture.ui.component;


import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.model.AbsenceNoticeInfos;
import org.olat.modules.lecture.ui.coach.AbsenceNoticeHelper;
import org.olat.modules.lecture.ui.coach.AbsenceNoticeRow;

/**
 * Show the date focused on a daily base. The actual date is not shown,
 * only the hours or if it's a full day.
 * 
 * Initial date: 4 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DailyDateCellRenderer implements FlexiCellRenderer {
	
	private Date currentDate;
	private int counter = 0;
	private final Formatter format;
	private final Translator translator;
	
	public DailyDateCellRenderer(Date currentDate, Locale locale, Translator translator) {
		format = Formatter.getInstance(locale);
		this.translator = translator;
		this.currentDate = currentDate;
	}
	
	public void setCurrentDate(Date date) {
		this.currentDate = date;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof AbsenceNoticeRow) {
			AbsenceNoticeRow noticeRow = (AbsenceNoticeRow)cellValue;
			render(target, noticeRow.getAbsenceNotice(), noticeRow.getLectureBlocks());
		} else if(cellValue instanceof AbsenceNoticeInfos) {
			AbsenceNoticeInfos infos = (AbsenceNoticeInfos)cellValue;
			render(target, infos.getAbsenceNotice(), null);
		} else if(cellValue instanceof AbsenceNotice) {
			AbsenceNotice notice = (AbsenceNotice)cellValue;
			render(target, notice, null);
		}
	}
	
	protected static boolean isRenderLectureBlock(AbsenceNotice notice, List<LectureBlock> lectures) {
		return notice.getNoticeTarget() == AbsenceNoticeTarget.lectureblocks
				&& (lectures != null && !lectures.isEmpty());
	}
	
	private void render(StringOutput target, AbsenceNotice notice, List<LectureBlock> lectures) {
		Date startDate = notice.getStartDate();
		Date endDate = notice.getEndDate();
		
		boolean startWholeDay = AbsenceNoticeHelper.isStartOfWholeDay(startDate);
		boolean endWholeDay = AbsenceNoticeHelper.isEndOfWholeDay(endDate);
		
		if(CalendarUtils.isSameDay(startDate, endDate)) {
			if(startWholeDay && endWholeDay) {
				renderWholeDay(target, notice, lectures);
			} else {
				renderSameDay(target, startDate, endDate);
			}
		} else if(startWholeDay && endWholeDay) {
			renderWholeDay(target, notice, lectures);
		} else if(currentDate != null) {
			if(CalendarUtils.isSameDay(currentDate, startDate)) {
				if(startWholeDay) {
					renderWholeDay(target, notice, lectures);
				} else {
					target.append(translator.translate("from", new String[] { format.formatTimeShort(startDate) }));
				}
			} else if(CalendarUtils.isSameDay(currentDate, endDate)) {
				if(endWholeDay) {
					renderWholeDay(target, notice, lectures);
				} else {
					target.append(translator.translate("upto", new String[] { format.formatTimeShort(endDate) }));
				}
			} else if(startDate.before(currentDate) && endDate.after(currentDate)) {
				renderWholeDay(target, notice, lectures);
			} else {
				renderError(target, startDate, endDate);
			}
		} else {
			renderError(target, startDate, endDate);
		}
	}
	
	private void renderWholeDay(StringOutput target, AbsenceNotice notice, List<LectureBlock> lectures) {
		if(isRenderLectureBlock(notice, lectures)) {
			renderLectures(target, lectures);
		} else {
			target.append(translator.translate("whole.day"));
		}
	}
	
	private void renderError(StringOutput target, Date startDate, Date endDate) {
		boolean startWholeDay = AbsenceNoticeHelper.isStartOfWholeDay(startDate);
		boolean endWholeDay = AbsenceNoticeHelper.isEndOfWholeDay(endDate);
		if(startDate != null && endDate != null && CalendarUtils.isSameDay(startDate, endDate)) {
			target.append(format.formatDate(startDate))
			      .append(" ");
			
			if(startWholeDay && endWholeDay) {
				target.append(translator.translate("whole.day"));
			} else {
				target.append(format.formatTimeShort(startDate))
				      .append(" - ")
				      .append(format.formatTimeShort(endDate));
			}
		} else if(startWholeDay && endWholeDay) {
			target.append(format.formatDate(startDate))
			      .append(" - ")
			      .append(format.formatDate(endDate));
		} else {
			// whole days without times
			if(startDate != null) {
				target.append(format.formatDateAndTime(startDate));
			}
			if(endDate != null) {
				if(startDate != null) {
					target.append(" - ");
				}
				target.append(format.formatDateAndTime(endDate));
			}
		}
	}
	
	private void renderSameDay(StringOutput target, Date start, Date end) {
		if(start != null) {
			target.append(format.formatTimeShort(start));
		}
		if(end != null) {
			if(start != null) {
				target.append(" - ");
			}
			target.append(format.formatTimeShort(end));
		}
	}
	
	private void renderLectures(StringOutput target, List<LectureBlock> lectureBlocks) {
		int numOfBlocks = lectureBlocks.size();
		int numOfLectures = 0;
		for(LectureBlock lectureBlock:lectureBlocks) {
			numOfLectures += lectureBlock.getCalculatedLecturesNumber();
		}
		
		String labelKey = numOfBlocks == 1 ? "table.lecture.infos" : "table.lectures.infos";
		String label = translator.translate(labelKey, new String[] { Integer.toString(numOfBlocks), Integer.toString(numOfLectures) });
		String id = "p_infos_" + ++counter;
		target.append("<span>").append(label).append(" <i id='").append(id).append("' class='o_icon o_icon-lg o_icon_info'>  </i></span>");
		
		StringBuilder sb = new StringBuilder();
		sb.append("<ul class='list-unstyled'>");
		for(LectureBlock lectureBlock:lectureBlocks) {
			sb.append("<li>").append(getLectureBlockLabel(lectureBlock)).append("</li>");
		}
		sb.append("</ul>");
		target.append("<script>jQuery(function () {jQuery('#").append(id).append("').tooltip({placement:\"bottom\",container: \"body\",html:true,title:\"")
		      .append(StringHelper.escapeJavaScript(sb.toString()))
		      .append("\"});})</script>");
	}
	
	private String getLectureBlockLabel(LectureBlock block) {
		int numOfLectures = block.getCalculatedLecturesNumber();
		String[] args = new String[] {
			format.formatDate(block.getStartDate()), 				// 0
			format.formatTimeShort(block.getStartDate()), 			// 1
			format.formatTimeShort(block.getEndDate()), 			// 2
			Integer.toString(numOfLectures),						// 3
			StringHelper.escapeHtml(block.getTitle()),				// 4
		};
		
		String key = numOfLectures <= 1 ? "table.lecture.explain" : "table.lectures.explain";
		return translator.translate(key, args);
	}
}
