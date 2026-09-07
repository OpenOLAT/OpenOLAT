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
package org.olat.resource.accesscontrol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.createRandomResource;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.manager.EvaluationFormTestsHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.manager.ACOfferSurveyDAO;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 1 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ACOfferSurveyDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACFrontendManager acFrontendManager;
	@Autowired
	private EvaluationFormTestsHelper evaluationFormTestsHelper;

	@Autowired
	private ACOfferSurveyDAO sut;

	@Test
	public void shouldCreateSurvey() {
		OLATResource resource = createRandomResource();
		RepositoryEntry formEntry = evaluationFormTestsHelper.createFormEntry();
		dbInstance.commitAndCloseSession();

		EvaluationFormSurvey survey = sut.createSurvey(resource, formEntry, "Step 1");
		dbInstance.commitAndCloseSession();

		assertThat(survey.getKey()).isNotNull();
		assertThat(survey.getFormEntry()).isEqualTo(formEntry);
		assertThat(survey.getDisplayName()).isEqualTo("Step 1");
	}

	@Test
	public void shouldLoadSurveysByResource() {
		OLATResource resource1 = createRandomResource();
		OLATResource resource2 = createRandomResource();
		RepositoryEntry formEntry = evaluationFormTestsHelper.createFormEntry();
		dbInstance.commitAndCloseSession();
		EvaluationFormSurvey survey11 = sut.createSurvey(resource1, formEntry, "Step 1");
		EvaluationFormSurvey survey12 = sut.createSurvey(resource1, formEntry, "Step 2");
		sut.createSurvey(resource2, formEntry, "Step 1");
		dbInstance.commitAndCloseSession();

		List<EvaluationFormSurvey> surveys = sut.loadSurveys(resource1);

		assertThat(surveys).containsExactlyInAnyOrder(survey11, survey12);
	}

	@Test
	public void shouldDeleteSurveyAndItsOfferToSurveys() {
		OLATResource resource = createRandomResource();
		RepositoryEntry formEntry = evaluationFormTestsHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(resource, formEntry, "Step 1");
		Offer offer = acFrontendManager.createOffer(createRandomResource(), random());
		offer = acFrontendManager.save(offer);
		sut.createOfferToSurvey(offer, survey, 1);
		dbInstance.commitAndCloseSession();

		sut.deleteSurvey(survey);
		dbInstance.commitAndCloseSession();

		assertThat(sut.loadOfferToSurveys(offer)).isEmpty();
		assertThat(sut.loadSurveys(resource)).isEmpty();
	}

	@Test
	public void shouldCreateAndLoadOfferToSurveysByOffer() {
		OLATResource resource = createRandomResource();
		RepositoryEntry formEntry = evaluationFormTestsHelper.createFormEntry();
		EvaluationFormSurvey survey1 = sut.createSurvey(resource, formEntry, "Step 1");
		EvaluationFormSurvey survey2 = sut.createSurvey(resource, formEntry, "Step 2");
		Offer offer = acFrontendManager.createOffer(createRandomResource(), random());
		offer = acFrontendManager.save(offer);
		dbInstance.commitAndCloseSession();

		OfferToSurvey offerToSurvey1 = sut.createOfferToSurvey(offer, survey1, 1);
		OfferToSurvey offerToSurvey2 = sut.createOfferToSurvey(offer, survey2, 2);
		dbInstance.commitAndCloseSession();

		List<OfferToSurvey> offerToSurveys = sut.loadOfferToSurveys(offer);

		assertThat(offerToSurveys).containsExactly(offerToSurvey1, offerToSurvey2);
	}

	@Test
	public void shouldLoadOfferToSurveysBySurvey() {
		OLATResource resource = createRandomResource();
		RepositoryEntry formEntry = evaluationFormTestsHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(resource, formEntry, "Step 1");
		Offer offer1 = acFrontendManager.createOffer(createRandomResource(), random());
		offer1 = acFrontendManager.save(offer1);
		Offer offer2 = acFrontendManager.createOffer(createRandomResource(), random());
		offer2 = acFrontendManager.save(offer2);
		dbInstance.commitAndCloseSession();

		OfferToSurvey offerToSurvey1 = sut.createOfferToSurvey(offer1, survey, 1);
		OfferToSurvey offerToSurvey2 = sut.createOfferToSurvey(offer2, survey, 1);
		dbInstance.commitAndCloseSession();

		assertThat(sut.loadOfferToSurveys(survey)).containsExactlyInAnyOrder(offerToSurvey1, offerToSurvey2);
	}

	@Test
	public void shouldUpdateOfferToSurveyPos() {
		OLATResource resource = createRandomResource();
		RepositoryEntry formEntry = evaluationFormTestsHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(resource, formEntry, "Step 1");
		Offer offer = acFrontendManager.createOffer(createRandomResource(), random());
		offer = acFrontendManager.save(offer);
		dbInstance.commitAndCloseSession();
		OfferToSurvey offerToSurvey = sut.createOfferToSurvey(offer, survey, 1);
		dbInstance.commitAndCloseSession();

		offerToSurvey.setPos(5);
		offerToSurvey = sut.updateOfferToSurvey(offerToSurvey);
		dbInstance.commitAndCloseSession();

		assertThat(sut.loadOfferToSurveys(offer).get(0).getPos()).isEqualTo(5);
	}

	@Test
	public void shouldDeleteOfferToSurvey() {
		OLATResource resource = createRandomResource();
		RepositoryEntry formEntry = evaluationFormTestsHelper.createFormEntry();
		EvaluationFormSurvey survey = sut.createSurvey(resource, formEntry, "Step 1");
		Offer offer = acFrontendManager.createOffer(createRandomResource(), random());
		offer = acFrontendManager.save(offer);
		OfferToSurvey offerToSurvey = sut.createOfferToSurvey(offer, survey, 1);
		dbInstance.commitAndCloseSession();

		sut.deleteOfferToSurvey(offerToSurvey);
		dbInstance.commitAndCloseSession();

		assertThat(sut.loadOfferToSurveys(offer)).isEmpty();
		assertThat(sut.loadSurveys(resource)).containsExactly(survey);
	}

	@Test
	public void shouldTellIfSurveyIsUsed() {
		OLATResource resource = createRandomResource();
		RepositoryEntry formEntry = evaluationFormTestsHelper.createFormEntry();
		EvaluationFormSurvey usedSurvey = sut.createSurvey(resource, formEntry, "Step 1");
		EvaluationFormSurvey unusedSurvey = sut.createSurvey(resource, formEntry, "Step 2");
		Offer offer = acFrontendManager.createOffer(createRandomResource(), random());
		offer = acFrontendManager.save(offer);
		sut.createOfferToSurvey(offer, usedSurvey, 1);
		dbInstance.commitAndCloseSession();

		assertThat(sut.isSurveyUsed(usedSurvey)).isTrue();
		assertThat(sut.isSurveyUsed(unusedSurvey)).isFalse();
	}

}
