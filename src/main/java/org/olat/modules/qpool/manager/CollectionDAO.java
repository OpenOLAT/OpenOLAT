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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.model.CollectionToItem;
import org.olat.modules.qpool.model.ItemCollectionImpl;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qcollectionDao")
public class CollectionDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QuestionItemDAO questionItemDao;
	
	public QuestionItemCollection createCollection(String name, Identity identity) {
		ItemCollectionImpl collection = new ItemCollectionImpl();
		collection.setCreationDate(new Date());
		collection.setLastModified(new Date());
		collection.setName(name);
		collection.setOwner(identity);
		dbInstance.getCurrentEntityManager().persist(collection);
		return collection;
	}
	
	public QuestionItemCollection mergeCollection(QuestionItemCollection collection, String newName) {
		ItemCollectionImpl coll = loadCollectionById(collection.getKey());
		coll.setName(newName);
		return dbInstance.getCurrentEntityManager().merge(coll);
	}
	
	public ItemCollectionImpl loadCollectionById(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select coll from qcollection coll where coll.key=:key");
		List<ItemCollectionImpl> items = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ItemCollectionImpl.class)
				.setParameter("key", key)
				.getResultList();
		if(items.isEmpty()) {
			return null;
		}
		return items.get(0);
	}
	
	/**
	 * Add an item to a collection
	 * @param itemKey
	 * @param collection
	 * @return true if the item is in the collection after the call
	 */
	public boolean addItemToCollection(QuestionItemShort item, List<QuestionItemCollection> collections) {
		QuestionItemImpl lockedItem = questionItemDao.loadForUpdate(item);
		if(lockedItem == null) {
			return false;
		}
		Set<QuestionItemCollection> uniqueCollections = new HashSet<>(collections);
		for(QuestionItemCollection collection:uniqueCollections) {
			if(!isInCollection(collection, lockedItem)) {
				CollectionToItem coll2Item = new CollectionToItem();
				coll2Item.setCreationDate(new Date());
				coll2Item.setCollection(collection);
				coll2Item.setItem(lockedItem);
				dbInstance.getCurrentEntityManager().persist(coll2Item);
			}
		}
		dbInstance.commit();
		return true;
	}
	
	protected boolean isInCollection(QuestionItemCollection collection, QuestionItemImpl item) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(coll2item) from qcollection2item coll2item where coll2item.collection.key=:collectionKey and coll2item.item.key=:itemKey");
		Number count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("collectionKey", collection.getKey())
				.setParameter("itemKey", item.getKey())
				.getSingleResult().intValue();
		return count.intValue() > 0;
	}
	
	public List<Long> getItemKeysOfCollection(QuestionItemCollection collection) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(coll2item.item.key) from qcollection2item coll2item ")
		  .append(" where coll2item.collection.key=:collectionKey");
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("collectionKey", collection.getKey());
		return query.getResultList();
	}
	
	public List<QuestionItemCollection> getCollections(Identity me) {
		StringBuilder sb = new StringBuilder();
		sb.append("select coll from qcollection coll where coll.owner.key=:identityKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemCollection.class)
				.setParameter("identityKey", me.getKey())
				.getResultList();
	}
	
	public int removeItemFromCollection(List<QuestionItemShort> items, QuestionItemCollection collection) {
		if(items == null || items.isEmpty()) return 0;//noting to do
		
		List<Long> keys = new ArrayList<>();
		for(QuestionItemShort item:items) {
			keys.add(item.getKey());
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from qcollection2item coll2item where coll2item.item.key in (:itemKeys) and coll2item.collection.key=:collectionKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("itemKeys", keys)
				.setParameter("collectionKey", collection.getKey())
				.executeUpdate();
	}
	
	public int deleteItemFromCollections(List<? extends QuestionItemShort> items) {
		if(items == null || items.isEmpty()) return 0;//noting to do
		
		List<Long> keys = new ArrayList<>();
		for(QuestionItemShort item:items) {
			keys.add(item.getKey());
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from qcollection2item coll2item where coll2item.item.key in (:itemKeys)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("itemKeys", keys)
				.executeUpdate();
	}
	
	public int deleteCollection(QuestionItemCollection collection) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from qcollection2item coll2item where coll2item.collection.key in (:collectionKey)");
		int count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("collectionKey", collection.getKey())
				.executeUpdate();
		
		QuestionItemCollection coll = dbInstance.getCurrentEntityManager().getReference(ItemCollectionImpl.class, collection.getKey());
		dbInstance.getCurrentEntityManager().remove(coll);
		return 1 + count;
	}
}
