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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.modules.jupyterhub.JupyterDeployment;
import org.olat.modules.jupyterhub.JupyterHub;
import org.olat.modules.jupyterhub.model.JupyterDeploymentImpl;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class JupyterDeploymentDAO {

	@Autowired
	private DB dbInstance;

	public JupyterDeployment getJupyterDeployment(RepositoryEntry repositoryEntry, String subIdent) {
		String query = "select d from jupyterdeployment d inner join d.ltiToolDeployment as td where td.entry.id=:entryId and td.subIdent=:subIdent";
		List<JupyterDeployment> jupyterDeployments = dbInstance.getCurrentEntityManager()
				.createQuery(query, JupyterDeployment.class)
				.setParameter("entryId", repositoryEntry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
		return jupyterDeployments == null || jupyterDeployments.isEmpty() ? null : jupyterDeployments.get(0);
	}

	public boolean exists(RepositoryEntry repositoryEntry, String subIdent) {
		String query = "select 1 from jupyterdeployment d inner join d.ltiToolDeployment as td where td.entry.id=:entryId and td.subIdent=:subIdent";
		return !dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("entryId", repositoryEntry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList()
				.isEmpty();
	}

	public JupyterDeployment createJupyterHubDeployment(JupyterHub jupyterHub, LTI13ToolDeployment toolDeployment,
														String description, String image,
														Boolean suppressDataTransmissionAgreement) {
		JupyterDeploymentImpl jupyterDeployment = new JupyterDeploymentImpl();
		jupyterDeployment.setCreationDate(new Date());
		jupyterDeployment.setLastModified(jupyterDeployment.getCreationDate());
		jupyterDeployment.setDescription(description);
		jupyterDeployment.setImage(image);
		jupyterDeployment.setJupyterHub(jupyterHub);
		jupyterDeployment.setLtiToolDeployment(toolDeployment);
		if (suppressDataTransmissionAgreement != null) {
			jupyterDeployment.setSuppressDataTransmissionAgreement(suppressDataTransmissionAgreement);
		}
		dbInstance.getCurrentEntityManager().persist(jupyterDeployment);
		return jupyterDeployment;
	}

	public void deleteJupyterDeployment(JupyterDeployment jupyterDeployment) {
		dbInstance.deleteObject(jupyterDeployment);
	}

	public void updateJupyterDeployment(JupyterDeployment jupyterDeployment) {
		if (jupyterDeployment instanceof JupyterDeploymentImpl jupyterDeploymentImpl) {
			jupyterDeploymentImpl.setLastModified(new Date());
		}
		dbInstance.getCurrentEntityManager().merge(jupyterDeployment);
	}
}
