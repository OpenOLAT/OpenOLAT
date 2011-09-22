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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;

/**
 * Description:<br>
 * Helper class for managing email templates
 * <P>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public class MailerWithTemplate {
	private VelocityEngine velocityEngine;
	private static MailerWithTemplate INSTANCE = new MailerWithTemplate();

	/**
	 * Singleton constructor
	 */
	private MailerWithTemplate() {
		super();
		// init velocity engine
		Properties p = null;
		try {
			velocityEngine = new VelocityEngine();
			p = new Properties();
			p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
			p.setProperty("runtime.log.logsystem.log4j.category", "syslog");
			// p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
			// "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
			// p.setProperty(RuntimeConstants.RUNTIME_LOG,
			// OLATContext.getUserdataRoot() + "logs/velocity-email.log.txt");
			// p.setProperty("runtime.log.logsystem.log4j.category", "syslog");
			// p.setProperty(RuntimeConstants.RUNTIME_LOG_ERROR_STACKTRACE, "false");
			// p.setProperty(RuntimeConstants.RUNTIME_LOG_INFO_STACKTRACE, "false");
			velocityEngine.init(p);
		} catch (Exception e) {
			throw new RuntimeException("config error " + p.toString());
		}
	}

	/**
	 * @return MailerWithTemplate returns the singleton instance
	 */
	public static MailerWithTemplate getInstance() {
		return INSTANCE;
	}

	/**
	 * Send email and use the given template-context.
	 * @param recipientTO
	 * @param recipientsCC
	 * @param recipientsBCC
	 * @param template
	 * @param sender
	 * @return
	 */
	public MailerResult sendMailUsingTemplateContext(Identity recipientTO, List<Identity> recipientsCC, List<Identity> recipientsBCC, MailTemplate template,
			Identity sender) {
		MailerResult result = new MailerResult();
		sendWithContext(template.getContext(), recipientTO, recipientsCC, recipientsBCC, template, sender, result);
		return result;
	}
	
	/**
	 * creates the subject and body for a preview.
	 * @param recipientTO
	 * @param recipientsCC
	 * @param recipientsBCC
	 * @param template
	 * @param sender
	 * @return String[] with index 0 is subject, and index 1 is body
	 */
	public String[] previewSubjectAndBody(Identity recipientTO, List<Identity> recipientsCC, List<Identity> recipientsBCC, MailTemplate template,
			Identity sender){
		MailerResult result = new MailerResult();

		VelocityContext context = new VelocityContext();		
		template.putVariablesInMailContext(context, recipientTO);
		
		MimeMessage msg = createWithContext(context, recipientTO, recipientsCC, recipientsBCC, template, sender, result);
		String subject=null;
		String body=null;
		try {
			subject = msg.getSubject();
			//
			//assume String because the body text is set via setText(..,"utf-8")
			//in the MailHelper
			//see also http://java.sun.com/j2ee/1.4/docs/api/javax/mail/internet/MimeMessage.html#getContent()
			body = (String)msg.getContent();
		} catch (MessagingException e) {
			result.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
		} catch (IOException e) {
			result.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
		}
		return new String[]{subject, body};
	}

	/**
	 * Send a mail to the given identity and the other recipients (CC and BCC)
	 * using the template. The mail is sent as identity sender.
	 * <p>
	 * The method uses sendMailAsSeparateMails to send the message. If you have
	 * multiple identities to which you need to send the same mail, use the
	 * sendMailAsSeparateMails message and not this one
	 * 
	 * @param recipientTO Identity for TO. Must not be NULL
	 * @param recipientsCC Identities List for CC. Can be NULL.
	 * @param recipientsBCC Identities List for BCC. Can be NULL
	 * @param template Mail template. Must not be NULL
	 * @param sender The senders identity. Can be NULL. In case of NULL the
	 *          systems send mail configuraton is used
	 * @return MailerResult with status and list of identites that could not be
	 *         mailed to
	 */
	public MailerResult sendMail(Identity recipientTO, List<Identity> recipientsCC, List<Identity> recipientsBCC, MailTemplate template,
			Identity sender) {
		List<Identity> recipientsTO = new ArrayList<Identity>();
		recipientsTO.add(recipientTO);
		return sendMailAsSeparateMails(recipientsTO, recipientsCC, recipientsBCC, template, sender);
	}

	/**
	 * Send a mail to the given identities (TO, CC and BCC) using the template.
	 * The mail is sent as identity sender.
	 * <p>
	 * The template is parsed for every identity in the TO list. For each of those
	 * identities a separate mail is sent. Use this only if the mail content
	 * differ for each identity. Otherwise use the sendMailAsOneMail method.
	 * <p>
	 * In this method the recipients will not know who else got the mail since the
	 * mail is addressed personally.
	 * 
	 * @param recipientsTO Identities List for TO. Must not be NULL and contain at
	 *          least one identity.
	 * @param recipientsCC Identities List for CC. Can be NULL.
	 * @param recipientsBCC Identities List for BCC. Can be NULL
	 * @param template Mail template. Must not be NULL
	 * @param sender The senders identity. Can be NULL. In case of NULL the
	 *          systems send mail configuraton is used
	 * @return MailerResult with status and list of identites that could not be
	 *         mailed to
	 */
	public MailerResult sendMailAsSeparateMails(List<Identity> recipientsTO, List<Identity> recipientsCC, List<Identity> recipientsBCC,
			MailTemplate template, Identity sender) {
		MailerResult result = new MailerResult();
		if (MailHelper.getMailhost() == null) {
			result.setReturnCode(MailerResult.MAILHOST_UNDEFINED);
			return result;
		}
		boolean isMailSendToRecipient = false;
		if (recipientsTO != null) {
			for (Identity recipient : recipientsTO) {
				// populate velocity context with variables
				VelocityContext context = new VelocityContext();
				template.putVariablesInMailContext(context, recipient);
				sendWithContext(context, recipient, null, null, template, sender, result);
				if (!result.getFailedIdentites().contains(recipient)) {
					isMailSendToRecipient = true;
				}
			}
		}
		if (recipientsCC != null) {
			for (Identity recipient : recipientsCC) {
				List<Identity> cc = new ArrayList<Identity>();
				cc.add(recipient);
				// populate velocity context with variables
				VelocityContext context = new VelocityContext();
				template.putVariablesInMailContext(context, recipient);
				sendWithContext(context, null, cc, null, template, sender, result);
				if (!result.getFailedIdentites().contains(recipient)) {
					isMailSendToRecipient = true;
				}
			}
		}
		if (recipientsBCC != null) {
			for (Identity recipient : recipientsBCC) {
				// populate velocity context with variables
				VelocityContext context = new VelocityContext();
				template.putVariablesInMailContext(context, recipient);
				sendWithContext(context, recipient, null, null, template, sender, result);
				if (!result.getFailedIdentites().contains(recipient)) {
					isMailSendToRecipient = true;
				}
			}
		}
		if(!isMailSendToRecipient) {
			result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
		}
		return result;
	}
	
	/**
	 * may return null, but then the result.getReturnCode != OK... so check the
	 * result code.
	 * @param context
	 * @param recipientTO
	 * @param recipientsCC
	 * @param recipientsBCC
	 * @param template
	 * @param sender
	 * @param result
	 * @return
	 */
	private MimeMessage createWithContext(VelocityContext context, Identity recipientTO, List<Identity> recipientsCC, List<Identity> recipientsBCC,
			MailTemplate template, Identity sender, MailerResult result){
		// prepare cc addresses - will stay the same for each mail
		Address[] addressesCC = createAddressesFromIdentities(recipientsCC, result);
		// prepare bcc addresses - will stay the same for each mail
		Address[] addressesBCC = createAddressesFromIdentities(recipientsBCC, result);

		// merge subject template with context variables
		StringWriter subjectWriter = new StringWriter();
		evaluate(context, template.getSubjectTemplate(), subjectWriter, result);
		// merge body template with context variables
		StringWriter bodyWriter = new StringWriter();
		evaluate(context, template.getBodyTemplate(), bodyWriter, result);
		// check for errors - exit
		if (result.getReturnCode() != MailerResult.OK) return null;
	
		Locale recLocale = Locale.getDefault();
		if(recipientTO!=null){
			recLocale = I18nManager.getInstance().getLocaleOrDefault(recipientTO.getUser().getPreferences().getLanguage());
		}else if(recipientsCC!=null && recipientsCC.size()>0){
			recLocale = I18nManager.getInstance().getLocaleOrDefault(recipientsCC.get(0).getUser().getPreferences().getLanguage());
		}
		String subject = subjectWriter.toString();
		String body = bodyWriter.toString() + MailHelper.getMailFooter(recLocale, sender) + "\n";
	
		// add sender to mail
		Address from, to;
		try {
			if (sender == null) {
				from = new InternetAddress(WebappHelper.getMailConfig("mailFrom"));
			} else {
				from = new InternetAddress(sender.getUser().getProperty(UserConstants.EMAIL, null));
			}
		} catch (AddressException e) {
			result.setReturnCode(MailerResult.SENDER_ADDRESS_ERROR);
			return null;
		}
		Address[] toArray = new Address[1];
		if(recipientTO != null){
			try {
				to = new InternetAddress(recipientTO.getUser().getProperty(UserConstants.EMAIL, null));
			} catch (AddressException e) {
				result.addFailedIdentites(recipientTO);
				// 	skip this user, go to next one
				return null;
			}
			toArray[0] = to;
		}else{
			toArray = new Address[0];
		}
		
		// create mime message
		return MailHelper.createMessage(from, toArray, addressesCC, addressesBCC, body, subject, template.getAttachments(), result);
		
	}
	
	/**
	 * Helper method to send email with given velocity-context.
	 * @param context
	 * @param recipientTO
	 * @param recipientsCC
	 * @param recipientsBCC
	 * @param template
	 * @param sender
	 * @param result
	 */
	private void sendWithContext(VelocityContext context, Identity recipientTO, List<Identity> recipientsCC, List<Identity> recipientsBCC,
			MailTemplate template, Identity sender, MailerResult result) {		
		List<Identity> identityTO = new ArrayList<Identity>();
		if(recipientTO != null) identityTO.add(recipientTO);
		List<Identity> failedIdentitiesTO = new ArrayList<Identity>();
		int sizeFailedIdentities = result.getFailedIdentites().size();
		result = MailHelper.removeDisabledMailAddress(identityTO, result);
		failedIdentitiesTO.addAll(result.getFailedIdentites());
		if(failedIdentitiesTO.size() == sizeFailedIdentities) {
			if(identityTO.size()>0) recipientTO = identityTO.get(0);
		} else {
			recipientTO = null;
		}
		List<Identity> failedIdentitiesCC = null;
		if (recipientsCC != null) {
			result = MailHelper.removeDisabledMailAddress(recipientsCC, result);
			failedIdentitiesCC = new ArrayList<Identity>();
			failedIdentitiesCC.addAll(result.getFailedIdentites());
			failedIdentitiesCC.removeAll(failedIdentitiesTO);
			recipientsCC.removeAll(failedIdentitiesCC);
		}
		if (recipientsBCC != null) {
			result = MailHelper.removeDisabledMailAddress(recipientsBCC, result);
			List<Identity> failedIdentitiesBCC = new ArrayList<Identity>();
			failedIdentitiesBCC.addAll(result.getFailedIdentites());
			failedIdentitiesBCC.removeAll(failedIdentitiesTO);
			failedIdentitiesBCC.removeAll(failedIdentitiesCC);
			recipientsBCC.removeAll(failedIdentitiesBCC);
		}
		MimeMessage msg = createWithContext(context, recipientTO, recipientsCC, recipientsBCC, template, sender, result);
		if(msg != null && result.getReturnCode() == MailerResult.OK){
			// send mail
			MailHelper.sendMessage(msg, result);
		}
	}

	/**
	 * Internal Helper: merges a velocity context with a template.
	 * 
	 * @param context
	 * @param template
	 * @param writer writer that contains merged result
	 * @param mailerResult
	 */
	void evaluate(Context context, String template, StringWriter writer, MailerResult mailerResult) {
		try {
			boolean result = velocityEngine.evaluate(context, writer, "mailTemplate", template);
			if (result) {
				mailerResult.setReturnCode(MailerResult.OK);
			} else {
				Tracing.logWarn("can't send email from user template with no reason", MailerWithTemplate.class);
				mailerResult.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
			}
		} catch (ParseErrorException e) {
			Tracing.logWarn("can't send email from user template", e, MailerWithTemplate.class);
			mailerResult.setReturnCode(MailerResult.TEMPLATE_PARSE_ERROR);
		} catch (MethodInvocationException e) {
			Tracing.logWarn("can't send email from user template", e, MailerWithTemplate.class);
			mailerResult.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
		} catch (ResourceNotFoundException e) {
			Tracing.logWarn("can't send email from user template", e, MailerWithTemplate.class);
			mailerResult.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
		} catch (IOException e) {
			Tracing.logWarn("can't send email from user template", e, MailerWithTemplate.class);
			mailerResult.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
		}
	}

	/**
	 * Helper: creates an address array from a list of identities
	 * 
	 * @param recipients
	 * @param result
	 * @return Address array
	 */
	private Address[] createAddressesFromIdentities(List<Identity> recipients, MailerResult result) {
		Address[] addresses = null;
		if (recipients != null && recipients.size() > 0) {
			List<Address> validRecipients = new ArrayList<Address>();
			for (int i = 0; i < recipients.size(); i++) {
				Identity identity = recipients.get(i);
				try {
					validRecipients.add(new InternetAddress(identity.getUser().getProperty(UserConstants.EMAIL, null)));
				} catch (AddressException e) {
					result.addFailedIdentites(identity);
				}
			}
			addresses = validRecipients.toArray(new Address[validRecipients.size()]);
		}
		return addresses;
	}

}