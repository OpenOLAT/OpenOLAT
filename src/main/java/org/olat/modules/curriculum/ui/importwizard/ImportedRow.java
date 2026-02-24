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
package org.olat.modules.curriculum.ui.importwizard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.AbstractExcelReader.ReaderLocalDate;
import org.olat.core.util.openxml.AbstractExcelReader.ReaderLocalTime;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.ui.CurriculumExportType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 20 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportedRow extends AbstractImportRow {
	
	private final CurriculumExportType type;
	private final String rawType;
	
	private final String identifier;
	private final String displayName;
	private String level;
	private String elementStatus;
	
	private String calendar;
	private String absences;
	private String progress;
	private String description;
	private String location;
	private String unit;
	
	private ReaderLocalDate startDate;
	private ReaderLocalTime startTime;
	private ReaderLocalDate endDate;
	private ReaderLocalTime endTime;
	
	private Curriculum curriculum;
	private String curriculumIdentifier;
	private ImportedRow curriculumRow;
	
	private CurriculumElement implementation;
	private String implementationIdentifier;
	private ImportedRow implementationRow;
	
	private Organisation organisation;
	private String organisationIdentifier;
	private String referenceExternalRef;

	private CurriculumElement curriculumElement;
	private CurriculumElementType curriculumElementType;
	private String curriculumElementTypeIdentifier;
	
	private LectureBlock lectureBlock;
	private RepositoryEntry course;
	private RepositoryEntry template;
	
	private List<ImportedRow> courseRows;
	private List<ImportedRow> templateRows;
	private List<ImportedRow> subCurriculumElementRows;
	
	private String subjects;
	private List<TaxonomyLevel> taxonomyLevels;
	
	private final LocalDateTime creationDate;
	private final LocalDateTime lastModified;

	public ImportedRow(int rowNum, String displayName, String identifier,
			String organisationIdentifier, String absences, String description,
			LocalDateTime creationDate, LocalDateTime lastModified) {
		super(rowNum);
		type = CurriculumExportType.CUR;
		this.rawType = null;
		this.displayName = displayName;
		this.identifier = identifier;
		this.curriculumIdentifier = identifier;
		this.organisationIdentifier = organisationIdentifier;
		this.description = description;
		this.absences = absences;
		this.creationDate = creationDate;
		this.lastModified = lastModified;
	}
	
	public ImportedRow(String type, int rowNum, String displayName, String identifier,
			String curriculumIdentifier, String implementationIdentifier, String level, String elementStatus, String curriculumElementTypeIdentifier,
			String referenceExternalRef, String unit, ReaderLocalDate startDate, ReaderLocalTime startTime, ReaderLocalDate endDate, ReaderLocalTime endTime,
			String location, String calendar, String absences, String progress, String subjects, LocalDateTime creationDate, LocalDateTime lastModified) {
		super(rowNum);
		this.type = CurriculumExportType.secureValueOf(type);
		this.rawType = type;
		this.displayName = displayName;
		this.identifier = identifier;
		this.curriculumIdentifier = curriculumIdentifier;
		this.implementationIdentifier = implementationIdentifier;
		this.referenceExternalRef = referenceExternalRef;
		this.elementStatus = elementStatus;
		this.curriculumElementTypeIdentifier = curriculumElementTypeIdentifier;
		this.calendar = calendar;
		this.absences = absences;
		this.progress = progress;
		this.unit = unit;
		this.level = level;
		this.startDate = startDate;
		this.startTime = startTime;
		this.endDate = endDate;
		this.endTime = endTime;
		this.subjects = subjects;
		this.location = location;
		this.creationDate = creationDate;
		this.lastModified = lastModified;
	}
	
	public CurriculumExportType type() {
		return type;
	}
	
	public String getRawType() {
		return rawType;
	}
	
	public boolean isNew() {
		return (type == CurriculumExportType.CUR && curriculum == null)
				|| (type == CurriculumExportType.IMPL && curriculumElement == null)
				|| (type == CurriculumExportType.ELEM && curriculumElement == null)
				|| (type == CurriculumExportType.EVENT && lectureBlock == null);
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ReaderLocalDate getStartDate() {
		return startDate;
	}

	public ReaderLocalTime getStartTime() {
		return startTime;
	}

	public ReaderLocalDate getEndDate() {
		return endDate;
	}

	public ReaderLocalTime getEndTime() {
		return endTime;
	}

	public String getLocation() {
		return location;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public String getLevel() {
		return level;
	}
	
	public String getParentLevel() {
		if(StringHelper.containsNonWhitespace(level) && level.indexOf('.') > 0) {
			int lastIndex = level.lastIndexOf('.');
			return level.substring(0, lastIndex);
		}
		return null;
	}
	
	public String getElementStatus() {
		return elementStatus;
	}
	

	public String getCurriculumIdentifier() {
		return curriculumIdentifier;
	}

	public Curriculum getCurriculum() {
		return curriculum;
	}

	public void setCurriculum(Curriculum curriculum) {
		this.curriculum = curriculum;
	}

	public ImportedRow getCurriculumRow() {
		return curriculumRow;
	}

	public void setCurriculumRow(ImportedRow curriculumRow) {
		this.curriculumRow = curriculumRow;
		if(curriculumRow != null && curriculumRow.getCurriculum() != null && curriculum == null) {
			curriculum = curriculumRow.getCurriculum();
		}
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}
	
	public String getOrganisationIdentifier() {
		return organisationIdentifier;
	}

	public void setOrganisationIdentifier(String organisationIdentifier) {
		this.organisationIdentifier = organisationIdentifier;
	}

	public CurriculumElement getImplementation() {
		return implementation;
	}

	public void setImplementation(CurriculumElement implementation) {
		this.implementation = implementation;
	}

	public String getImplementationIdentifier() {
		return implementationIdentifier;
	}

	public ImportedRow getImplementationRow() {
		return implementationRow;
	}

	public void setImplementationRow(ImportedRow implementationRow) {
		this.implementationRow = implementationRow;
		if(implementation == null && implementationRow != null) {
			implementation = implementationRow.getCurriculumElement();
		}
	}

	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	public void setLectureBlock(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}

	public CurriculumElementType getCurriculumElementType() {
		return curriculumElementType;
	}

	public void setCurriculumElementType(CurriculumElementType curriculumElementType) {
		this.curriculumElementType = curriculumElementType;
	}

	public String getCurriculumElementTypeIdentifier() {
		return curriculumElementTypeIdentifier;
	}

	public void setCurriculumElementTypeIdentifier(String curriculumElementTypeIdentifier) {
		this.curriculumElementTypeIdentifier = curriculumElementTypeIdentifier;
	}

	public void setCurriculumElementParentRow(ImportedRow curriculumElementParentRow) {
		super.setCurriculumElementParentRow(curriculumElementParentRow);
		
		if(curriculumElementParentRow != null) {
			if(type() == CurriculumExportType.COURSE) {
				curriculumElementParentRow.addCourseRow(this);
			} else if(type() == CurriculumExportType.TMPL) {
				curriculumElementParentRow.addTemplateRow(this);
			} else if(type() == CurriculumExportType.IMPL || type() == CurriculumExportType.ELEM) {
				curriculumElementParentRow.addSubElementRow(this);
			}
		}
	}
	
	public int getNumResources(CurriculumExportType ofType) {
		if(ofType == CurriculumExportType.COURSE) {
			return courseRows == null ? 0 : courseRows.size();
		}
		if(ofType == CurriculumExportType.TMPL) {
			return templateRows == null ? 0 : templateRows.size();
		}
		return 0;
	}

	public int getNumOfSubCurriculumElements() {
		return subCurriculumElementRows == null ? 0 : subCurriculumElementRows.size();
	}
	
	private void addCourseRow(ImportedRow row) {
		if(courseRows == null) {
			courseRows = new ArrayList<>(3);
		}
		courseRows.add(row);
	}
	
	private void addTemplateRow(ImportedRow row) {
		if(templateRows == null) {
			templateRows = new ArrayList<>(3);
		}
		templateRows.add(row);
	}
	
	private void addSubElementRow(ImportedRow row) {
		if(subCurriculumElementRows == null) {
			subCurriculumElementRows = new ArrayList<>(3);
		}
		subCurriculumElementRows.add(row);
	}

	public RepositoryEntry getCourse() {
		return course;
	}

	public void setCourse(RepositoryEntry course) {
		this.course = course;
	}

	public RepositoryEntry getTemplate() {
		return template;
	}

	public void setTemplate(RepositoryEntry template) {
		this.template = template;
	}
	
	public String getReferenceExternalRef() {
		return referenceExternalRef;
	}

	public String getCalendar() {
		return calendar;
	}

	public void setCalendar(String calendar) {
		this.calendar = calendar;
	}

	public String getAbsences() {
		return absences;
	}

	public void setAbsences(String absences) {
		this.absences = absences;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}
	
	public String getSubjects() {
		return subjects;
	}
	
	public List<String> getSubjectsList() {
		List<String> list = new ArrayList<>(3);
		if(StringHelper.containsNonWhitespace(subjects)) {
			String[] subjectsArr = subjects.split(";");
			for(String subject:subjectsArr) {
				if(StringHelper.containsNonWhitespace(subject)) {
					list.add(subject);
				}
			}
		}
		return list;
	}
	
	public List<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels;
	}
	
	public Set<Long> getTaxonomyLevelsKeys() {
		if(taxonomyLevels == null || taxonomyLevels.isEmpty()) {
			return Set.of();
		}
		return taxonomyLevels.stream()
				.map(TaxonomyLevel::getKey)
				.collect(Collectors.toSet());
	}
	
	public TaxonomyLevel getTaxonomyLevel(String subject) {
		if(taxonomyLevels == null) return null;
		
		for(TaxonomyLevel taxonomyLevel:taxonomyLevels) {
			String path = taxonomyLevel.getMaterializedPathIdentifiers();
			if(subject.equals(path)) {
				return taxonomyLevel;
			}
		}
		return null;
	}

	public void addTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		if(taxonomyLevels == null) {
			taxonomyLevels = new ArrayList<>();
		}
		taxonomyLevels.add(taxonomyLevel);
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type()).append("[identifier=").append(identifier == null ? "NULL" : identifier)
		  .append("]");
		return sb.toString();
	}

}
