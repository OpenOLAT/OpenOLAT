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

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.certificate.CertificateLight;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer.CompletionPassed;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.IdentityRepositoryEntryKey;
import org.olat.modules.coach.model.IdentityResourceKey;
import org.olat.modules.lecture.model.LectureBlockStatistics;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EfficiencyStatementEntryTableDataModel extends DefaultFlexiTableDataModel<EfficiencyStatementEntry> implements SortableFlexiTableDataModel<EfficiencyStatementEntry> {
	
	public static final Columns[] COLS = Columns.values();
	private ConcurrentMap<IdentityRepositoryEntryKey, Double> completionsMap;
	private ConcurrentMap<IdentityResourceKey, CertificateLight> certificateMap;
	private ConcurrentMap<IdentityRepositoryEntryKey, LectureBlockStatistics> lecturesStatisticsMap;
	
	public EfficiencyStatementEntryTableDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	public boolean contains(IdentityResourceKey key) {
		return certificateMap != null && certificateMap.containsKey(key);
	}
	
	public void putCertificate(CertificateLight certificate) {
		if(certificateMap != null && certificate != null) {
			IdentityResourceKey key = new IdentityResourceKey(certificate.getIdentityKey(), certificate.getOlatResourceKey());
			certificateMap.put(key, certificate);
		}
	}

	@Override
	public void sort(SortKey orderBy) {
		setObjects(new EfficiencyStatementEntrySortDelegate(orderBy, this, null).sort());
	}
	@Override
	public Object getValueAt(int row, int col) {
		EfficiencyStatementEntry entry = getObject(row);
		return getValueAt(entry, col);
	}
	
	@Override
	public Object getValueAt(EfficiencyStatementEntry entry, int col) {

		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case repoKey: return entry.getCourse().getKey(); 
				case repoName: return entry.getCourse().getDisplayname();
				case repoExternalId: return entry.getCourse().getExternalId();
				case repoExternalRef: return entry.getCourse().getExternalRef();
				case completion: return createCompletionPassed(entry);
				case score: return entry.getScore();
				case grade: return entry;
				case passed: return entry.getPassed();
				case certificate: return getCertificate(entry);
				case certificateValidity: {
					CertificateLight certificate = getCertificate(entry);
					return certificate == null ? null : certificate.getNextRecertificationDate();
				}
				case numberAssessments: {
					ProgressValue val = null;
					Integer totalNodes = entry.getTotalNodes();
					if (totalNodes != null && totalNodes.intValue() > 0) {
						val = new ProgressValue();
						val.setTotal(totalNodes.intValue());
						Integer attemptedNodes = entry.getAttemptedNodes();
						val.setGreen(attemptedNodes == null ? 0 : attemptedNodes.intValue());
					}
					return val;
				}
				case progress: {
					Integer totalNodes = entry.getTotalNodes();
					if(totalNodes == null) {
						ProgressValue val = new ProgressValue();
						val.setTotal(100);
						val.setGreen(0);
						return val;
					}
					
					ProgressValue val = new ProgressValue();
					val.setTotal(totalNodes.intValue());
					Integer attemptedNodes = entry.getAttemptedNodes();
					val.setGreen(attemptedNodes == null ? 0 : attemptedNodes.intValue());
					return val;
				}
				case lastModification: return entry.getLastModified();
				case lastUserModified: return entry.getLastUserModified();
				case lastCoachModified: return entry.getLastCoachModified();
				case plannedLectures: {
					LectureBlockStatistics statistics = getLectureBlockStatistics(entry);
					return statistics == null ? null : statistics.getTotalPersonalPlannedLectures();
				}
				case attendedLectures: {
					LectureBlockStatistics statistics = getLectureBlockStatistics(entry);
					return statistics == null ? null : statistics.getTotalAttendedLectures();
				}
				case unauthorizedAbsenceLectures:
				case absentLectures: {
					LectureBlockStatistics statistics = getLectureBlockStatistics(entry);
					return statistics == null ? null : statistics.getTotalAbsentLectures();
				}
				case authorizedAbsenceLectures: return getAuthorizedAbsenceLectures(entry);
				case dispensedLectures: return getTotalDispensationLectures(entry);
			}
		}
		
		int propPos = col - UserListController.USER_PROPS_OFFSET;
		return entry.getIdentityProp(propPos);
	}
	
	private Long getAuthorizedAbsenceLectures(EfficiencyStatementEntry entry) {
		LectureBlockStatistics statistics = getLectureBlockStatistics(entry);
		return statistics == null ? null : statistics.getTotalAuthorizedAbsentLectures();
	}
	
	private Long getTotalDispensationLectures(EfficiencyStatementEntry entry) {
		LectureBlockStatistics statistics = getLectureBlockStatistics(entry);
		return statistics == null ? null : statistics.getTotalDispensationLectures();
	}
	
	private CertificateLight getCertificate(EfficiencyStatementEntry entry) {
		if(certificateMap != null) {
			IdentityResourceKey key = new IdentityResourceKey(entry.getIdentityKey(), entry.getCourse().getOlatResource().getKey());
			return certificateMap.get(key);
		}
		return null;
	}

	public CompletionPassed createCompletionPassed(EfficiencyStatementEntry entry) {
		return new CompletionPassedImpl(getCompletion(entry), entry.getPassed());
	}
	
	private Double getCompletion(EfficiencyStatementEntry entry) {
		if(completionsMap != null) {
			IdentityRepositoryEntryKey key = new IdentityRepositoryEntryKey(entry);
			return completionsMap.get(key);
		}
		return null;
	}

	private LectureBlockStatistics getLectureBlockStatistics(EfficiencyStatementEntry entry) {
		if(lecturesStatisticsMap != null) {
			IdentityRepositoryEntryKey key = new IdentityRepositoryEntryKey(entry);
			return lecturesStatisticsMap.get(key);
		}
		return null;
	}

	public void setObjects(List<EfficiencyStatementEntry> objects, ConcurrentMap<IdentityResourceKey, CertificateLight> certificates) {
		setObjects(objects, certificates, null, null);
	}
	
	public void setObjects(List<EfficiencyStatementEntry> objects,
			ConcurrentMap<IdentityResourceKey, CertificateLight> certificates,
			ConcurrentMap<IdentityRepositoryEntryKey, Double> completionsMap,
			ConcurrentMap<IdentityRepositoryEntryKey, LectureBlockStatistics> lecturesStatisticsMap) {
		setObjects(objects);
		this.certificateMap = certificates;
		this.completionsMap = completionsMap;
		this.lecturesStatisticsMap = lecturesStatisticsMap;
	}
	
	public enum Columns implements FlexiSortableColumnDef {
		repoKey("table.header.course.key"),
		repoName("table.header.course.name"),
		repoExternalId("table.header.course.externalId"),
		repoExternalRef("table.header.course.externalRef"),
		completion("table.header.completion"),
		score("table.header.score"),
		grade("table.header.grade"),
		passed("table.header.passed"),
		certificate("table.header.certificate"),
		certificateValidity("table.header.certificate.validity"),
		numberAssessments("table.header.number.assessments"),
		progress("table.header.progress"),
		lastModification("table.header.lastScoreDate"),
		lastUserModified("table.header.lastUserModificationDate"),
		lastCoachModified("table.header.lastCoachModificationDate"),
		plannedLectures("table.header.planned.lectures"),
		attendedLectures("table.header.attended.lectures"),
		absentLectures("table.header.absent.lectures"),
		unauthorizedAbsenceLectures("table.header.unauthorized.absence"),
		authorizedAbsenceLectures("table.header.authorized.absence"),
		dispensedLectures("table.header.dispensation");
		
		private final String i18nKey;
		
		private Columns(String i18nKey) {
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

		public static Columns getValueAt(int ordinal) {
			if(ordinal >= 0 && ordinal < values().length) {
				return values()[ordinal];
			}
			return null;
		}
	}
	
	private static final class CompletionPassedImpl implements CompletionPassed {
		
		private final Double completion;
		private final Boolean passed;
		
		public CompletionPassedImpl(Double completion, Boolean passed) {
			this.completion = completion;
			this.passed = passed;
		}
		
		@Override
		public Double getCompletion() {
			return completion;
		}
		
		@Override
		public Boolean getPassed() {
			return passed;
		}
		
	}
	
}
