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
package org.olat.modules.quality.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.modules.quality.QualitySecurityCallback;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionDataModel extends DefaultFlexiTableDataSourceModel<DataCollectionRow> {

	private final Locale locale;
	private final QualitySecurityCallback secCallback;
	
	public DataCollectionDataModel(FlexiTableDataSourceDelegate<DataCollectionRow> dataSource,
			FlexiTableColumnModel columnsModel, Locale locale, QualitySecurityCallback secCallback) {
		super(dataSource, columnsModel);
		this.locale = locale;
		this.secCallback = secCallback;
	}
	
	public DataCollectionRow getObjectByKey(Long key) {
		List<DataCollectionRow> rows = getObjects();
		for (DataCollectionRow row: rows) {
			if (row != null && row.getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		DataCollectionRow dataCollectionRow = getObject(row);
		switch (DataCollectionCols.values()[col]) {
			case status: return dataCollectionRow.getStatus();
			case title: return dataCollectionRow.getTitle();
			case start: return dataCollectionRow.getStart();
			case deadline: return dataCollectionRow.getDeadline();
			case topicType: return dataCollectionRow.getTopicType();
			case topic: return dataCollectionRow.getTopic();
			case formName: return dataCollectionRow.getFormName();
			case numberParticipants: return dataCollectionRow.getNumberOfParticipants();
			case edit: return Boolean.TRUE;
			case delete: return secCallback.canDeleteDataCollection(dataCollectionRow.getDataCollection());
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataSourceModel<DataCollectionRow> createCopyWithEmptyList() {
		return new DataCollectionDataModel(getSourceDelegate(), getTableColumnModel(), locale, secCallback);
	}

	public enum DataCollectionCols implements FlexiSortableColumnDef {
		status("data.collection.status"),
		title("data.collection.title"),
		start("data.collection.start"),
		deadline("data.collection.deadline"),
		topicType("data.collection.topic.type"),
		topic("data.collection.topic"),
		formName("data.collection.form"),
		numberParticipants("data.collection.number.of.participants"),
		edit("data.collection.edit"),
		delete("data.collection.delete");
		
		private final String i18nKey;
		
		private DataCollectionCols(String i18nKey) {
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
