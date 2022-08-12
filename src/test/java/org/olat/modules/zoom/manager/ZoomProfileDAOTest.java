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
package org.olat.modules.zoom.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomProfileDAOTest extends OlatTestCase {

    @Autowired
    private ZoomProfileDAO zoomProfileDAO;

    @Autowired
    ZoomManager zoomManager;

    @Autowired
    private DB dbInstance;

    private LTI13Tool createTestLtiTool(String ltiKey, String clientId) {
        return ((ZoomManagerImpl) zoomManager).createLtiTool("TestLti", ltiKey, clientId);
    }

    private ZoomProfile createTestZoomProfile(String name) {
        String ltiKey = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString().substring(0, 8);
        String clientId = UUID.randomUUID().toString();
        LTI13Tool lti13Tool = createTestLtiTool(ltiKey, clientId);
        return zoomProfileDAO.createProfile(name, ltiKey, lti13Tool, token);
    }

    @Test
    public void testCreateProfile() {
        String ltiKey = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString().substring(0, 8);
        String clientId = UUID.randomUUID().toString();
        LTI13Tool lti13Tool = createTestLtiTool(ltiKey, clientId);
        ZoomProfile zoomProfile = zoomProfileDAO.createProfile("Test", ltiKey, lti13Tool, token);

        Assert.assertNotNull(zoomProfile);
        Assert.assertNotNull(zoomProfile.getKey());
        Assert.assertEquals("Test", zoomProfile.getName());
        Assert.assertNotNull(zoomProfile.getLtiTool());
        Assert.assertTrue(zoomProfile.getLtiTool().getInitiateLoginUrl().contains(ltiKey));
        Assert.assertEquals(clientId, zoomProfile.getLtiTool().getClientId());
    }

    @Test
    public void testGetProfile() {
        String ltiKey = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString().substring(0, 8);
        String clientId = UUID.randomUUID().toString();
        LTI13Tool lti13Tool = createTestLtiTool(ltiKey, clientId);
        zoomProfileDAO.createProfile("ClientId Test", ltiKey, lti13Tool, token);

        ZoomProfile zoomProfileForClientId = zoomProfileDAO.getProfile(clientId);
        ZoomProfile zoomProfileForOtherClientId = zoomProfileDAO.getProfile(UUID.randomUUID().toString());

        Assert.assertNotNull(zoomProfileForClientId);
        Assert.assertEquals("ClientId Test", zoomProfileForClientId.getName());
        Assert.assertEquals(clientId, zoomProfileForClientId.getLtiTool().getClientId());
        Assert.assertNull(zoomProfileForOtherClientId);
    }

    @Test
    public void testGetApplications() {
        ZoomProfile zoomProfile = createTestZoomProfile("Profile In Use");
        Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("zoom-author-1");
        RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author);

        zoomManager.initializeConfig(courseEntry, "1234", null,
                ZoomManager.ApplicationType.courseTool, zoomProfile.getLtiTool().getClientId(), null);
        zoomManager.initializeConfig(courseEntry, "2345", null,
                ZoomManager.ApplicationType.courseElement, zoomProfile.getLtiTool().getClientId(), null);

        List<ZoomProfileDAO.ZoomProfileApplication> applications = zoomProfileDAO.getApplications(zoomProfile.getKey());

        Assert.assertNotNull(applications);
        Assert.assertEquals(2, applications.size());
        Assert.assertEquals("Profile In Use", applications.get(0).getName());
        Assert.assertEquals("Profile In Use", applications.get(1).getName());
        Assert.assertEquals("courseElement-" + courseEntry.getKey() + "-2345", applications.get(0).getDescription());
        Assert.assertEquals("courseTool-" + courseEntry.getKey() + "-1234", applications.get(1).getDescription());
        Assert.assertEquals(ZoomManager.ApplicationType.courseElement, applications.get(0).getApplicationType());
        Assert.assertEquals(ZoomManager.ApplicationType.courseTool, applications.get(1).getApplicationType());
        Assert.assertNotNull(applications.get(0).getLti13ToolDeployment());
        Assert.assertEquals(applications.get(0).getLti13ToolDeployment().getEntry(), courseEntry);
        Assert.assertNull(applications.get(1).getLti13ToolDeployment().getBusinessGroup());
    }
}