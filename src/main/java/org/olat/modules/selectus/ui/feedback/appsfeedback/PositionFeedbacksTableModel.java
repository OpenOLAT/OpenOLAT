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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionFeedbacksTableModel extends DefaultFlexiTableDataModel<PositionFeedbackRow>
implements SortableFlexiTableDataModel<PositionFeedbackRow>, FilterableFlexiTableModel,
	FlexiBusinessPathModel{

	private static final PositionFeedCols[] COLS = PositionFeedCols.values();
	
	private final Locale locale;
	private final Translator translator;
	private final SalutationGenerator salutationGenerator;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private List<PositionFeedbackRow> backupRows;
	
	public PositionFeedbacksTableModel(FlexiTableColumnModel columnsModel, SalutationGenerator salutationGenerator,
			List<UserPropertyHandler> userPropertyHandlers, Translator translator, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.translator = translator;
		this.salutationGenerator = salutationGenerator;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<PositionFeedbackRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString)) {	
			//TODO flexi ql super.setObjects(results.getRows());
			super.setObjects(backupRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	@Override
	public String getUrl(Component source, Object object, String action) {
		if("app".equals(action) && object instanceof PositionFeedbackRow) {
			return ((PositionFeedbackRow)object).getApplicationUrl();
		}
		return null;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		PositionFeedbackRow ref = getObject(row);
		return getValueAt(ref, col);
	}

	@Override
	public Object getValueAt(PositionFeedbackRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case fullName: return row.getFullName();
				case type: return getType();
				case email: return FeedbackHelper.getEmail(row.getMember());
				case application: return getApplicationFullName(row);
				case decision: return row.getFeedback().getApplication().getDecision();
				case applicationStatus: return translator.translate("application.status.".concat(row.getFeedback().getApplication().getApplicationStatus().name()));
				case categories: return row.getCategories();
				case feedbackStatus: return row.getFeedback().getReferenceStatus();
				case submissionDeadline: return getSubmissionDeadline(row);
				case dateRequest: return row.getFeedback().getRequest();
				case dateLastReminder: return row.getFeedback().getLastReminder();
				case sendMail: return row.getSendLink();
				case viewFeedback: return row.hasComment();
				default: return "ERROR";
			}
		} else if(col >= PositionFeedbacksController.USER_PROP_OFFSET) {
			int propIndex = col - PositionFeedbacksController.USER_PROP_OFFSET;
			UserPropertyHandler prop = userPropertyHandlers.get(propIndex);
			return prop.getUserProperty(row.getMember().getUser(), translator.getLocale());
		}
		return "ERROR";
	}
	
	private String getApplicationFullName(PositionFeedbackRow row) {
		Application application = row.getFeedback().getApplication();
		return RecruitingHelper.formatFullName(application, translator);
	}
	
	private String getType() {
		return translator.translate("role.faculty.member");
	}
	
	private Date getSubmissionDeadline(PositionFeedbackRow row) {
		ApplicationFeedback feedback = row.getFeedback();
		Date deadline = feedback.getDeadline();
		if(deadline == null) {
			deadline = feedback.getConfiguration().getDeadline();
		}
		return deadline;
	}
	
	@Override
	public void setObjects(List<PositionFeedbackRow> objects) {
		this.backupRows = objects;
		super.setObjects(objects);
	}
	
	public enum PositionFeedCols implements FlexiSortableColumnDef {
		fullName("table.header.feedback.fullname"),
		type("table.header.reference.type"),
		email("table.header.reference.email"),
		application("table.header.feedback.application"),
		categories("table.header.categories"),
		applicationStatus("table.header.application.status"),
		decision("edit.application.decision"),
		feedbackStatus("table.header.reference.status"),
		submissionDeadline("table.header.reference.submission.deadline"),
		dateRequest("table.header.feedback.date.request"),
		dateLastReminder("table.header.feedback.date.last.reminder"),
		sendMail("table.header.action"),
		viewFeedback("table.header.feedback");
		
		private final String i18nKey;
		
		private PositionFeedCols(String i18nKey) {
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
