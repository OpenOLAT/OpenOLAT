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
package org.olat.modules.coach.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.certificate.CertificateLight;
import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 14 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CourseIdentityRow {
	
	private boolean marked;
	private final Date recentLaunch;
	private CertificateLight certificate;
	private final RepositoryEntry courseEnty;
	private final EfficiencyStatementEntry statementEntry;
	private final LectureBlockStatistics lectureBlockStatistics;
	private final RepositoryEntryLifecycle repositoryEntryLifecycle;
	private final AssessmentEntryScoring assessmentEntryScoring;
	
	private final ProgressValue numberAssessment;

	private FormLink markLink;
	
	public CourseIdentityRow(RepositoryEntry courseEntry, EfficiencyStatementEntry statementEntry, CertificateLight certificate,
			LectureBlockStatistics lectureBlockStatistics, AssessmentEntryScoring assessmentEntryScoring,
			Date recentLaunch, boolean marked) {
		this.marked = marked;
		this.courseEnty = courseEntry;
		this.certificate = certificate;
		this.recentLaunch = recentLaunch;
		this.statementEntry = statementEntry;
		this.lectureBlockStatistics = lectureBlockStatistics;
		this.repositoryEntryLifecycle = courseEntry.getLifecycle();
		this.assessmentEntryScoring = assessmentEntryScoring;
		
		Integer totalNodes = getStatementEntry().getTotalNodes();
		if (totalNodes != null && totalNodes.intValue() > 0) {
			numberAssessment = new ProgressValue();
			numberAssessment.setTotal(totalNodes.intValue());
			Integer attemptedNodes = getStatementEntry().getAttemptedNodes();
			numberAssessment.setGreen(attemptedNodes == null ? 0 : attemptedNodes.intValue());
		} else {
			numberAssessment = null;
		}
	}
	
	public RepositoryEntry getCourseEntry2() {
		return courseEnty;
	}
	
	public Long getRepositoryEntryKey() {
		return courseEnty.getKey();
	}
	
	public String getRepositoryEntryDisplayname() {
		return courseEnty.getDisplayname();
	}
	
	public String getRepositoryEntryExternalRef() {
		return courseEnty.getExternalRef();
	}
	
	public String getRepositoryEntryExternalId() {
		return courseEnty.getExternalId();
	}
	
	public String getRepositoryEntryTechnicalType() {
		return courseEnty.getTechnicalType();
	}
	
	public RepositoryEntryStatusEnum getRepositoryEntryStatus() {
		return courseEnty.getEntryStatus();
	}
	
	public Long getRepositoryEntryResourceKey() {
		return courseEnty.getOlatResource().getKey();
	}
	
	public String getLifecycleSoftKey() {
		return repositoryEntryLifecycle == null ? null : repositoryEntryLifecycle.getSoftKey();
	}
	
	public String getLifecycleLabel() {
		return repositoryEntryLifecycle == null ? null : repositoryEntryLifecycle.getLabel();
	}

	public Date getLifecycleValidFrom() {
		return repositoryEntryLifecycle == null ? null : repositoryEntryLifecycle.getValidFrom();
	}

	public Date getLifecycleValidTo() {
		return repositoryEntryLifecycle == null ? null : repositoryEntryLifecycle.getValidTo();
	}
	
	public Date getLastVisit() {
		return recentLaunch;
	}
	
	public CertificateLight getCertificate() {
		return certificate;
	}
	
	public void setCertificate(CertificateLight certificate) {
		this.certificate = certificate;
	}
	
	public Date getCertificategNextRecertificationDate() {
		return certificate == null ? null : certificate.getNextRecertificationDate();
	}

	public EfficiencyStatementEntry getStatementEntry() {
		return statementEntry;
	}
	
	public Double getAssessmentEntryCompletion() {
		return assessmentEntryScoring == null ? null : assessmentEntryScoring.getCompletion();
	}
	
	public Boolean  getAssessmentEntryPassed() {
		return assessmentEntryScoring == null ? null : assessmentEntryScoring.getPassed();
	}
	
	public ProgressValue getNumberAssessment() {
		return numberAssessment;
	}

	public LectureBlockStatistics getLectureBlockStatistics() {
		return lectureBlockStatistics;
	}
	
	public Long getTotalPersonalPlannedLectures() {
		return lectureBlockStatistics == null ? null : lectureBlockStatistics.getTotalPersonalPlannedLectures();
	}
	
	public Long getTotalAttendedLectures() {
		return lectureBlockStatistics == null ? null : lectureBlockStatistics.getTotalAttendedLectures();
	}
	
	public Long getTotalAbsentLectures() {
		return lectureBlockStatistics == null ? null : lectureBlockStatistics.getTotalAbsentLectures();
	}
	
	public Long getTotalAuthorizedAbsentLectures() {
		return lectureBlockStatistics == null ? null : lectureBlockStatistics.getTotalAuthorizedAbsentLectures();
	}
	
	public Long getTotalDispensationLectures() {
		return lectureBlockStatistics == null ? null : lectureBlockStatistics.getTotalDispensationLectures();
	}
	
	public Double getLecturesRequiredRate() {
		return lectureBlockStatistics == null ? null : lectureBlockStatistics.getRequiredRate();
	}
	
	public Double getLecturesAttendanceRate() {
		return lectureBlockStatistics == null ? null : lectureBlockStatistics.getAttendanceRate();
	}
	
	public long getLecturesTotalEffectiveLectures() {
		return lectureBlockStatistics == null ? 0l : lectureBlockStatistics.getTotalEffectiveLectures();
	}
	
	public boolean isLecturesCalculateRate() {
		return lectureBlockStatistics == null ? false : lectureBlockStatistics.isCalculateRate();
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	
	public String getMarkLinkName() {
		return markLink.getComponent().getComponentName();
	}

	public FormLink getMarkLink() {
		return markLink;
	}

	public void setMarkLink(FormLink markLink) {
		this.markLink = markLink;
	}
}
