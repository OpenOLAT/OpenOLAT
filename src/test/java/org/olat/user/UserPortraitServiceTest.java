/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.restapi.CourseTest;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Apr 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserPortraitServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	
	@Autowired
	private UserPortraitService sut;

	@Test
	public void shouldStorePortraitImage() throws URISyntaxException {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		File file = new File(CourseTest.class.getResource("portrait.jpg").toURI());
		
		sut.storePortraitImage(doer, doer, file, "portrait.jpg");
		dbInstance.commitAndCloseSession();
		
		doer = securityManager.loadIdentityByKey(doer.getKey());
		assertThat(doer.getUser().getPortraitPath()).isNotBlank();
	}

	@Test
	public void shouldReplacePortraitImage() throws URISyntaxException {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		File file = new File(CourseTest.class.getResource("portrait.jpg").toURI());
		
		sut.storePortraitImage(doer, doer, file, "portrait.jpg");
		dbInstance.commitAndCloseSession();
		
		doer = securityManager.loadIdentityByKey(doer.getKey());
		String portraitPath = doer.getUser().getPortraitPath();
		
		sut.storePortraitImage(doer, doer, file, "portrait.jpg");
		dbInstance.commitAndCloseSession();
		
		doer = securityManager.loadIdentityByKey(doer.getKey());
		String replacedPortraitPath = doer.getUser().getPortraitPath();
		
		assertThat(portraitPath).isNotEqualTo(replacedPortraitPath);
		assertThat(sut.getImage(portraitPath, null)).isNull();
		assertThat(sut.getImage(replacedPortraitPath, null)).isNotNull();
	}

	@Test
	public void shouldDeletePortraitImage() throws URISyntaxException {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		File file = new File(CourseTest.class.getResource("portrait.jpg").toURI());
		
		sut.storePortraitImage(doer, doer, file, "portrait.jpg");
		dbInstance.commitAndCloseSession();
		
		sut.deletePortraitImage(doer);
		
		doer = securityManager.loadIdentityByKey(doer.getKey());
		assertThat(doer.getUser().getPortraitPath()).isNull();
	}

}
