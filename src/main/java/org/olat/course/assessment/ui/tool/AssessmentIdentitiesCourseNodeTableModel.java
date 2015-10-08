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
package org.olat.course.assessment.ui.tool;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.modules.assessment.AssessmentEntry;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentIdentitiesCourseNodeTableModel extends DefaultFlexiTableDataModel<AssessedIdentityCourseElementRow>
	implements SortableFlexiTableDataModel<AssessedIdentityCourseElementRow> {

	private final AssessableCourseNode courseNode;
	
	public AssessmentIdentitiesCourseNodeTableModel(FlexiTableColumnModel columnModel, AssessableCourseNode courseNode) {
		super(columnModel);
		this.courseNode = courseNode;
	}
	
	@Override
	public void sort(SortKey sortKey) {
		//
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessedIdentityCourseElementRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(AssessedIdentityCourseElementRow row, int col) {
		if(col >= 0 && col < IdentityCourseElementCols.values().length) {
			AssessmentEntry aEntry = row.getAssessmentEntry();
			switch(IdentityCourseElementCols.values()[col]) {
				case username: return row.getIdentityName();
				case attempts: return aEntry == null ? null : aEntry.getAttempts();
				case score: return aEntry == null ? null : aEntry.getScore();
				case min: {
					if(!(courseNode instanceof STCourseNode) && courseNode.hasScoreConfigured()) {
						return courseNode.getMinScoreConfiguration();
					}
					return "";
				}
				case max: {
					if(!(courseNode instanceof STCourseNode) && courseNode.hasScoreConfigured()) {
						return courseNode.getMaxScoreConfiguration();
					}
					return "";
				}
				case status: return "";
				case passed: return aEntry == null ? null : aEntry.getPassed();
				case initialLaunchDate: return aEntry == null ? null : aEntry.getCreationDate();
				case lastScoreUpdate: return aEntry == null ? null : aEntry.getLastModified();
			}
		}
		int propPos = col - AssessmentIdentitiesCourseNodeController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	@Override
	public DefaultFlexiTableDataModel<AssessedIdentityCourseElementRow> createCopyWithEmptyList() {
		return new AssessmentIdentitiesCourseNodeTableModel(getTableColumnModel(), courseNode);
	}
	
	public enum IdentityCourseElementCols implements FlexiColumnDef {
		username("table.header.name"),
		attempts("table.header.attempts"),
		score("table.header.score"),
		min("table.header.min"),
		max("table.header.max"),
		status("table.header.status"),
		passed("table.header.passed"),
		initialLaunchDate("table.header.initialLaunchDate"),
		lastScoreUpdate("table.header.lastScoreDate");
		
		private final String i18nKey;
		
		private IdentityCourseElementCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}