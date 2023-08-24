/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.dialog.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 3 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DialogElementsTableModel extends DefaultFlexiTableDataModel<DialogElementRow> implements SortableFlexiTableDataModel<DialogElementRow> {

	public DialogElementsTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		DialogElementRow element = getObject(row);
		return getValueAt(element, col);
	}

	@Override
	public void sort(SortKey sortKey) {
		List<DialogElementRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, null).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(DialogElementRow row, int col) {
		if(col >= 0 && col < DialogCols.values().length) {
			switch (DialogCols.values()[col]) {
				case filename: return row.getFilename();
				case downloadLink: return row.getDownloadLink();
				case publishedBy: return row.getPublishedBy();
				case authoredBy: return row.getAuthoredBy();
				case filesize: return row.getSize();
				case creationDate: return row.getCreationDate();
				case lastActivityDate: return row.getLastActivityDate();
				case newThreads: return row.getNumOfUnreadThreads();
				case threads: return row.getNumOfThreads();
				case newMessages: return row.getNumOfUnreadMessages();
				case messages: return row.getNumOfMessages();
				case toolsLink: return row.getToolsLink();
				default: return "ERROR";
			}
		}

		int propPos = col - DialogElementListController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	public enum DialogCols implements FlexiSortableColumnDef {
		filename("table.header.filename"),
		downloadLink("table.header.filename"),
		publishedBy("table.header.published.by"),
		authoredBy("table.header.authored.by"),
		threads("table.header.threads"),
		newThreads("table.header.newthreads"),
		forum("table.header.forum"),
		filesize("table.header.size"),
		creationDate("table.header.creation.date"),
		lastActivityDate("table.header.last.activity.date"),
		newMessages("table.header.newmessages"),
		messages("table.header.messages"),
		toolsLink("table.header.action");

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
			return this != toolsLink;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
