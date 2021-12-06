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
package org.olat.modules.fo.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.model.ForumThread;

/**
 * 
 * Initial date: 10.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ThreadListDataModel extends DefaultFlexiTableDataModel<ForumThread>
	implements SortableFlexiTableDataModel<ForumThread> {
	
	private final Translator translator;
	
	public ThreadListDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ForumThread> views = new ThreadListDataModelSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		ForumThread thread = getObject(row);
		return getValueAt(thread, col);
	}

	@Override
	public Object getValueAt(ForumThread row, int col) {
		switch(ThreadListCols.values()[col]) {
			case type: return row.getStatusCode();
			case thread: return StringHelper.escapeHtml(row.getTitle());
			case creator: {
				if(StringHelper.containsNonWhitespace(row.getPseudonym())) {
					return row.getPseudonym();
				}
				if(row.isGuest()) {
					return translator.translate("anonymous.poster");
				}
				return row.getCreatorFullname();
			}
			case lastModified: return row.getLastModified();
			case markedMessages: return row.getMarkedMessages();
			case unreadMessages: return row.getNewMessages();
			case totalMessages: return row.getNumOfPosts();
			default: return "ERROR";
		}
	}

	public enum ThreadListCols implements FlexiSortableColumnDef {
		type("table.header.typeimg"),
		thread("table.thread"),
		creator("table.userfriendlyname"),
		lastModified("table.lastModified"),
		markedMessages("table.marked"),
		unreadMessages("table.unread"),
		totalMessages("table.total"),
		select("select");
		
		private final String i18nKey;
	
		private ThreadListCols(String i18nKey) {
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
	
	public class ThreadListDataModelSort extends SortableFlexiTableModelDelegate<ForumThread> {
		
		public ThreadListDataModelSort(SortKey orderBy, SortableFlexiTableDataModel<ForumThread> tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}
		
		@Override
		public List<ForumThread> sort() {
			SortableFlexiTableDataModel<ForumThread> model = getTableModel();
			int rowCount = model.getRowCount();
			List<ForumThread> rows = new ArrayList<>(rowCount);
			for(int i=0; i<rowCount; i++) {
				rows.add(model.getObject(i));
			}
			Collections.sort(rows, new StickyComparator(isAsc()));
			return rows;
		}
		
		public class StickyComparator extends DefaultComparator {
			
			private final boolean asc;
			
			public StickyComparator(boolean asc) {
				this.asc = asc;
			}
			
			@Override
			public int compare(ForumThread t1, ForumThread t2) {
				boolean s1 = Status.getStatus(t1.getStatusCode()).isSticky();
				boolean s2 = Status.getStatus(t2.getStatusCode()).isSticky();
				
				if(s1 && !s2) {
					return -1;
				}
				if(!s1 && s2) {
					return 1;
				}
				return (asc ? 1 : -1) * super.compare(t1, t2);
			}
		}
	}
}
