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
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.SortKey;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.model.PoolImpl;
import org.olat.modules.qpool.model.PoolToItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qpoolDao")
public class PoolDAO {
	
	@Autowired
	private DB dbInstance;
	
	
	public Pool createPool(String name) {
		PoolImpl pool = new PoolImpl();
		pool.setCreationDate(new Date());
		pool.setLastModified(new Date());
		pool.setName(name);
		dbInstance.getCurrentEntityManager().persist(pool);
		return pool;
	}
	
	public int getNumOfPools() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(pool) from qpool pool");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.getSingleResult().intValue();
	}
	
	public List<Pool> getPools() {
		StringBuilder sb = new StringBuilder();
		sb.append("select pool from qpool pool");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Pool.class)
				.getResultList();
	}
	
	public void addItemToPool(QuestionItem item, Pool pool) {
		PoolToItem p2i = new PoolToItem();
		p2i.setCreationDate(new Date());
		p2i.setItem(item);
		p2i.setPool(pool);
		dbInstance.getCurrentEntityManager().persist(p2i);
	}
	
	public int getNumOfItemsInPool(Pool pool) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(pool2item.item) from qpool2item pool2item where pool2item.pool.key=:poolKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("poolKey", pool.getKey())
				.getSingleResult().intValue();
	}
	
	public List<QuestionItem> getItemsOfPool(Pool pool, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select pool2item.item from qpool2item pool2item where pool2item.pool.key=:poolKey");
		PersistenceHelper.appendGroupBy(sb, "pool2item.item", orderBy);
		
		TypedQuery<QuestionItem> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItem.class)
				.setParameter("poolKey", pool.getKey());
		if(firstResult >= 0) {
			query.setFirstResult(0);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
}
