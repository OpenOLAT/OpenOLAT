package org.olat.modules.portfolio.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.PageRow;

/**
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageListSortableDataModelDelegate extends SortableFlexiTableModelDelegate<PageRow> {
	
	public PageListSortableDataModelDelegate(SortKey orderBy, SortableFlexiTableDataModel<PageRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<PageRow> rows) {
		Collections.sort(rows, new ClassicComparator());
	}

	private class ClassicComparator implements Comparator<PageRow> {
		
		@Override
		public int compare(PageRow r1, PageRow r2) {
			Section s1 = r1.getSection();
			Section s2 = r2.getSection();
			if(s1 == null && s2 != null) {
				return -1;
			}
			if(s1 != null && s2 == null) {
				return 1;
			}

			int c = compare(s1, s2);
			if(c == 0) {
				Page p1 = r1.getPage();
				Page p2 = r2.getPage();
				if(p1 == null && p2 != null) {
					return -1;
				}
				if(p1 != null && p2 == null) {
					return 1;
				}
				c = compareDateAndTimestamps(p1.getCreationDate(), p2.getCreationDate());
			}
			return c;
		}
		
		private int compare(Section s1, Section s2) {
			Date b1 = s1.getBeginDate();
			if(b1 == null) {
				b1 = s1.getCreationDate();
			}
			Date b2 = s2.getBeginDate();
			if(b2 == null) {
				b2 = s2.getBeginDate();
			}
			
			return compareDateAndTimestamps(b1, b2);
		}
	}
	
}
