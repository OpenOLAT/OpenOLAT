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
package org.olat.modules.coach.ui.curriculum.certificate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.course.certificate.CertificateLight;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.IdentityRepositoryEntryKey;
import org.olat.modules.coach.model.IdentityResourceKey;
import org.olat.modules.coach.ui.ProgressValue;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.lecture.model.LectureBlockStatistics;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementWithViewsDataModel extends DefaultFlexiTreeTableDataModel<CurriculumTreeWithViewsRow> implements FlexiBusinessPathModel {

	private static final ElementViewCols[] COLS = ElementViewCols.values();
	
	private ConcurrentMap<IdentityResourceKey, CertificateLight> certificateMap;
	private ConcurrentMap<IdentityRepositoryEntryKey, LectureBlockStatistics> lecturesStatisticsMap;

	public CurriculumElementWithViewsDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && filters.get(0) != null) {
			FlexiTableFilter filter = filters.get(0);
			if(filter == null || filter.isShowAll()) {
				setUnfilteredObjects();
			} else {
				List<CurriculumTreeWithViewsRow> filteredRows = new ArrayList<>(backupRows.size());
				// curriculum element inactive -> all repo are inactives
				// parent inactive, child is active -> parent is forced active
				for(CurriculumTreeWithViewsRow row:backupRows) {
					if (filters.get(0).getFilter().equals(FilterKeys.activ.name())) {
						boolean accept = active(row);
						if(accept) {
							filteredRows.add(row);
						}
					} else if (filters.get(0).getFilter().equals(FilterKeys.withStatementOnly.name())) {
						if (row.hasStatement()) {
							filteredRows.add(row);
						}
					}

				}
				setFilteredObjects(filteredRows);
			}
		} else {
			setObjects(backupRows);
		}
	}
	
	private boolean active(CurriculumTreeWithViewsRow row) {
		boolean active = true;
		if(row.isCurriculumElementOnly() || row.isCurriculumElementWithEntry()) {
			active = row.getCurriculumElementStatus() == CurriculumElementStatus.active;
		}
		if(active) {
			for(CurriculumTreeWithViewsRow parent = row.getParent(); parent != null; parent=parent.getParent()) {
				if(parent.isCurriculumElementOnly() || parent.isCurriculumElementWithEntry()) {
					active &= row.getCurriculumElementStatus() == CurriculumElementStatus.active;
				}
			}
		}
		return active;
	}
	
	@Override
	public boolean hasChildren(int row) {
		CurriculumTreeWithViewsRow element = getObject(row);
		return element.hasChildren();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumTreeWithViewsRow curriculum = getObject(row);
		switch(COLS[col]) {
			case key: return curriculum.getId();
			case displayName:
				return curriculum.getDisplayName();
			case hasStatement:
				return curriculum.hasStatement();
			case identifier: {
				String identifier;
				if(curriculum.isRepositoryEntryOnly()) {
					identifier = curriculum.getRepositoryEntryExternalRef();
				} else {
					identifier = curriculum.getCurriculumElementIdentifier();
				}
				return identifier;
			}
			case mark: return curriculum.getMarkLink();
			case select: return curriculum.getSelectLink();
			case details: return curriculum.getDetailsLink();
			case start: return curriculum.getStartLink();
			case calendars: return curriculum.getCalendarsLink();
			case completion: return curriculum.getCompletionItem();
			case score: return curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry().getScore() : null;
			case passed: return curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry().getPassed() : null;
			case certificate: return getCertificate(curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry() : null);
			case recertification: {
				CertificateLight certificate = getCertificate(curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry() : null);
				return certificate == null ? null : certificate.getNextRecertificationDate();
			}
			case numberAssessments: {
				ProgressValue val = null;
				Integer totalNodes = curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry().getTotalNodes() : null;
				if (totalNodes != null && totalNodes.intValue() > 0) {
					val = new ProgressValue();
					val.setTotal(totalNodes.intValue());
					Integer attemptedNodes = curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry().getAttemptedNodes() : null;
					val.setGreen(attemptedNodes == null ? 0 : attemptedNodes.intValue());
				}
				return val;
			}
			case progress: {
				Integer totalNodes = curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry().getTotalNodes() : null;
				if(totalNodes == null) {
					ProgressValue val = new ProgressValue();
					val.setTotal(100);
					val.setGreen(0);
					return val;
				}

				ProgressValue val = new ProgressValue();
				val.setTotal(totalNodes.intValue());
				Integer attemptedNodes = curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry().getAttemptedNodes() : null;
				val.setGreen(attemptedNodes == null ? 0 : attemptedNodes.intValue());
				return val;
			}
			case lastModification: return curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry().getLastModified() : null;
			case lastUserModified: return curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry().getLastUserModified() : null;
			case lastCoachModified: return curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry().getLastCoachModified() : null;
			case plannedLectures: {
				LectureBlockStatistics statistics = getLectureBlockStatistics(curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry() : null);
				return statistics == null ? null : statistics.getTotalPersonalPlannedLectures();
			}
			case attendedLectures: {
				LectureBlockStatistics statistics = getLectureBlockStatistics(curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry() : null);
				return statistics == null ? null : statistics.getTotalAttendedLectures();
			}
			case unauthorizedAbsenceLectures:
			case absentLectures: {
				LectureBlockStatistics statistics = getLectureBlockStatistics(curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry() : null);
				return statistics == null ? null : statistics.getTotalAbsentLectures();
			}
			case authorizedAbsenceLectures: {
				LectureBlockStatistics statistics = getLectureBlockStatistics(curriculum.getEfficiencyStatementEntry() != null ? curriculum.getEfficiencyStatementEntry() : null);
				return statistics == null ? null : statistics.getTotalAuthorizedAbsentLectures();
			}
			case efficiencyStatement:
				return curriculum.getEfficiencyStatementEntry() != null;
			default: return "ERROR";
		}
	}

	private CertificateLight getCertificate(EfficiencyStatementEntry entry) {
		if(certificateMap != null) {
			IdentityResourceKey key = new IdentityResourceKey(entry.getIdentityKey(), entry.getCourse().getOlatResource().getKey());
			return certificateMap.get(key);
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

	public void setObjects(List<CurriculumTreeWithViewsRow> objects, ConcurrentMap<IdentityResourceKey, CertificateLight> certificates) {
		setObjects(objects, certificates, null);
	}

	public void setObjects(List<CurriculumTreeWithViewsRow> objects,
						   ConcurrentMap<IdentityResourceKey, CertificateLight> certificates,
						   ConcurrentMap<IdentityRepositoryEntryKey, LectureBlockStatistics> lecturesStatisticsMap) {
		setObjects(objects);
		this.certificateMap = certificates;
		this.lecturesStatisticsMap = lecturesStatisticsMap;
	}

	@Override
	public CurriculumElementWithViewsDataModel createCopyWithEmptyList() {
		return new CurriculumElementWithViewsDataModel(getTableColumnModel());
	}

	public enum ElementViewCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.curriculum.element.displayName"),
		hasStatement("table.header.curriculum.element.has.statement"),
		identifier("table.header.curriculum.element.identifier"),
		mark("table.header.mark"),
		select("table.header.displayName"),
		completion("table.header.completion"),
		details("table.header.details"),
		start("table.header.start"),
		calendars("table.header.calendars"),
		score("table.header.score"),
		passed("table.header.passed"),
		certificate("table.header.certificate"),
		recertification("table.header.recertification"),
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
		efficiencyStatement("table.header.show"),
		deleteEfficiencyStatement("table.action.delete"),
		artefact("table.header.artefact");
		
		private final String i18nHeaderKey;
		
		private ElementViewCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}

	@Override
	public String getUrl(Component source, Object object, String action) {
		if("select".equals(action) && object instanceof CurriculumTreeWithViewsRow) {
			CurriculumTreeWithViewsRow row = (CurriculumTreeWithViewsRow)object;
			if(row.getStartUrl() != null) {
				return row.getStartUrl();
			}
			if(row.getDetailsUrl() != null) {
				return row.getDetailsUrl();
			}
		}
		return null;
	}

	public enum FilterKeys {
		activ("filter.activ"),
		withStatementOnly("filter.with.statement.only"),
		showAll("show.all");

		private final String i18nHeaderKey;

		private FilterKeys(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
