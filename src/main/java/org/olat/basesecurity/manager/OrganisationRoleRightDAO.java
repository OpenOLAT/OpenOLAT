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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.OrganisationRoleRight;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.model.OrganisationRoleRightImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 9 oct 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 *
 */
@Service
public class OrganisationRoleRightDAO {

    @Autowired
    private DB dbInstance;

    public List<String> getGrantedOrganisationRights(Organisation organisation, OrganisationRoles role) {
        String sb = """
        	select roleRight.right from organisationroleright as roleRight
        	inner join roleRight.organisation org
        	where org.key=:organisationKey and roleRight.role=:organisationRole""";

        return dbInstance.getCurrentEntityManager()
                .createQuery(sb, String.class)
                .setParameter("organisationKey", organisation.getKey())
                .setParameter("organisationRole", role)
                .getResultList();
    }
    
    public List<String> getGrantedOrganisationsRights(Collection<OrganisationRef> organisations, OrganisationRoles role) {
    	if(organisations == null || organisations.isEmpty()) return new ArrayList<>();
    	
        String sb = """
        	select roleRight.right from organisationroleright as roleRight
        	inner join roleRight.organisation org
        	where org.key in (:organisationsKeys) and roleRight.role=:organisationRole""";
        
        List<Long> organisationsKeys = organisations.stream()
        		.map(OrganisationRef::getKey)
        		.toList();
        return dbInstance.getCurrentEntityManager()
                .createQuery(sb, String.class)
                .setParameter("organisationsKeys", organisationsKeys)
                .setParameter("organisationRole", role)
                .getResultList();
    }
    

    public Collection<OrganisationRoleRight> getOrganisationRoleRights(OrganisationRoles orgRole) {
		String sb = "select roleRight from organisationroleright as roleRight where roleRight.role = :orgRole";
        return dbInstance.getCurrentEntityManager()
                .createQuery(sb, OrganisationRoleRight.class)
                .setParameter("orgRole", orgRole)
                .getResultList();
    }

    public void deleteGrantedOrganisationRights(Organisation organisation, OrganisationRoles role, Collection<String> deleteRights) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("delete from organisationroleright as orgRight")
                .append(" where orgRight.organisation.key=:orgKey")
                .append(" and orgRight.right in (:deleteRights)")
                .append(" and orgRight.role=:orgRole");

        dbInstance.getCurrentEntityManager()
                .createQuery(sb.toString())
                .setParameter("orgKey", organisation.getKey())
                .setParameter("deleteRights", deleteRights)
                .setParameter("orgRole", role)
                .executeUpdate();
    }
    
    public void deleteOrganisationRoleRight(OrganisationRoleRight orgRoleRight) {
        dbInstance.getCurrentEntityManager().remove(orgRoleRight);
    }

    public OrganisationRoleRight createOrganisationRoleRight(Organisation organisation, OrganisationRoles role, String right) {
        OrganisationRoleRightImpl newRight = new OrganisationRoleRightImpl();
        newRight.setCreationDate(new Date());
        newRight.setOrganisation(organisation);
        newRight.setRole(role);
        newRight.setRight(right);

        dbInstance.getCurrentEntityManager().persist(newRight);
        return newRight;
    }
}
