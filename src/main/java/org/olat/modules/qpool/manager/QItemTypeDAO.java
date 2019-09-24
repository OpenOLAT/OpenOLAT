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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QItemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("qpoolItemTypeDao")
public class QItemTypeDAO implements ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	private DB dbInstance;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		createDefaultTypes();
	}

	
	protected void createDefaultTypes() {
		List<QItemType> types = getItemTypes();
		Set<String> typeKeys = new HashSet<>();
		for(QItemType type:types) {
			typeKeys.add(type.getType().toLowerCase());
		}
		for(QuestionType defaultType:QuestionType.values()) {
			if(!typeKeys.contains(defaultType.name().toLowerCase())) {
				create(defaultType.name(), false);
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	public QItemType create(String type, boolean deletable) {
		QItemType itemType = new QItemType();
		itemType.setCreationDate(new Date());
		itemType.setType(type.toLowerCase());
		itemType.setDeletable(deletable);
		dbInstance.getCurrentEntityManager().persist(itemType);
		return itemType;
	}
	
	public QItemType loadById(Long key) {
		List<QItemType> types = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadQItemTypeById", QItemType.class)
				.setParameter("itemTypeKey", key)
				.getResultList();
		if(types.isEmpty()) {
			return null;
		}
		return types.get(0);
	}
	
	public QItemType loadByType(String type) {
		List<QItemType> types = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadQItemTypeByType", QItemType.class)
				.setParameter("itemType", type.toLowerCase())
				.getResultList();
		if(types.isEmpty()) {
			return null;
		}
		return types.get(0);
	}
	
	public int countItemUsing(QItemType type) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item) from questionitem item where item.type.key=:itemTypeKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("itemTypeKey", type.getKey())
				.getSingleResult().intValue();
	}
	
	public boolean delete(QItemType type) {
		QItemType reloadType = loadById(type.getKey());
		if(reloadType.isDeletable()) {
			dbInstance.getCurrentEntityManager().remove(reloadType);
			return true;
		}
		return false;
	}
	
	public List<QItemType> getItemTypes() {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadQItemTypes", QItemType.class)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
	}
}
