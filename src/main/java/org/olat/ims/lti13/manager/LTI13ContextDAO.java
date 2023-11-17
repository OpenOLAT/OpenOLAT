/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.lti13.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.group.BusinessGroup;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.model.LTI13ContextImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class LTI13ContextDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13IDGenerator idGenerator;
	
	public LTI13Context createContext(String targetUrl, LTI13ToolDeployment deployment,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		LTI13ContextImpl context = new LTI13ContextImpl();
		context.setCreationDate(new Date());
		context.setLastModified(context.getCreationDate());
		context.setContextId(idGenerator.newId());
		context.setResourceId(idGenerator.newId());
		context.setTargetUrl(targetUrl);
		context.setDeployment(deployment);
		context.setEntry(entry);
		context.setSubIdent(subIdent);
		context.setBusinessGroup(businessGroup);
		dbInstance.getCurrentEntityManager().persist(context);
		return context;
	}
	
	public LTI13Context updateContext(LTI13Context context) {
		((LTI13ContextImpl)context).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(context);
	}
	
	public LTI13Context loadContextByKey(Long contextKey) {
		String query = """
				select context from lticontext as context
				inner join fetch context.deployment as deployment
				inner join fetch deployment.tool tool
				left join fetch deployment.entry as v
				left join fetch v.olatResource as vOres
				left join fetch v.statistics as vStatistics
				left join fetch v.lifecycle as vLifecycle
				left join fetch deployment.businessGroup as businessGroup
				left join fetch businessGroup.resource as bOres
				where context.key=:contextKey""";
		
		List<LTI13Context> contexts = dbInstance.getCurrentEntityManager().createQuery(query, LTI13Context.class)
				.setParameter("contextKey", contextKey)
				.getResultList();
		return contexts == null || contexts.isEmpty() ? null : contexts.get(0);
	}
	
	public List<LTI13Context> loadContextsByDeploymentKey(Long deploymentKey) {
		String query = """
				select context from lticontext as context
				inner join fetch context.deployment as deployment
				inner join fetch deployment.tool tool
				left join fetch deployment.entry as v
				left join fetch v.olatResource as vOres
				left join fetch v.statistics as vStatistics
				left join fetch v.lifecycle as vLifecycle
				left join fetch deployment.businessGroup as businessGroup
				left join fetch businessGroup.resource as bOres
				where context.deployment.key=:deploymentKey""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, LTI13Context.class)
				.setParameter("deploymentKey", deploymentKey)
				.getResultList();
	}
	
	public LTI13Context loadContextByContextId(String contextId) {
		String query = """
				select context from lticontext as context
				inner join fetch context.deployment as deployment
				inner join fetch deployment.tool tool
				left join fetch context.entry as v
				left join fetch v.olatResource as vOres
				left join fetch v.statistics as vStatistics
				left join fetch v.lifecycle as vLifecycle
				left join fetch deployment.businessGroup as businessGroup
				left join fetch businessGroup.resource as bOres
				where context.contextId=:contextId""";
		
		List<LTI13Context> contexts = dbInstance.getCurrentEntityManager()
			.createQuery(query, LTI13Context.class)
			.setParameter("contextId", contextId)
			.getResultList();
		return contexts != null && !contexts.isEmpty() ? contexts.get(0) : null;
	}
	
	public List<LTI13Context> loadContextsBy(RepositoryEntryRef entry, String subIdent) {
		String query = """
				select context from lticontext as context
				inner join fetch context.deployment as deployment
				inner join fetch deployment.tool tool
				where context.entry.key=:entryKey and context.subIdent=:subIdent""";
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(query, LTI13Context.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("subIdent", subIdent)
			.getResultList();
	}
	
	public List<LTI13Context> loadContexts(LTI13Tool tool) {
		String query = """
				select context from lticontext as context
				inner join fetch context.deployment as deployment
				inner join fetch deployment.tool tool
				where tool.key=:toolKey""";
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(query, LTI13Context.class)
			.setParameter("toolKey", tool.getKey())
			.getResultList();
	}
	
	public void deleteContext(LTI13Context context) {
		dbInstance.getCurrentEntityManager().remove(context);
	}
}
