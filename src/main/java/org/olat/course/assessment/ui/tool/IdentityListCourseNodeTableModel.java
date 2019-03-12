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
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityListCourseNodeTableModel extends DefaultFlexiTableDataModel<AssessedIdentityElementRow>
	implements SortableFlexiTableDataModel<AssessedIdentityElementRow>, FilterableFlexiTableModel {
	
	private static final OLog log = Tracing.createLoggerFor(IdentityListCourseNodeTableModel.class);

	private final Locale locale;
	
	private Float minScore;
	private Float maxScore;
	private Float cutValue;
	private final AssessableCourseNode courseNode;
	private List<AssessedIdentityElementRow> backups;
	private ConcurrentMap<Long, CertificateLight> certificateMap;
	
	public IdentityListCourseNodeTableModel(FlexiTableColumnModel columnModel, AssessableCourseNode courseNode, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.courseNode = courseNode;
		
		if(courseNode != null && !(courseNode instanceof STCourseNode) && courseNode.hasScoreConfigured()) {
			maxScore = courseNode.getMaxScoreConfiguration();
			minScore = courseNode.getMinScoreConfiguration();
			if (courseNode.hasPassedConfigured()) {
				cutValue = courseNode.getCutValueConfiguration();
			}
		}
	}
	
	public void setCertificateMap(ConcurrentMap<Long, CertificateLight> certificateMap) {
		this.certificateMap = certificateMap;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if(StringHelper.containsNonWhitespace(key)) {
			List<AssessedIdentityElementRow> filteredRows = new ArrayList<>();
			if("passed".equals(key)) {
				for(AssessedIdentityElementRow row:backups) {
					if(row.getPassed() != null && row.getPassed().booleanValue()) {
						filteredRows.add(row);
					}
				}
			} else if("failed".equals(key)) {
				for(AssessedIdentityElementRow row:backups) {
					if(row.getPassed() != null && !row.getPassed().booleanValue()) {
						filteredRows.add(row);
					}
				}
			} else if(AssessmentEntryStatus.isValueOf(key)) {
				for(AssessedIdentityElementRow row:backups) {
					if(row.getAssessmentStatus() != null && key.equals(row.getAssessmentStatus().name())) {
						filteredRows.add(row);
					}
				}
			} else {
				filteredRows.addAll(backups);
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public void setObjects(List<AssessedIdentityElementRow> objects) {
		backups = objects;
		super.setObjects(objects);
	}

	@Override
	public void sort(SortKey orderBy) {
		try {
			List<AssessedIdentityElementRow> views = new IdentityListCourseNodeTableSortDelegate(orderBy, this, locale)
					.sort();
			super.setObjects(views);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessedIdentityElementRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(AssessedIdentityElementRow row, int col) {
		if(col >= 0 && col < IdentityCourseElementCols.values().length) {
			switch(IdentityCourseElementCols.values()[col]) {
				case username: return row.getIdentityName();
				case attempts: return row.getAttempts();
				case userVisibility: return row.getUserVisibility();
				case score: return row.getScore();
				case min: return minScore;
				case max: return maxScore;
				case cut: return cutValue;
				case status: return "";
				case passed: return row.getPassed();
				case numOfAssessmentDocs: {
					if(row.getNumOfAssessmentDocs() <= 0) {
						return null;
					}
					return row.getNumOfAssessmentDocs();
				}
				case assessmentStatus: return row.getAssessmentStatus();
				case currentCompletion: return row.getCurrentCompletion();
				case certificate: return certificateMap.get(row.getIdentityKey());
				case recertification: {
					CertificateLight certificate = certificateMap.get(row.getIdentityKey());
					return certificate == null ? null : certificate.getNextRecertificationDate();
				}
				case initialLaunchDate: return row.getInitialCourseLaunchDate();
				case lastModified: return row.getLastModified();
				case lastUserModified: return row.getLastUserModified();
				case lastCoachModified: return row.getLastCoachModified();
				case tools: return row.getToolsLink();
				case details: return row.getDetails();
			}
		}
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	@Override
	public DefaultFlexiTableDataModel<AssessedIdentityElementRow> createCopyWithEmptyList() {
		return new IdentityListCourseNodeTableModel(getTableColumnModel(), courseNode, locale);
	}
	
	public enum IdentityCourseElementCols implements FlexiSortableColumnDef {
		username("table.header.name"),
		attempts("table.header.attempts"),
		userVisibility("table.header.userVisibility"),
		score("table.header.score"),
		min("table.header.min"),
		max("table.header.max"),
		status("table.header.status"),
		passed("table.header.passed"),
		assessmentStatus("table.header.assessmentStatus"),
		certificate("table.header.certificate"),
		recertification("table.header.recertification"),
		initialLaunchDate("table.header.initialLaunchDate"),
		lastModified("table.header.lastScoreDate"),
		lastUserModified("table.header.lastUserModificationDate"),
		lastCoachModified("table.header.lastCoachModificationDate"),
		numOfAssessmentDocs("table.header.num.assessmentDocs"),
		currentCompletion("table.header.completion"),
		tools("table.header.tools"),
		details("table.header.details"),
		cut("table.header.cut");
		
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