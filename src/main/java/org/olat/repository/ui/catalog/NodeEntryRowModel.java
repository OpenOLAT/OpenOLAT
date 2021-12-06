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
package org.olat.repository.ui.catalog;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 04.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NodeEntryRowModel extends DefaultFlexiTableDataModel<NodeEntryRow> {
	
	public NodeEntryRowModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		NodeEntryRow item = getObject(row);
		switch (NodeCols.values()[col]) {
		case up:
			return row == 0 ? Boolean.FALSE : Boolean.TRUE;
		case down:
			return row >= (getRowCount() - 1) ? Boolean.FALSE : Boolean.TRUE;
		default:
		}
		return getValueAt(item, col);
	}
	
	public Object getValueAt(NodeEntryRow item, int col) {
		switch(NodeCols.values()[col]) {
			case key: return item.getKey();
			case type: return item;
			case displayName: return item.getDisplayname();
			case creationDate: return item.getCreationDate();
			case position: return item.getPositionLink();
			default: return "ERROR";
		}
	}
	
	public enum NodeCols {
		key("table.header.key"),
		type("table.header.typeimg"),
		displayName("tools.add.catalog.category"),
		creationDate("table.header.date"),
		up("table.header.up"),
		down("table.header.down"),
		position("table.header.position");
		
		private final String i18nKey;
		
		private NodeCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
