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

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementAuditLog;
import org.olat.modules.curriculum.CurriculumElementAuditLog.Action;
import org.olat.modules.curriculum.CurriculumElementAuditLog.ActionTarget;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.model.CurriculumAuditLogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumElementAuditLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CurriculumElementAuditLog createAuditLog(Action action, ActionTarget target, String before, String after,
			Curriculum curriculum, CurriculumElement element, Identity actor) {
		CurriculumAuditLogImpl auditLog = new CurriculumAuditLogImpl();
		auditLog.setCreationDate(new Date());
		auditLog.setAction(action);
		auditLog.setActionTarget(target);
		auditLog.setBefore(before);
		auditLog.setAfter(after);
		auditLog.setCurriculum(curriculum);
		auditLog.setCurriculumElement(element);
		auditLog.setIdentity(actor);
		dbInstance.getCurrentEntityManager().persist(auditLog);
		return auditLog;
	}
	
	public List<CurriculumElementAuditLog> loadAuditLogs(CurriculumElementRef element) {
		String query = """
				select auditlog from curriculumauditlog auditlog
				left join fetch auditlog.curriculum cur
				left join fetch auditlog.curriculumElement curEl
				left join fetch auditlog.identity ident
				where curEl.key=:elementKey""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, CurriculumElementAuditLog.class)
				.setParameter("elementKey", element.getKey())
				.getResultList();
	}
	
	public int deleteByCurriculumElement(CurriculumElementRef element) {
		String query = "delete from curriculumauditlog auditlog where auditlog.curriculumElement.key=:elementKey";
		return dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("elementKey", element.getKey())
				.executeUpdate();
	}
}
