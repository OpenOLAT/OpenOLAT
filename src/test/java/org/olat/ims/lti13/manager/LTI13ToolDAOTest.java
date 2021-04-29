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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13ToolDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13ToolDAO lti13ToolDao;
	
	@Test
	public void createTool() {
		String toolName = "LTI 1.3 spring demo";
		String toolUrl = "https://www.openolat.com/tool";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login_init";
		String redirectUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, redirectUrl, LTI13ToolType.EXTERNAL);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(tool);
		Assert.assertNotNull(tool.getKey());
		Assert.assertNotNull(tool.getCreationDate());
		Assert.assertNotNull(tool.getLastModified());
		
		Assert.assertEquals(toolName, tool.getToolName());
		Assert.assertEquals(toolUrl, tool.getToolUrl());
		Assert.assertEquals(clientId, tool.getClientId());
		Assert.assertEquals(initiateLoginUrl, tool.getInitiateLoginUrl());
		Assert.assertEquals(redirectUrl, tool.getRedirectUrl());
		Assert.assertEquals("www.openolat.com", tool.getToolDomain());
	}
	
	@Test
	public void getTools() {
		String toolName = "LTI 1.3 demo";
		String toolUrl = "https://www.openolat.com/lti";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, null, LTI13ToolType.EXTERNAL);
		dbInstance.commitAndCloseSession();
		
		List<LTI13Tool> tools = lti13ToolDao.getTools(LTI13ToolType.EXTERNAL);
		assertThat(tools)
			.isNotNull()
			.contains(tool);
	}

}
