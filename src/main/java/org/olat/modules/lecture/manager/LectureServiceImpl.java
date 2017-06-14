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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureParticipantSummary;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.Reason;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.lecture.model.LectureBlockToTeacher;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.ParticipantAndLectureSummary;
import org.olat.modules.lecture.ui.ConfigurationHelper;
import org.olat.modules.lecture.ui.LectureAdminController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureServiceImpl implements LectureService, UserDataDeletable {
	
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
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private LectureBlockToGroupDAO lectureBlockToGroupDao;
	@Autowired
	private LectureBlockRollCallDAO lectureBlockRollCallDao;
	@Autowired
	private LectureBlockReminderDAO lectureBlockReminderDao;
	@Autowired
	private LectureParticipantSummaryDAO lectureParticipantSummaryDao;
	@Autowired
	private RepositoryEntryLectureConfigurationDAO lectureConfigurationDao;
	
	
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
	public RepositoryEntryLectureConfiguration copyRepositoryEntryLectureConfiguration(RepositoryEntry sourceEntry, RepositoryEntry targetEntry) {
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.getConfiguration(sourceEntry);
		if(config != null) {
			config = lectureConfigurationDao.cloneConfiguration(config, targetEntry);
		}
		return config;
	}

	@Override
	public RepositoryEntryLectureConfiguration updateRepositoryEntryLectureConfiguration(RepositoryEntryLectureConfiguration config) {
		RepositoryEntryLectureConfiguration updatedConfig = lectureConfigurationDao.update(config);
		return updatedConfig;
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
	public void deleteLectureBlock(LectureBlock block) {
		lectureBlockDao.delete(block);
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
	public void deleteUserData(Identity identity, String newDeletedUserName, File archivePath) {
		lectureParticipantSummaryDao.deleteSummaries(identity);
		lectureBlockRollCallDao.deleteRollCalls(identity);
		lectureBlockReminderDao.deleteReminders(identity);
	}

	@Override
	public LectureBlock getLectureBlock(LectureBlockRef block) {
		return lectureBlockDao.loadByKey(block.getKey());
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
	public Reason createReason(String title, String description) {
		return reasonDao.createReason(title, description);
	}

	@Override
	public Reason updateReason(Reason reason) {
		return reasonDao.updateReason(reason);
	}

	@Override
	public List<Group> getLectureBlockToGroups(LectureBlockRef block) {
		return lectureBlockToGroupDao.getGroups(block);
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
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRef block) {
		return lectureBlockRollCallDao.getRollCalls(block);
	}

	@Override
	public LectureBlockRollCall getOrCreateRollCall(Identity identity, LectureBlock lectureBlock,
			Boolean authorizedAbsence, String reasonAbsence) {
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		if(rollCall == null) {//reload in case of concurrent usage
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity,
					authorizedAbsence, reasonAbsence, null, null);
		} else if(authorizedAbsence != null) {
			rollCall.setAbsenceAuthorized(authorizedAbsence);
			rollCall.setAbsenceReason(reasonAbsence);
			rollCall = lectureBlockRollCallDao.update(rollCall);
		}
		return rollCall;
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
		if(rollCall == null) {
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, absences);
		} else {
			rollCall = lectureBlockRollCallDao.addLecture(lectureBlock, rollCall, absences);
		}
		return rollCall;
	}
	
	@Override
	public LectureBlockRollCall addRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, String comment, List<Integer> absences) {
		if(rollCall == null) {//reload in case of concurrent usage
			rollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		}
		if(rollCall == null) {
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, comment, absences);
		} else {
			if(comment != null) {
				rollCall.setComment(comment);
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
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, absences);
		} else {
			rollCall = lectureBlockRollCallDao.removeLecture(lectureBlock, rollCall, absences);
		}
		return rollCall;
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
			List<LectureBlock> blocks = lectureBlockDao.loadOpenBlocksBefore(endDate);
			for(LectureBlock block:blocks) {
				autoClose(block);
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private void autoClose(LectureBlock lectureBlock) {
		lectureBlock.setStatus(LectureBlockStatus.done);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.autoclosed);
		if(lectureBlock.getEffectiveLecturesNumber() < 0) {
			lectureBlock.setEffectiveLecturesNumber(lectureBlock.getPlannedLecturesNumber());
		}
		lectureBlock = lectureBlockDao.update(lectureBlock);
		
		List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(lectureBlock);
		Map<Identity,LectureBlockRollCall> rollCallMap = rollCalls.stream().collect(Collectors.toMap(r -> r.getIdentity(), r -> r));

		//TODO absence first admission ???
		List<ParticipantAndLectureSummary> participantsAndSummaries = lectureParticipantSummaryDao.getLectureParticipantSummaries(lectureBlock);
		Set<Identity> participants = new HashSet<>();
		for(ParticipantAndLectureSummary participantAndSummary:participantsAndSummaries) {
			if(participants.contains(participantAndSummary.getIdentity())) {
				continue;
			}
			if(participantAndSummary.getSummary() != null) {
				LectureBlockRollCall rollCall = rollCallMap.get(participantAndSummary.getIdentity());
				if(rollCall == null) {
					lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, participantAndSummary.getIdentity(), null, null, null, new ArrayList<>());
				} else if(rollCall.getLecturesAbsentList().isEmpty() && rollCall.getLecturesAttendedList().isEmpty()) {
					lectureBlockRollCallDao.addLecture(lectureBlock, rollCall, new ArrayList<>());
				}
			}
		}
	}

	@Override
	public void sendReminders() {
		boolean reminderEnabled = lectureModule.isRollCallReminderEnabled();
		int reminderPeriod = lectureModule.getRollCallReminderPeriod();
		if(reminderEnabled && reminderPeriod > 0) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -reminderPeriod);
			Date endDate = cal.getTime();
			
			List<LectureBlockToTeacher> toRemindList = lectureBlockReminderDao.getLectureBlockTeachersToReminder(endDate);
			for(LectureBlockToTeacher toRemind:toRemindList) {
				Identity teacher = toRemind.getTeacher();
				LectureBlock lectureBlock = toRemind.getLectureBlock();
				sendReminder(teacher, lectureBlock);
			}
		}
	}
	
	private void sendReminder(Identity teacher, LectureBlock lectureBlock) {
		RepositoryEntry entry = lectureBlock.getEntry();
		String language = teacher.getUser().getPreferences().getLanguage();
		Locale locale = i18nManager.getLocaleOrDefault(language);
		String startDate = Formatter.getInstance(locale).formatDate(lectureBlock.getStartDate());
		
		MailContext context = new MailContextImpl("[RepositoryEntry:" + entry.getKey() + "]");
		String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();
		String[] args = new String[]{
				lectureBlock.getTitle(),					//{0}
				startDate,									//{1}
				entry.getDisplayname(),						//{2}
				url,										//{3}
				userManager.getUserDisplayName(teacher) 	//{4}	
		};
		
		Translator trans = Util.createPackageTranslator(LectureAdminController.class, locale);
		String subject = trans.translate("lecture.teacher.reminder.subject", args);
		String body = trans.translate("lecture.teacher.reminder.body", args);

		LectureReminderTemplate template = new LectureReminderTemplate(subject, body);
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, teacher, template, null, null, result);
		MailerResult sendResult = mailManager.sendMessage(bundle);
		result.append(sendResult);

		String status;
		List<Identity> failedIdentities = result.getFailedIdentites();
		if(failedIdentities != null && failedIdentities.contains(teacher)) {
			status = "error";
		} else {
			status = "ok";
		}
		
		lectureBlockReminderDao.createReminder(lectureBlock, teacher, status);
	}

	@Override
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry) {
		return lectureBlockDao.loadByEntry(entry);
	}
	
	@Override
	public List<LectureBlock> getLectureBlocks(IdentityRef teacher) {
		return lectureBlockDao.loadByTeacher(teacher);
	}

	@Override
	public List<LectureBlockWithTeachers> getLectureBlocksWithTeachers(RepositoryEntryRef entry) {
		return lectureBlockDao.getLecturesBlockWithTeachers(entry);
	}

	/**
	 * 
	 */
	@Override
	public List<LectureBlockWithTeachers> getLectureBlocksWithTeachers(RepositoryEntryRef entry, IdentityRef teacher) {
		return lectureBlockDao.getLecturesBlockWithTeachers(entry, teacher);
	}

	@Override
	public List<Identity> getTeachers(LectureBlock lectureBlock) {
		LectureBlockImpl block = (LectureBlockImpl)lectureBlock;
		return groupDao.getMembers(block.getTeacherGroup(), "teacher");
	}
	
	@Override
	public List<Identity> getTeachers(RepositoryEntry entry) {
		return lectureBlockDao.getTeachers(entry);
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
	public List<LectureBlockStatistics> getParticipantLecturesStatistics(IdentityRef identity) {
		boolean calculateAttendanceRate = lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled();
		boolean absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		boolean countAuthorizedAbsenceAsAttendant = lectureModule.isCountAuthorizedAbsenceAsAttendant();
		double defaultRequiredAttendanceRate = lectureModule.getRequiredAttendanceRateDefault();
		return lectureBlockRollCallDao.getStatistics(identity,
				absenceDefaultAuthorized, countAuthorizedAbsenceAsAttendant,
				calculateAttendanceRate, defaultRequiredAttendanceRate);
	}

	@Override
	public List<LectureBlockStatistics> getParticipantsLecturesStatistics(RepositoryEntry entry) {
		boolean calculateAttendanceRate = lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled();
		boolean absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		boolean countAuthorizedAbsenceAsAttendant = lectureModule.isCountAuthorizedAbsenceAsAttendant();
		double defaultRequiredAttendanceRate = lectureModule.getRequiredAttendanceRateDefault();
		RepositoryEntryLectureConfiguration config = getRepositoryEntryLectureConfiguration(entry);
		return lectureBlockRollCallDao.getStatistics(entry, config,
				absenceDefaultAuthorized, countAuthorizedAbsenceAsAttendant,
				calculateAttendanceRate, defaultRequiredAttendanceRate);
	}

	@Override
	public List<LectureBlockAndRollCall> getParticipantLectureBlocks(RepositoryEntryRef entry, IdentityRef participant) {
		return lectureBlockRollCallDao.getParticipantLectureBlockAndRollCalls(entry, participant);
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
		
		if(ConfigurationHelper.isSyncParticipantCalendarEnabled(config, lectureModule)) {
			List<LectureBlock> blocks = getLectureBlocks(entry);
			for(LectureBlock block:blocks) {
				List<Identity> participants = getParticipants(block);
				syncInternalCalendar(block, participants);
			}
		} else {
			unsyncParticipantsCalendar(entry);
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
		
		if(ConfigurationHelper.isSyncParticipantCalendarEnabled(config, lectureModule)) {
			List<Identity> participants = getParticipants(lectureBlock);
			syncInternalCalendar(lectureBlock, participants);
		} else {
			List<Identity> participants = getParticipants(lectureBlock);
			unsyncInternalCalendar(lectureBlock, participants);
		}
	}
	
	private void syncInternalCalendar(LectureBlock lectureBlock, List<Identity> identities) {
		RepositoryEntry entry = lectureBlock.getEntry();
		String eventExternalId = generateExternalId(lectureBlock, entry);
		for(Identity identity:identities) {
			boolean updated = false;
			
			Kalendar cal = calendarMgr.getCalendar(CalendarManager.TYPE_USER, identity.getName());
			for(KalendarEvent event:cal.getEvents()) {
				if(eventExternalId.equals(event.getExternalId())) {
					if(updateEvent(lectureBlock, event)) {
						calendarMgr.updateEventFrom(cal, event);
					}
					updated = true;
					break;
				}
			}
			
			if(!updated) {
				KalendarEvent newEvent = createEvent(lectureBlock, entry);
				calendarMgr.addEventTo(cal, newEvent);
			}
			lectureParticipantSummaryDao.updateCalendarSynchronization(entry, identity);
		}
	}
	
	private void unsyncInternalCalendar(LectureBlock lectureBlock, List<Identity> identities) {
		RepositoryEntry entry = lectureBlock.getEntry();
		String externalId = generateExternalId(lectureBlock, entry);
		for(Identity identity:identities) {
			Kalendar cal = calendarMgr.getCalendar(CalendarManager.TYPE_USER, identity.getName());
			List<KalendarEvent> events = new ArrayList<>(cal.getEvents());
			for(KalendarEvent event:events) {
				if(externalId.equals(event.getExternalId())) {
					calendarMgr.removeEventFrom(cal, event);
				}
			}
			lectureParticipantSummaryDao.updateCalendarSynchronization(entry, identity);
		}
	}
	
	private void unsyncTeachersCalendar(RepositoryEntry entry) {
		List<Identity> teachers = getTeachers(entry);
		unsyncInternalCalendar(entry, teachers);
	}
	
	private void unsyncParticipantsCalendar(RepositoryEntry entry) {
		List<Identity> participants = getParticipants(entry);
		unsyncInternalCalendar(entry, participants);
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
		event.setDescription(lectureBlock.getDescription());
		event.setManagedFlags(CAL_MANAGED_FLAGS);
		return event;
	}
	
	private boolean updateEvent(LectureBlock lectureBlock, KalendarEvent event) {
		event.setSubject(lectureBlock.getTitle());
		event.setLocation(lectureBlock.getLocation());
		event.setDescription(lectureBlock.getDescription());
		event.setBegin(lectureBlock.getStartDate());
		event.setEnd(lectureBlock.getEndDate());
		event.setManagedFlags(CAL_MANAGED_FLAGS);
		return true;
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