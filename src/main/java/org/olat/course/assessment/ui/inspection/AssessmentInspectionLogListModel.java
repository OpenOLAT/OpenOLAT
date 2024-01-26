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
package org.olat.course.assessment.ui.inspection;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionLog.Action;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;

/**
 * 
 * Initial date: 8 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionLogListModel extends DefaultFlexiTableDataModel<AssessmentInspectionLogRow>
implements SortableFlexiTableDataModel<AssessmentInspectionLogRow> {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentInspectionLogListModel.class);
	
	private static final LogCols[] COLS = LogCols.values();
	
	private final Translator translator;
	
	public AssessmentInspectionLogListModel(FlexiTableColumnModel columnsModel, Translator translator) {
		super(columnsModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey sortKey) {
		//
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessmentInspectionLogRow logRow = getObject(row);
		return getValueAt(logRow, col);
	}

	@Override
	public Object getValueAt(AssessmentInspectionLogRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case date: return row.getCreationDate();
				case action: return row.getAction();
				case before: return getBeforeValue(row.getAction(), row);
				case after: return getAfterValue(row.getAction(), row);
				default: return "ERROR";
			}
		}
		
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		if(propPos >= 0) {
			return row.getIdentityProp(propPos);
		}
		return "ERROR";
	}
	
	private Object getBeforeValue(Action action, AssessmentInspectionLogRow row) {
		AssessmentInspection before = row.getInspectionBefore();
		
		switch(action) {
			case cancelled: return "-";
			case start:
				return translator.translate("inspection.status.active");
			case finishByParticipant, finishByCoach:
				return before == null ? "???" : translator.translate("inspection.status." + before.getInspectionStatus());
			case effectiveDuration:
				return "-";
			default: return "-";
		}
	}
	
	private Object getAfterValue(Action action, AssessmentInspectionLogRow row) {
		AssessmentInspection after = row.getInspectionAfter();
		
		switch(action) {
			case cancelled: return row.getInspectionAfter() != null ? row.getInspectionAfter().getComment() : "-";
			case start, finishByParticipant, finishByCoach:
				return after == null ? "???" : translator.translate("inspection.status." + after.getInspectionStatus());
			case effectiveDuration: return getEffectiveDurationDecorated(row.getRawAfter());
			case noShow: return row.getRawAfter();
			default: return "-";
		}
	}
	
	private String getEffectiveDurationDecorated(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			try {
				long duration = Long.parseLong(val);
				if(duration < 60) {
					return translator.translate("duration.cell.seconds", Long.toString(duration));
				}
				return translator.translate("duration.cell", Long.toString(duration / 60));
			} catch (Exception e) {
				log.debug("cannot parse duration: {}", val, e);
			}
		}
		return "-";
	}
	
	public enum LogCols implements FlexiSortableColumnDef {
		date("table.header.date"),
		action("table.header.action"),
		before("table.header.before"),
		after("table.header.after")
		;

		private final String i18nKey;
		
		private LogCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

}
