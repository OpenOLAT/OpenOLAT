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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 19.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VisibleFlexiColumnsModel implements TableDataModel<FlexiColumnModel> {
	
	private Set<Integer> enabledCols = new HashSet<Integer>();
	private final Translator translator;
	private final FlexiTableColumnModel columns;
	
	public VisibleFlexiColumnsModel(FlexiTableColumnModel columns, Set<Integer> enabledCols, Translator translator) {
		this.columns = columns;
		this.translator = translator;
		this.enabledCols = new HashSet<Integer>(enabledCols);
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return columns == null ? 0 : columns.getColumnCount();
	}

	@Override
	public Object getValueAt(int row, int col) {
		FlexiColumnModel cd = getObject(row);
		switch (col) {
			case 0: // on/off indicator; true if column is visible
				return (enabledCols.contains(new Integer(cd.getColumnIndex())) ? Boolean.TRUE : Boolean.FALSE);
			case 1: // name of column
				return translator.translate(cd.getHeaderKey());
			default:
				return "ERROR";
		}
	}

	@Override
	public FlexiColumnModel getObject(int row) {
		if(columns == null) return null;
		return columns.getColumnModel(row);
	}

	@Override
	public void setObjects(List<FlexiColumnModel> objects) {
		//
	}

	@Override
	public VisibleFlexiColumnsModel createCopyWithEmptyList() {
		return new VisibleFlexiColumnsModel(columns, enabledCols, translator);
	}
}
