package org.olat.group.ui.main;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.group.ui.main.MemberListTableModel.Cols;

/**
 * 
 * Initial date: 18.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberListTableSort extends SortableFlexiTableModelDelegate<MemberView> {
	
	
	
	public MemberListTableSort(SortKey orderBy, SortableFlexiTableDataModel<MemberView> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<MemberView> rows) {
		int columnIndex = getColumnIndex();
		Cols column = Cols.values()[columnIndex];
		switch(column) {
			case role:
				Collections.sort(rows, new RoleMemberViewComparator());
				break;
			case groups:
				Collections.sort(rows, new GroupMemberViewComparator(getCollator()));
				break;
			default: {
				super.sort(rows);
			}
		}
	}

	private static class RoleMemberViewComparator implements Comparator<MemberView> {
		
		private final CourseMembershipComparator comparator = new CourseMembershipComparator();

		@Override
		public int compare(MemberView o1, MemberView o2) {
			return comparator.compare(o1.getMembership(), o2.getMembership());
		}
	}
}
