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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.course.assessment.UserEfficiencyStatement;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserEfficiencyStatementTableDataModel implements TableDataModel<UserEfficiencyStatement> {
	
	private List<UserEfficiencyStatement> group;
	
	public UserEfficiencyStatementTableDataModel(List<UserEfficiencyStatement> group) {
		this.group = group;
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public int getRowCount() {
		return group == null ? 0 : group.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		UserEfficiencyStatement entry = group.get(row);
		switch(Columns.getValueAt(col)) {
			case repoName: {
				return entry.getShortTitle();
			}
			case score: {
				UserEfficiencyStatement s = entry;
				return s == null ? null : s.getScore();
			}
			case passed: {
				UserEfficiencyStatement s = entry;
				return s == null ? null : s.getPassed();
			}
			case progress: {
				if(entry == null || entry.getTotalNodes() == null) {
					ProgressValue val = new ProgressValue();
					val.setTotal(100);
					val.setGreen(0);
					return val;
				}
				
				ProgressValue val = new ProgressValue();
				val.setTotal(entry.getTotalNodes().intValue());
				val.setGreen(entry.getAttemptedNodes() == null ? 0 : entry.getAttemptedNodes().intValue());
				return val;
			}
			case lastModification: {
				return entry == null ? null : entry.getLastModified();
			}
			default : {
				return entry;
			}
		}
	}

	@Override
	public UserEfficiencyStatement getObject(int row) {
		return group.get(row);
	}

	@Override
	public void setObjects(List<UserEfficiencyStatement> objects) {
		group = objects;
	}

	@Override
	public UserEfficiencyStatementTableDataModel createCopyWithEmptyList() {
		return new UserEfficiencyStatementTableDataModel(new ArrayList<UserEfficiencyStatement>());
	}
	
	public static enum Columns {
		studentName,
		repoName,
		score,
		passed,
		progress,
		lastModification,
		;

		public static Columns getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return studentName;
		}
	}
}
