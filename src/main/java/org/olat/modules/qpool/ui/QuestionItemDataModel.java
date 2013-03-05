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
package org.olat.modules.qpool.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSource;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;

/**
 * 
 * Initial date: 23.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemDataModel implements FlexiTableDataModel, FlexiTableDataSource<QuestionItemRow>, TableDataModel<QuestionItemRow> {

	private List<QuestionItemRow> rows;
	private FlexiTableColumnModel columnModel;
	private ItemRowsSource source;
	private final Translator translator;
	
	private int rowCount;
	
	public QuestionItemDataModel(FlexiTableColumnModel columnModel, ItemRowsSource source, Translator translator) {
		this.columnModel = columnModel;
		this.source = source;
		this.translator = translator;
	}
	
	public void setSource(ItemRowsSource source) {
		this.source = source;
		rows.clear();
	}
	
	@Override
	public FlexiTableColumnModel getTableColumnModel() {
		return columnModel;
	}

	@Override
	public void setTableColumnModel(FlexiTableColumnModel tableColumnModel) {
		this.columnModel = tableColumnModel;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public QuestionItemRow getObject(int row) {
		return rows.get(row);
	}

	@Override
	public void setObjects(List<QuestionItemRow> objects) {
		rows = new ArrayList<QuestionItemRow>(objects);
	}

	@Override
	public ResultInfos<QuestionItemRow> load(int firstResult, int maxResults, SortKey... orderBy) {
		if(rows == null) {
			rows = new ArrayList<QuestionItemRow>();
		}
		for(int i=rows.size(); i<firstResult; i++) {
			rows.add(null);
		}
		
		ResultInfos<QuestionItemRow> newRows = source.getRows(null, null, firstResult, maxResults, orderBy);
		if(firstResult == 0) {
			if(newRows.getObjects().size() < maxResults) {
				rowCount = newRows.getObjects().size();
			} else {
				rowCount = source.getRowCount();
			}
		}
		
		for(int i=0; i<newRows.getObjects().size(); i++) {
			int rowIndex = i + firstResult;
			if(rowIndex < rows.size()) {
				rows.set(rowIndex, newRows.getObjects().get(i));
			} else {
				rows.add(newRows.getObjects().get(i));
			}
		}
		return new DefaultResultInfos<QuestionItemRow>(newRows.getNextFirstResult(), newRows.getCorrectedRowCount(), rows);
	}
	
	@Override
	public ResultInfos<QuestionItemRow> search(String query, List<String> condQueries, int firstResult,
			int maxResults, SortKey... orderBy) {
		if(firstResult == 0) {
			rows = new ArrayList<QuestionItemRow>();
		} else {
			for(int i=rows.size(); i<firstResult; i++) {
				rows.add(null);
			}
		}
		ResultInfos<QuestionItemRow> newRows = source.getRows(query, condQueries, firstResult, maxResults, orderBy);
		if(newRows.getCorrectedRowCount() >= 0) {
			rowCount = newRows.getCorrectedRowCount();
		} else if(firstResult == 0) {
			rowCount = source.getRowCount();
		}
		
		for(int i=0; i<newRows.getObjects().size(); i++) {
			int rowIndex = i + firstResult;
			if(rowIndex < rows.size()) {
				rows.set(rowIndex, newRows.getObjects().get(i));
			} else {
				rows.add(newRows.getObjects().get(i));
			}
		}
		return new DefaultResultInfos<QuestionItemRow>(newRows.getNextFirstResult(), newRows.getCorrectedRowCount(), rows);
	}

	@Override
	public int getColumnCount() {
		return columnModel.getColumnCount();
	}
	
	@Override
	public QuestionItemDataModel createCopyWithEmptyList() {
		return new QuestionItemDataModel(columnModel, source, translator);
	}

	@Override
	public Object getValueAt(int row, int col) {
		QuestionItemRow item = getObject(row);
		switch(Cols.values()[col]) {
			case id: return item.getKey();
			case subject: return item.getSubject();
			case studyField: return item.getStudyFieldName();
			case point: return item.getPoint();
			case type: {
				QuestionType type = item.getQuestionType();
				if(type == null) {
					return "";
				}
				return type.name();
			}
			case status: {
				QuestionStatus s = item.getQuestionStatus();
				if(s == null) {
					return "";
				}
				return translator.translate(s.name());
			}
			case mark: {
				return item.getMarkLink();
			}
			default: {
				return "-";
			}
		}
	}
	
	public enum Cols {
		id("item.key"),
		subject("item.subject"),
		studyField("item.studyField"),
		point("item.point"),
		type("item.type"),
		status("item.status"),
		mark("mark");
		
		private final String i18nKey;
	
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}