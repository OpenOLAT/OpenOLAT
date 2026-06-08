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
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.manager.comparator.RecruitingAuditLogCreationDateComparator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAssignment;
import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.PublicFeedback;
import org.olat.modules.selectus.model.RecruitingAuditLog;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.RecruitingAuditLogLight;
import org.olat.modules.selectus.model.RecruitingAuditLogUserSettings;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.log.NotificationPermission;
import org.olat.modules.selectus.model.log.PositionNotificationsPermissions;
import org.olat.modules.selectus.model.log.RecruitingAuditLogSearchParameters;
import org.olat.modules.selectus.model.log.RecruitingAuditLogUserNotificationsImpl;
import org.olat.modules.selectus.model.position.PositionLightWithMembership;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewResponse;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.notifications.NotificationListController;

/**
 * 
 * Initial date: 17 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AuditServiceImpl implements AuditService {
	
	private static final Logger log = Tracing.createLoggerFor(AuditServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private RecruitingAuditLogDAO recruitingAuditLogDao;
	@Autowired
	private RecruitingAuditLogUserSettingsDAO userSettingsDao;
	@Autowired
	private RecruitingAuditLogReadDAO recruitingAuditLogReadDao;
	@Autowired
	private RecruitingAuditLogUserNotificationsDAO userNotificationsDao;

	@Override
	public RecruitingAuditLogUserSettings getOrCreateRecruitingAuditLogUserSettings(Identity identity) {
		RecruitingAuditLogUserSettings settings = userSettingsDao.findSettings(identity);
		if(settings == null) {
			settings = userSettingsDao.createAndPersist(identity);
			dbInstance.commit();
		}
		return settings;
	}

	@Override
	public RecruitingAuditLogUserSettings updateRecruitingAuditLogUserSettings(RecruitingAuditLogUserSettings settings) {
		return userSettingsDao.update(settings);
	}

	@Override
	public String toAuditXml(Position position) {
		return recruitingAuditLogDao.toXml(position);
	}

	@Override
	public String toAuditXml(PositionReviewDefinition reviewDefinition) {
		return recruitingAuditLogDao.toXml(reviewDefinition);
	}

	@Override
	public String toAuditXml(Application application) {
		return recruitingAuditLogDao.toXml(application);
	}

	@Override
	public String toAuditXml(Identity identity) {
		return recruitingAuditLogDao.toXml(identity);
	}

	@Override
	public String toAuditXml(Reference reference) {
		return recruitingAuditLogDao.toXml(reference);
	}

	@Override
	public String toAuditXml(List<ReviewResponse> responses) {
		return recruitingAuditLogDao.toXml(responses);
	}

	@Override
	public String toAuditXml(ApplicationComment comment) {
		return recruitingAuditLogDao.toXml(comment);
	}

	@Override
	public String toAuditXml(ApplicationAssignment assignment) {
		return recruitingAuditLogDao.toXml(assignment);
	}

	@Override
	public String toAuditXml(ApplicationAssignmentLight assignment) {
		return recruitingAuditLogDao.toXml(assignment);
	}

	@Override
	public String toAuditXml(PublicFeedback feedback) {
		return recruitingAuditLogDao.toXml(feedback);
	}

	@Override
	public String toAuditXml(ApplicationFeedback feedback) {
		return recruitingAuditLogDao.toXml(feedback);
	}

	@Override
	public void auditPositionLog(Action action, ActionTarget target, String before, String after,
			String messageI18n, String[] args, Translator translator, PositionRef position, Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, target, before, after, message, messageI18n, args,
				position, null, null, null, null, null, null, doer);
	}

	@Override
	public void auditApplicationLog(RecruitingAuditLog.Action action, RecruitingAuditLog.ActionTarget target,
			String before, String after, String messageI18n, String[] args, Translator translator,
			PositionRef position, ApplicationRef application, Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, target, before, after, message, messageI18n, args,
				position, application, null, null, null, null, null, doer);
	}
	
	@Override
	public void auditApplicationDecisionLog(RecruitingAuditLog.Action action, RecruitingAuditLog.ActionTarget target,
			Integer before, Integer after, String messageI18n, String[] args, Translator translator,
			PositionRef position, ApplicationRef application, Identity doer) {
		String message = translator.translate(messageI18n, args);
		
		String beforeStr = before == null ? null : before.toString();
		String afterStr = after == null ? null : after.toString();
		recruitingAuditLogDao.auditLog(action, target, beforeStr, afterStr, message, messageI18n, args,
				position, application, null, null, null, null, null, doer);
	}

	@Override
	public void auditCommitteeLog(Action action, ActionTarget target, String messageI18n, String[] args,
			Translator translator, PositionRef position, Identity member, Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, target, null, null, message, messageI18n, args,
				position, null, member, null, null, null, null, doer);
	}
	
	@Override
	public void auditCommitteeLog(Action action, ActionTarget target, String before, String after, String messageI18n,
			String[] args, Translator translator, PositionRef position, Identity member, Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, target, before, after, message, messageI18n, args,
				position, null, member, null, null, null, null, doer);
	}

	@Override
	public void auditRatingLog(Action action, ActionTarget target, String before, String after, String messageI18n,
			String[] args, Translator translator, PositionRef position, ApplicationRef application, UserRating rating, Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, target, before, after, message, messageI18n, args,
				position, application, null, rating, null, null, null, doer);
	}	

	@Override
	public void auditRefereeLog(Action action, ActionTarget target, String before, String after, String messageI18n,
			String[] args, Translator translator, PositionRef position, ApplicationRef application, Reference reference,
			Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, target, before, after, message, messageI18n, args,
				position, application, null, null, reference, null, null, doer);
	}
	
	@Override
	public void auditReviewLog(Action action, String before, String after, String messageI18n,
			String[] args, Translator translator, PositionRef position, ApplicationRef application, Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, ActionTarget.review, before, after, message, messageI18n, args,
				position, application, null, null, null, null, null, doer);
	}

	@Override
	public void auditCommentLog(Action action, String before, String after, String messageI18n,
			String[] args, Translator translator, PositionRef position, ApplicationRef application,
			ApplicationComment comment, Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, ActionTarget.comment, before, after, message, messageI18n, args,
				position, application, null, null, null, null, comment, doer);
	}

	@Override
	public void auditAssignmentLog(Action action, String before, String after, String messageI18n,
			String[] args, Translator translator, PositionRef position, ApplicationRef application, Identity member,
			Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, ActionTarget.assignment, before, after, message, messageI18n, args,
				position, application, member, null, null, null, null, doer);
	}

	@Override
	public void auditPublicFeedbackLog(Action action, String before, String after, String messageI18n, String[] args,
			Translator translator, PositionRef position, ApplicationRef application, Identity doer) {

		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, ActionTarget.publicFeedback, before, after, message, messageI18n, args,
				position, application, null, null, null, null, null, doer);
	}
	
	@Override
	public void auditPublicFeedbackLinkLog(Action action, String before, String after, String messageI18n, String[] args,
			Translator translator, PositionRef position, ApplicationRef application, Identity doer) {

		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, ActionTarget.publicFeedbackLink, before, after, message, messageI18n, args,
				position, application, null, null, null, null, null, doer);
	}

	@Override
	public void auditFeedbackLog(Action action, String before, String after, String messageI18n,
			String[] args, Translator translator, PositionRef position, ApplicationRef application,
			ApplicationFeedback feedback, Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, ActionTarget.memberFeedback, before, after, message, messageI18n, args,
				position, application, null, null, null, feedback, null, doer);
		
	}
	
	@Override
	public void auditFeedbackMemberLog(Action action, String before, String after, String messageI18n,
			String[] args, Translator translator, PositionRef position, ApplicationRef application,
			ApplicationFeedback feedback, Identity doer) {
		String message = translator.translate(messageI18n, args);
		recruitingAuditLogDao.auditLog(action, ActionTarget.memberFeedbackMgmt, before, after, message, messageI18n, args,
				position, application, null, null, null, feedback, null, doer);
	}

	@Override
	public int countLogs(Identity identity, Roles roles, RecruitingAuditLogSearchParameters params) {
		if(!enrichParametersWithPermissions(identity, roles, params)) {
			return 0;
		}
		return recruitingAuditLogDao.countPositionLogs(identity, params);
	}

	@Override
	public List<RecruitingAuditLog> getLogs(Identity identity, Roles roles, RecruitingAuditLogSearchParameters params) {
		if(!enrichParametersWithPermissions(identity, roles, params)) {
			return Collections.emptyList();
		}
		return recruitingAuditLogDao.getPositionLogs(identity, params);
	}
	
	@Override
	public List<RecruitingAuditLogLight> getLightLogs(Identity identity, Roles roles, RecruitingAuditLogSearchParameters params) {
		if(!enrichParametersWithPermissions(identity, roles, params)) {
			return Collections.emptyList();
		}
		return recruitingAuditLogDao.getPositionLightLogs(identity, params);
	}
	
	private boolean enrichParametersWithPermissions(Identity identity, Roles roles, RecruitingAuditLogSearchParameters params) {
		if(roles.isAdministrator()) {
			//can see all
		} else if(roles.isSelectusManager()) {
			ActionTarget[] targets = new ActionTarget[] {
					ActionTarget.position, ActionTarget.committee, ActionTarget.application,
					ActionTarget.review, ActionTarget.comment, ActionTarget.referee,
					ActionTarget.referenceLetter, ActionTarget.expert, ActionTarget.expertOpinion,
					ActionTarget.memberFeedback, ActionTarget.memberFeedbackMgmt,
					ActionTarget.publicFeedback, ActionTarget.publicFeedbackLink,
					ActionTarget.rating, ActionTarget.decision
				};
			params.setPermittedTargets(targets);
			if(!roles.isSelectusManager()) {
				params.setOrganisation(true);
			}
		} else {
			List<PositionStatus> allStatus = new ArrayList<>();
			for(PositionStatus status:PositionStatus.values()) {
				allStatus.add(status);
			}
			PositionStatusFilters filters = positionDao.getPositionStatusFilters(identity, roles, allStatus);
			List<PositionLightWithMembership> positions = positionDao.findPositionsLight(identity, filters, true);

			Map<PositionRole, PositionNotificationsPermissions> roleToPermissions = new EnumMap<>(PositionRole.class);
			for(PositionLightWithMembership position:positions) {
				PositionRole role = position.getRole();
				if(role != null) {
					PositionNotificationsPermissions permissions = roleToPermissions.computeIfAbsent(role, r -> {
						NotificationPermission[] permissionsFor = recruitingModule.getNotificationsPermissionsFor(r);
						return new PositionNotificationsPermissions(permissionsFor);
					});
					permissions.addPositionKey(position.getKey());
				}
			}

			// filter role without permissions or without positions
			List<PositionNotificationsPermissions> permittedPositions = new ArrayList<>(roleToPermissions.size());
			for(PositionNotificationsPermissions positionsPermissions:roleToPermissions.values()) {
				if(positionsPermissions.getPermissions() != null && positionsPermissions.getPermissions().length > 0
						&& !positionsPermissions.getPositionKeys().isEmpty()) {
					permittedPositions.add(positionsPermissions);
				}
			}
			
			if(permittedPositions.isEmpty()) {
				return false;
			}
			params.setPermittedPositions(permittedPositions);
		} 
		return true;
	}

	@Override
	public Set<Long> getReadAuditLogs(Identity identity) {
		return recruitingAuditLogReadDao.getRead(identity);
	}

	@Override
	public void markAsRead(List<Long> auditLogKeys, Identity identity) {
		Set<Long> read = recruitingAuditLogReadDao.getRead(identity);
		int count = 0;
		for(Long auditLogKey:auditLogKeys) {
			if(!read.contains(auditLogKey)) {
				RecruitingAuditLog auditLog = recruitingAuditLogDao.getReference(auditLogKey);
				recruitingAuditLogReadDao.create(identity, auditLog);
				if(++count % 25 == 0) {
					dbInstance.commitAndCloseSession();
				}
			}
		}
		dbInstance.commit();
	}

	@Override
	public void markAsUnread(List<Long> auditLogKeys, Identity identity) {
		Set<Long> read = recruitingAuditLogReadDao.getRead(identity);
		int count = 0;
		for(Long auditLogKey:auditLogKeys) {
			if(read.contains(auditLogKey)) {
				RecruitingAuditLog auditLog = recruitingAuditLogDao.getReference(auditLogKey);
				recruitingAuditLogReadDao.delete(identity, auditLog);
				if(++count % 25 == 0) {
					dbInstance.commit();
				}
			}
		}
		dbInstance.commit();
	}

	@Override
	public void sendNotifications() {
		List<RecruitingAuditLogUserSettings> usersSettings = userSettingsDao.findEnabledNotifications();
		List<RecruitingAuditLogUserNotificationsImpl> lastNotifications = userNotificationsDao.getUserNotifications();
		Map<Identity,RecruitingAuditLogUserNotificationsImpl> lastNotificationMap = lastNotifications.stream()
				.collect(Collectors.toMap(RecruitingAuditLogUserNotificationsImpl::getIdentity, ln -> ln, (u,v) -> u));
		
		for(RecruitingAuditLogUserSettings userSetting:usersSettings) {
			Identity identity = userSetting.getIdentity();
			RecruitingAuditLogUserNotificationsImpl lastNotification = lastNotificationMap.get(identity);
			if(checkSendNotificationsDate(userSetting, lastNotification)) {
				sendNotifications(identity,  lastNotification);
			}
		}
	}
	
	private boolean checkSendNotificationsDate(RecruitingAuditLogUserSettings userSetting, RecruitingAuditLogUserNotificationsImpl lastNotification) {
		boolean allOk = false;
		if(userSetting != null && userSetting.isEnabled() && NotificationIntervals.valid(userSetting.getInterval())) {
			NotificationIntervals interval =  NotificationIntervals.valueOf(userSetting.getInterval());
			if(interval == NotificationIntervals.never) {
				//
			} else if(lastNotification == null || lastNotification.getLastEmail() == null) {
				allOk = true;
			} else {
				Date lastSend = lastNotification.getLastEmail();
				Date compareDate = getCompareDateFromInterval(interval);
				if(lastSend.before(compareDate)) {
			  		allOk = true;
			  	}
			}
		}	
		return allOk;
	}

	private Date getCompareDateFromInterval(NotificationIntervals interval){
		Calendar calNow = Calendar.getInstance();
		// get hours to subtract from now
		int diffHours = interval.hours();
		calNow.add(Calendar.HOUR_OF_DAY, -diffHours);
		return calNow.getTime();	
	}
	
	private void sendNotifications(Identity identity,  RecruitingAuditLogUserNotificationsImpl lastNotification) {
		try {
			Roles roles = securityManager.getRoles(identity);
			RecruitingAuditLogSearchParameters params = new RecruitingAuditLogSearchParameters();
			params.setUnreadOnly(true);
			if(lastNotification != null && lastNotification.getLastEmail() != null) {
				params.setFrom(lastNotification.getLastEmail());
			}
			Map<Long,String> positionTitles = new HashMap<>();
			List<RecruitingAuditLog> logs = getLogs(identity, roles, params);
			if(!logs.isEmpty()) {
				Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
				
				Translator translator = Util.createPackageTranslator(NotificationListController.class, locale,
						Util.createPackageTranslator(PositionController.class, locale));
				String body = getBodyNotifications(logs, positionTitles, translator);
				String mail = identity.getUser().getProperty(UserConstants.EMAIL, locale);
				String subject = translator.translate("notifications.email.subject", translator.translate("topnav.home"));
				recruitingService.sendMail(mail, subject, body);
				
				if(lastNotification == null) {
					userNotificationsDao.create(identity, new Date());
				} else {
					lastNotification.setLastEmail(new Date());
					userNotificationsDao.update(lastNotification);
				}
				dbInstance.commit();
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private String getBodyNotifications(List<RecruitingAuditLog> auditLogs, Map<Long,String> positionTitles, Translator translator) {
		StringBuilder htmlText = new StringBuilder(10000);
		htmlText.append("<style>")
		        .append(".o_m_sub { background: #FAFAFA; padding: 5px 5px; margin: 10px 0; border-radius: 2px }")
		        .append(".o_m_go {padding: 5px 0 0 0 }")
		        .append(".o_date {font-size: 90%; color: #888 }")
		        .append(".o_m_footer {background: #FAFAFA; border: 1px solid #eee; border-radius: 5px; padding: 0 0.5em 0.5em 0.5em; margin: 1em 0 1em 0 }")
		        .append("</style>");
		
		if(auditLogs.size() > 1) {
			Collections.sort(auditLogs, new RecruitingAuditLogCreationDateComparator());
		}

		for (RecruitingAuditLog auditLog:auditLogs) {
			// o_m_wrap class for overriding styles in master mail template		
			htmlText.append("<div class='o_m_wrap'>");	 
			// add background here for gmail as they ignore classes. 
			htmlText.append("<div class='o_m_sub'>");			
			String businessPath = getBusinessPath(auditLog);
			if(StringHelper.containsNonWhitespace(businessPath) && StringHelper.containsNonWhitespace(auditLog.getMessage())) {
				String url = getNotificationsURLFromBusinessPath(businessPath);
				String msg = translator.translate(auditLog.getMessageI18n(), auditLog.getMessageValues());
				String notificationDate = Formatter.getInstance(translator.getLocale()).formatDateAndTime(auditLog.getCreationDate());
				String positionTitle = getPositionTitle(auditLog.getPositionKey(), positionTitles, translator.getLocale());
				
				htmlText.append("<div class='o_m_go'><a href='").append(url).append("'>").append(msg).append("</a> <span class='o_date'>");
				String note;
				if(auditLog.getIdentity() == null) {
					note = translator.translate("notifications.email.line.wrapper", notificationDate, positionTitle);
				} else {
					String actor = RecruitingHelper.formatFullNameWithTitle(auditLog.getIdentity(), translator.getLocale());
					note = translator.translate("notifications.email.line.wrapper.identity", actor, notificationDate, positionTitle);
				}
				htmlText.append(note).append("</span></div>");
			}
			htmlText.append("</div></div>");
		}
		String basePath =  Settings.getServerContextPathURI() + "/notifications/Positions/0";
		htmlText.append("<div class='o_m_footer'>");
		String installationName = translator.translate("topnav.home");
		htmlText.append(translator.translate("notifications.email.footer", basePath, installationName));
		htmlText.append("</div>");
		
		return htmlText.toString();
	}
	
	private String getPositionTitle(Long positionKey, Map<Long,String> positionTitles, Locale locale) {
		if(positionKey == null) return null;
		
		String positionTitle = positionTitles.get(positionKey);
		if(positionTitle == null) {
			Position pos = positionDao.loadPositionByKey(positionKey);
			if(pos != null) {
				positionTitle = pos.getMLTitle(locale);
			}
			if(!StringHelper.containsNonWhitespace(positionTitle)) {
				positionTitle = "-";
			}
			positionTitles.put(positionKey, positionTitle);
		}
		return positionTitle;
	}
	
	public String getNotificationsURLFromBusinessPath(String bPathString){
		try {
			List<ContextEntry> ceList = BusinessControlFactory.getInstance().createCEListFromString(bPathString);
			String busPath = BusinessControlFactory.getInstance().getBusinessPathAsURIFromCEList(ceList); 
			return Settings.getServerContextPathURI() + "/notifications/" + busPath;
		} catch(Exception e) {
			log.error("Error with business path: {}", bPathString, e);
			return null;
		}
	}
	
	public String getBusinessPath(RecruitingAuditLog row) {
		StringBuilder sb = new StringBuilder();
		sb.append("[Positions:0][Position:").append(row.getPositionKey()).append("]");
		if(row.getApplicationKey() != null) {
			if(row.getApplicationKey() != null) {
				sb.append("[Application:").append(row.getApplicationKey()).append("]");
			}
			
			if(row.getCommentKey() != null) {
				sb.append("[Comment:").append(row.getCommentKey()).append("]");
			} else if(ActionTarget.review.equals(row.getTargetEnum())) {
				sb.append("[Review:").append(row.getIdentity().getKey()).append("]");
			} else if(ActionTarget.committee.equals(row.getTargetEnum())) {
				sb.append("[Committee:0]");
			}
		} else if(ActionTarget.committee.equals(row.getTargetEnum())) {
			sb.append("[Committee:0]");
		} else if(ActionTarget.position.equals(row.getTargetEnum())
				&& (Action.changeConfiguration.equals(row.getActionEnum()) || Action.changeStatus.equals(row.getActionEnum()))) {
			sb.append("[Details:0]");
		} else {
			sb.append("[Applications:0]");
		}
		
		return sb.toString();
	}
}
