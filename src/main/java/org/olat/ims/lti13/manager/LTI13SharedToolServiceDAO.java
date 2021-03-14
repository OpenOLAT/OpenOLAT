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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.LTI13SharedToolService;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.ims.lti13.model.LTI13SharedToolServiceImpl;
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
public class LTI13SharedToolServiceDAO {
	
	@Autowired
	private DB dbInstance;
	
	public LTI13SharedToolService createServiceEndpoint(String contextId, LTI13SharedToolService.ServiceType type,
			String endpointUrl, LTI13SharedToolDeployment deployment) {
		LTI13SharedToolServiceImpl serviceEndpoint = new LTI13SharedToolServiceImpl();
		serviceEndpoint.setCreationDate(new Date());
		serviceEndpoint.setLastModified(serviceEndpoint.getCreationDate());
		serviceEndpoint.setContextId(contextId);
		serviceEndpoint.setType(type.name());
		serviceEndpoint.setEndpoint(endpointUrl);
		serviceEndpoint.setDeployment(deployment);
		dbInstance.getCurrentEntityManager().persist(serviceEndpoint);
		return serviceEndpoint;
	}
	
	public LTI13SharedToolService loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select toolservice from ltisharedtoolserivce as toolservice")
		  .append(" where toolservice.key=:serviceKey");
		
		List<LTI13SharedToolService> services = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LTI13SharedToolService.class)
				.setParameter("serviceKey", key)
				.getResultList();
		return services != null && !services.isEmpty() ? services.get(0) : null;
	}
	
	public List<LTI13SharedToolService> loadServiceEndpoint(String contextId, LTI13SharedToolService.ServiceType type,
			String endpointUrl, LTI13SharedToolDeployment deployment) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select toolservice from ltisharedtoolserivce as toolservice")
		  .append(" where toolservice.contextId=:contextId and toolservice.type=:serviceType")
		  .append(" and toolservice.endpointUrl=:endpointUrl and toolservice.deployment.key=:deploymentKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LTI13SharedToolService.class)
				.setParameter("contextId", contextId)
				.setParameter("serviceType", type.name())
				.setParameter("endpointUrl", endpointUrl)
				.setParameter("deploymentKey", deployment.getKey())
				.getResultList();
	}
	
	public List<LTI13SharedToolService> getSharedToolServices(RepositoryEntryRef entry, ServiceType type, List<String> issuers) {
		if(type == null || entry == null || entry.getKey() == null || issuers == null || issuers.isEmpty()) {
			return new ArrayList<>(2);
		}
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select toolservice from ltisharedtoolserivce as toolservice")
		  .append(" inner join fetch toolservice.deployment as deployment")
		  .append(" inner join fetch deployment.sharedTool as tool")
		  .append(" where tool.entry.key=:entryKey and tool.issuer in (:issuers) and toolservice.type=:serviceType");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13SharedToolService.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("serviceType", type.name())
			.setParameter("issuers", issuers)
			.getResultList();
	}
	
	protected void deleteSharedToolServices(LTI13SharedToolDeployment deployment) {
		String sb = "delete from ltisharedtoolserivce as toolservice where toolservice.deployment.key=:deploymentKey";
		dbInstance.getCurrentEntityManager()
			.createQuery(sb)
			.setParameter("deploymentKey", deployment.getKey())
			.executeUpdate();
	}
}
