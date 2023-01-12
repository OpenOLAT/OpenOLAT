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
package org.olat.basesecurity.manager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.RightProvider;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 9 oct 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 *
 */
public class OrganisationRoleRightDAOTest extends OlatTestCase {
	
    @Autowired
    private DB dbInstance;
    @Autowired
    private OrganisationService organisationService;

    @Test
    public void loadNotDefinedRights() {
    	Organisation root = organisationService.createOrganisation("Org-role-rights 1", "Org-role-rights 1", null, null, null);
    	dbInstance.commitAndCloseSession();
    	
        List<RightProvider> rights = organisationService.getGrantedOrganisationRights(root, OrganisationRoles.linemanager);
        Assert.assertNotNull(rights);
        Assert.assertTrue(rights.isEmpty());
    }

    @Test
    public void setRightsForRole() {
    	Organisation root = organisationService.createOrganisation("Org-role-rights 2", "Org-role-rights 2", null, null, null);
    	dbInstance.commitAndCloseSession();
    	
        List<RightProvider> allRights = organisationService.getAllOrganisationRights(OrganisationRoles.linemanager);
        Collection<String> selectedRights = allRights.stream().map(RightProvider::getRight).collect(Collectors.toSet());
        organisationService.setGrantedOrganisationRights(root, OrganisationRoles.linemanager, selectedRights);
        dbInstance.commitAndCloseSession();

        List<RightProvider> rightsFromOrgRole = organisationService.getGrantedOrganisationRights(root, OrganisationRoles.linemanager);
        Assert.assertNotNull(rightsFromOrgRole);
        Assert.assertEquals(allRights.size(), rightsFromOrgRole.size());
    }

    @Test
    public void getRightsForChildOrganisation() {
    	Organisation root = organisationService.createOrganisation("Org-role-rights 3", "Org-role-rights 3", null, null, null);
    	Organisation child = organisationService.createOrganisation("Org-role-rights 3.1", "Org-role-rights 3.1", null, root, null);
    	dbInstance.commitAndCloseSession();
    	
        List<RightProvider> allRights = organisationService.getAllOrganisationRights(OrganisationRoles.linemanager);
        Collection<String> selectedRights = allRights.stream().map(RightProvider::getRight).collect(Collectors.toList());
        organisationService.setGrantedOrganisationRights(root, OrganisationRoles.linemanager, selectedRights);
        dbInstance.commitAndCloseSession();

        List<RightProvider> rightsFromChild = organisationService.getGrantedOrganisationRights(child, OrganisationRoles.linemanager);
        Assert.assertNotNull(rightsFromChild);
        Assert.assertEquals(allRights.size(), rightsFromChild.size());
    }

    @Test
    public void moveOrganisation() {
    	Organisation root = organisationService.createOrganisation("Org-role-rights 4.1", "Org-role-rights 4.1", null, null, null);
    	Organisation rootAlt = organisationService.createOrganisation("Org-role-rights 4.2", "Org-role-rights 4.2", null, null, null);
    	dbInstance.commitAndCloseSession();
    	
        List<RightProvider> allRights = organisationService.getAllOrganisationRights(OrganisationRoles.linemanager);
        Collection<String> selectedRights = allRights.stream().map(RightProvider::getRight).collect(Collectors.toList());

        organisationService.setGrantedOrganisationRights(root, OrganisationRoles.linemanager, selectedRights);

        organisationService.moveOrganisation(root, rootAlt);

        List<RightProvider> rightsFromOrgRole = organisationService.getGrantedOrganisationRights(rootAlt, OrganisationRoles.linemanager);

        Assert.assertNotNull(rightsFromOrgRole);
        Assert.assertEquals(0, rightsFromOrgRole.size());
    }

    @Test
    public void deleteOrganisation() {
    	Organisation root = organisationService.createOrganisation("Org-role-rights 5", "Org-role-rights 5", null, null, null);
        List<RightProvider> allRights = organisationService.getAllOrganisationRights(OrganisationRoles.linemanager);
        Collection<String> selectedRights = allRights.stream().map(RightProvider::getRight).collect(Collectors.toList());

        organisationService.setGrantedOrganisationRights(root, OrganisationRoles.linemanager, selectedRights);

        organisationService.deleteOrganisation(root, null);

        List<RightProvider> rightsFromOrgRole = organisationService.getGrantedOrganisationRights(root, OrganisationRoles.linemanager);

        Assert.assertNotNull(rightsFromOrgRole);
        Assert.assertEquals(0, rightsFromOrgRole.size());
    }
}
