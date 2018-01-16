package org.olat.modules.lecture.ui;

import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 16 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantListSortDelegate extends SortableFlexiTableModelDelegate<ParticipantRow> {

	public ParticipantListSortDelegate(SortKey orderBy, ParticipantListDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

}
