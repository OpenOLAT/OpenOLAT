/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.ui.committee.assignment.AssigneeRow;
import org.olat.modules.selectus.ui.committee.list.CommitteeMemberRow;
import org.olat.modules.selectus.ui.model.ApplicationRow;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentsCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		Object object = source.getFormItem().getTableDataModel().getObject(row);
		if(object instanceof ApplicationRow) {
			ApplicationRow appRow = (ApplicationRow)object;
			render(target, appRow.getNumOfAssignedRatings(), appRow.getNumOfAssignments());
		} else if(object instanceof AssigneeRow) {
			AssigneeRow assigneeRow = (AssigneeRow)object;
			render(target, assigneeRow.getNumOfRatings(), assigneeRow.getNumOfAssignments());
		} else if(object instanceof CommitteeMemberRow) {
			CommitteeMemberRow memberRow = (CommitteeMemberRow)object;
			render(target, memberRow.getNumOfAssignedRatings(), memberRow.getNumOfAssignments());
		}
	}
	
	private void render(StringOutput target, int numOfRatings, int numOfAssignments) {
		if(numOfRatings < 0) {
			numOfRatings = 0;
		}
		if(numOfAssignments < 0) {
			numOfAssignments = 0;
		}
		target.append(numOfRatings).append("\u00A0/\u00A0").append(numOfAssignments);
	}
}
