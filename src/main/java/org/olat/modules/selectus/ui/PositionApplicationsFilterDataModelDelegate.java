/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import static org.olat.modules.selectus.ui.PositionApplicationsDataModel.toYear;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.ui.PositionApplicationsDataModel.Fields;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.fql.FilterableFlexiTableDataModelDelegate;
import org.olat.modules.selectus.ui.model.AppToCategory;
import org.olat.modules.selectus.ui.model.ApplicationRow;

/**
 * 
 * Initial date: 8 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionApplicationsFilterDataModelDelegate extends FilterableFlexiTableDataModelDelegate<ApplicationRow> {
	
	private static final Fields[] COLS = Fields.values();
	
	private final PositionApplicationsDataModel applicationsTableModel;
	
	public PositionApplicationsFilterDataModelDelegate(PositionApplicationsDataModel tableModel, Translator translator) {
		super(tableModel, translator);
		applicationsTableModel = tableModel;
	}

	@Override
	public boolean accept(FlexiColumnModel col, ApplicationRow row, String searchString) {
		ApplicationLight app = row.getApplication();
		if(col.getColumnIndex() >= 0 && col.getColumnIndex() < COLS.length) {
			switch(COLS[col.getColumnIndex()]) {
				case categories: {
					List<AppToCategory> categories = row.getCategories();
					if(categories != null && !categories.isEmpty()) {
						for(AppToCategory category:categories) {
							if(acceptString(category.getCategoryName(), searchString)) {
								return true;
							}
						}
					}
					return false;
				}
				case project:
					return acceptString(app.getProject().getTitle(), searchString)
							|| acceptString(app.getProject().getKeywords(), searchString)
							|| acceptString(app.getProject().getDescription(), searchString);
				case experts: return acceptSlash(row.getRefereesStats().getNumOfSubmittedExperts(),
						row.getRefereesStats().getNumOfExperts(), searchString);
				case recommendations: return acceptSlash(row.getRefereesStats().getNumOfSubmittedRecommendations(),
						row.getRefereesStats().getNumOfRecommendations(), searchString);
				case comparativeExperts: return acceptSlash(row.getRefereesStats().getNumOfSubmittedComparativeExperts(),
						row.getRefereesStats().getNumOfComparativeExperts(), searchString);
				case assignments: return acceptSlash(row.getNumOfAssignedRatings(), row.getNumOfAssignments(), searchString);
				// search year as a string
				case highestDegreeYearPhD: return acceptString(toYear(getValueAt(row, Fields.highestDegreeYearPhD.ordinal())), searchString);
				case highestDegreeYear: return acceptString(toYear(getValueAt(row, Fields.highestDegreeYear.ordinal())), searchString);
				case dissertationDate: return acceptString(toYear(getValueAt(row, Fields.dissertationDate.ordinal())), searchString);
				case habilitationDate: return acceptString(toYear(getValueAt(row, Fields.habilitationDate.ordinal())), searchString);
				// search date as a string
				case submittedDate: return acceptString(toDate(getValueAt(row, Fields.submittedDate.ordinal())), searchString);
				case applicationStatusDate: return acceptString(toDate(getValueAt(row, Fields.applicationStatusDate.ordinal())), searchString);
				// decision
				case decision: return acceptDecision(getValueAt(row, Fields.decision.ordinal()), searchString);
				case additionalNationalities:
					System.out.println(searchString);
					return false;
				default: // do nothing
			}
		}

		Object val = getValueAt(row, col.getColumnIndex());
		if(val instanceof String ) {
			return acceptString((String)val, searchString);
		}
		return val instanceof Integer && ((Integer)val).toString().equals(searchString);
	}
	
	private Object getValueAt(ApplicationRow row, int col) {
		return applicationsTableModel.getValueAt(row, col);
	}
	
	private boolean acceptDecision(Object val, String searchString) {
		if(val instanceof Integer) {
			int value = ((Integer)val).intValue();
			switch(value) {
				case 1: return "c".equalsIgnoreCase(searchString);
				case 2: return "b".equalsIgnoreCase(searchString);
				case 3: return "a".equalsIgnoreCase(searchString);
				default: return false;
			}
		}
		return false;
	}
	
	private boolean acceptSlash(int numOf1, int numOf2, String searchString) {
		String val = numOf1 + " / " + numOf2;
		return acceptString(val, searchString);
	}
	
	private String toDate(Object val) {
		if(val instanceof Date date) {
			return DateCellRenderer.format(date);
		}
		return val == null ? null : val.toString();
	}

}
