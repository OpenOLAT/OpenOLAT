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

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityDAOTest extends OlatTestCase {

	@Autowired
	private IdentityDAO identityDao;
	
	@Test
	public void findByNamesLogin() {
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("id-dao-1");
		String login = identity.getLogin();
		
		List<String> names = Arrays.asList(login);
		List<FindNamedIdentity> namedIdentities = identityDao.findByNames(names);
		Assert.assertNotNull(namedIdentities);
		Assert.assertEquals(1, namedIdentities.size());
		Assert.assertEquals(identity.getIdentity(), namedIdentities.get(0).getIdentity());
	}
	
	@Test
	public void findByNamesLegacyName() {
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("id-dao-2");
		String name = identity.getIdentity().getName();
		
		List<String> names = Arrays.asList(name);
		List<FindNamedIdentity> namedIdentities = identityDao.findByNames(names);
		Assert.assertNotNull(namedIdentities);
		Assert.assertEquals(1, namedIdentities.size());
		Assert.assertEquals(identity.getIdentity(), namedIdentities.get(0).getIdentity());
	}
	
	@Test
	public void findByNamesFirstLastNames() {
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("id-dao-3");
		String fullname = identity.getUser().getFirstName() + " " + identity.getUser().getLastName();
		
		List<String> names = Arrays.asList(fullname);
		List<FindNamedIdentity> namedIdentities = identityDao.findByNames(names);
		Assert.assertNotNull(namedIdentities);
		Assert.assertEquals(1, namedIdentities.size());
		Assert.assertEquals(identity.getIdentity(), namedIdentities.get(0).getIdentity());
	}
	
	@Test
	public void findByNamesALot() {
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("id-dao-5");
		String nickName = identity.getIdentity().getUser().getProperty(UserConstants.NICKNAME, null);
		
		List<String> lofOfNames = new ArrayList<>(65600);
		for(int i=0; i<65536; i++) {
			lofOfNames.add("this_is_a_fake_" + i);
		}
		lofOfNames.add(nickName);
		
		List<FindNamedIdentity> loadedIdentities = identityDao.findByNames(lofOfNames);
		assertThat(loadedIdentities)
			.isNotNull()
			.extracting(namedId -> namedId.getIdentity())
			.contains(identity.getIdentity());
	}
	
	@Test
	public void findIdentityByName() {
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("id-dao-3");
		String legacyName = identity.getIdentity().getName();
		
		Identity loadedIdentity = identityDao.findIdentityByName(legacyName);
		Assert.assertNotNull(loadedIdentity);
		Assert.assertEquals(identity.getIdentity(), loadedIdentity);
	}
	
	@Test
	public void findIdentityByNickName() {
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("id-dao-3");
		String nickName = identity.getIdentity().getUser().getProperty(UserConstants.NICKNAME, null);
		
		List<Identity> loadedIdentities = identityDao.findIdentitiesByNickName(nickName);
		Assert.assertNotNull(loadedIdentities);
		Assert.assertEquals(1, loadedIdentities.size());
		Assert.assertEquals(identity.getIdentity(), loadedIdentities.get(0));
	}

}
