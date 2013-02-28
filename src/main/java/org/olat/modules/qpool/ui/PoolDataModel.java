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

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.qpool.ui.PoolsAdminController.PoolSource;

/**
 * 
 * Initial date: 23.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PoolDataModel implements FlexiTableDataModel, TableDataModel<PoolRow> {

	private List<PoolRow> rows;
	private PoolSource source;
	private FlexiTableColumnModel columnModel;
	private final Translator translator;
	
	public PoolDataModel(FlexiTableColumnModel columnModel, PoolSource source, Translator translator) {
		this.columnModel = columnModel;
		this.translator = translator;
		this.source = source;
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
		return source.getNumOfItems();
	}

	@Override
	public PoolRow getObject(int row) {
		return rows.get(row);
	}

	@Override
	public void setObjects(List<PoolRow> objects) {
		rows = new ArrayList<PoolRow>(objects);
	}

	@Override
	public void load(int firstResult, int maxResults, SortKey... orderBy) {
		if(rows == null) {
			rows = new ArrayList<PoolRow>();
		}
		
		for(int i=rows.size(); i<firstResult; i++) {
			rows.add(null);
		}
		List<PoolRow> newRows = source.getRows(firstResult, maxResults, orderBy);
		for(int i=0; i<newRows.size(); i++) {
			int rowIndex = i + firstResult;
			if(rowIndex < rows.size()) {
				rows.set(rowIndex, newRows.get(i));
			} else {
				rows.add(newRows.get(i));
			}
		}
	}

	@Override
	public int getColumnCount() {
		return columnModel.getColumnCount();
	}
	
	@Override
	public PoolDataModel createCopyWithEmptyList() {
		return new PoolDataModel(columnModel, source, translator);
	}

	@Override
	public Object getValueAt(int row, int col) {
		PoolRow item = getObject(row);
		switch(Cols.values()[col]) {
			case id: return item.getKey();
			case name: return item.getName();
			default: return "";
		}
	}
	
	public enum Cols {
		id("pool.key"),
		name("pool.name");
		
		private final String i18nKey;
	
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}