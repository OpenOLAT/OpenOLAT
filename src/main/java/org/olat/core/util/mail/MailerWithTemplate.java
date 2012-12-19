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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.manager.MailManager;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	private static final OLog log = Tracing.createLoggerFor(MailerWithTemplate.class);
	private static MailerWithTemplate INSTANCE = new MailerWithTemplate();
	
	@Autowired
	private MailManager mailManager;

	/**
	 * Singleton constructor
	 */
	private MailerWithTemplate() {
		INSTANCE = this;
	}
	
	/**
	 * @return MailerWithTemplate returns the singleton instance
	 */
	public static MailerWithTemplate getInstance() {
		return INSTANCE;
	}
	
	/**
	 * [used by Spring]
	 */
	public void init() {
		// init velocity engine
		Properties p = null;
		try {
			velocityEngine = new VelocityEngine();
			p = new Properties();
			p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
			p.setProperty("runtime.log.logsystem.log4j.category", "syslog");
			velocityEngine.init(p);
		} catch (Exception e) {
			throw new RuntimeException("config error " + p.toString());
		}
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
		MessageContent msg = createWithContext(context, recipientTO, template, null, result);
		if(result.getReturnCode() != MailerResult.OK) {
			return result;
		}

		mailManager.sendExternMessage(null, null, recipientTO, null, null, null, null, msg.getSubject(), msg.getBody(), null, result);
		return result;
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
	 * @param mCtxt The mail contact (optional)
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
	public MailerResult sendMailAsSeparateMails(MailContext mCtxt, List<Identity> recipientsTO, List<Identity> recipientsCC,
			MailTemplate template, Identity sender) {
		
		String metaId = UUID.randomUUID().toString().replace("-", "");
		MailerResult result = new MailerResult();
		if (CoreSpringFactory.getImpl(MailModule.class).getMailhost() == null) {
			result.setReturnCode(MailerResult.MAILHOST_UNDEFINED);
			return result;
		}
		boolean isMailSendToRecipient = false;
		if (recipientsTO != null) {
			for (Identity recipient : recipientsTO) {
				// populate velocity context with variables
				VelocityContext context = new VelocityContext();
				template.putVariablesInMailContext(context, recipient);
				sendWithContext(context, mCtxt, metaId, recipient, template, sender, result);
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
				sendWithContext(context, mCtxt, metaId, recipient, template, sender, result);
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
	private MessageContent createWithContext(VelocityContext context, Identity recipientTO,
			MailTemplate template, Identity sender, MailerResult result){

		// merge subject template with context variables
		StringWriter subjectWriter = new StringWriter();
		evaluate(context, template.getSubjectTemplate(), subjectWriter, result);
		// merge body template with context variables
		StringWriter bodyWriter = new StringWriter();
		evaluate(context, template.getBodyTemplate(), bodyWriter, result);
		// check for errors - exit
		if (result.getReturnCode() != MailerResult.OK) return null;
	
		Locale recLocale = Locale.getDefault();
		if(recipientTO != null){
			recLocale = I18nManager.getInstance().getLocaleOrDefault(recipientTO.getUser().getPreferences().getLanguage());
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
	private void sendWithContext(VelocityContext context, MailContext mCtxt, String metaId, Identity recipientTO,
			MailTemplate template, Identity sender, MailerResult result) {	
		
		List<Identity> identityTO = new ArrayList<Identity>();
		if(recipientTO != null) {
			identityTO.add(recipientTO);
		}
		List<Identity> failedIdentitiesTO = new ArrayList<Identity>();
		int sizeFailedIdentities = result.getFailedIdentites().size();
		result = MailHelper.removeDisabledMailAddress(identityTO, result);
		failedIdentitiesTO.addAll(result.getFailedIdentites());
		if(failedIdentitiesTO.size() == sizeFailedIdentities) {
			if(identityTO.size()>0) recipientTO = identityTO.get(0);
		} else {
			recipientTO = null;
		}

		MessageContent msg = createWithContext(context, recipientTO, template, sender, result);
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

			MailerResult mgrResult = mailManager.sendMessage(mCtxt, sender, null, recipientTO, null, null, null, null,
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

	/**
	 * Internal Helper: merges a velocity context with a template.
	 * 
	 * @param context
	 * @param template
	 * @param writer writer that contains merged result
	 * @param mailerResult
	 */
	protected final void evaluate(Context context, String template, StringWriter writer, MailerResult mailerResult) {
		try {
			boolean result = velocityEngine.evaluate(context, writer, "mailTemplate", template);
			if (result) {
				mailerResult.setReturnCode(MailerResult.OK);
			} else {
				log.warn("can't send email from user template with no reason");
				mailerResult.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
			}
		} catch (ParseErrorException e) {
			log.warn("can't send email from user template", e);
			mailerResult.setReturnCode(MailerResult.TEMPLATE_PARSE_ERROR);
		} catch (MethodInvocationException e) {
			log.warn("can't send email from user template", e);
			mailerResult.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
		} catch (ResourceNotFoundException e) {
			log.warn("can't send email from user template", e);
			mailerResult.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
		} catch (Exception e) {
			log.warn("can't send email from user template", e);
			mailerResult.setReturnCode(MailerResult.TEMPLATE_GENERAL_ERROR);
		}
	}
}