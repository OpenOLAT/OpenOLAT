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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItem2Resource;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.ResourceShareImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
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
	
	private static final Logger log = Tracing.createLoggerFor(QuestionItemDAO.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	@Autowired
	private QPoolFileStorage qpoolFileStorage;
	
	
	public QuestionItemImpl create(String title, String format, String dir, String rootFilename) {
		QuestionItemImpl item = new QuestionItemImpl();
		
		Date now = new Date();
		String uuid = UUID.randomUUID().toString();
		item.setIdentifier(uuid);
		item.setCreationDate(now);
		item.setLastModified(now);
		item.setTitle(title);
		item.setStatus(QuestionStatus.draft.name());
		item.setQuestionStatusLastModified(now);
		item.setUsage(0);
		item.setNumOfAnswerAlternatives(0);
		item.setFormat(format);
		if(dir == null) {
			item.setDirectory(qpoolFileStorage.generateDir(uuid));
		} else {
			item.setDirectory(dir);
		}
		item.setRootFilename(rootFilename);
		return item;
	}

	public QuestionItemImpl createAndPersist(Identity owner, String title, String format, String language,
			TaxonomyLevel taxonLevel, String dir, String rootFilename, QItemType type) {
		QuestionItemImpl item = create(title, format, dir, rootFilename);
		if(type != null) {
			item.setType(type);
		}
		item.setLanguage(language);
		item.setTaxonomyLevel(taxonLevel);
		persist(owner, item);
		return item;
	}
	
	public void persist(Identity owner, QuestionItemImpl item) {
		if(item.getOwnerGroup() == null) {
			SecurityGroup ownerGroup = securityGroupDao.createAndPersistSecurityGroup();
			item.setOwnerGroup(ownerGroup);
		}
		dbInstance.getCurrentEntityManager().persist(item);
		if(owner != null) {
			securityGroupDao.addIdentityToSecurityGroup(owner, item.getOwnerGroup());
		}
	}
	
	/**
	 * The method make a copy of the original question. The copy is not persisted.
	 * 
	 * @param original
	 * @return A copy of the question.
	 */
	public QuestionItemImpl copy(QuestionItemImpl original) {
		String title = "(Copy) " + original.getTitle();
		QuestionItemImpl copy = create(title, original.getFormat(), null, original.getRootFilename());
		
		//general
		copy.setMasterIdentifier(original.getIdentifier());
		copy.setDescription(original.getDescription());
		copy.setKeywords(original.getKeywords());
		copy.setCoverage(original.getCoverage());
		copy.setAdditionalInformations(original.getAdditionalInformations());
		copy.setLanguage(original.getLanguage());
		
		//classification
		copy.setTaxonomyLevel(original.getTaxonomyLevel());
		copy.setTopic(original.getTopic());
		
		//educational
		copy.setEducationalContext(original.getEducationalContext());
		copy.setEducationalLearningTime(original.getEducationalLearningTime());
		
		//item
		copy.setType(original.getType());
		copy.setDifficulty(original.getDifficulty());
		copy.setStdevDifficulty(original.getStdevDifficulty());
		copy.setDifferentiation(original.getDifferentiation());
		copy.setNumOfAnswerAlternatives(original.getNumOfAnswerAlternatives());
		copy.setUsage(0);
		copy.setAssessmentType(original.getAssessmentType());
		
		//lifecycle
		copy.setItemVersion(original.getItemVersion());
		copy.setStatus(QuestionStatus.draft.name());
		
		// management
		copy.setCorrectionTime(original.getCorrectionTime());
		
		//technical
		copy.setEditor(original.getEditor());
		copy.setEditorVersion(original.getEditorVersion());
		return copy;
	}

	public QuestionItemImpl merge(QuestionItem item) {
		if(item instanceof QuestionItemImpl) {
			((QuestionItemImpl)item).setLastModified(new Date());
		}
		return (QuestionItemImpl)dbInstance.getCurrentEntityManager().merge(item);
	}
	
	public void addAuthors(List<Identity> authors, QuestionItemShort item) {
		QuestionItemImpl lockedItem = loadForUpdate(item);
		if (lockedItem == null) return;
		
		SecurityGroup secGroup = lockedItem.getOwnerGroup();
		for(Identity author:authors) {
			if(!securityGroupDao.isIdentityInSecurityGroup(author, secGroup)) {
				securityGroupDao.addIdentityToSecurityGroup(author, secGroup);
				log.info(Tracing.M_AUDIT, "Added owner identity '{}' to item with key {} ({}, {})",
						author.getKey(), item.getKey(), item.getTitle(), item.getTopic());
			}
		}
		dbInstance.commit();
	}
	
	public void removeAuthors(List<Identity> authors, QuestionItemShort item) {
		QuestionItemImpl lockedItem = loadForUpdate(item);
		if (lockedItem == null) return;
		
		SecurityGroup secGroup = lockedItem.getOwnerGroup();
		for(Identity author:authors) {
			if(securityGroupDao.isIdentityInSecurityGroup(author, secGroup)) {
				securityGroupDao.removeIdentityFromSecurityGroup(author, secGroup);
				log.info(Tracing.M_AUDIT, "Removed owner identity '{}' from item with key {} ({}, {})",
						author.getKey(), item.getKey(), item.getTitle(), item.getTopic());
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
	
	public List<QuestionItemFull> getAllItems(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.license license")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" order by item.key");

		TypedQuery<QuestionItemFull> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemFull.class);
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public List<QuestionItemShort> getItems(TaxonomyLevelRef taxonomyLevel) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item")
		  .append(" where item.taxonomyLevel.key=:taxonomyLevelKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemShort.class)
				.setParameter("taxonomyLevelKey", taxonomyLevel.getKey())
				.getResultList();
	}
	
	public List<QuestionItem> getItemsWithOneAuthor(Identity author) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item");
		sb.append(" where exists (").append("select sgmi.key from ");
		sb.append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi");
		sb.append("   where sgmi.identity.key=:identityKey and sgmi.securityGroup=item.ownerGroup");
		sb.append(" )");
		sb.append(" and 1 = (").append("select count(sgmi.key) from ");
		sb.append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi");
		sb.append("   where sgmi.securityGroup=item.ownerGroup");
		sb.append(" )");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItem.class)
				.setParameter("identityKey", author.getKey())
				.getResultList();
	}
	
	public void delete(List<? extends QuestionItemShort> items) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		for(QuestionItemShort item:items) {
			QuestionItem refItem = loadLazyReferenceId(item.getKey());
			if(refItem != null) {
				log.info(Tracing.M_AUDIT, "Delete question item {} ({}, {})", item.getKey(), item.getTitle(), item.getTopic());
				em.remove(refItem);
			}
		}
	}
	
	/**
	 * The method only load the question item and doesn't fetch
	 * anything.
	 * 
	 * @param key The primary key of the item
	 * @return The question item or null if not found
	 */
	private QuestionItem loadLazyReferenceId(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item")
		  .append(" where item.key=:key");
		List<QuestionItem> items = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItem.class)
				.setParameter("key", key)
				.getResultList();
		return items == null || items.isEmpty() ? null : items.get(0);
	}
	
	/**
	 * The method loads the question item and fetch
	 * the taxonomy level, license, item type and
	 * educational context.
	 * 
	 * @param key The primary key of the item
	 * @return The question item or null if not found
	 */
	public QuestionItemImpl loadById(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.license license")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where item.key=:key");
		List<QuestionItemImpl> items = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemImpl.class)
				.setParameter("key", key)
				.getResultList();
		
		if(items.isEmpty()) {
			return null;
		}
		return items.get(0);
	}
	
	/**
	 * The method loads the question items and fetch
	 * the taxonomy level, license, item type and
	 * educational context.
	 * 
	 * @param identifier The identifier of the item as defined in its metadata
	 * @return The question items with the corresponding identifier
	 */
	public List<QuestionItem> loadByIdentifier(String identifier) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select item from questionitem item")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.license license")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where item.identifier=:identifier");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItem.class)
				.setParameter("identifier", identifier)
				.getResultList();
	}

	/**
	 * The method loads the question items.
	 * 
	 * @param identifiers A list of identifiers as defined in metadata's items
	 * @return The question items with the corresponding identifier
	 */
	public List<QuestionItemShort> loadShortItemsByIdentifier(List<String> identifiers) {
		if(identifiers == null || identifiers.isEmpty()) return new ArrayList<>();
	
		String q ="select item from questionitem item where item.identifier in (:identifiers)";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, QuestionItemShort.class)
				.setParameter("identifiers", identifiers)
				.getResultList();
	}
	
	public List<QuestionItemFull> loadByIds(Collection<Long> key) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select item from questionitem item")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.license license")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where item.key in (:keys)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemFull.class)
				.setParameter("keys", key)
				.getResultList();
	}
	
	public QuestionItemImpl loadForUpdate(QuestionItemShort item) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item from questionitem item where item.key=:key");
		List<QuestionItemImpl> lockedItem = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemImpl.class)
				.setParameter("key", item.getKey())
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();
		return !lockedItem.isEmpty()? lockedItem.get(0): null;
	}
	
	public int getNumOfQuestions() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item) from questionitem item");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.getSingleResult().intValue();
	}
	
	public void resetAllStatesToDraft() {
		StringBuilder sb = new StringBuilder();
		sb.append("update questionitem item set item.status='").append(QuestionStatus.draft.toString()).append("'");
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.executeUpdate();
	}

	public List<Long> getFavoritKeys(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(mark.resId) from ").append(MarkImpl.class.getName()).append(" mark ")
		  .append(" where mark.creator.key=:identityKey and mark.resName='QuestionItem'");
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey());
		return query.getResultList();
	}
	
	public void share(QuestionItem item, OLATResource resource) {
		QuestionItem lockedItem = loadForUpdate(item);
		if (lockedItem == null) return;
		
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
	
	public void share(QuestionItemShort item, List<OLATResource> resources, boolean editable) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		QuestionItem lockedItem = loadForUpdate(item);
		if (lockedItem == null) return;
		
		for(OLATResource resource:resources) {
			if(!isShared(lockedItem, resource)) {
				ResourceShareImpl share = new ResourceShareImpl();
				share.setCreationDate(new Date());
				share.setItem(lockedItem);
				share.setEditable(editable);
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
	
	public List<BusinessGroup> getResourcesWithSharedItems(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(bgi) from businessgroup as bgi ")
		  .append(" inner join fetch bgi.resource bgResource")
		  .append(" inner join fetch bgi.baseGroup as baseGroup")
		  .append(" inner join fetch baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey")
		  .append(" and membership.role in ('").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
		  .append(" and exists (select share from qshareitem share where share.resource=bgResource)");

		TypedQuery<BusinessGroup> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("identityKey", identity.getKey());

		return query.getResultList();
	}
	
	public List<OLATResource> getSharedResources(QuestionItem item) {
		StringBuilder sb = new StringBuilder();
		sb.append("select resource from qshareitem share")
		  .append(" inner join share.resource resource")
		  .append(" where share.item.key=:itemKey");

		TypedQuery<OLATResource> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OLATResource.class)
				.setParameter("itemKey", item.getKey());
		return query.getResultList();
	}
	
	public List<QuestionItem2Resource> getSharedResourceInfos(QuestionItem item) {
		StringBuilder sb = new StringBuilder();
		sb.append("select share from qshare2itemshort share")
		  .append(" where share.itemKey=:itemKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItem2Resource.class)
				.setParameter("itemKey", item.getKey())
				.getResultList();
	}
	
	public int removeFromShares(List<? extends QuestionItemShort> items) {
		List<Long> keys = new ArrayList<>();
		for(QuestionItemShort item:items) {
			keys.add(item.getKey());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("delete from qshareitem share where share.item.key in (:itemKeys)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("itemKeys", keys)
				.executeUpdate();
	}
	
	public int removeFromShare(List<QuestionItemShort> items, OLATResource resource) {
		List<Long> keys = new ArrayList<>();
		for(QuestionItemShort item:items) {
			keys.add(item.getKey());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("delete from qshareitem share where share.item.key in (:itemKeys) and share.resource.key=:resourceKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("itemKeys", keys)
				.setParameter("resourceKey", resource.getKey())
				.executeUpdate();
	}
}