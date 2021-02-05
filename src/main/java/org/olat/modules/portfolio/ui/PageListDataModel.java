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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageListDataModel extends DefaultFlexiTableDataModel<PortfolioElementRow>
	implements SortableFlexiTableDataModel<PortfolioElementRow> {
	
	private static final Logger log = Tracing.createLoggerFor(PageListDataModel.class);
	
	private boolean flat;
	private final Locale locale;
	private List<PortfolioElementRow> backup;

	public PageListDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	public boolean isFlat() {
		return flat;
	}

	public void setFlat(boolean flat) {
		this.flat = flat;
	}

	@Override
	public void sort(SortKey orderBy) {
		PageListSortableDataModelDelegate sorter = new PageListSortableDataModelDelegate(orderBy, this, flat, locale);
		List<PortfolioElementRow> rows;
		try {
			rows = sorter.sort();
		} catch (IllegalArgumentException e) {
			log.error("Cannot sort with: " + orderBy , e);
			return;
		}
		
		// This say where is the link to create a new entry
		// if a section has assignments, it's at the end of
		// the section. If there isn't any assignment, it
		// under the section.
		boolean lastNewEntry = false;
		PortfolioElementRow previousRow = null;
		for(PortfolioElementRow row:rows) {
			if(row.isSection()) {
				if(lastNewEntry && previousRow != null) {
					previousRow.setNewEntry(true);
				}
				
				if(row.isAssignments()) {
					lastNewEntry = true;
				} else {
					lastNewEntry = false;
					row.setNewEntry(true);
				}
			} else {
				row.setNewEntry(false);
			}
			previousRow = row;
		}
		if(lastNewEntry && previousRow != null) {
			previousRow.setNewEntry(true);
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
		List<PortfolioElementRow> sectionRows = new ArrayList<>();
		for(PortfolioElementRow row:backup) {
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
	public void setObjects(List<PortfolioElementRow> objects) {
		backup = objects;
		super.setObjects(objects);
	}

	@Override
	public Object getValueAt(int row, int col) {
		PortfolioElementRow page = getObject(row);
		return getValueAt(page, col);
	}
	
	@Override
	public Object getValueAt(PortfolioElementRow page, int col) {
		switch(PageCols.values()[col]) {
			case key: return page.getKey();
			case type: return page;
			case title: {
				String title = null;
				if(page.isPage()) {
					title = page.getTitle();
				} else if(page.isSection()) {
					title = page.getSectionTitle();
				} else if(page.isPendingAssignment()) {
					title = page.getAssignmentTitle();
				}
				return title;
			}
			case date: {
				Date creationDate = null;
				if(page.isPage()) {
					creationDate = page.getPage().getCreationDate();
				} else if(page.isSection()) {
					creationDate = page.getSection().getBeginDate();
					if(creationDate == null) {
						creationDate = page.getSection().getCreationDate();
					}
				} else if(page.isPendingAssignment()) {
					creationDate = page.getAssignment().getCreationDate();
				}
				return creationDate;
			}
			case publicationDate: return page.getLastPublicationDate();
			case pageStatus:
			case status: {
				if(page.isPage()) {
					return page.getPageStatus();
				}
				if(page.isSection()) {
					return page.getSectionStatus();
				}
				return null;
			}
			case categories: return page.getPageCategories();
			case section: return page.getSectionTitle();
			case up: {
				if(page.isPendingAssignment()) {
					return page.getUpAssignmentLink() != null && page.getUpAssignmentLink().isEnabled();
				}
				return Boolean.FALSE;
			}
			case down: {
				if(page.isPendingAssignment()) {
					return page.getDownAssignmentLink() != null && page.getDownAssignmentLink().isEnabled();
				}
				return Boolean.FALSE;
			}
			case comment: return page.getCommentFormLink();
			case viewerStatus: return page;
		}
		return null;
	}

	
	@Override
	public DefaultFlexiTableDataModel<PortfolioElementRow> createCopyWithEmptyList() {
		return new PageListDataModel(getTableColumnModel(), locale);
	}

	public enum PageCols implements FlexiSortableColumnDef {
		type("table.header.type", false),
		key("table.header.key", true),
		title("table.header.title", true),
		status("table.header.status", true),
		date("table.header.date", true),
		publicationDate("table.header.publication.date", true),
		categories("table.header.categories", false),
		section("table.header.section", true),
		up("table.header.up", false),
		down("table.header.down", false),
		comment("comment.title", true),
		pageStatus("table.header.status.user", true),
		viewerStatus("table.header.status.viewer", true);
		
		private final String i18nKey;
		private final boolean sortable;
		
		private PageCols(String i18nKey, boolean sortable) {
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
	}
}
