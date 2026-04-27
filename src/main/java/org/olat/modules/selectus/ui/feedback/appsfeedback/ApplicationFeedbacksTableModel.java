/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.selectus.model.ApplicationFeedback;

/**
 * The model to show the feedbacks of an application to the committee.
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationFeedbacksTableModel  extends DefaultFlexiTableDataModel<ApplicationFeedbackRow>
implements SortableFlexiTableDataModel<ApplicationFeedbackRow> {

	private static final AppFeedCols[] COLS = AppFeedCols.values();
	
	private final Locale locale;
	
	public ApplicationFeedbacksTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ApplicationFeedbackRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ApplicationFeedbackRow ref = getObject(row);
		return getValueAt(ref, col);
	}

	@Override
	public Object getValueAt(ApplicationFeedbackRow row, int col) {
		switch(COLS[col]) {
			case fullName: return row.getFullName();
			case feedbackStatus: return row.getFeedback().getReferenceStatus();
			case submissionDeadline: return getSubmissionDeadline(row);
			case delete: return row.getDeleteLink();
			default: return "ERROR";
		}
	}
	
	private Date getSubmissionDeadline(ApplicationFeedbackRow row) {
		ApplicationFeedback feedback = row.getFeedback();
		Date deadline = feedback.getDeadline();
		if(deadline == null) {
			deadline = feedback.getConfiguration().getDeadline();
		}
		return deadline;
	}
	
	public enum AppFeedCols implements FlexiSortableColumnDef {
		fullName("table.header.feedback.fullname"),
		feedbackStatus("table.header.reference.status"),
		submissionDeadline("table.header.reference.submission.deadline"),
		delete("table.header.action");
		
		private final String i18nKey;
		
		private AppFeedCols(String i18nKey) {
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
