/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.util.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Authenticator;

import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<br>
 * Some mail helpers
 * <P>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public class MailHelper {
	private static String mailhost;
	private static String mailhostTimeout;
	private static Authenticator smtpAuth; // null or the username/pwd used for
											// authentication
	private static boolean sslEnabled = false;
	private static boolean sslCheckCertificate = false;
	
	private static int maxSizeOfAttachments = 5;
	
	private static Map<String, Translator> translators;

	static {
		mailhost = WebappHelper.getMailConfig("mailhost");
		mailhostTimeout = WebappHelper.getMailConfig("mailTimeout");
		sslEnabled = Boolean.parseBoolean(WebappHelper.getMailConfig("sslEnabled"));
		sslCheckCertificate = Boolean.parseBoolean(WebappHelper.getMailConfig("sslCheckCertificate"));
		translators = new HashMap<String, Translator>();
		String smtpUser = null, smtpPwd = null;
		if (WebappHelper.isMailHostAuthenticationEnabled()) {
			smtpUser = WebappHelper.getMailConfig("smtpUser");
			smtpPwd = WebappHelper.getMailConfig("smtpPwd");
			smtpAuth = new MailerSMTPAuthenticator(smtpUser, smtpPwd);
		} else {
			smtpAuth = null;
		}

		if (Tracing.isDebugEnabled(Emailer.class)) {
			Tracing.logDebug("using smtp host::" + mailhost + " with timeout::"
					+ mailhostTimeout + ", smtpUser::" + smtpUser
					+ " and smtpPwd::" + smtpPwd, Emailer.class);
		}
		
		String maxSizeStr = WebappHelper.getMailConfig("mailAttachmentMaxSize");
		if(StringHelper.containsNonWhitespace(maxSizeStr)) {
			maxSizeOfAttachments = Integer.parseInt(maxSizeStr);
		}
		
	}

	/**
	 * Create a configures mail message object that is ready to use
	 * 
	 * @return MimeMessage
	 */
//fxdiff VCRP-16: intern mail system
	//public static MimeMessage createMessage()

	/**
	 * create MimeMessage from given fields, this may be used for creation of
	 * the email but sending it later. E.g. previewing the email first.
	 * @param from
	 * @param recipients
	 * @param recipientsCC
	 * @param recipientsBCC
	 * @param body
	 * @param subject
	 * @param attachments
	 * @param result
	 * @return
	 */
	//fxdiff VCRP-16: intern mail system
	//protected static MimeMessage createMessage(Address from, Address[] recipients, Address[] recipientsCC, Address[] recipientsBCC, String body,

	
	/**
	 * Send an email message to the given TO, CC and BCC address. The result will
	 * be stored in the result object. The message can contain attachments.<br>
	 * At this point HTML mails are not supported.
	 * 
	 * fxdiff: change from/replyto, see FXOLAT-74
	 * @param replyTo Address used as reply-to address. The real sender is a no-reply-adress (see config: mailFrom). Must not be NULL
	 * @param recipients Address array used as sender addresses. Must not be NULL
	 *          and contain at lease one address
	 * @param recipientsCC Address array used as CC addresses. Can be NULL
	 * @param recipientsBCC Address array used as BCC addresses. Can be NULL
	 * @param body Body text of message. Must not be NULL
	 * @param subject Subject text of message. Must not be NULL
	 * @param attachments File array used as attachments. Can be NULL
	 * @param result MailerResult object that stores the result code

	//fxdiff VCRP-16: intern mail system
	//private static void sendMessage(Address from, Address[] recipients, Address[] recipientsCC, Address[] recipientsBCC, String body,
	//		String subject, File[] attachments, MailerResult result)
	
	/**
	 * @return the maximum size allowed for attachements in MB (default 5MB)
	 */
	public static int getMaxSizeForAttachement() {
		return maxSizeOfAttachments;
	}

	/**
	 * @return the configured mail host. Can be null, indicating that the system
	 *         should not send any mail at all
	 */
	public static Object getMailhost() {
		return mailhost;
	}

	/**
	 * Create a mail footer for the given locale and sender.
	 * 
	 * @param locale Defines language of footer text. If null, the systems default
	 *          locale is used
	 * @param sender Details about sender embedded in mail footer. If null no such
	 *          details are attached to the footer
	 * @return The mail footer as string
	 */
	public static String getMailFooter(Locale locale, Identity sender) {
		if (locale == null) {
			locale = I18nModule.getDefaultLocale();
		}
		Translator trans = getTranslator(locale);
		if (sender == null) {
			// mail sent by plattform configured sender address
			return trans.translate("footer.no.userdata", new String[] { Settings.getServerContextPathURI() });
		}
		// mail sent by a system user
		User user = sender.getUser();

		// FXOLAT-356: separate context for mail footer
		// username / server-url are always first [0], [1].		
		UserManager um = UserManager.getInstance();
		List<UserPropertyHandler> userPropertyHandlers = um.getUserPropertyHandlersFor(MailHelper.class.getCanonicalName(), false);
		ArrayList<String> uProps = new ArrayList<String>(userPropertyHandlers.size()+2);
		uProps.add(sender.getName());
		uProps.add(Settings.getServerContextPathURI());
		
		for (Iterator<UserPropertyHandler> iterator = userPropertyHandlers.iterator(); iterator.hasNext();) {
			UserPropertyHandler handler = iterator.next();
			uProps.add(handler.getUserProperty(user, locale));			
		}
		// add empty strings to prevent non-replaced wildcards like "{5}" etc. in emails.
		while (uProps.size() < 15){
			uProps.add("");
		}
		
		String[] userProps = new String[]{};
		userProps = ArrayHelper.toArray(uProps);
		return trans.translate("footer.with.userdata", userProps);
	}
	
	public static String getTitleForFailedUsersError(Locale locale) {
		return getTranslator(locale).translate("mailhelper.error.failedusers.title");
	}
	
	public static String getMessageForFailedUsersError(Locale locale, List<Identity> disabledIdentities) { 
		String message = getTranslator(locale).translate("mailhelper.error.failedusers");
		message += "\n<ul>\n";
		for (Identity identity : disabledIdentities) {
			message += "<li>\n";
			message += identity.getUser().getProperty(UserConstants.FIRSTNAME, null);
			message += " ";
			message += identity.getUser().getProperty(UserConstants.LASTNAME, null);
			message += "\n</li>\n";
		}
		message += "</ul>\n";
		return message;
	}

	/**
	 * Helper method to reuse translators. It makes no sense to build a new
	 * translator over and over again. We keep one for each language and reuse
	 * this one during the whole lifetime
	 * 
	 * @param locale
	 * @return a translator for the given locale
	 */
	private static Translator getTranslator(Locale locale) {
		String ident = locale.toString();
		synchronized (translators) {  //o_clusterok   brasato:::: nice idea, but move to translatorfactory and kick out translator.setLocale() (move it to LocaleChangableTranslator)
			Translator trans = translators.get(ident);
			if (trans == null) {
				trans = new PackageTranslator(Util.getPackageName(Emailer.class), locale);
				translators.put(ident, trans);
			}
			return trans;
		}
	}

	/**
	 * Method to evaluate the mailer result and disply general error and warning
	 * messages. If you want to display other messages instead you have to
	 * evaluate the mailer result yourself and print messages accordingly.
	 * 
	 * @param mailerResult The mailer result to be evaluated
	 * @param wControl The current window controller
	 * @param locale The users local
	 */
	public static void printErrorsAndWarnings(MailerResult mailerResult, WindowControl wControl, Locale locale) {
		StringBuilder errors = new StringBuilder();
		StringBuilder warnings = new StringBuilder();
		appendErrorsAndWarnings(mailerResult, errors, warnings, locale);
		// now print a warning to the users screen
		if (errors.length() > 0) {
			wControl.setError(errors.toString());
		}
		if (warnings.length() > 0) {
			wControl.setWarning(warnings.toString());
		}
	}

	/**
	 * Method to evaluate the mailer result. The errors and warnings will be
	 * attached to the given string buffers. If you want to display other messages
	 * instead you have to evaluate the mailer result yourself and print messages
	 * accordingly.
	 * 
	 * @param mailerResult The mailer result to be evaluated
	 * @param errors StringBuilder for the error messages
	 * @param warnings StringBuilder for the warnings
	 * @param locale The users local
	 */
	public static void appendErrorsAndWarnings(MailerResult mailerResult, StringBuilder errors, StringBuilder warnings, Locale locale) {
		Translator trans = new PackageTranslator(Util.getPackageName(MailerResult.class), locale);
		int returnCode = mailerResult.getReturnCode();
		List<Identity> failedIdentites = mailerResult.getFailedIdentites();

		// first the severe errors
		if (returnCode == MailerResult.SEND_GENERAL_ERROR) {
			errors.append("<p>").append(trans.translate("mailhelper.error.send.general")).append("</p>");
		} else if (returnCode == MailerResult.SENDER_ADDRESS_ERROR) {
			errors.append("<p>").append(trans.translate("mailhelper.error.sender.address")).append("</p>");
		} else if (returnCode == MailerResult.RECIPIENT_ADDRESS_ERROR) {
			errors.append("<p>").append(trans.translate("mailhelper.error.recipient.address")).append("</p>");
		} else if (returnCode == MailerResult.TEMPLATE_GENERAL_ERROR) {
			errors.append("<p>").append(trans.translate("mailhelper.error.template.general")).append("</p>");
		} else if (returnCode == MailerResult.TEMPLATE_PARSE_ERROR) {
			errors.append("<p>").append(trans.translate("mailhelper.error.template.parse")).append("</p>");
		} else if (returnCode == MailerResult.ATTACHMENT_INVALID) {
			errors.append("<p>").append(trans.translate("mailhelper.error.attachment")).append("</p>");
		} else {
			// mail could be send, but maybe not to all the users (e.g. invalid mail
			// adresses or a temporary problem)
			if (failedIdentites != null && failedIdentites.size() > 0) {
				warnings.append("<p>").append(trans.translate("mailhelper.error.failedusers"));
				warnings.append("<ul>");
				for (Identity identity : failedIdentites) {
					User user = identity.getUser();
					warnings.append("<li>");
					warnings.append(trans.translate("mailhelper.error.failedusers.user", new String[] { user.getProperty(UserConstants.FIRSTNAME, null), user.getProperty(UserConstants.LASTNAME, null),
							user.getProperty(UserConstants.EMAIL, null), identity.getName() }));
					warnings.append("</li>");
				}
				warnings.append("</ul></p>");
			}
		}
	}

	/**
	 * Checks if the given mail address is potentially a valid email address that
	 * can be used to send emails. It does NOT check if the mail address exists,
	 * it checks only for syntactical validity.
	 * 
	 * @param mailAddress
	 * @return 
	 */
	public static boolean isValidEmailAddress(String mailAddress) {
		return EmailAddressValidator.isValidEmailAddress(mailAddress);
	}
	
	
	/**
	 * Internal helper
	 * 
	 * @param from
	 * @param recipients
	 * @param recipientsCC
	 * @param recipientsBCC
	 * @param body
	 * @param subject
	 * @param attachments
	 */
	private static void doDebugMessage(Address from, Address[] recipients, Address[] recipientsCC, Address[] recipientsBCC, String body,
			String subject, File[] attachments) {
		String to = new String();
		String cc = new String();
		String bcc = new String();
		String att = new String();
		for (Address addr : recipients) {
			to = to + "'" + addr.toString() + "' ";
		}
		if (recipientsCC != null) {
			for (Address addr : recipientsCC) {
				cc = cc + "'" + addr.toString() + "' ";
			}
		}
		if (recipientsBCC != null) {
			for (Address addr : recipientsBCC) {
				bcc = bcc + "'" + addr.toString() + "' ";
			}
		}
		if (attachments != null) {
			for (File file : attachments) {
				if (file != null) att = att + "'" + file.getAbsolutePath() + "' ";
			}
		}
		Tracing.logDebug("Sending mail from::'" + from + "' to::" + to + " CC::" + cc + " BCC::" + bcc + " subject::" + subject + " body::"
				+ body + " attachments::" + att, Emailer.class);
	}
	
	/**
	 * check for disabled mail address
	 * @param recipients
	 * @param result
	 * @return
	 */
	public static MailerResult removeDisabledMailAddress(List<Identity> identities, MailerResult result) {
		String value = "";
		if (identities != null) {
			for (Identity identity : identities) {
				value = identity.getUser().getProperty("emailDisabled", null);
				if (value != null && value.equals("true")) {
					result.addFailedIdentites(identity);
					if(result.getReturnCode() != MailerResult.RECIPIENT_ADDRESS_ERROR) {
						result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
					}
				}
			}
		}
		return result;
	}
}



