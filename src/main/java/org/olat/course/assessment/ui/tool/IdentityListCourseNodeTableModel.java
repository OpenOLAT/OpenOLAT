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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityListCourseNodeTableModel extends DefaultFlexiTableDataModel<AssessedIdentityCourseElementRow>
	implements SortableFlexiTableDataModel<AssessedIdentityCourseElementRow>, FilterableFlexiTableModel {

	private final AssessableCourseNode courseNode;
	private List<AssessedIdentityCourseElementRow> backups;
	private ConcurrentMap<Long, CertificateLight> certificateMap;
	
	public IdentityListCourseNodeTableModel(FlexiTableColumnModel columnModel, AssessableCourseNode courseNode) {
		super(columnModel);
		this.courseNode = courseNode;
	}
	
	public void setCertificateMap(ConcurrentMap<Long, CertificateLight> certificateMap) {
		this.certificateMap = certificateMap;
	}

	@Override
	public void filter(String key) {
		if(StringHelper.containsNonWhitespace(key)) {
			List<AssessedIdentityCourseElementRow> filteredRows = new ArrayList<>();
			if("passed".equals(key)) {
				for(AssessedIdentityCourseElementRow row:backups) {
					if(row.getPassed() != null && row.getPassed().booleanValue()) {
						filteredRows.add(row);
					}
				}
			} else if("failed".equals(key)) {
				for(AssessedIdentityCourseElementRow row:backups) {
					if(row.getPassed() != null && !row.getPassed().booleanValue()) {
						filteredRows.add(row);
					}
				}
			} else if(AssessmentEntryStatus.isValueOf(key)) {
				for(AssessedIdentityCourseElementRow row:backups) {
					if(row.getAssessmentStatus() != null && key.equals(row.getAssessmentStatus().name())) {
						filteredRows.add(row);
					}
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public void setObjects(List<AssessedIdentityCourseElementRow> objects) {
		backups = objects;
		super.setObjects(objects);
	}

	@Override
	public void sort(SortKey orderBy) {
		SortableFlexiTableModelDelegate<AssessedIdentityCourseElementRow> sorter
				= new SortableFlexiTableModelDelegate<>(orderBy, this, null);
		List<AssessedIdentityCourseElementRow> views = sorter.sort();
		super.setObjects(views);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessedIdentityCourseElementRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(AssessedIdentityCourseElementRow row, int col) {
		if(col >= 0 && col < IdentityCourseElementCols.values().length) {
			switch(IdentityCourseElementCols.values()[col]) {
				case username: return row.getIdentityName();
				case attempts: return row.getAttempts();
				case score: return row.getScore();
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
				case passed: return row.getPassed();
				case assessmentStatus: return row.getAssessmentStatus();
				case certificate: return certificateMap.get(row.getIdentityKey());
				case initialLaunchDate: return row.getCreationDate();
				case lastScoreUpdate: return row.getLastModified();
			}
		}
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	@Override
	public DefaultFlexiTableDataModel<AssessedIdentityCourseElementRow> createCopyWithEmptyList() {
		return new IdentityListCourseNodeTableModel(getTableColumnModel(), courseNode);
	}
	
	public enum IdentityCourseElementCols implements FlexiSortableColumnDef {
		username("table.header.name"),
		attempts("table.header.attempts"),
		score("table.header.score"),
		min("table.header.min"),
		max("table.header.max"),
		status("table.header.status"),
		passed("table.header.passed"),
		assessmentStatus("table.header.assessmentStatus"),
		certificate("table.header.certificate"),
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