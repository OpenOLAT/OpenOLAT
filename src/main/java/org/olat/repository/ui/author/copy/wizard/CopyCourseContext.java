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
package org.olat.repository.ui.author.copy.wizard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.reminder.model.ReminderRow;
import org.olat.course.wizard.CourseDisclaimerContext;
import org.olat.group.model.BusinessGroupReference;
import org.olat.group.ui.main.BGTableItem;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.author.copy.wizard.additional.AssessmentModeCopyInfos;

/**
 * Initial date: 18.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyCourseContext {
	
	public static final String CONTEXT_KEY = CopyCourseContext.class.getSimpleName();
	
	// Executing identity
	private Identity executingIdentity;
	private Locale executingLocale;
	
	// Repository entry and course
	private RepositoryHandlerFactory handlerFactory;
	private RepositoryEntry sourceRepositoryEntry;
	private ICourse course;
	private List<CopyCourseOverviewRow> courseNodes;
	private Map<String, CopyCourseOverviewRow> courseNodesMap;
	private boolean customConfigsLoaded;
	private boolean isLearningPath;
	private boolean hasWiki;
	private boolean hasBlog;
	private boolean hasFolder;
	private boolean hasTest;
	private boolean hasTask;
	private boolean hasDateDependantNodes;
	private boolean hasLectureBlocks;
	private boolean hasReminders;
	private boolean hasDateDependantReminders;
	private boolean hasAssessmentModes;
	private boolean hasDocuments;
	private boolean hasCoachDocuments;
	private DateWithLabel earliestDateWithNode;
	private long dateDifferenceByEarliest = 0l;
	private long dateDifferenceByEarliestCurrent = 0l;
	private int daysShifted = 0; // Used in the shift all dates by days dialogue

	// Metadata
	private String displayName;
	private String externalRef;
	private String authors; 
	private String licenseTypeKey;
	private String licensor;
	private String licenseFreetext;
	private String expenditureOfWork;
	
	// GroupStep
	private CopyType groupCopyType;			 
	private CopyType customGroupCopyType;
	private List<BGTableItem> groups;
	private List<BusinessGroupReference> newGroupReferences = new ArrayList<>();
	private boolean hasGroups;
	
	// OwnersStep
	private CopyType ownersCopyType;
	private CopyType customOwnersCopyType;
	private List<Identity> newOwners;
	private boolean hasOwners;
	
	// CoachesStep
	private CopyType coachesCopyType;
	private CopyType customCoachesCopyType;
	private List<Identity> newCoaches;
	private boolean hasCoaches;
	
	// ExecutionStep
	private ExecutionType executionType;
	private Date beginDate;
	private Date endDate;
	private long dateDifference = 0;
	private Long semesterKey;
	private String location;
	
	// CatalogStep
	private CopyType catalogCopyType;
	private CopyType customCatalogCopyType;
	private boolean hasCatalogEntry;
	
	// DisclaimerStep
	private CopyType disclaimerCopyType;
	private CopyType customDisclaimerCopyType;
	private CourseDisclaimerContext disclaimerCopyContext;
	private boolean hasDisclaimer;
	
	// Learning resource copy types
	private CopyType blogCopyType;
	private CopyType folderCopyType;
	private CopyType wikiCopyType;
	private CopyType taskCopyType;
	private CopyType testCopyType;
	
	// ReminderStep
	private CopyType reminderCopyType;
	private List<ReminderRow> reminderRows;
	
	// AssessmentModeStep
	private CopyType assessmentModeCopyType;
	private CopyType customAssessmentModeCopyType;
	private List<AssessmentMode> assessmentModeRows;
	private Map<AssessmentMode, AssessmentModeCopyInfos> assessmentCopyInfos;
	
	// LectureBlockStep
	private CopyType lectureBlockCopyType;
	private CopyType customLectureBlockCopyType;
	private List<LectureBlockRow> lectureBlockRows;
	
	// Documents
	private CopyType documentsCopyType;
	private CopyType coachDocumentsCopyType;
	
	// Load config from module
	public void loadFromWizardConfig(CopyCourseWizardModule wizardModule) {
		setGroupCopyType(wizardModule.getGroupsCopyType());
		setOwnersCopyType(wizardModule.getOwnersCopyType());
		setCoachesCopyType(wizardModule.getCoachesCopyType());
		setCatalogCopyType(wizardModule.getCatalogCopyType());
		setDisclaimerCopyType(wizardModule.getDisclaimerCopyType());
		
		setTestCopyType(wizardModule.getTestCopyType());
		setTaskCopyType(wizardModule.getTaskCopyType());
		setBlogCopyType(wizardModule.getBlogCopyType());
		setFolderCopyType(wizardModule.getFolderCopyType());
		setWikiCopyType(wizardModule.getWikiCopyType());
		
		setReminderCopyType(wizardModule.getReminderCopyType());
		setAssessmentModeCopyType(wizardModule.getAssessmentCopyType());
		setLectureBlockCopyType(wizardModule.getLectureBlockCopyType());
		setDocumentsCopyType(wizardModule.getDocumentsCopyType());
		setCoachDocumentsCopyType(wizardModule.getCoachDocumentsCopyType());
	}
	
	public Identity getExecutingIdentity() {
		return executingIdentity;
	}
	
	public void setExecutingIdentity(Identity executingIdentity) {
		this.executingIdentity = executingIdentity;
	}
	
	public Locale getExecutingLocale() {
		return executingLocale;
	}
	
	public void setExecutingLocale(Locale executingLocale) {
		this.executingLocale = executingLocale;
	}
	
	public RepositoryHandlerFactory getHandlerFactory() {
		return handlerFactory;
	}
	
	public void setHandlerFactory(RepositoryHandlerFactory handlerFactory) {
		this.handlerFactory = handlerFactory;
	}
	
	public RepositoryEntry getSourceRepositoryEntry() {
		return sourceRepositoryEntry;
	}
	
	public void setSourceRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.sourceRepositoryEntry = repositoryEntry;
	}	
	
	public ICourse getCourse() {
		return course;
	}
	
	public void setCourse(ICourse course) {
		this.course = course;
	}
	
	public List<CopyCourseOverviewRow> getCourseNodes() {
		return courseNodes;
	}
	
	public void setCourseNodes(List<CopyCourseOverviewRow> courseNodes) {
		this.courseNodes = courseNodes;
		
		if (courseNodes != null && !courseNodes.isEmpty()) {
			// For easier handling, put all nodes into a map with their identifier
			Map<String, CopyCourseOverviewRow> sourceCourseNodesMap = getCourseNodes().stream().collect(Collectors.toMap(row -> row.getEditorNode().getIdent(), Function.identity()));
			setCourseNodesMap(sourceCourseNodesMap);
		}
	}
	
	public Map<String, CopyCourseOverviewRow> getCourseNodesMap() {
		return courseNodesMap;
	}
	
	public void setCourseNodesMap(Map<String, CopyCourseOverviewRow> courseNodesMap) {
		this.courseNodesMap = courseNodesMap;
	}
	
	public boolean isCustomConfigsLoaded() {
		return customConfigsLoaded;
	}
	
	public void setCustomConfigsLoaded(boolean customConfigsLoaded) {
		this.customConfigsLoaded = customConfigsLoaded;
	}
	
	public boolean isLearningPath() {
		return isLearningPath;
	}
	
	public void setLearningPath(boolean isLearningPath) {
		this.isLearningPath = isLearningPath;
	}
	
	public boolean hasBlog() {
		return hasBlog;
	}
	
	public void setBlog(boolean hasBlog) {
		this.hasBlog = hasBlog;
	}
	
	public boolean hasFolder() {
		return hasFolder;
	}
	
	public void setFolder(boolean hasFolder) {
		this.hasFolder = hasFolder;
	}
	
	public boolean hasTask() {
		return hasTask;
	}
	
	public void setHasTask(boolean hasTask) {
		this.hasTask = hasTask;
	}
	
	public boolean hasTest() {
		return hasTest;
	}
	
	public void setTest(boolean hasTest) {
		this.hasTest = hasTest;
	}
	
	public boolean hasWiki() {
		return hasWiki;
	}
	
	public void setWiki(boolean hasWiki) {
		this.hasWiki = hasWiki;
	}
	
	public boolean hasNodeSpecificSettings() {
		return hasFolder || hasWiki || hasBlog || hasTask || hasTest;
	}
	
	public RepositoryEntryLifecycle getRepositoryLifeCycle() {
		if (sourceRepositoryEntry != null) {
			return sourceRepositoryEntry.getLifecycle();
		} else {
			return null;
		}
	}
	
	public boolean hasDateDependantNodes() {
		return hasDateDependantNodes;
	}
	
	public void setDateDependantNodes(boolean hasDateDependantNodes) {
		this.hasDateDependantNodes = hasDateDependantNodes;
	}
	
	public boolean hasAssessmentModes() {
		return hasAssessmentModes;
	}
	
	public void setAssessmentModes(boolean hasAssessmentModes) {
		this.hasAssessmentModes = hasAssessmentModes;
	}
	
	public boolean hasLectureBlocks() {
		return hasLectureBlocks;
	}
	
	public void setLectureBlocks(boolean hasLectureBlocks) {
		this.hasLectureBlocks = hasLectureBlocks;
	}
	
	public boolean hasAdditionalSettings() {
		return hasDateDependantReminders || hasLectureBlocks || hasAssessmentModes;
	}
	
	public boolean hasReminders() {
		return hasReminders;
	}
	
	public void setHasReminders(boolean hasReminders) {
		this.hasReminders = hasReminders;
	}	
	
	public boolean hasDocuments() {
		return hasDocuments;
	}
	
	public void setDocuments(boolean hasDocuments) {
		this.hasDocuments = hasDocuments;
	}
	
	public CopyType getDocumentsCopyType() {
		return documentsCopyType;
	}
	
	public void setDocumentsCopyType(CopyType documentsCopyType) {
		this.documentsCopyType = documentsCopyType;
	}
	
	public boolean hasCoachDocuments() {
		return hasCoachDocuments;
	}
	
	public void setCoachDocuments(boolean hasCoachDocuments) {
		this.hasCoachDocuments = hasCoachDocuments;
	}
	
	public CopyType getCoachDocumentsCopyType() {
		return coachDocumentsCopyType;
	}
	
	public void setCoachDocumentsCopyType(CopyType coachDocumentsCopyType) {
		this.coachDocumentsCopyType = coachDocumentsCopyType;
	}
	
	public boolean hasDateDependantReminders() {
		return hasDateDependantReminders;
	}
	
	public void setDateDependantReminders(boolean hasDateDependantReminders) {
		this.hasDateDependantReminders = hasDateDependantReminders;
	}
	
	public String getDisplayName() {
		return getValue(displayName, sourceRepositoryEntry.getDisplayname());
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getExternalRef() {
		return getValue(externalRef, sourceRepositoryEntry.getExternalRef());
	}
	
	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}
	
	public long getDateDifference() {
		return dateDifference;
	}
	
	public void setDateDifference(long dateDifference) {
		this.dateDifference = dateDifference;
	}
	
	public int getDaysShifted() {
		return daysShifted;
	}
	
	public void setDaysShifted(int daysShifted) {
		this.daysShifted = daysShifted;
	}

	public String getAuthors() {
		return getValue(authors, sourceRepositoryEntry.getAuthors());
	}
	
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	
	public String getLicenseTypeKey() {
		return licenseTypeKey;
	}
	
	public void setLicenseTypeKey(String licenseTypeKey) {
		this.licenseTypeKey = licenseTypeKey;
	}
	
	public String getLicensor() {
		return licensor;
	}
	
	public void setLicensor(String licensor) {
		this.licensor = licensor;
	}
	
	public String getLicenseFreetext() {
		return licenseFreetext;
	}
	
	public void setLicenseFreetext(String freetext) {
		this.licenseFreetext = freetext;
	}
	
	public String getExpenditureOfWork() {
		return expenditureOfWork;
	}
	
	public void setExpenditureOfWork(String expenditureOfWork) {
		this.expenditureOfWork = expenditureOfWork;
	}

	public CopyType getGroupCopyType() {
		return groupCopyType;
	}
	
	public void setGroupCopyType(CopyType groupCopyType) {
		this.groupCopyType = groupCopyType;
	}
	
	public CopyType getCustomGroupCopyType() {
		return customGroupCopyType;
	}
	
	public void setCustomGroupCopyType(CopyType customGroupCopyType) {
		this.customGroupCopyType = customGroupCopyType;
	}
	
	public List<BGTableItem> getGroups() {
		return groups;
	}
	
	public void setGroups(List<BGTableItem> groups) {
		this.groups = groups;
	}
	
	public boolean hasGroups() {
		return hasGroups;
	}
	
	public void setHasGroups(boolean hasGroups) {
		this.hasGroups = hasGroups;
	}
	
	public List<BusinessGroupReference> getNewGroupReferences() {
		return newGroupReferences;
	}
	
	public void setNewGroupReferences(List<BusinessGroupReference> newGroupReferences) {
		this.newGroupReferences = newGroupReferences;
	}
	
	public CopyType getOwnersCopyType() {
		return ownersCopyType;
	}
	
	public void setOwnersCopyType(CopyType ownersCopyType) {
		this.ownersCopyType = ownersCopyType;
	}
	
	public CopyType getCustomOwnersCopyType() {
		return customOwnersCopyType;
	}
	
	public void setCustomOwnersCopyType(CopyType customOwnersCopyType) {
		this.customOwnersCopyType = customOwnersCopyType;
	}
	
	public List<Identity> getNewOwners() {
		return newOwners;
	}
	
	public void setNewOwners(List<Identity> newOwners) {
		this.newOwners = newOwners;
	}
	
	public boolean hasOwners() {
		return hasOwners;
	}
	
	public void setHasOwners(boolean hasOwners) {
		this.hasOwners = hasOwners;
	}
	
	public CopyType getCoachesCopyType() {
		return coachesCopyType;
	}
	
	public void setCoachesCopyType(CopyType coachesCopyType) {
		this.coachesCopyType = coachesCopyType;
	}
	
	public CopyType getCustomCoachesCopyType() {
		return customCoachesCopyType;
	}
	
	public void setCustomCoachesCopyType(CopyType customCoachesCopyType) {
		this.customCoachesCopyType = customCoachesCopyType;
	}
	
	public List<Identity> getNewCoaches() {
		if (getCoachesCopyType().equals(CopyType.ignore)) {
			return new ArrayList<>();
		}
		
		return newCoaches;
	}
	
	public void setNewCoaches(List<Identity> newCoaches) {
		this.newCoaches = newCoaches;
	}
	
	public boolean hasCoaches() {
		return hasCoaches;
	}
	
	public void setHasCoaches(boolean hasCoaches) {
		this.hasCoaches = hasCoaches;
	}
	
	public ExecutionType getExecutionType() {
		return executionType;
	}
	
	public void setExecutionType(ExecutionType executionType) {
		this.executionType = executionType;
	}
	
	public Date getBeginDate() {
		if (beginDate != null) {
			return beginDate;
		} else if (getRepositoryLifeCycle() != null) {
			return getRepositoryLifeCycle().getValidFrom();
		} else {
			return null;
		}
	}
	
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}
	
	public Date getEndDate() {
		if (endDate != null) {
			return endDate;
		} else if (getRepositoryLifeCycle() != null) {
			return getRepositoryLifeCycle().getValidTo();
		}
		
		return null;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public Long getSemesterKey() {
		return semesterKey;
	}
	
	public void setSemesterKey(Long semesterKey) {
		this.semesterKey = semesterKey;
	}
	
	public String getLocation() {
		if (location != null) {
			return location;
		} else if (sourceRepositoryEntry != null) {
			return sourceRepositoryEntry.getLocation();
		} else {
			return null;
		}
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public CopyType getCatalogCopyType() {
		return catalogCopyType;
	}
	
	public void setCatalogCopyType(CopyType catalogCopyType) {
		this.catalogCopyType = catalogCopyType;
	}
	
	public boolean hasCatalogEntry() {
		return hasCatalogEntry;
	}
	
	public void setHasCatalogEntry(boolean hasCatalogEntry) {
		this.hasCatalogEntry = hasCatalogEntry;
	}
	
	public CopyType getCustomCatalogCopyType() {
		return customCatalogCopyType;
	}
	
	public void setCustomCatalogCopyType(CopyType customCatalogCopyType) {
		this.customCatalogCopyType = customCatalogCopyType;
	}
	
	public CopyType getDisclaimerCopyType() {
		return disclaimerCopyType;
	}
	
	public void setDisclaimerCopyType(CopyType disclaimerCopyType) {
		this.disclaimerCopyType = disclaimerCopyType;
	}
	
	public CopyType getCustomDisclaimerCopyType() {
		return customDisclaimerCopyType;
	}
	
	public void setCustomDisclaimerCopyType(CopyType customDisclaimerCopyType) {
		this.customDisclaimerCopyType = customDisclaimerCopyType;
	}
	
	public CourseDisclaimerContext getDisclaimerCopyContext() {
		return disclaimerCopyContext;
	}
	
	public void setDisclaimerCopyContext(CourseDisclaimerContext disclaimerCopyContext) {
		this.disclaimerCopyContext = disclaimerCopyContext;
	}
	
	public boolean hasDisclaimer() {
		return hasDisclaimer;
	}
	
	public void setHasDisclaimer(boolean hasDisclaimer) {
		this.hasDisclaimer = hasDisclaimer;
	}
	
	public CopyType getBlogCopyType() {
		return blogCopyType;
	}
	
	public void setBlogCopyType(CopyType blogCopyType) {
		this.blogCopyType = blogCopyType;
	}
	
	public CopyType getFolderCopyType() {
		return folderCopyType;
	}
	
	public void setFolderCopyType(CopyType folderCopyType) {
		this.folderCopyType = folderCopyType;
	}
	
	public CopyType getWikiCopyType() {
		return wikiCopyType;
	}
	
	public void setWikiCopyType(CopyType wikiCopyType) {
		this.wikiCopyType = wikiCopyType;
	}
	
	public CopyType getTaskCopyType() {
		return taskCopyType;
	}
	
	public void setTaskCopyType(CopyType taskCopyType) {
		this.taskCopyType = taskCopyType;
	}
	
	public CopyType getTestCopyType() {
		return testCopyType;
	}
	
	public void setTestCopyType(CopyType testCopyType) {
		this.testCopyType = testCopyType;
	}
	
	public CopyType getReminderCopyType() {
		return reminderCopyType;
	}
	
	public void setReminderCopyType(CopyType reminderCopyType) {
		this.reminderCopyType = reminderCopyType;
	}
	
	public List<ReminderRow> getReminderRows() {
		return reminderRows;
	}
	
	public void setReminderRows(List<ReminderRow> reminderRows) {
		this.reminderRows = reminderRows;
	}
	
	public CopyType getAssessmentModeCopyType() {
		return assessmentModeCopyType;
	}
	public void setAssessmentModeCopyType(CopyType assessmentModeCopyType) {
		this.assessmentModeCopyType = assessmentModeCopyType;
	}
	
	public CopyType getCustomAssessmentModeCopyType() {
		return customAssessmentModeCopyType;
	}
	
	public void setCustomAssessmentModeCopyType(CopyType customAssessmentModeCopyType) {
		this.customAssessmentModeCopyType = customAssessmentModeCopyType;
	}
	
	public List<AssessmentMode> getAssessmentModeRows() {
		return assessmentModeRows;
	}
	
	public void setAssessmentModeRows(List<AssessmentMode> assessmentModeRows) {
		this.assessmentModeRows = assessmentModeRows;
	}	
	
	public Map<AssessmentMode, AssessmentModeCopyInfos> getAssessmentCopyInfos() {
		return assessmentCopyInfos;
	}
	
	public void setAssessmentCopyInfos(Map<AssessmentMode, AssessmentModeCopyInfos> assessmentCopyInfos) {
		this.assessmentCopyInfos = assessmentCopyInfos;
	}
	
	public CopyType getLectureBlockCopyType() {
		return lectureBlockCopyType;
	}
	
	public void setLectureBlockCopyType(CopyType lectureBlockCopyType) {
		this.lectureBlockCopyType = lectureBlockCopyType;
	}
	
	public CopyType getCustomLectureBlockCopyType() {
		return customLectureBlockCopyType;
	}
	
	public void setCustomLectureBlockCopyType(CopyType customLectureBlockCopyType) {
		this.customLectureBlockCopyType = customLectureBlockCopyType;
	}
	
	public List<LectureBlockRow> getLectureBlockRows() {
		return lectureBlockRows;
	}
	
	public void setLectureBlockRows(List<LectureBlockRow> lectureBlockRows) {
		this.lectureBlockRows = lectureBlockRows;
	}
	
	public DateWithLabel getEarliestDateWithNode() {
		return earliestDateWithNode;
	}
	
	public void setEarliestDateWithNode(DateWithLabel earliestDateWithNode) {
		this.earliestDateWithNode = earliestDateWithNode;
	}
	
	public long getDateDifferenceByEarliest() {
		return dateDifferenceByEarliest;
	}
	
	public void setDateDifferenceByEarliest(long dateDifferenceByEarliest) {
		this.dateDifferenceByEarliest = dateDifferenceByEarliest;
	}
	
	public long getDateDifferenceByEarliestCurrent() {
		return dateDifferenceByEarliestCurrent;
	}
	
	public void setDateDifferenceByEarliestCurrent(long dateDifferenceByEarliestCurrent) {
		this.dateDifferenceByEarliestCurrent = dateDifferenceByEarliestCurrent;
	}
	
	
	
	// Helpers
	public enum CopyType {
		copy,
		replace,
		reference,
		createNew,
		ignore,
		custom,
		automatic;
	}
	
	public enum ExecutionType {
		none, 
		beginAndEnd,
		semester;
	}
	
	public String getValue(String newValue, String oldValue) {
		return StringHelper.containsNonWhitespace(newValue) ? newValue : oldValue;
	}
	
	public Date getValue(Date newValue, Date oldValue) {
		return newValue != null ? newValue : oldValue;
	}
	
	public CopyType getCopyType(String copyTypeString) {
		try {
			return CopyType.valueOf(copyTypeString);
		} catch(Exception e) {
			return null;
		}
	}
	
	public long getDateDifference(String courseNodeIdent) {
		long dateDifference = getDateDifference();
		
		if (getCourseNodesMap() != null) {
			CopyCourseOverviewRow overviewRow = getCourseNodesMap().get(courseNodeIdent);
			
			if (overviewRow.getNewStartDate() != null && DueDateConfig.isAbsolute(overviewRow.getStart())) {
				dateDifference = overviewRow.getNewStartDate().getTime() - overviewRow.getStart().getAbsoluteDate().getTime();
			} else if (overviewRow.getNewEndDate() != null && DueDateConfig.isAbsolute(overviewRow.getEnd())) {
				dateDifference = overviewRow.getNewEndDate().getTime() - overviewRow.getEnd().getAbsoluteDate().getTime();
			}
		} 
		
		return dateDifference;
	}
}
