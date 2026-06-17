/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.RejectionEmailLog;
import org.olat.modules.selectus.model.RejectionEmailLogFull;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.model.mail.RejectionEmailLogFullImpl;
import org.olat.modules.selectus.model.mail.SentEmailTemplates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("rejectionDAO")
public class RejectionDAO  {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ApplicationDAO applicationDao;
	
	public List<RejectionEmailLog> getLog(Position position) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select log from rrejectionlog log")
		  .append(" inner join fetch log.application app")
		  .append(" where app.positionKey=:positionKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RejectionEmailLog.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public RejectionEmailLogFull getFullLog(RejectionEmailLog log) {
		List<RejectionEmailLogFull> logs = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadFullEmailLogByKey", RejectionEmailLogFull.class)
				.setParameter("logKey", log.getKey())
				.getResultList();
		return logs == null || logs.isEmpty() ? null : logs.get(0);
	}
	
	public List<Long> getRejectedApplicationKeys(PositionRef position) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select distinct app.key from rrejectionlog log")
		  .append(" inner join log.application app")
		  .append(" where log.rejected=true and app.positionKey=:positionKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public List<SentEmailTemplates> getApplicationSentEmails(PositionRef position) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select app.key, log.mailTemplate from rrejectionlog log")
		  .append(" inner join log.application app")
		  .append(" where app.positionKey=:positionKey");
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
		
		Map<Long, List<String>> applicationKeyToTemplates = new HashMap<>();
		for(Object[] rawObject:rawObjects) {
			Long applicationKey = (Long)rawObject[0];
			String mailTemplate = (String)rawObject[1];

			List<String> templates = applicationKeyToTemplates
					.computeIfAbsent(applicationKey, key -> new ArrayList<>());
			templates.add(mailTemplate);
		}

		final List<SentEmailTemplates> emails = new ArrayList<>();
		for(Map.Entry<Long, List<String>> entry:applicationKeyToTemplates.entrySet()) {
			String[] templates = entry.getValue().toArray(new String[entry.getValue().size()]);
			emails.add(new SentEmailTemplates(entry.getKey(), templates));
		}
		return emails;
	}

	public void addLog(String templateName, String subject, String content, MailAttachment attachment, boolean rejected, ApplicationLight application, MailerResult result) {
		RejectionEmailLogFullImpl log = new RejectionEmailLogFullImpl();
		log.setCreationDate(new Date());
		log.setApplication(application);
		log.setStatus(result.getReturnCode());
		log.setMailTemplate(templateName);
		log.setMailSubject(subject);
		log.setMailContent(content);
		log.setRejected(rejected);
		
		if(attachment != null && attachment.getContent() != null) {
			String filename = attachment.getFilename();
			if(!StringHelper.containsNonWhitespace(filename)) {
				filename = "Attachment.pdf";
			}
			String type = attachment.getMimeType();
			if(!StringHelper.containsNonWhitespace(type)) {
				type = DocumentType.pdf.name();
			} else if(type.indexOf('/') >= 0) {
				type = type.substring(type.indexOf('/') + 1);
			}
			Attachment data = applicationDao.setAttachmentDatas(null, filename, type, attachment.getContent());
			log.setLetter(data);
		}
		
		dbInstance.getCurrentEntityManager().persist(log);
	}

	public void deleteApplication(Application application) {
		String q = "delete from rrejectionlog log where log.application.key=:applicationKey";
		dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("applicationKey", application.getKey())
			.executeUpdate();
	}
}
