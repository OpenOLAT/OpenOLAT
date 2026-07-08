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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Send reminder email to committee member (with rating role)
 * which doesn't have a single rating row. 
 * 
 * Initial date: 19 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CommitteeReminderSender {
	
	private static final Logger log = Tracing.createLoggerFor(CommitteeReminderSender.class);
	
	@Autowired
	private MailService mailService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public void sendCommitteeReminder() {
		List<Position> positions = recruitingService.getPositionsToRemind();
		for(Position position:positions) {
			PositionRole[] roles = recruitingModule.getRolesAllowedToRate();
			List<Identity> committee = recruitingService.getCommittee(position, roles);
			Map<Long, Identity> committeeMap = committee.stream().collect(Collectors.toMap(Identity::getKey,  i -> i, (u, v) -> u));
			List<UserRating> ratings = recruitingService.getRatings(position, committee);
			for(UserRating rating:ratings) {
				committeeMap.remove(rating.getCreator().getKey());
			}
			if(!committeeMap.isEmpty()) {
				sendCommitteeReminder(position, committeeMap.values());
			}
			
			position = recruitingService.getPosition(position.getKey());
			position.setCommitteeReminderSentDate(new Date());
			recruitingService.savePosition(position);
			DBFactory.getInstance().commit();
		}
	}
	
	private void sendCommitteeReminder(Position position, Collection<Identity> membersToRemind) {
		Locale defaultPositionLocale = recruitingModule.getPositionDefaultLocale();
		Translator translator = Util.createPackageTranslator(RecruitingHelper.class, defaultPositionLocale);

		OrganisationUnit organisationSettings = recruitingService.getOrganisationUnit(position);
		String staffMail = recruitingModule.getStaffMail(position, organisationSettings);

		String headLastname = null;
		String headFirstname = null;
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		if(headOfCommittee != null) {
			headLastname = headOfCommittee.getUser().getProperty(UserConstants.LASTNAME, defaultPositionLocale);
			headFirstname = headOfCommittee.getUser().getProperty(UserConstants.FIRSTNAME, defaultPositionLocale);
		}
		
		Identity secretary = recruitingService.getSecretary(position);
		
		long days;
		String ratingDeadline = "";
		String ratingDeadlineDe = "";
		if(position.getRatingDeadline() != null) {
			long diffInMillies = position.getRatingDeadline().getTime() - new Date().getTime();
			days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
			ratingDeadline = DateCellRenderer.format(position.getRatingDeadline());
			ratingDeadlineDe = DateCellRenderer.format(position.getRatingDeadline(), Locale.GERMAN);
		} else {
			days = 2l;
		}
		
		for(Identity memberToRemind:membersToRemind) {
			String memberLastname = memberToRemind.getUser().getProperty(UserConstants.LASTNAME, defaultPositionLocale);
			String memberFirstname = memberToRemind.getUser().getProperty(UserConstants.FIRSTNAME, defaultPositionLocale);
			String[] args = new String[]{
				position.getMLTitle(defaultPositionLocale), // 0
				staffMail,									// 1
				headLastname,								// 2
				headFirstname,								// 3
				memberLastname,								// 4
				memberFirstname,							// 5
				Long.toString(days),						// 6
				ratingDeadline,								// 7
				ratingDeadlineDe							// 8
			};
			
			String subject = position.getCommitteeReminderMailSubject();
			if(!StringHelper.containsNonWhitespace(subject)) {
				subject = translator.translate("reminder.committee.member.subject", args);
			}
			String body = position.getCommitteeReminderMailTemplate();
			if(!StringHelper.containsNonWhitespace(body)) {
				body = translator.translate("reminder.committee.member.body", args);
			}
			MailAttachment letter = mailService.toAttachment(position.getCommitteeReminderMailLetter(), null, defaultPositionLocale);

			ApplicationMailTemplate template = new RecruitingMailTemplate(null, null, null, subject, body, letter,
					headOfCommittee, secretary, new SubjectAndBody(subject, body, letter), salutationGenerator, translator);
			recruitingService.sendReminder(position, memberToRemind, template);
			log.info(Tracing.M_AUDIT, "Send reminder to {} for {}", memberToRemind, position);
			
			String messageI18n = "audit.log.position.committee.reminder";
			String[] messageArgs = new String[] { position.getMLTitle(defaultPositionLocale), memberLastname, memberFirstname };
			auditService.auditPositionLog(Action.committeeReminder, ActionTarget.position, null, null,
					messageI18n, messageArgs, translator, position, null);
		}
	}
	
	public static String getMailTemplateBody(Locale locale) {
		Translator translator = Util.createPackageTranslator(RecruitingHelper.class, locale);
		String[] args = generateVariablesArguments();
		return translator.translate("reminder.committee.member.body", args);
	}
	
	public static String getMailTemplateSubject(Locale locale) {
		Translator translator = Util.createPackageTranslator(RecruitingHelper.class, locale);
		String[] args = generateVariablesArguments();
		return translator.translate("reminder.committee.member.subject", args);
	}
	
	public static String[] generateVariablesArguments() {
		return new String[]{
				"$" + RecruitingMailTemplate.POSITION_TITLE, 				// 0
				"$" + RecruitingMailTemplate.ORG_UNIT_MAIL,					// 1
				"$" + RecruitingMailTemplate.HEAD_LAST_NAME,				// 2
				"$" + RecruitingMailTemplate.HEAD_FIRST_NAME,				// 3
				"$" + RecruitingMailTemplate.COMMITTEE_MEMBER_LAST_NAME,	// 4
				"$" + RecruitingMailTemplate.COMMITTEE_MEMBER_FIRST_NAME,	// 5
				"$" + RecruitingMailTemplate.RATING_DEADLINE_DAYS,			// 6
				"$" + RecruitingMailTemplate.RATING_DEADLINE,				// 7
				"$" + RecruitingMailTemplate.RATING_DEADLINE_DE				// 8
			};
	}
}
