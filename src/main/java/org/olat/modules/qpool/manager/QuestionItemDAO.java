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

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("questionDao")
public class QuestionItemDAO {
	
	@Autowired
	private DB dbInstance;
	
	public QuestionItem create(String subject, QuestionType type) {
		QuestionItemImpl item = new QuestionItemImpl();
		item.setCreationDate(new Date());
		item.setLastModified(new Date());
		item.setSubject(subject);
		item.setStatus(QuestionStatus.inWork.name());
		item.setType(type.name());
		dbInstance.getCurrentEntityManager().persist(item);
		return item;
	}
	
	public int getNumOfQuestions() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item) from questionitem item");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.getSingleResult().intValue();
	}
	
	public int getNumOfFavoritItems(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item) from questionitem item")
		  .append(" where item.key in (")
		  .append("   select mark.resId from ").append(MarkImpl.class.getName()).append(" mark where mark.creator.key=:identityKey and mark.resName='QuestionItem'")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.getSingleResult().intValue();
	}
	
	public List<QuestionItem> getFavoritItems(Identity identity, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item")
		  .append(" where item.key in (")
		  .append("   select mark.resId from ").append(MarkImpl.class.getName()).append(" mark where mark.creator.key=:identityKey and mark.resName='QuestionItem'")
		  .append(" )");

		TypedQuery<QuestionItem> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItem.class)
				.setParameter("identityKey", identity.getKey());
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
}
