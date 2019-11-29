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

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallbackBuilder;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.doceditor.wopi.WopiService;
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
public class WopiServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AccessDAO accessDao;
	@Autowired
	private VFSMetadataDAO vfsMetadataDAO;
	
	@Autowired
	private WopiService sut;
	
	@Before
	public void cleanUp() {
		accessDao.deleteAll();
		dbInstance.commitAndCloseSession();;
	}
	
	@Test
	public void shouldReuseExistingAccess() {
		VFSMetadata metadata = randomMetadata();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder().build();
		Access access = sut.getOrCreateAccess(metadata, identity, secCallback, "App1", null);
		dbInstance.commitAndCloseSession();
		
		Access secondAccess = sut.getOrCreateAccess(metadata, identity, secCallback, "App1", null);
		dbInstance.commitAndCloseSession();
		
		assertThat(secondAccess).isEqualTo(access);
	}
	
	@Test
	public void shouldHaveFifferentAccessPerApp() {
		VFSMetadata metadata = randomMetadata();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder().build();
		
		Access access1 = sut.getOrCreateAccess(metadata, identity, secCallback, "App1", null);
		Access access2 = sut.getOrCreateAccess(metadata, identity, secCallback, "App2", null);
		dbInstance.commitAndCloseSession();
		
		assertThat(access1).isNotEqualTo(access2);
	}

	@Test
	public void shouldNotReuseAccessIfModeChanged() {
		VFSMetadata metadata = randomMetadata();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder().withMode(Mode.EDIT).build();
		Access access = sut.getOrCreateAccess(metadata, identity, secCallback, "App1", null);
		dbInstance.commitAndCloseSession();
		
		DocEditorSecurityCallback secCallbackChanged = DocEditorSecurityCallbackBuilder.builder().withMode(Mode.VIEW).build();
		Access accessChanged = sut.getOrCreateAccess(metadata, identity, secCallbackChanged, "App1", null);
		dbInstance.commitAndCloseSession();
		
		assertThat(accessChanged).isNotEqualTo(access);
	}

	@Test
	public void shouldNotReuseAccessIfCanCloseChanged() {
		VFSMetadata metadata = randomMetadata();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder().withCanClose(true).build();
		Access access = sut.getOrCreateAccess(metadata, identity, secCallback, "App1", null);
		dbInstance.commitAndCloseSession();
		
		DocEditorSecurityCallback secCallbackChanged = DocEditorSecurityCallbackBuilder.builder().withCanClose(false).build();
		Access accessChanged = sut.getOrCreateAccess(metadata, identity, secCallbackChanged, "App1", null);
		dbInstance.commitAndCloseSession();
		
		assertThat(accessChanged).isNotEqualTo(access);
	}

	@Test
	public void shouldNotReuseAccessIfVersionControlledChanged() {
		VFSMetadata metadata = randomMetadata();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder().withVersionControlled(true).build();
		Access access = sut.getOrCreateAccess(metadata, identity, secCallback, "App1", null);
		dbInstance.commitAndCloseSession();
		
		DocEditorSecurityCallback secCallbackChanged = DocEditorSecurityCallbackBuilder.builder().withVersionControlled(false).build();
		Access accessChanged = sut.getOrCreateAccess(metadata, identity, secCallbackChanged, "App1", null);
		dbInstance.commitAndCloseSession();
		
		assertThat(accessChanged).isNotEqualTo(access);
	}
	
	@Test
	public void shouldUpdateExpiresAtIfReused() {
		VFSMetadata metadata = randomMetadata();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder().build();
		sut.getOrCreateAccess(metadata, identity, secCallback, "App1", null);
		dbInstance.commitAndCloseSession();
		
		Date expiresIn24Hours = Date.from(Instant.now().plus(Duration.ofHours(24)));
		Access secondAccess = sut.getOrCreateAccess(metadata, identity, secCallback, "App1", expiresIn24Hours);
		dbInstance.commitAndCloseSession();
		
		assertThat(secondAccess.getExpiresAt()).isCloseTo(expiresIn24Hours, 2000);
	}
	
	@Test
	public void shouldDeleteAccessIfChanged() {
		VFSMetadata metadata = randomMetadata();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder().withCanClose(true).build();
		Access access = sut.getOrCreateAccess(metadata, identity, secCallback, "App1", null);
		DocEditorSecurityCallback secCallbackChanged = DocEditorSecurityCallbackBuilder.builder().withCanClose(false).build();
		sut.getOrCreateAccess(metadata, identity, secCallbackChanged, "App1", null);
		dbInstance.commitAndCloseSession();
		
		Access reloadedAccess = sut.getAccess(access.getToken());
		
		assertThat(reloadedAccess).isNull();
	}
	
	@Test
	public void shouldGetAccessWithoutExpiredAt() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder().build();
		Access access = sut.getOrCreateAccess(randomMetadata(), identity, secCallback, "App1", null);
		dbInstance.commitAndCloseSession();
		
		Access reloadedAccess = sut.getAccess(access.getToken());
		
		assertThat(reloadedAccess).isEqualTo(access);
	}
	
	@Test
	public void shouldGetAccessExpiredAtInFutire() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder().build();
		Date in24Hours = Date.from(Instant.now().plus(Duration.ofHours(24)));
		Access access = sut.getOrCreateAccess(randomMetadata(), identity, secCallback, "App1", in24Hours);
		dbInstance.commitAndCloseSession();
		
		Access reloadedAccess = sut.getAccess(access.getToken());
		
		assertThat(reloadedAccess).isEqualTo(access);
	}
	
	@Test
	public void shouldDeleteAccessIfExpired() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("wopi");
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder().build();
		Date expired = Date.from(Instant.now().minus(Duration.ofHours(24)));
		Access access = sut.getOrCreateAccess(randomMetadata(), identity, secCallback, "App1", expired);
		dbInstance.commitAndCloseSession();
		
		Access reloadedAccess = sut.getAccess(access.getToken());
		
		assertThat(reloadedAccess).isNull();
	}

	private VFSMetadata randomMetadata() {
		return vfsMetadataDAO.createMetadata(random(), random(), random(), new Date(), 1000l, false, "file://" + random(), "file", null);
	}

}
