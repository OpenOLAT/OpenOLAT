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
package org.olat.course.nodes.pf.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 *
 * Initial date: 07.12.2016<br>
 * @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 *
 */
public class DropBoxTableModel extends DefaultFlexiTableDataModel<DropBoxRow> implements SortableFlexiTableDataModel<DropBoxRow>{

	protected FormUIFactory uifactory = FormUIFactory.getInstance();

	public DropBoxTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		DropBoxRow content = getObject(row);
		return getValueAt(content, col);
	}

	@Override
	public Object getValueAt(DropBoxRow content, int col) { 
		if (col >= 0 && col < DropBoxCols.values().length) {			
			switch(DropBoxCols.values()[col]) {
				case numberFiles: return content.getFilecount();
				case numberFilesReturn: return content.getFilecountReturn();
				case newFiles: return content.getNewfolders();
				case lastUpdate: return content.getLastupdate();
				case lastUpdateReturn: return content.getLastupdateReturn();
				case status: return content.getStatus();
				default: return "";
			}
		} else if (col >= PFCoachController.USER_PROPS_OFFSET) {
			int userCol = col - PFCoachController.USER_PROPS_OFFSET;
			return content.getIdentity().getIdentityProp(userCol);
		}
		return "ERROR";
	}

	@Override
	public void sort(SortKey sortKey) {
		if(sortKey != null) {
			List<DropBoxRow> views = new SortableFlexiTableModelDelegate<>(sortKey, this, null).sort();
			super.setObjects(views);
		}
	}

	public enum DropBoxCols implements FlexiSortableColumnDef {

		numberFiles("table.cols.numFiles", true),
		numberFilesReturn("table.cols.numReturn", true),
		newFiles("table.cols.newFiles", true),
		lastUpdate("table.cols.lastUpdate", true),
		lastUpdateReturn("table.cols.lastUpdate", true),
		status("table.cols.status", true),
		openbox("table.cols.openbox", false);

		private final String i18nKey;
		
		private final boolean sortable;

		private DropBoxCols(String i18nKey, boolean sortable) {
			this.i18nKey = i18nKey;
			this.sortable = sortable;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return sortable;
		}

		@Override
		public String sortKey() {
			return name();
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}

}