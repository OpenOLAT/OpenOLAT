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
package org.olat.modules.jupyterhub;

import java.math.BigDecimal;
import java.util.List;

import org.olat.core.gui.components.util.SelectionValues;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.modules.jupyterhub.manager.JupyterHubDAO;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface JupyterManager {
	JupyterHub getJupyterHub(String selectedKey);

	List<JupyterHub> getJupyterHubs();

	SelectionValues getJupyterHubsKV();

	List<JupyterHubDAO.JupyterHubWithApplicationCount> getJupyterHubsWithApplicationCounts();

	List<JupyterHubDAO.JupyterHubApplication> getJupyterHubApplications(Long key);

	JupyterHub createJupyterHub(String name, String jupyterHubUrl, String clientId, String ramGuarantee, String ramLimit,
								BigDecimal cpuGuarantee, BigDecimal cpuLimit, String additionalFields,
								JupyterHub.AgreementSetting agreementSetting);

	boolean isInUse(JupyterHub jupyterHub);

	JupyterHub updateJupyterHub(JupyterHub jupyterHub);

	void deleteJupyterHub(JupyterHub jupyterHub);

	void copyProfile(JupyterHub jupyterHub, String copySuffix);

	JupyterDeployment getJupyterDeployment(RepositoryEntry repositoryEntry, String subIdent);

	void initializeJupyterHubDeployment(RepositoryEntry repositoryEntry, String subIdent, String clientId, String image,
										Boolean suppressDataTransmissionAgreement);

	LTI13Context createLtiContext(LTI13Tool ltiTool, RepositoryEntry repositoryEntry,
												String subIdent, JupyterHub jupyterHub, String image);

	void recreateJupyterHubDeployment(JupyterDeployment jupyterDeployment, RepositoryEntry courseEntry, String subIdent,
									  JupyterHub jupyterHub);

	void deleteJupyterHub(RepositoryEntry repositoryEntry, String subIdent);

	void updateJupyterDeployment(JupyterDeployment jupyterDeployment);

	CheckConnectionResponse checkConnection(String ltiKey, String clientId, String ltiMessageHint);

	record CheckConnectionResponse(boolean success, String message) {}
}
