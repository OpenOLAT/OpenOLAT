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
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.util.mail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

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
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.manager.MailManager;

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
		//fxdiff VCRP-16: intern mail system
		sendWithContext(template.getContext(), null, null, recipientTO, recipientsCC, recipientsBCC, template, sender, result);
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
		//fxdiff VCRP-16: intern mail system
		MessageContent msg = createWithContext(context, recipientTO, recipientsCC, recipientsBCC, template, sender, result);
		String subject = msg.getSubject();
		String body = msg.getBody();
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
	//fxdiff VCRP-16: intern mail system
	public MailerResult sendRealMail(Identity recipientTO, MailTemplate template) {
		MailerResult result = new MailerResult();
		VelocityContext context = new VelocityContext();
		MessageContent msg = createWithContext(context, recipientTO, null, null, template, null, result);
		if(result.getReturnCode() != MailerResult.OK) {
			return result;
		}

		MailManager.getInstance().sendExternMessage(null, null, recipientTO, null, null, null, null, msg.getSubject(), msg.getBody(), null, result);
		return result;
	}
	
	//fxdiff VCRP-16: intern mail system
	public MailerResult sendMail(MailContext mCtxt, Identity recipientTO, List<Identity> recipientsCC, List<Identity> recipientsBCC, MailTemplate template,
			Identity sender) {
		List<Identity> recipientsTO = new ArrayList<Identity>();
		recipientsTO.add(recipientTO);
		return sendMailAsSeparateMails(mCtxt, recipientsTO, recipientsCC, recipientsBCC, template, sender);
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
		return sendMailAsSeparateMails(null, recipientsTO, recipientsCC, recipientsBCC, template, sender);
	}
	//fxdiff VCRP-16: intern mail system
	public MailerResult sendMailAsSeparateMails(MailContext mCtxt, List<Identity> recipientsTO, List<Identity> recipientsCC, List<Identity> recipientsBCC,
			MailTemplate template, Identity sender) {
		
		String metaId = UUID.randomUUID().toString().replace("-", "");
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
				sendWithContext(context, mCtxt, metaId, recipient, null, null, template, sender, result);
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
				sendWithContext(context, mCtxt, metaId, recipient, null, null, template, sender, result);
			}
		}
		if (recipientsBCC != null) {
			for (Identity recipient : recipientsBCC) {
				// populate velocity context with variables
				VelocityContext context = new VelocityContext();
				template.putVariablesInMailContext(context, recipient);
				sendWithContext(context, mCtxt, metaId, recipient, null, null, template, sender, result);
			}
		}
		if(!isMailSendToRecipient) {
			result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
		}//only one successful to is needed to return OK
		else if(isMailSendToRecipient && result.getReturnCode() == MailerResult.RECIPIENT_ADDRESS_ERROR) {
			result.setReturnCode(MailerResult.OK);
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
	//fxdiff VCRP-16: intern mail system
	private MessageContent createWithContext(VelocityContext context, Identity recipientTO, List<Identity> recipientsCC, List<Identity> recipientsBCC,
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
		}else if(recipientsBCC!=null && recipientsBCC.size()>0){
			recLocale = I18nManager.getInstance().getLocaleOrDefault(recipientsBCC.get(0).getUser().getPreferences().getLanguage());
		}
		String subject = subjectWriter.toString();
		String body = bodyWriter.toString() + MailHelper.getMailFooter(recLocale, sender) + "\n";

		// create mime message
		return new MessageContent(subject, body);
		
	}
	
	private class MessageContent {
		private final String subject;
		private final String body;
		
		public MessageContent(String subject, String body) {
			this.subject = subject;
			this.body = body;
		}

		public String getSubject() {
			return subject;
		}

		public String getBody() {
			return body;
		}
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
	//fxdiff VCRP-16: intern mail system
	private void sendWithContext(VelocityContext context, MailContext mCtxt, String metaId, Identity recipientTO, List<Identity> recipientsCC, List<Identity> recipientsBCC,
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
		MessageContent msg = createWithContext(context, recipientTO, recipientsCC, recipientsBCC, template, sender, result);
		if(msg != null && result.getReturnCode() == MailerResult.OK){
			// send mail
			List<File> attachmentList = new ArrayList<File>();
			File[] attachments = template.getAttachments();
			if(attachments != null) {
				for(File attachment:attachments) {
					if(attachment == null || !attachment.exists()) {
						result.setReturnCode(MailerResult.ATTACHMENT_INVALID);
					} else {
						attachmentList.add(attachment);
					}
				}
			}
			
			List<ContactList> ccList = createContactList(recipientsCC);
			List<ContactList> bccList = createContactList(recipientsBCC);
			MailerResult mgrResult = MailManager.getInstance().sendMessage(mCtxt, sender, null, recipientTO, null, null, ccList, bccList,
					metaId, msg.getSubject(), msg.getBody(), attachmentList);
			
			if(mgrResult.getReturnCode() != MailerResult.OK) {
				result.setReturnCode(mgrResult.getReturnCode());
			}
			if(mgrResult.getFailedIdentites() != null) {
				for(Identity failedIdentity:mgrResult.getFailedIdentites()) {
					result.addFailedIdentites(failedIdentity);
				}
			}
			
		}
	}
	//fxdiff VCRP-16: intern mail system
	private List<ContactList> createContactList(List<Identity> recipients) {
		if(recipients == null || recipients.isEmpty()) {
			return null;
		}
		
		ContactList contactList = new ContactList("");
		contactList.addAllIdentites(recipients);
		List<ContactList> list = new ArrayList<ContactList>(1);
		list.add(contactList);
		return list;
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