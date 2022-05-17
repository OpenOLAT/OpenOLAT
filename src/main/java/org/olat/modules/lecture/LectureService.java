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
package org.olat.modules.lecture;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.modules.lecture.model.AbsenceNoticeInfos;
import org.olat.modules.lecture.model.AggregatedLectureBlocksStatistics;
import org.olat.modules.lecture.model.IdentityRateWarning;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.model.LectureBlockBlockStatistics;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureBlockRollCallAndCoach;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.lecture.model.LectureBlockWithNotice;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LectureCurriculumElementInfos;
import org.olat.modules.lecture.model.LectureCurriculumElementSearchParameters;
import org.olat.modules.lecture.model.LectureReportRow;
import org.olat.modules.lecture.model.LectureRepositoryEntryInfos;
import org.olat.modules.lecture.model.LectureRepositoryEntrySearchParameters;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.model.LecturesMemberSearchParameters;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LectureService {
	
	
	/**
	 * Get (or create) the configuration object for the specified repository
	 * entry.
	 * 
	 * @param entry
	 * @return A persisted configuration
	 */
	public RepositoryEntryLectureConfiguration getRepositoryEntryLectureConfiguration(RepositoryEntry entry);
	
	/**
	 * 
	 * @param entry The course
	 * @return true if the module is enabled and the course has the lectures enabled
	 */
	public boolean isRepositoryEntryLectureEnabled(RepositoryEntryRef entry);
	
	/**
	 * Update the specified configuration
	 * @param config The configuration to merge
	 * @return A merged configuration
	 */
	public RepositoryEntryLectureConfiguration updateRepositoryEntryLectureConfiguration(RepositoryEntryLectureConfiguration config);
	
	/**
	 * Clone the configuration of the source if it's available.
	 * 
	 * @param sourceEntry The source of the clonage
	 * @param targetEntry The target of the clonage
	 * @return A cloned configuration if the source has one, or null
	 */
	public RepositoryEntryLectureConfiguration copyRepositoryEntryLectureConfiguration(RepositoryEntry sourceEntry, RepositoryEntry targetEntry);
	
	/**
	 * Create but not persist a new lecture block.
	 * 
	 * @param entry The repository entry which own the block
	 * @return A new lecture block
	 */
	public LectureBlock createLectureBlock(RepositoryEntry entry);
	
	/**
	 * Merge or persist the specified lecture block and return
	 * the fresh block.
	 * 
	 * @param lectureBlock The block to merge or persist
	 * @return The merged block
	 */
	public LectureBlock save(LectureBlock lectureBlock, List<Group> groups);
	
	/**
	 * The method will set the status of the lecture block and 
	 * the status of the roll call.
	 * 
	 * @param lectureBlock The lecture block to close
	 * @param author The user which trigger the action
	 * @return The updated lecture block
	 */
	public LectureBlock close(LectureBlock lectureBlock, Identity author);
	
	/**
	 * The method will set the status of the lecture block and 
	 * the status of the roll call.
	 * 
	 * @param lectureBlock The lecture block to close
	 * @return The updated lecture block
	 */
	public LectureBlock cancel(LectureBlock lectureBlock);
	
	public String toAuditXml(LectureBlock lectureBlock);
	
	public LectureBlock toAuditLectureBlock(String xml);
	
	public String toAuditXml(LectureBlockRollCall rollCall);
	
	public LectureBlockRollCall toAuditLectureBlockRollCall(String xml);

	public String toAuditXml(LectureParticipantSummary summary);
	
	public String toAuditXml(AbsenceNotice absenceNotice);
	
	public AbsenceNotice toAuditAbsenceNotice(String xml);
	
	public LectureParticipantSummary toAuditLectureParticipantSummary(String xml);

	
	/**
	 * Append content to the log saved on the lecture block.
	 * 
	 * @param lectureBlock The lecture block
	 * @param log The content to append
	 */
	public void auditLog(LectureBlockAuditLog.Action action, String before, String after, String message,
			LectureBlockRef lectureBlock, LectureBlockRollCall rollCall,
			RepositoryEntryRef entry, IdentityRef assessedIdentity, IdentityRef author);
	
	/**
	 * 
	 * @param action
	 * @param before
	 * @param after
	 * @param message
	 * @param absenceNotice
	 * @param assessedIdentity
	 * @param author
	 */
	public void auditLog(LectureBlockAuditLog.Action action, String before, String after, String message,
			AbsenceNoticeRef absenceNotice, IdentityRef assessedIdentity, IdentityRef author);
	
	public List<LectureBlockAuditLog> getAuditLog(LectureBlockRef lectureBlock);
	
	/**
	 * The audit log of a specific user.
	 * 
	 * @param assessedIdentity The assessed identity (Mandatory)
	 * @return A list of roll call changes.
	 */
	public List<LectureBlockAuditLog> getAuditLog(IdentityRef assessedIdentity);
	
	/**
	 * The audit log of a specific user with a specific action in the
	 * specified course.
	 * 
	 * @param entry The course (mandatory)
	 * @param assessedIdentity The assessed identity (mandatory)
	 * @param action The action (mandatory)
	 * @return A list of roll call changes.
	 */
	public List<LectureBlockAuditLog> getAuditLog(RepositoryEntryRef entry, IdentityRef assessedIdentity, LectureBlockAuditLog.Action action);
	
	/**
	 * Returns the audit log of the specified course.
	 * 
	 * @param entry The course
	 * @return A list of roll call changes.
	 */
	public List<LectureBlockAuditLog> getAuditLog(RepositoryEntryRef entry);
	
	/**
	 * Reload the lecture block.
	 * @param block
	 * @return A fresh lecture block
	 */
	public LectureBlock getLectureBlock(LectureBlockRef block);
	
	public List<LectureBlock> getLectureBlocks(List<Long> keys);
	
	/**
	 * Make a copy of the specified lecture block.
	 * @param block
	 * @return
	 */
	public LectureBlock copyLectureBlock(String newTitle, LectureBlock block);
	
	public LectureBlock moveLectureBlock(LectureBlockRef block, RepositoryEntry newEntry);
	
	/**
	 * Delete the lecture block definitively, the roll calls...
	 * 
	 * @param block The block to delete.
	 */
	public void deleteLectureBlock(LectureBlock block, Identity doer);
	
	/**
	 * Delete all the lecture blocks and configuration of the specified course.
	 * 
	 * @param entry
	 */
	public int delete(RepositoryEntry entry);
	
	/**
	 * Returns active/inactive or all configured reasons.
	 * 
	 * @param enabled true for enabled, false disabled and null for all
	 * @return A list of reasons
	 */
	public List<Reason> getAllReasons();
	
	/**
	 * Load a reason by its primary key.
	 * 
	 * @param key The primary key
	 * @return A reason
	 */
	public Reason getReason(Long key);
	
	/**
	 * 
	 * @param title
	 * @param description
	 * @return
	 */
	public Reason createReason(String title, String description, boolean enabled);
	
	/**
	 * Updates the reason and return the freshest.
	 * 
	 * @param reason The reason to update
	 * @return A merged reason
	 */
	public Reason updateReason(Reason reason);
	
	public boolean isReasonInUse(Reason reason);
	
	public boolean deleteReason(Reason reason);
	
	/**
	 * Returns active/inactive or all configured absences categories.
	 * 
	 * @param enabled true for enabled, false disabled and null for all
	 * @return A list of absences categories
	 */
	public List<AbsenceCategory> getAbsencesCategories(Boolean enabled);
	
	/**
	 * Load a category by its primary key.
	 * 
	 * @param key The primary key
	 * @return A category of absences
	 */
	public AbsenceCategory getAbsenceCategory(Long key);
	
	/**
	 * Creates and persists a new category.
	 * 
	 * @param title The title
	 * @param description The description
	 * @return
	 */
	public AbsenceCategory createAbsenceCategory(String title, String description, boolean enabled);
	
	/**
	 * Updates the category and return the freshest.
	 * 
	 * @param reason The category to update
	 * @return A merged category
	 */
	public AbsenceCategory updateAbsenceCategory(AbsenceCategory category);
	
	public boolean isAbsenceCategoryInUse(AbsenceCategory category);
	
	public void deleteAbsenceCategory(AbsenceCategory category);
	
	/**
	 * Creates a new absence notice for the specified identity.
	 * 
	 * @param absentIdentity The absent (which is wrong)
	 * @param type The type of absence
	 * @return A new absence notice
	 */
	public AbsenceNotice createAbsenceNotice(Identity absentIdentity, AbsenceNoticeType type, AbsenceNoticeTarget target,
			Date startDate, Date endDate, AbsenceCategory category, String absenceRason, Boolean authorized,
			List<RepositoryEntry> entries, List<LectureBlock> lectureBlocks, Identity actingIdentity);
	
	/**
	 * 
	 * @param absenceNotice The absence notice to update
	 * @param authorizer The user which authorize the absence (or null)
	 * @param entries A new list of repository entries
	 * @param lectureBlocks Or a new list of lecture blocks
	 * @return A refreshed absence notice
	 */
	public AbsenceNotice updateAbsenceNotice(AbsenceNotice absenceNotice, Identity authorizer,
			List<RepositoryEntry> entries, List<LectureBlock> lectureBlocks, Identity actingIdentity);
	
	public AbsenceNotice updateAbsenceNoticeAuthorization(AbsenceNotice absenceNotice, Identity authorizer,
			Boolean authorize, Identity actingIdentity);
	
	/**
	 * Delete the absence notice and remove the absences from the roll calls.
	 * 
	 * @param absenceNotice Absence notice to delete
	 */
	public void deleteAbsenceNotice(AbsenceNotice absenceNotice, Identity actingIdentity);
	
	public AbsenceNotice updateAbsenceNoticeAttachments(AbsenceNotice absenceNotice, List<VFSItem> newFiles, List<VFSItem> filesToDelete, Identity updatedBy);
	
	public VFSContainer getAbsenceNoticeAttachmentsContainer(AbsenceNotice absenceNotice);
	
	/**
	 * Reload the absence notice.
	 * 
	 * @param notice The absence notice to reload
	 * @return A fresh reloaded absence notice
	 */
	public AbsenceNotice getAbsenceNotice(AbsenceNoticeRef notice);
	
	public AbsenceNotice getAbsenceNotice(IdentityRef identity, LectureBlock lectureBlock);
	
	public List<AbsenceNotice> getAbsenceNoticeRelatedTo(LectureBlock block);

	public List<AbsenceNoticeInfos> searchAbsenceNotices(AbsenceNoticeSearchParameters searchParams);
	
	public List<AbsenceNotice> getAbsenceNoticeUniquelyRelatedTo(List<LectureBlock> blocks);
	
	/**
	 * Detect an absence notice for the specified identity.
	 * 
	 * @param identity The identity which is absent
	 * @param notice A notice to ignore (optional)
	 * @param start The start of the absence (mandatory)
	 * @param end The end of the absence (mandatory)
	 * @return A list of notices which are at the same dates as the specified ones
	 */
	public List<AbsenceNotice> detectCollision(Identity identity, AbsenceNoticeRef notice, Date start, Date end);
	
	/**
	 * @param notices
	 * @return A list of lecture blocks linked to a absence notice
	 */
	public List<LectureBlockWithNotice> getLectureBlocksWithAbsenceNotices(List<AbsenceNotice> notices);
	
	/**
	 * @param notice The notice
	 * @return A list of relations from the absence notice to lecture blocks.
	 */
	public List<AbsenceNoticeToLectureBlock> getAbsenceNoticeToLectureBlocks(AbsenceNotice notice);
	
	/**
	 * @param notice The notice
	 * @return A list of relations from the absence notice to the learn resources
	 */
	public List<AbsenceNoticeToRepositoryEntry> getAbsenceNoticeToRepositoryEntries(AbsenceNotice notice);
	
	/**
	 * Lists the base groups attached to the specified lecture block.
	 * 
	 * @param block A lecture block
	 * @return A list of groups
	 */
	public List<Group> getLectureBlockToGroups(LectureBlockRef block);
	
	
	/**
	 * Lists the base groups attached to the specified lecture block with the specified
	 * type (linked to business groups, curriculum elements...)
	 * 
	 * @param block A lecture block
	 * @return A list of groups
	 */
	public List<Group> getLectureBlockToGroups(LectureBlockRef block, RepositoryEntryRelationType type);
	
	/**
	 * Returns the list of participants of a lecture block.
	 * 
	 * @param block The lecture block
	 * @return A list of identities
	 */
	public List<Identity> getParticipants(LectureBlockRef block);
	
	/**
	 * Return all possible participants in a lecture block linked to the specified
	 * repository.
	 * 
	 * @param entry The course / repository entry
	 * @return A list of identities
	 */
	public List<Identity> getParticipants(RepositoryEntry entry);
	
	/**
	 * Return all possible participants in a lecture block linked to the specified
	 * repository entry and where the specified teacher is linked.
	 * 
	 * @param entry The course / repository entry
	 * @param teacher The teacher
	 * @return A list of identities
	 */
	public List<Identity> getParticipants(RepositoryEntry entry, Identity teacher);
	
	public List<Identity> searchParticipants(LecturesMemberSearchParameters searchParams);
	
	
	/**
	 * The method will start the roll call, generate all roll call, generate missing
	 * summaries...
	 * 
	 * @param teacher
	 * @param lectureblock
	 * @return The list of participants
	 */
	public List<Identity> startLectureBlock(Identity teacher, LectureBlock lectureblock);
	
	/**
	 * Check that every participant of the lecture block has a summary with
	 * the first admission date set.
	 * 
	 * @param lectureBlock The lecture block
	 * @return The list of participant.
	 */
	public List<Identity> syncParticipantSummaries(LectureBlock lectureBlock);
	
	/**
	 * This will sync and update the first admission date if it is before to current one
	 * base on the start date of the lecture block. This will sync the roll call with the
	 * current participants.<br>
	 * This method is meaned for REST API
	 * 
	 * @param lectureBlock The lecture block
	 * @param firstAdmissionDate The admission date to set
	 * @return The list of participant.
	 */
	public List<Identity> syncParticipantSummariesAndRollCalls(LectureBlock lectureBlock, LectureBlockAuditLog.Action action);
	
	
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRef block);
	
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRollCallSearchParameters searchParams);
	
	public List<LectureBlockRollCallAndCoach> getLectureBlockAndRollCalls(LectureBlockRollCallSearchParameters searchParams, String teacherSeaparator);
	
	
	
	/**
	 * Create a roll call with some settings.
	 * 
	 * @param identity The participant of the lecture block
	 * @param lectureBlock The lecture block
	 * @param authorizedAbsence If there are authorized absence
	 * @return A new persisted roll call
	 */
	public LectureBlockRollCall getOrCreateRollCall(Identity identity, LectureBlock lectureBlock,
			Boolean authorizedAbsence, String absenceReason, AbsenceCategory category);
	
	public LectureBlockRollCall getRollCall(LectureBlockRollCallRef rollCall);
	
	/**
	 * Standard merge
	 * 
	 * @param rollCall The lecture block roll call
	 * @return The merge roll call
	 */
	public LectureBlockRollCall updateRollCall(LectureBlockRollCall rollCall);
	
	/**
	 * The method will adapt, trim or add lectures to the roll calls of
	 * the specified lecture block base on the effective lectures or the planned
	 * lectures.
	 * 
	 * @param lectureBlock The lecture block of which roll call need to be adapted
	 */
	public void adaptRollCalls(LectureBlock lectureBlock);
	
	/**
	 * Adapt all roll call on the database. Use with cautions!
	 * 
	 * @param author The user which trigger the action
	 */
	public void adaptAll(Identity author);
	
	/**
	 * Add the specified lectures to the ones the identity follows.
	 * 
	 * @param identity The participant of the lecture
	 * @param lectureBlock The lecture block
	 * @param roolCall The roll call (optional)
	 * @param absences The lectures where the participant was absent
	 * @return The updated roll call
	 */
	public LectureBlockRollCall addRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences);
	
	/**
	 * Add the specified lectures to the ones the identity follows.
	 * 
	 * @param identity The participant of the lecture
	 * @param lectureBlock The lecture block
	 * @param rollCall The roll call (optional)
	 * @param comment A comment
	 * @param absences The lectures where the participant was absent
	 * @return The updated roll call
	 */
	public LectureBlockRollCall addRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall,
			String comment, List<Integer> absences);
	
	/**
	 * Remove the specified lectures to the ones the identity follows. If the number of absences is zero,
	 * the authorized absence flag is set ot NULL.
	 * 
	 * @param identity The participant of the lecture
	 * @param lectureBlock The lecture block
	 * @param rollCall The roll call (optional)
	 * @param absences The lectures to remove from the "absence" list
	 * @return The updated roll call
	 */
	public LectureBlockRollCall removeRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences);
	
	public List<LectureBlock> saveDefaultRollCalls(List<LectureBlock> lectureBlocks, Identity teacher, boolean closeLectures);

	
	/**
	 * Will close all lecture block which end date are after the
	 * setting.
	 * 
	 */
	public void autoCloseRollCall();
	
	public void sendReminders();
	
	
	/**
	 * 
	 * @param entry
	 * @return
	 */
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry);
	
	/**
	 * Search lecture blocks. It returns only lecture blocks with a teacher and
	 * if the repository entry has the lectures enabled.
	 * 
	 * 
	 * @param searchParams The search parameters
	 * @return A list of lecture blocks
	 */
	public List<LectureBlock> getLectureBlocks(LecturesBlockSearchParameters searchParams);
	
	/**
	 * Return the list of lecture blocks of a course with the teachers.
	 * 
	 * @param entry
	 * @return
	 */
	public List<LectureBlockWithTeachers> getLectureBlocksWithTeachers(RepositoryEntryRef entry);
	
	/**
	 * Return the list of lecture blocks of a course with the teachers for a specific teacher.
	 * 
	 * @param entry
	 * @param teacher The teacher to filter with (optional)
	 * @return
	 */
	public List<LectureBlockWithTeachers> getLectureBlocksWithTeachers(LecturesBlockSearchParameters searchParams);

	public List<LectureBlockRef> getAssessedLectureBlocks(LecturesBlockSearchParameters searchParams);
	
	/**
	 * Returns the lecture block for the specified learning resource
	 * and the specified identity which has the "teacher" role for the
	 * block.
	 * 
	 * @param entry A repository entry
	 * @return A list of lecture blocks
	 */
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry, IdentityRef teacher);

	/**
	 * This method will check 2 things. First is the lectures for the specified
	 * repository entry enabled and if it is the case, it will checks that the
	 * identity is a teacher in at least one lecture block.
	 * 
	 * @param entry The course / repository entry
	 * @param identity The identity as teacher
	 * @return true if the lecture is enabled and the identity a teach
	 */
	public boolean hasLecturesAsTeacher(RepositoryEntryRef entry, Identity identity);
	
	/**
	 * Check if the user has a roll call to do. Now.
	 * 
	 * @param identity The teacher
	 * @return true if a roll call need to be done
	 */
	public List<LectureBlock> getRollCallAsTeacher(Identity identity);
	
	/**
	 * Load the teachers of the specified lecture blocks.
	 * 
	 * @param block The lecture block
	 * @return A list of identities
	 */
	public List<Identity> getTeachers(LectureBlock block);
	
	/**
	 * Load the teachers (deduplicated) of the specified lectures blocks.
	 * 
	 * @param blocks The lecture blocks
	 * @return A list of identities
	 */
	public List<Identity> getTeachers(List<LectureBlock> blocks);
	
	public List<Identity> getTeachers(RepositoryEntry entry);
	
	public boolean isMasterCoach(LectureBlock block, IdentityRef identity);

	public List<Identity> getMasterCoaches(IdentityRef participant, List<LectureBlock> blocks, List<RepositoryEntry> entries, Date start, Date end);
	
	/**
	 * Search the teachers 
	 * 
	 * @param participant The mandatory participant
	 * @param blocks An optional list of lecture blocks
	 * @param entries An optional list of repository entries
	 * @param start The mandatory start date
	 * @param end The mandatory end date
	 * @return A list of identities
	 */
	public List<Identity> getTeachers(Identity participant, List<LectureBlock> blocks, List<RepositoryEntry> entries, Date start, Date end);
	
	public List<Identity> searchTeachers(LecturesMemberSearchParameters searchParams);
	
	public void addTeacher(LectureBlock block, Identity teacher);
	
	public void removeTeacher(LectureBlock block, Identity teacher);
	
	public List<TaxonomyLevel> getTaxonomy(LectureBlockRef lectureBlock);
	
	/**
	 * 
	 * @param level The taxonomy level to search for
	 * @return A list of lecture blocks with this level
	 */
	public List<LectureBlock> getLectureBlocks(TaxonomyLevelRef level);
	
	/**
	 * This is an administrative tool which look at all lecture blocks
	 * regardless of the permissions.
	 * 
	 * @param from
	 * @param to
	 * @param status
	 * @return A list of lectures with some additional informations
	 */
	public List<LectureReportRow> getLectureBlocksReport(Date from, Date to, List<LectureRollCallStatus> status);
	
	/**
	 * The method will not set the date of admission.
	 * 
	 * @param entry
	 * @param identity
	 * @return
	 */
	public LectureParticipantSummary getOrCreateParticipantSummary(RepositoryEntry entry, Identity identity);
	
	
	public LectureParticipantSummary saveParticipantSummary(LectureParticipantSummary summary);
	
	public void recalculateSummary(RepositoryEntry entry);
	
	public void recalculateSummary(RepositoryEntry entry, Identity identity);
	

	/**
	 * Returns statistics centered around the lecture block.
	 * 
	 * @param teacher The teacher
	 * @param searchParams
	 * @return
	 */
	public List<LectureBlockBlockStatistics> getLectureBlocksStatistics(LecturesBlockSearchParameters searchParams);
	
	
	public List<LectureBlockIdentityStatistics> groupByIdentity(List<LectureBlockIdentityStatistics> statistics);
	
	/**
	 * The method calculate warnings on a user base. If a user as a course with a warning,
	 * the method return the warning for it. It's a "max" grouping, not an average one.
	 * 
	 * @param statistics
	 * @return
	 */
	public List<IdentityRateWarning> groupRateWarning(List<LectureBlockIdentityStatistics> statistics);
	
	/**
	 * Returns the statistics for the specified participant.
	 * 
	 * @param identity The participant
	 * @return A list of statistics
	 */
	public List<LectureBlockStatistics> getParticipantLecturesStatistics(IdentityRef identity);
	
	/**
	 * Return all the statistics for a course / repository entry.
	 * 
	 * @param entry The course / repository entry
	 * @return Statistics per user
	 */
	public List<LectureBlockStatistics> getParticipantsLecturesStatistics(RepositoryEntry entry);
	
	/**
	 * Aggregated the statistics with business rules based on the different possible settings.
	 * 
	 * @param statistics
	 * @return
	 */
	public AggregatedLectureBlocksStatistics aggregatedStatistics(List<? extends LectureBlockStatistics> statistics);
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	public List<LectureBlockIdentityStatistics> getLecturesStatistics(LectureStatisticsSearchParameters params,
			List<UserPropertyHandler> userPropertyHandlers, Identity identity);
	
	/**
	 * The list of roll calls within the specified course for the specified user
	 * after it's first admission.
	 * 
	 * @param entry
	 * @param participant
	 * @param teacherSeparator The separator between the name of 2 teachers
	 * @return
	 */
	public List<LectureBlockAndRollCall> getParticipantLectureBlocks(RepositoryEntryRef entry, IdentityRef participant, String teacherSeparator);

	
	/**
	 * The method doesn't check the configuration.
	 * 
	 * @param lectureBlock
	 */
	public void syncCalendars(LectureBlock lectureBlock);
	
	/**
	 * Sync the participants and teachers calendars of the specified
	 * entry with the configuration saved in the entry.
	 * 
	 * @param entry
	 */
	public void syncCalendars(RepositoryEntry entry);
	
	
	public List<LectureRepositoryEntryInfos> searchRepositoryEntries(LectureRepositoryEntrySearchParameters searchParams);
	
	public List<LectureCurriculumElementInfos> searchCurriculumElements(LectureCurriculumElementSearchParameters searchParams);
	
}
