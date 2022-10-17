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
package org.olat.core.util.mail;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jakarta.mail.Address;
import jakarta.mail.internet.MimeMessage;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.model.DBMail;
import org.olat.core.util.mail.model.DBMailAttachment;
import org.olat.core.util.mail.model.DBMailLight;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * 
 * 
 * Initial date: 17.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MailManager {
	

	public SubscriptionContext getSubscriptionContext();
	
	public PublisherData getPublisherData();

	
	/**
	 * Subscribe to mail news (asynchronously).
	 * 
	 * @param identity The identity which want to subscribe the mails news
	 */
	public void subscribe(Identity identity);
	
	/**
	 * @param key
	 * @return The mail or null
	 */
	public DBMail getMessageByKey(Long key);
	
	public boolean hasNewMail(Identity identity);
	
	public VFSLeaf getAttachmentDatas(Long key);
	
	public VFSLeaf getAttachmentDatas(MailAttachment attachment);
	
	public List<DBMailAttachment> getAttachments(DBMailLight mail);
	
	public String saveAttachmentToStorage(String name, String mimetype, long checksum, long size, InputStream stream);

	
	/**
	 * 
	 * @param mail
	 * @param read cannot be null
	 * @param identity
	 * @return true if the read flag has been changed
	 */
	public boolean setRead(DBMailLight mail, Boolean read, Identity identity);
	
	public DBMailLight toggleRead(DBMailLight mail, Identity identity);

	/**
	 * @param mail
	 * @param marked cannot be null
	 * @param identity
	 * @return true if the marked flag has been changed
	 */
	public boolean setMarked(DBMailLight mail, Boolean marked, Identity identity);
	
	public DBMailLight toggleMarked(DBMailLight mail, Identity identity);
	
	/**
	 * Set the mail as deleted for a user
	 * @param mail
	 * @param identity
	 */
	public void delete(DBMailLight mail, Identity identity, boolean deleteMetaMail);
	
	/**
	 * Load all mails with the identity as from, mail which are not deleted
	 * for this user. Recipients are loaded.
	 * @param from
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public List<DBMailLight> getOutbox(Identity from, int firstResult, int maxResults, boolean fetchRecipients);
	
	public List<DBMailLight> getEmailsByMetaId(String metaId);
	
	/**
	 * Load all mails with the identity as recipient, only mails which are not deleted
	 * for this user. Recipients are NOT loaded if not explicitly wanted!
	 * @param identity
	 * @param unreadOnly
	 * @param fetchRecipients
	 * @param from
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public List<DBMailLight> getInbox(IdentityRef identity, Boolean unreadOnly, Boolean fetchRecipients, Date from, int firstResult, int maxResults);
	
	/**
	 * Forward an E-Mail from the OpenOLAt mail box to the real one
	 * @param identity
	 * @param mail
	 * @param result
	 * @return
	 */
	public MailerResult forwardToRealInbox(Identity identity, DBMail mail, MailerResult result);
	
	public String getMailTemplate();
	
	public void setMailTemplate(String template);
	
	public void deleteCustomMailTemplate();
	
	public String getDefaultMailTemplate();

	
	/**
	 * Pack some standards inputs to separate MailBundle ready to be send
	 * @param ctxt
	 * @param recipientsTO
	 * @param template
	 * @param sender
	 * @param metaId
	 * @param result
	 * @return
	 */
	public MailBundle[] makeMailBundles(MailContext ctxt, List<Identity> recipientsTO,
			MailTemplate template, Identity sender, String metaId, MailerResult result);

	/**
	 * Package the mail in a bundle, use velocity on the template...
	 * 
	 * @param ctxt The context where the mail will be sent
	 * @param recipientTO The recipient
	 * @param template The mail template
	 * @param sender Who send the mail
	 * @param metaId The ID of the mailing
	 * @param result The mailer result
	 * @return The mail bundle
	 */
	public MailBundle makeMailBundle(MailContext ctxt, Identity recipientTO,
			MailTemplate template, Identity sender, String metaId, MailerResult result);
	
	/**
	 * Package the mail in a bundle, use velocity on the template...
	 * 
	 * @param ctxt The context where the mail will be sent
	 * @param externalRecipientTO The recipient
	 * @param template The mail template
	 * @param sender Who send the mail
	 * @param metaId The ID of the mailing
	 * @param result The mailer result
	 * @return The mail bundle
	 */
	public MailBundle makeMailBundle(MailContext ctxt, String externalRecipientTO,
			MailTemplate template, Identity sender, String metaId, MailerResult result);
	
	/**
	 * Package the mail in a bundle, use velocity on the template...
	 * 
	 * @param ctxt The context where the mail will be sent
	 * @param template The mail template
	 * @param sender Who send the mail
	 * @param metaId The ID of the mailing
	 * @param result The mailer result
	 * @return The mail bundle
	 */
	public MailBundle makeMailBundle(MailContext ctxt,
			MailTemplate template, Identity sender, String metaId, MailerResult result);
	
	/**
	 * Send the mail bundle
	 * @param bundles
	 * @return
	 */
	public MailerResult sendMessage(MailBundle... bundles);
	
	/**
	 * Send the mail bundle asynchronous. The queue is in memory (for the moment)
	 * and a shut down of the queue will mean looses.
	 * 
	 * @param bundles
	 * @return
	 */
	public void sendMessageAsync(MailBundle... bundles);
	
	public MailerResult sendExternMessage(MailBundle bundle, MailerResult result, boolean useTemplate);
	
	public MimeMessage createMimeMessage(Address from, Address[] tos, Address[] ccs, Address[] bccs, String subject, String body,
			List<File> attachments, MailerResult result);
	
	public void sendMessage(MimeMessage msg, MailerResult result);
	
	public MailContent decorateMail(MailBundle bundle);
	
	public String decorateMailBody(String body, Locale locale);
	
	public MailContent evaluateTemplate(MailTemplate template);

}
