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
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.olat.core.id.Identity;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.modules.jupyterhub.JupyterDeployment;
import org.olat.modules.jupyterhub.JupyterHub;
import org.olat.modules.jupyterhub.JupyterManager;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-04-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class JupyterDeploymentDAOTest extends OlatTestCase {

	@Autowired
	private JupyterDeploymentDAO jupyterDeploymentDAO;
	@Autowired
	private JupyterManager jupyterManager;

	@Test
	public void testCreateJupyterDeployment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("jupyter-test-author-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);

		String clientId = UUID.randomUUID().toString();
		String subIdent = "12345678";
		String image = "jupyter/minimal-notebook";
		JupyterHub jupyterHub = jupyterManager.createJupyterHub("Test hub",
				"https://jupyterhub.openolat.org", clientId, "3 G", BigDecimal.valueOf(3),
				JupyterHub.AgreementSetting.configurableByAuthor);
		LTI13ToolDeployment toolDeployment = jupyterManager.createLtiToolDeployment(jupyterHub.getLtiTool(), entry,
				subIdent, jupyterHub, image);
		JupyterDeployment jupyterDeployment = jupyterDeploymentDAO.createJupyterHubDeployment(jupyterHub, toolDeployment,
				"Test deployment", image, true);

		Assert.assertNotNull(jupyterDeployment);
		Assert.assertEquals(entry, jupyterDeployment.getLtiToolDeployment().getEntry());
		Assert.assertEquals(subIdent, jupyterDeployment.getLtiToolDeployment().getSubIdent());
		Assert.assertEquals(clientId, jupyterDeployment.getJupyterHub().getLtiTool().getClientId());
		Assert.assertEquals(jupyterDeployment.getLtiToolDeployment().getTool(), jupyterDeployment.getJupyterHub().getLtiTool());
		Assert.assertEquals(JupyterHub.AgreementSetting.configurableByAuthor, jupyterDeployment.getJupyterHub().getAgreementSetting());
		Assert.assertTrue(jupyterDeployment.getSuppressDataTransmissionAgreement());
		String customAttributes = jupyterDeployment.getLtiToolDeployment().getSendCustomAttributes();
		Assert.assertNotNull(customAttributes);
		String[] params = customAttributes.split("[\n;]");
		Assert.assertTrue(params.length >= 10);
		String memoryLimitKey = "memory_limit=";
		Optional<String> optionalMemoryLimit = Arrays.stream(params)
				.filter(p -> p.contains(memoryLimitKey))
				.map(p -> p.replace(memoryLimitKey, ""))
				.findFirst();
		Assert.assertTrue(optionalMemoryLimit.isPresent());
		Assert.assertTrue(JupyterHub.validateRam(optionalMemoryLimit.get()));
		Assert.assertEquals(JupyterHub.standardizeRam(jupyterHub.getRam()), optionalMemoryLimit.get());
		Assert.assertEquals(image, jupyterDeployment.getImage());
	}
}
