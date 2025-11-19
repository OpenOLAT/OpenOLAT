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
package org.olat.modules.certificationprogram.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.ui.CertificationHelper;
import org.olat.modules.certificationprogram.ui.CertificationProgramNotificationRow;
import org.olat.modules.certificationprogram.ui.CertificationProgramNotificationsController;
import org.olat.modules.creditpoint.CreditPointWallet;

/**
 * 
 * Initial date: 14 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramMailing {
	
	public static I18nKeys getCustomI18nKeys(CertificationProgramNotificationRow row) {
		String suffix = row.getI18nSuffix();
		String subjectKey = getCustomI18nMailSubject(suffix);
		String bodyKey = getCustomI18nMailBody(suffix);
		return new I18nKeys(subjectKey, bodyKey);
	}
	
	public static I18nKeys getI18nKeys(CertificationProgramMailConfiguration configuration, boolean withCreditPoints) {
		I18nKeys keys;
		if(configuration.isCustomized()) {
			String suffix = configuration.getI18nSuffix();
			String subjectKey = getCustomI18nMailSubject(suffix);
			String bodyKey = getCustomI18nMailBody(suffix);
			return new I18nKeys(subjectKey, bodyKey);
		} else {
			keys = getDefaultI18nKeys(configuration.getType(), withCreditPoints);
		}
		return keys;
	}

	public static String getCustomI18nMailBody(String suffix) {
		return "mail.body." + suffix;
	}
	
	public static String getCustomI18nMailSubject(String suffix) {
		return "mail.subject." + suffix;
	}
	
	public static I18nKeys getDefaultI18nKeys(CertificationProgramMailType type, boolean withCreditPoints) {
		if(type == null) return null;
		
		String creditPointSuffix = withCreditPoints ? ".cp" : "";
		
		return switch(type) {
			case certificate_issued -> new I18nKeys("mail.certificate_issued.subject", "mail.certificate_issued.body");
			case certificate_renewed -> new I18nKeys("mail.certificate_renewed.subject", "mail.certificate_renewed.body");
			case certificate_expired -> new I18nKeys("mail.certificate_expired.subject", "mail.certificate_expired.body");
			case certificate_revoked -> new I18nKeys("mail.certificate_revoked.subject", "mail.certificate_revoked.body");
			case program_removed -> new I18nKeys("mail.program_removed.subject", "mail.program_removed.body");
			case reminder_overdue -> new I18nKeys("mail.reminder_overdue.subject" + creditPointSuffix, "mail.reminder_overdue.body" + creditPointSuffix);
			case reminder_upcoming -> new I18nKeys("mail.reminder_upcoming.subject" + creditPointSuffix, "mail.reminder_upcoming.body" + creditPointSuffix);
			default -> null;
		};
	}

	public static MailTemplate getTemplate(CertificationProgram program, CertificationProgramMailConfiguration configuration,
			Identity recipient, Certificate certificate, File certificateFile, CreditPointWallet wallet, Identity actor) {
		if(configuration == null) return null;
		
		I18nKeys keys = getI18nKeys(configuration, program.hasCreditPoints());
		return createMailTemplate(program, recipient, certificate, certificateFile, wallet, actor, keys.subject(), keys.body());
	}
	
	public static MailTemplate createMailTemplate(CertificationProgram program, Identity recipient,
			Certificate certificate, File certificateFile, CreditPointWallet wallet, Identity actor, String subjectKey, String bodyKey) {
		// get some data about the actor and fetch the translated subject / body via i18n module
		String lang = null;
		if (recipient != null) {
			lang = recipient.getUser().getPreferences().getLanguage();
		}
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(lang);
		return createMailTemplate(program, recipient, certificate, certificateFile, wallet, actor, subjectKey, bodyKey, locale);
	}
	
	private static MailTemplate createMailTemplate(CertificationProgram program, Identity identity, Certificate certificate,
			File certificateFile, CreditPointWallet wallet, Identity actor, String subjectKey, String bodyKey, Locale locale) {
	
		Translator trans = Util.createPackageTranslator(CertificationProgramNotificationsController.class, locale);
		String subject = trans.translate(subjectKey);
		String body = trans.translate(bodyKey);
		return new CPMailTemplate(subject, body, certificateFile, program, identity, certificate, wallet, actor, trans);
	}
	
	public record I18nKeys(String subject, String body) {
		//
	}
	
	public static class CPMailTemplate extends MailTemplate {
		
		private static final String CERTIFICATION_PROGRAM_TITLE = "certificateProgramTitle";
		private static final String CERTIFICATION_PROGRAM_REF = "certificateProgramRef";
		private static final String ACTOR_EMAIL = "actorEmail";
		private static final String ACTOR_FIRSTNAME = "actorFirstname";
		private static final String ACTOR_LASTNAME = "actorLastname";
		private static final String CERTIFICATE_URL = "certificatesURL";
		private static final String REQUIRED_CREDIT_POINTS = "requiredCreditpoints";
		private static final String EXPIRATION_DATE = "expirationDate";
		private static final String REMOVAL_DATE = "removalDate";
		private static final String REVOCATION_DATE = "revocationDate";
		private static final String RECERTIFICATION_DEADLINE = "recertificationDeadline";
		private static final String ACCOUNT_BALANCE = "accountBalance";
		
		private final Identity actor;
		private final Identity identity;
		private final Translator translator;
		private final Certificate certificate;
		private final CreditPointWallet wallet;
		private final CertificationProgram certificationProgram;
		
		public CPMailTemplate(String subject, String body, File certificateFile, CertificationProgram certificationProgram, Identity identity,
				Certificate certificate, CreditPointWallet wallet, Identity actor, Translator translator) {
			super(subject, body, certificateFile == null ? null : new File[] { certificateFile });
			this.actor = actor;
			this.wallet = wallet;
			this.identity = identity;
			this.translator = translator;
			this.certificate = certificate;
			this.certificationProgram = certificationProgram;
		}
		
		public static final Collection<String> variableNames() {
			List<String> variableNames = new ArrayList<>(getStandardIdentityVariableNames());
			variableNames.add(CERTIFICATION_PROGRAM_TITLE);
			variableNames.add(CERTIFICATION_PROGRAM_REF);
			variableNames.add(CERTIFICATE_URL);
			variableNames.add(REQUIRED_CREDIT_POINTS);
			variableNames.add(EXPIRATION_DATE);
			variableNames.add(REMOVAL_DATE);
			variableNames.add(REVOCATION_DATE);
			variableNames.add(RECERTIFICATION_DEADLINE);
			variableNames.add(ACTOR_EMAIL);
			variableNames.add(ACTOR_FIRSTNAME);
			variableNames.add(ACTOR_LASTNAME);
			variableNames.add(ACCOUNT_BALANCE);
			return variableNames;
		}

		@Override
		public void putVariablesInMailContext(Identity recipient) {
			final Locale locale = translator.getLocale();
			final Formatter formatter = Formatter.getInstance(locale);
			fillContextWithStandardIdentityValues(identity, locale);
			
			if(certificate != null) {
				String url = DownloadCertificateCellRenderer.getUrl(certificate);
				putVariablesInMailContext(CERTIFICATE_URL, url);
				
				if(certificate.getNextRecertificationDate() != null) {
					putVariablesInMailContext(EXPIRATION_DATE, formatter.formatDate(certificate.getNextRecertificationDate()));
				}
				if(certificate.getRecertificationWindowDate() != null) {
					putVariablesInMailContext(RECERTIFICATION_DEADLINE, formatter.formatDate(certificate.getRecertificationWindowDate()));
				}
				if(certificate.getRevocationDate() != null) {
					putVariablesInMailContext(REVOCATION_DATE, formatter.formatDate(certificate.getRevocationDate()));
				}
				if(certificate.getRemovalDate() != null) {
					putVariablesInMailContext(REMOVAL_DATE, formatter.formatDate(certificate.getRemovalDate()));
				}
			} else {
				String url = Settings.getServerContextPath() + "/HomeSite/" + identity.getKey() + "/certificates/0/All/0";
				putVariablesInMailContext(CERTIFICATE_URL, url);
			}
			
			if(certificationProgram != null) {
				putVariablesInMailContext(CERTIFICATION_PROGRAM_TITLE, certificationProgram.getDisplayName());
				putVariablesInMailContext(CERTIFICATION_PROGRAM_REF, certificationProgram.getIdentifier());
				
				if(certificationProgram.getCreditPointSystem() != null) {
					String requiredCreditPoints = CertificationHelper.creditPointsToString(certificationProgram);
					putVariablesInMailContext(REQUIRED_CREDIT_POINTS, requiredCreditPoints);
				}
			}
			
			if(wallet != null) {
				String balance = CertificationHelper.creditPointsToString(wallet.getBalance(), certificationProgram.getCreditPointSystem());
				putVariablesInMailContext(ACCOUNT_BALANCE, balance);
			}
			
			if(actor != null) {
				putVariablesInMailContext(ACTOR_EMAIL, actor.getUser().getEmail());
				putVariablesInMailContext(ACTOR_FIRSTNAME, actor.getUser().getFirstName());
				putVariablesInMailContext(ACTOR_LASTNAME, actor.getUser().getLastName());
			}
		}
	}
}
