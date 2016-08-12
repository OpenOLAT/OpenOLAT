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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.ui.model.PageRow;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageListDataModel extends DefaultFlexiTableDataModel<PageRow>
	implements SortableFlexiTableDataModel<PageRow> {
	
	private List<PageRow> backup;
	
	public PageListDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		PageListSortableDataModelDelegate sorter = new PageListSortableDataModelDelegate(orderBy, this, null);
		List<PageRow> rows = sorter.sort();
		
		Section section = null;
		for(PageRow row:rows) {
			if(section == null || !section.equals(row.getSection())) {
				row.setFirstPageOfSection(true);
				section = row.getSection();
			} else {
				row.setFirstPageOfSection(false);
			}
		}
		
		super.setObjects(rows);
	}
	
	public List<Section> filter(Section section) {
		if(section == null) {
			super.setObjects(backup);
			return null;
		} else if(backup == null) {
			return new ArrayList<>();
		}
		
		Set<Section> sectionSet = new HashSet<>();
		List<Section> sectionList = new ArrayList<>();
		List<PageRow> sectionRows = new ArrayList<>();
		for(PageRow row:backup) {
			if(row.getSection() != null) {
				if(!sectionSet.contains(row.getSection())) {
					sectionSet.add(row.getSection());
					sectionList.add(row.getSection());
				}
				
				if(section.equals(row.getSection())) {
					sectionRows.add(row);
				}
			}
		}
		super.setObjects(sectionRows);
		return sectionList;
	}

	@Override
	public void setObjects(List<PageRow> objects) {
		backup = objects;
		super.setObjects(objects);
	}

	@Override
	public Object getValueAt(int row, int col) {
		PageRow page = getObject(row);
		return getValueAt(page, col);
	}
	
	@Override
	public Object getValueAt(PageRow page, int col) {
		switch(PageCols.values()[col]) {
			case key: return page.getKey();
			case title: {
				String title = page.getTitle();
				if(title == null && page.getSection() != null) {
					title = page.getSection().getTitle();
				}
				return title;
			}
			case date: {
				if(page.getPage() != null) {
					return page.getPage().getCreationDate();
				}
				if(page.getSection() != null) {
					if(page.getSectionBeginDate() != null) {
						return page.getSection().getBeginDate();
					}
					return page.getSection().getCreationDate();
				}
				return null;
			}
			case publicationDate: return page.getLastPublicationDate();
			case status: return page.getPageStatus();
			case section: return page.getSectionTitle();
			case comment: return page.getCommentFormLink();
		}
		return null;
	}
	
	@Override
	public DefaultFlexiTableDataModel<PageRow> createCopyWithEmptyList() {
		return new PageListDataModel(getTableColumnModel());
	}

	public enum PageCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		title("table.header.title"),
		status("table.header.status"),
		date("table.header.date"),
		publicationDate("table.header.publication.date"),
		section("table.header.section"),
		comment("comment.title");
		
		private final String i18nKey;
		
		private PageCols(String i18nKey) {
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
