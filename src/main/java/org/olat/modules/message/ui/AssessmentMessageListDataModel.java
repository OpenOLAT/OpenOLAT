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
package org.olat.modules.message.ui;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.modules.message.AssessmentMessageStatusEnum;

/**
 * 
 * Initial date: 14 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentMessageListDataModel extends DefaultFlexiTableDataModel<AssessmentMessageRow>
implements SortableFlexiTableDataModel<AssessmentMessageRow> {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentMessageListDataModel.class);
	
	private static final MessagesCols[] COLS = MessagesCols.values();

	private final Locale locale;
	private List<AssessmentMessageRow> backups;
	
	public AssessmentMessageListDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		try {
			List<AssessmentMessageRow> views = new AssessmentMessageListTableModelSortDelegate(orderBy, this, locale)
					.sort();
			super.setObjects(views);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public AssessmentMessageRow getMessageByKey(Long messageKey) {
		List<AssessmentMessageRow> objs = getObjects();
		return objs.stream()
				.filter(obj -> messageKey.equals(obj.getKey()))
				.findFirst().orElse(null);
	}
	
	public void filter(FlexiFiltersTab tab) {
		String id = tab == null ? "All" : tab.getId();
		List<AssessmentMessageRow> filteredRows;
		if("Planned".equals(id)) {
			filteredRows = backups.stream()
					.filter(r -> r.getStatus() == AssessmentMessageStatusEnum.planned)
					.collect(Collectors.toList());
		} else if("Published".equals(id)) {
			filteredRows = backups.stream()
					.filter(r -> r.getStatus() == AssessmentMessageStatusEnum.published)
					.collect(Collectors.toList());
		} else if("Expired".equals(id)) {
			filteredRows = backups.stream()
					.filter(r -> r.getStatus() == AssessmentMessageStatusEnum.expired)
					.collect(Collectors.toList());
		} else {
			filteredRows = backups;
		}
		super.setObjects(filteredRows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssessmentMessageRow messageRow = getObject(row);
		return getValueAt(messageRow, col);
	}

	@Override
	public Object getValueAt(AssessmentMessageRow row, int col) {
		switch(COLS[col]) {
			case message: return getText(row.getContent());
			case status: return row.getStatus();
			case creationDate: return row.getCreationDate();
			case publicationDate: return row.getPublicationDate();
			case expirationDate: return row.getExpirationDate();
			case author: return row.getAuthorFullName();
			case read: return Long.valueOf(row.getNumOfRead());
			case tools: return row.getToolLink();
			default: return "ERROR";
		}
	}
	
	private String getText(String text) {
		if(text != null && text.length() > 64) {
			text = Formatter.truncate(text, 64);
		}
		return text;
	}
	
	@Override
	public void setObjects(List<AssessmentMessageRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}
	
	public enum MessagesCols implements FlexiSortableColumnDef {
		message("table.header.message"),
		status("table.header.status"),
		creationDate("table.header.creation.date"),
		publicationDate("table.header.publication.date"),
		expirationDate("table.header.expiration.date"),
		author("table.header.author"),
		read("table.header.read"),
		tools("table.header.actions");

		private final String i18nKey;

		private MessagesCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
