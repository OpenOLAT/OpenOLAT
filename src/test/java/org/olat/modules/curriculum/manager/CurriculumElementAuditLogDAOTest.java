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
package org.olat.modules.curriculum.manager;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementAuditLog;
import org.olat.modules.curriculum.CurriculumElementAuditLog.Action;
import org.olat.modules.curriculum.CurriculumElementAuditLog.ActionTarget;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementAuditLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumElementAuditLogDAO curriculumElementAuditLogDao;
	
	@Test
	public void createCurriculumElementAuditLog() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-audit-1", "Curriculum for audit", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		String before = CurriculumElementStatus.preparation.name();
		String after = CurriculumElementStatus.active.name();
		CurriculumElementAuditLog auditLog = curriculumElementAuditLogDao
				.createAuditLog(Action.CHANGE_STATUS, ActionTarget.CURRICULUM, before, after, curriculum, element, actor);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auditLog);
		Assert.assertNotNull(auditLog.getKey());
		Assert.assertNotNull(auditLog.getCreationDate());
		Assert.assertEquals(Action.CHANGE_STATUS, auditLog.getAction());
		Assert.assertEquals(ActionTarget.CURRICULUM, auditLog.getActionTarget());
		Assert.assertEquals(before, auditLog.getBefore());
		Assert.assertEquals(after, auditLog.getAfter());
		Assert.assertEquals(actor, auditLog.getIdentity());
		Assert.assertEquals(curriculum, auditLog.getCurriculum());
		Assert.assertEquals(element, auditLog.getCurriculumElement());
	}
	
	@Test
	public void loadCurriculumElementAuditLog() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-audit-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		String before = CurriculumElementStatus.preparation.name();
		String after = CurriculumElementStatus.active.name();
		CurriculumElementAuditLog auditLog = curriculumElementAuditLogDao
				.createAuditLog(Action.CHANGE_STATUS, ActionTarget.CURRICULUM_ELEMENT, before, after, curriculum, element, null);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementAuditLog> reloadAuditLogs = curriculumElementAuditLogDao.loadAuditLogs(element);
		Assert.assertNotNull(reloadAuditLogs);
		Assert.assertEquals(1, reloadAuditLogs.size());
		
		CurriculumElementAuditLog reloadAuditLog = reloadAuditLogs.get(0);
		Assert.assertEquals(reloadAuditLog, auditLog);
		Assert.assertEquals(Action.CHANGE_STATUS, reloadAuditLog.getAction());
		Assert.assertEquals(ActionTarget.CURRICULUM_ELEMENT, reloadAuditLog.getActionTarget());
		Assert.assertEquals(before, reloadAuditLog.getBefore());
		Assert.assertEquals(after, reloadAuditLog.getAfter());
		Assert.assertEquals(curriculum, reloadAuditLog.getCurriculum());
		Assert.assertEquals(element, reloadAuditLog.getCurriculumElement());
	}
	
	@Test
	public void deleteCurriculumElementAuditLog() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-audit-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		String before = CurriculumElementStatus.preparation.name();
		String after = CurriculumElementStatus.active.name();
		CurriculumElementAuditLog auditLog = curriculumElementAuditLogDao
				.createAuditLog(Action.CHANGE_STATUS, ActionTarget.CURRICULUM_ELEMENT, before, after, curriculum, element, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auditLog);
		
		int deletedRows = curriculumElementAuditLogDao.deleteByCurriculumElement(element);
		Assert.assertEquals(1, deletedRows);
	}
}
