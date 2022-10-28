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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.logging.Tracing;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.grade.GradeSystemType;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityListCourseNodeTableModel extends DefaultFlexiTableDataModel<AssessedIdentityElementRow>
	implements SortableFlexiTableDataModel<AssessedIdentityElementRow> {
	
	private static final Logger log = Tracing.createLoggerFor(IdentityListCourseNodeTableModel.class);
	private static final IdentityCourseElementCols[] COLS = IdentityCourseElementCols.values();

	private final Locale locale;
	
	private Float minScore;
	private Float maxScore;
	private Float cutValue;
	private ConcurrentMap<Long, CertificateLight> certificateMap;
	private final GradeSystemType gradeSystemType;
	
	public IdentityListCourseNodeTableModel(FlexiTableColumnModel columnModel, RepositoryEntry courseEntry,
			CourseNode courseNode, Locale locale, GradeSystemType gradeSystemType) {
		super(columnModel);
		this.locale = locale;
		this.gradeSystemType = gradeSystemType;
	
		if (courseNode != null) {
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
			if(Mode.setByNode == assessmentConfig.getScoreMode()) {
				maxScore = assessmentConfig.getMaxScore();
				minScore = assessmentConfig.getMinScore();
				if (Mode.setByNode == assessmentConfig.getPassedMode()) {
					cutValue = assessmentConfig.getCutValue();
				}
			}
		}
	}

	public void setCertificateMap(ConcurrentMap<Long, CertificateLight> certificateMap) {
		this.certificateMap = certificateMap;
	}

	@Override
	public void sort(SortKey orderBy) {
		try {
			List<AssessedIdentityElementRow> views = new IdentityListCourseNodeTableSortDelegate(orderBy, this, locale, gradeSystemType)
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
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case attempts: return row.getAttempts();
				case userVisibility: return row.getUserVisibility();
				case score: return row;
				case min: return minScore;
				case max: return getMaxScore(row);
				case cut: return cutValue;
				case grade: return row;
				case status: return "";
				case passedOverriden: return row.getPassedOverriden();
				case passed: return row.getPassed();
				case numOfAssessmentDocs: {
					if(row.getNumOfAssessmentDocs() <= 0) {
						return null;
					}
					return row.getNumOfAssessmentDocs();
				}
				case assessmentStatus: return row.getAssessmentStatus();
				case currentCompletion: return row.getCurrentCompletion();
				case currentRunStart: return row.getCurrentRunStart();
				case certificate: return certificateMap.get(row.getIdentityKey());
				case recertification: {
					CertificateLight certificate = certificateMap.get(row.getIdentityKey());
					return certificate == null ? null : certificate.getNextRecertificationDate();
				}
				case initialLaunchDate: return row.getInitialCourseLaunchDate();
				case lastModified: return row.getLastModified();
				case lastUserModified: return row.getLastUserModified();
				case lastCoachModified: return row.getLastCoachModified();
				case externalGrader: return row.getGraderFullName();
				case tools: return row.getToolsLink();
				case details: return row.getDetails();
				default: return "ERROR";
			}
		}
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	private Float getMaxScore(AssessedIdentityElementRow row) {
		if(row == null || row.getMaxScore() == null) {
			return maxScore;
		}
		BigDecimal ms = row.getMaxScore();
		return Float.valueOf(ms.floatValue());
	}
	
	public enum IdentityCourseElementCols implements FlexiSortableColumnDef {
		attempts("table.header.attempts"),
		userVisibility("table.header.userVisibility", "o_icon o_icon-fw o_icon_results_hidden"),
		score("table.header.score"),
		min("table.header.min"),
		max("table.header.max"),
		grade("table.header.grade"),
		status("table.header.status"),
		passedOverriden("table.header.passed.overriden"),
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
		cut("table.header.cut"),
		externalGrader("table.header.external.grader"),
		currentRunStart("table.header.run.start");
		
		private final String i18nKey;
		private final String icon;

		private IdentityCourseElementCols(String i18nKey) {
			this(i18nKey, null);
		}
		
		private IdentityCourseElementCols(String i18nKey, String icon) {
			this.i18nKey = i18nKey;
			this.icon = icon;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public String iconHeader() {
			return icon;
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