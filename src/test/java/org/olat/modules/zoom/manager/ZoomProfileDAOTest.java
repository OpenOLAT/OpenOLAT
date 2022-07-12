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
import org.olat.ims.lti13.LTI13Tool;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

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
}