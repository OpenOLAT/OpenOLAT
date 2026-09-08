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
import java.util.Locale;

import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
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
	private UserManager userManager;

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

	/**
	 * OO-9632 / OO-3476: an identity with an administrative role keeps its first and
	 * last name when deleted. The comment view (and any other portrait usage) must
	 * show that name rather than the fully anonymous "unknown user" label.
	 */
	@Test
	public void shouldShowPreservedNameForDeletedIdentityWithName() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		admin.getUser().setProperty(UserConstants.FIRSTNAME, "Anna");
		admin.getUser().setProperty(UserConstants.LASTNAME, "Admin");
		userManager.updateUser(admin, admin.getUser());
		dbInstance.commitAndCloseSession();

		admin = securityManager.saveIdentityStatus(admin, Identity.STATUS_DELETED, admin);
		dbInstance.commitAndCloseSession();

		PortraitUser portraitUser = sut.createPortraitUser(Locale.GERMAN, admin);

		String expectedName = userManager.getUserDisplayName("Anna", "Admin");
		assertThat(portraitUser.getDisplayName()).isEqualTo(expectedName);
		assertThat(portraitUser.isPortraitAvailable()).isFalse();
		assertThat(portraitUser.getIdentityStatus()).isEqualTo(Identity.STATUS_DELETED);
	}

	/**
	 * OO-9632: a deleted identity without a preserved name (the regular case, user
	 * properties emptied) still shows the generic anonymous "unknown user" label.
	 */
	@Test
	public void shouldShowUnknownUserForDeletedIdentityWithoutName() {
		Identity learner = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		learner.getUser().setProperty(UserConstants.FIRSTNAME, null);
		learner.getUser().setProperty(UserConstants.LASTNAME, null);
		userManager.updateUser(learner, learner.getUser());
		dbInstance.commitAndCloseSession();

		learner = securityManager.saveIdentityStatus(learner, Identity.STATUS_DELETED, learner);
		dbInstance.commitAndCloseSession();

		PortraitUser portraitUser = sut.createPortraitUser(Locale.GERMAN, learner);

		String unknownLabel = Util.createPackageTranslator(UserPortraitComponent.class, Locale.GERMAN).translate("user.unknown");
		assertThat(portraitUser.getDisplayName()).isEqualTo(unknownLabel);
	}

}
