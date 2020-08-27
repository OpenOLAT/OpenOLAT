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
package org.olat.core.commons.services.doceditor.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.UserInfo;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserInfoDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	
	@Autowired
	private UserInfoDAO sut;
	
	@Test
	public void shouldCreate() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		String info = random();
		
		UserInfo userInfo = sut.create(identity, info);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(userInfo.getCreationDate()).isNotNull();
		softly.assertThat(userInfo.getLastModified()).isNotNull();
		softly.assertThat(userInfo.getInfo()).isEqualTo(info);
		softly.assertThat(userInfo.getIdentity()).isEqualTo(identity);
		softly.assertAll();
	}
	
	@Test
	public void shouldUpdateInfo() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		UserInfo userInfo = sut.create(identity, random());
		dbInstance.commitAndCloseSession();
		
		String info = random();
		userInfo.setInfo(info);
		userInfo = sut.save(userInfo);
		dbInstance.commitAndCloseSession();
		
		assertThat(userInfo.getInfo()).isEqualTo(info);
	}
	
	@Test
	public void shouldDelete() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		sut.create(identity, random());
		dbInstance.commitAndCloseSession();
		
		sut.delete(identity);
		dbInstance.commitAndCloseSession();
		
		UserInfo reloaded = sut.load(identity);
		assertThat(reloaded).isNull();
	}
	
	@Test
	public void shouldLoadByIdentity() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		UserInfo userInfo = sut.create(identity, random());
		dbInstance.commitAndCloseSession();
		
		UserInfo reloaded = sut.load(identity);
		
		assertThat(reloaded).isEqualTo(userInfo);
	}


}
