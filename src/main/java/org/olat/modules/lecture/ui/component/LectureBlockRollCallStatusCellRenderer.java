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

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.ui.AppealRollCallRow;
import org.olat.modules.lecture.ui.LectureBlockAndRollCallRow;

/**
 * 
 * Initial date: 20 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRollCallStatusCellRenderer implements FlexiCellRenderer {

	private final Translator translator;
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;
	
	public LectureBlockRollCallStatusCellRenderer(boolean authorizedAbsenceEnabled, boolean absenceDefaultAuthorized,
			Translator translator) {
		this.authorizedAbsenceEnabled = authorizedAbsenceEnabled;
		this.absenceDefaultAuthorized = absenceDefaultAuthorized;
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(renderer == null) {
			if(cellValue instanceof LectureBlockAndRollCallRow) {
				LectureBlockAndRollCallRow rollCallRow = (LectureBlockAndRollCallRow)cellValue;
				renderString(target, rollCallRow.getRow());
			} else if(cellValue instanceof LectureBlockAndRollCall) {
				renderString(target, (LectureBlockAndRollCall)cellValue);
			} else if(cellValue instanceof AppealRollCallRow) {
				AppealRollCallRow rollCallRow = (AppealRollCallRow)cellValue;
				renderString(target, rollCallRow.getLectureBlockAndRollCall());
			}
		} else if(cellValue instanceof LectureBlockAndRollCallRow) {
			LectureBlockAndRollCallRow rollCallRow = (LectureBlockAndRollCallRow)cellValue;
			render(target, rollCallRow.getRow());
		} else if(cellValue instanceof LectureBlockAndRollCall) {
			render(target, (LectureBlockAndRollCall)cellValue);
		} else if(cellValue instanceof AppealRollCallRow) {
			AppealRollCallRow rollCallRow = (AppealRollCallRow)cellValue;
			render(target, rollCallRow.getLectureBlockAndRollCall());
		}	
	}
	
	protected final void renderString(StringOutput target, LectureBlockAndRollCall rollCall) {
		if(rollCall.isRollCalled()) {
			LectureBlockStatus status = rollCall.getStatus();
			LectureRollCallStatus rollCallStatus = rollCall.getRollCallStatus();
			if(status == LectureBlockStatus.cancelled) {
				target.append(translator.translate("cancelled"));
			} else if(status == LectureBlockStatus.done
					&& (rollCallStatus == LectureRollCallStatus.closed || rollCallStatus == LectureRollCallStatus.autoclosed)) {
				renderClosed(target, rollCall, true);	
			} else {
				target.append(translator.translate("in.progress"));
			}
		} else if(!rollCall.isCompulsory()) {
			target.append(translator.translate("rollcall.tooltip.free"));
		}
	}
	
	protected final void render(StringOutput target, LectureBlockAndRollCall rollCall) {
		if(rollCall.isRollCalled()) {
			LectureBlockStatus status = rollCall.getStatus();
			LectureRollCallStatus rollCallStatus = rollCall.getRollCallStatus();
			if(status == LectureBlockStatus.cancelled) {
				String title = translator.translate("cancelled");
				target.append("<span title='").append(title).append("'><i class='o_icon o_icon-lg o_icon_cancelled'> </i></span>");
			} else if(status == LectureBlockStatus.done
					&& (rollCallStatus == LectureRollCallStatus.closed || rollCallStatus == LectureRollCallStatus.autoclosed)) {
				renderClosed(target, rollCall, false);	
			} else {
				String title = translator.translate("in.progress");
				target.append("<span title='").append(title).append("'><i class='o_icon o_icon-lg o_icon_status_in_review'> </i></span>");
			}
		} else if(!rollCall.isCompulsory()) {
			String title = translator.translate("rollcall.tooltip.free");
			target.append("<span title='").append(title).append("'><i class='o_icon o_icon-lg o_lectures_rollcall_free'> </i></span>");
		}
		
		AbsenceNoticeType absenceNoticeType = rollCall.getAbsenceNoticeType();
		if(absenceNoticeType  != null) {
			String noticeTitleI18n;
			switch(absenceNoticeType) {
				case dispensation: noticeTitleI18n = "noticed.dispensation"; break;
				case notified: noticeTitleI18n = "noticed.notice.absence"; break;
				case absence:
				default: noticeTitleI18n = "noticed.type.absence"; break;
			}
			
			String title = translator.translate(noticeTitleI18n);
			target.append(" <span title='").append(title).append("'><i class='o_icon o_icon-fw o_icon-lg o_filetype_html'> </i></span>");
		}
	}
	
	private void renderClosed(StringOutput target, LectureBlockAndRollCall rollCall, boolean textOnly) {
		int numOfLectures = rollCall.getEffectiveLecturesNumber();
		if(numOfLectures < 0) {
			numOfLectures = rollCall.getPlannedLecturesNumber();
		}
	
		String title;
		String iconCssClass;
		if(rollCall.isCompulsory()) {
			if(rollCall.getLecturesAttendedNumber() >= numOfLectures) {
				iconCssClass = "o_lectures_rollcall_ok";
				title = translator.translate("rollcall.tooltip.ok");
			} else if(authorizedAbsenceEnabled) {
				if(absenceDefaultAuthorized && rollCall.getLecturesAuthorizedAbsent() == null) {
					iconCssClass = "o_lectures_rollcall_ok";
					title = translator.translate("rollcall.tooltip.ok");
				} else if(rollCall.getLecturesAuthorizedAbsent() != null && rollCall.getLecturesAuthorizedAbsent().booleanValue()) {
					iconCssClass = "o_lectures_rollcall_warning";
					title = translator.translate("rollcall.tooltip.authorized.absence");
				} else {
					iconCssClass = "o_lectures_rollcall_danger";
					title = translator.translate("rollcall.tooltip.unauthorized.absence");
				}
			} else {
				iconCssClass = "o_lectures_rollcall_danger";
				title = translator.translate("rollcall.tooltip.absence");
			}
		} else {
			iconCssClass = "o_lectures_rollcall_free";
			title = translator.translate("rollcall.tooltip.free");
		}
		if(textOnly) {
			target.append(title);
		} else {
			target.append("<span title='").append(title).append("'><i class='o_icon o_icon-lg ").append(iconCssClass).append("'> </i></span>");
		}
	}
}
