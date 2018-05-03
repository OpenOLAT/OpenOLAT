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
package org.olat.repository.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryToOrganisation;
import org.olat.repository.RepositoryService;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryToOrganisationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryToOrganisationDAO repositoryEntryToOrganisationDao;
	
	@Test
	public void createRelation() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationDao.createAndPersistOrganisation("Repo-org-1", null, null, null, null);
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel", "rel", null, null, 0, defOrganisation);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryToOrganisation relation = repositoryEntryToOrganisationDao
				.createRelation(organisation, re, true);
		dbInstance.commit();
		Assert.assertNotNull(relation);
	}

}
