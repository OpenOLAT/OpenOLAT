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
package org.olat.modules.immunityproof.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofService;
import org.olat.modules.immunityproof.model.ImmunityProofImpl;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Initial date: 07.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@DisallowConcurrentExecution
public class ImmunityProofJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(ImmunityProofJob.class);
	
    @Override
    public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
    	log.info("Starting Covid Certificates Reminder Job");
    	
    	// Get managers and service
		MailManager mailManager = CoreSpringFactory.getImpl(MailManager.class);
		ImmunityProofService immunityProofService = CoreSpringFactory.getImpl(ImmunityProofService.class);
		ImmunityProofModule immunityProofModule = CoreSpringFactory.getImpl(ImmunityProofModule.class);
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		I18nManager i18nManager = I18nManager.getInstance();
    	
    	// Load all certificates
    	List<ImmunityProof> certificates = immunityProofService.getAllCertificates();
    	
    	long daysToMs = 24l * 60 * 60 * 1000;
    	long reminderPeriod = daysToMs * immunityProofModule.getReminderPeriod();
    	String reminderPeriodString = String.valueOf(immunityProofModule.getReminderPeriod());
    	Date currentDate = new Date();
    	
    	int successfulMails = 0;
    	int badMails = 0;
    	
    	for (ImmunityProof certificate : certificates) {
    		// Safe date must be before the reminder date
    		// Reminder must be active
    		// Mail must not be sent already
    		try {
	    		if (certificate.getSafeDate().getTime() - reminderPeriod < currentDate.getTime() && certificate.isSendMail() && !certificate.isMailSent()) {
	    			// Create translator for user
	    			Identity identity = securityManager.loadIdentityByKey(certificate.getIdentity().getKey(), true);
	    			Locale userLocale = i18nManager.getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
	    			Translator userTranslator = Util.createPackageTranslator(ImmunityProofModule.class, userLocale);
	    			
	    			// Send reminder and mark as sent
	    			if (sendMail(userTranslator, mailManager, identity.getUser(), userLocale, reminderPeriodString)) {
	    				((ImmunityProofImpl) certificate).setMailSent(true);
	    				immunityProofService.updateImmunityProof(certificate);
	    				
	    				successfulMails++;
	    			} else {
	    				badMails++;
	    			}
	    		} else if (certificate.getSafeDate().before(currentDate)) {
	    			// Delete certificate
	    			immunityProofService.deleteImmunityProof(certificate);
	    		}
    		} catch (Exception e) {
    			// Unlikely, immunity proofs are deleted, when user is deleted
    			log.error("Could not send immunity proof reminder message. Maybe the user is deleted already.", e);
    			badMails++;
			}
    		
    	}
    	
    	log.info("Covid Certificates Reminder Job finshed. Sent " + successfulMails + " successful mails, unable to send " + badMails + " mails.");
    }
    
    private boolean sendMail(Translator translator, MailManager mailManager, User user, Locale userLocale, String reminderPeriod) {
    	String name = "";
		String url = Settings.createServerURI() + "/covid";
		
		if (StringHelper.containsNonWhitespace(user.getFirstName())) {
			name += user.getFirstName();
		}
		
		if (StringHelper.containsNonWhitespace(user.getLastName())) {
			name += " " + user.getLastName();
		}
		
		if (!StringHelper.containsNonWhitespace(name)) {
			name = user.getNickName();
		}
		
		String[] params = new String[] {name, reminderPeriod, url};	    	
    	
    	String subject = translator.translate("immunity.proof.reminder.mail.subject");
        String body = translator.translate("immunity.proof.reminder.mail.body", params);
        String decoratedBody = mailManager.decorateMailBody(body, userLocale);
        String recipientAddress = user.getEmail();
        Address from;
        Address[] to;

        try {
            from = new InternetAddress(WebappHelper.getMailConfig("mailSupport"));
            to = new Address[] {new InternetAddress(((recipientAddress)))};
        } catch (AddressException e) {
            log.error("Could not send COVID reminder message, bad mail address", e);
            return false;
        }

        MailerResult result = new MailerResult();
        MimeMessage msg = mailManager.createMimeMessage(from, to, null, null, subject, decoratedBody, null, result);
        mailManager.sendMessage(msg, result);
        if (!result.isSuccessful()) {
            log.error("Could not send COVID reminder message to " + recipientAddress);
            return false;
        }
        
        return true;
    }
}
