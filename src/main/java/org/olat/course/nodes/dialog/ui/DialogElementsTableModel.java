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
package org.olat.course.nodes.dialog.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 3 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DialogElementsTableModel extends DefaultFlexiTableDataModel<DialogElementRow> {

	public DialogElementsTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		DialogElementRow entry = getObject(row);

		if(col >= 0 && col < DialogCols.values().length) {
			switch (DialogCols.values()[col]) {
				case filename: return entry.getDownloadLink();
				case filesize: return entry.getSize();
				case date: return entry.getCreationDate();
				case newMessages: return entry.getNumOfUnreadMessages();
				case messages: return entry.getNumOfMessages();
				default: return "ERROR";
			}
		}
		
		int propPos = col - DialogElementListController.USER_PROPS_OFFSET;
		return entry.getIdentityProp(propPos);
	}
	
	public enum DialogCols implements FlexiSortableColumnDef {
		filename("table.header.filename"),
		forum("table.header.forum"),
		filesize("table.header.size"),
		date("table.header.date"),
		newMessages("table.header.newmessages"),
		messages("table.header.messages");

		private final String i18nKey;
		
		private DialogCols(String i18nKey) {
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
