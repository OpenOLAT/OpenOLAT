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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
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
			return switch (COLS[col]) {
				case attempts -> row.getAttempts();
				case userVisibility -> row.getUserVisibility();
				case score, weightedScore, grade -> row;
				case min -> minScore;
				case max -> getMaxScore(row);
				case cut -> cutValue;
				case status -> "";
				case passedOverriden -> row.getPassedOverriden();
				case passed -> row.getPassed();
				case numOfAssessmentDocs -> {
					if (row.getNumOfAssessmentDocs() <= 0) {
						yield null;
					}
					yield row.getNumOfAssessmentDocs();
				}
				case assessmentStatus -> row.getAssessmentStatus();
				case currentCompletion -> row.getCurrentCompletion();
				case currentRunStart -> row.getCurrentRunStart();
				case certificate -> certificateMap.get(row.getIdentityKey());
				case certificateValidity -> {
					CertificateLight certificate = certificateMap.get(row.getIdentityKey());
					yield certificate == null ? null : certificate.getNextRecertificationDate();
				}
				case initialLaunchDate -> row.getInitialCourseLaunchDate();
				case lastModified -> row.getLastModified();
				case lastUserModified -> row.getLastUserModified();
				case lastCoachModified -> row.getLastCoachModified();
				case externalGrader -> row.getGraderFullName();
				case coachAssignment -> row.getCoachFullName();
				case tools -> row.getToolsLink();
				case details -> row.getDetails();
				case numOfAuthorisedUsers -> row.getNumOfAuthorisedUsers();
				case numOfInProgressSections -> row.getNumOfSectionsInProgress();
				case numOfPublishedEntries -> row.getNumOfEntriesPublished();
				case numOfInRevisionEntries -> row.getNumOfEntriesInRevision();
				case numOfNewEntries -> row.getNumOfEntriesNew();
				case numOfInProgressEntries -> row.getNumOfEntriesInProgress();
				case openBinder -> row.getOpenBinderLink();
				default -> "ERROR";
			};
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
		weightedScore("table.header.weighted.score"),
		min("table.header.min"),
		max("table.header.max"),
		grade("table.header.grade"),
		status("table.header.status"),
		passedOverriden("table.header.passed.overriden"),
		passed("table.header.passed"),
		assessmentStatus("table.header.assessmentStatus"),
		certificate("table.header.certificate"),
		certificateValidity("table.header.certificate.validity"),
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
		currentRunStart("table.header.run.start"),
		coachAssignment("table.header.coach.assignment"),
		numOfAuthorisedUsers("table.header.num.authorised.users"),
		numOfInProgressSections("table.header.num.inprogress.sections"),
		numOfNewEntries("table.header.num.new.entries"),
		numOfInProgressEntries("table.header.num.inprogress.entries"),
		numOfPublishedEntries("table.header.num.published.entries"),
		numOfInRevisionEntries("table.header.num.inrevision.entries"),
		openBinder("table.header.action.open.binder");
		
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