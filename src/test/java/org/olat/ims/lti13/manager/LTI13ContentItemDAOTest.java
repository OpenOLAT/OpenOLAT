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
import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13ContentItemTypesEnum;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13ContentItemDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13ToolDAO lti13ToolDao;
	@Autowired
	private LTI13ContentItemDAO lti13ContentItemDao;
	@Autowired
	private LTI13ToolDeploymentDAO lti13ToolDeploymentDao;
	
	@Test
	public void createContentItemByTool() {
		String toolName = "LTI DL 2.0";
		String toolUrl = "https://www.openolat.assessment/dl";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, null, LTI13ToolType.EXTERNAL);
		dbInstance.commitAndCloseSession();
		LTI13ContentItem minimalItem = lti13ContentItemDao.createItem(LTI13ContentItemTypesEnum.ltiResourceLink, tool, null);
		lti13ContentItemDao.persistItem(minimalItem);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(minimalItem);
		Assert.assertNotNull(minimalItem.getKey());
		Assert.assertNotNull(minimalItem.getCreationDate());
		Assert.assertNotNull(minimalItem.getLastModified());
		Assert.assertEquals(tool, minimalItem.getTool());
		Assert.assertEquals(LTI13ContentItemTypesEnum.ltiResourceLink, minimalItem.getType());
	}
	
	@Test
	public void createContentItemByDeployment() {
		String toolName = "LTI DL 2.0";
		String toolUrl = "https://www.openolat.assessment/dl";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, null, LTI13ToolType.EXTERNAL);

		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, tool, null, null, null);
		LTI13ContentItem minimalItem = lti13ContentItemDao.createItem(LTI13ContentItemTypesEnum.ltiResourceLink, tool, deployment);
		lti13ContentItemDao.persistItem(minimalItem);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(minimalItem);
		Assert.assertNotNull(minimalItem.getKey());
		Assert.assertNotNull(minimalItem.getCreationDate());
		Assert.assertNotNull(minimalItem.getLastModified());
		Assert.assertEquals(tool, minimalItem.getTool());
		Assert.assertEquals(LTI13ContentItemTypesEnum.ltiResourceLink, minimalItem.getType());
	}
	
	@Test
	public void loadContentItemByDeployment() {
		String toolName = "LTI DL 2.0";
		String toolUrl = "https://www.openolat.assessment/dl";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, null, LTI13ToolType.EXTERNAL);

		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, tool, null, null, null);
		LTI13ContentItem minimalItem = lti13ContentItemDao.createItem(LTI13ContentItemTypesEnum.link, tool, deployment);
		lti13ContentItemDao.persistItem(minimalItem);
		dbInstance.commitAndCloseSession();
		
		LTI13ContentItem reloadedItem = lti13ContentItemDao.loadItemByKey(minimalItem.getKey());
		
		Assert.assertNotNull(reloadedItem);
		Assert.assertNotNull(reloadedItem.getKey());
		Assert.assertNotNull(reloadedItem.getCreationDate());
		Assert.assertNotNull(reloadedItem.getLastModified());
		Assert.assertEquals(minimalItem, reloadedItem);
		Assert.assertEquals(tool, reloadedItem.getTool());
		Assert.assertEquals(deployment, reloadedItem.getDeployment());
		Assert.assertEquals(LTI13ContentItemTypesEnum.link, reloadedItem.getType());
	}
	
	@Test
	public void loadItemByTool() {
		String toolName = "LTI DL 2.0";
		String toolUrl = "https://www.openolat.assessment/dl";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://www.openolat.com/lti/api/login";
		LTI13Tool tool = lti13ToolDao.createTool(toolName, toolUrl, clientId, initiateLoginUrl, null, LTI13ToolType.EXTERNAL);

		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, tool, null, null, null);
		LTI13ContentItem minimalItem = lti13ContentItemDao.createItem(LTI13ContentItemTypesEnum.link, tool, deployment);
		lti13ContentItemDao.persistItem(minimalItem);
		dbInstance.commitAndCloseSession();
		
		List<LTI13ContentItem> loadedItems = lti13ContentItemDao.loadItemByTool(deployment);
		assertThat(loadedItems)
			.isNotNull()
			.hasSize(1)
			.containsExactly(minimalItem);
	}
}
