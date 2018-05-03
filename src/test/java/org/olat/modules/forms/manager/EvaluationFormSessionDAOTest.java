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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.manager.BinderDAO;
import org.olat.modules.portfolio.manager.PageDAO;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.repository.RepositoryEntry;
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
	private EvaluationFormSessionDAO evaluationFormSessionDao;
	@Autowired
	private EvaluationFormTestsHelper evaTestHelper;
	
	@Before
	public void cleanUp() {
		evaTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateSession() {
		EvaluationFormParticipation participation = evaTestHelper.createParticipation();
		dbInstance.commit();
		
		EvaluationFormSession session = evaluationFormSessionDao.createSession(participation);
		dbInstance.commit();
		
		assertThat(session).isNotNull();
		assertThat(session.getKey()).isNotNull();
		assertThat(session.getCreationDate()).isNotNull();
		assertThat(session.getLastModified()).isNotNull();
		assertThat(session.getParticipation()).isEqualTo(participation);
		assertThat(session.getSurvey()).isEqualTo(participation.getSurvey());
	}
	
	@Test
	public void shouldMakeSessionAnonymous() {
		EvaluationFormParticipation participation = evaTestHelper.createParticipation();
		EvaluationFormSession session = evaluationFormSessionDao.createSession(participation);
		dbInstance.commit();
		
		EvaluationFormSession anonymousSession = evaluationFormSessionDao.makeAnonymous(session);
		
		assertThat(anonymousSession.getParticipation()).isNull();
	}

	
	@Test
	public void shouldLoadByParticipation() {
		EvaluationFormParticipation participation = evaTestHelper.createParticipation();
		EvaluationFormSession session = evaluationFormSessionDao.createSession(participation);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormSession loadedSession = evaluationFormSessionDao.loadSessionByParticipation(participation);
		
		assertThat(loadedSession).isNotNull();
		assertThat(loadedSession).isEqualTo(session);
	}
	
	@Test
	public void shouldCheckIfHasSessions() {
		EvaluationFormParticipation participation = evaTestHelper.createParticipation();
		EvaluationFormSurvey survey = participation.getSurvey();
		evaluationFormSessionDao.createSession(participation);
		dbInstance.commitAndCloseSession();
		
		boolean hasSessions = evaluationFormSessionDao.hasSessions(survey);
		
		assertThat(hasSessions).isTrue();
	}
	
	@Test
	public void shouldCheckIfHasNoSessions() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		dbInstance.commit();
		
		boolean hasSessions = evaluationFormSessionDao.hasSessions(survey);
		
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
		RepositoryEntry formEntry = evaTestHelper.createFormEntry();

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
	
}
