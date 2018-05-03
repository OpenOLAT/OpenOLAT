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

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.forms.EvaluationFormResponse;
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
public class EvaluationFormResponseDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private EvaluationFormSessionDAO evaluationFormSessionDao;
	@Autowired
	private EvaluationFormResponseDAO evaluationFormResponseDao;
	
	
	@Test
	public void createResponseForPortfolio() {
		//prepare a test case with the binder up to the page body
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("eva-1");
		BinderImpl binder = binderDao.createAndPersist("Binder evaluation 1", "A binder with an evaluation", null, null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commit();
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist("Page 1", "A page with an evalutation.", null, null, true, reloadedSection, null);
		dbInstance.commit();
		RepositoryEntry formEntry = createFormEntry("Eva. form for responses");

		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		EvaluationFormSession session = evaluationFormSessionDao.createSessionForPortfolio(id, reloadedBody, formEntry);
		dbInstance.commit();
		
		//create a response
		String responseIdentifier = UUID.randomUUID().toString();
		BigDecimal numericalValue = new BigDecimal("2.2");
		String stringuifiedResponse = numericalValue.toPlainString();
		Path fileResponse = Paths.get("this", "is", "a", "path");
		EvaluationFormResponse response = evaluationFormResponseDao.createResponse(responseIdentifier,
				numericalValue, stringuifiedResponse, fileResponse, session);
		dbInstance.commit();

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getKey());
		Assert.assertNotNull(response.getCreationDate());
		Assert.assertNotNull(response.getLastModified());
		Assert.assertFalse(response.isNoResponse());
		Assert.assertEquals(session, response.getSession());
		Assert.assertEquals(numericalValue, response.getNumericalResponse());
		Assert.assertEquals(stringuifiedResponse, response.getStringuifiedResponse());
		Assert.assertEquals(fileResponse, response.getFileResponse());
		Assert.assertEquals(responseIdentifier, response.getResponseIdentifier());
	}
	
	@Test
	public void shouldUpdateResponse() {
		EvaluationFormSession session = createSession();
		EvaluationFormResponse response = createResponse(session);
		dbInstance.commit();
		
		BigDecimal numericalValue = new BigDecimal("3.3");
		String stringuifiedResponse = numericalValue.toPlainString();
		Path fileResponse = Paths.get("a", "b", "a", "c");
		EvaluationFormResponse updatedResponse = evaluationFormResponseDao.updateResponse(numericalValue, stringuifiedResponse, fileResponse, response);
		
		assertThat(updatedResponse.getNumericalResponse()).isEqualTo(numericalValue);
		assertThat(updatedResponse.getStringuifiedResponse()).isEqualTo(stringuifiedResponse);
		assertThat(updatedResponse.getFileResponse()).isEqualTo(fileResponse);
		assertThat(updatedResponse.isNoResponse()).isFalse();
	}

	@Test
	public void shouldCreateNoResponse() {
		EvaluationFormSession session = createSession();
		String responseIdentifier = UUID.randomUUID().toString();
		EvaluationFormResponse response = evaluationFormResponseDao.createNoResponse(responseIdentifier, session);
		dbInstance.commit();
		
		assertThat(response.getCreationDate()).isNotNull();
		assertThat(response.getLastModified()).isNotNull();
		assertThat(response.getResponseIdentifier()).isEqualTo(responseIdentifier);
		assertThat(response.getSession()).isEqualTo(session);
		assertThat(response.isNoResponse()).isTrue();
		assertThat(response.getNumericalResponse()).isNull();
		assertThat(response.getStringuifiedResponse()).isNull();
		assertThat(response.getFileResponse()).isNull();
	}
	
	@Test
	public void shouldUpdateNoResponse() {
		EvaluationFormSession session = createSession();
		EvaluationFormResponse initialResponse = createResponse(session);
		dbInstance.commit();

		EvaluationFormResponse response = evaluationFormResponseDao.updateNoResponse(initialResponse);

		assertThat(response.isNoResponse()).isTrue();
		assertThat(response.getNumericalResponse()).isNull();
		assertThat(response.getStringuifiedResponse()).isNull();
		assertThat(response.getFileResponse()).isNull();
	}
	
	@Test
	public void shouldDeleteResponses() {
		EvaluationFormSession session = createSession();
		String responseIdentifier = UUID.randomUUID().toString();
		createResponse(session, responseIdentifier);
		createResponse(session, responseIdentifier);
		createResponse(session, responseIdentifier);
		dbInstance.commit();
		
		List<EvaluationFormResponse> responses = evaluationFormResponseDao.loadResponses(responseIdentifier, session);
		assertThat(responses).hasSize(3);
		
		List<Long> keys = responses.stream().map(EvaluationFormResponse::getKey).collect(Collectors.toList());
		evaluationFormResponseDao.deleteResponses(keys);
		dbInstance.commit();
		
		
		responses = evaluationFormResponseDao.loadResponses(responseIdentifier, session);
		assertThat(responses).hasSize(0);
	}

	private EvaluationFormResponse createResponse(EvaluationFormSession session) {
		String responseIdentifier = UUID.randomUUID().toString();
		return createResponse(session, responseIdentifier);
	}

	private EvaluationFormResponse createResponse(EvaluationFormSession session, String responseIdentifier) {
		BigDecimal numericalValue = new BigDecimal("2.2");
		String stringuifiedResponse = numericalValue.toPlainString();
		Path fileResponse = Paths.get("this", "is", "a", "path");
		return evaluationFormResponseDao.createResponse(responseIdentifier, numericalValue, stringuifiedResponse,
				fileResponse, session);
	}

	private EvaluationFormSession createSession() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("eva-resp");
		RepositoryEntry formEntry = createFormEntry(UUID.randomUUID().toString());
		EvaluationFormSession session = evaluationFormSessionDao.createSessionForPortfolio(id, null, formEntry);
		return session;
	}
	
	private RepositoryEntry createFormEntry(String displayname) {
		EvaluationFormResource ores = new EvaluationFormResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("eva-form-author");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(author, null, "", displayname, "Description", resource, RepositoryEntry.ACC_OWNERS, defOrganisation);
		dbInstance.commit();
		return re;
	}
}
