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
package org.olat.course.learningpath.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 2 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathIdentityDataModel extends DefaultFlexiTableDataModel<LearningPathIdentityRow> 
implements SortableFlexiTableDataModel<LearningPathIdentityRow>, ExportableFlexiTableDataModel {
	
	static final int USER_PROPS_OFFSET = 500;

	private final Translator translator;

	public LearningPathIdentityDataModel(FlexiTableColumnModel columnsModel, Translator translator) {
		super(columnsModel);
		this.translator = translator;
	}

	@Override
	public Object getValueAt(int row, int col) {
		LearningPathIdentityRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(LearningPathIdentityRow row, int col) {
		if(col >= 0 && col < LearningPathIdentityCols.values().length) {
			switch(LearningPathIdentityCols.values()[col]) {
				case progress: return row;
				case completion: return row.getCompletion();
				case passed: return row.getPassed();
				case score: return row.getScore();
				default: return "ERROR";
			}
		}
		int propPos = col - USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	@Override
	public void sort(SortKey orderBy) {
		List<LearningPathIdentityRow> rows = new LearningPathIdentitySortDelegate(orderBy, this, translator.getLocale()).sort();
		super.setObjects(rows);
	}
	
	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		FlexiTableColumnModel columnModel = getTableColumnModel();
		int numOfColumns = columnModel.getColumnCount();
		List<FlexiColumnModel> columns = new ArrayList<>();
		for(int i=0; i<numOfColumns; i++) {
			FlexiColumnModel column = columnModel.getColumnModel(i);
			if(column.isExportable()) {
				columns.add(column);
			}
		}
		return new LearningPathIdentityExport().export(ftC, columns, translator);
	}
	
	public enum LearningPathIdentityCols implements FlexiSortableColumnDef {
		progress("table.header.completion"),
		completion("table.header.completion"),
		passed("table.header.passed"),
		score("table.header.score");
		
		private final String i18nKey;
		
		private LearningPathIdentityCols(String i18nKey) {
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
