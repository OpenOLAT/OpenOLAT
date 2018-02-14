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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.organisation.OrganisationType;
import org.olat.modules.organisation.manager.OrganisationTypeDAO;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationTypeDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationTypeDAO organisationTypeDao;
	
	@Test
	public void createOrganisationType() {
		OrganisationType type = organisationTypeDao.createAndPersist("Typo", "3.0");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(type);
		Assert.assertNotNull(type.getKey());
		Assert.assertNotNull(type.getCreationDate());
		Assert.assertNotNull(type.getLastModified());
		Assert.assertEquals("Typo", type.getDisplayName());
		Assert.assertEquals("3.0", type.getIdentifier());
	}
	
	@Test
	public void loadOrganisationType() {
		OrganisationType type = organisationTypeDao.createAndPersist("Typo", "4.0");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(type);
		
		OrganisationType reloadedType = organisationTypeDao.loadByKey(type.getKey());
		Assert.assertNotNull(reloadedType);
		Assert.assertNotNull(reloadedType.getCreationDate());
		Assert.assertNotNull(reloadedType.getLastModified());
		Assert.assertEquals("Typo", reloadedType.getDisplayName());
		Assert.assertEquals("4.0", reloadedType.getIdentifier());
		Assert.assertEquals(type, reloadedType);
	}
}
