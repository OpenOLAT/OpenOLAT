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
package org.olat.modules.lecture.manager;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeRef;
import org.olat.modules.lecture.AbsenceNoticeToRepositoryEntry;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureParticipantSummary;
import org.olat.modules.lecture.model.AbsenceCategoryImpl;
import org.olat.modules.lecture.model.AbsenceNoticeImpl;
import org.olat.modules.lecture.model.AbsenceNoticeRelationsAuditImpl;
import org.olat.modules.lecture.model.AbsenceNoticeToLectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockAuditLogImpl;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockRollCallImpl;
import org.olat.modules.lecture.model.LectureParticipantSummaryImpl;
import org.olat.modules.lecture.model.ReasonImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 11 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureBlockAuditLogDAO {
	
	private static final Logger log = Tracing.createLoggerFor(LectureBlockAuditLogDAO.class);
	
	@Autowired
	private DB dbInstance;
	
	private static final XStream lectureBlockXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(lectureBlockXStream);
		lectureBlockXStream.ignoreUnknownElements();
		
		lectureBlockXStream.alias("lectureBlock", LectureBlockImpl.class);
		lectureBlockXStream.omitField(LectureBlockImpl.class, "entry");
		lectureBlockXStream.omitField(LectureBlockImpl.class, "teacherGroup");
		lectureBlockXStream.omitField(LectureBlockImpl.class, "groups");
		lectureBlockXStream.omitField(LectureBlockImpl.class, "lastModified");
		
		lectureBlockXStream.alias("reason", ReasonImpl.class);
		lectureBlockXStream.omitField(ReasonImpl.class, "lastModified");
		lectureBlockXStream.omitField(ReasonImpl.class, "creationDate");
		lectureBlockXStream.omitField(ReasonImpl.class, "description");
	}

	private static final XStream rollCallXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(rollCallXStream);
		rollCallXStream.alias("rollcall", LectureBlockRollCallImpl.class);
		rollCallXStream.ignoreUnknownElements();
		rollCallXStream.omitField(LectureBlockRollCallImpl.class, "identity");
		rollCallXStream.omitField(LectureBlockRollCallImpl.class, "lectureBlock");
		rollCallXStream.omitField(LectureBlockRollCallImpl.class, "lastModified");
	}
	
	private static final XStream summaryXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(summaryXStream);
		summaryXStream.alias("summary", LectureParticipantSummaryImpl.class);
		summaryXStream.ignoreUnknownElements();
		summaryXStream.omitField(LectureParticipantSummaryImpl.class, "identity");
		summaryXStream.omitField(LectureParticipantSummaryImpl.class, "entry");
	}
	
	private static final XStream absenceNoticeXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(absenceNoticeXStream);
		absenceNoticeXStream.alias("absenceNotice", AbsenceNoticeImpl.class);
		absenceNoticeXStream.ignoreUnknownElements();
		absenceNoticeXStream.omitField(LectureParticipantSummaryImpl.class, "identity");
		absenceNoticeXStream.omitField(LectureParticipantSummaryImpl.class, "entry");
		absenceNoticeXStream.alias("identity", IdentityImpl.class);
		absenceNoticeXStream.omitField(IdentityImpl.class, "user");
		absenceNoticeXStream.alias("absenceCategory", AbsenceCategoryImpl.class);
	}
	
	private static final XStream absenceNoticeRelationsXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(absenceNoticeRelationsXStream);
		absenceNoticeRelationsXStream.alias("absenceNoticeRelations", AbsenceNoticeRelationsAuditImpl.class);
		absenceNoticeRelationsXStream.ignoreUnknownElements();
		
		absenceNoticeRelationsXStream.omitField(AbsenceNoticeToLectureBlockImpl.class, "lectureToBlock");
		absenceNoticeRelationsXStream.omitField(AbsenceNoticeToRepositoryEntry.class, "lectureToEntry");
		absenceNoticeRelationsXStream.omitField(AbsenceNoticeToLectureBlockImpl.class, "absenceNotice");
		absenceNoticeRelationsXStream.omitField(AbsenceNoticeToRepositoryEntry.class, "absenceNotice");
		
		absenceNoticeRelationsXStream.alias("absenceNotice", AbsenceNoticeImpl.class);
		absenceNoticeRelationsXStream.omitField(AbsenceNoticeImpl.class, "identity");
		absenceNoticeRelationsXStream.omitField(AbsenceNoticeImpl.class, "notifier");
		absenceNoticeRelationsXStream.omitField(AbsenceNoticeImpl.class, "authorizer");
		
		absenceNoticeRelationsXStream.alias("lectureBlock", LectureBlockImpl.class);
		absenceNoticeRelationsXStream.omitField(LectureBlockImpl.class, "entry");
		absenceNoticeRelationsXStream.omitField(LectureBlockImpl.class, "teacherGroup");
		absenceNoticeRelationsXStream.omitField(LectureBlockImpl.class, "groups");
		absenceNoticeRelationsXStream.omitField(LectureBlockImpl.class, "lastModified");
		absenceNoticeRelationsXStream.omitField(LectureBlockImpl.class, "taxonomyLevels");
		
		absenceNoticeRelationsXStream.alias("repositoryEntry", RepositoryEntry.class);
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "olatResource");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "groups");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "organisations");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "taxonomyLevels");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "description");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "objectives");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "requirements");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "location");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "credits");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "expenditureOfWork");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "lifecycle");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "deletedBy");
		absenceNoticeRelationsXStream.omitField(RepositoryEntry.class, "statistics");
	
		absenceNoticeRelationsXStream.alias("rollcall", LectureBlockRollCallImpl.class);
		absenceNoticeRelationsXStream.ignoreUnknownElements();
		absenceNoticeRelationsXStream.omitField(LectureBlockRollCallImpl.class, "identity");
		absenceNoticeRelationsXStream.omitField(LectureBlockRollCallImpl.class, "lastModified");

		absenceNoticeRelationsXStream.alias("absenceCategory", AbsenceCategoryImpl.class);
	}

	public void auditLog(LectureBlockAuditLog.Action action, String before, String after, String message,
			LectureBlockRef lectureBlock, LectureBlockRollCall rollCall,
			RepositoryEntryRef entry, IdentityRef assessedIdentity, IdentityRef author) {
		LectureBlockAuditLogImpl auditLog = new LectureBlockAuditLogImpl();
		auditLog.setCreationDate(new Date());
		auditLog.setAction(action.name());
		auditLog.setBefore(before);
		auditLog.setAfter(after);
		auditLog.setMessage(message);
		if(lectureBlock != null) {
			auditLog.setLectureBlockKey(lectureBlock.getKey());
		}
		if(rollCall != null) {
			auditLog.setRollCallKey(rollCall.getKey());
		}
		if(entry != null) {
			auditLog.setEntryKey(entry.getKey());
		}
		if(assessedIdentity != null) {
			auditLog.setIdentityKey(assessedIdentity.getKey());
		}
		if(author != null) {
			auditLog.setAuthorKey(author.getKey());
		}
		dbInstance.getCurrentEntityManager().persist(auditLog);
	}
	
	public void auditLog(LectureBlockAuditLog.Action action, String before, String after, String message,
			AbsenceNoticeRef absenceNotice, IdentityRef assessedIdentity, IdentityRef author) {
		LectureBlockAuditLogImpl auditLog = new LectureBlockAuditLogImpl();
		auditLog.setCreationDate(new Date());
		auditLog.setAction(action.name());
		auditLog.setBefore(before);
		auditLog.setAfter(after);
		auditLog.setMessage(message);
		if(absenceNotice != null) {
			auditLog.setAbsenceNoticeKey(absenceNotice.getKey());
		}
		if(assessedIdentity != null) {
			auditLog.setIdentityKey(assessedIdentity.getKey());
		}
		if(author != null) {
			auditLog.setAuthorKey(author.getKey());
		}
		dbInstance.getCurrentEntityManager().persist(auditLog);
	}
	
	public List<LectureBlockAuditLog> getAuditLog(LectureBlockRef lectureBlock) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select log from lectureblockauditlog log where log.lectureBlockKey=:lectureBlockKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockAuditLog.class)
				.setParameter("lectureBlockKey", lectureBlock.getKey())
				.getResultList();
	}
	
	public List<LectureBlockAuditLog> getAuditLog(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select log from lectureblockauditlog log where log.identityKey=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockAuditLog.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<LectureBlockAuditLog> getAuditLog(RepositoryEntryRef entry, IdentityRef identity, LectureBlockAuditLog.Action action) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select log from lectureblockauditlog log where log.entryKey=:repoEntryKey and log.identityKey=:identityKey and log.action=:action");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockAuditLog.class)
				.setParameter("repoEntryKey", entry.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("action", action.name())
				.getResultList();
	}
	
	public List<LectureBlockAuditLog> getAuditLog(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select log from lectureblockauditlog log where log.entryKey=:repoEntryKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockAuditLog.class)
				.setParameter("repoEntryKey", entry.getKey())
				.getResultList();
	}
	
	public int moveAudit(Long lectureBlockKey, Long originRepositoryEntry, Long targetRepositoryEntry) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("update lectureblockauditlog log set log.entryKey=:targetEntryKey where log.entryKey=:repoEntryKey and log.lectureBlockKey=:lectureBlockKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("targetEntryKey", targetRepositoryEntry)
				.setParameter("repoEntryKey", originRepositoryEntry)
				.setParameter("lectureBlockKey", lectureBlockKey)
				.executeUpdate();
	}
	
	public String toXml(LectureBlock lectureBlock) {
		if(lectureBlock == null) return null;
		return lectureBlockXStream.toXML(lectureBlock);
	}
	
	public LectureBlock lectureBlockFromXml(String xml) {
		if(StringHelper.containsNonWhitespace(xml)) {
			try {
				Object obj = lectureBlockXStream.fromXML(xml);
				if(obj instanceof LectureBlock) {
					return (LectureBlock)obj;
				}
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}
		return null;
	}
	
	public String toXml(LectureBlockRollCall rollCall) {
		if(rollCall == null) return null;
		return rollCallXStream.toXML(rollCall);
	}
	
	public LectureBlockRollCall rollCallFromXml(String xml) {
		if(StringHelper.containsNonWhitespace(xml)) {
			try {
				Object obj = rollCallXStream.fromXML(xml);
				if(obj instanceof LectureBlockRollCall) {
					return (LectureBlockRollCall)obj;
				}
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}
		return null;
	}
	
	public String toXml(LectureParticipantSummary summary) {
		if(summary == null) return null;
		return summaryXStream.toXML(summary);
	}

	public LectureParticipantSummary summaryFromXml(String xml) {
		if(StringHelper.containsNonWhitespace(xml)) {
			try {
				Object obj = summaryXStream.fromXML(xml);
				if(obj instanceof LectureParticipantSummary) {
					return (LectureParticipantSummary)obj;
				}
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}
		return null;
	}
	
	public String toXml(AbsenceNotice absenceNotice) {
		if(absenceNotice == null) return null;
		return absenceNoticeXStream.toXML(absenceNotice);
	}

	public AbsenceNotice absenceNoticeFromXml(String xml) {
		if(StringHelper.containsNonWhitespace(xml)) {
			try {
				Object obj = absenceNoticeXStream.fromXML(xml);
				if(obj instanceof AbsenceNotice) {
					return (AbsenceNotice)obj;
				}
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}
		return null;
	}
	
	public String toXml(AbsenceNoticeRelationsAuditImpl absenceNoticeAudit) {
		if(absenceNoticeAudit == null) return null;
		return absenceNoticeRelationsXStream.toXML(absenceNoticeAudit);
	}
	
}
