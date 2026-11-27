/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.edusharing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.UserSession;
import org.olat.modules.edusharing.model.EdusharingUsageImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.settings.RepositoryEdusharingProvider;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 27.11.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EdusharingDispatcherAccessTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private EdusharingDispatcher edusharingDispatcher;
	
	@Test
	public void shouldAllowMember() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("es-participant");
		repositoryService.addRole(participant, entry, GroupRoles.participant.name());
		EdusharingUsage usage = createUsage(participant, entry, "node");
		
		assertThat(isAllowed(participant, new UserSession(), usage)).isTrue();
	}
	
	@Test
	public void shouldDenyNonMember() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("es-author");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("es-other");
		EdusharingUsage usage = createUsage(author, entry, "node");
		
		assertThat(isAllowed(other, new UserSession(), usage)).isFalse();
	}
	
	@Test
	public void shouldAllowAdministrator() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("es-admin");
		EdusharingUsage usage = createUsage(admin, entry, "node");
		
		assertThat(isAllowed(admin, new UserSession(), usage)).isTrue();
	}
	
	@Test
	public void shouldAllowCourseInfoForNonMember() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("es-author");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("es-other");
		EdusharingUsage usage = createUsage(author, entry, RepositoryEdusharingProvider.SUB_PATH_INFO);
		
		assertThat(isAllowed(other, new UserSession(), usage)).isTrue();
	}
	
	@Test
	public void shouldDenyCourseInfoOfUnpublishedCourse() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		entry = repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.preparation);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("es-author");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("es-other");
		EdusharingUsage usage = createUsage(author, entry, RepositoryEdusharingProvider.SUB_PATH_INFO);
		
		assertThat(isAllowed(other, new UserSession(), usage)).isFalse();
	}
	
	@Test
	public void shouldDenyMemberOfUnpublishedCourse() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		entry = repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.preparation);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("es-participant");
		repositoryService.addRole(participant, entry, GroupRoles.participant.name());
		EdusharingUsage usage = createUsage(participant, entry, "node");
		
		assertThat(isAllowed(participant, new UserSession(), usage)).isFalse();
	}
	
	@Test
	public void shouldCheckCourseOncePerSession() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("es-participant");
		repositoryService.addRole(participant, entry, GroupRoles.participant.name());
		EdusharingUsage usage1 = createUsage(participant, entry, "node1");
		EdusharingUsage usage2 = createUsage(participant, entry, "node2");
		UserSession usess = new UserSession();
		assertThat(isAllowed(participant, usess, usage1)).isTrue();
		
		repositoryService.removeRole(participant, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		assertThat(isAllowed(participant, usess, usage2)).isTrue();
		assertThat(isAllowed(participant, new UserSession(), usage2)).isFalse();
	}
	
	@Test
	public void shouldDenyOtherResources() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("es-admin");
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		EdusharingUsage usage = createUsage(admin, ores, "node");
		
		assertThat(isAllowed(admin, new UserSession(), usage)).isFalse();
	}
	
	private boolean isAllowed(Identity identity, UserSession usess, EdusharingUsage usage) {
		return edusharingDispatcher.isAllowed(identity, securityManager.getRoles(identity), usess, usage);
	}
	
	private EdusharingUsage createUsage(Identity identity, OLATResourceable ores, String subPath) {
		EdusharingUsageImpl usage = new EdusharingUsageImpl();
		usage.setCreationDate(new Date());
		usage.setLastModified(usage.getCreationDate());
		usage.setIdentifier(random());
		usage.setResName(ores.getResourceableTypeName());
		usage.setResId(ores.getResourceableId());
		usage.setSubPath(subPath);
		usage.setObjectUrl(random());
		usage.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(usage);
		dbInstance.commitAndCloseSession();
		return usage;
	}
	
	private String random() {
		return UUID.randomUUID().toString();
	}
	
}
