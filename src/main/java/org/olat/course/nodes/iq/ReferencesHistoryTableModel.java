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
package org.olat.course.nodes.iq;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 4 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferencesHistoryTableModel extends DefaultFlexiTableDataModel<ReferenceHistoryRow> {
	
	private static final ReferencesCols[] COLS = ReferencesCols.values();

	private final Translator translator;
	private final RepositoryEntry currentTestEntry;
	
	public ReferencesHistoryTableModel(FlexiTableColumnModel columnModel,
			RepositoryEntry currentTestEntry, Translator translator) {
		super(columnModel);
		this.translator = translator;
		this.currentTestEntry = currentTestEntry;
	}

	@Override
	public Object getValueAt(int row, int col) {
		ReferenceHistoryRow infos = getObject(row);
		return switch(COLS[col]) {
			case id -> infos.testEntry().getKey();
			case displayName -> getDisplayname(infos);
			case externalRef -> infos.testEntry().getExternalRef();
			case assignedOn -> infos.assignedOn();
			case assignedBy -> infos.assignedBy();
			case runs -> infos.runs();
			default -> "ERROR";
		};
	}
	
	private String getDisplayname(ReferenceHistoryRow infos) {
		String displayname = infos.testEntry().getDisplayname();
		if(currentTestEntry != null && currentTestEntry.equals(infos.testEntry())) {
			displayname += " " + translator.translate("reference.current");
		}
		return displayname;
	}
	
	public enum ReferencesCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		displayName("table.header.test.displayname"),
		externalRef("table.header.test.external.ref"),
		assignedOn("table.header.assigned.on"),
		assignedBy("table.header.assigned.by"),
		runs("table.header.runs");
		
		private final String i18nKey;
		
		private ReferencesCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
