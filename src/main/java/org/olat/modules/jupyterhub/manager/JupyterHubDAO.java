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
package org.olat.modules.jupyterhub.manager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.modules.jupyterhub.JupyterHub;
import org.olat.modules.jupyterhub.model.JupyterHubImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class JupyterHubDAO {

	@Autowired
	private DB dbInstance;

	public JupyterHub createJupyterHub(String name, String ram, BigDecimal cpu, LTI13Tool ltiTool,
									   JupyterHub.AgreementSetting agreementSetting) {
		JupyterHubImpl jupyterHub = new JupyterHubImpl();
		jupyterHub.setCreationDate(new Date());
		jupyterHub.setLastModified(jupyterHub.getCreationDate());
		jupyterHub.setName(name);
		jupyterHub.setStatus(JupyterHub.JupyterHubStatus.active);
		jupyterHub.setRam(ram);
		jupyterHub.setCpu(cpu);
		jupyterHub.setLtiTool(ltiTool);
		jupyterHub.setAgreementSetting(agreementSetting);
		dbInstance.getCurrentEntityManager().persist(jupyterHub);
		return jupyterHub;
	}

	public JupyterHub getJupyterHub(Long key) {
		return dbInstance.getCurrentEntityManager().find(JupyterHubImpl.class, key);
	}

	public List<JupyterHub> getJupyterHubs() {
		String query = "select hub from jupyterhub hub";
		return dbInstance.getCurrentEntityManager().createQuery(query, JupyterHub.class).getResultList();
	}

	public List<JupyterHubWithApplicationCount> getJupyterHubsWithApplicationCounts() {
		String queryString = "select hub," +
				" (select count(deployment.key) from jupyterdeployment as deployment" +
				" where deployment.jupyterHub.key = hub.key" +
				" ) as numberOfApplications " +
				"from jupyterhub as hub";
		return dbInstance
				.getCurrentEntityManager()
				.createQuery(queryString, Object[].class)
				.getResultList()
				.stream()
				.map(JupyterHubWithApplicationCount::new).collect(Collectors.toList());
	}

	public List<JupyterHubApplication> getApplications(Long jupyterHubKey) {
		String queryString = "select hub.name, jd.description, jd.ltiToolDeployment from jupyterhub as hub inner join jupyterdeployment as jd on (jd.jupyterHub.key=hub.key) where hub.key=:jupyterHubKey order by jd.description asc";
		return dbInstance.getCurrentEntityManager()
				.createQuery(queryString, Object[].class)
				.setParameter("jupyterHubKey", jupyterHubKey)
				.getResultList()
				.stream()
				.map(JupyterHubApplication::new)
				.toList();
	}
	public void deleteJupyterHub(JupyterHub jupyterHub) {
		dbInstance.deleteObject(jupyterHub);
	}

	public boolean isInUse(JupyterHub jupyterHub) {
		String query = "select 1 from jupyterdeployment d where d.jupyterHub=:jupyterHub";
		return !dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("jupyterHub", jupyterHub)
				.getResultList()
				.isEmpty();
	}

	public JupyterHub updateJupyterHub(JupyterHub jupyterHub) {
		jupyterHub.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(jupyterHub);
	}

	public JupyterHub getJupyterHub(String clientId) {
		String query = "select h from jupyterhub h inner join h.ltiTool as t where t.clientId=:clientId";
		List<JupyterHub> jupyterHubs = dbInstance.getCurrentEntityManager()
				.createQuery(query, JupyterHub.class)
				.setParameter("clientId", clientId)
				.getResultList();
		return jupyterHubs == null || jupyterHubs.isEmpty() ? null : jupyterHubs.get(0);
	}

	public static class JupyterHubWithApplicationCount {
		private final JupyterHub jupyterHub;
		private final Long applicationCount;

		public JupyterHubWithApplicationCount(Object[] objectArray) {
			this.jupyterHub = (JupyterHub) objectArray[0];
			this.applicationCount = PersistenceHelper.extractPrimitiveLong(objectArray, 1);
		}

		public JupyterHub getJupyterHub() {
			return jupyterHub;
		}

		public Long getApplicationCount() {
			return applicationCount;
		}
	}

	public static class JupyterHubApplication {
		private final String name;
		private final String description;
		private final LTI13ToolDeployment lti13ToolDeployment;

		public JupyterHubApplication(Object[] objectArray) {
			this.name = PersistenceHelper.extractString(objectArray, 0);
			this.description = PersistenceHelper.extractString(objectArray, 1);
			this.lti13ToolDeployment = (LTI13ToolDeployment) objectArray[2];
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public LTI13ToolDeployment getLti13ToolDeployment() {
			return lti13ToolDeployment;
		}
	}
}
