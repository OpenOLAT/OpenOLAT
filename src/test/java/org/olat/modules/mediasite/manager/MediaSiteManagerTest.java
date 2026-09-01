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
package org.olat.modules.mediasite.manager;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.course.nodes.MediaSiteCourseNode;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolDeploymentType;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.manager.LTI13ToolDAO;
import org.olat.ims.lti13.manager.LTI13ToolDeploymentDAO;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.mediasite.MediaSiteManager;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Regression tests for the OO-9717 "Implicit first deployment" bullet: a course-local MediaSite
 * LTI 1.3 tool's deployment must be resolved by the explicit key stored in the course node's
 * module configuration, falling back to the tool's only/first deployment only for courses
 * configured before that key was stored.
 *
 * Initial date: 2026-09-01<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MediaSiteManagerTest extends OlatTestCase {

	@Autowired
	private MediaSiteManager mediaSiteManager;
	@Autowired
	private LTI13ToolDAO lti13ToolDao;
	@Autowired
	private LTI13ToolDeploymentDAO lti13ToolDeploymentDao;

	private LTI13Tool createTool() {
		String toolUrl = "https://mediasite.example.com/tool";
		String clientId = UUID.randomUUID().toString();
		String initiateLoginUrl = "https://mediasite.example.com/lti/login";
		return lti13ToolDao.createTool("MediaSite course tool", toolUrl, clientId, initiateLoginUrl, null, LTI13ToolType.MEDIASITE_COURSE);
	}

	@Test
	public void resolveCourseDeployment_withExplicitKey() {
		LTI13Tool tool = createTool();
		// Two deployments on the same tool: an old/first one that an implicit ".get(0)" lookup
		// would incorrectly return, and the one actually referenced by the stored explicit key.
		lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.MULTIPLE_CONTEXTS, null, tool);
		LTI13ToolDeployment explicitDeployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.MULTIPLE_CONTEXTS, null, tool);

		ModuleConfiguration config = new ModuleConfiguration();
		config.setStringValue(MediaSiteCourseNode.CONFIG_LTI13_DEPLOYMENT_KEY, String.valueOf(explicitDeployment.getKey()));

		LTI13ToolDeployment resolved = mediaSiteManager.resolveCourseDeployment(config, tool);

		Assert.assertNotNull(resolved);
		Assert.assertEquals(explicitDeployment.getKey(), resolved.getKey());
	}

	@Test
	public void resolveCourseDeployment_legacyFallbackWithoutExplicitKey() {
		LTI13Tool tool = createTool();
		LTI13ToolDeployment deployment = lti13ToolDeploymentDao.createDeployment(null, LTI13ToolDeploymentType.MULTIPLE_CONTEXTS, null, tool);

		// Simulate a course configured before the deployment key was stored explicitly:
		// only the tool key would have been present, never the deployment key.
		ModuleConfiguration config = new ModuleConfiguration();

		LTI13ToolDeployment resolved = mediaSiteManager.resolveCourseDeployment(config, tool);

		Assert.assertNotNull(resolved);
		Assert.assertEquals(deployment.getKey(), resolved.getKey());
	}

	@Test
	public void resolveCourseDeployment_noDeploymentAtAll() {
		// A tool that (for whatever reason) never got a deployment - must not throw, must return null
		// so the caller can show its own "not configured" message, instead of an implicit .get(0)
		// on an empty list blowing up with an IndexOutOfBoundsException.
		LTI13Tool tool = createTool();
		ModuleConfiguration config = new ModuleConfiguration();

		LTI13ToolDeployment resolved = mediaSiteManager.resolveCourseDeployment(config, tool);

		Assert.assertNull(resolved);
	}
}
