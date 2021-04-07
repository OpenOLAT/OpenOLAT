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
package org.olat.ims.lti13.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.model.LTI13SharedToolDeploymentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13SharedToolDeploymentDAO {
	
	@Autowired
	private DB dbInstance;
	
	public LTI13SharedToolDeployment createDeployment(String deploymentId, LTI13Platform platform,
			RepositoryEntry entry, BusinessGroup businessGroup) {
		LTI13SharedToolDeploymentImpl deployment = new LTI13SharedToolDeploymentImpl();
		deployment.setCreationDate(new Date());
		deployment.setLastModified(deployment.getCreationDate());
		deployment.setDeploymentId(deploymentId);
		deployment.setPlatform(platform);
		deployment.setBusinessGroup(businessGroup);
		deployment.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(deployment);
		return deployment;
	}
	
	public LTI13SharedToolDeployment updateDeployment(LTI13SharedToolDeployment deployment) {
		((LTI13SharedToolDeploymentImpl)deployment).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(deployment);
	}
	
	public List<LTI13SharedToolDeployment> getSharedToolDeployment(String deploymentId, LTI13Platform platform) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltisharedtooldeployment deployment")
		  .append(" inner join deployment.platform platform")
		  .append(" where deployment.deploymentId=:deploymentId and platform.key=:platformKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13SharedToolDeployment.class)
			.setParameter("deploymentId", deploymentId)
			.setParameter("platformKey", platform.getKey())
			.getResultList();
	}
	
	public List<LTI13SharedToolDeployment> getSharedToolDeployment(RepositoryEntryRef entryRef) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltisharedtooldeployment deployment")
		  .append(" inner join fetch deployment.platform platform")
		  .append(" where deployment.entry.key=:entryKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13SharedToolDeployment.class)
			.setParameter("entryKey", entryRef.getKey())
			.getResultList();
	}
	
	public List<LTI13SharedToolDeployment> getSharedToolDeployment(BusinessGroupRef businessGroup) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltisharedtooldeployment deployment")
		  .append(" inner join fetch deployment.platform platform")
		  .append(" where deployment.businessGroup.key=:groupKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13SharedToolDeployment.class)
			.setParameter("groupKey", businessGroup.getKey())
			.getResultList();
	}
	
	public List<LTI13SharedToolDeployment> loadSharedToolDeployments(LTI13Platform platform) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltisharedtooldeployment deployment")
		  .append(" inner join fetch deployment.platform platform")
		  .append(" where platform.key=:platformKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13SharedToolDeployment.class)
			.setParameter("platformKey", platform.getKey())
			.getResultList();
	}
	
	public boolean hasDeployment(Long repositoryEntryKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment.key from ltisharedtooldeployment deployment")
		  .append(" inner join deployment.platform as platform")
		  .append(" where deployment.entry.key=:entryKey");
		
		List<Long> firstKey = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("entryKey", repositoryEntryKey)
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return firstKey != null && !firstKey.isEmpty() && firstKey.get(0) != null
				&& firstKey.get(0).intValue() > 0;
	}
	
	public void deleteSharedDeployment(LTI13SharedToolDeployment deployment) {
		dbInstance.getCurrentEntityManager().remove(deployment);
	}
}
