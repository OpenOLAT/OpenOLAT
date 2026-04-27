/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
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
import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.model.AppToCategory;
import org.olat.user.propertyhandlers.UserPropertyHandler;

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
	
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private List<PositionFeedbackRow> backupRows;
	
	public PositionFeedbacksTableModel(FlexiTableColumnModel columnsModel, List<UserPropertyHandler> userPropertyHandlers,
			Translator translator) {
		super(columnsModel);
		this.translator = translator;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<PositionFeedbackRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || (filters != null && !filters.isEmpty())) {
			final String loweredSearchString = searchString == null || !StringHelper.containsNonWhitespace(searchString)
					? null : searchString.toLowerCase();
			final Set<String> decisions = getFilteredList(filters, PositionFeedbacksController.FILTER_DECISION);
			final Set<String> referenceStatus = getFilteredList(filters, PositionFeedbacksController.FILTER_FEEDBACK_STATUS);
			final Set<String> applicationStatus = getFilteredList(filters, PositionFeedbacksController.FILTER_APPLICATION_STATUS);
			final Set<String> categories = getFilteredList(filters, PositionFeedbacksController.FILTER_CATEGORIES);

			List<PositionFeedbackRow> filteredRows = new ArrayList<>(backupRows.size());
			for(PositionFeedbackRow row:backupRows) {
				boolean accept = accept(loweredSearchString, row)
						&& acceptDecision(decisions, row)
						&& acceptReferenceStatus(referenceStatus, row)
						&& acceptApplicationStatus(applicationStatus, row)
						&& acceptCategories(categories, row);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private Set<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? Set.copyOf(filterValues) : Set.of();
		}
		return Set.of();
	}
	
	private boolean acceptDecision(Set<String> status, PositionFeedbackRow row) {
		if(status == null || status.isEmpty()) return true;
		
		Integer decision = row.getApplication().getDecision();
		if((decision == null || decision.intValue() <= 0) && status.contains(PositionFeedbacksController.FILTER_NULL_KEY)
				|| (decision != null && status.contains(decision.toString()))) {
			return true;
		}
		return false;
	}
	
	private boolean acceptReferenceStatus(Set<String> status, PositionFeedbackRow row) {
		if(status == null || status.isEmpty()) return true;
		
		ReferenceStatus referenceStatus = row.getFeedback().getReferenceStatus();
		return status.contains(referenceStatus.name());
	}
	
	private boolean acceptApplicationStatus(Set<String> status, PositionFeedbackRow row) {
		if(status == null || status.isEmpty()) return true;
		
		ApplicationStatus applicationStatus = row.getApplication().getApplicationStatus();
		return status.contains(applicationStatus.name());
	}
	
	private boolean acceptCategories(Set<String> categories, PositionFeedbackRow row) {
		if(categories == null || categories.isEmpty()) return true;
		
		if(row.getCategories() != null && !row.getCategories().isEmpty()) {
			for(AppToCategory cat:row.getCategories()) {
				String name = cat.getCategoryName();
				if(cat.isAdministrative()) {
					name = "a:" + name;
				}
				if(categories.contains(name)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean accept(String searchValue, PositionFeedbackRow row) {
		if(searchValue == null) return true;
		
		for(UserPropertyHandler handler:this.userPropertyHandlers) {
			String val = handler.getUserProperty(row.getMember().getUser(), translator.getLocale());
			if(accept(searchValue, val)) {
				return true;
			}
		}

		return accept(searchValue, row.getFullName())
				|| accept(searchValue, row.getApplication().getPerson().getFirstName())
				|| accept(searchValue, row.getApplication().getPerson().getLastName())
				|| accept(searchValue, row.getApplication().getPerson().getMail())
				|| accept(searchValue, FeedbackHelper.getEmail(row.getMember()));
	}
	
	private boolean accept(String searchValue, String val) {
		return val != null && val.toLowerCase().contains(searchValue);
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
				case decision: return row.getApplication().getDecision();
				case applicationStatus: return translator.translate("application.status.".concat(row.getApplication().getApplicationStatus().name()));
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
