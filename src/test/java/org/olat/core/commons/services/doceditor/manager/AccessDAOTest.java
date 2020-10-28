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

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
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
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		String app = random();
		boolean versionControlled = true;
		boolean download = false;
		Date expiresAt = Date.from(Instant.now().plus(Duration.ofHours(23)));
		
		Access access = sut.createAccess(vfsMetadata, identity, app, Mode.EDIT, versionControlled, download, expiresAt);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(access.getCreationDate()).isNotNull();
		softly.assertThat(access.getLastModified()).isNotNull();
		softly.assertThat(access.getMode()).isEqualTo(Mode.EDIT);
		softly.assertThat(access.isVersionControlled()).isEqualTo(versionControlled);
		softly.assertThat(access.isDownload()).isFalse();
		softly.assertThat(access.getExpiresAt()).isCloseTo(expiresAt, 2000);
		softly.assertThat(access.getMetadata()).isEqualTo(vfsMetadata);
		softly.assertThat(access.getIdentity()).isEqualTo(identity);
		softly.assertAll();
	}
	
	@Test
	public void shouldUpdateExpiresAt() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		Access access = sut.createAccess(vfsMetadata, identity, random(), Mode.EDIT, true, true, new Date());
		dbInstance.commitAndCloseSession();
		
		Date expiresIn24Hours = Date.from(Instant.now().plus(Duration.ofHours(24)));
		access = sut.updateExpiresAt(access, expiresIn24Hours);
		dbInstance.commitAndCloseSession();
		
		assertThat(access.getExpiresAt()).isCloseTo(expiresIn24Hours, 2000);
	}
	
	@Test
	public void shouldUpdateMode() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		Access access = sut.createAccess(vfsMetadata, identity, random(), Mode.EDIT, true, true, new Date());
		dbInstance.commitAndCloseSession();
		
		access = sut.updateMode(access, Mode.VIEW);
		dbInstance.commitAndCloseSession();
		
		assertThat(access.getMode()).isEqualTo(Mode.VIEW);
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
		Access access = sut.loadAccess(() -> new Random().nextLong());
		
		assertThat(access).isNull();
	}
	
	@Test
	public void shouldGetAccessesByMode() {
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor");
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor2");
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor3");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		Access accessEdit1 = sut.createAccess(vfsMetadata, identity1, "app1", Mode.EDIT, true, true, new Date());
		Access accessEdit2 = sut.createAccess(vfsMetadata, identity2, "app1", Mode.EDIT, true, true, new Date());
		Access accessEdit3 = sut.createAccess(vfsMetadata, identity1, "app2", Mode.EDIT, true, true, new Date());
		Access accessView = sut.createAccess(vfsMetadata, identity3, "app1", Mode.VIEW, true, true, new Date());
		dbInstance.commitAndCloseSession();
		
		List<Access> accesses = sut.getAccesses(Mode.EDIT);
		
		assertThat(accesses)
				.containsExactlyInAnyOrder(accessEdit1, accessEdit2, accessEdit3)
				.doesNotContain(accessView);
	}
	
	@Test
	public void shouldGetAccessCount() {
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor");
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor2");
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor3");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		sut.createAccess(vfsMetadata, identity1, "app1", Mode.EDIT, true, true, new Date());
		sut.createAccess(vfsMetadata, identity2, "app1", Mode.EDIT, true, true, new Date());
		sut.createAccess(vfsMetadata, identity1, "app2", Mode.EDIT, true, true, new Date());
		sut.createAccess(vfsMetadata, identity3, "app1", Mode.VIEW, true, true, new Date());
		dbInstance.commitAndCloseSession();
		
		Long accessCount = sut.getAccessCount("app1", Mode.EDIT);
		
		assertThat(accessCount).isEqualTo(2);
	}
	
	@Test
	public void shouldGetAccessCountByMetadataAndIdentity() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor");
		Identity identityOther = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor2");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		VFSMetadata vfsMetadataOther = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		String editorType = "et";
		sut.createAccess(vfsMetadata, identity, editorType, Mode.EDIT, true, true, new Date());
		sut.createAccess(vfsMetadata, identity, editorType, Mode.EDIT, true, true, new Date());
		sut.createAccess(vfsMetadata, identity, editorType, Mode.VIEW, true, true, new Date());
		sut.createAccess(vfsMetadataOther, identity, editorType, Mode.EDIT, true, true, new Date());
		sut.createAccess(vfsMetadata, identityOther, editorType, Mode.EDIT, true, true, new Date());
		sut.createAccess(vfsMetadata, identity, "other", Mode.EDIT, true, true, new Date());
		dbInstance.commitAndCloseSession();
		
		Long accessCount = sut.getAccessCount(editorType, vfsMetadata, identity);
		
		assertThat(accessCount).isEqualTo(3);
	}

	@Test
	public void shouldDelete() {
		Access createdAccess = createRandomAccess();

		sut.delete(createdAccess);
		dbInstance.commitAndCloseSession();
		
		Access reloadedAccess = reload(createdAccess);
		assertThat(reloadedAccess).isNull();
	}
	
	@Test
	public void shouldDeleteAccessByIdentity() {
		Access access = createRandomAccess();
		
		sut.deleteByIdentity(access.getIdentity());
		dbInstance.commitAndCloseSession();
		
		Access reloadedAccess = reload(access);
		assertThat(reloadedAccess).isNull();
	}
	
	@Test
	public void shouldDeleteExpired() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		Date twoDaysAgo = Date.from(Instant.now().minus(Duration.ofDays(2)));
		Access expiredTwoDaysAgo = sut.createAccess(vfsMetadata, identity, random(), Mode.EDIT, true, true, twoDaysAgo);
		Date oneDayAgo = Date.from(Instant.now().minus(Duration.ofDays(1)));
		Access expiredOneDayAgo = sut.createAccess(vfsMetadata, identity, random(), Mode.EDIT, true, true, oneDayAgo);
		Date inOneDay = Date.from(Instant.now().plus(Duration.ofDays(1)));
		Access expiresInOneDay = sut.createAccess(vfsMetadata, identity, random(), Mode.EDIT, true, true, inOneDay);
		dbInstance.commitAndCloseSession();
		
		Date now = new Date();
		sut.deleteExpired(now);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(reload(expiredOneDayAgo)).isNull();
		softly.assertThat(reload(expiredTwoDaysAgo)).isNull();
		softly.assertThat(reload(expiresInOneDay)).isNotNull();
		softly.assertAll();
	}

	private Access reload(Access access) {
		return sut.loadAccess(access);
	}
	
	private Access createRandomAccess() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("doceditor");
		VFSMetadata vfsMetadata = vfsMetadataDAO.createMetadata(random(), "relPath", "file.name", new Date(), 1000l, false, "file://" + random(), "file", null);
		String app = random();
		boolean versionControlled = true;
		
		return sut.createAccess(vfsMetadata, identity, app, Mode.EDIT, versionControlled, true, new Date());
	}
}
