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

import static org.olat.modules.forms.EvaluationFormSurveyIdentifier.of;

import java.util.UUID;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 29.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class EvaluationFormTestsHelper {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public void deleteAll() {
		// quality management
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualityreportaccess")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualityreminder")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from contexttocurriculum")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from contexttocurriculumelement")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from contexttoorganisation")
					.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from contexttotaxonomylevel")
					.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualitycontext")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualitydatacollectiontoorganisation")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualitydatacollection")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualitygeneratorconfig")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualitygeneratortoorganisation")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualitygenerator")
				.executeUpdate();
		// evaluation forms
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from evaluationformresponse")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from evaluationformsession")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from evaluationformparticipation")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("update evaluationformsurvey s set s.seriesPrevious.key = null")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from evaluationformsurvey")
				.executeUpdate();
		dbInstance.commitAndCloseSession();
	}

	public RepositoryEntry createFormEntry() {
		EvaluationFormResource ores = new EvaluationFormResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser(UUID.randomUUID().toString());
		return repositoryService.create(author, null, "", "Display name", "Description", resource, RepositoryEntryStatusEnum.preparation, null);
	}
	
	public EvaluationFormSurvey createSurvey() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		String subIdent = UUID.randomUUID().toString();
		RepositoryEntry formEntry = createFormEntry();
		return evaluationFormManager.createSurvey(of(ores, subIdent), formEntry);
	}
	
	EvaluationFormParticipation createParticipation() {
		EvaluationFormSurvey survey = createSurvey();
		return createParticipation(survey, false);
	}
	
	EvaluationFormParticipation createParticipation(EvaluationFormSurvey survey, boolean withUser) {
		Identity identity = withUser? JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random()): null;
		return evaluationFormManager.createParticipation(survey, identity);
	}
	
	EvaluationFormSession createSession() {
		EvaluationFormParticipation participation = createParticipation();
		return createSession(participation);
	}
	
	public EvaluationFormSession createSession(EvaluationFormSurvey survey) {
		EvaluationFormParticipation participation = createParticipation(survey, false);
		return createSession(participation);
	}
	
	EvaluationFormSession createSession(EvaluationFormParticipation participation) {
		return evaluationFormManager.createSession(participation);
	}
	
	EvaluationFormResponse createResponse() {
		EvaluationFormSession session = createSession();
		return createResponse(session);
	}
	
	EvaluationFormResponse createResponse(EvaluationFormSession session) {
		String responseIdentifier = UUID.randomUUID().toString();
		String value = UUID.randomUUID().toString();
		return evaluationFormManager.createStringResponse(responseIdentifier, session, value);
	}
	
}
