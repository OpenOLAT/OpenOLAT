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

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.AbstractExcelReader;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.ui.CurriculumExportType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsFinishStepCallback implements StepRunnerCallback {
	
	private static final Logger log = Tracing.createLoggerFor(ImportCurriculumsFinishStepCallback.class);
	
	private final ImportCurriculumsContext context;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	
	public ImportCurriculumsFinishStepCallback(ImportCurriculumsContext context) {
		CoreSpringFactory.autowireObject(this);
		this.context = context;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		processCurriculums();
		processCurriculumElements();
		processRepositoryEntries();
		processEvents();
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private void processEvents() {
		List<ImportedRow> importedRows = context.getImportedElementsRows();
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.EVENT && !isIgnored(importedRow)) {
				if(importedRow.getLectureBlock() == null) {
					RepositoryEntry course = importedRow.getCourse();
					if(course != null) {
						createLectureBlock(importedRow, course);
					}
				} else {
					LectureBlock lectureBlock = lectureService.getLectureBlock(importedRow.getLectureBlock());
					if(lectureBlock != null) {
						updateLectureBlock(lectureBlock, importedRow);
					}
				}
				dbInstance.commit();
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void createLectureBlock(ImportedRow importedRow, RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(entry);
		lectureBlock.setExternalRef(importedRow.getIdentifier());
		updateLectureBlock(lectureBlock, importedRow);
	}

	private void updateLectureBlock(LectureBlock lectureBlock, ImportedRow importedRow) {
		lectureBlock.setTitle(importedRow.getDisplayName());
		lectureBlock.setDescription(importedRow.getDescription());
		lectureBlock.setLocation(importedRow.getLocation());
		
		Date startDate = AbstractExcelReader.toDate(importedRow.getStartDate(), importedRow.getStartTime());
		lectureBlock.setStartDate(startDate);
		Date endDate = AbstractExcelReader.toDate(importedRow.getEndDate(), importedRow.getEndTime());
		lectureBlock.setEndDate(endDate);
		
		String unit = importedRow.getUnit();
		int plannedLectures = 1;
		if(StringHelper.isLong(unit)) {
			int lectures = Integer.parseInt(unit);
			if(lectures > 0 && lectures <= 12) {
				plannedLectures = lectures;
			}
		}
		lectureBlock.setPlannedLecturesNumber(plannedLectures);
		
		List<Group> groups = null;
		CurriculumElement element = importedRow.getCurriculumElementParentRow().getCurriculumElement();
		if(element != null) {
			groups = List.of(element.getGroup());
		}
		lectureBlock = lectureService.save(lectureBlock, groups);
		
		Set<Long> taxonomyLevelsKeys = importedRow.getTaxonomyLevelsKeys();
		if(!taxonomyLevelsKeys.isEmpty()) {
			lectureService.updateTaxonomyLevels(lectureBlock, taxonomyLevelsKeys);
		}
	}

	private void processCurriculums() {
		List<ImportedRow> importedRows = context.getImportedCurriculumsRows();
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.CUR && !isIgnored(importedRow)) {
				if(importedRow.getCurriculum() == null) {
					createCurriculum(importedRow);
				} else {
					Curriculum curriculum = curriculumService.getCurriculum(importedRow.getCurriculum());
					if(curriculum != null) {
						updateCurriculum(curriculum, importedRow);
					}
				}
			}
			dbInstance.commit();
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void createCurriculum(ImportedRow importedRow) {
		String identifier = Formatter.truncateOnly(importedRow.getIdentifier(), 255);
		String displayName = Formatter.truncateOnly(importedRow.getDisplayName(), 255);
		String description = importedRow.getDescription();
		
		Organisation organisation = importedRow.getOrganisation();
		if(organisation == null) {
			organisation = organisationService.getDefaultOrganisation();
		}
		
		if(StringHelper.containsNonWhitespace(identifier)
				&& StringHelper.containsNonWhitespace(displayName)
				&& organisation != null) {
			boolean absences = "ON".equalsIgnoreCase(importedRow.getAbsences());
			Curriculum curriculum = curriculumService.createCurriculum(identifier, displayName, description, absences, organisation);
			importedRow.setCurriculum(curriculum);
		} else {
			log.debug("Curriculum not imported, missing mandatory data: {}, {}, {}",
					identifier, displayName, organisation);
		}
	}
	
	private void updateCurriculum(Curriculum curriculum, ImportedRow importedRow) {
		String title = importedRow.getDisplayName();
		if(StringHelper.containsNonWhitespace(title)) {
			curriculum.setDisplayName(Formatter.truncateOnly(title, 255));
		}
		
		String description = importedRow.getDescription();
		if(StringHelper.containsNonWhitespace(description)) {
			curriculum.setDescription(description);
		}
		
		if("ON".equalsIgnoreCase(importedRow.getAbsences()) || "OFF".equalsIgnoreCase(importedRow.getAbsences())) {
			curriculum.setLecturesEnabled("ON".equalsIgnoreCase(importedRow.getAbsences()));
		}
		curriculum = curriculumService.updateCurriculum(curriculum);
		importedRow.setCurriculum(curriculum);
		dbInstance.commit();
	}
	
	
	private void processCurriculumElements() {
		List<ImportedRow> importedRows = context.getImportedElementsRows();
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.IMPL) {
				processCurriculumElement(importedRow);
				dbInstance.commit();
			}
		}
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.type() == CurriculumExportType.ELEM) {
				processCurriculumElement(importedRow);
				dbInstance.commit();
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void processCurriculumElement(ImportedRow importedRow) {
		CurriculumElement element = null;
		if(importedRow.getCurriculumElement() == null) {
			element = createCurriculumElement(importedRow);
		} else {
			element = curriculumService.getCurriculumElement(importedRow.getCurriculumElement());
			if(element != null) {
				element = updateCurriculumElement(element, importedRow);
			}
		}
		importedRow.setCurriculumElement(element);
		
		List<TaxonomyLevel> taxonomyLevels = importedRow.getTaxonomyLevels();
		if(element != null && taxonomyLevels != null && !taxonomyLevels.isEmpty()) {
			curriculumService.updateTaxonomyLevels(element, taxonomyLevels, List.of());
		}
	}

	private CurriculumElement createCurriculumElement(ImportedRow importedRow) {
		CurriculumElementStatus status = toStatus(importedRow.getElementStatus());

		Date beginDate = importedRow.getStartDate() == null
				? null
				: AbstractExcelReader.toDate(importedRow.getStartDate(), null);
		Date endDate = importedRow.getEndDate() == null
				? null
				: AbstractExcelReader.toEndOfDay(importedRow.getEndDate());
		
		CurriculumElementType elementType = importedRow.getCurriculumElementType();
		if(elementType == null) {
			elementType = curriculumService.getDefaultCurriculumElementType();
		}
		
		CurriculumCalendars calendars = toCurriculumCalendars(importedRow.getCalendar());
		CurriculumLectures lectures = toCurriculumLectures(importedRow.getAbsences());
		CurriculumLearningProgress learningProgress = toCurriculumLearningProgress(importedRow.getProgress());
		
		Curriculum curriculum = importedRow.getCurriculumRow().getCurriculum();

		CurriculumElementRef parent = null;
		if(importedRow.type() == CurriculumExportType.IMPL) {
			// No parent, never
		} else if(importedRow.getCurriculumElementParentRow() != null) {
			parent = importedRow.getCurriculumElementParentRow().getCurriculumElement();
		} else if(importedRow.getImplementation() != null) {
			parent = importedRow.getImplementation();
		} else if(importedRow.getImplementationRow() != null) {
			parent = importedRow.getImplementationRow().getCurriculumElement();
		}
		
		return curriculumService.createCurriculumElement(importedRow.getIdentifier(), importedRow.getDisplayName(),
				status, beginDate, endDate, parent, elementType,
				calendars, lectures, learningProgress, curriculum);
	}
	
	private CurriculumElement updateCurriculumElement(CurriculumElement element, ImportedRow importedRow) {
		if(StringHelper.containsNonWhitespace(importedRow.getElementStatus())) {
			((CurriculumElementImpl)element).setElementStatus(toStatus(importedRow.getElementStatus()));
		}
		
		Date beginDate = importedRow.getStartDate() == null
				? null
				: AbstractExcelReader.toDate(importedRow.getStartDate(), null);
		if(beginDate != null) {
			element.setBeginDate(beginDate);
		}
		
		Date endDate = importedRow.getEndDate() == null
				? null
				: AbstractExcelReader.toEndOfDay(importedRow.getEndDate());
		if(endDate != null) {
			element.setEndDate(endDate);
		}
		
		if(StringHelper.containsNonWhitespace(importedRow.getCalendar())) {
			element.setCalendars(toCurriculumCalendars(importedRow.getCalendar()));
		}
		if(StringHelper.containsNonWhitespace(importedRow.getAbsences())) {
			element.setLectures(toCurriculumLectures(importedRow.getAbsences()));
		}
		if(StringHelper.containsNonWhitespace(importedRow.getProgress())) {
			element.setLearningProgress(toCurriculumLearningProgress(importedRow.getProgress()));
		}
		
		return curriculumService.updateCurriculumElement(element);
	}
	
	private CurriculumLearningProgress toCurriculumLearningProgress(String val) {
		if(ImportCurriculumsHelper.ON.equalsIgnoreCase(val)) {
			return CurriculumLearningProgress.enabled;
		}
		if(ImportCurriculumsHelper.OFF.equalsIgnoreCase(val)) {
			return CurriculumLearningProgress.disabled;
		}
		return CurriculumLearningProgress.inherited;
	}
	
	private CurriculumLectures toCurriculumLectures(String val) {
		if(ImportCurriculumsHelper.ON.equalsIgnoreCase(val)) {
			return CurriculumLectures.enabled;
		}
		if(ImportCurriculumsHelper.OFF.equalsIgnoreCase(val)) {
			return CurriculumLectures.disabled;
		}
		return CurriculumLectures.inherited;
	}
	
	private CurriculumCalendars toCurriculumCalendars(String val) {
		if(ImportCurriculumsHelper.ON.equalsIgnoreCase(val)) {
			return CurriculumCalendars.enabled;
		}
		if(ImportCurriculumsHelper.OFF.equalsIgnoreCase(val)) {
			return CurriculumCalendars.disabled;
		}
		return CurriculumCalendars.inherited;
	}
	
	private CurriculumElementStatus toStatus(String val) {
		return CurriculumElementStatus.valueOf(val.toLowerCase());
	}
	
	private void processRepositoryEntries() {
		List<ImportedRow> importedRows = context.getImportedElementsRows();
		for(ImportedRow importedRow:importedRows) {
			if(importedRow.getCurriculumElementParentRow() == null) {
				continue;
			}
			CurriculumElement element = importedRow.getCurriculumElementParentRow().getCurriculumElement();
			
			if(importedRow.type() == CurriculumExportType.COURSE && !isIgnored(importedRow)) {
				curriculumService.addRepositoryEntry(element, importedRow.getCourse(), false);
				updateRepositoryEntry(importedRow, importedRow.getCourse());
			} else if(importedRow.type() == CurriculumExportType.TMPL && !isIgnored(importedRow)) {
				curriculumService.addRepositoryTemplate(element, importedRow.getTemplate());
				updateRepositoryEntry(importedRow, importedRow.getTemplate());
			}
			
			dbInstance.commit();
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void updateRepositoryEntry(ImportedRow importedRow, RepositoryEntry entry) {
		if(!Objects.equals(importedRow.getDisplayName(), entry.getDisplayname())) {
			entry = repositoryManager.setDescriptionAndName(entry, importedRow.getDisplayName(), entry.getDescription());
		}
		
		if((importedRow.getStartDate() != null && importedRow.getEndDate() != null)
			|| StringHelper.containsNonWhitespace(importedRow.getLocation())) {
			Date beginDate = AbstractExcelReader.toDate(importedRow.getStartDate(), null);
			Date endDate = AbstractExcelReader.toDate(importedRow.getEndDate(), null);
			RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
			if(beginDate != null && endDate != null) {
				if(lifecycle == null || !lifecycle.isPrivateCycle()) {
					String softKey = "lf_" + entry.getSoftkey();
					lifecycle = lifecycleDao.create(entry.getDisplayname(), softKey, true, beginDate, endDate);
				} else {
					lifecycle.setValidFrom(beginDate);
					lifecycle.setValidTo(endDate);
					lifecycle = lifecycleDao.updateLifecycle(lifecycle);
				}
			}
			
			String location = entry.getLocation();
			if(StringHelper.containsNonWhitespace(importedRow.getLocation())) {
				location = importedRow.getLocation();
			}
			repositoryManager.setLocationAndLifecycle(entry, location, lifecycle);
		}
	}
	
	/**
	 * Checks if the row is ignored, has errors...
	 * 
	 * @param importedRow The row to import
	 * @return true if the row is ignored
	 */
	private boolean isIgnored(ImportedRow importedRow) {
		if(importedRow.isIgnored()
				|| importedRow.getStatus() == ImportCurriculumsStatus.NO_CHANGES
				|| importedRow.getStatus() == ImportCurriculumsStatus.ERROR
				|| importedRow.getValidationStatistics().errors() > 0) {
			log.debug("Curriculum not imported: status: {}, errors: {}, to ignore: {}",
					importedRow.getStatus(), importedRow.getValidationStatistics().errors(), importedRow.isIgnored());
			return true;
		}
		return false;
	}
}
