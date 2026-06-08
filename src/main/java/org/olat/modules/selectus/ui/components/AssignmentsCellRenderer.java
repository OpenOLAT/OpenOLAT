/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
