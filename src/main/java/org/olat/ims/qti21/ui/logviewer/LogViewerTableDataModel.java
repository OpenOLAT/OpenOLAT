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
package org.olat.ims.qti21.ui.logviewer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModelDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.XlsFlexiTableExporter;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.model.LogViewerEntry;
import org.olat.ims.qti21.model.QTI21QuestionType;

/**
 * 
 * Initial date: 24 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LogViewerTableDataModel extends DefaultFlexiTableDataModel<LogViewerEntry> implements ExportableFlexiTableDataModel {
	
	private static final LogEntryCols[] COLS = LogEntryCols.values();
	protected static final String OUTCOMES = "OUTCOMES";
	protected static final String MANUAL_CORRECTION = "MANUAL_CORRECTION";
	
	private final Translator translator;
	private List<LogViewerEntry> backups;
	private final String downloadFilename;
	
	public LogViewerTableDataModel(FlexiTableColumnModel columnModel, String downloadFilename, Translator translator) {
		super(columnModel);
		this.translator = translator;
		this.downloadFilename = downloadFilename;
	}
	
	protected void filter(List<String> types, String title, Date from, Date to) {
		if((types != null && !types.isEmpty()) || StringHelper.containsNonWhitespace(title) || from != null || to != null) {
			if(title != null) {
				title = title.toLowerCase();
			}
			
			List<LogViewerEntry> filteredRows = new ArrayList<>();
			for(LogViewerEntry backupedRow:backups) {
				if(acceptTypes(backupedRow, types) && acceptTitle(backupedRow, title)
						&& acceptRange(backupedRow, from, to)) {
					filteredRows.add(backupedRow);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}
	
	private boolean acceptRange(LogViewerEntry row, Date from, Date to) {
		if(to == null && from == null) return true;

		boolean allOk = true;
		if(from != null && from.after(row.getDate())) {
			allOk &= false;
		}
		if(to != null && to.before(row.getDate())) {
			allOk &= false;
		}
		return allOk;
	}

	private boolean acceptTitle(LogViewerEntry row, String title) {
		if(!StringHelper.containsNonWhitespace(title)) return true;
		return row.getAssessmentItemTitle() != null && row.getAssessmentItemTitle().toLowerCase().contains(title);
	}
	
	private boolean acceptTypes(LogViewerEntry row, List<String> types) {
		if(types == null || types.isEmpty()) return true;
		
		for(String type:types) {
			if((row.getTestEventType() != null && type.equals(row.getTestEventType().name()))
					|| (row.getItemEventType() != null && type.equals(row.getItemEventType().name()))
						|| (OUTCOMES.equals(type) && row.isOutcomes())
						|| (MANUAL_CORRECTION.equals(type) && row.isManualCorrection())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		List<FlexiColumnModel> columns = ExportableFlexiTableDataModelDelegate.getColumnModels(ftC.getFormItem());
		return new XlsFlexiTableExporter(downloadFilename).export(ftC, columns, translator);
	}

	@Override
	public Object getValueAt(int row, int col) {
		LogViewerEntry entry = getObject(row);
		switch(COLS[col]) {
			case date: return entry.getDate();
			case event: return getEvents(entry);
			case itemTitle: return entry.getAssessmentItemTitle();
			case itemId: return entry.getAssessmentItemId();
			case interactionsTypes: return translateQuestionType(entry.getQuestionType());
			case minMaxScore: return getMinMaxScore(entry);
			case response: return entry.getAnswers();
			case responseIds: return entry.getAnswers();
			case score: return entry.getScore();
			default: return "ERROR";
		}
	}
	
	private String translateQuestionType(QTI21QuestionType type) {
		if(type == null) return null;
		return translator.translate("new." + type.name());
	}
	
	private String getEvents(LogViewerEntry entry) {
		StringBuilder sb = new StringBuilder();
		if(entry.isOutcomes()) {
			sb.append(OUTCOMES);
		} else if(entry.isManualCorrection()) {
			sb.append(MANUAL_CORRECTION);
		} else {
			if(entry.getTestEventType() != null) {
				sb.append(entry.getTestEventType());
			}
			if(entry.getItemEventType() != null) {
				if(sb.length() > 0) {
					sb.append(" / ");
				}
				sb.append(entry.getItemEventType());
			}
		}
		return sb.toString();
	}
	
	private String getMinMaxScore(LogViewerEntry entry) {
		if(!entry.isOutcomes()) return null;

		StringBuilder sb = new StringBuilder();
		if(entry.getMinScore() != null) {
			sb.append(AssessmentHelper.getRoundedScore(entry.getMinScore()));
		} else {
			sb.append("-");
		}
		sb.append(" / ");
		if(entry.getMaxScore() != null) {
			sb.append(AssessmentHelper.getRoundedScore(entry.getMaxScore()));
		} else {
			sb.append("-");
		}
		return sb.toString();
	}
	
	@Override
	public void setObjects(List<LogViewerEntry> objects) {
		backups = new ArrayList<>(objects);
		super.setObjects(objects);
	}

	public enum LogEntryCols implements FlexiSortableColumnDef {
		date("table.header.date"),
		event("table.header.event"),
		itemTitle("table.header.item.title"),
		itemId("table.header.item.id"),
		interactionsTypes("table.header.interactions.types"),
		response("table.header.response"),
		responseIds("table.header.response.ids"),
		minMaxScore("table.header.min.max.score"),
		score("table.header.score");
		
		private final String i18nKey;
		
		private LogEntryCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
