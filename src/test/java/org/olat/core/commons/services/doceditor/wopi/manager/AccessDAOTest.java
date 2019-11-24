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
package org.olat.core.commons.services.doceditor.wopi.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AccessDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSMetadataDAO vfsMetadataDAO;
	
	@Autowired
	private AccessDAO sut;
	
	@Before
	public void cleanUp() {
		sut.deleteAll();
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void shouldCreateAccess() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		String app = random();
		String token = random();
		boolean canEdit = false;
		boolean canClose = true;
		boolean versionControlled = true;
		Date expiresAt = Date.from(Instant.now().plus(Duration.ofHours(23)));
		
		Access access = sut.createAccess(vfsMetadata, identity, app, token, canEdit, canClose, versionControlled, expiresAt);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(access.getCreationDate()).isNotNull();
		softly.assertThat(access.getLastModified()).isNotNull();
		softly.assertThat(access.getToken()).isEqualTo(token);
		softly.assertThat(access.isCanEdit()).isEqualTo(canEdit);
		softly.assertThat(access.isCanClose()).isEqualTo(canClose);
		softly.assertThat(access.isVersionControlled()).isEqualTo(versionControlled);
		softly.assertThat(access.getExpiresAt()).isCloseTo(expiresAt, 2000);
		softly.assertThat(access.getMetadata()).isEqualTo(vfsMetadata);
		softly.assertThat(access.getIdentity()).isEqualTo(identity);
		softly.assertAll();
	}
	
	@Test
	public void shouldUpdateExpiresAt() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		Access access = sut.createAccess(vfsMetadata, identity, random(), random(), true, true, true, null);
		dbInstance.commitAndCloseSession();
		
		Date expiresIn24Hours = Date.from(Instant.now().plus(Duration.ofHours(24)));
		access = sut.updateExpiresAt(access, expiresIn24Hours);
		dbInstance.commitAndCloseSession();
		
		assertThat(access.getExpiresAt()).isCloseTo(expiresIn24Hours, 2000);
	}
	
	@Test
	public void shouldLoadAccessByToken() {
		createRandomAccess();
		createRandomAccess();
		Access createdAccess = createRandomAccess();
		dbInstance.commitAndCloseSession();
		
		Access reloadedAccess = reload(createdAccess);
		
		assertThat(reloadedAccess).isEqualTo(createdAccess);
	}

	@Test
	public void shouldLoadNoAccessIfInvalidToken() {
		Access access = sut.loadAccess(random());
		
		assertThat(access).isNull();
	}
	
	@Test
	public void shouldLoadAccessByMetadataAndIdentity() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi2");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		String app = random();
		boolean canEdit = true;
		Access access = sut.createAccess(vfsMetadata, identity, app, random(), canEdit, true, true, null);
		sut.createAccess(vfsMetadata, identity2, app, random(), canEdit, true, true, null);
		sut.createAccess(vfsMetadata, identity, app, random(), !canEdit, true, true, null);
		dbInstance.commitAndCloseSession();
		
		Access reloadedAccess = sut.loadAccess(vfsMetadata, identity, app, canEdit);
		
		assertThat(reloadedAccess).isEqualTo(access);
	}

	@Test
	public void shouldDeleteAccess() {
		Access createdAccess = createRandomAccess();

		sut.deleteAccess(createdAccess.getToken());
		dbInstance.commitAndCloseSession();
		
		Access reloadedAccess = reload(createdAccess);
		assertThat(reloadedAccess).isNull();
	}
	
	@Test
	public void shouldDeleteExpired() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		Date twoDaysAgo = Date.from(Instant.now().minus(Duration.ofDays(2)));;
		Access expiredTwoDaysAgo = sut.createAccess(vfsMetadata, identity, random(), random(), true, true, true, twoDaysAgo);
		Date oneDayAgo = Date.from(Instant.now().minus(Duration.ofDays(1)));;
		Access expiredOneDayAgo = sut.createAccess(vfsMetadata, identity, random(), random(), true, true, true, oneDayAgo);
		Date inOneDay = Date.from(Instant.now().plus(Duration.ofDays(1)));;
		Access expiresInOneDay = sut.createAccess(vfsMetadata, identity, random(), random(), true, true, true, inOneDay);
		Access noExpiration = sut.createAccess(vfsMetadata, identity, random(), random(), true, true, true, null);
		dbInstance.commitAndCloseSession();
		
		Date now = new Date();
		sut.deleteExpired(now);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(reload(expiredOneDayAgo)).isNull();
		softly.assertThat(reload(expiredTwoDaysAgo)).isNull();
		softly.assertThat(reload(expiresInOneDay)).isNotNull();
		softly.assertThat(reload(noExpiration)).isNotNull();
		softly.assertAll();
	}

	private Access reload(Access access) {
		return sut.loadAccess(access.getToken());
	}
	
	private Access createRandomAccess() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		String app = random();
		String token = random();
		boolean canEdit = false;
		boolean canClose = true;
		boolean versionControlled = true;
		
		return sut.createAccess(vfsMetadata, identity, app, token, canEdit, canClose, versionControlled, null);
	}
}
