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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.qpool.model.QEducationalContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qEduContextDao")
public class QEducationalContextDAO {
	
	@Autowired
	private DB dbInstance;
	
	public QEducationalContext create(String level, boolean deletable) {
		QEducationalContext itemLevel = new QEducationalContext();
		itemLevel.setCreationDate(new Date());
		itemLevel.setLevel(level);
		itemLevel.setDeletable(deletable);
		dbInstance.getCurrentEntityManager().persist(itemLevel);
		return itemLevel;
	}
	
	public QEducationalContext loadById(Long key) {
		List<QEducationalContext> contexts = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadQEduContextById", QEducationalContext.class)
				.setParameter("contextKey", key)
				.getResultList();
		if(contexts.isEmpty()) {
			return null;
		}
		return contexts.get(0);
	}
	
	public QEducationalContext loadByLevel(String level) {
		List<QEducationalContext> contexts = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadQEduContextByLevel", QEducationalContext.class)
				.setParameter("level", level)
				.getResultList();
		if(contexts.isEmpty()) {
			return null;
		}
		return contexts.get(0);
	}
	
	public boolean delete(QEducationalContext level) {
		if(level == null) return false;
		
		QEducationalContext reloadLevel = loadById(level.getKey());
		if(reloadLevel != null && reloadLevel.isDeletable()) {
			dbInstance.getCurrentEntityManager().remove(reloadLevel);
			return true;
		}
		return false;
	}
	
	public List<QEducationalContext> getEducationalContexts() {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadQEduContexts", QEducationalContext.class)
				.getResultList();
	}
	
	public boolean isEducationalContextInUse(QEducationalContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item) from questionitem item where item.educationalContext.key=:contextKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("contextKey", context.getKey())
				.getSingleResult().intValue() > 0;
	}
}
