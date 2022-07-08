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
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomConfigDAOTest extends OlatTestCase {

    @Autowired
    private ZoomConfigDAO zoomConfigDAO;

    @Autowired
    ZoomManager zoomManager;

    @Autowired
    private DB dbInstance;

    private ZoomProfile createTestProfile(String ltiKey, String clientId) {
        return zoomManager.createProfile("Test", ltiKey, clientId, "1234");
    }

    @Test
    public void testCreateConfig() {
        Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("zoom-test-author-1");
        RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);

        String clientId = UUID.randomUUID().toString();
        String ltiKey = UUID.randomUUID().toString();
        String subIdent = "12345678";
        ZoomProfile zoomProfile = createTestProfile(ltiKey, clientId);
        LTI13ToolDeployment toolDeployment = zoomManager.createLtiToolDeployment(zoomProfile.getLtiTool(), entry, subIdent, null);
        ZoomConfig zoomConfig = zoomConfigDAO.createConfig(zoomProfile, toolDeployment, "test");
        dbInstance.commitAndCloseSession();

        Assert.assertNotNull(zoomConfig);
        Assert.assertNotNull(zoomConfig.getKey());
        Assert.assertNotNull(zoomConfig.getLtiToolDeployment());
        Assert.assertEquals(zoomConfig.getLtiToolDeployment().getEntry(), toolDeployment.getEntry());
    }
}