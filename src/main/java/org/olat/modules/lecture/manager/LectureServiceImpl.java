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
package org.olat.modules.lecture.manager;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.group.BusinessGroup;
import org.olat.group.DeletableGroupData;
import org.olat.modules.coach.model.IdentityRepositoryEntryKey;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeRef;
import org.olat.modules.lecture.AbsenceNoticeSearchParameters;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeToLectureBlock;
import org.olat.modules.lecture.AbsenceNoticeToRepositoryEntry;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockAuditLog.Action;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockRollCallRef;
import org.olat.modules.lecture.LectureBlockRollCallSearchParameters;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureParticipantSummary;
import org.olat.modules.lecture.LectureRateWarning;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.Reason;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.AbsenceNoticeImpl;
import org.olat.modules.lecture.model.AbsenceNoticeInfos;
import org.olat.modules.lecture.model.AbsenceNoticeRelationsAuditImpl;
import org.olat.modules.lecture.model.AggregatedLectureBlocksStatistics;
import org.olat.modules.lecture.model.IdentityRateWarning;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.model.LectureBlockBlockStatistics;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockRollCallAndCoach;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.lecture.model.LectureBlockToTeacher;
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
import org.olat.modules.lecture.model.ParticipantAndLectureSummary;
import org.olat.modules.lecture.ui.ConfigurationHelper;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureServiceImpl implements LectureService, UserDataDeletable, DeletableGroupData {
	private static final Logger log = Tracing.createLoggerFor(LectureServiceImpl.class);
	private static final CalendarManagedFlag[] CAL_MANAGED_FLAGS = new CalendarManagedFlag[] { CalendarManagedFlag.all };

	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private ReasonDAO reasonDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CalendarManager calendarMgr;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private AbsenceNoticeDAO absenceNoticeDao;
	@Autowired
	private LectureBlockAuditLogDAO auditLogDao;
	@Autowired
	private AbsenceCategoryDAO absenceCategoryDao;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private LectureBlockToGroupDAO lectureBlockToGroupDao;
	@Autowired
	private LectureBlockRollCallDAO lectureBlockRollCallDao;
	@Autowired
	private LectureBlockReminderDAO lectureBlockReminderDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private LectureParticipantSummaryDAO lectureParticipantSummaryDao;
	@Autowired
	private AbsenceNoticeToLectureBlockDAO absenceNoticeToLectureBlockDao;
	@Autowired
	private LectureBlockToTaxonomyLevelDAO lectureBlockToTaxonomyLevelDao;
	@Autowired
	private RepositoryEntryLectureConfigurationDAO lectureConfigurationDao;
	@Autowired
	private AbsenceNoticeToRepositoryEntryDAO absenceNoticeToRepositoryEntryDao;
	
	
	@Override
	public RepositoryEntryLectureConfiguration getRepositoryEntryLectureConfiguration(RepositoryEntry entry) {
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.getConfiguration(entry);
		if(config == null) {
			RepositoryEntry reloadedEntry = repositoryEntryDao.loadForUpdate(entry);
			config = lectureConfigurationDao.getConfiguration(entry);
			if(config == null) {
				config = lectureConfigurationDao.createConfiguration(reloadedEntry);
			}
			dbInstance.commit();
		}
		return config;
	}
	
	@Override
	public boolean isRepositoryEntryLectureEnabled(RepositoryEntryRef entry) {
		if(!lectureModule.isEnabled()) {
			return false;
		}
		return lectureConfigurationDao.isConfigurationEnabledFor(entry);
	}

	@Override
	public RepositoryEntryLectureConfiguration copyRepositoryEntryLectureConfiguration(RepositoryEntry sourceEntry, RepositoryEntry targetEntry) {
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.getConfiguration(sourceEntry);
		if(config != null) {
			config = lectureConfigurationDao.cloneConfiguration(config, targetEntry);
		}
		return config;
	}

	@Override
	public RepositoryEntryLectureConfiguration updateRepositoryEntryLectureConfiguration(RepositoryEntryLectureConfiguration config) {
		return lectureConfigurationDao.update(config);
	}

	@Override
	public LectureBlock createLectureBlock(RepositoryEntry entry) {
		return lectureBlockDao.createLectureBlock(entry);
	}

	@Override
	public LectureBlock save(LectureBlock lectureBlock, List<Group> groups) {
		LectureBlockImpl block = (LectureBlockImpl)lectureBlockDao.update(lectureBlock);
		if(groups != null) {
			List<LectureBlockToGroup> lectureToGroups = lectureBlockToGroupDao.getLectureBlockToGroups(block);
			for(Group group:groups) {
				boolean found = false;
				for(LectureBlockToGroup lectureToGroup:lectureToGroups) {
					if(lectureToGroup.getGroup().equals(group)) {
						found = true;
						break;
					}
				}
				
				if(!found) {
					LectureBlockToGroup blockToGroup = lectureBlockToGroupDao.createAndPersist(block, group);
					lectureToGroups.add(blockToGroup);
				}
			}
			
			for(Iterator<LectureBlockToGroup> lectureToGroupIt=lectureToGroups.iterator(); lectureToGroupIt.hasNext(); ) {
				LectureBlockToGroup lectureBlockToGroup = lectureToGroupIt.next();
				if(!groups.contains(lectureBlockToGroup.getGroup())) {
					lectureBlockToGroupDao.remove(lectureBlockToGroup);
				}
			}
		}
		block.getTeacherGroup().getKey();
		return block;
	}
	
	@Override
	public LectureBlock close(LectureBlock lectureBlock, Identity author) {
		lectureBlock.setStatus(LectureBlockStatus.done);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.closed);
		LectureBlockImpl block = (LectureBlockImpl)lectureBlockDao.update(lectureBlock);
		
		int numOfLectures = block.getCalculatedLecturesNumber();

		List<LectureBlockRollCall> rollCallList = lectureBlockRollCallDao.getRollCalls(lectureBlock);
		for(LectureBlockRollCall rollCall:rollCallList) {
			lectureBlockRollCallDao.adaptLecture(block, rollCall, numOfLectures, author);
		}
		dbInstance.commit();
		recalculateSummary(block.getEntry());
		return block;
	}

	@Override
	public LectureBlock cancel(LectureBlock lectureBlock) {
		lectureBlock.setStatus(LectureBlockStatus.cancelled);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.closed);
		lectureBlock.setEffectiveLecturesNumber(0);
		LectureBlockImpl block = (LectureBlockImpl)lectureBlockDao.update(lectureBlock);
		dbInstance.commit();
		recalculateSummary(block.getEntry());
		return block;
	}

	@Override
	public String toAuditXml(LectureBlock lectureBlock) {
		return auditLogDao.toXml(lectureBlock);
	}
	
	@Override
	public String toAuditXml(LectureBlockRollCall rollCall) {
		return auditLogDao.toXml(rollCall);
	}
	
	@Override
	public LectureBlock toAuditLectureBlock(String xml) {
		return auditLogDao.lectureBlockFromXml(xml);
	}

	@Override
	public LectureBlockRollCall toAuditLectureBlockRollCall(String xml) {
		return auditLogDao.rollCallFromXml(xml);
	}

	@Override
	public String toAuditXml(LectureParticipantSummary summary) {
		return auditLogDao.toXml(summary);
	}

	@Override
	public LectureParticipantSummary toAuditLectureParticipantSummary(String xml) {
		return auditLogDao.summaryFromXml(xml);
	}

	@Override
	public String toAuditXml(AbsenceNotice absenceNotice) {
		return auditLogDao.toXml(absenceNotice);
	}

	@Override
	public AbsenceNotice toAuditAbsenceNotice(String xml) {
		return auditLogDao.absenceNoticeFromXml(xml);
	}

	@Override
	public void auditLog(LectureBlockAuditLog.Action action, String before, String after, String message,
			LectureBlockRef lectureBlock, LectureBlockRollCall rollCall,
			RepositoryEntryRef entry, IdentityRef assessedIdentity, IdentityRef author) {
		auditLogDao.auditLog(action, before, after, message, lectureBlock, rollCall, entry, assessedIdentity, author);
	}
	
	@Override
	public void auditLog(LectureBlockAuditLog.Action action, String before, String after, String message,
			AbsenceNoticeRef absenceNotice, IdentityRef assessedIdentity, IdentityRef author) {
		auditLogDao.auditLog(action, before, after, message, absenceNotice, assessedIdentity, author);
	}

	@Override
	public List<LectureBlockAuditLog> getAuditLog(LectureBlockRef lectureBlock) {
		return auditLogDao.getAuditLog(lectureBlock);
	}

	@Override
	public List<LectureBlockAuditLog> getAuditLog(IdentityRef assessedIdentity) {
		return auditLogDao.getAuditLog(assessedIdentity);
	}
	@Override
	public List<LectureBlockAuditLog> getAuditLog(RepositoryEntryRef entry, IdentityRef assessedIdentity, Action action) {
		return auditLogDao.getAuditLog(entry, assessedIdentity, action);
	}

	@Override
	public List<LectureBlockAuditLog> getAuditLog(RepositoryEntryRef entry) {
		return auditLogDao.getAuditLog(entry);
	}

	@Override
	public LectureBlock moveLectureBlock(LectureBlockRef block, RepositoryEntry newEntry) {
		LectureBlockImpl blockToMove = (LectureBlockImpl)lectureBlockDao.loadByKey(block.getKey());
		blockToMove.setEntry(newEntry);
		LectureBlock mergedBlock = lectureBlockDao.update(blockToMove);
		dbInstance.commit();
		recalculateSummary(mergedBlock.getEntry());
		return mergedBlock;
	}

	public int healMovedLectureBlocks(RepositoryEntry entry, RepositoryEntry originEntry) {
		int rows = 0;
		// move log if necessary
		List<LectureBlockAuditLog> auditLogs = getAuditLog(originEntry);
		if(!auditLogs.isEmpty()) {
			List<LectureBlock> blocks = getLectureBlocks(entry);
			Set<Long> blockKeys = blocks.stream().map(LectureBlock::getKey).collect(Collectors.toSet());
			for(LectureBlockAuditLog auditLog:auditLogs) {
				if(auditLog.getLectureBlockKey() != null && blockKeys.contains(auditLog.getLectureBlockKey())) {
					rows += auditLogDao.moveAudit(auditLog.getLectureBlockKey(), originEntry.getKey(), entry.getKey());
					blockKeys.remove(auditLog.getLectureBlockKey());// move all log of the lecture block
					dbInstance.commit();
				}
			}
		}
		
		//check summary
		List<LectureParticipantSummary> originSummaries = lectureParticipantSummaryDao.getSummary(originEntry);
		if(!originSummaries.isEmpty()) {	
			Map<Identity, LectureParticipantSummary> originSummariesMap = new HashMap<>();
			for(LectureParticipantSummary originSummary:originSummaries) {
				originSummariesMap.put(originSummary.getIdentity(), originSummary);
			}
		
			List<LectureParticipantSummary> summaries = lectureParticipantSummaryDao.getSummary(entry);
			for(LectureParticipantSummary summary:summaries) {
				LectureParticipantSummary originSummary = originSummariesMap.get(summary.getIdentity());
				if(originSummary != null && originSummary.getFirstAdmissionDate() != null && summary.getFirstAdmissionDate() != null
						&& originSummary.getFirstAdmissionDate().before(summary.getFirstAdmissionDate())) {
					summary.setFirstAdmissionDate(originSummary.getFirstAdmissionDate());
					lectureParticipantSummaryDao.update(summary);
					rows++;
				}
			}
		}
		dbInstance.commit();
		return rows;
	}

	@Override
	public LectureBlock copyLectureBlock(String newTitle, LectureBlock block) {
		LectureBlock copy = lectureBlockDao.createLectureBlock(block.getEntry());
		copy.setTitle(newTitle);
		copy.setDescription(block.getDescription());
		copy.setPreparation(block.getPreparation());
		copy.setLocation(block.getLocation());
		copy.setRollCallStatus(LectureRollCallStatus.open);
		copy.setEffectiveLecturesNumber(block.getEffectiveLecturesNumber());
		copy.setPlannedLecturesNumber(block.getPlannedLecturesNumber());
		copy.setStartDate(block.getStartDate());
		copy.setEndDate(block.getEndDate());
		copy = lectureBlockDao.update(copy);
		return copy;
	}

	@Override
	public void deleteLectureBlock(LectureBlock lectureBlock, Identity actingIdentity) {
		//first remove events
		LectureBlock reloadedBlock = lectureBlockDao.loadByKey(lectureBlock.getKey());
		RepositoryEntry entry = reloadedBlock.getEntry();
		RepositoryEntryLectureConfiguration config = getRepositoryEntryLectureConfiguration(entry);
		if(ConfigurationHelper.isSyncCourseCalendarEnabled(config, lectureModule)) {
			unsyncCourseCalendar(lectureBlock, entry);
		}
		if(ConfigurationHelper.isSyncTeacherCalendarEnabled(config, lectureModule)) {
			List<Identity> teachers = getTeachers(reloadedBlock);
			unsyncInternalCalendar(reloadedBlock, teachers);
		}
		
		List<AbsenceNotice> absenceNotices = getAbsenceNoticeUniquelyRelatedTo(Collections.singletonList(lectureBlock));
		for(AbsenceNotice absenceNotice:absenceNotices) {
			deleteAbsenceNotice(absenceNotice, actingIdentity);
		}
		absenceNoticeToLectureBlockDao.deleteRelations(reloadedBlock);
		lectureBlockDao.delete(reloadedBlock);
		dbInstance.commit();// make it quick
	}

	@Override
	public List<AbsenceNotice> getAbsenceNoticeUniquelyRelatedTo(List<LectureBlock> blocks) {
		List<AbsenceNoticeToLectureBlock> relations = absenceNoticeToLectureBlockDao.getRelationsAStepFurther(blocks);

		Set<Long> lectureBlockKeys = blocks.stream()
				.map(LectureBlock::getKey)
				.collect(Collectors.toSet());
		
		Map<AbsenceNotice,AtomicInteger> counters = new HashMap<>();
		for(AbsenceNoticeToLectureBlock relation:relations) {
			AbsenceNotice notice = relation.getAbsenceNotice();
			LectureBlock lectureBlock = relation.getLectureBlock();
			
			AtomicInteger counter = counters
					.computeIfAbsent(notice, n -> new AtomicInteger(0));
			if(!lectureBlockKeys.contains(lectureBlock.getKey())) {
				counter.incrementAndGet();
			}
		}
		
		List<AbsenceNotice> uniquelyRelated = new ArrayList<>();
		for(Map.Entry<AbsenceNotice,AtomicInteger> counterEntry:counters.entrySet()) {
			int count = counterEntry.getValue().intValue();
			if(count == 0) {
				uniquelyRelated.add(counterEntry.getKey());
			}
		}
		return uniquelyRelated;
	}

	@Override
	public int delete(RepositoryEntry entry) {
		int rows = 0;
		List<LectureBlock> blocksToDelete = lectureBlockDao.getLectureBlocks(entry);
		for(LectureBlock blockToDelete:blocksToDelete) {
			rows += lectureBlockDao.delete(blockToDelete);
		}
		rows += lectureConfigurationDao.deleteConfiguration(entry);
		rows += lectureParticipantSummaryDao.deleteSummaries(entry);
		return rows;
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		lectureParticipantSummaryDao.deleteSummaries(identity);
		lectureBlockRollCallDao.deleteRollCalls(identity);
		lectureBlockReminderDao.deleteReminders(identity);
	}

	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		lectureBlockToGroupDao.deleteLectureBlockToGroup(group.getBaseGroup());
		return true;
	}

	@Override
	public LectureBlock getLectureBlock(LectureBlockRef block) {
		return lectureBlockDao.loadByKey(block.getKey());
	}

	@Override
	public List<LectureBlock> getLectureBlocks(List<Long> keys) {
		return lectureBlockDao.loadByKeys(keys);
	}

	@Override
	public List<Reason> getAllReasons() {
		return reasonDao.getReasons();
	}

	@Override
	public Reason getReason(Long key) {
		return reasonDao.loadReason(key);
	}	

	@Override
	public boolean isReasonInUse(Reason reason) {
		return reasonDao.isReasonInUse(reason);
	}

	@Override
	public boolean deleteReason(Reason reason) {
		return reasonDao.delete(reason);
	}

	@Override
	public Reason createReason(String title, String description, boolean enabled) {
		return reasonDao.createReason(title, description, enabled);
	}

	@Override
	public Reason updateReason(Reason reason) {
		return reasonDao.updateReason(reason);
	}

	@Override
	public List<AbsenceCategory> getAbsencesCategories(Boolean enabled) {
		return absenceCategoryDao.getAbsencesCategories(enabled);
	}

	@Override
	public AbsenceCategory getAbsenceCategory(Long key) {
		return absenceCategoryDao.getAbsenceCategory(key);
	}

	@Override
	public AbsenceCategory createAbsenceCategory(String title, String description, boolean enabled) {
		return absenceCategoryDao.createAbsenceCategory(title, description, enabled);
	}

	@Override
	public AbsenceCategory updateAbsenceCategory(AbsenceCategory category) {
		return absenceCategoryDao.updateAbsenceCategory(category);
	}

	@Override
	public boolean isAbsenceCategoryInUse(AbsenceCategory category) {
		return absenceCategoryDao.isAbsenceCategoryInUse(category);
	}

	@Override
	public void deleteAbsenceCategory(AbsenceCategory category) {
		absenceCategoryDao.deleteAbsenceCategory(category);
	}
	
	@Override
	public AbsenceNotice createAbsenceNotice(Identity absentIdentity, AbsenceNoticeType type, AbsenceNoticeTarget target, Date start, Date end,
			AbsenceCategory category, String absenceRason, Boolean authorized, List<RepositoryEntry> entries, List<LectureBlock> lectureBlocks,
			Identity actingIdentity) {
		if(type == AbsenceNoticeType.dispensation) {
			authorized = Boolean.TRUE;
		}
		Identity authorizer = null;
		if(authorized != null && authorized.booleanValue()) {
			authorizer = actingIdentity;
		}

		AbsenceNoticeRelationsAuditImpl after = new AbsenceNoticeRelationsAuditImpl();
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(absentIdentity, type, target, start, end,
				category, absenceRason, authorized, authorizer, actingIdentity);
		if(entries != null && !entries.isEmpty()) {
			List<AbsenceNoticeToRepositoryEntry> relations = new ArrayList<>();
			for(RepositoryEntry entry:entries) {
				AbsenceNoticeToRepositoryEntry noticeToEntry = absenceNoticeToRepositoryEntryDao.createRelation(notice, entry);
				relations.add(noticeToEntry);
			}
			after.setNoticeToEntries(relations);
		}
		if(lectureBlocks != null && !lectureBlocks.isEmpty()) {
			List<AbsenceNoticeToLectureBlock> relations = new ArrayList<>();
			for(LectureBlock lectureBlock:lectureBlocks) {
				AbsenceNoticeToLectureBlock noticeToBlock = absenceNoticeToLectureBlockDao.createRelation(notice, lectureBlock);
				relations.add(noticeToBlock);
			}
			after.setNoticeToBlocks(relations);
		}
		dbInstance.commit();
		
		// calculate roll calls
		calculateAbsencesOfRollcall(notice, null, after);
		dbInstance.commit();

		String afterXml = auditLogDao.toXml(after);
		auditLog(Action.createAbsenceNoticeRelations, null, afterXml, null, notice, absentIdentity, actingIdentity);
		return notice;
	}

	@Override
	public void deleteAbsenceNotice(AbsenceNotice absenceNotice, Identity actingIdentity) {
		AbsenceNotice notice = absenceNoticeDao.loadAbsenceNotice(absenceNotice.getKey());
		if(notice == null) return; // nothing to do
		
		String beforeNotice = toAuditXml(absenceNotice);
		Identity assessedIdentity = notice.getIdentity();
		AbsenceNoticeRelationsAuditImpl beforeRelations = new AbsenceNoticeRelationsAuditImpl();
		
		List<AbsenceNoticeToLectureBlock> noticeToBlocks = absenceNoticeToLectureBlockDao.getRelations(absenceNotice);
		if(noticeToBlocks != null && !noticeToBlocks.isEmpty()) {
			beforeRelations.setNoticeToBlocks(noticeToBlocks);
			absenceNoticeToLectureBlockDao.deleteRelations(noticeToBlocks);
		}
		List<AbsenceNoticeToRepositoryEntry> noticeToEntries = absenceNoticeToRepositoryEntryDao.getRelations(absenceNotice);
		if(noticeToEntries != null && !noticeToEntries.isEmpty()) {
			beforeRelations.setNoticeToEntries(noticeToEntries);
			absenceNoticeToRepositoryEntryDao.deleteRelations(noticeToEntries);
		}
		
		List<LectureBlockRollCall> rollCalls = absenceNoticeDao.getRollCalls(notice);
		if(rollCalls != null && !rollCalls.isEmpty()) {
			for(LectureBlockRollCall rollCall:rollCalls) {
				lectureBlockRollCallDao.removeLectureBlockRollCallAbsenceNotice(rollCall);
			}
		}
		// delete
		absenceNoticeDao.deleteAbsenceNotice(notice);
		dbInstance.commit();

		auditLog(Action.deleteAbsenceNotice, beforeNotice, null, null, absenceNotice, assessedIdentity, actingIdentity);
		if((noticeToBlocks != null && !noticeToBlocks.isEmpty()) || (noticeToEntries != null && !noticeToEntries.isEmpty())) {
			String beforeXml = auditLogDao.toXml(beforeRelations);
			auditLog(Action.deleteAbsenceNotice, beforeXml, null, null, absenceNotice, assessedIdentity, actingIdentity);
		}
		dbInstance.commit();
	}

	@Override
	public AbsenceNotice updateAbsenceNotice(AbsenceNotice absenceNotice, Identity authorizer,
			List<RepositoryEntry> entries, List<LectureBlock> lectureBlocks, Identity actingIdentity) {
		
		if(authorizer != null && absenceNotice.getAuthorizer() == null) {
			((AbsenceNoticeImpl)absenceNotice).setAuthorizer(authorizer);
		}
		AbsenceNotice notice = absenceNoticeDao.updateAbsenceNotice(absenceNotice);
		List<AbsenceNoticeToLectureBlock> currentNoticeToBlocks = absenceNoticeToLectureBlockDao.getRelations(absenceNotice);
		List<AbsenceNoticeToRepositoryEntry> currentNoticeToEntries = absenceNoticeToRepositoryEntryDao.getRelations(absenceNotice);
		
		AbsenceNoticeRelationsAuditImpl before = new AbsenceNoticeRelationsAuditImpl();
		before.setNoticeToBlocks(currentNoticeToBlocks);
		before.setNoticeToEntries(currentNoticeToEntries);
		
		AbsenceNoticeRelationsAuditImpl after = new AbsenceNoticeRelationsAuditImpl();
		
		if(notice.getNoticeTarget() == AbsenceNoticeTarget.allentries) {
			absenceNoticeToLectureBlockDao.deleteRelations(currentNoticeToBlocks);
			absenceNoticeToRepositoryEntryDao.deleteRelations(currentNoticeToEntries);
		} else if(notice.getNoticeTarget() == AbsenceNoticeTarget.entries) {
			absenceNoticeToLectureBlockDao.deleteRelations(currentNoticeToBlocks);
			if(entries != null) {
				List<RepositoryEntry> currentEntries = new ArrayList<>(currentNoticeToEntries.size());
				List<AbsenceNoticeToRepositoryEntry> relations = new ArrayList<>(currentNoticeToEntries);
				for(AbsenceNoticeToRepositoryEntry currentNoticeToEntry:currentNoticeToEntries) {
					currentEntries.add(currentNoticeToEntry.getEntry());
					if(entries.contains(currentNoticeToEntry.getEntry())) {
						relations.add(currentNoticeToEntry);
					} else {
						absenceNoticeToRepositoryEntryDao.deleteRelation(currentNoticeToEntry);
					}
				}
	
				for(RepositoryEntry entry:entries) {
					if(!currentEntries.contains(entry)) {
						AbsenceNoticeToRepositoryEntry noticeToEntry = absenceNoticeToRepositoryEntryDao.createRelation(notice, entry);
						relations.add(noticeToEntry);
					}
				}
				after.setNoticeToEntries(relations);
			}
			
		} else if(notice.getNoticeTarget() == AbsenceNoticeTarget.lectureblocks) {
			absenceNoticeToRepositoryEntryDao.deleteRelations(currentNoticeToEntries);
			if(lectureBlocks != null) {
				List<LectureBlock> currentBlocks = new ArrayList<>();
				List<AbsenceNoticeToLectureBlock> relations = new ArrayList<>();
				for(AbsenceNoticeToLectureBlock currentNoticeToBlock:currentNoticeToBlocks) {
					currentBlocks.add(currentNoticeToBlock.getLectureBlock());
					if(lectureBlocks.contains(currentNoticeToBlock.getLectureBlock())) {
						relations.add(currentNoticeToBlock);
					} else {
						absenceNoticeToLectureBlockDao.deleteRelation(currentNoticeToBlock);
					}
				}

				for(LectureBlock lectureBlock:lectureBlocks) {
					if(!currentBlocks.contains(lectureBlock)) {
						AbsenceNoticeToLectureBlock noticeToBlock = absenceNoticeToLectureBlockDao.createRelation(notice, lectureBlock);
						relations.add(noticeToBlock);
					}
				}
				after.setNoticeToBlocks(relations);
			}
		}

		dbInstance.commit();
		// calculate roll calls
		calculateAbsencesOfRollcall(notice, before, after);
		dbInstance.commit();
		
		String beforeXml = auditLogDao.toXml(before);
		String afterXml = auditLogDao.toXml(after);
		auditLog(Action.updateAbsenceNoticeRelations, beforeXml, afterXml, null, absenceNotice, absenceNotice.getIdentity(), actingIdentity);
		return notice;
	}

	private void calculateAbsencesOfRollcall(AbsenceNotice notice, AbsenceNoticeRelationsAuditImpl before, AbsenceNoticeRelationsAuditImpl after) {
		List<LectureBlockRollCall> currentRollCalls = absenceNoticeDao.getRollCalls(notice);
		if(before != null) {
			before.setRollCalls(currentRollCalls);
		}
		Set<LectureBlockRollCall> currentRollCallSet = new HashSet<>(currentRollCalls);
		
		IdentityRef identity = notice.getIdentity();
		Date start = notice.getStartDate();
		Date end = notice.getEndDate();
		
		List<LectureBlockRollCall> rollCalls;
		switch(notice.getNoticeTarget()) {
			case lectureblocks: rollCalls = absenceNoticeToLectureBlockDao.searchRollCallsByLectureBlock(notice); break;
			case entries: rollCalls = absenceNoticeToRepositoryEntryDao.searchRollCallsByRepositoryEntry(notice); break;
			case allentries: rollCalls = absenceNoticeToRepositoryEntryDao.searchRollCallsOfAllEntries(identity, start, end); break;
			default: rollCalls = Collections.emptyList();
		}
		
		for(LectureBlockRollCall rollCall:rollCalls) {
			if(!rollCall.getIdentity().equals(notice.getIdentity())) {
				continue;// this is a paranoia check, the search methods already do this
			}
			if(currentRollCallSet.contains(rollCall)) {
				currentRollCallSet.remove(rollCall);
			} else {
				lectureBlockRollCallDao.updateLectureBlockRollCallAbsenceNotice(rollCall, notice);
			}
		}
		
		for(LectureBlockRollCall toUnlink: currentRollCallSet) {
			lectureBlockRollCallDao.removeLectureBlockRollCallAbsenceNotice(toUnlink);
		}
		
		if(after != null) {
			after.setRollCalls(rollCalls);
		}
	}

	@Override
	public AbsenceNotice updateAbsenceNoticeAuthorization(AbsenceNotice absenceNotice, Identity authorizer,
			Boolean authorize, Identity actingIdentity) {
		absenceNotice = absenceNoticeDao.loadAbsenceNotice(absenceNotice.getKey());
		if(absenceNotice != null) {
			String beforeNotice = toAuditXml(absenceNotice);
			
			absenceNotice.setAbsenceAuthorized(authorize);
			((AbsenceNoticeImpl)absenceNotice).setAuthorizer(authorizer);
			absenceNotice = absenceNoticeDao.updateAbsenceNotice(absenceNotice);
			dbInstance.commit();

			String afterNotice = toAuditXml(absenceNotice);
			auditLog(Action.updateAbsenceNotice, beforeNotice, afterNotice, null, absenceNotice, absenceNotice.getIdentity(), actingIdentity);
		}
		return absenceNotice;
	}

	@Override
	public AbsenceNotice updateAbsenceNoticeAttachments(AbsenceNotice absenceNotice, List<VFSItem> newFiles,
			List<VFSItem> filesToDelete, Identity updatedBy) {
		if(!filesToDelete.isEmpty()) {
			for(VFSItem file:filesToDelete) {
				file.delete();
			}
		}
	
		if(!newFiles.isEmpty()) {
			LocalFolderImpl rootContainer = getAbsenceNoticesAttachmentsPath();
			File rootDir = rootContainer.getBasefile();
			String noticeStorage = ((AbsenceNoticeImpl)absenceNotice).getAttachmentsDirectory();
			if(noticeStorage == null) {	
				File userDir = new File(rootDir, absenceNotice.getIdentity().getKey().toString());
				File noticeDir = new File(userDir, absenceNotice.getKey().toString());
				noticeDir.mkdirs();
				
				Path relativePath = rootDir.toPath().relativize(noticeDir.toPath());
				noticeStorage = relativePath.toString();
				((AbsenceNoticeImpl)absenceNotice).setAttachmentsDirectory(noticeStorage);
				absenceNotice = absenceNoticeDao.updateAbsenceNotice(absenceNotice);
				dbInstance.commit();
			}
			
			VFSItem noticeItem = rootContainer.resolve(noticeStorage);
			if(noticeItem instanceof VFSContainer) {
				VFSContainer noticeContainer = (VFSContainer)noticeItem;
				for(VFSItem file:newFiles) {
					noticeContainer.copyFrom(file, updatedBy);
				}
			}
		}
		
		return absenceNotice;
	}

	
    @Override
	public VFSContainer getAbsenceNoticeAttachmentsContainer(AbsenceNotice absenceNotice) {
    	if(absenceNotice == null || absenceNotice.getKey() == null) return null;

		LocalFolderImpl rootContainer = getAbsenceNoticesAttachmentsPath();
		VFSItem userItem = rootContainer.resolve(absenceNotice.getIdentity().getKey().toString());
    	if(userItem instanceof VFSContainer) {
    		VFSContainer userContainer = (VFSContainer)userItem;
    		VFSItem noticeItem = userContainer.resolve(absenceNotice.getKey().toString());
    		if(noticeItem instanceof VFSContainer) {
    			return (VFSContainer)noticeItem;
    		}
    	}
		return null;
	}

	private LocalFolderImpl getAbsenceNoticesAttachmentsPath() {
    	return VFSManager.olatRootContainer("/lectures/notices/", null);
	}

	@Override
	public AbsenceNotice getAbsenceNotice(AbsenceNoticeRef notice) {
		return absenceNoticeDao.loadAbsenceNotice(notice.getKey());
	}

	@Override
	public List<AbsenceNotice> getAbsenceNoticeRelatedTo(LectureBlock block) {
		return absenceNoticeDao.getAbsenceNotices(null, block);
	}

	@Override
	public List<AbsenceNoticeInfos> searchAbsenceNotices(AbsenceNoticeSearchParameters searchParams) {
		boolean absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		return absenceNoticeDao.search(searchParams, absenceDefaultAuthorized);
	}

	@Override
	public List<AbsenceNotice> detectCollision(Identity identity, AbsenceNoticeRef notice, Date start, Date end) {
		return absenceNoticeDao.detectCollision(identity, notice, start, end);
	}

	@Override
	public List<LectureBlockWithNotice> getLectureBlocksWithAbsenceNotices(List<AbsenceNotice> notices) {
		List<AbsenceNotice> noticeWithLectureBlocks = notices.stream()
				.filter(n -> n.getNoticeTarget() == AbsenceNoticeTarget.lectureblocks)
				.collect(Collectors.toList());
		List<LectureBlockWithNotice> blocksWithLectures = absenceNoticeDao.loadLectureBlocksOf(noticeWithLectureBlocks, AbsenceNoticeTarget.lectureblocks);

		List<AbsenceNotice> noticeWithEntries = notices.stream()
				.filter(n -> n.getNoticeTarget() == AbsenceNoticeTarget.entries)
				.collect(Collectors.toList());
		List<LectureBlockWithNotice> blocksWithEntries = absenceNoticeDao.loadLectureBlocksOf(noticeWithEntries, AbsenceNoticeTarget.entries);

		List<AbsenceNotice> noticeWithAllEntries = notices.stream()
				.filter(n -> n.getNoticeTarget() == AbsenceNoticeTarget.allentries)
				.collect(Collectors.toList());
		List<LectureBlockWithNotice> blocksWithAllEntries = absenceNoticeDao.loadLectureBlocksOf(noticeWithAllEntries, AbsenceNoticeTarget.allentries);

		List<LectureBlockWithNotice> lectureBlocks = new ArrayList<>(blocksWithLectures.size() + blocksWithEntries.size() + blocksWithAllEntries.size());
		lectureBlocks.addAll(blocksWithLectures);
		lectureBlocks.addAll(blocksWithEntries);
		lectureBlocks.addAll(blocksWithAllEntries);
		return lectureBlocks;
	}

	@Override
	public List<AbsenceNoticeToLectureBlock> getAbsenceNoticeToLectureBlocks(AbsenceNotice notice) {
		return absenceNoticeToLectureBlockDao.getRelations(notice);
	}

	@Override
	public List<AbsenceNoticeToRepositoryEntry> getAbsenceNoticeToRepositoryEntries(AbsenceNotice notice) {
		return absenceNoticeToRepositoryEntryDao.getRelations(notice);
	}

	@Override
	public List<Group> getLectureBlockToGroups(LectureBlockRef block) {
		return lectureBlockToGroupDao.getGroups(block);
	}

	@Override
	public List<Group> getLectureBlockToGroups(LectureBlockRef block, RepositoryEntryRelationType type) {
		return lectureBlockToGroupDao.getGroups(block, type);
	}

	@Override
	public List<Identity> getParticipants(LectureBlockRef block) {
		return lectureBlockDao.getParticipants(block);
	}

	@Override
	public List<Identity> getParticipants(RepositoryEntry entry) {
		return lectureBlockDao.getParticipants(entry);
	}

	@Override
	public List<Identity> getParticipants(RepositoryEntry entry, Identity teacher) {
		return lectureBlockDao.getParticipants(entry, teacher);
	}


	@Override
	public List<Identity> searchParticipants(LecturesMemberSearchParameters searchParams) {
		return lectureBlockDao.getParticipants(searchParams);
	}

	@Override
	public List<Identity> startLectureBlock(Identity teacher, LectureBlock lectureBlock) {
		RepositoryEntry entry = lectureBlock.getEntry();
		Date now = new Date();

		List<ParticipantAndLectureSummary> participantsAndSummaries = lectureParticipantSummaryDao.getLectureParticipantSummaries(lectureBlock);
		Set<Identity> participants = new HashSet<>();
		for(ParticipantAndLectureSummary participantAndSummary:participantsAndSummaries) {
			if(participants.contains(participantAndSummary.getIdentity())) {
				continue;
			}
			if(participantAndSummary.getSummary() == null) {
				lectureParticipantSummaryDao.createSummary(entry, participantAndSummary.getIdentity(), now);
			}
			participants.add(participantAndSummary.getIdentity());
		}
		return new ArrayList<>(participants);
	}
	
	@Override
	public List<Identity> syncParticipantSummaries(LectureBlock lectureBlock) {
		RepositoryEntry entry = lectureBlock.getEntry();
		Date now = new Date();

		List<ParticipantAndLectureSummary> participantsAndSummaries = lectureParticipantSummaryDao.getLectureParticipantSummaries(lectureBlock);
		Set<Identity> participants = new HashSet<>();
		for(ParticipantAndLectureSummary participantAndSummary:participantsAndSummaries) {
			if(participants.contains(participantAndSummary.getIdentity())) {
				continue;
			}
			if(participantAndSummary.getSummary() == null) {
				lectureParticipantSummaryDao.createSummary(entry, participantAndSummary.getIdentity(), now);
			}
			participants.add(participantAndSummary.getIdentity());
		}
		return new ArrayList<>(participants);
	}
	
	@Override
	public List<Identity> syncParticipantSummariesAndRollCalls(LectureBlock lectureBlock, LectureBlockAuditLog.Action action) {
		RepositoryEntry entry = lectureBlock.getEntry();
		Date firstAdmission = lectureBlock.getStartDate();
		firstAdmission = CalendarUtils.removeTime(firstAdmission);

		//load data
		List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(lectureBlock);
		Map<Identity,LectureBlockRollCall> rollCallMap = rollCalls.stream().collect(Collectors.toMap(LectureBlockRollCall::getIdentity, r -> r));
		List<ParticipantAndLectureSummary> participantsAndSummaries = lectureParticipantSummaryDao.getLectureParticipantSummaries(lectureBlock);
		
		Set<Identity> participants = new HashSet<>();
		for(ParticipantAndLectureSummary participantAndSummary:participantsAndSummaries) {
			if(participants.contains(participantAndSummary.getIdentity())) {
				continue;
			}
			
			// update or create summary
			if(participantAndSummary.getSummary() == null) {
				lectureParticipantSummaryDao.createSummary(entry, participantAndSummary.getIdentity(), firstAdmission);
			} else if(participantAndSummary.getSummary().getFirstAdmissionDate() == null || participantAndSummary.getSummary().getFirstAdmissionDate().after(firstAdmission)) {
				participantAndSummary.getSummary().setFirstAdmissionDate(firstAdmission);
				lectureParticipantSummaryDao.update(participantAndSummary.getSummary());
			}

			LectureBlockRollCall rollCall = rollCallMap.get(participantAndSummary.getIdentity());
			String before = auditLogDao.toXml(rollCall);
			if(rollCall == null) {
				AbsenceNotice notice =  getAbsenceNotice(participantAndSummary.getIdentity(), lectureBlock);
				rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, participantAndSummary.getIdentity(),
						null, null, null, notice, null, new ArrayList<>());
			} else if(rollCall.getLecturesAbsentList().isEmpty() && rollCall.getLecturesAttendedList().isEmpty()) {
				rollCall = lectureBlockRollCallDao.addLecture(lectureBlock, rollCall, new ArrayList<>());
			}

			String after = auditLogDao.toXml(rollCall);
			auditLogDao.auditLog(action, before, after, null,
					lectureBlock, rollCall, lectureBlock.getEntry(), participantAndSummary.getIdentity(), null);

			participants.add(participantAndSummary.getIdentity());
		}
		
		return new ArrayList<>(participants);
	}

	@Override
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRef block) {
		return lectureBlockRollCallDao.getRollCalls(block);
	}

	@Override
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRollCallSearchParameters searchParams) {
		return lectureBlockRollCallDao.getRollCalls(searchParams);
	}

	@Override
	public List<LectureBlockRollCallAndCoach> getLectureBlockAndRollCalls(LectureBlockRollCallSearchParameters searchParams,
			String teacherSeaparator) {
		return lectureBlockRollCallDao.getLectureBlockAndRollCalls(searchParams, teacherSeaparator);
	}

	@Override
	public LectureBlockRollCall getOrCreateRollCall(Identity identity, LectureBlock lectureBlock,
			Boolean authorizedAbsence, String reasonAbsence, AbsenceCategory category) {
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		if(rollCall == null) {//reload in case of concurrent usage
			AbsenceNotice notice =  getAbsenceNotice(identity, lectureBlock);
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity,
					authorizedAbsence, reasonAbsence, category, notice, null, null);
		} else if(authorizedAbsence != null) {
			rollCall.setAbsenceAuthorized(authorizedAbsence);
			rollCall.setAbsenceReason(reasonAbsence);
			rollCall.setAbsenceCategory(category);
			rollCall = lectureBlockRollCallDao.update(rollCall);
		}
		return rollCall;
	}
	
	@Override
	public LectureBlockRollCall getRollCall(LectureBlockRollCallRef rollCall) {
		if(rollCall == null) return null;
		return lectureBlockRollCallDao.loadByKey(rollCall.getKey());
	}

	@Override
	public LectureBlockRollCall updateRollCall(LectureBlockRollCall rollCall) {
		return lectureBlockRollCallDao.update(rollCall);
	}

	@Override
	public LectureBlockRollCall addRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences) {
		if(rollCall == null) {//reload in case of concurrent usage
			rollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		}
		
		boolean checkAuthorized = lectureModule.isAuthorizedAbsenceEnabled() &&  lectureModule.isAbsenceDefaultAuthorized();
		if(rollCall == null) {
			Boolean authorized = null;
			if(checkAuthorized && absences != null && !absences.isEmpty()) {
				authorized = Boolean.TRUE;
			}
			AbsenceNotice notice =  getAbsenceNotice(identity, lectureBlock);
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, authorized, null, null, notice, null, absences);
		} else {
			if(checkAuthorized && absences != null && !absences.isEmpty() && rollCall.getAbsenceAuthorized() == null) {
				rollCall.setAbsenceAuthorized(Boolean.TRUE);
			}
			rollCall = lectureBlockRollCallDao.addLecture(lectureBlock, rollCall, absences);
		}
		return rollCall;
	}
	
	@Override
	public LectureBlockRollCall addRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, String comment, List<Integer> absences) {
		if(rollCall == null) {//reload in case of concurrent usage
			rollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		}
		boolean checkAuthorized = lectureModule.isAuthorizedAbsenceEnabled() &&  lectureModule.isAbsenceDefaultAuthorized();
		if(rollCall == null) {
			Boolean authorized = null;
			if(checkAuthorized && absences != null && !absences.isEmpty()) {
				authorized = Boolean.TRUE;
			}
			AbsenceNotice notice =  getAbsenceNotice(identity, lectureBlock);
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, authorized, null, null, notice, comment, absences);
		} else {
			if(comment != null) {
				rollCall.setComment(comment);
			}
			if(checkAuthorized && absences != null && !absences.isEmpty() && rollCall.getAbsenceAuthorized() == null) {
				rollCall.setAbsenceAuthorized(Boolean.TRUE);
			}
			rollCall = lectureBlockRollCallDao.addLecture(lectureBlock, rollCall, absences);
		}
		return rollCall;
	}

	@Override
	public LectureBlockRollCall removeRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences) {
		if(rollCall == null) {//reload in case of concurrent usage
			rollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		}
		if(rollCall == null) {
			AbsenceNotice notice =  getAbsenceNotice(identity, lectureBlock);
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, notice, null, absences);
		} else {
			rollCall = lectureBlockRollCallDao.removeLecture(lectureBlock, rollCall, absences);
		}
		return rollCall;
	}
	
	@Override
	public List<LectureBlock> saveDefaultRollCalls(List<LectureBlock> lectureBlocks, Identity teacher, boolean closeLectures) {
		List<LectureBlock> mergedBlocks = new ArrayList<>(lectureBlocks.size());
		for(LectureBlock lectureBlock:lectureBlocks) {
			lectureBlock = getLectureBlock(lectureBlock);
			lectureBlock = saveDefaultRollCall(lectureBlock, teacher, closeLectures);
			dbInstance.commit();
			mergedBlocks.add(lectureBlock);
		}
		return mergedBlocks;
	}
	
	private LectureBlock saveDefaultRollCall(LectureBlock lectureBlock, Identity teacher, boolean closeLectures) {
		String before = toAuditXml(lectureBlock);
		
		List<Identity> participants = startLectureBlock(teacher, lectureBlock);
		List<AbsenceNotice> notices = getAbsenceNoticeRelatedTo(lectureBlock);
		Map<Identity,AbsenceNotice> identityToNotices = notices.stream()
				.collect(Collectors.toMap(AbsenceNotice::getIdentity, notice -> notice, (u, v) -> u));

		List<LectureBlockRollCall> rollCalls = getRollCalls(lectureBlock);
		Map<Identity,LectureBlockRollCall> identityToRollCallMap = rollCalls.stream()
				.collect(Collectors.toMap(LectureBlockRollCall::getIdentity, call -> call, (u, v) -> u));
		
		int numOfLectures = lectureBlock.getCalculatedLecturesNumber();
		
		for(Identity participant:participants) {
			LectureBlockRollCall rollCall = identityToRollCallMap.get(participant);
			AbsenceNotice notice = identityToNotices.get(participant);
			
			List<Integer> absenceList;
			if(notice != null) {
				absenceList = new ArrayList<>();
				for(int i=0; i<numOfLectures; i++) {
					absenceList.add(Integer.valueOf(i));
				}
			} else if(rollCall != null) {
				absenceList = rollCall.getLecturesAbsentList();
			} else {
				absenceList = new ArrayList<>();
			}
			addRollCall(participant, lectureBlock, rollCall, null, absenceList);
		}

		lectureBlock = getLectureBlock(lectureBlock);
		
		if(closeLectures) {
			lectureBlock.setEffectiveLecturesNumber(lectureBlock.getPlannedLecturesNumber());
			lectureBlock.setStatus(LectureBlockStatus.done);
			lectureBlock.setRollCallStatus(LectureRollCallStatus.closed);
		} else {
			if(lectureBlock.getRollCallStatus() == null) {
				lectureBlock.setRollCallStatus(LectureRollCallStatus.open);
			}
			if(lectureBlock.getStatus() == null || lectureBlock.getStatus() == LectureBlockStatus.active) {
				lectureBlock.setStatus(LectureBlockStatus.active);
			}
		}
		
		lectureBlock = save(lectureBlock, null);
		recalculateSummary(lectureBlock.getEntry());
		
		String after = toAuditXml(lectureBlock);
		auditLog(LectureBlockAuditLog.Action.saveLectureBlock, before, after, null, lectureBlock, null, lectureBlock.getEntry(), null, teacher);
		
		return lectureBlock;
	}

	@Override
	public AbsenceNotice getAbsenceNotice(IdentityRef identity, LectureBlock lectureBlock) {
		List<AbsenceNotice> notices = absenceNoticeDao.getAbsenceNotices(identity, lectureBlock);
		AbsenceNotice preferedNotice = null;
		if(notices.size() == 1) {
			preferedNotice = notices.get(0);
		} else if(notices.size() > 1) {
			// sort by date
			Collections.sort(notices, (n1, n2) -> n1.getCreationDate().compareTo(n2.getCreationDate()));
			
			for(int i=notices.size(); i-->=0 && preferedNotice == null;) {
				AbsenceNotice notice = notices.get(i);
				if(notice.getAbsenceAuthorized() != null && notice.getAbsenceAuthorized().booleanValue()) {
					preferedNotice = notice;
				}
			}
			
			if(preferedNotice == null) {
				preferedNotice = notices.get(notices.size() - 1);
			}
		}
		return preferedNotice;
	}

	@Override
	public void adaptRollCalls(LectureBlock lectureBlock) {
		LectureBlockStatus status = lectureBlock.getStatus();
		LectureRollCallStatus rollCallStatus = lectureBlock.getRollCallStatus();
		if(status == LectureBlockStatus.done || rollCallStatus == LectureRollCallStatus.closed || rollCallStatus == LectureRollCallStatus.autoclosed) {
			log.warn("Try to adapt roll call of a closed lecture block: {}", lectureBlock.getKey());
			return;
		}

		List<LectureBlockRollCall> rollCallList = lectureBlockRollCallDao.getRollCalls(lectureBlock);
		for(LectureBlockRollCall rollCall:rollCallList) {
			int numOfLectures = lectureBlock.getCalculatedLecturesNumber();
			lectureBlockRollCallDao.adaptLecture(lectureBlock, rollCall, numOfLectures, null);
		}
	}
	
	@Override
	public void adaptAll(Identity author) {
		List<LectureBlock> lectureBlocks = lectureBlockDao.getLectureBlocks();
		for(LectureBlock lectureBlock:lectureBlocks) {
			List<LectureBlockRollCall> rollCallList = lectureBlockRollCallDao.getRollCalls(lectureBlock);
			for(LectureBlockRollCall rollCall:rollCallList) {
				int numOfLectures = lectureBlock.getCalculatedLecturesNumber();
				lectureBlockRollCallDao.adaptLecture(lectureBlock, rollCall, numOfLectures, author);
			}
			dbInstance.commitAndCloseSession();
		}
	}

	@Override
	public void recalculateSummary(RepositoryEntry entry) {
		List<LectureBlockStatistics> statistics = getParticipantsLecturesStatistics(entry);
		int count = 0;
		for(LectureBlockStatistics statistic:statistics) {
			if(lectureParticipantSummaryDao.updateStatistics(statistic) == 0) {
				Identity identity = dbInstance.getCurrentEntityManager()
						.getReference(IdentityImpl.class, statistic.getIdentityKey());
				lectureParticipantSummaryDao.createSummary(entry, identity, new Date(), statistic);
			}
			if(++count % 20 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
	}

	@Override
	public void recalculateSummary(RepositoryEntry entry, Identity identity) {
		List<LectureBlockStatistics> statistics = getParticipantsLecturesStatistics(entry);
		for(LectureBlockStatistics statistic:statistics) {
			if(identity.getKey().equals(statistic.getIdentityKey())) {
				if(lectureParticipantSummaryDao.updateStatistics(statistic) == 0) {
					lectureParticipantSummaryDao.createSummary(entry, identity, new Date(), statistic);
				}
			}
		}
	}

	@Override
	public void autoCloseRollCall() {
		int period = lectureModule.getRollCallAutoClosePeriod();
		if(period > 0) {
			Date now = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(now);
			cal.add(Calendar.DATE, -period);
			Date endDate = cal.getTime();
			List<LectureBlockImpl> blocks = lectureBlockDao.loadOpenBlocksBefore(endDate);
			for(LectureBlockImpl block:blocks) {
				autoClose(block);
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private void autoClose(LectureBlockImpl lectureBlock) {
		String blockBefore = auditLogDao.toXml(lectureBlock);
		lectureBlock.setStatus(LectureBlockStatus.done);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.autoclosed);
		if(lectureBlock.getEffectiveLecturesNumber() <= 0 && lectureBlock.getStatus() != LectureBlockStatus.cancelled) {
			lectureBlock.setEffectiveLecturesNumber(lectureBlock.getPlannedLecturesNumber());
		}
		lectureBlock.setAutoClosedDate(new Date());
		lectureBlock = (LectureBlockImpl)lectureBlockDao.update(lectureBlock);
		dbInstance.commit();
		
		List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(lectureBlock);
		Map<Identity,LectureBlockRollCall> rollCallMap = rollCalls.stream().collect(Collectors.toMap(LectureBlockRollCall::getIdentity, r -> r));
		List<ParticipantAndLectureSummary> participantsAndSummaries = lectureParticipantSummaryDao.getLectureParticipantSummaries(lectureBlock);
		Set<Identity> participants = new HashSet<>();
		for(ParticipantAndLectureSummary participantAndSummary:participantsAndSummaries) {
			if(participants.contains(participantAndSummary.getIdentity())) {
				continue;
			}
			if(participantAndSummary.getSummary() != null) {
				LectureBlockRollCall rollCall = rollCallMap.get(participantAndSummary.getIdentity());
				
				String before = auditLogDao.toXml(rollCall);
				if(rollCall == null) {
					AbsenceNotice notice = getAbsenceNotice(participantAndSummary.getIdentity(), lectureBlock);
					rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, participantAndSummary.getIdentity(),
							null, null, null, notice, null, new ArrayList<>());
				} else if(rollCall.getLecturesAbsentList().isEmpty() && rollCall.getLecturesAttendedList().isEmpty()) {
					rollCall = lectureBlockRollCallDao.addLecture(lectureBlock, rollCall, new ArrayList<>());
				}

				String after = auditLogDao.toXml(rollCall);
				auditLogDao.auditLog(LectureBlockAuditLog.Action.autoclose, before, after, null,
						lectureBlock, rollCall, lectureBlock.getEntry(), participantAndSummary.getIdentity(), null);
			}
		}

		String blockAfter = auditLogDao.toXml(lectureBlock);
		auditLogDao.auditLog(LectureBlockAuditLog.Action.autoclose, blockBefore, blockAfter, null, lectureBlock, null, lectureBlock.getEntry(), null, null);
		dbInstance.commit();
		
		recalculateSummary(lectureBlock.getEntry());
		dbInstance.commit();
		
		//send email
		sendAutoCloseNotifications(lectureBlock);
	}
	
	private void sendAutoCloseNotifications(LectureBlock lectureBlock) {
		RepositoryEntry entry = lectureBlock.getEntry();
		RepositoryEntryLectureConfiguration config = getRepositoryEntryLectureConfiguration(entry);
		if(config.isLectureEnabled()
				&& entry.getEntryStatus() != RepositoryEntryStatusEnum.trash
				&& entry.getEntryStatus() != RepositoryEntryStatusEnum.deleted) {
			List<Identity> owners = repositoryEntryRelationDao
					.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.owner.name());
			List<Identity> teachers = getTeachers(lectureBlock);
			
			for(Identity owner:owners) {
				MailerResult result = sendMail("lecture.autoclose.notification.subject", "lecture.autoclose.notification.body",
						owner, teachers, lectureBlock);
				if(result.getReturnCode() == MailerResult.OK) {
					log.info(Tracing.M_AUDIT, "Notification of lecture auto-close: {} in course: {}", lectureBlock.getKey(), entry.getKey());
				} else {
					log.error("Notification of lecture auto-close cannot be send: {} in course: {}", lectureBlock.getKey(), entry.getKey());
				}
			}
		}
	}

	@Override
	public void sendReminders() {
		int reminderPeriod = lectureModule.getRollCallReminderPeriod();
		if(reminderPeriod > 0) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -reminderPeriod);
			Date endDate = cal.getTime();

			boolean reminderEnabled = lectureModule.isRollCallReminderEnabled();
			List<LectureBlockToTeacher> toRemindList = lectureBlockReminderDao.getLectureBlockTeachersToReminder(endDate);
			for(LectureBlockToTeacher toRemind:toRemindList) {
				Identity teacher = toRemind.getTeacher();
				LectureBlock lectureBlock = toRemind.getLectureBlock();
				if(reminderEnabled) {
					sendReminder(teacher, lectureBlock);
				} else {
					lectureBlockReminderDao.createReminder(lectureBlock, teacher, "disabled");
				}
			}
		}
	}
	
	private void sendReminder(Identity teacher, LectureBlock lectureBlock) {
		MailerResult result = sendMail("lecture.teacher.reminder.subject", "lecture.teacher.reminder.body",
				teacher, Collections.singletonList(teacher), lectureBlock);
		String status;
		List<Identity> failedIdentities = result.getFailedIdentites();
		if(failedIdentities != null && failedIdentities.contains(teacher)) {
			status = "error";
		} else {
			status = "ok";
		}
		
		lectureBlockReminderDao.createReminder(lectureBlock, teacher, status);
	}
	
	private MailerResult sendMail(String subjectI18nKey, String bodyI18nKey,
			Identity recipient, List<Identity> teachers, LectureBlock lectureBlock) {
		
		RepositoryEntry entry = lectureBlock.getEntry();
		String language = recipient.getUser().getPreferences().getLanguage();
		Locale locale = i18nManager.getLocaleOrDefault(language);
		String startDate = Formatter.getInstance(locale).formatDate(lectureBlock.getStartDate());
		
		StringBuilder sb = new StringBuilder();
		if(teachers != null && !teachers.isEmpty()) {
			for(Identity teacher:teachers) {
				if(sb.length() > 0) sb.append(",");
				sb.append(userManager.getUserDisplayName(teacher));
			}
		}
		
		MailContext context = new MailContextImpl("[RepositoryEntry:" + entry.getKey() + "]");
		String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey() + "/LectureBlock/" + lectureBlock.getKey();
		String[] args = new String[]{
				lectureBlock.getTitle(),					// 0
				startDate,									// 1
				entry.getDisplayname(),						// 2
				url,										// 3
				userManager.getUserDisplayName(recipient), 	// 4 The recipient
				sb.toString()								// 5 The teachers
		};
		
		Translator trans = Util.createPackageTranslator(LectureRepositoryAdminController.class, locale);
		String subject = trans.translate(subjectI18nKey, args);
		String body = trans.translate(bodyI18nKey, args);

		LectureReminderTemplate template = new LectureReminderTemplate(subject, body);
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, recipient, template, null, null, result);
		MailerResult sendResult = mailManager.sendMessage(bundle);
		result.append(sendResult);
		return sendResult;
	}

	@Override
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry) {
		return lectureBlockDao.getLectureBlocks(entry);
	}
	
	@Override
	public List<LectureBlock> getLectureBlocks(LecturesBlockSearchParameters searchParams) {
		return lectureBlockDao.searchLectureBlocks(searchParams);
	}

	@Override
	public List<LectureBlockRef> getAssessedLectureBlocks(LecturesBlockSearchParameters searchParams) {
		return lectureBlockDao.searchAssessedLectureBlocks(searchParams);
	}

	@Override
	public List<LectureBlockWithTeachers> getLectureBlocksWithTeachers(RepositoryEntryRef entry) {
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.getConfiguration(entry);
		return lectureBlockDao.getLecturesBlockWithTeachers(entry, config);
	}
	@Override
	public List<LectureReportRow> getLectureBlocksReport(Date from, Date to, List<LectureRollCallStatus> status) {
		return lectureBlockDao.getLecturesBlocksReport(from, to, status);
	}

	@Override
	public List<LectureBlockWithTeachers> getLectureBlocksWithTeachers(LecturesBlockSearchParameters searchParams) {
		return lectureBlockDao.getLecturesBlockWithTeachers(searchParams);
	}

	@Override
	public List<Identity> getTeachers(LectureBlock lectureBlock) {
		LectureBlockImpl block = (LectureBlockImpl)lectureBlock;
		return groupDao.getMembers(block.getTeacherGroup(), "teacher");
	}

	@Override
	public List<Identity> getTeachers(List<LectureBlock> blocks) {
		return lectureBlockDao.getTeachers(blocks);
	}

	@Override
	public List<Identity> getTeachers(RepositoryEntry entry) {
		return lectureBlockDao.getTeachers(entry);
	}

	@Override
	public List<Identity> getTeachers(Identity participant, List<LectureBlock> blocks, List<RepositoryEntry> entries, Date start, Date end) {
		return lectureBlockDao.getTeachers(participant, blocks, entries, start, end);
	}

	@Override
	public List<Identity> searchTeachers(LecturesMemberSearchParameters searchParams) {
		return lectureBlockDao.getTeachers(searchParams);
	}

	@Override
	public boolean isMasterCoach(LectureBlock block, IdentityRef identity) {
		return lectureBlockDao. isMasterCoach(block, identity);
	}

	@Override
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry, IdentityRef teacher) {
		return lectureBlockDao.getLecturesAsTeacher(entry, teacher);
	}

	@Override
	public boolean hasLecturesAsTeacher(RepositoryEntryRef entry, Identity identity) {
		return lectureBlockDao.hasLecturesAsTeacher(entry, identity);
	}

	@Override
	public List<LectureBlock> getRollCallAsTeacher(Identity identity) {
		return lectureBlockDao.getRollCallAsTeacher(identity);
	}

	@Override
	public void addTeacher(LectureBlock lectureBlock, Identity teacher) {
		LectureBlockImpl block = (LectureBlockImpl)lectureBlock;
		if(!groupDao.hasRole(block.getTeacherGroup(), teacher, "teacher")) {
			groupDao.addMembershipOneWay(block.getTeacherGroup(), teacher, "teacher");
		}
	}

	@Override
	public void removeTeacher(LectureBlock lectureBlock, Identity teacher) {
		LectureBlockImpl block = (LectureBlockImpl)lectureBlock;
		groupDao.removeMembership(block.getTeacherGroup(), teacher);
	}
	
	@Override
	public List<TaxonomyLevel> getTaxonomy(LectureBlockRef lectureBlock) {
		if(lectureBlock == null || lectureBlock.getKey() == null) return Collections.emptyList();
		return lectureBlockToTaxonomyLevelDao.getTaxonomyLevels(lectureBlock);
	}

	@Override
	public List<LectureBlock> getLectureBlocks(TaxonomyLevelRef level) {
		return lectureBlockToTaxonomyLevelDao.getLectureBlocks(level);
	}

	@Override
	public LectureParticipantSummary getOrCreateParticipantSummary(RepositoryEntry entry, Identity identity) {
		LectureParticipantSummary summary = lectureParticipantSummaryDao.getSummary(entry, identity);
		if(summary == null) {
			summary = lectureParticipantSummaryDao.createSummary(entry, identity, null);
		}
		return summary;
	}

	@Override
	public LectureParticipantSummary saveParticipantSummary(LectureParticipantSummary summary) {
		return lectureParticipantSummaryDao.update(summary);
	}
	
	

	@Override
	public List<LectureBlockBlockStatistics> getLectureBlocksStatistics(LecturesBlockSearchParameters searchParams) {
		List<LectureBlock> lectureBlocks = lectureBlockDao.searchLectureBlocks(searchParams);
		if(lectureBlocks == null || lectureBlocks.isEmpty()) {
			return new ArrayList<>();
		}

		Map<Long,Long> numOfParticipantMap = lectureBlockDao.getNumOfParticipants(lectureBlocks);
		Map<Long,LectureBlockBlockStatistics> lectureBlockKeyToStatistics = lectureBlocks.stream()
				.collect(Collectors.toMap(LectureBlock::getKey, block -> {
					Long numberOfParticipants = numOfParticipantMap.get(block.getKey());
					long numOfParticipants = numberOfParticipants == null ? 0l : numberOfParticipants.longValue();
					return new LectureBlockBlockStatistics(block, numOfParticipants);
				}, (u, v) -> v));
		
		List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(lectureBlocks);
		for(LectureBlockRollCall rollCall:rollCalls) {
			AbsenceNotice absenceNotice = rollCall.getAbsenceNotice();
			LectureBlock lectureBlock = rollCall.getLectureBlock();
			
			LectureBlockBlockStatistics statistics = lectureBlockKeyToStatistics.get(lectureBlock.getKey());
			if(statistics != null) {
				statistics.aggregate(rollCall, absenceNotice);
			}
		}

		return new ArrayList<>(lectureBlockKeyToStatistics.values());
	}

	@Override
	public List<LectureBlockIdentityStatistics> groupByIdentity(List<LectureBlockIdentityStatistics> statistics) {
		Map<Long,LectureBlockIdentityStatistics> groupBy = new HashMap<>();
		for(LectureBlockIdentityStatistics statistic:statistics) {
			if(groupBy.containsKey(statistic.getIdentityKey())){
				groupBy.get(statistic.getIdentityKey()).aggregate(statistic);
			} else {
				groupBy.put(statistic.getIdentityKey(), statistic.cloneForAggregation());
			}
		}

		boolean countDispensationAsAttendant = lectureModule.isCountDispensationAsAttendant() && lectureModule.isAbsenceNoticeEnabled();
		boolean countAuthorizedAbsenceAsAttendant = lectureModule.isCountAuthorizedAbsenceAsAttendant();
		List<LectureBlockIdentityStatistics> aggregatedStatistics = new ArrayList<>(groupBy.values());
		for(LectureBlockIdentityStatistics statistic:aggregatedStatistics) {
			lectureBlockRollCallDao.calculateAttendanceRate(statistic, countAuthorizedAbsenceAsAttendant, countDispensationAsAttendant);
		}
		return aggregatedStatistics;
	}

	@Override
	public List<IdentityRateWarning> groupRateWarning(List<LectureBlockIdentityStatistics> statistics) {
		Map<IdentityRepositoryEntryKey,LectureBlockIdentityStatistics> groupBy = new HashMap<>();
		for(LectureBlockIdentityStatistics statistic:statistics) {
			IdentityRepositoryEntryKey key = new IdentityRepositoryEntryKey(statistic.getIdentityKey(), statistic.getRepoKey());
			if(groupBy.containsKey(key)){
				groupBy.get(key).aggregate(statistic);
			} else {
				groupBy.put(key, statistic.cloneAll());
			}
		}

		boolean countDispensationAsAttendant = lectureModule.isCountDispensationAsAttendant() && lectureModule.isAbsenceNoticeEnabled();
		boolean countAuthorizedAbsenceAsAttendant = lectureModule.isCountAuthorizedAbsenceAsAttendant();
		List<LectureBlockIdentityStatistics> aggregatedStatistics = new ArrayList<>(groupBy.values());
		for(LectureBlockIdentityStatistics statistic:aggregatedStatistics) {
			lectureBlockRollCallDao.calculateAttendanceRate(statistic, countAuthorizedAbsenceAsAttendant, countDispensationAsAttendant);
		}
		
		Map<Long,IdentityRateWarning> warnings = new HashMap<>();
		for(LectureBlockIdentityStatistics aggregatedStatistic:aggregatedStatistics) {
			LectureRateWarning warning = calculateWarning(aggregatedStatistic);
			if(warning == LectureRateWarning.error || warning == LectureRateWarning.warning) {
				Long identityKey = aggregatedStatistic.getIdentityKey();
				if(warnings.containsKey(identityKey)) {
					warnings.get(identityKey).updateWarning(warning);
				} else {
					warnings.put(identityKey, new IdentityRateWarning(identityKey, warning));
				}
			}
		}
		return new ArrayList<>(warnings.values());
	}
	
	private LectureRateWarning calculateWarning(LectureBlockIdentityStatistics stats) {
		if(stats.isCalculateRate() && stats.getTotalPersonalPlannedLectures() > 0 &&
				(stats.getTotalAbsentLectures() > 0 || stats.getTotalAttendedLectures() > 0 || stats.getTotalAuthorizedAbsentLectures() > 0)) {
			double attendanceRate = stats.getAttendanceRate();
			double requiredRate = stats.getRequiredRate();
			
			if(requiredRate > attendanceRate) {
				return LectureRateWarning.error;
			} else if(attendanceRate - requiredRate < 0.05) {// less than 5%
				return LectureRateWarning.warning;
			}
		}
		return LectureRateWarning.none;
	}

	@Override
	public List<LectureBlockStatistics> getParticipantLecturesStatistics(IdentityRef identity) {
		boolean authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		boolean calculateAttendanceRate = lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled();
		boolean absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		boolean countAuthorizedAbsenceAsAttendant = lectureModule.isCountAuthorizedAbsenceAsAttendant();
		boolean countDispensationAsAttendant = lectureModule.isCountDispensationAsAttendant() && lectureModule.isAbsenceNoticeEnabled();
		double defaultRequiredAttendanceRate = lectureModule.getRequiredAttendanceRateDefault();
		return lectureBlockRollCallDao.getStatistics(identity, RepositoryEntryStatusEnum.publishedAndClosed(),
				authorizedAbsenceEnabled, absenceDefaultAuthorized, countAuthorizedAbsenceAsAttendant, countDispensationAsAttendant,
				calculateAttendanceRate, defaultRequiredAttendanceRate);
	}

	@Override
	public List<LectureBlockStatistics> getParticipantsLecturesStatistics(RepositoryEntry entry) {
		boolean authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		boolean calculateAttendanceRate = lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled();
		boolean absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		boolean countAuthorizedAbsenceAsAttendant = lectureModule.isCountAuthorizedAbsenceAsAttendant();
		boolean countDispensationAsAttendant = lectureModule.isCountDispensationAsAttendant() && lectureModule.isAbsenceNoticeEnabled();
		double defaultRequiredAttendanceRate = lectureModule.getRequiredAttendanceRateDefault();
		RepositoryEntryLectureConfiguration config = getRepositoryEntryLectureConfiguration(entry);
		return lectureBlockRollCallDao.getStatistics(entry, config, authorizedAbsenceEnabled,
				absenceDefaultAuthorized, countAuthorizedAbsenceAsAttendant, countDispensationAsAttendant,
				calculateAttendanceRate, defaultRequiredAttendanceRate);
	}
	
	@Override
	public List<LectureBlockIdentityStatistics> getLecturesStatistics(LectureStatisticsSearchParameters params,
			List<UserPropertyHandler> userPropertyHandlers, Identity identity) {
		boolean authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		boolean calculateAttendanceRate = lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled();
		boolean absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		boolean countAuthorizedAbsenceAsAttendant = lectureModule.isCountAuthorizedAbsenceAsAttendant();
		boolean countDispensationAsAttendant = lectureModule.isCountDispensationAsAttendant() && lectureModule.isAbsenceNoticeEnabled();
		double defaultRequiredAttendanceRate = lectureModule.getRequiredAttendanceRateDefault();
		return lectureBlockRollCallDao.getStatistics(params, userPropertyHandlers, identity, 
				authorizedAbsenceEnabled, absenceDefaultAuthorized, countAuthorizedAbsenceAsAttendant, countDispensationAsAttendant,
				calculateAttendanceRate, defaultRequiredAttendanceRate);
	}

	@Override
	public AggregatedLectureBlocksStatistics aggregatedStatistics(List<? extends LectureBlockStatistics> statistics) {
		boolean countDispensationAsAttendant = lectureModule.isCountDispensationAsAttendant() && lectureModule.isAbsenceNoticeEnabled();
		boolean countAuthorizedAbsenceAsAttendant = lectureModule.isCountAuthorizedAbsenceAsAttendant();
		return lectureBlockRollCallDao.aggregatedStatistics(statistics, countAuthorizedAbsenceAsAttendant, countDispensationAsAttendant);
	}

	@Override
	public List<LectureBlockAndRollCall> getParticipantLectureBlocks(RepositoryEntryRef entry, IdentityRef participant,
			String teacherSeaparator) {
		return lectureBlockRollCallDao.getParticipantLectureBlockAndRollCalls(entry, participant, teacherSeaparator);
	}
	
	@Override
	public List<LectureRepositoryEntryInfos> searchRepositoryEntries(LectureRepositoryEntrySearchParameters searchParams) {
		return lectureBlockDao.searchRepositoryEntries(searchParams);
	}

	@Override
	public List<LectureCurriculumElementInfos> searchCurriculumElements(LectureCurriculumElementSearchParameters searchParams) {
		return lectureBlockDao.searchCurriculumElements(searchParams);
	}

	@Override
	public void syncCalendars(RepositoryEntry entry) {
		RepositoryEntryLectureConfiguration config = getRepositoryEntryLectureConfiguration(entry);
		if(ConfigurationHelper.isSyncTeacherCalendarEnabled(config, lectureModule)) {
			List<LectureBlock> blocks = getLectureBlocks(entry);
			for(LectureBlock block:blocks) {
				List<Identity> teachers = getTeachers(block);
				syncInternalCalendar(block, teachers);
			}
		} else {
			unsyncTeachersCalendar(entry);
		}
		
		if(ConfigurationHelper.isSyncCourseCalendarEnabled(config, lectureModule)) {
			fullSyncCourseCalendar(config.getEntry());
		} else {
			unsyncInternalCalendar(config.getEntry());
		}
	}

	@Override
	public void syncCalendars(LectureBlock lectureBlock) {
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.getConfiguration(lectureBlock);
		if(ConfigurationHelper.isSyncTeacherCalendarEnabled(config, lectureModule)) {
			List<Identity> teachers = getTeachers(lectureBlock);
			syncInternalCalendar(lectureBlock, teachers);
		} else {
			List<Identity> teachers = getTeachers(lectureBlock);
			unsyncInternalCalendar(lectureBlock, teachers);
		}

		if(ConfigurationHelper.isSyncCourseCalendarEnabled(config, lectureModule)) {
			syncCourseCalendar(lectureBlock, config.getEntry());
		} else {
			unsyncCourseCalendar(lectureBlock, config.getEntry());
		}
	}

	private void syncCourseCalendar(LectureBlock lectureBlock, RepositoryEntry entry) {
		Kalendar cal = calendarMgr.getCalendar(CalendarManager.TYPE_COURSE, entry.getOlatResource().getResourceableId().toString());
		syncEvent(lectureBlock, entry, cal);
	}
	
	private void unsyncCourseCalendar(LectureBlock lectureBlock, RepositoryEntry entry) {
		Kalendar cal = calendarMgr.getCalendar(CalendarManager.TYPE_COURSE, entry.getOlatResource().getResourceableId().toString());
		unsyncEvent(lectureBlock, entry, cal);
	}
	
	private void fullSyncCourseCalendar(RepositoryEntry entry) {
		List<LectureBlock> blocks = getLectureBlocks(entry);
		Map<String, LectureBlock> externalIds = blocks.stream()
				.collect(Collectors.toMap(b -> generateExternalId(b, entry), b -> b));
		
		Kalendar cal = calendarMgr.getCalendar(CalendarManager.TYPE_COURSE, entry.getOlatResource().getResourceableId().toString());
		String prefix = generateExternalIdPrefix(entry);
		
		List<KalendarEvent> events = new ArrayList<>(cal.getEvents());
		for(KalendarEvent event:events) {
			String externalId = event.getExternalId();
			if(StringHelper.containsNonWhitespace(externalId) && externalId.startsWith(prefix)) {
				if(externalIds.containsKey(externalId)) {
					if(updateEvent(externalIds.get(externalId), event)) {
						calendarMgr.updateEventFrom(cal, event);
					}
					externalIds.remove(externalId);
				} else {
					calendarMgr.removeEventFrom(cal, event);
				}
			}
		}
		
		// add new calendar events
		List<KalendarEvent> eventsToAdd = new ArrayList<>();
		for(Map.Entry<String, LectureBlock> entryToAdd:externalIds.entrySet()) {
			eventsToAdd.add(createEvent(entryToAdd.getValue(), entry));
		}
		if(!eventsToAdd.isEmpty()) {
			calendarMgr.addEventTo(cal, eventsToAdd);
		}
	}
	
	private void unsyncInternalCalendar(RepositoryEntry entry) {
		Kalendar cal = calendarMgr.getCalendar(CalendarManager.TYPE_COURSE, entry.getOlatResource().getResourceableId().toString());
		String prefix = generateExternalIdPrefix(entry);
		List<KalendarEvent> events = new ArrayList<>(cal.getEvents());
		for(KalendarEvent event:events) {
			String externalId = event.getExternalId();
			if(StringHelper.containsNonWhitespace(externalId) && externalId.startsWith(prefix)) {
				calendarMgr.removeEventFrom(cal, event);
			}
		}
	}
	
	private void syncInternalCalendar(LectureBlock lectureBlock, List<Identity> identities) {
		RepositoryEntry entry = lectureBlock.getEntry();
		for(Identity identity:identities) {
			Kalendar cal = calendarMgr.getCalendar(CalendarManager.TYPE_USER, identity.getName());
			syncEvent(lectureBlock, entry, cal);
			lectureParticipantSummaryDao.updateCalendarSynchronization(entry, identity);
		}
	}
	
	/**
	 * Try to update the vent. If there is not an event with
	 * the right external identifier, it returns false and do nothing.
	 * 
	 * @param lectureBlock
	 * @param eventExternalId
	 * @param cal
	 * @return
	 */
	private boolean syncEvent(LectureBlock lectureBlock, RepositoryEntry entry, Kalendar cal) {
		boolean updated = false;
		String eventExternalId = generateExternalId(lectureBlock, entry);
		
		for(KalendarEvent event:cal.getEvents()) {
			if(eventExternalId.equals(event.getExternalId())) {
				if(updateEvent(lectureBlock, event)) {
					calendarMgr.updateEventFrom(cal, event);
				}
				return true;
			}
		}
		
		if(!updated) {
			KalendarEvent newEvent = createEvent(lectureBlock, entry);
			calendarMgr.addEventTo(cal, newEvent);
		}
		
		return true;
	}
	
	private void unsyncInternalCalendar(LectureBlock lectureBlock, List<Identity> identities) {
		RepositoryEntry entry = lectureBlock.getEntry();
		for(Identity identity:identities) {
			Kalendar cal = calendarMgr.getCalendar(CalendarManager.TYPE_USER, identity.getName());
			unsyncEvent(lectureBlock, entry, cal);
			lectureParticipantSummaryDao.updateCalendarSynchronization(entry, identity);
		}
	}
	
	private void unsyncEvent(LectureBlock lectureBlock, RepositoryEntry entry, Kalendar cal) {
		String externalId = generateExternalId(lectureBlock, entry);
		List<KalendarEvent> events = new ArrayList<>(cal.getEvents());
		for(KalendarEvent event:events) {
			if(externalId.equals(event.getExternalId())) {
				calendarMgr.removeEventFrom(cal, event);
			}
		}
	}
	
	private void unsyncTeachersCalendar(RepositoryEntry entry) {
		List<Identity> teachers = getTeachers(entry);
		unsyncInternalCalendar(entry, teachers);
	}
	
	private void unsyncInternalCalendar(RepositoryEntry entry, List<Identity> identities) {
		String prefix = generateExternalIdPrefix(entry);
		for(Identity identity:identities) {
			Kalendar cal = calendarMgr.getCalendar(CalendarManager.TYPE_USER, identity.getName());
			List<KalendarEvent> events = new ArrayList<>(cal.getEvents());
			for(KalendarEvent event:events) {
				if(event.getExternalId() != null && event.getExternalId().startsWith(prefix)) {
					calendarMgr.removeEventFrom(cal, event);
				}
			}
			lectureParticipantSummaryDao.updateCalendarSynchronization(entry, identity);
		}
	}
	
	private KalendarEvent createEvent(LectureBlock lectureBlock, RepositoryEntry entry) {
		String eventId = UUID.randomUUID().toString();
		String title = lectureBlock.getTitle();
		KalendarEvent event = new KalendarEvent(eventId, null, title, lectureBlock.getStartDate(), lectureBlock.getEndDate());
		event.setExternalId(generateExternalId(lectureBlock, entry));
		event.setLocation(lectureBlock.getLocation());
		updateEventDescription(lectureBlock, event);
		event.setManagedFlags(CAL_MANAGED_FLAGS);
		return event;
	}
	
	private boolean updateEvent(LectureBlock lectureBlock, KalendarEvent event) {
		event.setSubject(lectureBlock.getTitle());
		event.setLocation(lectureBlock.getLocation());
		updateEventDescription(lectureBlock, event);
		event.setBegin(lectureBlock.getStartDate());
		event.setEnd(lectureBlock.getEndDate());
		event.setManagedFlags(CAL_MANAGED_FLAGS);
		return true;
	}
	
	private void updateEventDescription(LectureBlock lectureBlock, KalendarEvent event) {
		StringBuilder descr = new StringBuilder();
		if(StringHelper.containsNonWhitespace(lectureBlock.getDescription())) {
			descr.append(lectureBlock.getDescription());
		}
		if(StringHelper.containsNonWhitespace(lectureBlock.getPreparation())) {
			if(descr.length() > 0) descr.append("\n");
			descr.append(lectureBlock.getPreparation());
		}
		event.setDescription(descr.toString());
	}
	
	private String generateExternalIdPrefix(RepositoryEntry entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("lecture-block-").append(entry.getKey()).append("-");
		return sb.toString();
	}
	
	private String generateExternalId(LectureBlock lectureBlock, RepositoryEntry entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("lecture-block-").append(entry.getKey()).append("-").append(lectureBlock.getKey());
		return sb.toString();
	}
	
	public class LectureReminderTemplate extends MailTemplate {
		
		public LectureReminderTemplate(String subjectTemplate, String bodyTemplate) {
			super(subjectTemplate, bodyTemplate, null);
		}

		@Override
		public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
			//
		}
	}
}