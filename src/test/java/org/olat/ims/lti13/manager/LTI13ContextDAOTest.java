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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolDeploymentType;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LTI13ContextDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13ToolDAO lti13ToolDao;
	@Autowired
	private LTI13ContextDAO lti13ContextDao;
	@Autowired
	private LTI13ToolDeploymentDAO lti13ToolDeploymentDao;
	
	@Test
	public void createContext() {
		String toolName = "LTI 1.3";
		String toolUrl = "https://www.openolat.assessment/context";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, null, LTI13ToolType.EXTERNAL);
		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.SINGLE_CONTEXT, null, tool);
		LTI13Context context = lti13ContextDao.createContext(null, deployment, null, initiateLoginUrl, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(context);
		Assert.assertNotNull(context.getKey());
		Assert.assertNotNull(context.getContextId());
		Assert.assertNotNull(context.getCreationDate());
		Assert.assertNotNull(context.getLastModified());
	}
	
	@Test
	public void loadContextsByDeploymentKey() {
		String toolName = "LTI 1.3 context - 1";
		String toolUrl = "https://www.openolat.assessment/context";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, null, LTI13ToolType.EXTERNAL);
		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.SINGLE_CONTEXT, null, tool);
		LTI13Context context = lti13ContextDao.createContext(null, deployment, null, initiateLoginUrl, null);
		dbInstance.commitAndCloseSession();
		
		List<LTI13Context> contexts = lti13ContextDao.loadContextsByDeploymentKey(deployment.getKey());
		assertThat(contexts)
			.isNotNull()
			.containsExactly(context);
	}
	
	@Test
	public void loadContextByContextId() {
		String toolName = "LTI 1.3 context - 2";
		String toolUrl = "https://www.openolat.com/tool";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login_init";
		String redirectUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, redirectUrl, LTI13ToolType.EXTERNAL);
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-3");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.SINGLE_CONTEXT, null, tool);
		LTI13Context context = lti13ContextDao.createContext(null, deployment, entry, "2836480", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(context.getContextId());
		
		LTI13Context reloadedContext = lti13ContextDao.loadContextByContextId(context.getContextId());
		
		Assert.assertNotNull(reloadedContext);
		Assert.assertNotNull(reloadedContext.getKey());
		Assert.assertEquals(deployment, reloadedContext.getDeployment());
		Assert.assertEquals(entry, reloadedContext.getEntry());
		Assert.assertEquals("2836480", reloadedContext.getSubIdent());
		Assert.assertEquals(context.getContextId(), reloadedContext.getContextId());
	}
	
	@Test
	public void loadContextsByRepositoryEntryAndSubIdent() {
		String toolName = "LTI 1.3 context - 3";
		String toolUrl = "https://www.openolat.com/tool";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login_init";
		String redirectUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, redirectUrl, LTI13ToolType.EXTERNAL);
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lti-13-author-4");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.SINGLE_CONTEXT, null, tool);
		LTI13Context ltiContext = lti13ContextDao.createContext(null, deployment, entry, "2836481", null);
		dbInstance.commitAndCloseSession();
		
		List<LTI13Context> reloadedContexts = lti13ContextDao.loadContextsBy(entry, "2836481");
		assertThat(reloadedContexts)
			.isNotNull()
			.containsExactly(ltiContext);
	}
	
	@Test
	public void loadContextsByTool() {
		String toolName = "LTI 1.3 context - 4";
		String toolUrl = "https://www.openolat.com/tool";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login_init";
		String redirectUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, redirectUrl, LTI13ToolType.EXTERNAL);
		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.SINGLE_CONTEXT, null, tool);
		LTI13Context ltiContext = lti13ContextDao.createContext(null, deployment, null, null, null);
		dbInstance.commitAndCloseSession();
		
		List<LTI13Context> reloadedContexts = lti13ContextDao.loadContexts(tool);
		assertThat(reloadedContexts)
			.isNotNull()
			.containsExactly(ltiContext);
	}
}
