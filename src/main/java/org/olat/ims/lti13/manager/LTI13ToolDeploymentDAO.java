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
import java.util.UUID;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolDeploymentType;
import org.olat.ims.lti13.model.LTI13ToolDeploymentImpl;
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
	
	public LTI13ToolDeployment createDeployment(String targetUrl, LTI13ToolDeploymentType type, String deploymentId, LTI13Tool tool) {
		LTI13ToolDeploymentImpl deployment = new LTI13ToolDeploymentImpl();
		deployment.setCreationDate(new Date());
		deployment.setLastModified(deployment.getCreationDate());
		deployment.setDeploymentType(type);
		if(StringHelper.containsNonWhitespace(deploymentId)) {
			deployment.setDeploymentId(deploymentId);
		} else {
			deployment.setDeploymentId(idGenerator.newId());
		}
		deployment.setDeploymentResourceId(UUID.randomUUID().toString());
		deployment.setTargetUrl(targetUrl);
		deployment.setTool(tool);
		dbInstance.getCurrentEntityManager().persist(deployment);
		return deployment;
	}
	
	public LTI13ToolDeployment makeMultiContextsToolDeployment(LTI13ToolDeployment deployment) {
		if(deployment instanceof LTI13ToolDeploymentImpl ltiDeployment) {
			ltiDeployment.setDeploymentType(LTI13ToolDeploymentType.MULTIPLE_CONTEXTS);
			ltiDeployment.setEntry(null);
			ltiDeployment.setSubIdent(null);
			ltiDeployment.setBusinessGroup(null);
		}
		return updateToolDeployment(deployment);
	}
	
	public LTI13ToolDeployment updateToolDeployment(LTI13ToolDeployment deployment) {
		((LTI13ToolDeploymentImpl)deployment).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(deployment);
	}
	
	public void deleteToolDeployment(LTI13ToolDeployment deployment) {
		dbInstance.getCurrentEntityManager().remove(deployment);
	}
	
	public LTI13ToolDeployment loadDeploymentBy(RepositoryEntryRef entry, String subIdent) {
		String query = """
				select deployment from ltitooldeployment as deployment
				inner join fetch deployment.tool tool
				where deployment.entry.key=:entryKey and deployment.subIdent=:subIdent
				order by deployment.creationDate desc""";
		
		List<LTI13ToolDeployment> deployments = dbInstance.getCurrentEntityManager()
			.createQuery(query, LTI13ToolDeployment.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("subIdent", subIdent)
			.getResultList();
		return deployments != null && !deployments.isEmpty() ? deployments.get(0) : null;
	}
	
	/**
	 * Load the deployment by key and fetch the repository entry
	 * and the tool.
	 * 
	 * @param key The primary key
	 * @return The deployment
	 */
	public LTI13ToolDeployment loadDeploymentByKey(Long key) {
		String query = """
				select deployment from ltitooldeployment as deployment
				inner join fetch deployment.tool tool
				left join fetch deployment.entry as v
				left join fetch v.olatResource as vOres
				left join fetch v.statistics as vStatistics
				left join fetch v.lifecycle as vLifecycle
				left join fetch deployment.businessGroup as businessGroup
				left join fetch businessGroup.resource as bOres
				where deployment.key=:deploymentKey""";
		
		List<LTI13ToolDeployment> deployments = dbInstance.getCurrentEntityManager()
			.createQuery(query, LTI13ToolDeployment.class)
			.setParameter("deploymentKey", key)
			.getResultList();
		return deployments != null && !deployments.isEmpty() ? deployments.get(0) : null;
	}
	
	public LTI13ToolDeployment loadDeploymentByDeploymentId(String deploymentId) {
		String query = """
				select deployment from ltitooldeployment as deployment
				inner join fetch deployment.tool tool
				left join fetch deployment.entry as v
				left join fetch v.olatResource as vOres
				left join fetch v.statistics as vStatistics
				left join fetch v.lifecycle as vLifecycle
				left join fetch deployment.businessGroup as businessGroup
				left join fetch businessGroup.resource as bOres
				where deployment.deploymentId=:deploymentId""";
		
		List<LTI13ToolDeployment> deployments = dbInstance.getCurrentEntityManager()
			.createQuery(query, LTI13ToolDeployment.class)
			.setParameter("deploymentId", deploymentId)
			.getResultList();
		return deployments != null && !deployments.isEmpty() ? deployments.get(0) : null;
	}
	
	public List<LTI13ToolDeployment> loadDeployments(LTI13Tool tool) {
		String query = """
				select deployment from ltitooldeployment as deployment
				inner join fetch deployment.tool tool
				where tool.key=:toolKey""";
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(query, LTI13ToolDeployment.class)
			.setParameter("toolKey", tool.getKey())
			.getResultList();
	}
}
