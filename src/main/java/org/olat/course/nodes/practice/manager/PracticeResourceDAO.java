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
package org.olat.course.nodes.practice.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.model.PracticeResourceImpl;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PracticeResourceDAO {

	@Autowired
	private DB dbInstance;
	
	public PracticeResource createResource(RepositoryEntry courseEntry, String subIdent,
			RepositoryEntry testEntry, Pool pool, QuestionItemCollection collection, OLATResource sharedResource) {
		PracticeResourceImpl resource = new PracticeResourceImpl();
		resource.setCreationDate(new Date());
		resource.setLastModified(resource.getCreationDate());
		resource.setRepositoryEntry(courseEntry);
		resource.setSubIdent(subIdent);
		resource.setTestEntry(testEntry);
		resource.setPool(pool);
		resource.setItemCollection(collection);
		resource.setResourceShare(sharedResource);
		dbInstance.getCurrentEntityManager().persist(resource);
		return resource;
	}
	
	public List<PracticeResource> getResources(RepositoryEntry courseEntry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rsrc from practiceresource as rsrc")
		  .append(" inner join fetch rsrc.repositoryEntry as v")
		  .append(" left join fetch rsrc.testEntry as test")
		  .append(" left join fetch rsrc.pool as pool")
		  .append(" left join fetch rsrc.itemCollection as iCollection")
		  .append(" left join fetch rsrc.resourceShare as share")
		  .and().append(" rsrc.repositoryEntry.key=:repoEntryKey")
		  .and().append(" rsrc.subIdent=:subIdent");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PracticeResource.class)
				.setParameter("repoEntryKey", courseEntry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}
	
	public List<PracticeResource> getResources(Pool pool) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rsrc from practiceresource as rsrc")
		  .append(" inner join fetch rsrc.repositoryEntry as v")
		  .append(" inner join fetch rsrc.pool as pool")
		  .and().append(" pool.key=:poolKey");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PracticeResource.class)
				.setParameter("poolKey", pool.getKey())
				.getResultList();
	}
	
	public List<PracticeResource> getResources(QuestionItemCollection collection) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rsrc from practiceresource as rsrc")
		  .append(" inner join fetch rsrc.repositoryEntry as v")
		  .append(" inner join fetch rsrc.itemCollection as iCollection")
		  .and().append(" iCollection.key=:collectionKey");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PracticeResource.class)
				.setParameter("collectionKey", collection.getKey())
				.getResultList();
	}
	
	public List<PracticeResource> getResourcesOfTest(RepositoryEntry testEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rsrc from practiceresource as rsrc")
		  .append(" inner join fetch rsrc.repositoryEntry as v")
		  .append(" inner join fetch rsrc.testEntry as test")
		  .and().append(" test.key=:repoEntryKey");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PracticeResource.class)
				.setParameter("repoEntryKey", testEntry.getKey())
				.getResultList();
	}
	
	public List<PracticeResource> getResourcesOfSharedResource(OLATResource resource) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rsrc from practiceresource as rsrc")
		  .append(" inner join fetch rsrc.repositoryEntry as v")
		  .append(" inner join fetch rsrc.resourceShare as share")
		  .and().append(" share.key=:resourceKey");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), PracticeResource.class)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
	}
	
	public void deleteResource(PracticeResource resource) {
		PracticeResource reloadedResource = dbInstance.getCurrentEntityManager()
				.getReference(PracticeResourceImpl.class, resource.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedResource);
	}
}
