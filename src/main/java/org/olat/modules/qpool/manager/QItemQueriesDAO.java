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
import java.util.Collection;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionItemView.OrderBy;
import org.olat.modules.qpool.model.ItemWrapper;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service do only the main queries for the question pool and mapped them
 * to the standardized QuestionItemView
 * 
 * 
 * Initial date: 12.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qItemQueriesDAO")
public class QItemQueriesDAO {
	
	@Autowired
	private DB dbInstance;
	
	public int countFavoritItems(SearchQuestionItemParams params) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item.key)")
		  .append(" from questionitem item, ").append(IdentityImpl.class.getName()).append(" as ident ")
		  .append(" where ident.key=:identityKey and exists (")
		  .append("   select mark.key from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator=ident and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" )");
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", params.getIdentity().getKey());
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		return query.getSingleResult().intValue();
	}
	
	public List<QuestionItemView> getFavoritItems(SearchQuestionItemParams params, Collection<Long> inKeys,
			int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item, ")
		  .append(" (select count(sgmi.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity=ident and sgmi.securityGroup=ownerGroup")
		  .append(" ) as owners,")
		  .append(" (select avg(rating.rating) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append(" ) as rating")
		  .append(" from questionitem item, ").append(IdentityImpl.class.getName()).append(" as ident ")
		  .append(" inner join fetch item.ownerGroup ownerGroup")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where ident.key=:identityKey and exists (")
		  .append("   select mark.key from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator=ident and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" )");
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null && !OrderBy.marks.name().equals(orderBy[0].getKey())) {
			appendOrderBy(sb, "item", orderBy);
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", params.getIdentity().getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for(Object[] result:results) {
			QuestionItemImpl item = (QuestionItemImpl)result[0];
			Number ownerCount = (Number)result[1];
			boolean editable = ownerCount == null ? false : ownerCount.longValue() > 0;
			Double rating = (Double)result[2];
			views.add(new ItemWrapper(item, editable, true, rating));
		}
		return views;
	}
	
	public List<QuestionItemView> getItemsOfCollection(Identity identity, QuestionItemCollection collection, Collection<Long> inKeys,
			String format, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item, ")
		  .append(" (select count(sgmi.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity=ident and sgmi.securityGroup=ownerGroup")
		  .append(" ) as owners,")
		  .append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator=ident and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" ) as marks,")
		  .append(" (select avg(rating.rating) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append(" ) as rating")
		  .append(" from qcollection2item coll2item, ").append(IdentityImpl.class.getName()).append(" as ident ")
		  .append(" inner join coll2item.item item")
		  .append(" inner join fetch item.ownerGroup ownerGroup")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where ident.key=:identityKey and coll2item.collection.key=:collectionKey");
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if(StringHelper.containsNonWhitespace(format)) {
			sb.append(" and item.format=:format");
		}
		appendOrderBy(sb, "item", orderBy);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("collectionKey", collection.getKey())
				.setParameter("identityKey", identity.getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(StringHelper.containsNonWhitespace(format)) {
			query.setParameter("format", format);
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for(Object[] result:results) {
			QuestionItemImpl item = (QuestionItemImpl)result[0];
			Number ownerCount = (Number)result[1];
			boolean editable = ownerCount == null ? false : ownerCount.longValue() > 0;
			Number markCount = (Number)result[2];
			boolean marked = markCount == null ? false : markCount.longValue() > 0;
			Double rating = (Double)result[3];
			views.add(new ItemWrapper(item, editable, marked, rating));
		}
		return views;
	}
	
	public int countItemsByAuthor(SearchQuestionItemParams params) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item) from questionitem item ")
		  .append(" where exists (")
		  .append("   select sgmi.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity.key=:authorKey and sgmi.securityGroup=item.ownerGroup")
		  .append(" )");	
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("authorKey", params.getAuthor().getKey());
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		return query.getSingleResult().intValue();
	}
	
	public List<QuestionItemView> getItemsByAuthor(SearchQuestionItemParams params,
			Collection<Long> inKeys, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item, ")
		  .append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator=ident and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" ) as marks,")
		  .append(" (select avg(rating.rating) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append(" ) as rating")
		  .append(" from questionitem item, ").append(IdentityImpl.class.getName()).append(" as ident ")
		  .append(" inner join fetch item.ownerGroup ownerGroup")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where ident.key=:identityKey and exists (")
		  .append("   select sgmi.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity.key=:authorKey and sgmi.securityGroup=ownerGroup")
		  .append(" )");
		
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		appendOrderBy(sb, "item", orderBy);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("authorKey", params.getAuthor().getKey())
				.setParameter("identityKey", params.getIdentity().getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for(Object[] result:results) {
			QuestionItemImpl item = (QuestionItemImpl)result[0];
			Number markCount = (Number)result[1];
			boolean marked = markCount == null ? false : markCount.longValue() > 0;
			Double rating = (Double)result[2];
			views.add(new ItemWrapper(item, true, marked, rating));
		}
		return views;
	}
	
	public List<QuestionItemView> getSharedItemByResource(Identity identity, OLATResource resource, List<Long> inKeys,
			String format, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item, shareditem.editable, ")
		  .append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator=ident and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" ) as marks,")
		  .append(" (select avg(rating.rating) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append(" ) as rating")
		  .append(" from qshareitem shareditem, ").append(IdentityImpl.class.getName()).append(" as ident ")
		  .append(" inner join shareditem.item item")
		  .append(" inner join fetch item.ownerGroup ownerGroup")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where ident.key=:identityKey and shareditem.resource.key=:resourceKey");
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if(StringHelper.containsNonWhitespace(format)) {
			sb.append(" and item.format=:format");
		}
		appendOrderBy(sb, "item", orderBy);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("identityKey", identity.getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(StringHelper.containsNonWhitespace(format)) {
			query.setParameter("format", format);
		}
		return processQuery(query, firstResult, maxResults);
	}
	
	public List<QuestionItemView> getItemsOfPool(SearchQuestionItemParams params, Collection<Long> inKeys,
			int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item, pool2item.editable, ")
		  .append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator=ident and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" ) as marks,")
		  .append(" (select avg(rating.rating) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append(" ) as rating")
		  .append(" from qpool2item pool2item, ").append(IdentityImpl.class.getName()).append(" as ident ")
		  .append(" inner join pool2item.item item")
		  .append(" inner join fetch item.ownerGroup ownerGroup")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where ident.key=:identityKey and pool2item.pool.key=:poolKey");
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		appendOrderBy(sb, "item", orderBy);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("poolKey", params.getPoolKey())
				.setParameter("identityKey", params.getIdentity().getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		return processQuery(query, firstResult, maxResults);
	}
	
	private List<QuestionItemView> processQuery(TypedQuery<Object[]> query, int firstResult, int maxResults) {
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for(Object[] result:results) {
			QuestionItemImpl item = (QuestionItemImpl)result[0];
			Boolean editableObj = (Boolean)result[1];
			boolean editable = editableObj == null ? false : editableObj.booleanValue();
			Number markCount = (Number)result[2];
			boolean marked = markCount == null ? false : markCount.longValue() > 0;
			Double rating = (Double)result[3];
			views.add(new ItemWrapper(item, editable, marked, rating));
		}
		return views;
	}
	
	private void appendOrderBy(StringBuilder sb, String dbRef, SortKey... orderBy) {
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			String sortKey = orderBy[0].getKey();
			boolean asc = orderBy[0].isAsc();
			sb.append(" order by ");
			switch(sortKey) {
				case "itemType": sb.append(dbRef).append(".type.type"); break;
				case "marks": sb.append("marks"); break;
				case "rating": sb.append("rating"); break;
				default: sb.append(dbRef).append(".").append(sortKey); break;
			}
			if(asc) {
				sb.append(" asc ");
			} else {
				sb.append(" desc ");
			}
		} else {
			sb.append(" order by item.key asc ");
		}
	}
}
