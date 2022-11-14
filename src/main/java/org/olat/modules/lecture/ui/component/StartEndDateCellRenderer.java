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

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.ui.coach.AbsenceNoticeHelper;
import org.olat.modules.lecture.ui.coach.AbsenceNoticeRow;

/**
 * 
 * Initial date: 14 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StartEndDateCellRenderer implements FlexiCellRenderer {
	
	private final boolean startDate;
	private final Formatter formatter;
	
	public StartEndDateCellRenderer(boolean startDate, Locale locale) {
		this.startDate = startDate;
		formatter = Formatter.getInstance(locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		
		Object obj = source.getFormItem().getTableDataModel().getObject(row);
		if(obj instanceof AbsenceNoticeRow) {
			AbsenceNoticeRow noticeRow = (AbsenceNoticeRow)obj;
			
			Date date = null;
			if(noticeRow.getAbsenceNotice().getNoticeTarget() == AbsenceNoticeTarget.lectureblocks
					&& (noticeRow.getLectureBlocks() != null && !noticeRow.getLectureBlocks().isEmpty())) {
				if(startDate) {
					date = noticeRow.getLectureBlocks().stream()
							.map(LectureBlock::getStartDate)
							.min(Date::compareTo)
							.orElse(null);
				} else {
					date = noticeRow.getLectureBlocks().stream()
							.map(LectureBlock::getEndDate)
							.max(Date::compareTo)
							.orElse(null);
				}
			} else {
				date = startDate ? noticeRow.getStartDate() : noticeRow.getEndDate();
			}

			if(date != null) {
				if((startDate && AbsenceNoticeHelper.isStartOfWholeDay(date))
						|| (!startDate && AbsenceNoticeHelper.isEndOfWholeDay(date))) {
					target.append(formatter.formatDate(date));
				} else {
					target.append(formatter.formatDateAndTime(date));
				}
			}
		}
	}
}
