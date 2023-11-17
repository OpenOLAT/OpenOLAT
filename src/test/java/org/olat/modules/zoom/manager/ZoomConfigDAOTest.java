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


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.ims.lti13.LTI13Context;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

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
	@Autowired
	private BusinessGroupService businessGroupService;

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
        LTI13Context ltiContext = zoomManager.createLtiContext(zoomProfile.getLtiTool(), entry, subIdent, null);
        
        ZoomConfig zoomConfig = zoomConfigDAO.createConfig(zoomProfile, ltiContext, "test");
        dbInstance.commitAndCloseSession();

        Assert.assertNotNull(zoomConfig);
        Assert.assertNotNull(zoomConfig.getKey());
        Assert.assertNotNull(zoomConfig.getLtiContext());
        Assert.assertEquals(zoomConfig.getLtiContext().getEntry(), ltiContext.getEntry());
    }
    
    @Test
    public void getConfigForCourse() {
        Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("zoom-test-author-2");
        RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);

        String clientId = UUID.randomUUID().toString();
        String ltiKey = UUID.randomUUID().toString();
        String subIdent = "12345678";
        ZoomProfile zoomProfile = createTestProfile(ltiKey, clientId);
        LTI13Context ltiContext = zoomManager.createLtiContext(zoomProfile.getLtiTool(), entry, subIdent, null);
        ZoomConfig zoomConfig = zoomConfigDAO.createConfig(zoomProfile, ltiContext, "test-get");
        
        dbInstance.commitAndCloseSession();
        
        ZoomConfig reloadedConfig = zoomConfigDAO.getConfig(entry, subIdent, null);
        Assert.assertNotNull(reloadedConfig);
        Assert.assertEquals(zoomConfig, reloadedConfig);
        Assert.assertEquals(ltiContext, reloadedConfig.getLtiContext());
    }
    
    @Test
    public void getConfigByRepositoryEntryKey() {
        Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("zoom-test-author-2");
        RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);

        String clientId = UUID.randomUUID().toString();
        String ltiKey = UUID.randomUUID().toString();
        String subIdent = "12345679";
        ZoomProfile zoomProfile = createTestProfile(ltiKey, clientId);
        LTI13Context ltiContext = zoomManager.createLtiContext(zoomProfile.getLtiTool(), entry, subIdent, null);
        ZoomConfig zoomConfig = zoomConfigDAO.createConfig(zoomProfile, ltiContext, "test-get");
        
        dbInstance.commitAndCloseSession();
        
        List<ZoomConfig> reloadedConfigs = zoomConfigDAO.getConfigs(entry.getKey());
		assertThat(reloadedConfigs)
			.isNotNull()
			.containsExactly(zoomConfig);
    }
    
    @Test
    public void getConfigByBusinessGroup() {
    	Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("zoom-test-author-3");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "Zoom Group", "", BusinessGroup.BUSINESS_TYPE, null, null, null, null, false, false, null);
		
		String clientId = UUID.randomUUID().toString();
        String ltiKey = UUID.randomUUID().toString();
        ZoomProfile zoomProfile = createTestProfile(ltiKey, clientId);
        LTI13Context ltiContext = zoomManager.createLtiContext(zoomProfile.getLtiTool(), null, null, businessGroup);
        ZoomConfig zoomConfig = zoomConfigDAO.createConfig(zoomProfile, ltiContext, "test-get");
		
        dbInstance.commitAndCloseSession();
        
        ZoomConfig reloadedConfig = zoomConfigDAO.getConfig(null, null, businessGroup);
        Assert.assertNotNull(reloadedConfig);
        Assert.assertEquals(zoomConfig, reloadedConfig);
        Assert.assertEquals(ltiContext, reloadedConfig.getLtiContext());
    }
    
    @Test
    public void getConfigByContextId() {
        Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("zoom-test-author-2");
        RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);

        String clientId = UUID.randomUUID().toString();
        String ltiKey = UUID.randomUUID().toString();
        String subIdent = "12345680";
        ZoomProfile zoomProfile = createTestProfile(ltiKey, clientId);
        LTI13Context ltiContext = zoomManager.createLtiContext(zoomProfile.getLtiTool(), entry, subIdent, null);
        ZoomConfig zoomConfig = zoomConfigDAO.createConfig(zoomProfile, ltiContext, "test-get");
        
        dbInstance.commitAndCloseSession();
        
        Optional<ZoomConfig> optionalConfig = zoomConfigDAO.getConfig(ltiContext.getContextId());
        Assert.assertTrue(optionalConfig.isPresent());
        Assert.assertEquals(zoomConfig, optionalConfig.get()); 
    }
    
}