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
package org.olat.modules.quality.generator.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 9 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PreviewDataModel extends DefaultFlexiTableDataModel<PreviewRow> implements SortableFlexiTableDataModel<PreviewRow> {
	
	private static final PreviewCols[] COLS = PreviewCols.values();

	private final Locale locale;
	
	public PreviewDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<PreviewRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		PreviewRow decision = getObject(row);
		return getValueAt(decision, col);
	}

	@Override
	public Object getValueAt(PreviewRow row, int col) {
		switch(COLS[col]) {
		case status: return row.getTranslatedStatus();
		case title: return row.getTitle();
		case start: return row.getStart();
		case deadline: return row.getDeadline();
		case topicType: return row.getTopicType();
		case topic: return row.getTopic();
		case formName: return row.getFormName();
		case numberParticipants: return row.getNumberParticipants();
		case generatorId: return row.getGeneratorId();
		case generatorTitle: return row.getGeneratorTitle();
		default: return null;
		}
	}
	
	public enum PreviewCols implements FlexiSortableColumnDef {
		status("data.collection.status"),
		title("data.collection.title"),
		start("data.collection.start"),
		deadline("data.collection.deadline"),
		topicType("data.collection.topic.type"),
		topic("data.collection.topic"),
		formName("data.collection.form"),
		numberParticipants("data.collection.number.of.participants"),
		generatorId("data.collection.generator.id"),
		generatorTitle("data.collection.generator.title");
		
		private final String i18nKey;
		
		private PreviewCols(String i18nKey) {
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
