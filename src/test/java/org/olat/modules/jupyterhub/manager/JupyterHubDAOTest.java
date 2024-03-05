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
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.olat.core.id.Identity;
import org.olat.ims.lti13.LTI13Tool;
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
public class JupyterHubDAOTest extends OlatTestCase {

	@Autowired
	private JupyterHubDAO jupyterHubDAO;
	@Autowired
	private JupyterManager jupyterManager;

	@Test
	public void testCreateJupyterHub() {
		String clientId = UUID.randomUUID().toString();
		LTI13Tool ltiTool = createTestLtiTool(clientId);
		JupyterHub jupyterHub = jupyterHubDAO.createJupyterHub("JupyterHubTest", "1G", "2G",
				BigDecimal.valueOf(1), BigDecimal.valueOf(2), "", ltiTool,
				JupyterHub.AgreementSetting.suppressAgreement);

		Assert.assertNotNull(jupyterHub);
		Assert.assertNotNull(jupyterHub.getKey());
		Assert.assertEquals("JupyterHubTest", jupyterHub.getName());
		Assert.assertEquals("2G", jupyterHub.getRamLimit());
		Assert.assertEquals(BigDecimal.valueOf(2).setScale(2, RoundingMode.HALF_UP), jupyterHub.getCpuLimit());
		Assert.assertEquals(JupyterHub.AgreementSetting.suppressAgreement, jupyterHub.getAgreementSetting());

		Assert.assertNotNull(jupyterHub.getLtiTool());
		Assert.assertTrue(jupyterHub.getLtiTool().getInitiateLoginUrl().startsWith("https://jupyterhub."));
		Assert.assertEquals(clientId, jupyterHub.getLtiTool().getClientId());
	}

	private LTI13Tool createTestLtiTool(String clientId) {
		String jupyterHubUrl = "https://jupyterhub.openolat.org";
		return ((JupyterManagerImpl) jupyterManager).createLtiTool("LtiToolForJupyter", jupyterHubUrl, clientId);
	}

	@Test
	public void testGetJupyterHub() {
		String clientId = UUID.randomUUID().toString();
		JupyterHub jupyterHub = createTestJupyterHub(clientId, "Test1", "0.5 G", "1G",
				BigDecimal.valueOf(1), BigDecimal.valueOf(1), JupyterHub.AgreementSetting.suppressAgreement);

		JupyterHub jupyterHubForClientId = jupyterHubDAO.getJupyterHub(clientId);
		JupyterHub jupyterHubForRandomClientId = jupyterHubDAO.getJupyterHub(UUID.randomUUID().toString());
		JupyterHub jupyterHubForKey = jupyterHubDAO.getJupyterHub(jupyterHub.getKey());

		Assert.assertNotNull(jupyterHubForClientId);
		Assert.assertNull(jupyterHubForRandomClientId);
		Assert.assertNotNull(jupyterHubForKey);

		Assert.assertEquals("Test1", jupyterHubForClientId.getName());
		Assert.assertEquals(jupyterHubForClientId, jupyterHubForKey);
		Assert.assertEquals(clientId, jupyterHubForKey.getLtiTool().getClientId());
	}

	private JupyterHub createTestJupyterHub(String clientId, String name, String ramGuarantee, String ramLimit,
											BigDecimal cpuGuarantee, BigDecimal cpuLimit,
											JupyterHub.AgreementSetting agreementSetting) {
		LTI13Tool ltiTool = createTestLtiTool(clientId);
		return jupyterHubDAO.createJupyterHub(name, ramGuarantee, ramLimit, cpuGuarantee, cpuLimit, "", ltiTool, agreementSetting);
	}

	@Test
	public void testGetJupyterHubs() {
		String clientId1 = UUID.randomUUID().toString();
		String clientId2 = UUID.randomUUID().toString();
		JupyterHub jupyterHub1 = createTestJupyterHub(clientId1, "Test1", "0.5G", "1G", BigDecimal.valueOf(1), BigDecimal.valueOf(1), JupyterHub.AgreementSetting.suppressAgreement);
		JupyterHub jupyterHub2 = createTestJupyterHub(clientId2, "Test2", "1 G", "2G", BigDecimal.valueOf(1), BigDecimal.valueOf(2), JupyterHub.AgreementSetting.requireAgreement);

		List<JupyterHub> jupyterHubs = jupyterHubDAO.getJupyterHubs();

		Assert.assertTrue(jupyterHubs.size() >= 2);
		Assert.assertTrue(jupyterHubs.containsAll(List.of(jupyterHub1, jupyterHub2)));
		Assert.assertTrue(jupyterHubs.stream().anyMatch(h -> h.getLtiTool().getClientId().equals(clientId1) && h.getName().equals("Test1")));
		Assert.assertEquals(1, jupyterHubs.stream().filter(h -> h.getLtiTool().getClientId().equals(clientId2) && h.getAgreementSetting().equals(JupyterHub.AgreementSetting.requireAgreement)).toList().size());
	}

	@Test
	public void testGetApplications() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("jupyter-author-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author);
		String clientId = UUID.randomUUID().toString();
		JupyterHub jupyterHub = createTestJupyterHub(clientId, "Used JupyterHub", "4G", "16G", BigDecimal.valueOf(4), BigDecimal.valueOf(16), JupyterHub.AgreementSetting.configurableByAuthor);
		String subIdent1 = "1234";
		String subIdent2 = "2345";
		String image1 = "images/image1";
		String image2 = "images/image2";
		jupyterManager.initializeJupyterHubDeployment(courseEntry, subIdent1, clientId, image1, true);
		jupyterManager.initializeJupyterHubDeployment(courseEntry, subIdent2, clientId, image2, false);

		List<JupyterHubDAO.JupyterHubApplication> applications = jupyterManager.getJupyterHubApplications(jupyterHub.getKey());

		Assert.assertNotNull(applications);
		Assert.assertEquals(2, applications.size());
		Assert.assertEquals(2, applications.stream().filter(a -> a.getName().equals("Used JupyterHub")).toList().size());
		List<JupyterHubDAO.JupyterHubApplication> jupyterNode1Applications = applications.stream().filter(a -> a.getDescription().equals(courseEntry.getKey() + "-" + subIdent1)).toList();
		Assert.assertEquals(1, jupyterNode1Applications.size());
		Assert.assertEquals(subIdent1, jupyterNode1Applications.get(0).getLti13Context().getSubIdent());

		List<JupyterHubDAO.JupyterHubWithApplicationCount> hubsWithApplicationCounts = jupyterManager.getJupyterHubsWithApplicationCounts();

		Assert.assertFalse(hubsWithApplicationCounts.isEmpty());
		List<JupyterHubDAO.JupyterHubWithApplicationCount> matchingHubs = hubsWithApplicationCounts.stream().filter(h -> h.getJupyterHub().getLtiTool().getClientId().equals(clientId)).toList();
		Assert.assertEquals(1, matchingHubs.size());
		Assert.assertEquals("Used JupyterHub", matchingHubs.get(0).getJupyterHub().getName());
		Assert.assertEquals(Long.valueOf(2), matchingHubs.get(0).getApplicationCount());
	}
}