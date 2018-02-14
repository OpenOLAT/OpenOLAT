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
package org.olat.modules.organisation.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.organisation.Organisation;
import org.olat.modules.organisation.OrganisationType;
import org.olat.modules.organisation.model.OrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OrganisationDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	
	public Organisation create(String displayName, String identifier, String description,
			Organisation parentOrganisation, OrganisationType type) {
		OrganisationImpl organisation = new OrganisationImpl();
		organisation.setCreationDate(new Date());
		organisation.setLastModified(organisation.getCreationDate());
		organisation.setDisplayName(displayName);
		organisation.setIdentifier(identifier);
		organisation.setDescription(description);
		organisation.setParent(parentOrganisation);
		if(parentOrganisation != null && parentOrganisation.getRoot() != null) {
			organisation.setRoot(parentOrganisation.getRoot());
		} else {
			organisation.setRoot(parentOrganisation);
		}
		organisation.setType(type);
		return organisation;
	}
	
	public Organisation createAndPersistOrganisation(String displayName, String identifier, String description,
			Organisation parentOrganisation, OrganisationType type) {
		OrganisationImpl organisation = (OrganisationImpl)create(displayName, identifier, description, parentOrganisation, type);
		organisation.setGroup(groupDao.createGroup());
		dbInstance.getCurrentEntityManager().persist(organisation);
		organisation.setMaterializedPathKeys(getMaterializedPathKeys(parentOrganisation, organisation));
		organisation = dbInstance.getCurrentEntityManager().merge(organisation);
		return organisation;
	}
	
	private String getMaterializedPathKeys(Organisation parent, Organisation level) {
		if(parent != null) {
			String parentPathOfKeys = parent.getMaterializedPathKeys();
			if(parentPathOfKeys == null || "/".equals(parentPathOfKeys)) {
				parentPathOfKeys = "";
			}
			return parentPathOfKeys + level.getKey() + "/";
		}
		return "/" + level.getKey() + "/";
	}
	
	public Organisation update(Organisation organisation) {
		if(organisation.getKey() == null) {
			OrganisationImpl orgImpl = (OrganisationImpl)organisation;
			if(orgImpl.getGroup() == null) {
				orgImpl.setGroup(groupDao.createGroup());
			}
			if(orgImpl.getCreationDate() == null) {
				orgImpl.setCreationDate(new Date());
			}
			if(orgImpl.getLastModified() == null) {
				orgImpl.setLastModified(orgImpl.getCreationDate());
			}
			dbInstance.getCurrentEntityManager().persist(orgImpl);
			orgImpl.setMaterializedPathKeys(getMaterializedPathKeys(orgImpl.getParent(), organisation));
			organisation = dbInstance.getCurrentEntityManager().merge(orgImpl);
		} else {
			((OrganisationImpl)organisation).setLastModified(new Date());
		}
		
		return dbInstance.getCurrentEntityManager().merge(organisation);
	}
	
	/**
	 * The method fetch the group, the organisation type and the parent
	 * organisation but not the root.
	 * 
	 * @param key the primary key of an organisation
	 * @return The organisation or null if not found
	 */
	public Organisation loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where org.key=:key");
		
		List<Organisation> organisations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("key", key)
				.getResultList();
		return organisations == null || organisations.isEmpty() ? null : organisations.get(0);
	}
	
	public List<Organisation> loadByIdentifier(String identifier) {
		StringBuilder sb = new StringBuilder();
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where org.identifier=:identifier");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("identifier", identifier)
				.getResultList();
	}
	
	public List<Organisation> find() {
		StringBuilder sb = new StringBuilder();
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.getResultList();
	}
}
