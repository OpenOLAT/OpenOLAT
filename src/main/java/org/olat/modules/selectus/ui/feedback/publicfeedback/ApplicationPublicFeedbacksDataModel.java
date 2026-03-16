/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.publicfeedback;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationPublicFeedbacksDataModel extends DefaultFlexiTableDataModel<PublicFeedbackRow>
implements SortableFlexiTableDataModel<PublicFeedbackRow> {
	
	private static final FeedbackCols[] COLS = FeedbackCols.values();
	
	private final Locale locale;
	
	public ApplicationPublicFeedbacksDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<PublicFeedbackRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		PublicFeedbackRow feedback = getObject(row);
		return getValueAt(feedback, col);
	}

	@Override
	public Object getValueAt(PublicFeedbackRow row, int col) {
		switch(COLS[col]) {
			case email: return row.getEmail();
			case comment: return row.getComment();
			case lastModified: return row.getLastModified();
			default: return "ERROR";
		}
	}
	
	public enum FeedbackCols implements FlexiSortableColumnDef {
		email("table.header.referee.fullname"),
		comment("table.header.expert.fullname"),
		lastModified("table.header.reference.status");
		
		private final String i18nKey;
		
		private FeedbackCols(String i18nKey) {
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
