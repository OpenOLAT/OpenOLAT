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
package org.olat.modules.lecture.ui.coach;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.lecture.model.LectureRepositoryEntryInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntriesListTableModel extends DefaultFlexiTableDataModel<LectureRepositoryEntryInfos>
implements SortableFlexiTableDataModel<LectureRepositoryEntryInfos>  {
	
	private final Locale locale;
	
	public RepositoryEntriesListTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<LectureRepositoryEntryInfos> rows = new RepositoryEntriesListTableModelSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		LectureRepositoryEntryInfos entry = getObject(row);
		return getValueAt(entry, col);
	}

	@Override
	public Object getValueAt(LectureRepositoryEntryInfos row, int col) {
		RepositoryEntry entry = row.getEntry();
		RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
		switch(LectureRepoCols.values()[col]) {
			case id: return entry.getKey();
			case repoEntry: return entry;
			case displayname: return entry.getDisplayname();
			case externalId: return entry.getExternalId();
			case externalRef: return entry.getExternalRef();
			case lifecycleLabel: return lifecycle == null ? null : lifecycle.getLabel();
			case lifecycleSoftKey: return lifecycle == null ? null : lifecycle.getSoftKey();
			case lifecycleStart: return lifecycle == null ? null : lifecycle.getValidFrom();
			case lifecycleEnd: return lifecycle == null ? null : lifecycle.getValidTo();
			case access: return entry;
			case numOfParticipants: return row.getNumOfParticipants();
			default: return "ERROR";
		}
	}
	
	public enum LectureRepoCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		repoEntry("table.header.typeimg"),
		displayname("table.header.displayname"),
		externalId("table.header.externalid"),
		externalRef("table.header.externalref"),
		lifecycleLabel("table.header.lifecycle.label"),
		lifecycleSoftKey("table.header.lifecycle.softkey"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		access("table.header.access"),
		numOfParticipants("table.header.num.participants");
		
		private final String i18nKey;
		
		private LectureRepoCols(String i18nKey) {
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
