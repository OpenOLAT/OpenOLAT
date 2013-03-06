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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
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
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private BaseSecurity securityManager;
	
	public PoolImpl createPool(String name) {
		PoolImpl pool = new PoolImpl();
		pool.setCreationDate(new Date());
		pool.setLastModified(new Date());
		pool.setName(name);
		SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
		pool.setOwnerGroup(ownerGroup);
		SecurityGroup participantGroup = securityManager.createAndPersistSecurityGroup();
		pool.setParticipantGroup(participantGroup);
		dbInstance.getCurrentEntityManager().persist(pool);
		return pool;
	}
	
	public int deleteFromPools(List<QuestionItem> items) {
		List<Long> keys = new ArrayList<Long>();
		for(QuestionItem item:items) {
			keys.add(item.getKey());
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from qpool2item pool2item where pool2item.item.key in (:itemKeys)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("itemKeys", keys)
				.executeUpdate();
	}
	
	public Pool updatePool(Pool pool) {
		return dbInstance.getCurrentEntityManager().merge(pool);
	}
	
	public void deletePool(Pool pool) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from qpool2item pool2item where pool2item.pool.key=:poolKey");
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("poolKey", pool.getKey())
				.executeUpdate();
		
		PoolImpl poolRef = dbInstance.getCurrentEntityManager().getReference(PoolImpl.class, pool.getKey());
		dbInstance.getCurrentEntityManager().remove(poolRef);
	}
	
	public int countPools() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(pool) from qpool pool");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.getSingleResult().intValue();
	}
	
	public List<Pool> getPools(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select pool from qpool pool");
		
		TypedQuery<Pool> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Pool.class);
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public List<Pool> getPools(QuestionItem item) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(pool) from qpool2item pool2item")
		  .append(" inner join pool2item.pool pool")
		  .append(" where pool2item.item.key=:itemKey");
		
		TypedQuery<Pool> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Pool.class)
				.setParameter("itemKey", item.getKey());
		return query.getResultList();
	}
	
	public void addItemToPool(QuestionItem item, Pool pool) {
		QuestionItem lockedItem = questionItemDao.loadForUpdate(item.getKey());
		if(!isInPool(lockedItem, pool)) {
			PoolToItem p2i = new PoolToItem();
			p2i.setCreationDate(new Date());
			p2i.setItem(lockedItem);
			p2i.setPool(pool);
			dbInstance.getCurrentEntityManager().persist(p2i);
		}
		dbInstance.commit();//release lock asap
	}
	
	protected boolean isInPool(QuestionItem item, Pool pool) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(pool2item.item) from qpool2item pool2item where pool2item.pool.key=:poolKey and pool2item.item.key=:itemKey");
		Number count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("poolKey", pool.getKey())
				.setParameter("itemKey", item.getKey())
				.getSingleResult().intValue();
		return count.intValue() > 0;
	}
	
	public int getNumOfItemsInPool(Pool pool) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(pool2item.item) from qpool2item pool2item where pool2item.pool.key=:poolKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("poolKey", pool.getKey())
				.getSingleResult().intValue();
	}
	
	public List<QuestionItem> getItemsOfPool(Pool pool, List<Long> inKeys, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from qpool2item pool2item")
		  .append(" inner join pool2item.item item ")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel ")
		  .append(" where pool2item.pool.key=:poolKey");
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		
		PersistenceHelper.appendGroupBy(sb, "pool2item.item", orderBy);
		
		TypedQuery<QuestionItem> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItem.class)
				.setParameter("poolKey", pool.getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
}
