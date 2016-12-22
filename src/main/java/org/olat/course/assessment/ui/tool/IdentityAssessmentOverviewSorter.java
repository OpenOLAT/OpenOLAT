package org.olat.course.assessment.ui.tool;

import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.model.AssessmentNodeData;

/**
 * 
 * Initial date: 22 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityAssessmentOverviewSorter extends SortableFlexiTableModelDelegate<AssessmentNodeData> {
	
	public IdentityAssessmentOverviewSorter(SortKey orderBy, SortableFlexiTableDataModel<AssessmentNodeData> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

}
