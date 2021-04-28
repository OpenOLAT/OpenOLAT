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
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.model.LTI13ToolDeploymentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13ToolDeploymentDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13IDGenerator idGenerator;
	
	public LTI13ToolDeployment createDeployment(String targetUrl, LTI13Tool tool, RepositoryEntry entry, String subIdent) {
		LTI13ToolDeploymentImpl deployment = new LTI13ToolDeploymentImpl();
		deployment.setCreationDate(new Date());
		deployment.setLastModified(deployment.getCreationDate());
		deployment.setDeploymentId(idGenerator.newId());
		deployment.setTargetUrl(targetUrl);
		deployment.setTool(tool);
		deployment.setEntry(entry);
		deployment.setSubIdent(subIdent);
		dbInstance.getCurrentEntityManager().persist(deployment);
		return deployment;
	}
	
	public LTI13ToolDeployment updateToolDeployment(LTI13ToolDeployment deployment) {
		((LTI13ToolDeploymentImpl)deployment).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(deployment);
	}
	
	public void deleteToolDeployment(LTI13ToolDeployment deployment) {
		dbInstance.getCurrentEntityManager().remove(deployment);
	}
	
	public LTI13ToolDeployment loadDeploymentBy(RepositoryEntryRef entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltitooldeployment as deployment")
		  .append(" inner join fetch deployment.tool tool")
		  .append(" where deployment.entry.key=:entryKey and deployment.subIdent=:subIdent");
		
		List<LTI13ToolDeployment> deployments = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13ToolDeployment.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("subIdent", subIdent)
			.getResultList();
		return deployments != null && !deployments.isEmpty() ? deployments.get(0) : null;
	}
	
	public List<LTI13ToolDeployment> loadDeploymentsBy(RepositoryEntryRef entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltitooldeployment as deployment")
		  .append(" inner join fetch deployment.tool tool")
		  .append(" where deployment.entry.key=:entryKey and deployment.subIdent=:subIdent");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13ToolDeployment.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("subIdent", subIdent)
			.getResultList();
	}
	
	/**
	 * Load the deployment by key and fetch the repository entry
	 * and the tool.
	 * 
	 * @param key The primary key
	 * @return The deployment
	 */
	public LTI13ToolDeployment loadDeploymentByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltitooldeployment as deployment")
		  .append(" inner join fetch deployment.tool tool")
		  .append(" left join fetch deployment.entry as v")
		  .append(" left join fetch v.olatResource as vOres")
		  .append(" left join fetch v.statistics as vStatistics")
		  .append(" left join fetch v.lifecycle as vLifecycle")
		  .append(" where deployment.key=:deploymentKey");
		
		List<LTI13ToolDeployment> deployments = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13ToolDeployment.class)
			.setParameter("deploymentKey", key)
			.getResultList();
		return deployments != null && !deployments.isEmpty() ? deployments.get(0) : null;
	}
	
	public List<LTI13ToolDeployment> loadDeployments(LTI13Tool tool) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltitooldeployment as deployment")
		  .append(" inner join fetch deployment.tool tool")
		  .append(" where tool.key=:toolKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13ToolDeployment.class)
			.setParameter("toolKey", tool.getKey())
			.getResultList();
	}
}
