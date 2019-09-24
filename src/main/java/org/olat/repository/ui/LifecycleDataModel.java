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
package org.olat.repository.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LifecycleDataModel implements FlexiTableDataModel<RepositoryEntryLifecycle>, TableDataModel<RepositoryEntryLifecycle> {
	
	private FlexiTableColumnModel columnModel;
	private List<RepositoryEntryLifecycle> lifecycles;
	
	public LifecycleDataModel(FlexiTableColumnModel columnModel) {
		this.columnModel = columnModel;
		this.lifecycles = new ArrayList<>();
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
	public boolean isSelectable(int row) {
		return true;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public void setObjects(List<RepositoryEntryLifecycle> objects) {
		lifecycles = new ArrayList<>(objects);
	}

	@Override
	public LifecycleDataModel createCopyWithEmptyList() {
		return new LifecycleDataModel(columnModel);
	}

	@Override
	public int getRowCount() {
		return lifecycles == null ? 0 : lifecycles.size();
	}

	@Override
	public boolean isRowLoaded(int row) {
		return true;
	}

	@Override
	public RepositoryEntryLifecycle getObject(int row) {
		return lifecycles.get(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		RepositoryEntryLifecycle cycle = getObject(row);
		switch(LCCols.values()[col]) {
			case label: return cycle.getLabel();
			case softkey: return cycle.getSoftKey();
			case validFrom: return cycle.getValidFrom();
			case validTo: return cycle.getValidTo();
			case delete: return Boolean.FALSE;
			default: return "ERROR";
		}
	}

	public enum LCCols {
		softkey("lifecycle.softkey"),
		label("lifecycle.label"),
		validFrom("lifecycle.validFrom"),
		validTo("lifecycle.validTo"),
		edit("edit"),
		delete("delete");
		
		private final String i18nKey;
	
		private LCCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
		
	}
	

}
