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

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolDeploymentType;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13ToolDeploymentDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13ToolDAO lti13ToolDao;
	@Autowired
	private LTI13ToolDeploymentDAO lti13ToolDeploymentDao;
	
	@Test
	public void createDeployment() {
		String toolName = "LTI 1.3 spring demo";
		String toolUrl = "https://www.openolat.com/tool";
		String clientId = UUID.randomUUID().toString();
		String deploymentId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login";
		
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, null, LTI13ToolType.EXTERNAL);
		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.MULTIPLE_CONTEXTS, deploymentId, tool);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(deployment);
		Assert.assertNotNull(deployment.getKey());
		Assert.assertEquals(deploymentId, deployment.getDeploymentId());
		Assert.assertEquals(tool, deployment.getTool());
		Assert.assertEquals(LTI13ToolDeploymentType.MULTIPLE_CONTEXTS, deployment.getDeploymentType());
		Assert.assertNull(deployment.getEntry());
		Assert.assertNull(deployment.getSubIdent());
		Assert.assertNull(deployment.getBusinessGroup());
	}
	
	@Test
	public void loadDeploymentByKey() {
		String toolName = "LTI 1.3 deployment - 2";
		String toolUrl = "https://www.openolat.com/tool";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login_init";
		String redirectUrl = "https://www.openolat.com/lti/api/login";
		
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, redirectUrl, LTI13ToolType.EXTERNAL);
		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.SINGLE_CONTEXT, null, tool);
		dbInstance.commitAndCloseSession();
		
		LTI13ToolDeployment reloadedDeployment = lti13ToolDeploymentDao.loadDeploymentByKey(deployment.getKey());
		
		Assert.assertNotNull(reloadedDeployment);
		Assert.assertNotNull(reloadedDeployment.getKey());
		Assert.assertNotNull(reloadedDeployment.getDeploymentId());
		Assert.assertEquals(tool, reloadedDeployment.getTool());
		Assert.assertEquals(LTI13ToolDeploymentType.SINGLE_CONTEXT, deployment.getDeploymentType());
		Assert.assertNull(reloadedDeployment.getEntry());
		Assert.assertNull(reloadedDeployment.getSubIdent());
		Assert.assertNull(reloadedDeployment.getBusinessGroup());
	}
	
	@Test
	public void loadDeploymentByDeployment() {
		String toolName = "LTI 1.3 deployment - 3";
		String toolUrl = "https://www.openolat.com/tool";
		String clientId = UUID.randomUUID().toString();
		String deploymentId = clientId + "-deployment";
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login_init";
		String redirectUrl = "https://www.openolat.com/lti/api/login";
		
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, redirectUrl, LTI13ToolType.EXTERNAL);
		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.SINGLE_CONTEXT, deploymentId, tool);
		dbInstance.commitAndCloseSession();
		
		LTI13ToolDeployment reloadedDeployment = lti13ToolDeploymentDao.loadDeploymentByDeploymentId(deploymentId);
		
		Assert.assertNotNull(reloadedDeployment);
		Assert.assertNotNull(reloadedDeployment.getKey());
		Assert.assertEquals(deployment, reloadedDeployment);
		Assert.assertEquals(deployment.getKey(), reloadedDeployment.getKey());
	}
	
	@Test
	public void loadDeploymentByTool() {
		String toolName = "LTI 1.3 deployment - 4";
		String toolUrl = "https://www.openolat.com/tool";
		String clientId = UUID.randomUUID().toString();
		String deploymentId = clientId + "-deployment";
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login_init";
		String redirectUrl = "https://www.openolat.com/lti/api/login";
		
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, redirectUrl, LTI13ToolType.EXTERNAL);
		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.SINGLE_CONTEXT, deploymentId, tool);
		dbInstance.commitAndCloseSession();
		
		List<LTI13ToolDeployment> reloadedDeployments = lti13ToolDeploymentDao.loadDeployments(tool);
		Assertions.assertThat(reloadedDeployments)
			.hasSize(1)
			.containsExactly(deployment);
	}
}
