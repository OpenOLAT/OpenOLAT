/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.bulk;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.table.DefaultTableDataModel;

/**
 * 
 * Initial date: 18.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskDataModel extends DefaultTableDataModel<TaskData> implements FlexiTableDataModel<TaskData> {
	private FlexiTableColumnModel columnModel; 
	
	public TaskDataModel(List<TaskData> tasks, FlexiTableColumnModel columnModel) {
		super(tasks);
		this.columnModel = columnModel;
	}

	@Override
	public FlexiTableColumnModel getTableColumnModel() {
		return columnModel;
	}

	@Override
	public void setTableColumnModel(FlexiTableColumnModel tableColumnModel) {
		this.columnModel = tableColumnModel;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int row, int col) {
		TaskData data = getObject(row);
		switch(Cols.values()[col]) {
			case courseNode: return data.getCourseNode();
			case score: return new Boolean(data.isHasScore());
			case status: return new Boolean(data.isHasPassed());
			case comment: return new Boolean(data.isHasUserComment());
			case returnFile: return new Boolean(data.isHasReturnFiles());
			case numOfAssessedUsers: return data.getNumOfAssessedIds();
			case scheduledDate: return data.getTask().getScheduledDate();
			case taskStatus: return data.getTask().getStatus();
			case owner: return data.getOwnerFullName();
		}
		return null;
	}
	
	public enum Cols {
		courseNode,
		score,
		status,
		comment,
		returnFile,
		numOfAssessedUsers,
		scheduledDate,
		taskStatus,
		owner
	}
}
