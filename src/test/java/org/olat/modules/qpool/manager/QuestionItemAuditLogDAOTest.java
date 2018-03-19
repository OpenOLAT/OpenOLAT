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
package org.olat.modules.qpool.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemAuditLogDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QuestionItemAuditLogDAO sut;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionDao;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private LicenseService licenseService;
	
	@Test
	public void shouldPersistAuditLog() {
		QItemType qItemType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("qitem-audit-log");
		QuestionItem item = questionDao.createAndPersist(id, "NGC 55", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		licenseService.loadOrCreateLicense(item);
		QuestionItemAuditLog auditLog = qpoolService.createAuditLogBuilder(id, QuestionItemAuditLog.Action.CREATE_QUESTION_ITEM_NEW)
				.withBefore(item)
				.withAfter(item)
				.withMessage("item was created")
				.create();
		dbInstance.commitAndCloseSession();
		
		sut.persist(auditLog);
		dbInstance.commitAndCloseSession();
		
		List<QuestionItemAuditLog> auditLogs = sut.getAuditLogByQuestionItem(item);
		QuestionItemAuditLog loadedAuditLog = auditLogs.get(0);
		assertThat(loadedAuditLog.getAuthorKey()).isNotNull();
		assertThat(loadedAuditLog.getBefore()).isNotNull();
		assertThat(loadedAuditLog.getAfter()).isNotNull();
		assertThat(loadedAuditLog.getLicenseBefore()).isNotNull();
		assertThat(loadedAuditLog.getLicenseAfter()).isNotNull();
		assertThat(loadedAuditLog.getMessage()).isNotNull();
	}
	
	@Test
	public void shouldFindAuditLogByQuestionItem() {
		QItemType qItemType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("qitem-audit-log");
		QuestionItem item = questionDao.createAndPersist(id, "NGC 55", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItemAuditLog auditLog = qpoolService.createAuditLogBuilder(id, QuestionItemAuditLog.Action.CREATE_QUESTION_ITEM_NEW).withBefore(item).create();
		sut.persist(auditLog);
		auditLog = qpoolService.createAuditLogBuilder(id, QuestionItemAuditLog.Action.UPDATE_QUESTION).withBefore(item).create();
		sut.persist(auditLog);
		auditLog = qpoolService.createAuditLogBuilder(id, QuestionItemAuditLog.Action.UPDATE_QUESTION_ITEM_METADATA).withBefore(item).create();
		sut.persist(auditLog);
		QuestionItem otherItem = questionDao.createAndPersist(id, "NGC 55", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		auditLog = qpoolService.createAuditLogBuilder(id, QuestionItemAuditLog.Action.UPDATE_QUESTION_ITEM_METADATA).withBefore(otherItem).create();
		sut.persist(auditLog);
		dbInstance.commitAndCloseSession();
		
		List<QuestionItemAuditLog> auditLogs = sut.getAuditLogByQuestionItem(item);
		
		assertThat(auditLogs).hasSize(3);
	}

	
	@Test
	public void shouldConvertToXMLAndBack() {
		QItemType qItemType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("qitem-audit-log");
		String title = "NGC 55";
		String format = QTI21Constants.QTI_21_FORMAT;
		String language = Locale.ENGLISH.getLanguage();
		QuestionItemImpl item = questionDao.createAndPersist(id, title, format, language, null, null, null, qItemType);
		
		String xml = sut.toXml(item);
		QuestionItem itemFromXml = sut.questionItemFromXml(xml);
		
		assertThat(itemFromXml.getTitle()).isEqualTo(title);
		assertThat(itemFromXml.getFormat()).isEqualTo(format);
		assertThat(itemFromXml.getLanguage()).isEqualTo(language);
	}
}
