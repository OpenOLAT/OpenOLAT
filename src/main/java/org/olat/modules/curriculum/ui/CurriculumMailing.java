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
package org.olat.modules.curriculum.ui;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.ui.member.ConfirmationByEnum;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 7 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumMailing {
	
	public static MailTemplate getStatusConfirmedMailTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.status.confirmed.subject";
		String bodyKey = "notification.mail.status.confirmed.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	public static MailTemplate getStatusCancelledMailTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.status.cancelled.subject";
		String bodyKey = "notification.mail.status.cancelled.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	public static MailTemplate getMembershipByStatusTemplate(GroupMembershipStatus nextStatus,
			Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		if(nextStatus == null) {
			return CurriculumMailing.getMembershipChangedTemplate(curriculum, curriculumElement, actor);
		}
		return switch(nextStatus) {
			case booking, parentBooking, reservation -> getMembershipBookedByAdminTemplate(curriculum, curriculumElement, actor);
			case active -> getMembershipAcceptedTemplate(curriculum, curriculumElement, actor);
			case declined -> getMembershipDeclinedTemplate(curriculum, curriculumElement, actor);
			case cancel, cancelWithFee -> getMembershipCancelledTemplate(curriculum, curriculumElement, Map.of(), actor);
			case removed -> getMembershipRemovedTemplate(curriculum, curriculumElement, actor);
			default -> CurriculumMailing.getMembershipChangedTemplate(curriculum, curriculumElement, actor);
		};
	}

	public static MailTemplate findBestMailTemplate(List<CurriculumElementMembershipChange> changes, Identity doer) {
		if(changes == null || changes.isEmpty()) return null;
		
		if(changes.size() == 1) {
			return findBestMailTemplate(changes.get(0), doer);
		}
		
		Set<GroupMembershipStatus> nextStatus = new HashSet<>();
		Set<CurriculumElement> curriculumElements = new HashSet<>();
		for(CurriculumElementMembershipChange change:changes) {
			nextStatus.addAll(change.getNextStatusList());
			curriculumElements.add(change.getCurriculumElement());
		}
		
		CurriculumElementMembershipChange firstChange = changes.get(0);
		Curriculum curriculum = firstChange.getCurriculumElement().getCurriculum();
		CurriculumElement curriculumElement = curriculumElements.size() == 1
				? curriculumElements.iterator().next()
				: null;
		if(nextStatus.size() == 1) {
			return getMembershipByStatusTemplate(nextStatus.iterator().next(), curriculum, curriculumElement, doer);
		}

		return getMembershipChangedTemplate(curriculum, curriculumElement, doer);
	}
	
	public static MailTemplate findBestMailTemplate(CurriculumElementMembershipChange change, Identity doer) {
		CurriculumElement curriculumElement = change.getCurriculumElement();
		Curriculum curriculum = curriculumElement.getCurriculum();
		
		GroupMembershipStatus nextStatus = change.getNextStatus(CurriculumRoles.participant);
		if(nextStatus != null) {
			if(nextStatus == GroupMembershipStatus.reservation && change.getMember().equals(doer)) {
				if(change.getConfirmationBy() == ConfirmationByEnum.ADMINISTRATIVE_ROLE) {
					return getMembershipBookedByParticipantNeedConfirmationTemplate(curriculum, curriculumElement, doer);
				}
				return getMembershipBookedByParticipantTemplate(curriculum, curriculumElement, doer);
			}
			if(nextStatus == GroupMembershipStatus.active) {
				if (change.getMember().equals(doer)) {
					return getMembershipBookedByParticipantTemplate(curriculum, curriculumElement, doer);
				} else {
					return getMembershipBookedByAdminTemplate(curriculum, curriculumElement, doer);
				}
			}
			return getMembershipByStatusTemplate(nextStatus, curriculum, curriculumElement, doer);
		}
		return null;
	}
	
	/**
	 * 1.3. BOOKING BY ADMINISTRATIVE ROLE - WITHOUT CONFIRMATION BY ADMINISTRATIVE ROLES (OFFER SETTING)
	 * 
	 * @see <a href="https://track.frentix.com/issue/OO-8389">OO-8389</a>
	 * 
	 * @param curriculum The curriculum
	 * @param curriculumElement The curriculum element
	 * @param actor The user which send the mail
	 * @return A template
	 */
	public static MailTemplate getMembershipBookedByAdminTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.booked.by.admin.subject";
		String bodyKey = "notification.mail.member.booked.by.admin.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	/**
	 * 1.4. BOOKING BY ADMINISTRATIVE ROLE - WITH CONFIRMATION BY ADMINISTRATIVE ROLES (OFFER SETTING)
	 * 
	 * @see <a href="https://track.frentix.com/issue/OO-8389">OO-8389</a>
	 * 
	 * @param curriculum The curriculum
	 * @param curriculumElement The curriculum element
	 * @param actor The user which send the mail
	 * @return A template
	 */
	public static MailTemplate getMembershipBookedByAdminTemplateConfirmation(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.booked.by.admin.confirm.subject";
		String bodyKey = "notification.mail.member.booked.by.admin.confirm.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	public static MailTemplate getMembershipBookedByParticipantTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.booked.by.participant.subject";
		String bodyKey = "notification.mail.member.booked.by.participant.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	public static MailTemplate getMembershipBookedByParticipantNeedConfirmationTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.booked.by.participant.need.confirmation.subject";
		String bodyKey = "notification.mail.member.booked.by.participant.need.confirmation.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	public static MailTemplate getMembershipAcceptedTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.accepted.subject";
		String bodyKey = "notification.mail.member.accepted.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	/**
	 * 2.1 INVITED - STANDARD (WITHOUT CONFIMRATION)<br>
	 * Administrator add a member without confirmation
	 * 
	 * @see <a href="https://track.frentix.com/issue/OO-8389">OO-8389</a>
	 * 
	 * @param curriculum The curriculum
	 * @param curriculumElement The curriculum element
	 * @param actor The user which send the mail
	 * @return A template
	 */
	public static MailTemplate getMembershipAddTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.invited.subject";
		String bodyKey = "notification.mail.member.invited.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	/**
	 * 2.2 INVITED - WITH CONFIRMATION BY ADMINISTRATIVE ROLES
	 * 
	 * @see <a href="https://track.frentix.com/issue/OO-8389">OO-8389</a>
	 * 
	 * Administrator add a member with confirmation by an administrative roles
	 * 
	 * @param curriculum The curriculum
	 * @param curriculumElement The curriculum element
	 * @param actor The user which send the mail
	 * @return A template
	 */
	public static MailTemplate getMembershipAddWithAdminConfirmationTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.invited.confirm.admin.subject";
		String bodyKey = "notification.mail.member.invited.confirm.admin.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	/**
	 * 2.3. INVITED - WITH CONFIRMATION BY PARTICIPANT
	 * 
	 * @see <a href="https://track.frentix.com/issue/OO-8389">OO-8389</a>
	 * 
	 * @param curriculum The curriculum
	 * @param curriculumElement The curriculum element
	 * @param actor The user which send the mail
	 * @return A template
	 */
	public static MailTemplate getMembershipAddWithParticipantConfirmationTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.invited.confirm.participant.subject";
		String bodyKey = "notification.mail.member.invited.confirm.participant.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	public static MailTemplate getMembershipDeclinedTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.declined.subject";
		String bodyKey = "notification.mail.member.declined.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	public static MailTemplate getMembershipCancelledByParticipantTemplate(Curriculum curriculum, CurriculumElement curriculumElement,
			Map<Long,Price> cancellationFees, Identity actor) {
		String subjectKey = "notification.mail.member.cancelled.by.participant.subject";
		String bodyKey = "notification.mail.member.cancelled.by.participant.body";
		CurriculumMailTemplate template = createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
		template.setCancellationFees(cancellationFees);
		return template;
	}
	
	public static MailTemplate getMembershipCancelledTemplate(Curriculum curriculum, CurriculumElement curriculumElement,
			Map<Long,Price> cancellationFees, Identity actor) {
		String subjectKey = "notification.mail.member.cancelled.subject";
		String bodyKey = "notification.mail.member.cancelled.body";
		CurriculumMailTemplate template = createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
		template.setCancellationFees(cancellationFees);
		return template;
	}
	
	public static MailTemplate getMembershipRemovedTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.removed.subject";
		String bodyKey = "notification.mail.member.removed.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	/**
	 * 6.3 REMOVED BY ADMINISTRATIVE ROLE
	 * 
	 * @see <a href="https://track.frentix.com/issue/OO-8389">OO-8389</a>
	 * 
	 * @param curriculum The curriculum
	 * @param curriculumElement The curriculum element
	 * @param actor The user which send the mail
	 * @return A template
	 */
	public static MailTemplate getMembershipRemovedByAdminTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.removed.by.admin.subject";
		String bodyKey = "notification.mail.member.removed.by.admin.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	public static MailTemplate getMembershipChangedTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.member.changed.subject";
		String bodyKey = "notification.mail.member.changed.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}

	/**
	 * The mail template when adding owner to a course.
	 * 
	 * @param re
	 * @param actor
	 * @return the generated MailTemplate
	 */
	public static MailTemplate getDefaultMailTemplate(Curriculum curriculum, CurriculumElement curriculumElement, Identity actor) {
		String subjectKey = "notification.mail.added.subject";
		String bodyKey = "notification.mail.added.body";
		return createMailTemplate(curriculum, curriculumElement, actor, subjectKey, bodyKey);
	}
	
	private static CurriculumMailTemplate createMailTemplate(Curriculum curriculum, CurriculumElement curriculumElement,
			Identity actor, String subjectKey, String bodyKey) {
		// Get some data about the actor and fetch the translated subject / body via i18n module
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(actor.getUser().getPreferences().getLanguage());
		String[] bodyArgs = new String[] {
				actor.getUser().getProperty(UserConstants.FIRSTNAME, null),		// 0
				actor.getUser().getProperty(UserConstants.LASTNAME, null),		// 1
				UserManager.getInstance().getUserDisplayEmail(actor, locale),	// 2
				UserManager.getInstance().getUserDisplayEmail(actor, locale),	// 3 (2x for compatibility with old i18n properties)
				Formatter.getInstance(locale).formatDate(new Date())			// 4
			};
		
		Translator trans = Util.createPackageTranslator(CurriculumMailing.class, locale);
		String subject = trans.translate(subjectKey);
		String body = trans.translate(bodyKey, bodyArgs);
		
		// Create a mail template which all these data
		return new CurriculumMailTemplate(curriculum, curriculumElement, subject, body, locale);
	}
	
	public static void sendEmail(Identity ureqIdentity, Identity identity, Curriculum curriculum, CurriculumElement curriculumElement, MailPackage mailing) {
		if(mailing == null || !mailing.isSendEmail()) {
			return;
		}

		MailTemplate template = mailing.getTemplate();
		if(template == null) {
			template = getDefaultMailTemplate(curriculum, curriculumElement, ureqIdentity);
		}
		
		MailContext context = mailing.getContext();
		if(context == null) {
			context = new MailContextImpl(null, null, "[MyCoursesSite:0][Curriculum:0][Curriculum:" + curriculum.getKey() + "]");
		}
		
		MailerResult result = new MailerResult();
		String metaId = mailing.getUuid();
		MailManager mailService = CoreSpringFactory.getImpl(MailManager.class);
		MailBundle bundle = mailService.makeMailBundle(context, identity, template, ureqIdentity, metaId, result);
		if(bundle != null) {
			mailService.sendMessage(bundle);
		}
		mailing.appendResult(result);
	}
	
	public static class CurriculumMailTemplate extends MailTemplate {
		
		private static final String LOGIN = "login";
		private static final String CURRICULUM_NAME = "curriculumName";
		private static final String CURRICULUM_DESCRIPTION = "curriculumDescription";
		private static final String CURRICULUM_URL = "curriculumUrl";
		private static final String CURRICULUM_ELEMENT_NAME = "curriculumElementName";
		private static final String CURRICULUM_ELEMENT_DESCRIPTION = "curriculumElementDescription";
		private static final String CURRICULUM_ELEMENT_IDENTIFIER = "curriculumElementIdentifier";
		private static final String CURRICULUM_ELEMENT_TYPE_NAME = "curriculumElementTypeName";
		private static final String MY_COURSES_URL = "myCoursesUrl";
		private static final String FEE = "fee";
		
		private final Locale locale;
		private final Curriculum curriculum;
		private final CurriculumElement curriculumElement;
		
		private Map<Long,Price> cancellationFees;
		
		public CurriculumMailTemplate(Curriculum curriculum, CurriculumElement curriculumElement,
				String subject, String body, Locale locale) {
			super(subject, body, null);
			this.locale = locale;
			this.curriculum = curriculum;
			this.curriculumElement = curriculumElement;
		}
		
		public Map<Long, Price> getCancellationFees() {
			return cancellationFees;
		}

		public void setCancellationFees(Map<Long, Price> cancellationFees) {
			this.cancellationFees = cancellationFees;
		}

		@Override
		public Collection<String> getVariableNames() {
			Set<String> variableNames = new HashSet<>();
			variableNames.addAll(getStandardIdentityVariableNames());
			variableNames.add(LOGIN);
			variableNames.add(CURRICULUM_NAME);
			variableNames.add(CURRICULUM_DESCRIPTION);
			variableNames.add(CURRICULUM_URL);
			variableNames.add(CURRICULUM_ELEMENT_NAME);
			variableNames.add(CURRICULUM_ELEMENT_DESCRIPTION);
			variableNames.add(CURRICULUM_ELEMENT_IDENTIFIER);
			variableNames.add(CURRICULUM_ELEMENT_TYPE_NAME);
			variableNames.add(FEE);
			variableNames.add(MY_COURSES_URL);
			return variableNames;
		}

		@Override
		public void putVariablesInMailContext(VelocityContext context, Identity identity) {
			// build learning resources as list of url as string
			final String curriculumName = curriculum.getDisplayName();
			final String curriculumDescription = (StringHelper.containsNonWhitespace(curriculum.getDescription())
					? FilterFactory.getHtmlTagAndDescapingFilter().filter(curriculum.getDescription()) : ""); 
			final String curriculumUrl = Settings.getServerContextPathURI() + "/url/MyCoursesSite/0/Curriculum/0/Curriculum/" + curriculum.getKey();
			final String myCoursesUrl = Settings.getServerContextPathURI() + "/url/MyCoursesSite/0";
			
			final String curriculumElementName;
			final String curriculumElementDescription;
			final String curriculumElementIdentifier;
			final String curriculumElementTypeName;
			if(curriculumElement == null) {
				curriculumElementName = "";
				curriculumElementDescription = "";
				curriculumElementIdentifier = "";
				curriculumElementTypeName = "";
			} else {
				curriculumElementName = curriculumElement.getDisplayName();
				curriculumElementDescription = (StringHelper.containsNonWhitespace(curriculumElement.getDescription())
						? FilterFactory.getHtmlTagAndDescapingFilter().filter(curriculumElement.getDescription()) : ""); 
				curriculumElementIdentifier = curriculumElement.getIdentifier();
				curriculumElementTypeName = curriculumElement.getType() == null ? null : curriculumElement.getType().getDisplayName();
			}
			
			// Put user variables into velocity context
			fillContextWithStandardIdentityValues(context, identity, locale);
			if(identity != null) {
				User user = identity.getUser();
				context.put(LOGIN, UserManager.getInstance().getUserDisplayEmail(user, locale));
			}
			// Put variables from greater context
			putVariablesInMailContext(context, CURRICULUM_NAME, curriculumName);
			putVariablesInMailContext(context, CURRICULUM_DESCRIPTION, curriculumDescription);
			putVariablesInMailContext(context, CURRICULUM_URL, curriculumUrl);
			putVariablesInMailContext(context, MY_COURSES_URL, myCoursesUrl);
			
			putVariablesInMailContext(context, CURRICULUM_ELEMENT_NAME, curriculumElementName);
			putVariablesInMailContext(context, CURRICULUM_ELEMENT_DESCRIPTION, curriculumElementDescription);
			putVariablesInMailContext(context, CURRICULUM_ELEMENT_IDENTIFIER, curriculumElementIdentifier);
			putVariablesInMailContext(context, CURRICULUM_ELEMENT_TYPE_NAME, curriculumElementTypeName);
			// Backwards compatibility
			putVariablesInMailContext(context, "curriculumTypeName", curriculumElementTypeName);
			
			String feeStr = "-";
			if(identity != null && cancellationFees != null
					&& cancellationFees.containsKey(identity.getKey())) {
				Price fee = cancellationFees.get(identity.getKey());
				feeStr = PriceFormat.fullFormat(fee);
			}
			putVariablesInMailContext(context, FEE, feeStr);
		}
	}
}
