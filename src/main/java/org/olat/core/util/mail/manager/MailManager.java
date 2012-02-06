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

package org.olat.core.util.mail.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerSMTPAuthenticator;
import org.olat.core.util.mail.model.DBMail;
import org.olat.core.util.mail.model.DBMailAttachment;
import org.olat.core.util.mail.model.DBMailAttachmentData;
import org.olat.core.util.mail.model.DBMailImpl;
import org.olat.core.util.mail.model.DBMailRecipient;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.PublisherData;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.notifications.SubscriptionContext;

/**
 * 
 * Description:<br>
 * Manager which send e-mails, make the triage between mails which are
 * really send by POP, or only saved in the intern mail system (a.k.a on
 * the database).
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailManager extends BasicManager {
	
	private final MailModule mailModule;
	private DB dbInstance;
	private NotificationsManager notificationsManager;
	
	private static MailManager INSTANCE;
	
	public MailManager(MailModule mailModule) {
		INSTANCE = this;
		this.mailModule = mailModule;
	}
	
	public static MailManager getInstance() {
		return INSTANCE;
	}

	/**
	 * [used by Spring]
	 * 
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	/**
	 * [used by Spring]
	 * @param notificationsManager
	 */
	public void setNotificationsManager(NotificationsManager notificationsManager) {
		this.notificationsManager = notificationsManager;
	}
	
	/**
	 * [used by Spring]
	 */
	public void init() {
		PublisherData pdata = getPublisherData();
		SubscriptionContext scontext = getSubscriptionContext();
		notificationsManager.getOrCreatePublisher(scontext, pdata);
	}
	
	public SubscriptionContext getSubscriptionContext() {
		return new SubscriptionContext("Inbox", 0l, "");
	}
	
	public PublisherData getPublisherData() {
		String data = "";
		String businessPath = "[Inbox:0]";
		PublisherData publisherData = new PublisherData("Inbox", data, businessPath);
		return publisherData;
	}
	
	public Subscriber getSubscriber(Identity identity) {
		SubscriptionContext context = getSubscriptionContext();
		if(context == null) return null;
		Publisher publisher = notificationsManager.getPublisher(context);
		if(publisher == null) {
			return null;
		}
		return notificationsManager.getSubscriber(identity, publisher);
	}
	
	public void subscribe(Identity identity) {
		PublisherData data = getPublisherData();
		SubscriptionContext context = getSubscriptionContext();
		if(context != null) {
			notificationsManager.subscribe(identity, context, data);
		}
	}

	public DBMail getMessageByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mail from ").append(DBMailImpl.class.getName()).append(" mail")
			.append(" left join fetch mail.recipients recipients")
			.append(" where mail.key=:mailKey");

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("mailKey", key);
		
		List<DBMailImpl> mails = query.list();
		if(mails.isEmpty()) return null;
		DBMailImpl mail = mails.get(0);
		return mail;
	}
	
	public List<DBMailAttachment> getAttachments(DBMail mail) {
		StringBuilder sb = new StringBuilder();
		sb.append("select attachment from ").append(DBMailAttachment.class.getName()).append(" attachment")
			.append(" inner join attachment.mail mail")
			.append(" where mail.key=:mailKey");

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("mailKey", mail.getKey());
		
		List<DBMailAttachment> attachments = query.list();
		return attachments;
	}
	
	public DBMailAttachmentData getAttachmentWithData(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select attachment from ").append(DBMailAttachmentData.class.getName()).append(" attachment")
			.append(" where attachment.key=:attachmentKey");

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("attachmentKey", key);
		
		List<DBMailAttachmentData> mails = query.list();
		if(mails.isEmpty()) return null;
		return mails.get(0);
	}
	
	public boolean hasNewMail(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(mail) from ").append(DBMailImpl.class.getName()).append(" mail")
			.append(" inner join mail.recipients recipient")
			.append(" inner join recipient.recipient recipientIdentity")
			.append(" where recipientIdentity.key=:recipientKey and recipient.read=false and recipient.deleted=false");

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("recipientKey", identity.getKey());

		List<Number> mails = query.list();
		if(mails.isEmpty()) return false;
		return mails.get(0).intValue() > 0;
	}
	
	/**
	 * 
	 * @param mail
	 * @param read cannot be null
	 * @param identity
	 * @return true if the read flag has been changed
	 */
	public boolean setRead(DBMail mail, Boolean read, Identity identity) {
		if(mail == null || read == null || identity == null) throw new NullPointerException();
		
		boolean changed = false;
		for(DBMailRecipient recipient:mail.getRecipients()) {
			if(recipient == null) continue;
			if(recipient.getRecipient() != null && recipient.getRecipient().equalsByPersistableKey(identity)) {
				if(!read.equals(recipient.getRead())) {
					recipient.setRead(read);
					dbInstance.updateObject(recipient);
					changed |= true;
				}
			}
		}
		return changed;
	}
	
	public DBMail toggleRead(DBMail mail, Identity identity) {
		Boolean read = null;
		for(DBMailRecipient recipient:mail.getRecipients()) {
			if(recipient == null) continue;
			if(recipient.getRecipient() != null && recipient.getRecipient().equalsByPersistableKey(identity)) {
				if(read == null) {
					read = recipient.getRead() == null ? Boolean.FALSE : recipient.getRead();
				}
				recipient.setRead(read.booleanValue() ? Boolean.FALSE : Boolean.TRUE);
				dbInstance.updateObject(recipient);
			}
		}
		return mail;
	}

	/**
	 * @param mail
	 * @param marked cannot be null
	 * @param identity
	 * @return true if the marked flag has been changed
	 */
	public boolean setMarked(DBMail mail, Boolean marked, Identity identity) {
		if(mail == null || marked == null || identity == null) throw new NullPointerException();

		boolean changed = false;
		for(DBMailRecipient recipient:mail.getRecipients()) {
			if(recipient == null) continue;
			if(recipient != null && recipient.getRecipient() != null && recipient.getRecipient().equalsByPersistableKey(identity)) {
				if(marked == null) {
					marked = Boolean.FALSE;
				}
				if(!marked.equals(recipient.getMarked())) {
					recipient.setMarked(marked.booleanValue());
					dbInstance.updateObject(recipient);
					changed |= true;
				}
			}
		}
		return changed;
	}
	
	public DBMail toggleMarked(DBMail mail, Identity identity) {
		Boolean marked = null;
		for(DBMailRecipient recipient:mail.getRecipients()) {
			if(recipient == null) continue;
			if(recipient != null && recipient.getRecipient() != null && recipient.getRecipient().equalsByPersistableKey(identity)) {
				if(marked == null) {
					marked = recipient.getMarked() == null ? Boolean.FALSE : recipient.getMarked();
				}
				recipient.setMarked(marked.booleanValue() ? Boolean.FALSE : Boolean.TRUE);
				dbInstance.updateObject(recipient);
			}
		}
		return mail;
	}
	
	/**
	 * Set the mail as deleted for a user
	 * @param mail
	 * @param identity
	 */
	public void delete(DBMail mail, Identity identity, boolean deleteMetaMail) {
		if(StringHelper.containsNonWhitespace(mail.getMetaId()) && deleteMetaMail) {
			List<DBMail> mails = getEmailsByMetaId(mail.getMetaId());
			for(DBMail childMail:mails) {
				deleteMail(childMail, identity, false);
			}
		} else {
			deleteMail(mail, identity, false);
		}
	}

	private void deleteMail(DBMail mail, Identity identity, boolean forceRemoveRecipient) {
		boolean delete = true;
		List<DBMailRecipient> updates = new ArrayList<DBMailRecipient>();
		if(mail.getFrom() != null && mail.getFrom().getRecipient() != null) {
			if(identity.equalsByPersistableKey(mail.getFrom().getRecipient())) {
				DBMailRecipient from = mail.getFrom();
				from.setDeleted(Boolean.TRUE);
				if(forceRemoveRecipient) {
					from.setRecipient(null);
				}
				updates.add(from);
			}
			if(mail.getFrom().getDeleted() != null) {
				delete &= mail.getFrom().getDeleted().booleanValue();
			}
		}
		
		for(DBMailRecipient recipient:mail.getRecipients()) {
			if(recipient == null) continue;
			if(recipient.getRecipient() != null && recipient.getRecipient().equalsByPersistableKey(identity)) {
				recipient.setDeleted(Boolean.TRUE);
				if(forceRemoveRecipient) {
					recipient.setRecipient(null);
				}
				updates.add(recipient);
			}
			if(recipient.getDeleted() != null) {
				delete &= recipient.getDeleted().booleanValue();
			}
		}
		
		if(delete) {
			//all marked as deleted -> delete the mail
			List<DBMailAttachment> attachments = getAttachments(mail);
			for(DBMailAttachment attachment: attachments) {
				mail = attachment.getMail();//reload from the hibernate session
				dbInstance.deleteObject(attachment);
			}
			dbInstance.deleteObject(mail);
		} else {
			for(DBMailRecipient update:updates) {
				dbInstance.updateObject(update);
			}
		}
	}
	
	/**
	 * Load all mails with the identity as from, mail which are not deleted
	 * for this user. Recipients are loaded.
	 * @param from
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public List<DBMail> getOutbox(Identity from, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mail from ").append(DBMailImpl.class.getName()).append(" mail")
			.append(" inner join fetch mail.from fromRecipient")
			.append(" inner join fromRecipient.recipient fromRecipientIdentity")
			.append(" inner join fetch mail.recipients recipient")
			.append(" inner join recipient.recipient recipientIdentity")
			.append(" where fromRecipientIdentity.key=:fromKey and fromRecipient.deleted=false and recipientIdentity.key!=:fromKey")
			.append(" order by mail.creationDate desc");

		DBQuery query = dbInstance.createQuery(sb.toString());
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		query.setLong("fromKey", from.getKey());

		List<DBMail> mails = query.list();
		return mails;
	}
	
	public List<DBMail> getEmailsByMetaId(String metaId) {
		if(!StringHelper.containsNonWhitespace(metaId)) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select mail from ").append(DBMailImpl.class.getName()).append(" mail")
			.append(" inner join fetch mail.from fromRecipient")
			.append(" inner join fromRecipient.recipient fromRecipientIdentity")
			.append(" where mail.metaId=:metaId");

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setString("metaId", metaId);
		List<DBMail> mails = query.list();
		return mails;
	}
	
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
	public List<DBMail> getInbox(Identity identity, Boolean unreadOnly, Boolean fetchRecipients, Date from, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		String fetchOption = (fetchRecipients != null && fetchRecipients.booleanValue()) ? "fetch" : "";
		sb.append("select mail from ").append(DBMailImpl.class.getName()).append(" mail")
			.append(" inner join ").append(fetchOption).append(" mail.recipients recipient")
			.append(" inner join ").append(fetchOption).append(" recipient.recipient recipientIdentity")
			.append(" where recipientIdentity.key=:recipientKey and recipient.deleted=false");
		if(unreadOnly != null && unreadOnly.booleanValue()) {
			sb.append(" and recipient.read=false");
		}
		if(from != null) {
			sb.append(" and mail.creationDate>=:from");
		}
		
		sb.append(" order by mail.creationDate desc");

		DBQuery query = dbInstance.createQuery(sb.toString());
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		query.setLong("recipientKey", identity.getKey());
		if(from != null) {
			query.setDate("from", from);
		}

		List<DBMail> mails = query.list();
		return mails;
	}
	
	public MailerResult sendMessage(MailContext context, Identity fromId, String from, Identity toId, String to,
			Identity cc, List<ContactList> ccLists, List<ContactList> bccLists, 
			String metaId, String subject, String body, List<File> attachments) {
		
		MailerResult result = new MailerResult();
		if(mailModule.isInternSystem()) {
			saveDBMessage(context, fromId, from, toId, to, cc, ccLists, bccLists, metaId, subject, body, attachments, result);
		} else {
			sendExternMessage(fromId, from, toId, to, cc, ccLists, bccLists, subject, body, attachments, result);
		}
		return result;
	}
	
	
	public MailerResult forwardToRealInbox(Identity identity, DBMail mail, MailerResult result) {
		
		if(result == null) {
			result = new MailerResult();
		}
		
		List<DBMailAttachment> attachments = getAttachments(mail);

		try {
			Address from = createAddress(WebappHelper.getMailConfig("mailFrom"));
			Address to = createAddress(identity, result, true);
			MimeMessage message = createForwardMimeMessage(from, to, mail.getSubject(), mail.getBody(), attachments, result);
			if(message != null) {
				sendMessage(message, result);
			}
		} catch (AddressException e) {
			logError("mailFrom is not configured", e);
		}
		return result;
	}
	
	
	/**
	 * Send the message via e-mail, always.
	 * @param from
	 * @param to
	 * @param cc
	 * @param contactLists
	 * @param listAsBcc
	 * @param subject
	 * @param body
	 * @param attachments
	 * @return
	 */
	public MailerResult sendExternMessage(Identity fromId, String from, Identity toId, String to, Identity cc, List<ContactList> ccLists, List<ContactList> bccLists,
			String subject, String body, List<File> attachments, MailerResult result) {

		if(result == null) {
			result = new MailerResult();
		}
		MimeMessage mail = createMimeMessage(fromId, from, toId, to, cc, ccLists, bccLists, subject, body, attachments, result);
		if(mail != null) {
			sendMessage(mail, result);
		}
		return result;
	}
	
	private boolean wantRealMailToo(Identity id) {
		if(id == null) return false;
		String want = id.getUser().getPreferences().getReceiveRealMail();
		if(want != null) {
			return "true".equals(want);
		}
		return mailModule.isReceiveRealMailUserDefaultSetting();
	}
	
	protected DBMail saveDBMessage(MailContext context, Identity fromId, String from, Identity toId, String to, 
			Identity cc, List<ContactList> ccLists, List<ContactList> bccLists,
			String metaId, String subject, String body, List<File> attachments, MailerResult result) {
		
		try {
			DBMailImpl mail = new DBMailImpl();
			if(result == null) {
				result = new MailerResult();
			}
			
			boolean makeRealMail = makeRealMail(toId, ccLists, bccLists);
			Address fromAddress = null;
			List<Address> toAddress = new ArrayList<Address>();
			List<Address> ccAddress = new ArrayList<Address>();
			List<Address> bccAddress = new ArrayList<Address>();
			
			if(fromId != null) {
				DBMailRecipient fromRecipient = new DBMailRecipient();
				fromRecipient.setRecipient(fromId);
				if(StringHelper.containsNonWhitespace(from)) {
					fromRecipient.setEmailAddress(from);
					fromAddress = createFromAddress(from, result);
				} else {
					fromAddress = createFromAddress(fromId, result);
				}
				fromRecipient.setVisible(Boolean.TRUE);
				fromRecipient.setMarked(Boolean.FALSE);
				fromRecipient.setDeleted(Boolean.FALSE);
				mail.setFrom(fromRecipient);
			} else {
				if(!StringHelper.containsNonWhitespace(from)) {
					from = WebappHelper.getMailConfig("mailFrom");
				}
				DBMailRecipient fromRecipient = new DBMailRecipient();
				fromRecipient.setEmailAddress(from);
				fromRecipient.setVisible(Boolean.TRUE);
				fromRecipient.setMarked(Boolean.FALSE);
				fromRecipient.setDeleted(Boolean.TRUE);//marked as delted as nobody can read it
				mail.setFrom(fromRecipient);
				fromAddress = createFromAddress(from, result);
			}
			
			if(result.getReturnCode() != MailerResult.OK) {
				return null;
			}
			
			mail.setMetaId(metaId);
			if(subject != null && subject.length() > 500) {
				logWarn("Cut a too long subkect in name. Size: " + subject.length(), null);
				subject = subject.substring(0, 500);
			}
			mail.setSubject(subject);
			if(body != null && body.length() > 16777210) {
				logWarn("Cut a too long body in mail. Size: " + body.length(), null);
				body = body.substring(0, 16000000);
			}
			mail.setBody(body);
			mail.setLastModified(new Date());
			
			if(context != null) {
				OLATResourceable ores = context.getOLATResourceable();
				if(ores != null) {
					String resName = ores.getResourceableTypeName();
					if(resName != null && resName.length() > 50) {
						logWarn("Cut a too long resourceable type name in mail context: " + resName, null);
						resName = resName.substring(0, 49);
					}
					mail.getContext().setResName(ores.getResourceableTypeName());
					mail.getContext().setResId(ores.getResourceableId());
				}
				
				String resSubPath = context.getResSubPath();
				if(resSubPath != null && resSubPath.length() > 2000) {
					logWarn("Cut a too long resSubPath in mail context: " + resSubPath, null);
					resSubPath = resSubPath.substring(0, 2000);
				}
				mail.getContext().setResSubPath(resSubPath);
				
				String businessPath = context.getBusinessPath();
				if(businessPath != null && businessPath.length() > 2000) {
					logWarn("Cut a too long resSubPath in mail context: " + businessPath, null);
					businessPath = businessPath.substring(0, 2000);
				}
				mail.getContext().setBusinessPath(businessPath);
			}
			
			//add to
			DBMailRecipient recipientTo = null;
			if(toId != null) {
				recipientTo = new DBMailRecipient();
				if(toId instanceof PersistentObject) {
					recipientTo.setRecipient(toId);
				} else {
					to = toId.getUser().getProperty(UserConstants.EMAIL, null);
				}
				if(StringHelper.containsNonWhitespace(to)) {
					recipientTo.setEmailAddress(to);
				}
				recipientTo.setVisible(Boolean.TRUE);
				recipientTo.setDeleted(Boolean.FALSE);
				recipientTo.setMarked(Boolean.FALSE);
				recipientTo.setRead(Boolean.FALSE);
			} else if (StringHelper.containsNonWhitespace(to)) {
				recipientTo = new DBMailRecipient();
				recipientTo.setEmailAddress(to);
				recipientTo.setVisible(Boolean.TRUE);
				recipientTo.setDeleted(Boolean.TRUE);
				recipientTo.setMarked(Boolean.FALSE);
				recipientTo.setRead(Boolean.FALSE);
			}
			
			if(recipientTo != null) {
				mail.getRecipients().add(recipientTo);
				createAddress(toAddress, recipientTo, true, result, true);
			} 
			if(makeRealMail && StringHelper.containsNonWhitespace(to)) {
				createAddress(toAddress, to, result);
			}
			
			if(cc != null) {
				DBMailRecipient recipient = new DBMailRecipient();
				if(cc instanceof PersistentObject) {
					recipient.setRecipient(cc);
				} else {
					recipient.setEmailAddress(cc.getUser().getProperty(UserConstants.EMAIL, null));
				}
				recipient.setVisible(Boolean.TRUE);
				recipient.setDeleted(Boolean.FALSE);
				recipient.setMarked(Boolean.FALSE);
				recipient.setRead(Boolean.FALSE);
				mail.getRecipients().add(recipient);
				createAddress(ccAddress, recipient, false, result, true);
			}
			
			//add cc recipients
			appendRecipients(mail, ccLists, toAddress, ccAddress, true, makeRealMail, result);
			
			//add bcc recipients
			appendRecipients(mail, bccLists, toAddress, bccAddress, false, makeRealMail, result);
			
			dbInstance.saveObject(mail);
			
			//save attachments
			if(attachments != null && !attachments.isEmpty()) {
				for(File attachment:attachments) {
					DBMailAttachmentData data = new DBMailAttachmentData();
					data.setSize(attachment.length());
					data.setName(attachment.getName());
					data.setMimetype(WebappHelper.getMimeType(attachment.getName()));
					data.setMail(mail);
					
					try {
						byte[] datas = new byte[(int)attachment.length()];
						FileInputStream fis = new FileInputStream(attachment);
						fis.read(datas);
						data.setDatas(datas);
						dbInstance.saveObject(data);
					} catch (FileNotFoundException e) {
						logError("File attachment not found: " + attachment, e);
					} catch (IOException e) {
						logError("Error with file attachment: " + attachment, e);
					}
				}
			}
			
			if(makeRealMail) {
				sendRealMessage(fromAddress, toAddress, ccAddress, bccAddress, subject, body, attachments, result);
			}

			//update subscription
			for(DBMailRecipient recipient:mail.getRecipients()) {
				if(recipient.getRecipient() != null) {
					subscribe(recipient.getRecipient());
				}
			}

			SubscriptionContext subContext = getSubscriptionContext();
			Publisher publisher = NotificationsManager.getInstance().getPublisher(subContext);
			if(publisher != null && publisher.getKey() != null) {
				publisher.setLatestNewsDate(new Date());
				notificationsManager.updatePublisher(publisher);
			}
			return mail;
		} catch (AddressException e) {
			logError("Cannot send e-mail: ", e);
			result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
			return null;
		}
	}
	
	private void appendRecipients(DBMailImpl mail, List<ContactList> ccLists, List<Address> toAddress, List<Address> ccAddress,
			boolean visible, boolean makeRealMail, MailerResult result) throws AddressException {
		
		//append cc/bcc recipients
		if(ccLists != null && !ccLists.isEmpty()) {
			for(ContactList contactList:ccLists) {
				if(makeRealMail && StringHelper.containsNonWhitespace(contactList.getName())) {
					Address[] groupAddress = InternetAddress.parse(contactList.getRFC2822Name() + ";");
					if(groupAddress != null && groupAddress.length > 0) {
						for(Address groupAdd:groupAddress) {
							toAddress.add(groupAdd);
						}
					}
				}

				for(String email:contactList.getStringEmails().values()) {
					DBMailRecipient recipient = new DBMailRecipient();
					recipient.setEmailAddress(email);
					recipient.setGroup(contactList.getName());
					recipient.setVisible(visible);
					recipient.setDeleted(Boolean.FALSE);
					recipient.setMarked(Boolean.FALSE);
					recipient.setRead(Boolean.FALSE);
					mail.getRecipients().add(recipient);
					
					if(makeRealMail) {
						createAddress(ccAddress, recipient, false, result, false);
					}
				}
				
				for(Identity identityEmail:contactList.getIdentiEmails().values()) {
					DBMailRecipient recipient = new DBMailRecipient();
					if(identityEmail instanceof PersistentObject) {
						recipient.setRecipient(identityEmail);
					} else {
						recipient.setEmailAddress(identityEmail.getUser().getProperty(UserConstants.EMAIL, null));
					}
					recipient.setGroup(contactList.getName());
					recipient.setVisible(visible);
					recipient.setDeleted(Boolean.FALSE);
					recipient.setMarked(Boolean.FALSE);
					recipient.setRead(Boolean.FALSE);
					mail.getRecipients().add(recipient);
					
					if(makeRealMail) {
						createAddress(ccAddress, recipient, false, result, false);
					}
				}
			}
		}
	}
	
	private boolean makeRealMail(Identity toId, List<ContactList> ccLists, List<ContactList> bccLists) {
		//need real mail to???
		boolean makeRealMail = false;
		// can occur on self-registration
		if (toId == null && ccLists == null && bccLists == null) return true;
		
		if(toId != null) {
			makeRealMail |= wantRealMailToo(toId);
		}
		
		//add bcc recipients
		if(bccLists != null && !bccLists.isEmpty()) {
			for(ContactList contactList:bccLists) {
				for(Identity identityEmail:contactList.getIdentiEmails().values()) {
					makeRealMail |= wantRealMailToo(identityEmail);
				}
				
				if(!contactList.getStringEmails().isEmpty()) {
					makeRealMail |= true;
				}
			}
		}
		
		//add bcc recipients
		if(ccLists != null && !ccLists.isEmpty()) {
			for(ContactList contactList:ccLists) {
				for(Identity identityEmail:contactList.getIdentiEmails().values()) {
					makeRealMail |= wantRealMailToo(identityEmail);
				}
				
				if(!contactList.getStringEmails().isEmpty()) {
					makeRealMail |= true;
				}
			}
		}
		
		return makeRealMail;
	}
	
	private MimeMessage createMimeMessage(Identity fromId, String mailFrom, Identity toId, String to, Identity ccId,
			List<ContactList> ccLists, List<ContactList> bccLists,
			String subject, String body, List<File> attachments, MailerResult result) {
		try {
			Address from;
			if(StringHelper.containsNonWhitespace(mailFrom)) {
				from = createFromAddress(mailFrom, result);
			} else if (fromId != null) {
				from = createFromAddress(fromId, result);
			} else {
				// fxdiff: change from/replyto, see FXOLAT-74 . if no from is set, use default sysadmin-address (adminemail).
				from = createAddress(WebappHelper.getMailConfig("mailReplyTo"));
				if(from == null) {
					logError("MailConfigError: mailReplyTo is not set", null);
				}
			}

			List<Address> toList = new ArrayList<Address>();
			if(StringHelper.containsNonWhitespace(to)) {
				Address[] toAddresses = InternetAddress.parse(to);
				for(Address toAddress:toAddresses) {
					toList.add(toAddress);
				}
			} else if (toId != null) {
				Address toAddress = createAddress(toId, result, true);
				if(toAddress != null) {
					toList.add(toAddress);
				} 
			}
			
			List<Address> ccList = new ArrayList<Address>();
			if(ccId != null) {
				Address ccAddress = createAddress(ccId, result, true);
				ccList.add(ccAddress);
			}

			//add cc contact list
			if(ccLists != null) {
				for (ContactList contactList : ccLists) {
					if(StringHelper.containsNonWhitespace(contactList.getName())) {
						Address[] groupNames = InternetAddress.parse(contactList.getRFC2822Name() + ";");
						for(Address groupName:groupNames) {
							toList.add(groupName);
						}
					}
					
					Address[] members = contactList.getEmailsAsAddresses();
					for(Address member:members) {
						ccList.add(member);
					}
				}
			}
			
			//add bcc contact lists
			List<Address> bccList = new ArrayList<Address>();
			if(bccLists != null) {
				for (ContactList contactList : bccLists) {
					if(StringHelper.containsNonWhitespace(contactList.getName())) {
						Address[] groupNames = InternetAddress.parse(contactList.getRFC2822Name() + ";");
						for(Address groupName:groupNames) {
							toList.add(groupName);
						}
					}
					
					Address[] members = contactList.getEmailsAsAddresses();
					for(Address member:members) {
						bccList.add(member);
					}
				}
			}
			
			Address[] tos = toList.toArray(new Address[toList.size()]);
			Address[] ccs = ccList.toArray(new Address[ccList.size()]);
			Address[] bccs = bccList.toArray(new Address[bccList.size()]);
			return createMimeMessage(from, tos, ccs, bccs, subject, body, attachments, result);
		} catch (MessagingException e) {
			logError("", e);
			return null;
		}
	}
	
	private Address createAddress(String address) throws AddressException {
		if(address == null) return null;
		return new InternetAddress(address);
	}
	
	private Address createAddressWithName(String address, String name) throws UnsupportedEncodingException, AddressException {
		InternetAddress add = new InternetAddress(address, name);
		try {
			add.validate();
		} catch (AddressException e) {
			throw e;
		}
		return add;
	}
	
	private Address createFromAddress(String address, MailerResult result) throws AddressException {
		try {
			Address add = new InternetAddress(address);
			return add;
		} catch (AddressException e) {
			result.setReturnCode(MailerResult.SENDER_ADDRESS_ERROR);
			throw e;
		}
	}
	
	private boolean createAddress(List<Address> addressList, String address, MailerResult result) throws AddressException {
		Address add = createAddress(address);
		if(add != null) {
			addressList.add(add);
		}
		return true;
	}
	
	private boolean createAddress(List<Address> addressList, DBMailRecipient recipient, boolean force, MailerResult result, boolean error) {
		String emailAddress = recipient.getEmailAddress();
		if(recipient.getRecipient() == null) {
			try {
				Address address = createAddress(emailAddress);
				if(address != null) {
					addressList.add(address);
					return true;
				} else {
					if(error) {
						result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
					}
				}
			} catch (AddressException e) {
				if(error) {
					result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
				}
			}
		} else {
			if(force || wantRealMailToo(recipient.getRecipient())) {
				if(!StringHelper.containsNonWhitespace(emailAddress)) {
					emailAddress = recipient.getRecipient().getUser().getProperty(UserConstants.EMAIL, null);
				}
				try {
					Address address = createAddress(emailAddress);
					if(address != null) {
						addressList.add(address);
						return true;
					} else {
						result.addFailedIdentites(recipient.getRecipient());
						if(error) {
							result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
						}
					}
				} catch (AddressException e) {
					result.addFailedIdentites(recipient.getRecipient());
					if(error) {
						result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
					}
				}
			}
		}
		return false;
	}
	
	private Address createAddress(Identity recipient, MailerResult result, boolean error) {
		if(recipient != null) {
			String emailAddress = recipient.getUser().getProperty(UserConstants.EMAIL, null);
			Address address;
			try {
				address = createAddress(emailAddress);
				if(address == null) {
					result.addFailedIdentites(recipient);
					if(error) {
						result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
					}
				}
				return address;
			} catch (AddressException e) {
				result.addFailedIdentites(recipient);
				if(error) {
					result.setReturnCode(MailerResult.RECIPIENT_ADDRESS_ERROR);
				}
			}
		}
		return null;
	}
	
	private Address createFromAddress(Identity recipient, MailerResult result) {
		if(recipient != null) {
			String emailAddress = recipient.getUser().getProperty(UserConstants.EMAIL, null);
			String name = recipient.getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + recipient.getUser().getProperty(UserConstants.LASTNAME, null); 
			Address address;
			try {
				address = createAddressWithName(emailAddress, name);
				if(address == null) {
					result.addFailedIdentites(recipient);
					result.setReturnCode(MailerResult.SENDER_ADDRESS_ERROR);
				}
				return address;
			} catch (AddressException e) {
				result.addFailedIdentites(recipient);
				result.setReturnCode(MailerResult.SENDER_ADDRESS_ERROR);			
			} catch (UnsupportedEncodingException e) {
				result.addFailedIdentites(recipient);
				result.setReturnCode(MailerResult.SENDER_ADDRESS_ERROR);	
			}
		}
		return null;
	}
	
	private void sendRealMessage(Address from, List<Address> toList, List<Address> ccList, List<Address> bccList, String subject, String body,
			List<File> attachments, MailerResult result) {
		
		Address[] tos = null;
		if(toList != null && !toList.isEmpty()) {
			tos = new Address[toList.size()];
			tos = toList.toArray(tos);
		}
		
		Address[] ccs = null;
		if(ccList != null && !ccList.isEmpty()) {
			ccs = new Address[ccList.size()];
			ccs = ccList.toArray(ccs);
		}
		
		Address[] bccs = null;
		if(bccList != null && !bccList.isEmpty()) {
			bccs = new Address[bccList.size()];
			bccs = bccList.toArray(bccs);
		}

		MimeMessage msg = createMimeMessage(from, tos, ccs, bccs, subject, body, attachments, result);
		sendMessage(msg, result);
	}
	
	private MimeMessage createForwardMimeMessage(Address from, Address to, String subject, String body,
			List<DBMailAttachment> attachments, MailerResult result) {
		
		try {
			Address convertedFrom = getRawEmailFromAddress(from);
			MimeMessage msg = createMessage(convertedFrom);
			msg.setFrom(from);
			msg.setSubject(subject, "utf-8");

			if(to != null) {
				msg.addRecipient(RecipientType.TO, to);
			}

			if (attachments != null && !attachments.isEmpty()) {
				// with attachment use multipart message
				Multipart multipart = new MimeMultipart();
				// 1) add body part
				BodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(body);
				multipart.addBodyPart(messageBodyPart);
				// 2) add attachments
				for (DBMailAttachment attachment : attachments) {
					// abort if attachment does not exist
					if (attachment == null || attachment.getSize()  <= 0) {
						result.setReturnCode(MailerResult.ATTACHMENT_INVALID);
						logError("Tried to send mail wit attachment that does not exist::"
								+ (attachment == null ? null : attachment.getName()), null);
						return msg;
					}
					messageBodyPart = new MimeBodyPart();
					
					DBMailAttachmentData data = getAttachmentWithData(attachment.getKey());
					DataSource source = new ByteArrayDataSource(data.getDatas(), attachment.getMimetype());
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(attachment.getName());
					multipart.addBodyPart(messageBodyPart);
				}
				// Put parts in message
				msg.setContent(multipart);
			} else {
				// without attachment everything is easy, just set as text
				msg.setText(body, "utf-8");
			}
			msg.setSentDate(new Date());
			msg.saveChanges();
			return msg;
		} catch (MessagingException e) {
			logError("", e);
			return null;
		}
	}
	
	/**
	 * 
	 * @param bounceAdress must be a raw email, without anything else (no "bla bli <bla@bli.ch>" !)
	 * @return
	 */
	public MimeMessage createMessage(Address bounceAdress) {
		String mailhost = WebappHelper.getMailConfig("mailhost");
		String mailhostTimeout = WebappHelper.getMailConfig("mailTimeout");
		boolean sslEnabled = Boolean.parseBoolean(WebappHelper.getMailConfig("sslEnabled"));
		boolean sslCheckCertificate = Boolean.parseBoolean(WebappHelper.getMailConfig("sslCheckCertificate"));
		
		Authenticator smtpAuth;
		if (WebappHelper.isMailHostAuthenticationEnabled()) {
			String smtpUser = WebappHelper.getMailConfig("smtpUser");
			String smtpPwd = WebappHelper.getMailConfig("smtpPwd");
			smtpAuth = new MailerSMTPAuthenticator(smtpUser, smtpPwd);
		} else {
			smtpAuth = null;
		}
		
		Properties p = new Properties();
		p.put("mail.smtp.from", bounceAdress.toString());
		p.put("mail.smtp.host", mailhost);
		p.put("mail.smtp.timeout", mailhostTimeout);
		p.put("mail.smtp.connectiontimeout", mailhostTimeout);
		p.put("mail.smtp.ssl.enable", sslEnabled);
		p.put("mail.smtp.ssl.checkserveridentity", sslCheckCertificate);
		Session mailSession;
		if (smtpAuth == null) {
			mailSession = javax.mail.Session.getInstance(p);
		} else {
			// use smtp authentication from configuration
			p.put("mail.smtp.auth", "true");
			mailSession = Session.getDefaultInstance(p, smtpAuth); 
		}
		if (isLogDebugEnabled()) {
			// enable mail session debugging on console
			mailSession.setDebug(true);
		}
		return new MimeMessage(mailSession);
	}
	
	// converts an address "bla bli <bla@bli.ch>" => "bla@bli.ch"
	private InternetAddress getRawEmailFromAddress(Address address) throws AddressException{
		InternetAddress fromAddress = new InternetAddress(address.toString());
		String fromPlainAddress = fromAddress.getAddress();
		return new InternetAddress(fromPlainAddress);
	}
	
	public MimeMessage createMimeMessage(Address from, Address[] tos, Address[] ccs, Address[] bccs, String subject, String body,
			List<File> attachments, MailerResult result) {
		
		try {
			//	see FXOLAT-74: send all mails as <fromemail> (in config) to have a valid reverse lookup and therefore pass spam protection.
			// following doesn't work correctly, therefore add bounce-address in message already
			Address convertedFrom = getRawEmailFromAddress(from); 
			MimeMessage msg = createMessage(convertedFrom);
			String uri = Settings.createServerURI();
			Address viewableFrom = createAddressWithName(WebappHelper.getMailConfig("mailFrom"), "OLAT @ " + uri);
			msg.setFrom(viewableFrom);
			msg.setSubject(subject, "utf-8");
			// reply to can only be an address without name (at least for postfix!), see FXOLAT-312
			msg.setReplyTo(new Address[] { convertedFrom });

			if(tos != null && tos.length > 0) {
				msg.addRecipients(RecipientType.TO, tos);
			}
			
			if(ccs != null && ccs.length > 0) {
				msg.addRecipients(RecipientType.CC, ccs);
			}
			
			if(bccs != null && bccs.length > 0) {
				msg.addRecipients(RecipientType.BCC, bccs);
			}

			if (attachments != null && !attachments.isEmpty()) {
				// with attachment use multipart message
				Multipart multipart = new MimeMultipart();
				// 1) add body part
				BodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(body);
				multipart.addBodyPart(messageBodyPart);
				// 2) add attachments
				for (File attachmentFile : attachments) {
					// abort if attachment does not exist
					if (attachmentFile == null || !attachmentFile.exists()) {
						result.setReturnCode(MailerResult.ATTACHMENT_INVALID);
						logError("Tried to send mail wit attachment that does not exist::"
								+ (attachmentFile == null ? null : attachmentFile.getAbsolutePath()), null);
						return msg;
					}
					messageBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(attachmentFile);
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(attachmentFile.getName());
					multipart.addBodyPart(messageBodyPart);
				}
				// Put parts in message
				msg.setContent(multipart);
			} else {
				// without attachment everything is easy, just set as text
				msg.setText(body, "utf-8");
			}
			msg.setSentDate(new Date());
			msg.saveChanges();
			return msg;
		} catch (MessagingException e) {
			result.setReturnCode(MailerResult.SEND_GENERAL_ERROR);
			logError("", e);
			return null;
		} catch (UnsupportedEncodingException e) {
			result.setReturnCode(MailerResult.SENDER_ADDRESS_ERROR);
			logError("", e);
			return null;
		}
	}
	
	public void sendMessage(MimeMessage msg, MailerResult result){
		try{
			if(Settings.isJUnitTest()) {
				//we want not send really e-mails
			} else if (mailModule.isMailHostEnabled() && result.getReturnCode() == MailerResult.OK) {
				// now send the mail
				Transport.send(msg);
			} else {
				result.setReturnCode(MailerResult.MAILHOST_UNDEFINED);
			}
		} catch (MessagingException e) {
			result.setReturnCode(MailerResult.SEND_GENERAL_ERROR);
			logWarn("Could not send mail", e);
		}
	}

}
