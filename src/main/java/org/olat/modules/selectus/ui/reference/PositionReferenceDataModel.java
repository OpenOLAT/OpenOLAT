/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
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

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.comparator.LastnameComparator;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionReferenceDataModel extends DefaultFlexiTableDataModel<PositionReferenceRow>
	implements SortableFlexiTableDataModel<PositionReferenceRow>, FilterableFlexiTableModel, FlexiBusinessPathModel {
	
	private static final ReferenceCols[] COLS = ReferenceCols.values();

	private Position position;
	private final Locale locale;
	private final Translator translator;
	private final SalutationGenerator salutationGenerator;
	private final RecruitingModule recruitingModule;
	
	private List<PositionReferenceRow> backups;
	
	public PositionReferenceDataModel(FlexiTableColumnModel columnsModel, Position position, SalutationGenerator salutationGenerator, Translator translator, Locale locale) {
		super(columnsModel);
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		this.locale = locale;
		this.position = position;
		this.translator = translator;
		this.salutationGenerator = salutationGenerator;
	}
	
	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if(StringHelper.containsNonWhitespace(key) && !"showall".equals(key)) {
			List<PositionReferenceRow> filteredRows = new ArrayList<>();
			for(PositionReferenceRow row:backups) {
				ReferenceType type = row.getReference().getReferenceType();
				if(type != null && key != null && key.equals(type.name())) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<PositionReferenceRow> views = new PositionReferenceSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}
	
	public boolean flexiSearch(String searchString, String query) {
		boolean allErrors = false;
		if(StringHelper.containsNonWhitespace(searchString) || StringHelper.containsNonWhitespace(query)) {
			//TODO flexi ql
			super.setObjects(backups);
		} else {
			super.setObjects(backups);
		}
		return allErrors;
	}
	
	@Override
	public String getUrl(Component source, Object object, String action) {
		if("app".equals(action) && object instanceof PositionReferenceRow) {
			return ((PositionReferenceRow)object).getApplicationUrl();
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		PositionReferenceRow ref = getObject(row);
		return getValueAt(ref, col);
	}

	@Override
	public Object getValueAt(PositionReferenceRow row, int col) {
		Application app = row.getApplication();
		Project project = app == null ? null : app.getProject();
		
		switch(COLS[col]) {
			case fullName: return RecruitingHelper.formatPersonLastnameFirstname(row.getReference());
			case email: return row.getReference().getEmail();
			case type: return getReferenceType(row);
			case application: return row.getApplicationsLinks();
			case applicationId: return getApplicationId(row);
			case projectTitle: return project == null ? null : project.getTitle();
			case projectAcronym: return project == null ? null : project.getAcronym();
			case projectKeywords: return project == null ? null : project.getKeywords();
			case projectDisciplines: return project == null ? null : project.getDisciplines();
			case projectStartDate: return project == null ? null : project.getStartDate();
			case projectDuration: return project == null ? null : project.getDuration();
			case projectFinancialImpact1: return project == null ? null : recruitingModule.getApplicationProjectFinancialImpact1Type()
					.toTypedValue(project.getFinancialImpact1());
			case projectFinancialImpact2: return project == null ? null : recruitingModule.getApplicationProjectFinancialImpact2Type()
					.toTypedValue(project.getFinancialImpact2());
			case projectFinancialImpact3: return project == null ? null : recruitingModule.getApplicationProjectFinancialImpact3Type()
					.toTypedValue(project.getFinancialImpact3());
			case projectFinancialImpact4: return project == null ? null : recruitingModule.getApplicationProjectFinancialImpact4Type()
					.toTypedValue(project.getFinancialImpact4());
			case projectFinancialImpact5: return project == null ? null : recruitingModule.getApplicationProjectFinancialImpact5Type()
					.toTypedValue(project.getFinancialImpact5());
			case projectDescription: return project == null ? null : project.getDescription();
			case referenceStatus: return row;
			case submissionDeadline: return getSubmissionDeadline(row);
			case dateInvitation: return row.getReference().getDateInvitation();
			case dateLastReminder: return row.getReference().getDateLastReminder();
			case sendMail: return row.getSendLink();
			case viewLetter: return row.getDocumentLink();
			case categories: return row.getCategories();
			case decision: return app == null ? null : app.getDecision();
			case applicationStatus: return getApplicationStatus(row);
			case adminNote: return row.getReference().getAdminNote();
			default: return "ERROR";
		}
	}
	
	private Object getApplicationId(PositionReferenceRow row) {
		if(row.getApplication() != null) {
			return row.getApplication().getId();
		}
		if(row.getApplications() != null && !row.getApplications().isEmpty()) {
			return row.getApplications().stream().map(Application::getId)
				.map(id -> id.toString())
				.collect(Collectors.joining(", "));
		}
		return null;
	}
	
	private String getApplicationStatus(PositionReferenceRow row) {
		if(row.getApplication() != null) {
			return translator.translate("application.status.".concat(row.getApplication().getApplicationStatus().name()));
		}
		if(row.getApplications() != null && !row.getApplications().isEmpty()) {
			List<Application> apps = row.getApplications();
			Collection<String> status = apps.stream()
					.map(app -> translator.translate("application.status.".concat(app.getApplicationStatus().name())))
					.collect(Collectors.toSet());
			if(status.size() > 1) {
				List<String> statusList = new ArrayList<>(status);
				Collections.sort(statusList);
				status = statusList;
			}
			return String.join(", ", status);
		}
		return null;
	}
	
	private Date getSubmissionDeadline(PositionReferenceRow row) {
		Date deadline = row.getReference().getSubmissionDeadline();
		if(deadline == null) {
			ReferenceType t = row.getReference().getReferenceType();
			if(t == ReferenceType.expert) {
				deadline = position.getExpertRecommandationDeadline();
			} else if(t == ReferenceType.recommendation) {
				deadline = position.getRefereeRecommandationDeadline();
			}
		}
		return deadline;
	}
	
	private String getReferenceType(PositionReferenceRow row) {
		ReferenceType t = row.getReference().getReferenceType();
		if(t == ReferenceType.expert) {
			return translator.translate("table.header.reference.type.expert");
		}
		if(t == ReferenceType.recommendation) {
			return translator.translate("table.header.reference.type.recommendation");
		}
		if(t == ReferenceType.comparativeAssessmentExpert) {
			return translator.translate("table.header.reference.type.comparativeAssessmentExpert");
		}
		return null;
	}

	@Override
	public void setObjects(List<PositionReferenceRow> objects) {
		backups = objects;
		super.setObjects(objects);
	}
	
	public enum ReferenceCols implements FlexiSortableColumnDef {
		fullName("table.header.reference.fullname", true),
		email("table.header.reference.email", true),
		type("table.header.reference.type", true),
		application("table.header.reference.application", true),
		applicationId("table.header.reference.application.id", true),
		projectTitle("table.header.project.title", true),
		projectAcronym("table.header.project.acronym", true),
		projectKeywords("table.header.project.keywords", true),
		projectDisciplines("table.header.project.disciplines", true),
		projectStartDate("table.header.project.start.date", true),
		projectDuration("table.header.project.duration", true),
		projectFinancialImpact1("table.header.project.impactFactor.1", true),
		projectFinancialImpact2("table.header.project.impactFactor.2", true),
		projectFinancialImpact3("table.header.project.impactFactor.3", true),
		projectFinancialImpact4("table.header.project.impactFactor.4", true),
		projectFinancialImpact5("table.header.project.impactFactor.5", true),
		projectDescription("table.header.project.description", true),
		referenceStatus("table.header.reference.status", true),
		submissionDeadline("table.header.reference.submission.deadline", true),
		dateInvitation("table.header.reference.date.invitation", true),
		dateLastReminder("table.header.reference.date.last.reminder", true),
		sendMail("table.header.action", false),
		viewLetter("table.header.letter", false),
		categories("table.header.categories", true),
		applicationStatus("table.header.application.status", true),
		decision("edit.application.decision", true),
		adminNote("table.header.admin.note", false);
		
		private final String i18nKey;
		private final boolean sortable;
		
		private ReferenceCols(String i18nKey, boolean sortable) {
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
		
		public static ReferenceCols getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return fullName;
		}
	}
	
	private static class PositionReferenceSortDelegate extends SortableFlexiTableModelDelegate<PositionReferenceRow> {
		
		public PositionReferenceSortDelegate(SortKey orderBy, PositionReferenceDataModel tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}
		
		@Override
		protected void sort(List<PositionReferenceRow> rows) {
			int columnIndex = getColumnIndex();
			if(columnIndex == ReferenceCols.referenceStatus.ordinal()) {
				Collections.sort(rows, new ReferenceStatusComparator());
			} else if(columnIndex == ReferenceCols.application.ordinal()) {
				Collections.sort(rows, new ApplicationComparator());
			} else {
				super.sort(rows);
			}
		}
		
		private class ApplicationComparator implements Comparator<PositionReferenceRow> {
			
			private final LastnameComparator lastNameComparator = new LastnameComparator();

			@Override
			public int compare(PositionReferenceRow o1, PositionReferenceRow o2) {
				int c = 0;
				if(o1 == null || o2 == null) {
					c = compareNullObjects(o1, o2);
				} else if(o1.getReference() == null || o2.getReference() == null) {
					c = compareNullObjects(o1.getReference(), o2.getReference());
				} else {
					Application a1 = o1.getApplication();
					if(a1 == null && o1.getApplications() != null && !o1.getApplications().isEmpty()) {
						a1 = o1.getApplications().get(0);
					}
					Application a2 = o2.getApplication();
					if(a2 == null && o2.getApplications() != null && !o2.getApplications().isEmpty()) {
						a2 = o2.getApplications().get(0);
					}
					c = lastNameComparator.compare(a1, a2);
				}
				return c;
			}	
		}
		
		private class ReferenceStatusComparator implements Comparator<PositionReferenceRow> {

			@Override
			public int compare(PositionReferenceRow o1, PositionReferenceRow o2) {
				int c = 0;
				if(o1 == null || o2 == null) {
					c = compareNullObjects(o1, o2);
				} else if(o1.getReference() == null || o2.getReference() == null) {
					c = compareNullObjects(o1.getReference(), o2.getReference());
				} else {
					c = ReferenceHelper.compareStatus(o1.getReference().getReferenceStatus(), o1.getReference().getRequestStatus(),
							o2.getReference().getReferenceStatus(), o2.getReference().getRequestStatus());
				}
				return c;
			}	
		}
	}
}
