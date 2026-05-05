/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 20.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectForInvitationEmailDataModel extends DefaultFlexiTableDataModel<ReferenceInvitationRow>
	implements SortableFlexiTableDataModel<ReferenceInvitationRow> {
	
	private static final IRCols[] COLS = IRCols.values();
	
	private final Translator translator;
	
	public SelectForInvitationEmailDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ReferenceInvitationRow> views = new ReferenceSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ReferenceInvitationRow ref = getObject(row);
		return getValueAt(ref, col);
	}

	@Override
	public Object getValueAt(ReferenceInvitationRow row, int col) {
		switch(COLS[col]) {
			case fullName: return RecruitingHelper.formatPersonLastnameFirstname(row.getReference());
			case email: return row.getEmail();
			case type: return getReferenceType(row);
			case application: return getApplicationFirstNameLastName(row);
			case status: return row.getReferenceStatus();
			case submissionDeadline: return row.getSubmissionDeadline();
			case dateInvitation: return row.getDateInvitation();
			case dateLastReminder: return row.getDateLastReminder();
			default: return "ERROR";
		}
	}
	
	private String getApplicationFirstNameLastName(ReferenceInvitationRow row) {
		if(row.getApplication() != null) {
			return RecruitingHelper.formatPersonLastnameFirstname(row.getApplication().getPerson());
		}
		if(row.getApplications() != null) {
			return RecruitingHelper.formatPersonLastnameFirstname(row.getApplications());
		}
		return null;
	}
	
	private String getReferenceType(ReferenceInvitationRow row) {
		ReferenceType t = row.getReferenceType();
		if(t == ReferenceType.expert) {
			return translator.translate("table.header.reference.type.expert");
		}
		if(t == ReferenceType.recommendation) {
			return translator.translate("table.header.reference.type.recommendation");
		}
		if(t== ReferenceType.comparativeAssessmentExpert) {
			return translator.translate("table.header.reference.type.comparativeAssessmentExpert");
		}
		return null;
	}
	
	public enum IRCols implements FlexiSortableColumnDef {
		fullName("table.header.reference.fullname", true),
		email("table.header.reference.email", true),
		type("table.header.reference.type", true),
		application("table.header.reference.application", true),
		status("table.header.reference.status", true),
		submissionDeadline("table.header.reference.submission.deadline", true),
		dateInvitation("table.header.reference.date.invitation", true),
		dateLastReminder("table.header.reference.date.last.reminder", true);
		
		private final String i18nKey;
		private final boolean sortable;
		
		private IRCols(String i18nKey, boolean sortable) {
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
		
		public static IRCols getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return fullName;
		}
	}
	
	private static class ReferenceSortDelegate extends SortableFlexiTableModelDelegate<ReferenceInvitationRow> {
		
		public ReferenceSortDelegate(SortKey orderBy, SelectForInvitationEmailDataModel tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}
		
		@Override
		protected void sort(List<ReferenceInvitationRow> rows) {
			super.sort(rows);
		}
	}
}
