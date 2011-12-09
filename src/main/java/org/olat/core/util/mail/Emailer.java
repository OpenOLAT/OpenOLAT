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
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;

import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.manager.MailManager;

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
	private Identity mailFromIdentity;
	private String footer; // footer appended to the end of the mail

	/**
	 * Constructs an Emailer which derives its <b>mail from address </b> from the
	 * mailfrom configuration in OLATContext. The <b>mail host </b> used to send the
	 * email is taken from the <code>OLATContext</code>
	 * @param locale locale that should be used for message localization
	 */
	public Emailer(Locale locale) {
		this.mailfrom =  WebappHelper.getMailConfig("mailReplyTo");
		// initialize the mail footer with info about this OLAt installation
		PackageTranslator trans = new PackageTranslator(Util.getPackageName(Emailer.class), locale);
		footer = trans.translate("footer.no.userdata", new String[] { Settings.getServerContextPathURI() });
	}

	//fxdiff
	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
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
		//fxdiff VCRP-16: intern mail system
		this.mailFromIdentity = mailFromIdentity;
		// initialize the mail footer with infos about this OLAT installation and the user who sent the mail
		User user = mailFromIdentity.getUser();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(user.getPreferences().getLanguage());
		footer = MailHelper.getMailFooter(locale, mailFromIdentity);
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
	//fxdiff VCRP-16: intern mail system
	public boolean sendEmail(MailContext context, List<ContactList> listOfContactLists, String subject, String body) throws AddressException, MessagingException {
		return sendEmail(context, listOfContactLists, subject, body, null);
	}
	//fxdiff VCRP-16: intern mail system
	public boolean sendEmail(MailContext context, List<ContactList> listOfContactLists, String subject, String body, List<File> attachments) throws AddressException, MessagingException {
		body += footer;
		MailerResult result = MailManager.getInstance().sendMessage(context, mailFromIdentity, mailfrom, null, null, null, null, listOfContactLists, null, subject, body, attachments);
		return result.getReturnCode() == MailerResult.OK;
	}
	//fxdiff VCRP-16: intern mail system
	public boolean sendEmailCC(MailContext context, Identity cc, String subject, String body, List<File> attachments) throws AddressException, MessagingException {
		MailerResult result = MailManager.getInstance().sendMessage(context, mailFromIdentity, mailfrom, null, null, cc, null, null, null, subject, body, attachments);
		return result.getReturnCode() == MailerResult.OK;
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
	//fxdiff VCRP-16: intern mail system
	public boolean sendEmail(String mailto, String subject, String body) throws AddressException, SendFailedException, MessagingException {
		//OK context
		body += footer;
		//TO
		MailerResult result = MailManager.getInstance().sendMessage(null, mailFromIdentity, mailfrom, null, mailto, null, null, null, null, subject, body, null);
		return result.getReturnCode() == MailerResult.OK;
	}
}