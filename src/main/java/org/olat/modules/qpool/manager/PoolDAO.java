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
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
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
	
	public PoolImpl createPool(Identity owner, String name, boolean publicPool) {
		PoolImpl pool = new PoolImpl();
		pool.setCreationDate(new Date());
		pool.setLastModified(new Date());
		pool.setName(name);
		pool.setPublicPool(publicPool);
		SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
		pool.setOwnerGroup(ownerGroup);
		dbInstance.getCurrentEntityManager().persist(pool);
		if(owner != null) {
			securityManager.addIdentityToSecurityGroup(owner, ownerGroup);
		}
		return pool;
	}
	
	public int deleteFromPools(List<QuestionItemShort> items) {
		List<Long> keys = new ArrayList<Long>();
		for(QuestionItemShort item:items) {
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
	
	public List<Pool> getPools(Identity identity, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select pool from qpool pool")
		  .append(" inner join fetch pool.ownerGroup ownerGroup ")
		  .append(" where pool.publicPool=true")
		  .append(" or exists (from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as vmember ")
		  .append("     where vmember.identity.key=:identityKey and vmember.securityGroup=ownerGroup)");
		
		TypedQuery<Pool> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Pool.class)
				.setParameter("identityKey", identity.getKey());
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
	
	public void addItemToPool(QuestionItemShort item, List<Pool> pools, boolean editable) {
		QuestionItem lockedItem = questionItemDao.loadForUpdate(item.getKey());
		for(Pool pool:pools) {
			if(!isInPool(lockedItem, pool)) {
				PoolToItem p2i = new PoolToItem();
				p2i.setCreationDate(new Date());
				p2i.setItem(lockedItem);
				p2i.setEditable(editable);
				p2i.setPool(pool);
				dbInstance.getCurrentEntityManager().persist(p2i);
			}
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
	
	public List<QuestionItemView> getItemsOfPool(Pool pool, List<Long> inKeys, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from qpoolitem item where item.poolKey=:poolKey");
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		PersistenceHelper.appendGroupBy(sb, "item", orderBy);
		
		TypedQuery<QuestionItemView> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemView.class)
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
