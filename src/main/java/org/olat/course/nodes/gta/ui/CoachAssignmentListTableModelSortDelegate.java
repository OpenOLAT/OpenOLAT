package org.olat.course.nodes.gta.ui;

import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 22 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachAssignmentListTableModelSortDelegate extends SortableFlexiTableModelDelegate<IdentityAssignmentRow> {
	
	public CoachAssignmentListTableModelSortDelegate(SortKey orderBy, CoachAssignmentListTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
}
