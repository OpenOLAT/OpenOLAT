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
package org.olat.modules.forms.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.manager.BinderDAO;
import org.olat.modules.portfolio.manager.PageDAO;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormSessionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private EvaluationFormSessionDAO evaluationFormSessionDao;
	
	@Test
	public void shouldCreateSession() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		String subIdent = "sub";
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		RepositoryEntry formEntry = createFormEntry(UUID.randomUUID().toString());
		
		EvaluationFormSession session = evaluationFormSessionDao.createSession(ores, subIdent, identity, formEntry);
		
		assertThat(session).isNotNull();
		assertThat(session.getKey()).isNotNull();
		assertThat(session.getCreationDate()).isNotNull();
		assertThat(session.getLastModified()).isNotNull();
		assertThat(session.getIdentity()).isEqualTo(identity);
	}
	
	@Test
	public void shouldCreateAndLoadSessionWithSubIdent() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		String subIdent = "sub";
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		RepositoryEntry formEntry = createFormEntry(UUID.randomUUID().toString());
		
		EvaluationFormSession session = evaluationFormSessionDao.createSession(ores, subIdent, identity, formEntry);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSession loadedSession = evaluationFormSessionDao.loadSession(ores, subIdent, identity);
		
		assertThat(loadedSession).isNotNull();
		assertThat(loadedSession).isEqualTo(session);
	}
	
	@Test
	public void shouldCreateAndLoadSessionWithoutSubIdent() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		RepositoryEntry formEntry = createFormEntry(UUID.randomUUID().toString());
		
		EvaluationFormSession session = evaluationFormSessionDao.createSession(ores, null, identity, formEntry);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSession loadedSession = evaluationFormSessionDao.loadSession(ores, null, identity);
		
		assertThat(loadedSession).isNotNull();
		assertThat(loadedSession).isEqualTo(session);
	}
	
	@Test
	public void shouldCheckIfHasSessions() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		RepositoryEntry formEntry = createFormEntry(UUID.randomUUID().toString());
		evaluationFormSessionDao.createSession(ores, null, identity, formEntry);
		dbInstance.commitAndCloseSession();
		
		boolean hasSessions = evaluationFormSessionDao.hasSessions(ores, null);
		
		assertThat(hasSessions).isTrue();
	}
	
	@Test
	public void shouldCheckIfHasNoSessions() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		
		boolean hasSessions = evaluationFormSessionDao.hasSessions(ores, "subIdent");
		
		assertThat(hasSessions).isFalse();
	}

	
	@Test
	public void createSessionForPortfolio() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("eva-1");
		
		BinderImpl binder = binderDao.createAndPersist("Binder evaluation 1", "A binder with an evaluation", null, null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commit();
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist("Page 1", "A page with an evalutation.", null, null, true, reloadedSection, null);
		dbInstance.commit();
		RepositoryEntry formEntry = createFormEntry("Eva. form for session");

		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		EvaluationFormSession session = evaluationFormSessionDao.createSessionForPortfolio(id, reloadedBody, formEntry);
		dbInstance.commit();
		
		Assert.assertNotNull(session);
		Assert.assertNotNull(session.getKey());
		Assert.assertNotNull(session.getCreationDate());
		Assert.assertNotNull(session.getLastModified());
		Assert.assertEquals(reloadedBody, session.getPageBody());
		Assert.assertEquals(id, session.getIdentity());	
	}
	
	private RepositoryEntry createFormEntry(String displayname) {
		EvaluationFormResource ores = new EvaluationFormResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("eva-form-author");
		RepositoryEntry re = repositoryService.create(author, null, "", displayname, "Description", resource, RepositoryEntry.ACC_OWNERS);
		dbInstance.commit();
		return re;
	}

}
