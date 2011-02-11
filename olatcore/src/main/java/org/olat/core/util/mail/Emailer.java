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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util.mail;


import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;

/**
 * 
 * Helper class for sending emails. All mails sent by this class will have a
 * footer that includes a link to this OLAT installation
 * 
 * Initial Date: Feb 10, 2005
 * @author Sabina Jeger
 */
public class Emailer {
	private String mailfrom;
	private String footer; // footer appended to the end of the mail

	/**
	 * Constructs an Emailer which derives its <b>mail from address </b> from the
	 * mailfrom configuration in OLATContext. The <b>mail host </b> used to send the
	 * email is taken from the <code>OLATContext</code>
	 * @param locale locale that should be used for message localization
	 */
	public Emailer(Locale locale) {
		this.mailfrom =  WebappHelper.getMailConfig("mailFrom");
		// initialize the mail footer with info about this OLAt installation
		PackageTranslator trans = new PackageTranslator(Util.getPackageName(Emailer.class), locale);
		footer = trans.translate("footer.no.userdata", new String[] { Settings.getServerContextPathURI() });
	}

	/**
	 * Constructs an Emailer which derives its <b>mail from address </b> from the
	 * given <code>Identity</code>. The <b>mail host </b> used to send the
	 * email is taken from the <code>OLATContext</code>
	 * <p>
	 * The <code>boolean</code> parameter determines which email address is
	 * used.
	 * <UL>
	 * <LI><b>true: </b> <br>
	 * <UL>
	 * <LI>try first the Institutional email</LI>
	 * <LI>if not defined, take user defined email</LI>
	 * </UL>
	 * </LI>
	 * <LI><b>false: </b> <br>
	 * <UL>
	 * <LI>take user defined email</LI>
	 * </UL>
	 * </LI>
	 * </UL>
	 * 
	 * @param mailFromIdentity <b>not null </b>, containing the senders e-mail
	 * @param tryInstitutionalEmail specifies email address priority order
	 */
	public Emailer(Identity mailFromIdentity, boolean tryInstitutionalEmail) {
		String myMailfrom = mailFromIdentity.getUser().getProperty(UserConstants.EMAIL, null);
		if (tryInstitutionalEmail) {
			String tmpFrom = mailFromIdentity.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
			if (StringHelper.containsNonWhitespace(tmpFrom)) {
				myMailfrom = tmpFrom;
			}
		}
		this.mailfrom = myMailfrom;
		// initialize the mail footer with infos about this OLAT installation and the user who sent the mail
		User user = mailFromIdentity.getUser();
		
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(user.getPreferences().getLanguage());
		
		PackageTranslator trans = new PackageTranslator(Util.getPackageName(Emailer.class), locale);
		String institution = user.getProperty(UserConstants.INSTITUTIONALNAME, null);
		if (institution == null) institution = "";
		footer = trans.translate("footer.with.userdata", new String[] { user.getProperty(UserConstants.FIRSTNAME, null), user.getProperty(UserConstants.LASTNAME, null), mailFromIdentity.getName(),
				institution, Settings.getServerContextPathURI()  });

	}

	/**
	 * Creates a e-mail message with the given subject and body. The sender is
	 * taken from the value which was given to the constructor. The recipient
	 * fields TO: and BCC: are generated in the follwing manner:
	 * <ul>
	 * <li>each ContactList-Name from the listOfContactLists is used as (empty
	 * group) in the TO: field</li>
	 * <li>all ContactList-Members are added to the BCC: field</li>
	 * </ul>
	 * 
	 * @param listOfContactLists
	 * @param subject
	 * @param body
	 * @return
	 * @throws MessagingException
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public boolean sendEmail(List<ContactList> listOfContactLists, String subject, String body) throws AddressException, MessagingException {
		return sendEmail(listOfContactLists, subject, body, null);
	}
	
	public boolean sendEmail(List<ContactList> listOfContactLists, String subject, String body, List<File> attachments) throws AddressException, MessagingException {
		/*
		 * if the mailhost was not set in the olat.local.properties, we assume that no
		 * emailing is wished.
		 */
		if (MailHelper.getMailhost() == null || MailHelper.getMailhost().equals("") || ((MailHelper.getMailhost() instanceof String) && ((String)MailHelper.getMailhost()).equalsIgnoreCase("disabled"))) return false;
		MimeMessage msg = MailHelper.createMessage();
		msg.setFrom(new InternetAddress(this.mailfrom));
		msg.setSubject(subject, "utf-8");
		msg.setText(body + footer, "utf-8");
		msg.setSentDate(new Date());
		for (ContactList tmp : listOfContactLists) {
			InternetAddress groupName[] = InternetAddress.parse(tmp.getRFC2822Name() + ";");
			InternetAddress members[] = tmp.getEmailsAsAddresses();
			msg.addRecipients(RecipientType.TO, groupName);
			msg.addRecipients(RecipientType.BCC, members);
		}
		msg.saveChanges();
		
		File[] attachmentsArray = null;
		if(attachments != null && !attachments.isEmpty()) {
			attachmentsArray = attachments.toArray(new File[attachments.size()]);
		}
		
		MailerResult result = new MailerResult();
		MailHelper.sendMessage(msg.getFrom()[0], msg.getRecipients(RecipientType.TO), msg.getRecipients(RecipientType.CC), msg
				.getRecipients(RecipientType.BCC), body + footer, subject, attachmentsArray, result);
		return result.getReturnCode() == MailerResult.OK;
	}
	
	public boolean sendEmailCC(String cc, String subject, String body, List<File> attachments) throws AddressException, MessagingException {
		if (MailHelper.getMailhost() == null || MailHelper.getMailhost().equals("") || ((MailHelper.getMailhost() instanceof String) && ((String)MailHelper.getMailhost()).equalsIgnoreCase("disabled"))) return false;
		MimeMessage msg = MailHelper.createMessage();
		msg.setFrom(new InternetAddress(mailfrom));
		msg.setRecipients(RecipientType.CC, InternetAddress.parse(cc));
		msg.setSubject(subject, "utf-8");
		msg.setText(body + footer, "utf-8");
		msg.setSentDate(new Date());
		msg.saveChanges();
		MailerResult result = new MailerResult();
		
		File[] attachmentsArray = null;
		if(attachments != null && !attachments.isEmpty()) {
			attachmentsArray = attachments.toArray(new File[attachments.size()]);
		}
		
		MailHelper.sendMessage(msg.getFrom()[0], msg.getRecipients(RecipientType.TO), msg.getRecipients(RecipientType.CC), msg
				.getRecipients(RecipientType.BCC), body + footer, subject, attachmentsArray, result);
		return true;
	}

	/**
	 * @param mailto
	 * @param subject
	 * @param body
	 * @return
	 * @throws AddressException
	 * @throws SendFailedException
	 * @throws MessagingException
	 * TODO:gs handle exceptions internally and may return some error codes or so to get rid of dependecy of mail/activatoin jars in olat
	 */
	public boolean sendEmail(String mailto, String subject, String body) throws AddressException, SendFailedException, MessagingException {
		return sendEmail(mailfrom, mailto, subject, body);
	}
	
	private boolean sendEmail(String from, String mailto, String subject, String body) throws AddressException, SendFailedException,
			MessagingException {
		/*
		 * if the mailhost was not set in the olat.local.properties, we assume that no
		 * emailing is wished.
		 */
		if (MailHelper.getMailhost() == null || MailHelper.getMailhost().equals("") || ((MailHelper.getMailhost() instanceof String) && ((String)MailHelper.getMailhost()).equalsIgnoreCase("disabled"))) return false;
		MimeMessage msg = MailHelper.createMessage();
		msg.setFrom(new InternetAddress(from));
		msg.setRecipients(RecipientType.TO, InternetAddress.parse(mailto));
		msg.setSubject(subject, "utf-8");
		msg.setText(body + footer, "utf-8");
		msg.setSentDate(new Date());
		msg.saveChanges();
		MailerResult result = new MailerResult();
		MailHelper.sendMessage(msg.getFrom()[0], msg.getRecipients(RecipientType.TO), msg.getRecipients(RecipientType.CC), msg
				.getRecipients(RecipientType.BCC), body + footer, subject, null, result);
		return true;
	}

	static InternetAddress asInternetAddressArray(String address) {
		InternetAddress ia = null;
		try {
			ia = new InternetAddress(address);
		} catch (AddressException ae) {
			throw new RuntimeException("Error in InternetAddress : " + address);
		}
		return ia;
	}
}