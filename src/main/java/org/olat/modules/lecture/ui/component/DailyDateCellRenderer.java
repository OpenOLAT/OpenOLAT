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
import java.util.Locale;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.model.AbsenceNoticeInfos;
import org.olat.modules.lecture.ui.coach.AbsenceNoticeHelper;

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
		if(cellValue instanceof AbsenceNoticeInfos) {
			AbsenceNoticeInfos infos = (AbsenceNoticeInfos)cellValue;
			render(target, infos.getStartDate(), infos.getEndDate());
		} else if(cellValue instanceof AbsenceNotice) {
			AbsenceNotice notice = (AbsenceNotice)cellValue;
			render(target, notice.getStartDate(), notice.getEndDate());
		}
	}
	
	private void render(StringOutput target, Date startDate, Date endDate) {
		boolean startWholeDay = AbsenceNoticeHelper.isStartOfWholeDay(startDate);
		boolean endWholeDay = AbsenceNoticeHelper.isEndOfWholeDay(endDate);
		
		if(CalendarUtils.isSameDay(startDate, endDate)) {
			if(startWholeDay && endWholeDay) {
				target.append(translator.translate("whole.day"));
			} else {
				renderSameDay(target, startDate, endDate);
			}
		} else if(startWholeDay && endWholeDay) {
			target.append(translator.translate("whole.day"));	
		} else if(currentDate != null) {
			if(CalendarUtils.isSameDay(currentDate, startDate)) {
				if(startWholeDay) {
					target.append(translator.translate("whole.day"));
				} else {
					target.append(translator.translate("from", new String[] { format.formatTimeShort(startDate) }));
				}
			} else if(CalendarUtils.isSameDay(currentDate, endDate)) {
				if(endWholeDay) {
					target.append(translator.translate("whole.day"));
				} else {
					target.append(translator.translate("upto", new String[] { format.formatTimeShort(endDate) }));
				}
			} else if(startDate.before(currentDate) && endDate.after(currentDate)) {
				target.append(translator.translate("whole.day"));	
			} else {
				renderError(target, startDate, endDate);
			}
		} else {
			renderError(target, startDate, endDate);
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
	
	

}
