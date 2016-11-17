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
package org.olat.modules.portfolio.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.BinderUserInformations;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderUserInformationsDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private BinderUserInformationsDAO binderUserInformationsDAO;
	
	@Test
	public void updateBinderUserInformations() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("bu-1");
		BinderImpl binder = binderDao.createAndPersist("Binder infos", "Binder with one section.", null, null);
		dbInstance.commitAndCloseSession();

		//update the infos
		binderUserInformationsDAO.updateBinderUserInformations(binder, identity);
		dbInstance.commit();
		
		//load the infos and check
		BinderUserInformations infos = binderUserInformationsDAO.getBinderUserInfos(binder, identity);
		Assert.assertNotNull(infos);
		Assert.assertNotNull(infos.getKey());
		Assert.assertNotNull(infos.getCreationDate());
		Assert.assertNotNull(infos.getLastModified());
		Assert.assertNotNull(infos.getInitialLaunch());
		Assert.assertNotNull(infos.getRecentLaunch());
		Assert.assertEquals(1, infos.getVisit());
		Assert.assertEquals(binder, infos.getBinder());
		Assert.assertEquals(identity, infos.getIdentity());
	}
}
