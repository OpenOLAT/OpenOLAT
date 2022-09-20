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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.model.DBMail;
import org.olat.core.util.mail.model.DBMailLight;
import org.olat.core.util.mail.model.DBMailRecipient;
import org.olat.core.util.mail.ui.MailContextResolver;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataExportable;
import org.olat.user.UserManager;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MailUserDataManager implements UserDataDeletable, UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(MailUserDataManager.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private MailContextResolver contextResolver;
	

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		//set as deleted all recipients
		log.info("Delete intern messages for identity::{}", identity.getKey());
		
		Collection<DBMailLight> inbox = new HashSet<>(mailManager.getInbox(identity, null, Boolean.FALSE, null, 0, 0));
		for(DBMailLight inMail:inbox) {
			mailManager.delete(inMail, identity, true);
		}

		Collection<DBMailLight> outbox = new HashSet<>(mailManager.getOutbox(identity, 0, 0, false));
		for(DBMailLight outMail:outbox) {
			mailManager.delete(outMail, identity, true);
		}
		
		log.info("Delete {} messages in INBOX and {} in OUTBOX for identity::{}", inbox.size(), outbox.size(), identity.getKey());
	}

	@Override
	public String getExporterID() {
		return "mail";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		Map<String,String> bpToContexts = new HashMap<>();
		exportInbox(identity, bpToContexts, manifest, archiveDirectory, locale);
		exportOutbox(identity, bpToContexts, manifest, archiveDirectory, locale);
	}
	
	private void exportInbox(Identity identity, Map<String,String> bpToContexts, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File inbox = new File(archiveDirectory, "MailInbox.xlsx");

		try(OutputStream out = new FileOutputStream(inbox);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			
			Row header = sheet.newRow();
			header.addCell(0, "Created");
			header.addCell(1, "Context");
			header.addCell(2, "Subject");
			header.addCell(3, "From");
			
			List<DBMailLight> mails = mailManager.getInbox(identity, null, null, null, 0, -1);
			dbInstance.commitAndCloseSession();
			for(DBMailLight mail:mails) {
				Row row = sheet.newRow();
				row.addCell(0, mail.getCreationDate(), workbook.getStyles().getDateTimeStyle());
				String businessPath = mail.getContext().getBusinessPath();
				if(StringHelper.containsNonWhitespace(businessPath)) {
					String contextName = bpToContexts.computeIfAbsent(businessPath, bp -> contextResolver.getName(businessPath, locale));
					row.addCell(1, contextName);	
				}
				row.addCell(2, mail.getSubject());
				
				DBMailRecipient from = mail.getFrom();
				if(from != null) {
					if(from.getRecipient() != null) {
						row.addCell(3, userManager.getUserDisplayName(from.getRecipient()));
					} else if(from.getEmailAddress() != null) {
						row.addCell(3, from.getEmailAddress());
					}
				}
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		manifest.appendFile(inbox.getName());
	}
	
	private void exportOutbox(Identity identity, Map<String,String> bpToContexts, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File outbox = new File(archiveDirectory, "MailOutbox.xlsx");

		try(OutputStream out = new FileOutputStream(outbox);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			
			Row header = sheet.newRow();
			header.addCell(0, "Created");
			header.addCell(1, "Context");
			header.addCell(2, "Subject");
			header.addCell(3, "Content");
			
			int count = 0;
			List<DBMailLight> mails = mailManager.getOutbox(identity, 0, -1, false);
			for(DBMailLight mail:mails) {
				Row row = sheet.newRow();
				DBMail mailWithContent = mailManager.getMessageByKey(mail.getKey());
				row.addCell(0, mailWithContent.getCreationDate(), workbook.getStyles().getDateTimeStyle());
				String businessPath = mail.getContext().getBusinessPath();
				if(StringHelper.containsNonWhitespace(businessPath)) {
					String contextName = bpToContexts.computeIfAbsent(businessPath, bp -> contextResolver.getName(businessPath, locale));
					row.addCell(1, contextName);	
				}
				row.addCell(2, mailWithContent.getSubject());
				row.addCell(3, Formatter.truncate(mailWithContent.getBody(), 32000));
				
				if(count++ % 25 == 0) {
					dbInstance.commitAndCloseSession();
				}
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}
		manifest.appendFile(outbox.getName());
	}

}
