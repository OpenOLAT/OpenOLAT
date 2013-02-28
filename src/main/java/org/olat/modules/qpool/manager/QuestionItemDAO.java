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
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.StudyField;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.ResourceShareImpl;
import org.olat.resource.OLATResource;
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
	@Autowired
	private BaseSecurity securityManager;
	
	
	public QuestionItem create(Identity owner, String subject, String format, String language,
			StudyField field, String rootFilename, QuestionType type) {

		String uuid = UUID.randomUUID().toString();
		return create(owner, subject, format, language, field, uuid, rootFilename, type) ;
	}
	
	public QuestionItem create(Identity owner, String subject, String format, String language,
			StudyField field, String uuid, String rootFilename, QuestionType type) {
		QuestionItemImpl item = new QuestionItemImpl();
		
		
		item.setUuid(uuid);
		item.setCreationDate(new Date());
		item.setLastModified(new Date());
		item.setSubject(subject);
		item.setStatus(QuestionStatus.inWork.name());
		item.setUsage(0);
		if(type != null) {
			item.setType(type.name());
		}
		item.setFormat(format);
		item.setLanguage(language);
		item.setStudyField(field);
		item.setDirectory(generateDir(uuid));
		item.setRootFilename(rootFilename);
		SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
		item.setOwnerGroup(ownerGroup);
		dbInstance.getCurrentEntityManager().persist(item);
		if(owner != null) {
			securityManager.addIdentityToSecurityGroup(owner, ownerGroup);
		}
		return item;
	}
	
	public String generateDir(String uuid) {
		String cleanUuid = uuid.replace("-", "");
		String firstToken = cleanUuid.substring(0, 2);
		String secondToken = cleanUuid.substring(2, 4);
		String thirdToken = cleanUuid.substring(4, 6);
		String forthToken = cleanUuid.substring(6, 8);
		StringBuilder sb = new StringBuilder();
		sb.append(firstToken).append("/")
		  .append(secondToken).append("/")
		  .append(thirdToken).append("/")
		  .append(forthToken).append("/");
		return sb.toString();
	}
	
	public void addAuthors(List<Identity> authors, QuestionItem item) {
		QuestionItemImpl lockedItem = loadForUpdate(item.getKey());
		SecurityGroup secGroup = lockedItem.getOwnerGroup();
		for(Identity author:authors) {
			if(!securityManager.isIdentityInSecurityGroup(author, secGroup)) {
				securityManager.addIdentityToSecurityGroup(author, secGroup);
			}
		}
		dbInstance.commit();
	}
	
	public int countItems(Identity me) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item) from questionitem item")
		  .append(" inner join item.ownerGroup ownerGroup ")
		  .append(" where ownerGroup in (")
		  .append("   select vmember.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as vmember ")
		  .append("     where vmember.identity.key=:identityKey and vmember.securityGroup=ownerGroup")
		  .append(" )");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", me.getKey())
				.getSingleResult().intValue();
	}
	
	public List<QuestionItem> getItems(Identity me, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item")
		  .append(" inner join item.ownerGroup ownerGroup ")
		  .append(" where ownerGroup in (")
		  .append("   select vmember.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as vmember ")
		  .append("     where vmember.identity.key=:identityKey and vmember.securityGroup=ownerGroup")
		  .append(" )");

		TypedQuery<QuestionItem> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItem.class)
				.setParameter("identityKey", me.getKey());
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public List<QuestionItem> getAllItems(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item order by item.key");

		TypedQuery<QuestionItem> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItem.class);
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public void delete(List<QuestionItem> items) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		for(QuestionItem item:items) {
			QuestionItem refItem = em.getReference(QuestionItemImpl.class, item.getKey());
			em.remove(refItem);
		}
	}
	
	public QuestionItemImpl loadById(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item where item.key=:key");
		List<QuestionItemImpl> items = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemImpl.class)
				.setParameter("key", key)
				.getResultList();
		
		if(items.isEmpty()) {
			return null;
		}
		return items.get(0);
	}
	
	public QuestionItemImpl loadForUpdate(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item where item.key=:key");
		QuestionItemImpl item = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemImpl.class)
				.setParameter("key", key)
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getSingleResult();
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
	
	public void share(QuestionItem item, OLATResource resource) {
		QuestionItem lockedItem = loadForUpdate(item.getKey());
		if(!isShared(item, resource)) {
			EntityManager em = dbInstance.getCurrentEntityManager();
			ResourceShareImpl share = new ResourceShareImpl();
			share.setCreationDate(new Date());
			share.setItem(lockedItem);
			share.setResource(resource);
			em.persist(share);
		}
		dbInstance.commit();//release the lock asap
	}
	
	public void share(QuestionItem item, List<OLATResource> resources) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		QuestionItem lockedItem = loadForUpdate(item.getKey());
		for(OLATResource resource:resources) {
			if(!isShared(item, resource)) {
				ResourceShareImpl share = new ResourceShareImpl();
				share.setCreationDate(new Date());
				share.setItem(lockedItem);
				share.setResource(resource);
				em.persist(share);
			}
		}
		dbInstance.commit();//release the lock asap
	}
	
	protected boolean isShared(QuestionItem item, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(share) from qshareitem share")
		  .append(" where share.resource.key=:resourceKey and share.item.key=:itemKey");

		Number count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("itemKey", item.getKey())
				.getSingleResult();
		return count.intValue() > 0;
	}
	
	public int countSharedItemByResource(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(share.item) from qshareitem share")
		  .append(" where share.resource.key=:resourceKey");

		Number count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resource.getKey())
				.getSingleResult();
		return count.intValue();
	}
	
	public List<QuestionItem> getSharedItemByResource(OLATResource resource, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select share.item from qshareitem share")
		  .append(" where share.resource.key=:resourceKey");

		TypedQuery<QuestionItem> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItem.class)
				.setParameter("resourceKey", resource.getKey());
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public List<BusinessGroup> getResourcesWithSharedItems(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(bgi) from ").append(org.olat.group.BusinessGroupImpl.class.getName()).append(" as bgi ")
		  .append("inner join fetch bgi.ownerGroup ownerGroup ")
		  .append("inner join fetch bgi.partipiciantGroup participantGroup ")
			.append("inner join fetch bgi.waitingGroup waitingGroup ")
			.append("inner join fetch bgi.resource bgResource ")
			.append("where (ownerGroup.key in (select ownerMemberShip.securityGroup.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" ownerMemberShip ")
			.append("   where ownerMemberShip.identity.key=:identityKey ")
			.append(" ) or participantGroup.key in (select partMembership.securityGroup.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partMembership ")
			.append("  where partMembership.identity.key=:identityKey")
			.append(" )) and exists (select share from qshareitem share where share.resource=bgResource)");

		TypedQuery<BusinessGroup> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("identityKey", identity.getKey());

		return query.getResultList();
	}
	
	public int deleteFromShares(List<QuestionItem> items) {
		List<Long> keys = new ArrayList<Long>();
		for(QuestionItem item:items) {
			keys.add(item.getKey());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("delete from qshareitem share where share.item.key in (:itemKeys)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("itemKeys", keys)
				.executeUpdate();
	}
	
	
}
